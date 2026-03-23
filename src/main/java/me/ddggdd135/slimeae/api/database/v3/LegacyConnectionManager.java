package me.ddggdd135.slimeae.api.database.v3;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.sql.Connection;
import java.sql.SQLException;
import me.ddggdd135.slimeae.api.wrappers.CraftHikariDataSource;
import me.ddggdd135.slimeae.utils.ReflectionUtils;

public class LegacyConnectionManager implements ConnectionManager {
    private final CraftHikariDataSource ds;
    private final boolean mysql;

    public LegacyConnectionManager() {
        BlockDataController bdc = Slimefun.getDatabaseManager().getBlockDataController();
        IDataSourceAdapter<?> adapter = ReflectionUtils.getField(bdc, "dataAdapter");
        this.ds = new CraftHikariDataSource(ReflectionUtils.<Object>getField(adapter, "ds"));
        String className = adapter.getClass().getSimpleName();
        this.mysql = className.toLowerCase().contains("mysql");
    }

    @Override
    public Connection getWriteConnection() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public Connection getReadConnection() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public boolean isMysql() {
        return mysql;
    }

    @Override
    public void shutdown() {}
}
