package me.ddggdd135.slimeae.api.database.v3;

import java.util.logging.Logger;

public class ConnectionManagerFactory {
    private static final Logger logger = Logger.getLogger("SlimeAE-ConnFactory");

    private ConnectionManagerFactory() {}

    public static ConnectionManager create(StorageConfig config) {
        if (!config.isIndependentPool()) {
            logger.warning("Using Slimefun shared connection pool. Consider enabling independent-pool in config.yml");
            return new LegacyConnectionManager();
        }
        if (config.isMysql()) {
            return new MysqlConnectionManager(config);
        }
        return new SqliteConnectionManager(config);
    }
}
