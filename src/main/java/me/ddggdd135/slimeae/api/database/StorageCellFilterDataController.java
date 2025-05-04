package me.ddggdd135.slimeae.api.database;

import java.util.*;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.MEStorageCellFilterData;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.inventory.ItemStack;

public class StorageCellFilterDataController extends DatabaseController<MEStorageCellFilterData> {
    public final Set<MEStorageCellFilterData> needSave = new HashSet<>();

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
        saveAllAsync();
        super.shutdown();
    }

    @Override
    public String getTableName() {
        return "ae_storagecell_filter_data";
    }

    @Override
    public void update(MEStorageCellFilterData data) {
        cancelWriteTask(data);
        delete(data);

        for (ItemKey itemKey : data.getFilters()) {
            executeSql("INSERT INTO " + getTableName() + " (uuid, field_name, data) VALUES ('"
                    + data.getUuid().toString() + "', 'filter', '"
                    + SerializeUtils.object2String(itemKey.getItemStack()) + "');");
        }

        executeSql("INSERT INTO " + getTableName() + " (uuid, field_name, data) VALUES ('"
                + data.getUuid().toString() + "', 'reversed', '"
                + SerializeUtils.boolToString(data.isReversed()) + "');");

        executeSql("INSERT INTO " + getTableName() + " (uuid, field_name, data) VALUES ('"
                + data.getUuid().toString() + "', 'fuzzy', '"
                + SerializeUtils.boolToString(data.isFuzzy()) + "');");
    }

    public void delete(MEStorageCellFilterData data) {
        cancelWriteTask(data);
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + data.getUuid() + "';");
    }

    public void delete(UUID uuid) {
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + uuid + "';");
    }

    public void updateAsync(MEStorageCellFilterData data) {
        cancelWriteTask(data);
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
        Set<MEStorageCellFilterData> diff = new HashSet<>(needSave);
        needSave.clear();

        for (MEStorageCellFilterData data : diff) {
            updateAsync(data);
        }
    }
}
