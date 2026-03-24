package me.ddggdd135.slimeae.api.database.v3;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import org.bukkit.inventory.ItemStack;

public class ItemKeyTplIdBridge {
    private final ConcurrentHashMap<ItemKey, Long> keyToTplId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ItemKey> tplIdToKey = new ConcurrentHashMap<>();
    private final ItemTemplateRegistry registry;

    public ItemKeyTplIdBridge(ItemTemplateRegistry registry) {
        this.registry = registry;
    }

    public long getOrResolve(@Nonnull ItemKey itemKey) {
        Long cached = keyToTplId.get(itemKey);
        if (cached != null) return cached;

        long tplId = registry.getOrRegister(itemKey.getItemStack());
        keyToTplId.put(itemKey, tplId);
        tplIdToKey.put(tplId, itemKey);
        return tplId;
    }

    @Nullable public ItemKey resolveKey(long tplId) {
        ItemKey cached = tplIdToKey.get(tplId);
        if (cached != null) return cached;

        ItemStack itemStack = registry.resolveItem(tplId);
        if (itemStack == null) return null;
        ItemKey key = new ItemKey(itemStack);
        keyToTplId.put(key, tplId);
        tplIdToKey.put(tplId, key);
        return key;
    }

    public void preload(Collection<Long> tplIds) {
        for (long tplId : tplIds) {
            if (!tplIdToKey.containsKey(tplId)) {
                resolveKey(tplId);
            }
        }
    }
}
