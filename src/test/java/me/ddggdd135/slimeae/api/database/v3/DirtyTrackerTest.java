package me.ddggdd135.slimeae.api.database.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DirtyTrackerTest {

    @Test
    void newerSequenceWinsWhenOlderAmountArrivesLater() {
        DirtyTracker tracker = new DirtyTracker();
        UUID cell = UUID.randomUUID();

        tracker.record(cell, 7L, 260000L, 'P', 2L);
        tracker.record(cell, 7L, 4096L, 'P', 1L);

        List<JournalRow> rows = tracker.drainPhase1();

        assertEquals(1, rows.size());
        assertEquals(260000L, rows.get(0).newAmount());
    }

    @Test
    void rollbackKeepsNewerDirtyOverPendingFlush() {
        DirtyTracker tracker = new DirtyTracker();
        UUID cell = UUID.randomUUID();

        tracker.record(cell, 8L, 4096L, 'P', 1L);
        tracker.drainPhase1();
        tracker.record(cell, 8L, 260000L, 'P', 2L);
        tracker.rollbackFlush();

        List<JournalRow> rows = tracker.drainPhase1();

        assertEquals(1, rows.size());
        assertEquals(260000L, rows.get(0).newAmount());
    }
}
