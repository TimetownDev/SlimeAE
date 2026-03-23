package me.ddggdd135.slimeae.api.database.v3;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchemaManager {
    private static final Logger logger = Logger.getLogger("SlimeAE-Schema");
    private final ConnectionManager connMgr;
    private final DDLProvider ddl;

    public SchemaManager(ConnectionManager connMgr) {
        this(connMgr, false);
    }

    public SchemaManager(ConnectionManager connMgr, boolean strictJournalOrdering) {
        this.connMgr = connMgr;
        this.ddl = new DDLProvider(connMgr.isMysql(), strictJournalOrdering);
    }

    public DDLProvider getDDL() {
        return ddl;
    }

    public void initSchema() {
        try (Connection conn = connMgr.getWriteConnection()) {
            if (!connMgr.isMysql()) {
                exec(conn, "PRAGMA auto_vacuum=INCREMENTAL");
                exec(conn, "PRAGMA journal_mode=WAL");
                exec(conn, "PRAGMA busy_timeout=5000");
                exec(conn, "PRAGMA synchronous=NORMAL");
                exec(conn, "PRAGMA foreign_keys=ON");
            } else {
                exec(conn, "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ");
            }
            migrateOldSchemaInfoColumns(conn);
            exec(conn, ddl.createSchemaInfoTable());
            exec(conn, ddl.createItemTemplatesTable());
            execIfNotNull(conn, ddl.createItemTemplatesDedup());
            execIfNotNull(conn, ddl.createItemTemplatesItemIdIndex());
            exec(conn, ddl.createCellMetaTable());
            exec(conn, ddl.createCellItemsTable());
            execIfNotNull(conn, ddl.createCellItemsIndex());
            exec(conn, ddl.createJournalTable());
            execIfNotNull(conn, ddl.createJournalIndex());
            exec(conn, ddl.createJournalArchiveTable());
            execIfNotNull(conn, ddl.createArchiveIndexes());
            execIfNotNull(conn, ddl.createArchiveTimestampIndex());
            exec(conn, ddl.createReskinTable());
            exec(conn, ddl.createMigrationErrorsTable());

            initSchemaVersion(conn);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Schema initialization failed: " + e.getMessage(), e);
            throw new RuntimeException("Schema initialization failed", e);
        }
    }

    private void initSchemaVersion(Connection conn) throws SQLException {
        var rs = conn.createStatement()
                .executeQuery("SELECT config_value FROM ae_v3_schema_info WHERE config_key = 'created_at'");
        if (!rs.next()) {
            long now = System.currentTimeMillis();
            exec(conn, "INSERT INTO ae_v3_schema_info (config_key, config_value) VALUES ('created_at', '" + now + "')");
            exec(
                    conn,
                    "INSERT INTO ae_v3_schema_info (config_key, config_value) VALUES ('last_migrated', '" + now + "')");
        }
        rs.close();
    }

    public void markSchemaVersion3() {
        try (Connection conn = connMgr.getWriteConnection()) {
            exec(conn, "DELETE FROM ae_v3_schema_info WHERE config_key = 'schema_version'");
            exec(conn, "INSERT INTO ae_v3_schema_info (config_key, config_value) VALUES ('schema_version', '3')");
            long now = System.currentTimeMillis();
            exec(conn, "DELETE FROM ae_v3_schema_info WHERE config_key = 'last_migrated'");
            exec(
                    conn,
                    "INSERT INTO ae_v3_schema_info (config_key, config_value) VALUES ('last_migrated', '" + now + "')");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to mark schema version: " + e.getMessage(), e);
        }
    }

    private void exec(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void execIfNotNull(Connection conn, String sql) throws SQLException {
        if (sql != null) exec(conn, sql);
    }

    private void migrateOldSchemaInfoColumns(Connection conn) {
        try {
            boolean hasOldColumn = false;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "ae_v3_schema_info", "key")) {
                hasOldColumn = rs.next();
            }
            if (!hasOldColumn) return;
            exec(conn, "ALTER TABLE ae_v3_schema_info RENAME TO ae_v3_schema_info_old");
            exec(conn, ddl.createSchemaInfoTable());
            exec(
                    conn,
                    "INSERT INTO ae_v3_schema_info (config_key, config_value) "
                            + "SELECT `key`, `value` FROM ae_v3_schema_info_old");
            exec(conn, "DROP TABLE ae_v3_schema_info_old");
            logger.info("Migrated ae_v3_schema_info columns: key -> config_key, value -> config_value");
        } catch (SQLException e) {
            logger.log(Level.FINE, "No old schema_info table to migrate: " + e.getMessage());
        }
    }
}
