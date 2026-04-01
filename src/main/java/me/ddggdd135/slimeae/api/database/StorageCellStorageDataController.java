package me.ddggdd135.slimeae.api.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.slimeae.api.MEStorageCellStorageData;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.inventory.ItemStack;

public class StorageCellStorageDataController extends DatabaseController<MEStorageCellStorageData> {
    public final Set<MEStorageCellStorageData> needSave = ConcurrentHashMap.newKeySet();

    public StorageCellStorageDataController() {
        super(MEStorageCellStorageData.class);
    }

    @Override
    public void init() {
        super.init();

        executeSql("CREATE TABLE IF NOT EXISTS "
                + getTableName()
                + "("
                + "uuid CHAR(64) NOT NULL, "
                + "item_hash BIGINT, "
                + "item_base64 TEXT, "
                + "amount BIGINT"
                + ");");
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void shutdown() {
        saveAllSync();
        super.shutdown();
    }

    public void saveAllSync() {
        Set<MEStorageCellStorageData> diff = new HashSet<>();
        needSave.removeIf(item -> {
            diff.add(item);
            return true;
        });

        if (!diff.isEmpty()) {
            logger.log(Level.INFO, "正在同步保存 {0} 存储元件数据...", diff.size());
            for (MEStorageCellStorageData data : diff) {
                try {
                    update(data);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "同步保存存储元件数据失败 (" + data.getUuid() + "): " + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public String getTableName() {
        return "ae_storagecell_data";
    }

    @Override
    public void update(MEStorageCellStorageData data) {
        Map<ItemStack, Long> snapshot;
        try {
            // 无需 synchronized：ItemHashMap 在快照时的短暂不一致是可接受的，
            // 因为 dirtyTracker 持有精确的增量数据，下次 flush 会修正
            snapshot = new LinkedHashMap<>(data.getStorage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "无法快照存储元件数据 " + data.getUuid() + ": " + e.getMessage(), e);
            return;
        }

        String uuid = data.getUuid().toString();
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement deleteStmt =
                        conn.prepareStatement("DELETE FROM " + getTableName() + " WHERE uuid = ?")) {
                    deleteStmt.setString(1, uuid);
                    deleteStmt.executeUpdate();
                }
                try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO " + getTableName()
                        + " (uuid, item_hash, item_base64, amount) VALUES (?, ?, ?, ?)")) {
                    int batch = 0;
                    for (Map.Entry<ItemStack, Long> entry : snapshot.entrySet()) {
                        if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) continue;
                        insertStmt.setString(1, uuid);
                        insertStmt.setLong(2, getItemHash(entry.getKey()));
                        insertStmt.setString(3, SerializeUtils.object2String(entry.getKey()));
                        insertStmt.setLong(4, entry.getValue());
                        insertStmt.addBatch();
                        if (++batch % 500 == 0) {
                            insertStmt.executeBatch();
                        }
                    }
                    if (batch % 500 != 0) {
                        insertStmt.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.log(Level.SEVERE, "回滚存储元件数据失败 " + uuid + ": " + rollbackEx.getMessage(), rollbackEx);
                }
                throw e;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "无法更新存储元件数据 " + uuid + ": " + e.getMessage(), e);
        }
    }

    public void update(MEStorageCellStorageData data, ItemStack itemStack, int amount, boolean insert) {
        if (!insert)
            executeSql("UPDATE " + getTableName() + " SET amount = " + amount + " WHERE uuid = '" + data.getUuid()
                    + "' AND item_hash = " + getItemHash(itemStack) + ";");
        else
            executeSql("INSERT INTO " + getTableName() + " (uuid, item_hash, item_base64, amount) VALUES ('"
                    + data.getUuid().toString() + "', '" + getItemHash(itemStack) + "', '"
                    + SerializeUtils.object2String(itemStack) + "', " + amount + ");");
    }

    public void delete(MEStorageCellStorageData data) {
        cancelWriteTask(data);
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + data.getUuid() + "';");
    }

    public void delete(UUID uuid) {
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + uuid + "';");
    }

    public void delete(MEStorageCellStorageData data, ItemStack itemStack) {
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + data.getUuid() + "' AND item_hash = "
                + getItemHash(itemStack) + ";");
    }

    public void updateAsync(MEStorageCellStorageData data) {
        submitWriteTask(data, () -> update(data));
    }

    public void updateAsync(MEStorageCellStorageData data, ItemStack itemStack, int amount, boolean insert) {
        submitWriteTask(data, () -> update(data, itemStack, amount, insert));
    }

    public void deleteAsync(MEStorageCellStorageData data) {
        cancelWriteTask(data);
        submitWriteTask(data, () -> delete(data));
    }

    public void deleteAsync(UUID uuid) {
        submitWriteTask(null, () -> delete(uuid));
    }

    public void deleteAsync(MEStorageCellStorageData data, ItemStack itemStack) {
        submitWriteTask(data, () -> delete(data, itemStack));
    }

    protected long getItemHash(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() == 0) return 0;

        return SerializeUtils.getItemHash(itemStack);
    }

    public MEStorageCellStorageData loadData(ItemStack itemStack) {
        MEStorageCellStorageData storageCellData = new MEStorageCellStorageData(itemStack);
        ItemHashMap<Long> storage = storageCellData.getStorage();
        long stored = 0;
        List<Map<String, String>> data = execQuery("SELECT * FROM " + getTableName() + " WHERE uuid = '"
                + storageCellData.getUuid().toString() + "';");
        for (Map<String, String> itemData : data) {
            ItemStack item = (ItemStack) SerializeUtils.string2Object(itemData.get("item_base64"));
            if (item == null || item.getType().isAir()) continue;
            long amount = Long.parseLong(itemData.get("amount"));
            if (storage.containsKey(item)) {
                amount += storage.get(item);
            }

            storage.put(item, amount);
            stored += amount;
        }

        storageCellData.setStored(stored);

        return storageCellData;
    }

    public void markDirty(MEStorageCellStorageData data) {
        needSave.add(data);
    }

    public void saveAllAsync() {
        Set<MEStorageCellStorageData> diff = new HashSet<>();
        needSave.removeIf(item -> {
            diff.add(item);
            return true;
        });

        for (MEStorageCellStorageData data : diff) {
            updateAsync(data);
        }
    }
}
