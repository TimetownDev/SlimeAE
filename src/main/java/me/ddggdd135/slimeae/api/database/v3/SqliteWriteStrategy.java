package me.ddggdd135.slimeae.api.database.v3;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SqliteWriteStrategy implements WriteConcurrencyStrategy {
    private final ReentrantLock globalWriteLock = new ReentrantLock();

    @Override
    public boolean acquireJournalWrite(long timeout, TimeUnit unit) throws InterruptedException {
        return globalWriteLock.tryLock(timeout, unit);
    }

    @Override
    public void releaseJournalWrite() {
        globalWriteLock.unlock();
    }

    @Override
    public boolean acquireCheckpoint(long timeout, TimeUnit unit) throws InterruptedException {
        return globalWriteLock.tryLock(timeout, unit);
    }

    @Override
    public void releaseCheckpoint() {
        globalWriteLock.unlock();
    }
}
