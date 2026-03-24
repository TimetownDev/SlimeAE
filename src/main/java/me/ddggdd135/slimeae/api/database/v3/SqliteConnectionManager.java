package me.ddggdd135.slimeae.api.database.v3;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SqliteConnectionManager implements ConnectionManager {
    private final HikariDataSource writePool;
    private final HikariDataSource readPool;

    public SqliteConnectionManager(StorageConfig config) {
        HikariConfig writeConfig = new HikariConfig();
        writeConfig.setJdbcUrl("jdbc:sqlite:" + config.getSqliteFile());
        writeConfig.setMaximumPoolSize(1);
        writeConfig.setMinimumIdle(1);
        writeConfig.setPoolName("SlimeAE-SQLite-Write");
        writeConfig.addDataSourceProperty("journal_mode", "WAL");
        writeConfig.addDataSourceProperty("busy_timeout", String.valueOf(config.getBusyTimeout()));
        writeConfig.addDataSourceProperty("synchronous", "NORMAL");
        writeConfig.setConnectionInitSql("PRAGMA foreign_keys=ON");
        writePool = new HikariDataSource(writeConfig);

        HikariConfig readConfig = new HikariConfig();
        readConfig.setJdbcUrl("jdbc:sqlite:file:" + config.getSqliteFile() + "?mode=ro");
        readConfig.setMaximumPoolSize(config.getReadPoolSize());
        readConfig.setMinimumIdle(1);
        readConfig.setPoolName("SlimeAE-SQLite-Read");
        readConfig.addDataSourceProperty("journal_mode", "WAL");
        readConfig.addDataSourceProperty("busy_timeout", String.valueOf(config.getBusyTimeout()));
        readConfig.setConnectionInitSql("PRAGMA foreign_keys=ON");
        readPool = new HikariDataSource(readConfig);
    }

    @Override
    public Connection getWriteConnection() throws SQLException {
        return writePool.getConnection();
    }

    @Override
    public Connection getReadConnection() throws SQLException {
        return readPool.getConnection();
    }

    @Override
    public boolean isMysql() {
        return false;
    }

    @Override
    public void shutdown() {
        readPool.close();
        writePool.close();
    }
}
