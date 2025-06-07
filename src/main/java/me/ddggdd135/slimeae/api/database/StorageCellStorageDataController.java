package me.ddggdd135.slimeae.api.database;

import java.util.*;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.slimeae.api.MEStorageCellStorageData;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.inventory.ItemStack;

public class StorageCellStorageDataController extends DatabaseController<MEStorageCellStorageData> {
    public final Set<MEStorageCellStorageData> needSave = new HashSet<>();

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
        saveAllAsync();
        super.shutdown();
    }

    @Override
    public String getTableName() {
        return "ae_storagecell_data";
    }

    @Override
    public void update(MEStorageCellStorageData data) {
        cancelWriteTask(data);
        delete(data);

        for (Map.Entry<ItemStack, Long> entry : data.getStorage().entrySet()) {
            executeSql("INSERT INTO " + getTableName() + " (uuid, item_hash, item_base64, amount) VALUES ('"
                    + data.getUuid().toString() + "', '" + getItemHash(entry.getKey()) + "', '"
                    + SerializeUtils.object2String(entry.getKey()) + "', " + entry.getValue() + ");");
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
        cancelWriteTask(data);
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
        Set<MEStorageCellStorageData> diff = new HashSet<>(needSave);
        needSave.clear();

        for (MEStorageCellStorageData data : diff) {
            updateAsync(data);
        }
    }
}
