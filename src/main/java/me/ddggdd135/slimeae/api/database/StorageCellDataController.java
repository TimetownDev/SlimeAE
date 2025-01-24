package me.ddggdd135.slimeae.api.database;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import me.ddggdd135.slimeae.api.MEStorageCellCache;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class StorageCellDataController extends DatabaseController<MEStorageCellCache>{
    private final Map<ItemStack, Long> cache;

    public StorageCellDataController() {
        super(MEStorageCellCache.class);
        cache = new HashMap<>();
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
    public String getTableName() {
        return "STORAGECELL_DATA";
    }

    @Override
    public void update(MEStorageCellCache data) {
        delete(data);

        for (Map.Entry<ItemStack, Integer> entry : data.getStorage().entrySet()) {
            executeSql("INSERT INTO " + getTableName() + " (uuid, item_hash, item_base64, amount) VALUES ('" + data.getUuid().toString() + "', '" + getItemHash(entry.getKey()) + "', '" + SerializeUtils.object2String(entry.getKey()) + "', " + entry.getValue() + ");");
        }
    }

    public void update(MEStorageCellCache data, ItemStack itemStack, int amount, boolean insert) {
        if (!insert)
            executeSql("UPDATE " + getTableName() + " SET amount = " + amount + " WHERE uuid = '" + data.getUuid() + "' AND item_hash = " + getItemHash(itemStack) + ";");
        else
            executeSql("INSERT INTO " + getTableName() + " (uuid, item_hash, item_base64, amount) VALUES ('" + data.getUuid().toString() + "', '" + getItemHash(itemStack) + "', '" + SerializeUtils.object2String(itemStack) + "', " + amount + ");");
    }

    public void delete(MEStorageCellCache data) {
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + data.getUuid() + "';");
    }

    public void delete(MEStorageCellCache data, ItemStack itemStack) {
        executeSql("DELETE FROM " + getTableName() + " WHERE uuid = '" + data.getUuid() + "' AND item_hash = " + getItemHash(itemStack) + ";");
    }


    public void updateAsync(MEStorageCellCache data) {
        submitWriteTask(data, ()->{update(data);});
    }

    public void updateAsync(MEStorageCellCache data, ItemStack itemStack, int amount, boolean insert) {
        submitWriteTask(data, ()->{update(data, itemStack, amount, insert);});
    }

    public void deleteAsync(MEStorageCellCache data) {
        submitWriteTask(data, ()->{delete(data);});
    }

    public void deleteAsync(MEStorageCellCache data, ItemStack itemStack) {
        submitWriteTask(data, ()->{delete(data, itemStack);});
    }

    public void submitWriteTask(MEStorageCellCache data, Runnable runnable) {
        int tasks = 0;
        if (scheduledWriteTasks.containsKey(data)) {
            tasks = scheduledWriteTasks.get(data);
        }
        scheduledWriteTasks.put(data, tasks + 1);
        this.tasks.incrementAndGet();
        writeExecutor.submit(() -> {
            try {
                if (scheduledWriteTasks.containsKey(data))
                    runnable.run();
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage());
            } finally {
                int tasks1 = scheduledWriteTasks.get(data);
                scheduledWriteTasks.put(data, tasks1 - 1);
                this.tasks.decrementAndGet();
            }
        });

    }

    protected long getItemHash(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() == 0) return 0;

        if (cache.containsKey(itemStack)) {
            return cache.get(itemStack);
        }

        long hash = SerializeUtils.getItemHash(itemStack);
        cache.put(itemStack, hash);

        return hash;
    }

    public MEStorageCellCache loadData(ItemStack itemStack) {

        MEStorageCellCache storageCellCache = new MEStorageCellCache(itemStack);
        Map<ItemStack, Integer> storage = storageCellCache.getSourceStorage();
        int stored = 0;
        List<Map<String, String>> data = execQuery("SELECT * FROM " + getTableName() + " WHERE uuid = '" + storageCellCache.getUuid().toString() + "';");
        for (Map<String, String> itemData : data) {
            ItemStack item = (ItemStack) SerializeUtils.string2Object(itemData.get("item_base64"));
            int amount = Integer.parseInt(itemData.get("amount"));
            if (storage.containsKey(itemStack)) {
                amount += storage.get(item);
            }

            storage.put(item, amount);
            stored += amount;
        }

        storageCellCache.updateStored(stored);

        return storageCellCache;
    }
}
