package me.ddggdd135.slimeae.api.database;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseThreadFactory implements ThreadFactory {
    private final AtomicInteger threadCount = new AtomicInteger(0);
    private String threadName = "SF-Database-Thread";
    public DatabaseThreadFactory() {}
    public DatabaseThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public int getThreadCount() {
        return threadCount.get();
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        return new Thread(r, threadName + threadCount.getAndIncrement());
    }
}
