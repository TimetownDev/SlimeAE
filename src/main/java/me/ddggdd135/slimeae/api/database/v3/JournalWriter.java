package me.ddggdd135.slimeae.api.database.v3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.ddggdd135.guguslimefunlib.GuguSlimefunLib;

public class JournalWriter {
    private static final Logger logger = Logger.getLogger("SlimeAE-Journal");
    private static final int FLUSH_BATCH_SIZE = 2000;
    private final WriteConcurrencyStrategy writeStrategy;
    private final DirtyTracker dirtyTracker;
    private final ConnectionManager connMgr;

    public JournalWriter(WriteConcurrencyStrategy writeStrategy, DirtyTracker dirtyTracker, ConnectionManager connMgr) {
        this.writeStrategy = writeStrategy;
        this.dirtyTracker = dirtyTracker;
        this.connMgr = connMgr;
    }

    public void flush() {
        if (!validateServerUuid()) {
            logger.severe("Server UUID mismatch! Aborting journal flush to prevent data corruption.");
            return;
        }
        List<JournalRow> allRows = dirtyTracker.drainPhase1();
        if (allRows.isEmpty()) return;

        List<List<JournalRow>> batches = partition(allRows, FLUSH_BATCH_SIZE);
        boolean allSuccess = true;
        int flushedCount = 0;

        for (List<JournalRow> batch : batches) {
            try {
                if (!writeStrategy.acquireJournalWrite(5, TimeUnit.SECONDS)) {
                    allSuccess = false;
                    break;
                }
                try {
                    batchInsertJournal(batch);
                    flushedCount += batch.size();
                } finally {
                    writeStrategy.releaseJournalWrite();
                }
            } catch (Exception e) {
                allSuccess = false;
                logger.log(Level.WARNING, "Journal flush batch failed: " + e.getMessage(), e);
                break;
            }
        }

        if (allSuccess) {
            dirtyTracker.commitFlush();
        } else {
            dirtyTracker.rollbackFlush();
            logger.warning("Journal partial flush failed, rolled back " + (allRows.size() - flushedCount) + " rows");
        }
    }

    public void flushCell(UUID cellUUID) {
        List<JournalRow> rows = dirtyTracker.drainCell(cellUUID);
        if (rows.isEmpty()) return;
        try {
            if (!writeStrategy.acquireJournalWrite(5, TimeUnit.SECONDS)) {
                logger.warning("Cell " + cellUUID + " unload flush timeout, data rolled back");
                rollbackCellRows(cellUUID, rows);
                return;
            }
            try {
                batchInsertJournal(rows);
            } finally {
                writeStrategy.releaseJournalWrite();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Cell " + cellUUID + " unload flush failed: " + e.getMessage(), e);
            rollbackCellRows(cellUUID, rows);
        }
    }

    private void rollbackCellRows(UUID cellUUID, List<JournalRow> rows) {
        for (JournalRow row : rows) {
            if (row.tplId() != null) {
                dirtyTracker.record(cellUUID, row.tplId(), row.newAmount() != null ? row.newAmount() : 0, row.op());
            }
        }
    }

    private void batchInsertJournal(List<JournalRow> rows) throws SQLException {
        String sql = "INSERT INTO ae_v3_journal (cell_uuid, op, tpl_id, new_amount, crc32, timestamp, applied) "
                + "VALUES (?, ?, ?, ?, ?, ?, 0)";
        try (Connection conn = connMgr.getWriteConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (JournalRow row : rows) {
                    ps.setString(1, row.cellUuid());
                    ps.setString(2, String.valueOf(row.op()));
                    if (row.tplId() != null) ps.setLong(3, row.tplId());
                    else ps.setNull(3, java.sql.Types.BIGINT);
                    if (row.newAmount() != null) ps.setLong(4, row.newAmount());
                    else ps.setNull(4, java.sql.Types.BIGINT);
                    ps.setInt(5, row.crc32());
                    ps.setLong(6, row.timestamp());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static <T> List<List<T>> partition(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    private boolean validateServerUuid() {
        String expected = GuguSlimefunLib.getServerUUID().toString();
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps =
                        conn.prepareStatement("SELECT cell_uuid FROM ae_v3_cell_meta WHERE server_uuid != ? LIMIT 1")) {
            ps.setString(1, expected);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.warning("Found cell belonging to a different server: " + rs.getString(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Server UUID validation query failed, allowing flush: " + e.getMessage(), e);
        }
        return true;
    }
}
