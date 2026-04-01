package me.ddggdd135.slimeae.api.database.v3;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionManager {
    Connection getWriteConnection() throws SQLException;

    Connection getReadConnection() throws SQLException;

    boolean isMysql();

    void shutdown();
}
