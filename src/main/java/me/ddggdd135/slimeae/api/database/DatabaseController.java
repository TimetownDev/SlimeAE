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
    protected volatile boolean shutdownInProgress = false;

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
        shutdownInProgress = true;
        readExecutor.shutdownNow();
        callbackExecutor.shutdownNow();

        writeExecutor.shutdown();
        try {
            int waitSeconds = 0;
            while (activeWriteTasks.get() > 0) {
                logger.log(Level.INFO, "数据保存中... 队列中还有: {0} 个", activeWriteTasks.get());
                TimeUnit.SECONDS.sleep(1);
                if (++waitSeconds >= 300) {
                    logger.log(Level.WARNING, "写入任务超时 (300s), 剩余任务: {0}", activeWriteTasks.get());
                    break;
                }
            }

            if (!writeExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                writeExecutor.shutdownNow();
                writeExecutor.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "数据保存被中断", e);
            writeExecutor.shutdownNow();
        }

        drainScheduledTasks();
        logger.info("数据保存成功.");
    }

    private void drainScheduledTasks() {
        Map<TData, Queue<Runnable>> remaining;
        synchronized (scheduledWriteTasks) {
            remaining = new HashMap<>(scheduledWriteTasks);
            scheduledWriteTasks.clear();
        }
        if (remaining.isEmpty()) return;

        logger.log(Level.INFO, "正在处理 {0} 个剩余的计划写入任务", remaining.size());
        for (Queue<Runnable> tasks : remaining.values()) {
            Runnable task;
            while ((task = tasks.poll()) != null) {
                try {
                    task.run();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "同步写入失败: " + e.getMessage(), e);
                }
            }
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
        if (shutdownInProgress) {
            runSafely(runnable);
            return;
        }

        synchronized (scheduledWriteTasks) {
            scheduledWriteTasks
                    .computeIfAbsent(data, k -> new ConcurrentLinkedQueue<>())
                    .add(runnable);
        }

        try {
            writeExecutor.submit(() -> {
                Queue<Runnable> tasks;
                synchronized (scheduledWriteTasks) {
                    tasks = scheduledWriteTasks.remove(data);
                }
                if (tasks == null) return;
                activeWriteTasks.incrementAndGet();
                try {
                    Runnable next;
                    while ((next = tasks.poll()) != null) {
                        runSafely(next);
                    }
                } finally {
                    activeWriteTasks.decrementAndGet();
                }
            });
        } catch (RejectedExecutionException e) {
            logger.log(Level.WARNING, "写入任务被拒绝，正在同步执行");
            synchronized (scheduledWriteTasks) {
                Queue<Runnable> tasks = scheduledWriteTasks.remove(data);
                if (tasks != null) {
                    Runnable next;
                    while ((next = tasks.poll()) != null) {
                        runSafely(next);
                    }
                }
            }
        }
    }

    private void runSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            logger.log(Level.WARNING, "写入任务失败: " + e.getMessage(), e);
        }
    }

    public void cancelWriteTask(TData data) {
        scheduledWriteTasks.remove(data);
    }
}
