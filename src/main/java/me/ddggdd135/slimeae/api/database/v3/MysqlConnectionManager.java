package me.ddggdd135.slimeae.api.database.v3;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class MysqlConnectionManager implements ConnectionManager {
    private final HikariDataSource pool;

    public MysqlConnectionManager(StorageConfig config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl("jdbc:mysql://" + config.getMysqlHost() + ":" + config.getMysqlPort() + "/"
                + config.getMysqlDatabase() + "?useSSL=false&characterEncoding=utf8mb4");
        hikari.setUsername(config.getMysqlUsername());
        hikari.setPassword(config.getMysqlPassword());
        hikari.setMaximumPoolSize(config.getMysqlPoolSize());
        hikari.setMinimumIdle(2);
        hikari.setPoolName("SlimeAE-MySQL-Pool");
        hikari.setConnectionTimeout(10000);
        hikari.setIdleTimeout(300000);
        hikari.setMaxLifetime(600000);
        pool = new HikariDataSource(hikari);
    }

    @Override
    public Connection getWriteConnection() throws SQLException {
        return pool.getConnection();
    }

    @Override
    public Connection getReadConnection() throws SQLException {
        return pool.getConnection();
    }

    @Override
    public boolean isMysql() {
        return true;
    }

    @Override
    public void shutdown() {
        pool.close();
    }
}
