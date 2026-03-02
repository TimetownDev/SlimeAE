package me.ddggdd135.slimeae.api.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import org.bukkit.Location;

public class ReskinDataController extends DatabaseController<String> {

    private final Map<String, String[]> cache = new ConcurrentHashMap<>();

    public final Set<String> needSave = ConcurrentHashMap.newKeySet();

    public ReskinDataController() {
        super(String.class);
    }

    @Override
    public String getTableName() {
        return "ae_reskin_data";
    }

    @Override
    public void init() {
        super.init();
        executeSql("CREATE TABLE IF NOT EXISTS " + getTableName() + "("
                + "world VARCHAR(128) NOT NULL, "
                + "x INT NOT NULL, "
                + "y INT NOT NULL, "
                + "z INT NOT NULL, "
                + "reskin_type VARCHAR(16) NOT NULL, "
                + "reskin_value TEXT NOT NULL, "
                + "PRIMARY KEY (world, x, y, z)"
                + ");");
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void shutdown() {
        saveAllAsync();
        super.shutdown();
    }

    @Nonnull
    private static String toKey(@Nonnull Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
                + location.getBlockZ();
    }

    /**
     * 从缓存获取 reskin 数据（优先内存，未命中则从 DB 加载）
     * @return [type, value] 或 null
     */
    @Nullable public String[] getReskinData(@Nonnull Location location) {
        String key = toKey(location);
        String[] cached = cache.get(key);
        if (cached != null) return cached;

        // 从数据库加载
        String[] loaded = loadFromDb(location);
        if (loaded != null) {
            cache.put(key, loaded);
        }
        return loaded;
    }

    @Nullable private String[] loadFromDb(@Nonnull Location location) {
        if (location.getWorld() == null) return null;
        List<Map<String, String>> result = execQuery("SELECT reskin_type, reskin_value FROM " + getTableName()
                + " WHERE world = '" + location.getWorld().getName().replace("'", "''") + "'"
                + " AND x = " + location.getBlockX()
                + " AND y = " + location.getBlockY()
                + " AND z = " + location.getBlockZ());
        if (result.isEmpty()) return null;
        Map<String, String> row = result.get(0);
        return new String[] {row.get("reskin_type"), row.get("reskin_value")};
    }

    /**
     * 设置 reskin 数据（写入缓存 + 标脏）
     */
    public void setReskinData(@Nonnull Location location, @Nonnull String type, @Nonnull String value) {
        String key = toKey(location);
        cache.put(key, new String[] {type, value});
        needSave.add(key);
    }

    /**
     * 移除 reskin 数据
     */
    public void removeReskinData(@Nonnull Location location) {
        String key = toKey(location);
        cache.remove(key);
        needSave.remove(key);

        if (location.getWorld() == null) return;
        String worldName = location.getWorld().getName().replace("'", "''");
        submitWriteTask(key, () -> {
            executeSql("DELETE FROM " + getTableName() + " WHERE world = '" + worldName + "'"
                    + " AND x = " + location.getBlockX()
                    + " AND y = " + location.getBlockY()
                    + " AND z = " + location.getBlockZ());
        });
    }

    @Override
    public void update(String key) {
        String[] data = cache.get(key);
        if (data == null) return;

        String[] parts = key.split(",", 4);
        if (parts.length != 4) return;

        String world = parts[0];
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);

        cancelWriteTask(key);
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM " + getTableName() + " WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
                    deleteStmt.setString(1, world);
                    deleteStmt.setInt(2, x);
                    deleteStmt.setInt(3, y);
                    deleteStmt.setInt(4, z);
                    deleteStmt.executeUpdate();
                }
                try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO " + getTableName()
                        + " (world, x, y, z, reskin_type, reskin_value) VALUES (?, ?, ?, ?, ?, ?)")) {
                    insertStmt.setString(1, world);
                    insertStmt.setInt(2, x);
                    insertStmt.setInt(3, y);
                    insertStmt.setInt(4, z);
                    insertStmt.setString(5, data[0]);
                    insertStmt.setString(6, data[1]);
                    insertStmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to update reskin data: " + e.getMessage());
        }
    }

    public void updateAsync(String key) {
        cancelWriteTask(key);
        submitWriteTask(key, () -> update(key));
    }

    public void saveAllAsync() {
        Set<String> diff = new HashSet<>();
        needSave.removeIf(item -> {
            diff.add(item);
            return true;
        });

        for (String key : diff) {
            updateAsync(key);
        }
    }
}
