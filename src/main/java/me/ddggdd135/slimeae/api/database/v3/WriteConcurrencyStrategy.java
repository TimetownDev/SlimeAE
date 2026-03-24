package me.ddggdd135.slimeae.api.database.v3;

import java.util.concurrent.TimeUnit;

public interface WriteConcurrencyStrategy {
    boolean acquireJournalWrite(long timeout, TimeUnit unit) throws InterruptedException;

    void releaseJournalWrite();

    boolean acquireCheckpoint(long timeout, TimeUnit unit) throws InterruptedException;

    void releaseCheckpoint();
}
