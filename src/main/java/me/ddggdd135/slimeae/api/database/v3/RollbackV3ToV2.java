package me.ddggdd135.slimeae.api.database.v3;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RollbackV3ToV2 {
    private static final Logger logger = Logger.getLogger("SlimeAE-Rollback");
    private final ConnectionManager connMgr;
    private final DDLProvider ddl;

    public RollbackV3ToV2(ConnectionManager connMgr, DDLProvider ddl) {
        this.connMgr = connMgr;
        this.ddl = ddl;
    }

    public boolean canRollback() {
        try (Connection conn = connMgr.getReadConnection()) {
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "ae_v3_cell_items", null)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public void rollback() {
        logger.info("Starting v3 -> v2 rollback...");
        try {
            boolean hasBackup = tableExists("ae_storagecell_data_v2_backup");
            if (hasBackup) {
                rollbackFromBackup();
            } else {
                rollbackFromV3Tables();
            }
            updateSchemaVersion();
            logger.info("Rollback v3 -> v2 completed. Please restart the server.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Rollback failed: " + e.getMessage(), e);
        }
    }

    private boolean tableExists(String tableName) {
        try (Connection conn = connMgr.getReadConnection();
                ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void rollbackFromBackup() throws SQLException {
        logger.info("Restoring from v2 backup tables...");
        try (Connection conn = connMgr.getWriteConnection();
                Statement stmt = conn.createStatement()) {
            if (tableExists("ae_storagecell_data")) {
                stmt.execute("DROP TABLE IF EXISTS ae_storagecell_data");
            }
            stmt.execute("ALTER TABLE ae_storagecell_data_v2_backup RENAME TO ae_storagecell_data");
            if (tableExists("ae_storagecell_filter_data_v2_backup")) {
                if (tableExists("ae_storagecell_filter_data")) {
                    stmt.execute("DROP TABLE IF EXISTS ae_storagecell_filter_data");
                }
                stmt.execute("ALTER TABLE ae_storagecell_filter_data_v2_backup RENAME TO ae_storagecell_filter_data");
            }
        }
        logger.info("Restored from backup tables");
    }

    private void rollbackFromV3Tables() throws SQLException {
        logger.info("Generating v2 tables from v3 data...");
        try (Connection conn = connMgr.getWriteConnection()) {
            conn.setAutoCommit(false);
            try {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE TABLE IF NOT EXISTS ae_storagecell_data ("
                            + "uuid VARCHAR(36) NOT NULL, "
                            + "item_hash VARCHAR(64) NOT NULL, "
                            + "item_base64 TEXT NOT NULL, "
                            + "amount BIGINT NOT NULL, "
                            + "PRIMARY KEY (uuid, item_hash))");
                }
                String selectSql = "SELECT ci.cell_uuid, ci.tpl_id, ci.amount, "
                        + "it.item_id, it.item_data "
                        + "FROM ae_v3_cell_items ci "
                        + "JOIN ae_v3_item_templates it ON ci.tpl_id = it.tpl_id";
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(selectSql);
                        PreparedStatement ins = conn.prepareStatement(
                                "INSERT INTO ae_storagecell_data (uuid, item_hash, item_base64, amount) VALUES (?, ?, ?, ?)")) {
                    while (rs.next()) {
                        String cellUuid = rs.getString("cell_uuid");
                        String itemId = rs.getString("item_id");
                        String itemData = rs.getString("item_data");
                        long amount = rs.getLong("amount");
                        String base64;
                        if ("CUSTOM".equals(itemId) && itemData != null) {
                            base64 = itemData;
                        } else {
                            base64 = itemId;
                        }
                        String itemHash = String.valueOf(base64.hashCode());
                        ins.setString(1, cellUuid);
                        ins.setString(2, itemHash);
                        ins.setString(3, base64);
                        ins.setLong(4, amount);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                rollbackFilterData(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        logger.info("Generated v2 tables from v3 data");
    }

    private void rollbackFilterData(Connection conn) throws SQLException {
        String selectSql = "SELECT cell_uuid, filter_json FROM ae_v3_cell_meta WHERE filter_json IS NOT NULL";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS ae_storagecell_filter_data ("
                    + "uuid VARCHAR(36) NOT NULL, "
                    + "field_name VARCHAR(64) NOT NULL, "
                    + "data TEXT NOT NULL, "
                    + "PRIMARY KEY (uuid, field_name))");
        }
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(selectSql);
                PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO ae_storagecell_filter_data (uuid, field_name, data) VALUES (?, ?, ?)")) {
            while (rs.next()) {
                String uuid = rs.getString("cell_uuid");
                String json = rs.getString("filter_json");
                if (json == null || json.isEmpty()) continue;
                parseFilterJsonToV2(ins, uuid, json);
            }
            ins.executeBatch();
        }
    }

    private void parseFilterJsonToV2(PreparedStatement ins, String uuid, String json) throws SQLException {
        int filtersStart = json.indexOf("[");
        int filtersEnd = json.indexOf("]");
        if (filtersStart >= 0 && filtersEnd > filtersStart) {
            String filtersStr = json.substring(filtersStart + 1, filtersEnd);
            if (!filtersStr.isEmpty()) {
                String[] parts = filtersStr.split(",");
                for (String part : parts) {
                    String val = part.trim();
                    if (val.startsWith("\"")) val = val.substring(1);
                    if (val.endsWith("\"")) val = val.substring(0, val.length() - 1);
                    if (!val.isEmpty()) {
                        ins.setString(1, uuid);
                        ins.setString(2, "filter");
                        ins.setString(3, val);
                        ins.addBatch();
                    }
                }
            }
        }
        if (json.contains("\"reversed\":true")) {
            ins.setString(1, uuid);
            ins.setString(2, "reversed");
            ins.setString(3, "true");
            ins.addBatch();
        }
        if (json.contains("\"fuzzy\":true")) {
            ins.setString(1, uuid);
            ins.setString(2, "fuzzy");
            ins.setString(3, "true");
            ins.addBatch();
        }
    }

    private void updateSchemaVersion() {
        try (Connection conn = connMgr.getWriteConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM ae_v3_schema_info WHERE config_key = 'schema_version'");
            stmt.execute("INSERT INTO ae_v3_schema_info (config_key, config_value) VALUES ('schema_version', '2')");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to update schema version: " + e.getMessage(), e);
        }
    }
}
