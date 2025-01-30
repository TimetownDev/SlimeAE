package me.ddggdd135.slimeae.api.database;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.zaxxer.hikari.HikariDataSource;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.utils.ReflectionUtils;

public abstract class DatabaseController<TData> {
    protected BlockDataController blockDataController;
    protected IDataSourceAdapter<?> adapter;
    protected HikariDataSource ds;
    protected final DatabaseThreadFactory threadFactory = new DatabaseThreadFactory();
    protected final Class<TData> clazz;
    protected ExecutorService readExecutor;
    protected ExecutorService writeExecutor;
    protected ExecutorService callbackExecutor;
    protected final Map<TData, Queue<Runnable>> scheduledWriteTasks;
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
        ds = ReflectionUtils.getField(adapter, "ds");
        readExecutor = Executors.newFixedThreadPool(3, threadFactory);
        writeExecutor = Executors.newFixedThreadPool(5, threadFactory);
        callbackExecutor = Executors.newCachedThreadPool(threadFactory);
    }

    @OverridingMethodsMustInvokeSuper
    public void shutdown() {
        readExecutor.shutdownNow();
        callbackExecutor.shutdownNow();
        try {
            float totalTask = scheduledWriteTasks.size();
            var pendingTask = scheduledWriteTasks.size();
            while (pendingTask > 0) {
                var doneTaskPercent = String.format("%.1f", (totalTask - pendingTask) / totalTask * 100);
                logger.log(Level.INFO, "数据保存中，请稍候... 剩余 {0} 个任务 ({1}%)", new Object[] {pendingTask, doneTaskPercent});
                TimeUnit.SECONDS.sleep(1);
                pendingTask = scheduledWriteTasks.size();
            }

            logger.info("数据保存完成.");
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Exception thrown while saving data: ", e);
        }
        writeExecutor.shutdownNow();
    }

    public void executeSql(String sql) {
        ReflectionUtils.invokePrivateMethod(adapter, "executeSql", new Class[] {String.class}, sql);
    }

    public List<Map<String, String>> execQuery(String sql) {
        try(Connection conn = ds.getConnection()) {
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
                writeExecutor.submit(() -> {
                    Queue<Runnable> tasks;
                    synchronized (scheduledWriteTasks) {
                        tasks = scheduledWriteTasks.remove(data);
                    }
                    while (!tasks.isEmpty()) {
                        Runnable next = tasks.remove();
                        try {
                            next.run();
                        } catch (Exception e) {
                            logger.log(Level.WARNING, e.getMessage());
                        }
                    }
                });
            }
        }
    }

    public void cancelWriteTask(TData data) {
        scheduledWriteTasks.remove(data);
    }
}
