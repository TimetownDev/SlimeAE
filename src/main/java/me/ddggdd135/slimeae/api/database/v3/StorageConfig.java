package me.ddggdd135.slimeae.api.database.v3;

import org.bukkit.configuration.ConfigurationSection;

public class StorageConfig {
    private final String backend;
    private final boolean independentPool;
    private final String sqliteFile;
    private final boolean walMode;
    private final int busyTimeout;
    private final int readPoolSize;
    private final String mysqlHost;
    private final int mysqlPort;
    private final String mysqlDatabase;
    private final String mysqlUsername;
    private final String mysqlPassword;
    private final int mysqlPoolSize;
    private final long journalRetentionMinutes;
    private final boolean archiveEnabled;
    private final int archiveRetentionDays;
    private final int archiveMaxRows;
    private final int journalFlushBatchSize;
    private final int checkpointInterval;
    private final int checkpointThreshold;
    private final int autoSavePeriod;
    private final boolean flushOnCellUnload;
    private final int startupVerifyLevel;
    private final boolean backupEnabled;
    private final int backupIntervalHours;
    private final int maxBackups;
    private final String migrationMode;
    private final boolean strictJournalOrdering;

    public StorageConfig(ConfigurationSection config) {
        ConfigurationSection storage = config.getConfigurationSection("storage");
        if (storage == null) {
            backend = "sqlite";
            independentPool = false;
            sqliteFile = "plugins/SlimeAE/data/ae_storage.db";
            walMode = true;
            busyTimeout = 5000;
            readPoolSize = 3;
            mysqlHost = "localhost";
            mysqlPort = 3306;
            mysqlDatabase = "slimeae";
            mysqlUsername = "root";
            mysqlPassword = "";
            mysqlPoolSize = 5;
            journalRetentionMinutes = 30;
            archiveEnabled = true;
            archiveRetentionDays = 7;
            archiveMaxRows = 2000000;
            journalFlushBatchSize = 2000;
            checkpointInterval = 60;
            checkpointThreshold = 5000;
            autoSavePeriod = 10;
            flushOnCellUnload = false;
            startupVerifyLevel = 1;
            backupEnabled = true;
            backupIntervalHours = 24;
            maxBackups = 7;
            migrationMode = "auto";
            strictJournalOrdering = false;
            return;
        }
        backend = storage.getString("backend", "sqlite");
        independentPool = storage.getBoolean("independent-pool", false);
        ConfigurationSection sq = storage.getConfigurationSection("sqlite");
        sqliteFile = sq != null
                ? sq.getString("file", "plugins/SlimeAE/data/ae_storage.db")
                : "plugins/SlimeAE/data/ae_storage.db";
        walMode = sq != null && sq.getBoolean("wal-mode", true);
        busyTimeout = sq != null ? sq.getInt("busy-timeout", 5000) : 5000;
        readPoolSize = sq != null ? sq.getInt("read-pool-size", 3) : 3;
        strictJournalOrdering = sq != null && sq.getBoolean("strict-journal-ordering", false);
        ConfigurationSection my = storage.getConfigurationSection("mysql");
        mysqlHost = my != null ? my.getString("host", "localhost") : "localhost";
        mysqlPort = my != null ? my.getInt("port", 3306) : 3306;
        mysqlDatabase = my != null ? my.getString("database", "slimeae") : "slimeae";
        mysqlUsername = my != null ? my.getString("username", "root") : "root";
        mysqlPassword = my != null ? my.getString("password", "") : "";
        mysqlPoolSize = my != null ? my.getInt("pool-size", 5) : 5;
        journalRetentionMinutes = storage.getLong("journal-retention-minutes", 30);
        ConfigurationSection arc = storage.getConfigurationSection("archive");
        archiveEnabled = arc != null && arc.getBoolean("enabled", true);
        archiveRetentionDays = arc != null ? arc.getInt("retention-days", 7) : 7;
        archiveMaxRows = arc != null ? arc.getInt("max-rows", 2000000) : 2000000;
        journalFlushBatchSize = storage.getInt("journal-flush-batch-size", 2000);
        checkpointInterval = storage.getInt("checkpoint-interval", 60);
        checkpointThreshold = storage.getInt("checkpoint-threshold", 5000);
        autoSavePeriod = storage.getInt("auto-save-period", 10);
        flushOnCellUnload = storage.getBoolean("flush-on-cell-unload", false);
        startupVerifyLevel = storage.getInt("startup-verify-level", 1);
        ConfigurationSection bk = storage.getConfigurationSection("backup");
        backupEnabled = bk != null && bk.getBoolean("enabled", true);
        backupIntervalHours = bk != null ? bk.getInt("interval-hours", 24) : 24;
        maxBackups = bk != null ? bk.getInt("max-backups", 7) : 7;
        migrationMode = storage.getString("migration-mode", "auto");
    }

    public boolean isSqlite() {
        return "sqlite".equalsIgnoreCase(backend);
    }

    public boolean isMysql() {
        return "mysql".equalsIgnoreCase(backend);
    }

    public boolean isIndependentPool() {
        return independentPool;
    }

    public String getSqliteFile() {
        return sqliteFile;
    }

    public boolean isWalMode() {
        return walMode;
    }

    public int getBusyTimeout() {
        return busyTimeout;
    }

    public int getReadPoolSize() {
        return readPoolSize;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public int getMysqlPoolSize() {
        return mysqlPoolSize;
    }

    public long getJournalRetentionMinutes() {
        return journalRetentionMinutes;
    }

    public boolean isArchiveEnabled() {
        return archiveEnabled;
    }

    public int getArchiveRetentionDays() {
        return archiveRetentionDays;
    }

    public int getArchiveMaxRows() {
        return archiveMaxRows;
    }

    public int getJournalFlushBatchSize() {
        return journalFlushBatchSize;
    }

    public int getCheckpointInterval() {
        return checkpointInterval;
    }

    public int getCheckpointThreshold() {
        return checkpointThreshold;
    }

    public int getAutoSavePeriod() {
        return autoSavePeriod;
    }

    public boolean isFlushOnCellUnload() {
        return flushOnCellUnload;
    }

    public int getStartupVerifyLevel() {
        return startupVerifyLevel;
    }

    public boolean isBackupEnabled() {
        return backupEnabled;
    }

    public int getBackupIntervalHours() {
        return backupIntervalHours;
    }

    public int getMaxBackups() {
        return maxBackups;
    }

    public String getMigrationMode() {
        return migrationMode;
    }

    public String getBackend() {
        return backend;
    }

    public boolean isStrictJournalOrdering() {
        return strictJournalOrdering;
    }
}
