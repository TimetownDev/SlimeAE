package me.ddggdd135.slimeae.api.database.v3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.ddggdd135.guguslimefunlib.GuguSlimefunLib;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.inventory.ItemStack;

public class MigrationV2ToV3 {
    private static final Logger logger = Logger.getLogger("SlimeAE-Migration");
    private final ConnectionManager connMgr;
    private final ItemTemplateRegistry registry;
    private final DDLProvider ddl;

    public MigrationV2ToV3(ConnectionManager connMgr, ItemTemplateRegistry registry, DDLProvider ddl) {
        this.connMgr = connMgr;
        this.registry = registry;
        this.ddl = ddl;
    }

    public boolean needsMigration() {
        boolean hasSchemaVersion3 = false;
        try (Connection conn = connMgr.getReadConnection();
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery(
                        "SELECT config_value FROM ae_v3_schema_info WHERE config_key = 'schema_version'")) {
            if (rs.next()) {
                hasSchemaVersion3 = "3".equals(rs.getString("config_value"));
            }
        } catch (SQLException ignored) {
        }

        if (hasSchemaVersion3) {
            boolean v3HasData = false;
            try (Connection conn = connMgr.getReadConnection();
                    var stmt = conn.createStatement();
                    var rs = stmt.executeQuery("SELECT COUNT(*) FROM ae_v3_cell_items")) {
                if (rs.next()) {
                    v3HasData = rs.getInt(1) > 0;
                }
            } catch (SQLException ignored) {
            }
            if (v3HasData) {
                return false;
            }
            boolean v2HasData = false;
            try (Connection conn = connMgr.getReadConnection();
                    var rs = conn.getMetaData().getTables(null, null, "ae_storagecell_data", null)) {
                if (rs.next()) {
                    try (Connection conn2 = connMgr.getReadConnection();
                            var stmt2 = conn2.createStatement();
                            var rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM ae_storagecell_data")) {
                        if (rs2.next()) {
                            v2HasData = rs2.getInt(1) > 0;
                        }
                    }
                }
            } catch (SQLException ignored) {
            }
            return v2HasData;
        }

        boolean hasV2Table = false;
        try (Connection conn = connMgr.getReadConnection();
                var rs = conn.getMetaData().getTables(null, null, "ae_storagecell_data", null)) {
            hasV2Table = rs.next();
        } catch (SQLException ignored) {
        }

        if (hasV2Table) {
            boolean hasV2Data = false;
            try (Connection conn = connMgr.getReadConnection();
                    var stmt = conn.createStatement();
                    var rs = stmt.executeQuery("SELECT COUNT(*) FROM ae_storagecell_data")) {
                if (rs.next()) {
                    hasV2Data = rs.getInt(1) > 0;
                }
            } catch (SQLException ignored) {
            }
            return hasV2Data;
        }

        return false;
    }

    public void migrate() {
        logger.info("Starting v2 -> v3 migration...");
        try {
            backupV2Tables();
            migrateStorageData();
            migrateFilterData();
            migrateReskinData();
            renameV2Tables();
            logger.info("Migration v2 -> v3 completed successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Migration failed: " + e.getMessage(), e);
        }
    }

