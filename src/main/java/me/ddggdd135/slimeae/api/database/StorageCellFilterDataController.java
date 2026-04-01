package me.ddggdd135.slimeae.api.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.MEStorageCellFilterData;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.inventory.ItemStack;

public class StorageCellFilterDataController extends DatabaseController<MEStorageCellFilterData> {
    public final Set<MEStorageCellFilterData> needSave = ConcurrentHashMap.newKeySet();

    public StorageCellFilterDataController() {
        super(MEStorageCellFilterData.class);
    }

    @Override
    public void init() {
        super.init();

        executeSql("CREATE TABLE IF NOT EXISTS "
                + getTableName()
                + "("
                + "uuid CHAR(64) NOT NULL, "
                + "field_name TEXT, "
                + "data TEXT"
                + ");");
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void shutdown() {
        saveAllSync();
        super.shutdown();
    }

    public void saveAllSync() {
        Set<MEStorageCellFilterData> diff = new HashSet<>();
        needSave.removeIf(item -> {
            diff.add(item);
            return true;
        });

        if (!diff.isEmpty()) {
            logger.log(Level.INFO, "正在同步保存 {0} 筛选器数据...", diff.size());
            for (MEStorageCellFilterData data : diff) {
                try {
                    update(data);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "同步保存筛选器数据失败 (" + data.getUuid() + "): " + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public String getTableName() {
        return "ae_storagecell_filter_data";
    }

    @Override
    public void update(MEStorageCellFilterData data) {
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement deleteStmt =
                        conn.prepareStatement("DELETE FROM " + getTableName() + " WHERE uuid = ?")) {
                    deleteStmt.setString(1, data.getUuid().toString());
                    deleteStmt.executeUpdate();
                }
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO " + getTableName() + " (uuid, field_name, data) VALUES (?, ?, ?)")) {
                    for (ItemKey itemKey : data.getFilters()) {
                        insertStmt.setString(1, data.getUuid().toString());
                        insertStmt.setString(2, "filter");
                        insertStmt.setString(3, SerializeUtils.object2String(itemKey.getItemStack()));
                        insertStmt.addBatch();
                    }
                    insertStmt.setString(1, data.getUuid().toString());
                    insertStmt.setString(2, "reversed");
                    insertStmt.setString(3, SerializeUtils.boolToString(data.isReversed()));
                    insertStmt.addBatch();
                    insertStmt.setString(1, data.getUuid().toString());
                    insertStmt.setString(2, "fuzzy");
                    insertStmt.setString(3, SerializeUtils.boolToString(data.isFuzzy()));
                    insertStmt.addBatch();
                    insertStmt.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to batch update filter data: " + e.getMessage());
        }
    }

    public void delete(MEStorageCellFilterData data) {
        cancelWriteTask(data);
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + data.getUuid() + "';");
    }

    public void delete(UUID uuid) {
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + uuid + "';");
    }

    public void updateAsync(MEStorageCellFilterData data) {
        submitWriteTask(data, () -> update(data));
    }

    public void deleteAsync(MEStorageCellFilterData data) {
        cancelWriteTask(data);
        submitWriteTask(data, () -> delete(data));
    }

    public void deleteAsync(UUID uuid) {
        submitWriteTask(null, () -> delete(uuid));
    }

    public MEStorageCellFilterData loadData(ItemStack itemStack) {
        MEStorageCellFilterData storageCellData = new MEStorageCellFilterData(itemStack);

        List<Map<String, String>> data = execQuery("SELECT * FROM " + getTableName() + " WHERE uuid = '"
                + storageCellData.getUuid().toString() + "';");
        for (Map<String, String> itemData : data) {
            String fieldName = itemData.get("field_name");
            String fieldData = itemData.get("data");
            if (fieldName.equals("filter")) {
                ItemStack item = (ItemStack) SerializeUtils.string2Object(fieldData);
                if (item == null) continue;
                storageCellData.getFilters().add(item);
            }

            if (fieldName.equals("reversed")) {
                storageCellData.setReversed(SerializeUtils.stringToBool(fieldData));
            }

            if (fieldName.equals("fuzzy")) {
                storageCellData.setFuzzy(SerializeUtils.stringToBool(fieldData));
            }
        }

        storageCellData.updateItemTypes();

        return storageCellData;
    }

    public void markDirty(MEStorageCellFilterData data) {
        needSave.add(data);
    }

    public void saveAllAsync() {
        Set<MEStorageCellFilterData> diff = new HashSet<>();
        needSave.removeIf(item -> {
            diff.add(item);
            return true;
        });

        for (MEStorageCellFilterData data : diff) {
            updateAsync(data);
        }
    }
}
