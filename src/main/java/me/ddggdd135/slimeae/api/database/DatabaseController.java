package me.ddggdd135.slimeae.api.database;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.api.wrappers.CraftHikariDataSource;
import me.ddggdd135.slimeae.utils.ReflectionUtils;

public abstract class DatabaseController<TData> {
    protected BlockDataController blockDataController;
    protected IDataSourceAdapter<?> adapter;
    protected CraftHikariDataSource ds;
    protected final DatabaseThreadFactory threadFactory = new DatabaseThreadFactory("AE-Database-Thread");
    protected final Class<TData> clazz;
    protected ExecutorService readExecutor;
    protected ExecutorService writeExecutor;
    protected ExecutorService callbackExecutor;
    protected final Map<TData, Queue<Runnable>> scheduledWriteTasks;
    protected final AtomicInteger activeWriteTasks = new AtomicInteger(0);
    protected final Logger logger;

    public DatabaseController(Class<TData> clazz) {
        this.clazz = clazz;
        scheduledWriteTasks = new ConcurrentHashMap<>();
        logger = Logger.getLogger("AE-Data-Controller");
    }

    public abstract String getTableName();

    @OverridingMethodsMustInvokeSuper
    public void init() {
        blockDataController = Slimefun.getDatabaseManager().getBlockDataController();
        adapter = ReflectionUtils.getField(blockDataController, "dataAdapter");
        ds = new CraftHikariDataSource(ReflectionUtils.<Object>getField(adapter, "ds"));
        readExecutor = Executors.newFixedThreadPool(2, threadFactory);
        writeExecutor = Executors.newFixedThreadPool(3, threadFactory);
        callbackExecutor = Executors.newCachedThreadPool(threadFactory);
    }

    @OverridingMethodsMustInvokeSuper
    public void shutdown() {
        readExecutor.shutdownNow();
        callbackExecutor.shutdownNow();

        writeExecutor.shutdown();
        try {
            int waitSeconds = 0;
            while (scheduledWriteTasks.size() > 0 || activeWriteTasks.get() > 0) {
                int pending = scheduledWriteTasks.size();
                int active = activeWriteTasks.get();
                logger.log(Level.INFO, "数据保存中，请稍候... 排队 {0} 个，执行中 {1} 个", new Object[] {pending, active});
                TimeUnit.SECONDS.sleep(1);
                waitSeconds++;
                if (waitSeconds >= 120) {
                    logger.log(Level.WARNING, "数据保存等待超时（120秒），强制关闭。排队 {0}，执行中 {1}", new Object[] {
                        scheduledWriteTasks.size(), activeWriteTasks.get()
                    });
                    break;
                }
            }

            if (!writeExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warning("写入线程池在30秒内未能完全终止，强制关闭。");
                writeExecutor.shutdownNow();
            }

            logger.info("数据保存完成.");
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "数据保存被中断: ", e);
            writeExecutor.shutdownNow();
        }
    }

    public void executeSql(String sql) {
        ReflectionUtils.invokePrivateMethod(adapter, "executeSql", new Class[] {String.class}, sql);
    }

    public List<Map<String, String>> execQuery(String sql) {
        try (Connection conn = ds.getConnection()) {
            return execQuery(conn, sql);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "An exception thrown while executing sql: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    public List<Map<String, String>> execQuery(Connection conn, String sql) throws SQLException {
        try (var stmt = conn.createStatement()) {
            try (var result = stmt.executeQuery(sql)) {
                List<Map<String, String>> re = new ArrayList<>();
                while (result.next()) {
                    Map<String, String> data = new HashMap<>();
                    ResultSetMetaData metaData = result.getMetaData();
                    for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                        String name = metaData.getColumnName(i);
                        data.put(name, result.getString(i));
                    }

                    re.add(data);
                }
                return re;
            }
        }
    }

    public abstract void update(TData data);

    public void delete() {
        executeSql("DELETE FROM " + getTableName());
    }

    public void submitWriteTask(TData data, Runnable runnable) {
        Queue<Runnable> queue;
        synchronized (scheduledWriteTasks) {
            if (scheduledWriteTasks.containsKey(data)) {
                queue = scheduledWriteTasks.get(data);
                queue.add(runnable);
            } else {
                queue = new ConcurrentLinkedQueue<>();
                queue.add(runnable);
                scheduledWriteTasks.put(data, queue);
            }
        }

        writeExecutor.submit(() -> {
            Queue<Runnable> tasks;
            synchronized (scheduledWriteTasks) {
                tasks = scheduledWriteTasks.remove(data);
            }
            if (tasks == null) return;
            activeWriteTasks.incrementAndGet();
            try {
                while (!tasks.isEmpty()) {
                    Runnable next = tasks.remove();
                    try {
                        next.run();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "写入任务执行异常: " + e.getMessage(), e);
                    }
                }
            } finally {
                activeWriteTasks.decrementAndGet();
            }
        });
    }

    public void cancelWriteTask(TData data) {
        scheduledWriteTasks.remove(data);
    }
}
