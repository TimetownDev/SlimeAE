package me.ddggdd135.slimeae.api.wrappers;

import java.sql.Connection;
import me.ddggdd135.slimeae.utils.ReflectionUtils;

public class CraftHikariDataSource extends HikariObject {
    public CraftHikariDataSource(Object handle) {
        super(handle);
    }

    public Connection getConnection() {
        return ReflectionUtils.invokePrivateMethod(handle, "getConnection", new Class[0]);
    }
}
