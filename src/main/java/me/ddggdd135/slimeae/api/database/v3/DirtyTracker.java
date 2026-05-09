package me.ddggdd135.slimeae.api.database.v3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;

public class DirtyTracker {
    private final AtomicReference<ConcurrentHashMap<UUID, ConcurrentHashMap<Long, DirtyEntry>>> dirtyMapRef =
            new AtomicReference<>(new ConcurrentHashMap<>());
    private final AtomicLong fallbackSequence = new AtomicLong();
    private volatile Map<UUID, Map<Long, DirtyEntry>> pendingFlush = null;
    private final ReentrantLock flushLock = new ReentrantLock();

    public record DirtyEntry(char op, long newAmount, long timestamp, long sequence) {}

    public void record(@Nonnull UUID cellUUID, long tplId, long newAmount, char op) {
        record(cellUUID, tplId, newAmount, op, fallbackSequence.incrementAndGet());
    }

    public void record(@Nonnull UUID cellUUID, long tplId, long newAmount, char op, long sequence) {
        DirtyEntry entry = new DirtyEntry(op, newAmount, System.currentTimeMillis(), sequence);
        dirtyMapRef
                .get()
                .computeIfAbsent(cellUUID, k -> new ConcurrentHashMap<>())
                .merge(tplId, entry, DirtyTracker::newerEntry);
    }

    public void recordDeleteCell(@Nonnull UUID cellUUID) {
        ConcurrentHashMap<Long, DirtyEntry> cellMap = new ConcurrentHashMap<>();
        cellMap.put(-1L, new DirtyEntry('D', 0, System.currentTimeMillis(), fallbackSequence.incrementAndGet()));
        dirtyMapRef.get().put(cellUUID, cellMap);
    }

    public List<JournalRow> drainPhase1() {
        flushLock.lock();
        try {
            ConcurrentHashMap<UUID, ConcurrentHashMap<Long, DirtyEntry>> dirtyMap =
                    dirtyMapRef.getAndSet(new ConcurrentHashMap<>());
            if (dirtyMap.isEmpty()) return Collections.emptyList();
            Map<UUID, Map<Long, DirtyEntry>> snapshot = new HashMap<>();
            for (var entry : dirtyMap.entrySet()) {
                snapshot.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }
            pendingFlush = snapshot;
            return toJournalRows(snapshot);
        } finally {
            flushLock.unlock();
        }
    }

    public void commitFlush() {
        flushLock.lock();
        try {
            pendingFlush = null;
        } finally {
            flushLock.unlock();
        }
    }

    public void rollbackFlush() {
        flushLock.lock();
        try {
            if (pendingFlush == null) return;
            for (var cellEntry : pendingFlush.entrySet()) {
                UUID cellUUID = cellEntry.getKey();
                ConcurrentHashMap<Long, DirtyEntry> cellMap =
                        dirtyMapRef.get().computeIfAbsent(cellUUID, k -> new ConcurrentHashMap<>());
                for (var itemEntry : cellEntry.getValue().entrySet()) {
                    cellMap.merge(itemEntry.getKey(), itemEntry.getValue(), DirtyTracker::newerEntry);
                }
            }
            pendingFlush = null;
        } finally {
            flushLock.unlock();
        }
    }

    public boolean hasPendingChanges(UUID cellUUID) {
        return dirtyMapRef.get().containsKey(cellUUID);
    }

    public List<JournalRow> drainCell(UUID cellUUID) {
        flushLock.lock();
        try {
            ConcurrentHashMap<Long, DirtyEntry> cellMap = dirtyMapRef.get().remove(cellUUID);
            if (cellMap == null) return Collections.emptyList();
            return toJournalRows(Map.of(cellUUID, new HashMap<>(cellMap)));
        } finally {
            flushLock.unlock();
        }
    }

    private static DirtyEntry newerEntry(DirtyEntry a, DirtyEntry b) {
        if (a.sequence() != b.sequence()) return a.sequence() > b.sequence() ? a : b;
        return a.timestamp() >= b.timestamp() ? a : b;
    }

    private List<JournalRow> toJournalRows(Map<UUID, Map<Long, DirtyEntry>> data) {
        List<JournalRow> rows = new ArrayList<>();
        for (var cellEntry : data.entrySet()) {
            String cellUuid = cellEntry.getKey().toString();
            for (var itemEntry : cellEntry.getValue().entrySet()) {
                DirtyEntry de = itemEntry.getValue();
                Long tplId = itemEntry.getKey() == -1L ? null : itemEntry.getKey();
                Long newAmount = (de.op() == 'D' || de.op() == 'R') ? null : de.newAmount();
                int crc = CRC32Utils.computeJournal(cellUuid, de.op(), tplId, newAmount);
                rows.add(new JournalRow(cellUuid, de.op(), tplId, newAmount, crc, de.timestamp()));
            }
        }
        return rows;
    }
}
