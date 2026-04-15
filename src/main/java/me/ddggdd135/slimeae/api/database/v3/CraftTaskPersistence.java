package me.ddggdd135.slimeae.api.database.v3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.*;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import me.ddggdd135.slimeae.api.items.StorageCollection;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.Location;

public class CraftTaskPersistence {
    private static final Logger logger = Logger.getLogger("SlimeAE-CraftTask");
    private final ConnectionManager connMgr;
    private final boolean mysql;

    public CraftTaskPersistence(V3DatabaseManager dbManager) {
        this.connMgr = dbManager.getConnectionManager();
        this.mysql = connMgr.isMysql();
    }

    public void initSchema() {
        try (Connection conn = connMgr.getWriteConnection()) {
            String ddl;
            if (mysql) {
                ddl = "CREATE TABLE IF NOT EXISTS ae_v3_craft_tasks ("
                        + "task_id VARCHAR(36) PRIMARY KEY, "
                        + "controller_world VARCHAR(128) NOT NULL, "
                        + "controller_x INT NOT NULL, "
                        + "controller_y INT NOT NULL, "
                        + "controller_z INT NOT NULL, "
                        + "recipe_data MEDIUMBLOB NOT NULL, "
                        + "task_count BIGINT NOT NULL, "
                        + "steps_data MEDIUMBLOB NOT NULL, "
                        + "deps_data MEDIUMBLOB NOT NULL, "
                        + "completed_idx TEXT NOT NULL, "
                        + "storage_data MEDIUMBLOB NOT NULL, "
                        + "global_fail INT NOT NULL DEFAULT 0, "
                        + "cancel_fail INT NOT NULL DEFAULT 0, "
                        + "is_cancelling TINYINT NOT NULL DEFAULT 0, "
                        + "created_at BIGINT NOT NULL, "
                        + "suspended_at BIGINT NOT NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            } else {
                ddl = "CREATE TABLE IF NOT EXISTS ae_v3_craft_tasks ("
                        + "task_id TEXT PRIMARY KEY, "
                        + "controller_world TEXT NOT NULL, "
                        + "controller_x INTEGER NOT NULL, "
                        + "controller_y INTEGER NOT NULL, "
                        + "controller_z INTEGER NOT NULL, "
                        + "recipe_data BLOB NOT NULL, "
                        + "task_count INTEGER NOT NULL, "
                        + "steps_data BLOB NOT NULL, "
                        + "deps_data BLOB NOT NULL, "
                        + "completed_idx TEXT NOT NULL, "
                        + "storage_data BLOB NOT NULL, "
                        + "global_fail INTEGER NOT NULL DEFAULT 0, "
                        + "cancel_fail INTEGER NOT NULL DEFAULT 0, "
                        + "is_cancelling INTEGER NOT NULL DEFAULT 0, "
                        + "created_at INTEGER NOT NULL, "
                        + "suspended_at INTEGER NOT NULL)";
            }
            conn.createStatement().execute(ddl);

            String idx;
            if (mysql) {
                idx =
                        "CREATE INDEX idx_craft_tasks_ctrl ON ae_v3_craft_tasks(controller_world, controller_x, controller_y, controller_z)";
                try {
                    conn.createStatement().execute(idx);
                } catch (SQLException ignored) {
                }
            } else {
                idx =
                        "CREATE INDEX IF NOT EXISTS idx_craft_tasks_ctrl ON ae_v3_craft_tasks(controller_world, controller_x, controller_y, controller_z)";
                conn.createStatement().execute(idx);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create ae_v3_craft_tasks table", e);
        }
    }

    public void save(AutoCraftingTask task) {
        String upsert;
        if (mysql) {
            upsert = "INSERT INTO ae_v3_craft_tasks "
                    + "(task_id, controller_world, controller_x, controller_y, controller_z, "
                    + "recipe_data, task_count, steps_data, deps_data, completed_idx, "
                    + "storage_data, global_fail, cancel_fail, is_cancelling, created_at, suspended_at) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "recipe_data=VALUES(recipe_data), task_count=VALUES(task_count), "
                    + "steps_data=VALUES(steps_data), deps_data=VALUES(deps_data), "
                    + "completed_idx=VALUES(completed_idx), storage_data=VALUES(storage_data), "
                    + "global_fail=VALUES(global_fail), cancel_fail=VALUES(cancel_fail), "
                    + "is_cancelling=VALUES(is_cancelling), suspended_at=VALUES(suspended_at)";
        } else {
            upsert = "INSERT OR REPLACE INTO ae_v3_craft_tasks "
                    + "(task_id, controller_world, controller_x, controller_y, controller_z, "
                    + "recipe_data, task_count, steps_data, deps_data, completed_idx, "
                    + "storage_data, global_fail, cancel_fail, is_cancelling, created_at, suspended_at) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        }
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps = conn.prepareStatement(upsert)) {
            Location ctrl = task.getNetworkInfo().getController();
            ps.setString(1, task.getTaskId().toString());
            ps.setString(2, ctrl.getWorld().getName());
            ps.setInt(3, ctrl.getBlockX());
            ps.setInt(4, ctrl.getBlockY());
            ps.setInt(5, ctrl.getBlockZ());
            ps.setBytes(6, CraftTaskSerializer.serializeRecipe(task.getRecipe()));
            ps.setLong(7, task.getCount());
            ps.setBytes(8, CraftTaskSerializer.serializeSteps(task.getCraftingSteps()));
            ps.setBytes(9, CraftTaskSerializer.serializeDeps(task.getStepDependencies(), task.getCraftingSteps()));
            ps.setString(
                    10, CraftTaskSerializer.serializeCompletedSteps(task.getCompletedSteps(), task.getCraftingSteps()));
            ps.setBytes(11, CraftTaskSerializer.serializeStorage(task.getStorage()));
            ps.setInt(12, task.getGlobalFailTimes());
            ps.setInt(13, task.getCancelFailTimes());
            ps.setInt(14, task.isCancelling() ? 1 : 0);
            ps.setLong(15, task.getCreatedAt());
            ps.setLong(16, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save craft task " + task.getTaskId(), e);
        }
    }

    public void saveAllSync(Collection<AutoCraftingTask> tasks) {
        for (AutoCraftingTask task : tasks) {
            save(task);
        }
    }

    public void delete(UUID taskId) {
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM ae_v3_craft_tasks WHERE task_id = ?")) {
            ps.setString(1, taskId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to delete craft task " + taskId, e);
        }
    }

    public int tryRestore(NetworkInfo info) {
        Location ctrl = info.getController();
        String worldName = ctrl.getWorld().getName();
        int restored = 0;

        List<TaskRecord> records = new ArrayList<>();
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM ae_v3_craft_tasks WHERE controller_world = ? AND controller_x = ? AND controller_y = ? AND controller_z = ?")) {
            ps.setString(1, worldName);
            ps.setInt(2, ctrl.getBlockX());
            ps.setInt(3, ctrl.getBlockY());
            ps.setInt(4, ctrl.getBlockZ());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TaskRecord rec = new TaskRecord();
                    rec.taskId = UUID.fromString(rs.getString("task_id"));
                    rec.recipeData = rs.getBytes("recipe_data");
                    rec.taskCount = rs.getLong("task_count");
                    rec.stepsData = rs.getBytes("steps_data");
                    rec.depsData = rs.getBytes("deps_data");
                    rec.completedIdx = rs.getString("completed_idx");
                    rec.storageData = rs.getBytes("storage_data");
                    rec.globalFail = rs.getInt("global_fail");
                    rec.cancelFail = rs.getInt("cancel_fail");
                    rec.isCancelling = rs.getInt("is_cancelling") != 0;
                    rec.createdAt = rs.getLong("created_at");
                    records.add(rec);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to query suspended tasks for " + worldName + " " + ctrl, e);
            return 0;
        }

        for (TaskRecord rec : records) {
            try {
                CraftingRecipe recipe = CraftTaskSerializer.deserializeRecipe(rec.recipeData);
                if (recipe == null) {
                    logger.warning("Cannot restore task " + rec.taskId + ": recipe deserialization failed");
                    delete(rec.taskId);
                    continue;
                }

                List<CraftStep> steps = CraftTaskSerializer.deserializeSteps(rec.stepsData);
                if (steps == null || steps.isEmpty()) {
                    logger.warning("Cannot restore task " + rec.taskId + ": steps deserialization failed");
                    delete(rec.taskId);
                    continue;
                }

                Map<CraftStep, Set<CraftStep>> deps = CraftTaskSerializer.deserializeDeps(rec.depsData, steps);
                Set<Integer> completedIndices = CraftTaskSerializer.deserializeCompletedIndices(rec.completedIdx);
                Set<CraftStep> completedSteps = new HashSet<>();
                for (int idx : completedIndices) {
                    if (idx >= 0 && idx < steps.size()) {
                        completedSteps.add(steps.get(idx));
                    }
                }
                ItemStorage storage = CraftTaskSerializer.deserializeStorage(rec.storageData);

                for (CraftStep step : steps) {
                    if (step.getVirtualRunning() > 0) {
                        ItemHashMap<Long> refund = new ItemHashMap<>();
                        for (Map.Entry<ItemKey, Long> entry :
                                step.getRecipe().getInputAmounts().keyEntrySet()) {
                            refund.putKey(entry.getKey(), entry.getValue() * step.getVirtualRunning());
                        }
                        storage.addItem(refund);
                        step.setAmount(step.getAmount() + step.getVirtualRunning());
                        step.setVirtualRunning(0);
                        step.setVirtualProcess(0);
                    }

                    Set<Location> toRemove = new HashSet<>();
                    for (Location deviceLoc : step.getRunningDevices()) {
                        if (!deviceLoc.isChunkLoaded()) {
                            ItemHashMap<Long> refund = new ItemHashMap<>();
                            for (Map.Entry<ItemKey, Long> entry :
                                    step.getRecipe().getInputAmounts().keyEntrySet()) {
                                refund.putKey(entry.getKey(), entry.getValue());
                            }
                            storage.addItem(refund);
                            toRemove.add(deviceLoc);
                        }
                    }
                    for (Location loc : toRemove) {
                        step.removeRunningDevice(loc);
                    }
                }

                AutoCraftingTask task = AutoCraftingTask.restore(
                        rec.taskId,
                        info,
                        recipe,
                        rec.taskCount,
                        steps,
                        deps,
                        completedSteps,
                        storage,
                        rec.globalFail,
                        rec.cancelFail,
                        rec.isCancelling,
                        rec.createdAt);

                task.start();
                delete(rec.taskId);
                restored++;
                logger.info("Restored craft task " + rec.taskId);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to restore task " + rec.taskId + ", returning materials", e);
                returnMaterialsFromRecord(rec, info);
                delete(rec.taskId);
            }
        }
        return restored;
    }

    public void backupAll() {
        Set<NetworkInfo> allNetworks = new HashSet<>(SlimeAEPlugin.getNetworkData().AllNetworkData);
        for (NetworkInfo info : allNetworks) {
            if (info.isDisposed()) continue;
            for (AutoCraftingTask task : info.getAutoCraftingSessions()) {
                if (task.getTaskState() == TaskState.RUNNING) {
                    try {
                        save(task);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to backup task " + task.getTaskId(), e);
                    }
                }
            }
        }
    }

    public void cleanupExpired(long maxAgeMs) {
        long cutoff = System.currentTimeMillis() - maxAgeMs;
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM ae_v3_craft_tasks WHERE suspended_at < ? AND suspended_at > 0")) {
            ps.setLong(1, cutoff);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                logger.info("Cleaned up " + deleted + " expired suspended craft tasks");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to cleanup expired tasks", e);
        }
    }

    private void returnMaterialsFromRecord(TaskRecord rec, NetworkInfo info) {
        try {
            ItemStorage storage = CraftTaskSerializer.deserializeStorage(rec.storageData);
            ItemHashMap<Long> toReturn = new ItemHashMap<>(storage.getStorageUnsafe());
            StorageCollection currentStorage = info.getStorage();
            currentStorage.clearNotIncluded();
            currentStorage.clearTakeAndPushCache();
            currentStorage.pushItem(toReturn);
            ItemUtils.trim(toReturn);
            if (!toReturn.isEmpty()) {
                synchronized (info) {
                    info.getTempStorage().addItem(toReturn, true);
                }
            }
            currentStorage.invalidateStorageCache();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to return materials for task " + rec.taskId, e);
        }
    }

    private static class TaskRecord {
        UUID taskId;
        byte[] recipeData;
        long taskCount;
        byte[] stepsData;
        byte[] depsData;
        String completedIdx;
        byte[] storageData;
        int globalFail;
        int cancelFail;
        boolean isCancelling;
        long createdAt;
    }
}