    private void backupV2Tables() {
        logger.info("Creating backup of v2 tables...");
        try (Connection conn = connMgr.getWriteConnection();
                var stmt = conn.createStatement()) {
            if (tableExists("ae_storagecell_data")) {
                stmt.execute("DROP TABLE IF EXISTS ae_storagecell_data_v2_backup");
                stmt.execute("CREATE TABLE ae_storagecell_data_v2_backup AS SELECT * FROM ae_storagecell_data");
                logger.info("Backed up ae_storagecell_data -> ae_storagecell_data_v2_backup");
            }
            if (tableExists("ae_storagecell_filter_data")) {
                stmt.execute("DROP TABLE IF EXISTS ae_storagecell_filter_data_v2_backup");
                stmt.execute(
                        "CREATE TABLE ae_storagecell_filter_data_v2_backup AS SELECT * FROM ae_storagecell_filter_data");
                logger.info("Backed up ae_storagecell_filter_data -> ae_storagecell_filter_data_v2_backup");
            }
            if (tableExists("ae_reskin_data")) {
                stmt.execute("DROP TABLE IF EXISTS ae_reskin_data_v2_backup");
                stmt.execute("CREATE TABLE ae_reskin_data_v2_backup AS SELECT * FROM ae_reskin_data");
                logger.info("Backed up ae_reskin_data -> ae_reskin_data_v2_backup");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Backup of v2 tables failed: " + e.getMessage(), e);
        }
    }

    private boolean tableExists(String tableName) {
        try (Connection conn = connMgr.getReadConnection();
                var rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void renameV2Tables() {
        logger.info("Renaming original v2 tables...");
        try (Connection conn = connMgr.getWriteConnection();
                var stmt = conn.createStatement()) {
            if (tableExists("ae_storagecell_data")) {
                stmt.execute("DROP TABLE ae_storagecell_data");
                logger.info("Dropped original ae_storagecell_data (backup preserved as ae_storagecell_data_v2_backup)");
            }
            if (tableExists("ae_storagecell_filter_data")) {
                stmt.execute("DROP TABLE ae_storagecell_filter_data");
                logger.info(
                        "Dropped original ae_storagecell_filter_data (backup preserved as ae_storagecell_filter_data_v2_backup)");
            }
            if (tableExists("ae_reskin_data")) {
                stmt.execute("DROP TABLE ae_reskin_data");
                logger.info("Dropped original ae_reskin_data (backup preserved as ae_reskin_data_v2_backup)");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Rename v2 tables failed: " + e.getMessage(), e);
        }
    }

    private void migrateStorageData() throws SQLException {
        String selectSql = "SELECT uuid, item_hash, item_base64, amount FROM ae_storagecell_data";
        String serverUuid = GuguSlimefunLib.getServerUUID().toString();
        long now = System.currentTimeMillis();

        java.util.List<RawRow> rawRows = new java.util.ArrayList<>();
        try (Connection readConn = connMgr.getReadConnection();
                var stmt = readConn.createStatement();
                ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                String base64 = rs.getString("item_base64");
                long amount = rs.getLong("amount");
                if (amount <= 0) continue;
                rawRows.add(new RawRow(uuid, base64, amount));
            }
        }

        java.util.Map<String, java.util.List<ItemRow>> cellMap = new java.util.LinkedHashMap<>();
        int errorCount = 0;

        for (RawRow raw : rawRows) {
            try {
                Object obj = SerializeUtils.string2Object(raw.base64);
                if (!(obj instanceof ItemStack item) || item.getType().isAir()) continue;
                long tplId = registry.getOrRegister(item);
                cellMap.computeIfAbsent(raw.uuid, k -> new java.util.ArrayList<>())
                        .add(new ItemRow(tplId, raw.amount));
            } catch (Exception e) {
                errorCount++;
                logger.warning("Migration: failed to deserialize item in cell " + raw.uuid + ": " + e.getMessage());
                logMigrationError(raw.uuid, raw.base64, e.getMessage());
            }
        }

        try (Connection writeConn = connMgr.getWriteConnection()) {
            writeConn.setAutoCommit(false);
            try {
                for (var entry : cellMap.entrySet()) {
                    String cellUuid = entry.getKey();
                    long totalStored = 0;

                    try (PreparedStatement ps = writeConn.prepareStatement(ddl.upsertCellItem())) {
                        for (ItemRow row : entry.getValue()) {
                            ps.setString(1, cellUuid);
                            ps.setLong(2, row.tplId);
                            ps.setLong(3, row.amount);
                            ps.setInt(4, CRC32Utils.computeCellItem(cellUuid, row.tplId, row.amount));
                            ps.setLong(5, now);
                            ps.addBatch();
                            totalStored += row.amount;
                        }
                        ps.executeBatch();
                    }

                    try (PreparedStatement ps = writeConn.prepareStatement(ddl.upsertCellMeta())) {
                        ps.setString(1, cellUuid);
                        ps.setString(2, serverUuid);
                        ps.setLong(3, 0);
                        ps.setLong(4, totalStored);
                        ps.setString(5, null);
                        ps.setString(6, null);
                        ps.setLong(7, now);
                        ps.setLong(8, now);
                        ps.executeUpdate();
                    }
                }
                writeConn.commit();
            } catch (SQLException e) {
                writeConn.rollback();
                throw e;
            }
        }

        logger.info("Migrated " + cellMap.size() + " storage cells, " + errorCount + " items failed");
    }

    private void migrateFilterData() throws SQLException {
        String selectSql = "SELECT uuid, field_name, data FROM ae_storagecell_filter_data";

        java.util.Map<String, FilterInfo> filterMap = new java.util.LinkedHashMap<>();

        try (Connection readConn = connMgr.getReadConnection();
                var stmt = readConn.createStatement();
                ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                String fieldName = rs.getString("field_name");
                String fieldData = rs.getString("data");

                FilterInfo fi = filterMap.computeIfAbsent(uuid, k -> new FilterInfo());
                if ("filter".equals(fieldName)) {
                    fi.filters.add(fieldData);
                } else if ("reversed".equals(fieldName)) {
                    fi.reversed = "true".equals(fieldData);
                } else if ("fuzzy".equals(fieldName)) {
                    fi.fuzzy = "true".equals(fieldData);
                }
            }
        }

        try (Connection writeConn = connMgr.getWriteConnection()) {
            long now = System.currentTimeMillis();
            for (var entry : filterMap.entrySet()) {
                String cellUuid = entry.getKey();
                FilterInfo fi = entry.getValue();
                String filterJson = buildFilterJson(fi);

                try (PreparedStatement ps = writeConn.prepareStatement(
                        "UPDATE ae_v3_cell_meta SET filter_json = ?, updated_at = ? WHERE cell_uuid = ?")) {
                    ps.setString(1, filterJson);
                    ps.setLong(2, now);
                    ps.setString(3, cellUuid);
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        try (PreparedStatement ins = writeConn.prepareStatement(ddl.upsertCellMeta())) {
                            ins.setString(1, cellUuid);
                            ins.setString(2, GuguSlimefunLib.getServerUUID().toString());
                            ins.setLong(3, 0);
                            ins.setLong(4, 0);
                            ins.setString(5, filterJson);
                            ins.setString(6, null);
                            ins.setLong(7, now);
                            ins.setLong(8, now);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        }

        logger.info("Migrated " + filterMap.size() + " filter entries");
    }

    private void migrateReskinData() throws SQLException {
        try (Connection readConn = connMgr.getReadConnection();
                var rs = readConn.getMetaData().getTables(null, null, "ae_reskin_data", null)) {
            if (!rs.next()) return;
        }

        String selectSql = "SELECT world, x, y, z, reskin_type, reskin_value FROM ae_reskin_data";

        record ReskinRow(String world, int x, int y, int z, String type, String value) {}

        java.util.List<ReskinRow> reskinRows = new java.util.ArrayList<>();
        try (Connection readConn = connMgr.getReadConnection();
                var stmt = readConn.createStatement();
                ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                reskinRows.add(new ReskinRow(
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getString("reskin_type"),
                        rs.getString("reskin_value")));
            }
        }

        int count = 0;
        try (Connection writeConn = connMgr.getWriteConnection()) {
            try (PreparedStatement ps = writeConn.prepareStatement(ddl.upsertReskin())) {
                for (ReskinRow row : reskinRows) {
                    ps.setString(1, row.world);
                    ps.setInt(2, row.x);
                    ps.setInt(3, row.y);
                    ps.setInt(4, row.z);
                    ps.setString(5, row.type);
                    ps.setString(6, row.value);
                    ps.setInt(7, CRC32Utils.computeReskin(row.world, row.x, row.y, row.z, row.type, row.value));
                    ps.setLong(8, System.currentTimeMillis());
                    ps.addBatch();
                    count++;
                }
                ps.executeBatch();
            }
        }
        logger.info("Migrated " + count + " reskin entries");
    }

    private static String buildFilterJson(FilterInfo fi) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"filters\":[");
        for (int i = 0; i < fi.filters.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('"')
                    .append(fi.filters.get(i).replace("\\", "\\\\").replace("\"", "\\\""))
                    .append('"');
        }
        sb.append("],\"reversed\":").append(fi.reversed);
        sb.append(",\"fuzzy\":").append(fi.fuzzy);
        sb.append('}');
        return sb.toString();
    }

    private void logMigrationError(String cellUuid, String itemBase64, String errorMsg) {
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO ae_v3_migration_errors (cell_uuid, item_base64, error_msg, created_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, cellUuid);
            ps.setString(2, itemBase64);
            ps.setString(3, errorMsg);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Failed to log migration error: " + e.getMessage());
        }
    }

    private record ItemRow(long tplId, long amount) {}

    private record RawRow(String uuid, String base64, long amount) {}

    private static class FilterInfo {
        java.util.List<String> filters = new java.util.ArrayList<>();
        boolean reversed = false;
        boolean fuzzy = false;
    }
}
