package me.ddggdd135.slimeae.api.database.v3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class DirtyTracker {
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<Long, DirtyEntry>> dirtyMap = new ConcurrentHashMap<>();
    private volatile Map<UUID, Map<Long, DirtyEntry>> pendingFlush = null;

    public record DirtyEntry(char op, long newAmount, long timestamp) {}

    public void record(@Nonnull UUID cellUUID, long tplId, long newAmount, char op) {
        dirtyMap.computeIfAbsent(cellUUID, k -> new ConcurrentHashMap<>())
                .put(tplId, new DirtyEntry(op, newAmount, System.currentTimeMillis()));
    }

    public void recordDeleteCell(@Nonnull UUID cellUUID) {
        ConcurrentHashMap<Long, DirtyEntry> cellMap = new ConcurrentHashMap<>();
        cellMap.put(-1L, new DirtyEntry('D', 0, System.currentTimeMillis()));
        dirtyMap.put(cellUUID, cellMap);
    }

    public List<JournalRow> drainPhase1() {
        Map<UUID, Map<Long, DirtyEntry>> snapshot;
        synchronized (this) {
            if (dirtyMap.isEmpty()) return Collections.emptyList();
            snapshot = new HashMap<>();
            for (var entry : dirtyMap.entrySet()) {
                snapshot.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }
            dirtyMap.clear();
            pendingFlush = snapshot;
        }
        return toJournalRows(snapshot);
    }

    public void commitFlush() {
        synchronized (this) {
            pendingFlush = null;
        }
    }

    public void rollbackFlush() {
        synchronized (this) {
            if (pendingFlush == null) return;
            for (var cellEntry : pendingFlush.entrySet()) {
                UUID cellUUID = cellEntry.getKey();
                ConcurrentHashMap<Long, DirtyEntry> cellMap =
                        dirtyMap.computeIfAbsent(cellUUID, k -> new ConcurrentHashMap<>());
                for (var itemEntry : cellEntry.getValue().entrySet()) {
                    cellMap.merge(
                            itemEntry.getKey(),
                            itemEntry.getValue(),
                            (existing, pending) -> existing.timestamp() >= pending.timestamp() ? existing : pending);
                }
            }
            pendingFlush = null;
        }
    }

    public boolean hasPendingChanges(UUID cellUUID) {
        return dirtyMap.containsKey(cellUUID);
    }

    public List<JournalRow> drainCell(UUID cellUUID) {
        synchronized (this) {
            ConcurrentHashMap<Long, DirtyEntry> cellMap = dirtyMap.remove(cellUUID);
            if (cellMap == null) return Collections.emptyList();
            return toJournalRows(Map.of(cellUUID, new HashMap<>(cellMap)));
        }
    }

    private List<JournalRow> toJournalRows(Map<UUID, Map<Long, DirtyEntry>> data) {
        List<JournalRow> rows = new ArrayList<>();
        for (var cellEntry : data.entrySet()) {
            String cellUuid = cellEntry.getKey().toString();
            for (var itemEntry : cellEntry.getValue().entrySet()) {
                DirtyEntry de = itemEntry.getValue();
                Long tplId = itemEntry.getKey() == -1L ? null : itemEntry.getKey();
                Long newAmount = de.op() == 'D' ? null : de.newAmount();
                int crc = CRC32Utils.computeJournal(cellUuid, de.op(), tplId, newAmount);
                rows.add(new JournalRow(cellUuid, de.op(), tplId, newAmount, crc, de.timestamp()));
            }
        }
        return rows;
    }
}
