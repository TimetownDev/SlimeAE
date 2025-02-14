package me.ddggdd135.slimeae.api.database;

import java.util.*;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.api.items.MEStorageCellCache;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.inventory.ItemStack;

public class StorageCellDataController extends DatabaseController<MEStorageCellCache> {
    public final Set<MEStorageCellCache> needSave = new HashSet<>();

    public StorageCellDataController() {
        super(MEStorageCellCache.class);
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
        saveAllAsync();
        super.shutdown();
    }

    @Override
    public String getTableName() {
        return "ae_storagecell_data";
    }

    @Override
    public void update(MEStorageCellCache data) {
        cancelWriteTask(data);
        delete(data);

        for (Map.Entry<ItemStack, Long> entry : data.getStorage().entrySet()) {
            executeSql("INSERT INTO " + getTableName() + " (uuid, item_hash, item_base64, amount) VALUES ('"
                    + data.getUuid().toString() + "', '" + getItemHash(entry.getKey()) + "', '"
                    + SerializeUtils.object2String(entry.getKey()) + "', " + entry.getValue() + ");");
        }
    }

    public void update(MEStorageCellCache data, ItemStack itemStack, int amount, boolean insert) {
        if (!insert)
            executeSql("UPDATE " + getTableName() + " SET amount = " + amount + " WHERE uuid = '" + data.getUuid()
                    + "' AND item_hash = " + getItemHash(itemStack) + ";");
        else
            executeSql("INSERT INTO " + getTableName() + " (uuid, item_hash, item_base64, amount) VALUES ('"
                    + data.getUuid().toString() + "', '" + getItemHash(itemStack) + "', '"
                    + SerializeUtils.object2String(itemStack) + "', " + amount + ");");
    }

    public void delete(MEStorageCellCache data) {
        cancelWriteTask(data);
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + data.getUuid() + "';");
    }

    public void delete(UUID uuid) {
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + uuid + "';");
    }

    public void delete(MEStorageCellCache data, ItemStack itemStack) {
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + data.getUuid() + "' AND item_hash = "
                + getItemHash(itemStack) + ";");
    }

    public void updateAsync(MEStorageCellCache data) {
        cancelWriteTask(data);
        submitWriteTask(data, () -> update(data));
    }

    public void updateAsync(MEStorageCellCache data, ItemStack itemStack, int amount, boolean insert) {
        submitWriteTask(data, () -> update(data, itemStack, amount, insert));
    }

    public void deleteAsync(MEStorageCellCache data) {
        cancelWriteTask(data);
        submitWriteTask(data, () -> delete(data));
    }

    public void deleteAsync(UUID uuid) {
        submitWriteTask(null, () -> delete(uuid));
    }

    public void deleteAsync(MEStorageCellCache data, ItemStack itemStack) {
        submitWriteTask(data, () -> delete(data, itemStack));
    }

    protected long getItemHash(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() == 0) return 0;

        return SerializeUtils.getItemHash(itemStack);
    }

    public MEStorageCellCache loadData(ItemStack itemStack) {
        MEStorageCellCache storageCellCache = new MEStorageCellCache(itemStack);
        Map<ItemStack, Long> storage = storageCellCache.getSourceStorage();
        long stored = 0;
        List<Map<String, String>> data = execQuery("SELECT * FROM " + getTableName() + " WHERE uuid = '"
                + storageCellCache.getUuid().toString() + "';");
        for (Map<String, String> itemData : data) {
            ItemStack item = (ItemStack) SerializeUtils.string2Object(itemData.get("item_base64"));
            long amount = Long.parseLong(itemData.get("amount"));
            if (storage.containsKey(itemStack)) {
                amount += storage.get(item);
            }

            storage.put(item, amount);
            stored += amount;
        }

        storageCellCache.updateStored(stored);

        return storageCellCache;
    }

    public void markDirty(MEStorageCellCache data) {
        needSave.add(data);
    }

    public void saveAllAsync() {
        Set<MEStorageCellCache> diff = new HashSet<>(needSave);
        needSave.clear();
        for (MEStorageCellCache data : diff) {
            updateAsync(data);
        }
    }
}
