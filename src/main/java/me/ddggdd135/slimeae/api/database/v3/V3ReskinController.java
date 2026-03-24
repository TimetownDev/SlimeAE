package me.ddggdd135.slimeae.api.database.v3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Location;

public class V3ReskinController {
    private static final Logger logger = Logger.getLogger("SlimeAE-V3Reskin");
    private final Map<String, String[]> cache = new ConcurrentHashMap<>();
    private final Set<String> needSave = ConcurrentHashMap.newKeySet();
    private final Set<String> needDelete = ConcurrentHashMap.newKeySet();
    private final ConnectionManager connMgr;
    private final DDLProvider ddl;

    public V3ReskinController(ConnectionManager connMgr, DDLProvider ddl) {
        this.connMgr = connMgr;
        this.ddl = ddl;
    }

    @Nonnull
    private static String toKey(@Nonnull Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
                + location.getBlockZ();
    }

    @Nullable public String[] getReskinData(@Nonnull Location location) {
        String key = toKey(location);
        String[] cached = cache.get(key);
        if (cached != null) return cached;

        String[] loaded = loadFromDb(location);
        if (loaded != null) {
            cache.put(key, loaded);
        }
        return loaded;
    }

    @Nullable private String[] loadFromDb(@Nonnull Location location) {
        if (location.getWorld() == null) return null;
        String sql = "SELECT reskin_type, reskin_value FROM ae_v3_reskin_data "
                + "WHERE world = ? AND x = ? AND y = ? AND z = ?";
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location.getWorld().getName());
            ps.setInt(2, location.getBlockX());
            ps.setInt(3, location.getBlockY());
            ps.setInt(4, location.getBlockZ());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[] {rs.getString("reskin_type"), rs.getString("reskin_value")};
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Load reskin data error: " + e.getMessage(), e);
        }
        return null;
    }

    public void setReskinData(@Nonnull Location location, @Nonnull String type, @Nonnull String value) {
        String key = toKey(location);
        cache.put(key, new String[] {type, value});
        needDelete.remove(key);
        needSave.add(key);
    }

    public void removeReskinData(@Nonnull Location location) {
        String key = toKey(location);
        cache.remove(key);
        needSave.remove(key);
        needDelete.add(key);
    }

    public void saveAllSync() {
        Set<String> toSave = new HashSet<>();
        needSave.removeIf(item -> {
            toSave.add(item);
            return true;
        });
        Set<String> toDelete = new HashSet<>();
        needDelete.removeIf(item -> {
            toDelete.add(item);
            return true;
        });

        for (String key : toSave) {
            saveKey(key);
        }
        for (String key : toDelete) {
            deleteKey(key);
        }
    }

    public void saveAllAsync() {
        CompletableFuture.runAsync(this::saveAllSync);
    }

    private void saveKey(String key) {
        String[] data = cache.get(key);
        if (data == null) return;

        String[] parts = key.split(",", 4);
        if (parts.length != 4) return;

        String world = parts[0];
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);

        String sql = ddl.upsertReskin();
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            ps.setString(5, data[0]);
            ps.setString(6, data[1]);
            ps.setInt(7, CRC32Utils.computeReskin(world, x, y, z, data[0], data[1]));
            ps.setLong(8, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Save reskin data error: " + e.getMessage(), e);
        }
    }

    private void deleteKey(String key) {
        String[] parts = key.split(",", 4);
        if (parts.length != 4) return;

        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM ae_v3_reskin_data WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
            ps.setString(1, parts[0]);
            ps.setInt(2, Integer.parseInt(parts[1]));
            ps.setInt(3, Integer.parseInt(parts[2]));
            ps.setInt(4, Integer.parseInt(parts[3]));
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Delete reskin data error: " + e.getMessage(), e);
        }
    }

    public void shutdown() {
        saveAllSync();
    }
}
