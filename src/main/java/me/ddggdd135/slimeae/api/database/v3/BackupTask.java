package me.ddggdd135.slimeae.api.database.v3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackupTask implements Runnable {
    private static final Logger logger = Logger.getLogger("SlimeAE-Backup");
    private final StorageConfig config;

    public BackupTask(StorageConfig config, ConnectionManager connMgr) {
        this.config = config;
    }

    @Override
    public void run() {
        if (!config.isBackupEnabled()) return;
        try {
            doBackup();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Backup failed: " + e.getMessage(), e);
        }
    }

    public void doBackup() {
        if (!config.isSqlite()) {
            logger.info("Backup is only supported for SQLite backend");
            return;
        }
        File dbFile = new File(config.getSqliteFile());
        if (!dbFile.exists()) {
            logger.warning("Database file not found: " + dbFile.getAbsolutePath());
            return;
        }
        File backupDir = new File(dbFile.getParentFile(), "backups");
        backupDir.mkdirs();
        String timestamp = String.valueOf(System.currentTimeMillis());
        File backupFile = new File(backupDir, "ae_storage_" + timestamp + ".db");
        try {
            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Backup created: " + backupFile.getName());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to copy database file: " + e.getMessage(), e);
            return;
        }
        cleanupOldBackups(backupDir);
    }

    private void cleanupOldBackups(File backupDir) {
        File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("ae_storage_") && name.endsWith(".db"));
        if (backups == null || backups.length <= config.getMaxBackups()) return;
        Arrays.sort(backups, Comparator.comparingLong(File::lastModified));
        int toDelete = backups.length - config.getMaxBackups();
        for (int i = 0; i < toDelete; i++) {
            try {
                Files.deleteIfExists(backups[i].toPath());
                logger.info("Deleted old backup: " + backups[i].getName());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to delete old backup: " + backups[i].getName(), e);
            }
        }
    }
}
