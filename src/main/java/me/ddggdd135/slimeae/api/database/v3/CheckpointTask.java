package me.ddggdd135.slimeae.api.database.v3;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckpointTask implements Runnable {
    private static final Logger logger = Logger.getLogger("SlimeAE-Checkpoint");
    private final int checkpointBatchSize;
    private final long journalRetentionMs;
    private final long archiveRetentionMs;
    private final boolean archiveEnabled;
    private final int archiveMaxRows;

    private final ConnectionManager connMgr;
    private final WriteConcurrencyStrategy writeStrategy;
    private final DDLProvider ddl;

    public CheckpointTask(
            ConnectionManager connMgr, WriteConcurrencyStrategy writeStrategy, DDLProvider ddl, StorageConfig config) {
        this.connMgr = connMgr;
        this.writeStrategy = writeStrategy;
        this.ddl = ddl;
        this.checkpointBatchSize = 500;
        this.journalRetentionMs = config.getJournalRetentionMinutes() * 60L * 1000L;
        this.archiveRetentionMs = config.getArchiveRetentionDays() * 24L * 60L * 60L * 1000L;
        this.archiveEnabled = config.isArchiveEnabled();
        this.archiveMaxRows = config.getArchiveMaxRows();
    }

    @Override
    public void run() {
        try {
            doCheckpoint();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Checkpoint failed: " + e.getMessage(), e);
        }
    }

    public void doCheckpoint() {
        long maxJournalId = queryMaxPendingJournalId();
        if (maxJournalId < 0) return;

        boolean more = true;
        while (more) {
            try {
                if (!writeStrategy.acquireCheckpoint(5, TimeUnit.SECONDS)) {
                    logger.warning("Checkpoint acquire lock timeout, skipping");
                    return;
                }
                try {
                    int processed = doCheckpointBatch(maxJournalId);
                    more = processed >= checkpointBatchSize;
                } finally {
                    writeStrategy.releaseCheckpoint();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            Thread.yield();
        }

        if (archiveEnabled) {
            archiveAndCleanup();
        }
    }

    private int doCheckpointBatch(long maxJournalId) {
        try (Connection conn = connMgr.getWriteConnection()) {
            conn.setAutoCommit(false);
            try {
                List<JournalEntry> entries = loadPendingJournal(conn, maxJournalId);
                if (entries.isEmpty()) {
                    conn.commit();
                    return 0;
                }

                Map<String, Map<Long, JournalEntry>> grouped = groupByCell(entries);
                Set<String> updatedCells = new HashSet<>();

                for (var cellEntry : grouped.entrySet()) {
                    String cellUuid = cellEntry.getKey();
                    for (var itemEntry : cellEntry.getValue().entrySet()) {
                        JournalEntry je = itemEntry.getValue();
                        applyJournalEntry(conn, cellUuid, je);
                    }
                    updatedCells.add(cellUuid);
                }

                markApplied(conn, entries);

                for (String cellUuid : updatedCells) {
                    updateCellMetaStored(conn, cellUuid);
                }

                conn.commit();
                return entries.size();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Checkpoint batch error: " + e.getMessage(), e);
            return 0;
        }
    }

    private void applyJournalEntry(Connection conn, String cellUuid, JournalEntry je) throws SQLException {
        switch (je.op) {
            case 'P': {
                if (je.tplId == null || je.newAmount == null) break;
                String sql = ddl.upsertCellItem();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, cellUuid);
                    ps.setLong(2, je.tplId);
                    ps.setLong(3, je.newAmount);
                    ps.setInt(4, CRC32Utils.computeCellItem(cellUuid, je.tplId, je.newAmount));
                    ps.setLong(5, je.timestamp);
                    ps.executeUpdate();
                }
                if (je.newAmount == 0) {
                    try (PreparedStatement ps =
                            conn.prepareStatement("DELETE FROM ae_v3_cell_items WHERE cell_uuid = ? AND tpl_id = ?")) {
                        ps.setString(1, cellUuid);
                        ps.setLong(2, je.tplId);
                        ps.executeUpdate();
                    }
                }
                break;
            }
            case 'R': {
                if (je.tplId == null) break;
                try (PreparedStatement ps =
                        conn.prepareStatement("DELETE FROM ae_v3_cell_items WHERE cell_uuid = ? AND tpl_id = ?")) {
                    ps.setString(1, cellUuid);
                    ps.setLong(2, je.tplId);
                    ps.executeUpdate();
                }
                break;
            }
            case 'D': {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ae_v3_cell_items WHERE cell_uuid = ?")) {
                    ps.setString(1, cellUuid);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ae_v3_cell_meta WHERE cell_uuid = ?")) {
                    ps.setString(1, cellUuid);
                    ps.executeUpdate();
                }
                break;
            }
        }
    }

    private void updateCellMetaStored(Connection conn, String cellUuid) throws SQLException {
        long stored = 0;
        try (PreparedStatement ps =
                conn.prepareStatement("SELECT COALESCE(SUM(amount), 0) FROM ae_v3_cell_items WHERE cell_uuid = ?")) {
            ps.setString(1, cellUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stored = rs.getLong(1);
                }
            }
        }

        String snapshotHash = null;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            try (PreparedStatement ps = conn.prepareStatement("SELECT cell_uuid, tpl_id, amount FROM ae_v3_cell_items "
                    + "WHERE cell_uuid = ? ORDER BY tpl_id")) {
                ps.setString(1, cellUuid);
                try (ResultSet rs = ps.executeQuery()) {
                    StringBuilder digest = new StringBuilder();
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) digest.append('\n');
                        first = false;
                        digest.append(rs.getString("cell_uuid"))
                                .append('|')
                                .append(rs.getLong("tpl_id"))
                                .append('|')
                                .append(rs.getLong("amount"));
                    }
                    byte[] hash = sha256.digest(digest.toString().getBytes(StandardCharsets.UTF_8));
                    snapshotHash = bytesToHex(hash);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            logger.warning("SHA-256 not available, skipping snapshot_hash computation");
        }

        try (PreparedStatement upd = conn.prepareStatement(
                "UPDATE ae_v3_cell_meta SET `stored` = ?, snapshot_hash = ?, updated_at = ? WHERE cell_uuid = ?")) {
            upd.setLong(1, stored);
            upd.setString(2, snapshotHash);
            upd.setLong(3, System.currentTimeMillis());
            upd.setString(4, cellUuid);
            upd.executeUpdate();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private List<JournalEntry> loadPendingJournal(Connection conn, long maxJournalId) throws SQLException {
        String sql = "SELECT journal_id, cell_uuid, op, tpl_id, new_amount, crc32, timestamp "
                + "FROM ae_v3_journal WHERE applied = 0 AND journal_id <= ? ORDER BY journal_id LIMIT ?";
        List<JournalEntry> entries = new ArrayList<>();
        List<Long> corruptedIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maxJournalId);
            ps.setInt(2, checkpointBatchSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JournalEntry je = new JournalEntry();
                    je.journalId = rs.getLong("journal_id");
                    je.cellUuid = rs.getString("cell_uuid");
                    je.op = rs.getString("op").charAt(0);
                    long rawTplId = rs.getLong("tpl_id");
                    je.tplId = rs.wasNull() ? null : rawTplId;
                    long rawNewAmount = rs.getLong("new_amount");
                    je.newAmount = rs.wasNull() ? null : rawNewAmount;
                    je.crc32 = rs.getInt("crc32");
                    je.timestamp = rs.getLong("timestamp");

                    int expectedCrc = CRC32Utils.computeJournal(je.cellUuid, je.op, je.tplId, je.newAmount);
                    if (je.crc32 != expectedCrc) {
                        logger.warning("CRC32 mismatch on journal_id=" + je.journalId
                                + " (expected=" + expectedCrc + ", actual=" + je.crc32
                                + ", op=" + je.op + ", tplId=" + je.tplId + ", newAmount=" + je.newAmount
                                + "), skipping corrupted entry");
                        corruptedIds.add(je.journalId);
                        continue;
                    }

                    entries.add(je);
                }
            }
        }
        if (!corruptedIds.isEmpty()) {
            markCorruptedApplied(conn, corruptedIds);
        }
        return entries;
    }

    private void markCorruptedApplied(Connection conn, List<Long> journalIds) {
        try (PreparedStatement ps =
                conn.prepareStatement("UPDATE ae_v3_journal SET applied = 1 WHERE journal_id = ?")) {
            for (long id : journalIds) {
                ps.setLong(1, id);
                ps.addBatch();
            }
            ps.executeBatch();
            logger.info("Marked " + journalIds.size() + " corrupted journal entries as applied");
        } catch (SQLException e) {
            logger.warning("Failed to mark corrupted entries as applied: " + e.getMessage());
        }
    }

    private Map<String, Map<Long, JournalEntry>> groupByCell(List<JournalEntry> entries) {
        Map<String, Map<Long, JournalEntry>> grouped = new LinkedHashMap<>();
        for (JournalEntry je : entries) {
            grouped.computeIfAbsent(je.cellUuid, k -> new LinkedHashMap<>());
            long key = je.op == 'D' ? -1L : (je.tplId != null ? je.tplId : -1L);
            JournalEntry existing = grouped.get(je.cellUuid).get(key);
            if (existing == null || je.journalId > existing.journalId) {
                grouped.get(je.cellUuid).put(key, je);
            }
        }
        return grouped;
    }

    private void markApplied(Connection conn, List<JournalEntry> entries) throws SQLException {
        try (PreparedStatement ps =
                conn.prepareStatement("UPDATE ae_v3_journal SET applied = 1 WHERE journal_id = ?")) {
            for (JournalEntry je : entries) {
                ps.setLong(1, je.journalId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private long queryMaxPendingJournalId() {
        try (Connection conn = connMgr.getReadConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT MAX(journal_id) FROM ae_v3_journal WHERE applied = 0")) {
            if (rs.next()) {
                long val = rs.getLong(1);
                return rs.wasNull() ? -1 : val;
            }
        } catch (SQLException e) {
            logger.warning("Query max journal_id failed: " + e.getMessage());
        }
        return -1;
    }

    private void archiveAndCleanup() {
        long now = System.currentTimeMillis();
        long retentionCutoff = now - journalRetentionMs;
        long archiveCutoff = now - archiveRetentionMs;
        try (Connection conn = connMgr.getWriteConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO ae_v3_journal_archive (journal_id, cell_uuid, op, tpl_id, new_amount, crc32, timestamp) "
                            + "SELECT journal_id, cell_uuid, op, tpl_id, new_amount, crc32, timestamp "
                            + "FROM ae_v3_journal WHERE applied = 1 AND timestamp < ?")) {
                ps.setLong(1, retentionCutoff);
                ps.executeUpdate();
            }
            try (PreparedStatement ps =
                    conn.prepareStatement("DELETE FROM ae_v3_journal WHERE applied = 1 AND timestamp < ?")) {
                ps.setLong(1, retentionCutoff);
                ps.executeUpdate();
            }
            try (PreparedStatement ps =
                    conn.prepareStatement("DELETE FROM ae_v3_journal_archive WHERE timestamp < ?")) {
                ps.setLong(1, archiveCutoff);
                ps.executeUpdate();
            }
            if (archiveMaxRows > 0) {
                trimArchiveToMaxRows(conn);
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Archive/cleanup failed: " + e.getMessage(), e);
        }
    }

    private void trimArchiveToMaxRows(Connection conn) throws SQLException {
        long count = 0;
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ae_v3_journal_archive")) {
            if (rs.next()) {
                count = rs.getLong(1);
            }
        }
        if (count > archiveMaxRows) {
            long toDelete = count - archiveMaxRows;
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ae_v3_journal_archive WHERE journal_id IN ("
                    + "SELECT journal_id FROM ae_v3_journal_archive ORDER BY timestamp ASC LIMIT ?)")) {
                ps.setLong(1, toDelete);
                ps.executeUpdate();
                logger.info("Trimmed " + toDelete + " rows from journal archive (max-rows=" + archiveMaxRows + ")");
            }
        }
    }

    public void replayPendingJournal() {
        long maxId = queryMaxPendingJournalId();
        if (maxId < 0) {
            logger.info("No pending journal entries to replay");
            return;
        }
        logger.info("Replaying pending journal entries up to id=" + maxId);
        boolean more = true;
        while (more) {
            try {
                if (!writeStrategy.acquireCheckpoint(10, TimeUnit.SECONDS)) {
                    logger.warning("Replay acquire lock timeout");
                    return;
                }
                try {
                    int processed = doCheckpointBatch(maxId);
                    more = processed >= checkpointBatchSize;
                    if (processed > 0) {
                        logger.info("Replayed " + processed + " journal entries");
                    }
                } finally {
                    writeStrategy.releaseCheckpoint();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static class JournalEntry {
        long journalId;
        String cellUuid;
        char op;
        Long tplId;
        Long newAmount;
        int crc32;
        long timestamp;
    }
}
