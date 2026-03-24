package me.ddggdd135.slimeae.api.database.v3;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class MysqlWriteStrategy implements WriteConcurrencyStrategy {
    private final Semaphore journalSemaphore = new Semaphore(3);
    private final ReentrantLock checkpointLock = new ReentrantLock();

    @Override
    public boolean acquireJournalWrite(long timeout, TimeUnit unit) throws InterruptedException {
        return journalSemaphore.tryAcquire(timeout, unit);
    }

    @Override
    public void releaseJournalWrite() {
        journalSemaphore.release();
    }

    @Override
    public boolean acquireCheckpoint(long timeout, TimeUnit unit) throws InterruptedException {
        return checkpointLock.tryLock(timeout, unit);
    }

    @Override
    public void releaseCheckpoint() {
        checkpointLock.unlock();
    }
}
