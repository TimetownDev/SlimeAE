package me.ddggdd135.slimeae.api.database.v3;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RepairTask {
    private static final Logger logger = Logger.getLogger("SlimeAE-Repair");
    private final ConnectionManager connMgr;
    private final DDLProvider ddl;
    private final boolean dryRun;
    private final int maxLevel;
    private final List<String> report = new ArrayList<>();

    public RepairTask(ConnectionManager connMgr, DDLProvider ddl, int maxLevel, boolean dryRun) {
        this.connMgr = connMgr;
        this.ddl = ddl;
        this.maxLevel = maxLevel;
        this.dryRun = dryRun;
    }

    public List<String> run() {
        report.clear();
        if (maxLevel >= 0) level0IntegrityCheck();
        if (maxLevel >= 1) level1TableStructure();
        if (maxLevel >= 2) level2RowCrc();
        if (maxLevel >= 3) level3LogicConsistency();
        if (maxLevel >= 4) level4OrphanReferences();
        return report;
    }

    private void level0IntegrityCheck() {
        if (connMgr.isMysql()) {
            report.add("[L0] Skipped: integrity_check not available for MySQL");
            return;
        }
        try (Connection conn = connMgr.getReadConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("PRAGMA integrity_check")) {
            if (rs.next()) {
                String result = rs.getString(1);
                if ("ok".equalsIgnoreCase(result)) {
                    report.add("[L0] PASS: Database integrity check OK");
                } else {
                    report.add("[L0] FAIL: " + result);
                }
            }
        } catch (SQLException e) {
            report.add("[L0] ERROR: " + e.getMessage());
        }
    }

    private void level1TableStructure() {
        String[] tables = {
            "ae_v3_schema_info",
            "ae_v3_item_templates",
            "ae_v3_cell_meta",
            "ae_v3_cell_items",
            "ae_v3_journal",
            "ae_v3_journal_archive",
            "ae_v3_reskin_data"
        };
        int missing = 0;
        for (String table : tables) {
            try (Connection conn = connMgr.getReadConnection();
                    ResultSet rs = conn.getMetaData().getTables(null, null, table, null)) {
                if (!rs.next()) {
                    report.add("[L1] MISSING TABLE: " + table);
                    missing++;
                    if (!dryRun) {
                        rebuildTable(table);
                        report.add("[L1] REBUILT: " + table);
                    }
                }
            } catch (SQLException e) {
                report.add("[L1] ERROR checking " + table + ": " + e.getMessage());
            }
        }
        if (missing == 0) {
            report.add("[L1] PASS: All tables present");
        }
    }

    private void rebuildTable(String table) {
        try (Connection conn = connMgr.getWriteConnection();
                Statement stmt = conn.createStatement()) {
            switch (table) {
                case "ae_v3_schema_info":
                    stmt.execute(ddl.createSchemaInfoTable());
                    break;
                case "ae_v3_item_templates":
                    stmt.execute(ddl.createItemTemplatesTable());
                    break;
                case "ae_v3_cell_meta":
                    stmt.execute(ddl.createCellMetaTable());
                    break;
                case "ae_v3_cell_items":
                    stmt.execute(ddl.createCellItemsTable());
                    break;
                case "ae_v3_journal":
                    stmt.execute(ddl.createJournalTable());
                    break;
                case "ae_v3_journal_archive":
                    stmt.execute(ddl.createJournalArchiveTable());
                    break;
                case "ae_v3_reskin_data":
                    stmt.execute(ddl.createReskinTable());
                    break;
            }
        } catch (SQLException e) {
            report.add("[L1] REBUILD FAILED " + table + ": " + e.getMessage());
        }
    }

    private void level2RowCrc() {
        int corrupted = 0;
        int repaired = 0;
        try (Connection conn = connMgr.getReadConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT cell_uuid, tpl_id, amount, crc32 FROM ae_v3_cell_items")) {
            while (rs.next()) {
                String cellUuid = rs.getString("cell_uuid");
                long tplId = rs.getLong("tpl_id");
                long amount = rs.getLong("amount");
                int storedCrc = rs.getInt("crc32");
                int expectedCrc = CRC32Utils.computeCellItem(cellUuid, tplId, amount);
                if (storedCrc != expectedCrc) {
                    corrupted++;
                    report.add("[L2] CRC mismatch: cell=" + cellUuid + " tpl=" + tplId + " expected=" + expectedCrc
                            + " actual=" + storedCrc);
                    if (!dryRun) {
                        Long journalAmount = findLatestJournalAmount(cellUuid, tplId);
                        if (journalAmount != null) {
                            repairCellItem(cellUuid, tplId, journalAmount);
                            repaired++;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            report.add("[L2] ERROR: " + e.getMessage());
        }
        if (corrupted == 0) {
            report.add("[L2] PASS: All CRC32 values valid");
        } else {
            report.add("[L2] FOUND " + corrupted + " corrupted rows, repaired " + repaired);
        }
    }

    private Long findLatestJournalAmount(String cellUuid, long tplId) {
        String sql = "SELECT new_amount FROM ae_v3_journal "
                + "WHERE cell_uuid = ? AND tpl_id = ? AND op = 'P' "
                + "ORDER BY journal_id DESC LIMIT 1";
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cellUuid);
            ps.setLong(2, tplId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("new_amount");
            }
        } catch (SQLException ignored) {
        }
        String archiveSql = "SELECT new_amount FROM ae_v3_journal_archive "
                + "WHERE cell_uuid = ? AND tpl_id = ? AND op = 'P' "
                + "ORDER BY timestamp DESC LIMIT 1";
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(archiveSql)) {
            ps.setString(1, cellUuid);
            ps.setLong(2, tplId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("new_amount");
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private void repairCellItem(String cellUuid, long tplId, long amount) {
        int crc = CRC32Utils.computeCellItem(cellUuid, tplId, amount);
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps =
                        conn.prepareStatement("UPDATE ae_v3_cell_items SET amount = ?, crc32 = ?, updated_at = ? "
                                + "WHERE cell_uuid = ? AND tpl_id = ?")) {
            ps.setLong(1, amount);
            ps.setInt(2, crc);
            ps.setLong(3, System.currentTimeMillis());
            ps.setString(4, cellUuid);
            ps.setLong(5, tplId);
            ps.executeUpdate();
        } catch (SQLException e) {
            report.add("[L2] REPAIR FAILED: cell=" + cellUuid + " tpl=" + tplId + ": " + e.getMessage());
        }
    }

    private void level3LogicConsistency() {
        int inconsistent = 0;
        try (Connection conn = connMgr.getReadConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT cell_uuid, `stored` FROM ae_v3_cell_meta")) {
            while (rs.next()) {
                String cellUuid = rs.getString("cell_uuid");
                long metaStored = rs.getLong("stored");
                long actualStored = queryActualStored(cellUuid);
                if (metaStored != actualStored) {
                    inconsistent++;
                    report.add("[L3] Stored mismatch: cell=" + cellUuid + " meta=" + metaStored + " actual="
                            + actualStored);
                    if (!dryRun) {
                        fixStored(cellUuid, actualStored);
                    }
                }
            }
        } catch (SQLException e) {
            report.add("[L3] ERROR: " + e.getMessage());
        }
        if (inconsistent == 0) {
            report.add("[L3] PASS: All stored counts consistent");
        } else {
            report.add("[L3] FIXED " + inconsistent + " inconsistent cells");
        }
    }

    private long queryActualStored(String cellUuid) {
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT COALESCE(SUM(amount), 0) FROM ae_v3_cell_items WHERE cell_uuid = ?")) {
            ps.setString(1, cellUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    private void fixStored(String cellUuid, long actualStored) {
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE ae_v3_cell_meta SET `stored` = ?, updated_at = ? WHERE cell_uuid = ?")) {
            ps.setLong(1, actualStored);
            ps.setLong(2, System.currentTimeMillis());
            ps.setString(3, cellUuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            report.add("[L3] FIX FAILED: cell=" + cellUuid + ": " + e.getMessage());
        }
    }

    private void level4OrphanReferences() {
        int orphans = 0;
        try (Connection conn = connMgr.getReadConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT DISTINCT ci.tpl_id, ci.cell_uuid FROM ae_v3_cell_items ci "
                        + "LEFT JOIN ae_v3_item_templates it ON ci.tpl_id = it.tpl_id "
                        + "WHERE it.tpl_id IS NULL")) {
            while (rs.next()) {
                orphans++;
                long tplId = rs.getLong("tpl_id");
                String cellUuid = rs.getString("cell_uuid");
                report.add("[L4] Orphan reference: cell=" + cellUuid + " tpl_id=" + tplId);
                if (!dryRun) {
                    deleteOrphanItem(cellUuid, tplId);
                }
            }
        } catch (SQLException e) {
            report.add("[L4] ERROR: " + e.getMessage());
        }
        if (orphans == 0) {
            report.add("[L4] PASS: No orphan references");
        } else {
            report.add("[L4] FOUND " + orphans + " orphan references" + (dryRun ? " (dry-run)" : ", removed"));
        }
    }

    private void deleteOrphanItem(String cellUuid, long tplId) {
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps =
                        conn.prepareStatement("DELETE FROM ae_v3_cell_items WHERE cell_uuid = ? AND tpl_id = ?")) {
            ps.setString(1, cellUuid);
            ps.setLong(2, tplId);
            ps.executeUpdate();
        } catch (SQLException e) {
            report.add("[L4] DELETE FAILED: cell=" + cellUuid + " tpl=" + tplId + ": " + e.getMessage());
        }
    }
}
