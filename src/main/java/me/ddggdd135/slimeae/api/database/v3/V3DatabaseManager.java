package me.ddggdd135.slimeae.api.database.v3;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class V3DatabaseManager {
    private static final Logger logger = Logger.getLogger("SlimeAE-V3DB");
    private final StorageConfig storageConfig;
    private final ConnectionManager connMgr;
    private final SchemaManager schemaManager;
    private final ItemTemplateRegistry templateRegistry;
    private final ItemKeyTplIdBridge bridge;
    private final DirtyTracker dirtyTracker;
    private final WriteConcurrencyStrategy writeStrategy;
    private final JournalWriter journalWriter;
    private final CheckpointTask checkpointTask;
    private final V3StorageCellController storageController;
    private final V3FilterController filterController;
    private final V3ReskinController reskinController;
    private final MigrationV2ToV3 migration;
    private final BackupTask backupTask;
    private ScheduledExecutorService checkpointExecutor;
    private ScheduledExecutorService backupExecutor;
    private final AtomicLong flushCounter = new AtomicLong(0);

    public V3DatabaseManager(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
        this.connMgr = ConnectionManagerFactory.create(storageConfig);
        this.schemaManager = new SchemaManager(connMgr, storageConfig.isStrictJournalOrdering());
        DDLProvider ddl = schemaManager.getDDL();
        this.templateRegistry = new ItemTemplateRegistry(connMgr, ddl);
        this.bridge = new ItemKeyTplIdBridge(templateRegistry);
        this.dirtyTracker = new DirtyTracker();
        this.writeStrategy = connMgr.isMysql() ? new MysqlWriteStrategy() : new SqliteWriteStrategy();
        this.journalWriter = new JournalWriter(writeStrategy, dirtyTracker, connMgr);
        this.checkpointTask = new CheckpointTask(connMgr, writeStrategy, ddl, storageConfig);
        this.storageController = new V3StorageCellController(connMgr, bridge, dirtyTracker);
        this.filterController = new V3FilterController(connMgr, ddl);
        this.reskinController = new V3ReskinController(connMgr, ddl);
        this.migration = new MigrationV2ToV3(connMgr, templateRegistry, ddl);
        this.backupTask = new BackupTask(storageConfig, connMgr);
    }

    public void init() {
        schemaManager.initSchema();

        int schemaVersion = querySchemaVersion();
        if (schemaVersion > 3) {
            logger.log(
                    Level.SEVERE,
                    "Database schema_version=" + schemaVersion + " is newer than supported (v3). "
                            + "Please upgrade SlimeAE or rollback the database. Aborting initialization.");
            throw new IllegalStateException(
                    "Unsupported schema_version: " + schemaVersion + " (max supported: 3). Please upgrade SlimeAE.");
        }

        if (migration.needsMigration()) {
            if ("auto".equalsIgnoreCase(storageConfig.getMigrationMode())) {
                migration.migrate();
            } else {
                logger.warning("Migration needed but migration-mode is 'manual'. Use /slimeae migrate");
            }
        }
        schemaManager.markSchemaVersion3();

        templateRegistry.preloadAll();
        checkpointTask.replayPendingJournal();

        checkpointExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SlimeAE-Checkpoint");
            t.setDaemon(true);
            return t;
        });
        checkpointExecutor.scheduleWithFixedDelay(
                checkpointTask,
                storageConfig.getCheckpointInterval(),
                storageConfig.getCheckpointInterval(),
                TimeUnit.SECONDS);

        if (storageConfig.isBackupEnabled() && storageConfig.isSqlite()) {
            backupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "SlimeAE-Backup");
                t.setDaemon(true);
                return t;
            });
            long intervalMs = storageConfig.getBackupIntervalHours() * 3600L;
            backupExecutor.scheduleWithFixedDelay(backupTask, intervalMs, intervalMs, TimeUnit.SECONDS);
        }

        logger.info("V3 database manager initialized");
    }

    private int querySchemaVersion() {
        try (Connection conn = connMgr.getReadConnection()) {
            try (ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT config_value FROM ae_v3_schema_info WHERE config_key = 'schema_version'")) {
                if (rs.next()) {
                    return Integer.parseInt(rs.getString("config_value"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to query schema_version: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid schema_version format: " + e.getMessage(), e);
        }
        return 0;
    }

    public void saveAllAsync() {
        journalWriter.flush();
        long count = flushCounter.incrementAndGet();
        if (storageConfig.getCheckpointThreshold() > 0 && count % storageConfig.getCheckpointThreshold() == 0) {
            checkpointExecutor.submit(checkpointTask);
        }
    }

    public void flushCellOnUnload(java.util.UUID cellUUID) {
        if (storageConfig.isFlushOnCellUnload()) {
            journalWriter.flushCell(cellUUID);
        }
    }

    public void shutdown() {
        if (backupExecutor != null) {
            backupExecutor.shutdownNow();
        }
        if (checkpointExecutor != null) {
            checkpointExecutor.shutdownNow();
            try {
                checkpointExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }
        journalWriter.flush();
        checkpointTask.doCheckpoint();
        reskinController.shutdown();
        connMgr.shutdown();
        logger.info("V3 database manager shut down");
    }

    public V3StorageCellController getStorageController() {
        return storageController;
    }

    public V3FilterController getFilterController() {
        return filterController;
    }

    public V3ReskinController getReskinController() {
        return reskinController;
    }

    public DirtyTracker getDirtyTracker() {
        return dirtyTracker;
    }

    public JournalWriter getJournalWriter() {
        return journalWriter;
    }

    public ItemKeyTplIdBridge getBridge() {
        return bridge;
    }

    public ItemTemplateRegistry getTemplateRegistry() {
        return templateRegistry;
    }

    public ConnectionManager getConnectionManager() {
        return connMgr;
    }

    public StorageConfig getStorageConfig() {
        return storageConfig;
    }

    public MigrationV2ToV3 getMigration() {
        return migration;
    }

    public SchemaManager getSchemaManager() {
        return schemaManager;
    }

    public CheckpointTask getCheckpointTask() {
        return checkpointTask;
    }
}
