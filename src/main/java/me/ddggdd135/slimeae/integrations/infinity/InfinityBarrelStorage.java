package me.ddggdd135.slimeae.integrations.infinity;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.mooy1.infinityexpansion.items.storage.StorageCache;
import io.github.mooy1.infinityexpansion.items.storage.StorageUnit;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.ReflectionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InfinityBarrelStorage implements IStorage {
    private StorageCache cache;
    private boolean isReadOnly;

    public InfinityBarrelStorage(@Nonnull Block block) {
        this(block, false);
    }

    public InfinityBarrelStorage(@Nonnull Block block, boolean isReadOnly) {
        if (!SlimeAEPlugin.getInfinityIntegration().isLoaded()) throw new RuntimeException("Infinity is not loaded");
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(block.getLocation());
        if (blockData != null && SlimefunItem.getById(blockData.getSfId()) instanceof StorageUnit storageUnit) {
            cache = storageUnit.getCache(block.getLocation());
        }
        this.isReadOnly = isReadOnly;
    }

    @Override
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        if (!isReadOnly && cache != null) cache.depositAll(itemStacks);
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        if (cache == null) return false;
        boolean toReturn = true;
        for (ItemRequest request : requests) {
            if (Boolean.TRUE.equals(ReflectionUtils.invokePrivateMethod(
                    cache, "matches", new Class<?>[] {ItemStack.class}, request.getTemplate()))) {
                int amount = ReflectionUtils.getField(cache, "amount");
                toReturn = toReturn && amount >= request.getAmount();
            }
        }
        return toReturn;
    }

    @Nonnull
    @Override
    public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
        if (cache == null) return new ItemStack[0];
        ItemStorage toReturn = new ItemStorage();
        for (ItemRequest request : requests) {
            if (Boolean.TRUE.equals(ReflectionUtils.invokePrivateMethod(
                    cache, "matches", new Class<?>[] {ItemStack.class}, request.getTemplate()))) {
                int amount = ReflectionUtils.getField(cache, "amount");
                int toTake = Math.min(amount, request.getAmount());
                if (toTake != 0) {
                    ReflectionUtils.setField(cache, "amount", amount - toTake);
                    toReturn.addItem(ItemUtils.createItems(request.getTemplate(), toTake));
                }
            }
        }
        return toReturn.toItemStacks();
    }

    @Override
    public @Nonnull Map<ItemStack, Integer> getStorage() {
        Map<ItemStack, Integer> storage = new ItemHashMap<>();
        Material material = ReflectionUtils.getField(cache, "material");
        ItemMeta meta = ReflectionUtils.getField(cache, "meta");

        if (cache == null || material == null || ReflectionUtils.<Integer>getField(cache, "amount") <= 0)
            return storage;
        ItemStack itemStack = new ItemStack(material);
        if (meta != null) itemStack.setItemMeta(meta);

        storage.put(itemStack, ReflectionUtils.<Integer>getField(cache, "amount"));
        return storage;
    }

    @Override
    public int getEmptySlots() {
        return 0;
    }
}
