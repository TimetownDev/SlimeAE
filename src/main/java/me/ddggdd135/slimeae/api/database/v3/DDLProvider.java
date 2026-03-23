package me.ddggdd135.slimeae.api.database.v3;

public class DDLProvider {
    private final boolean mysql;
    private final boolean strictJournalOrdering;

    public DDLProvider(boolean mysql) {
        this(mysql, false);
    }

    public DDLProvider(boolean mysql, boolean strictJournalOrdering) {
        this.mysql = mysql;
        this.strictJournalOrdering = strictJournalOrdering;
    }

    public String createSchemaInfoTable() {
        return "CREATE TABLE IF NOT EXISTS ae_v3_schema_info ("
                + "config_key VARCHAR(64) PRIMARY KEY, "
                + "config_value TEXT NOT NULL)";
    }

    public String createItemTemplatesTable() {
        if (mysql) {
            return "CREATE TABLE IF NOT EXISTS ae_v3_item_templates ("
                    + "tpl_id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "item_id VARCHAR(256) NOT NULL, "
                    + "item_data TEXT, "
                    + "item_data_hash BIGINT NOT NULL, "
                    + "crc32 INT NOT NULL, "
                    + "created_at BIGINT NOT NULL, "
                    + "UNIQUE KEY idx_tpl_dedup (item_id, item_data_hash)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        }
        return "CREATE TABLE IF NOT EXISTS ae_v3_item_templates ("
                + "tpl_id INTEGER PRIMARY KEY, "
                + "item_id VARCHAR(256) NOT NULL, "
                + "item_data TEXT, "
                + "item_data_hash BIGINT NOT NULL, "
                + "crc32 INT NOT NULL, "
                + "created_at BIGINT NOT NULL)";
    }

    public String createItemTemplatesDedup() {
        if (mysql) return null;
        return "CREATE UNIQUE INDEX IF NOT EXISTS idx_tpl_dedup ON ae_v3_item_templates(item_id, item_data_hash)";
    }

    public String createItemTemplatesItemIdIndex() {
        if (mysql) return null;
        return "CREATE INDEX IF NOT EXISTS idx_tpl_item_id ON ae_v3_item_templates(item_id)";
    }

    public String createCellMetaTable() {
        if (mysql) {
            return "CREATE TABLE IF NOT EXISTS ae_v3_cell_meta ("
                    + "cell_uuid CHAR(36) PRIMARY KEY, "
                    + "server_uuid CHAR(36) NOT NULL, "
                    + "`size` BIGINT NOT NULL, "
                    + "`stored` BIGINT NOT NULL DEFAULT 0, "
                    + "filter_json TEXT, "
                    + "schema_ver INT NOT NULL DEFAULT 3, "
                    + "snapshot_hash VARCHAR(64), "
                    + "updated_at BIGINT NOT NULL, "
                    + "created_at BIGINT NOT NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        }
        return "CREATE TABLE IF NOT EXISTS ae_v3_cell_meta ("
                + "cell_uuid CHAR(36) PRIMARY KEY, "
                + "server_uuid CHAR(36) NOT NULL, "
                + "size BIGINT NOT NULL, "
                + "stored BIGINT NOT NULL DEFAULT 0, "
                + "filter_json TEXT, "
                + "schema_ver INT NOT NULL DEFAULT 3, "
                + "snapshot_hash VARCHAR(64), "
                + "updated_at BIGINT NOT NULL, "
                + "created_at BIGINT NOT NULL)";
    }

    public String createCellItemsTable() {
        if (mysql) {
            return "CREATE TABLE IF NOT EXISTS ae_v3_cell_items ("
                    + "cell_uuid CHAR(36) NOT NULL, "
                    + "tpl_id BIGINT NOT NULL, "
                    + "amount BIGINT NOT NULL DEFAULT 0, "
                    + "crc32 INT NOT NULL, "
                    + "updated_at BIGINT NOT NULL, "
                    + "PRIMARY KEY (cell_uuid, tpl_id), "
                    + "FOREIGN KEY (tpl_id) REFERENCES ae_v3_item_templates(tpl_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        }
        return "CREATE TABLE IF NOT EXISTS ae_v3_cell_items ("
                + "cell_uuid CHAR(36) NOT NULL, "
                + "tpl_id BIGINT NOT NULL, "
                + "amount BIGINT NOT NULL DEFAULT 0, "
                + "crc32 INT NOT NULL, "
                + "updated_at BIGINT NOT NULL, "
                + "PRIMARY KEY (cell_uuid, tpl_id), "
                + "FOREIGN KEY (tpl_id) REFERENCES ae_v3_item_templates(tpl_id))";
    }

    public String createCellItemsIndex() {
        if (mysql) return null;
        return "CREATE INDEX IF NOT EXISTS idx_cell_items_uuid ON ae_v3_cell_items(cell_uuid)";
    }

    public String createJournalTable() {
        if (mysql) {
            return "CREATE TABLE IF NOT EXISTS ae_v3_journal ("
                    + "journal_id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "cell_uuid CHAR(36) NOT NULL, "
                    + "op CHAR(1) NOT NULL, "
                    + "tpl_id BIGINT, "
                    + "new_amount BIGINT, "
                    + "crc32 INT NOT NULL, "
                    + "timestamp BIGINT NOT NULL, "
                    + "applied TINYINT NOT NULL DEFAULT 0, "
                    + "INDEX idx_journal_applied (applied, timestamp)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        }
        return "CREATE TABLE IF NOT EXISTS ae_v3_journal ("
                + "journal_id INTEGER PRIMARY KEY"
                + (strictJournalOrdering ? " AUTOINCREMENT" : "") + ", "
                + "cell_uuid CHAR(36) NOT NULL, "
                + "op CHAR(1) NOT NULL, "
                + "tpl_id BIGINT, "
                + "new_amount BIGINT, "
                + "crc32 INT NOT NULL, "
                + "timestamp BIGINT NOT NULL, "
                + "applied TINYINT NOT NULL DEFAULT 0)";
    }

    public String createJournalIndex() {
        if (mysql) return null;
        return "CREATE INDEX IF NOT EXISTS idx_journal_applied ON ae_v3_journal(applied, timestamp)";
    }

    public String createJournalArchiveTable() {
        if (mysql) {
            return "CREATE TABLE IF NOT EXISTS ae_v3_journal_archive ("
                    + "journal_id BIGINT NOT NULL, "
                    + "cell_uuid CHAR(36) NOT NULL, "
                    + "op CHAR(1) NOT NULL, "
                    + "tpl_id BIGINT, "
                    + "new_amount BIGINT, "
                    + "crc32 INT NOT NULL, "
                    + "timestamp BIGINT NOT NULL, "
                    + "INDEX idx_archive_cell_tpl (cell_uuid, tpl_id, timestamp DESC), "
                    + "INDEX idx_archive_timestamp (timestamp)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        }
        return "CREATE TABLE IF NOT EXISTS ae_v3_journal_archive ("
                + "journal_id BIGINT NOT NULL, "
                + "cell_uuid CHAR(36) NOT NULL, "
                + "op CHAR(1) NOT NULL, "
                + "tpl_id BIGINT, "
                + "new_amount BIGINT, "
                + "crc32 INT NOT NULL, "
                + "timestamp BIGINT NOT NULL)";
    }

    public String createArchiveIndexes() {
        if (mysql) return null;
        return "CREATE INDEX IF NOT EXISTS idx_archive_cell_tpl ON ae_v3_journal_archive(cell_uuid, tpl_id, timestamp DESC)";
    }

    public String createArchiveTimestampIndex() {
        if (mysql) return null;
        return "CREATE INDEX IF NOT EXISTS idx_archive_timestamp ON ae_v3_journal_archive(timestamp)";
    }

    public String createReskinTable() {
        if (mysql) {
            return "CREATE TABLE IF NOT EXISTS ae_v3_reskin_data ("
                    + "world VARCHAR(128) NOT NULL, "
                    + "x INT NOT NULL, "
                    + "y INT NOT NULL, "
                    + "z INT NOT NULL, "
                    + "reskin_type VARCHAR(16) NOT NULL, "
                    + "reskin_value TEXT NOT NULL, "
                    + "crc32 INT NOT NULL, "
                    + "updated_at BIGINT NOT NULL, "
                    + "PRIMARY KEY (world, x, y, z)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        }
        return "CREATE TABLE IF NOT EXISTS ae_v3_reskin_data ("
                + "world VARCHAR(128) NOT NULL, "
                + "x INT NOT NULL, "
                + "y INT NOT NULL, "
                + "z INT NOT NULL, "
                + "reskin_type VARCHAR(16) NOT NULL, "
                + "reskin_value TEXT NOT NULL, "
                + "crc32 INT NOT NULL, "
                + "updated_at BIGINT NOT NULL, "
                + "PRIMARY KEY (world, x, y, z))";
    }

    public String insertIgnore() {
        return mysql ? "INSERT IGNORE" : "INSERT OR IGNORE";
    }

    public String upsertCellItem() {
        if (mysql) {
            return "INSERT INTO ae_v3_cell_items (cell_uuid, tpl_id, amount, crc32, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE amount = VALUES(amount), crc32 = VALUES(crc32), updated_at = VALUES(updated_at)";
        }
        return "INSERT OR REPLACE INTO ae_v3_cell_items (cell_uuid, tpl_id, amount, crc32, updated_at) "
                + "VALUES (?, ?, ?, ?, ?)";
    }

    public String upsertCellMeta() {
        if (mysql) {
            return "INSERT INTO ae_v3_cell_meta (cell_uuid, server_uuid, `size`, `stored`, filter_json, schema_ver, snapshot_hash, updated_at, created_at) "
                    + "VALUES (?, ?, ?, ?, ?, 3, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE server_uuid = VALUES(server_uuid), `size` = VALUES(`size`), `stored` = VALUES(`stored`), "
                    + "filter_json = VALUES(filter_json), snapshot_hash = VALUES(snapshot_hash), updated_at = VALUES(updated_at)";
        }
        return "INSERT OR REPLACE INTO ae_v3_cell_meta (cell_uuid, server_uuid, size, stored, filter_json, schema_ver, snapshot_hash, updated_at, created_at) "
                + "VALUES (?, ?, ?, ?, ?, 3, ?, ?, ?)";
    }

    public String upsertReskin() {
        if (mysql) {
            return "INSERT INTO ae_v3_reskin_data (world, x, y, z, reskin_type, reskin_value, crc32, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE reskin_type = VALUES(reskin_type), reskin_value = VALUES(reskin_value), "
                    + "crc32 = VALUES(crc32), updated_at = VALUES(updated_at)";
        }
        return "INSERT OR REPLACE INTO ae_v3_reskin_data (world, x, y, z, reskin_type, reskin_value, crc32, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    public String updateFilterJsonOnly() {
        return "UPDATE ae_v3_cell_meta SET filter_json = ?, updated_at = ? WHERE cell_uuid = ?";
    }

    public String createMigrationErrorsTable() {
        if (mysql) {
            return "CREATE TABLE IF NOT EXISTS ae_v3_migration_errors ("
                    + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "cell_uuid CHAR(36), "
                    + "item_base64 TEXT, "
                    + "error_msg TEXT NOT NULL, "
                    + "created_at BIGINT NOT NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        }
        return "CREATE TABLE IF NOT EXISTS ae_v3_migration_errors ("
                + "id INTEGER PRIMARY KEY, "
                + "cell_uuid CHAR(36), "
                + "item_base64 TEXT, "
                + "error_msg TEXT NOT NULL, "
                + "created_at BIGINT NOT NULL)";
    }
}
