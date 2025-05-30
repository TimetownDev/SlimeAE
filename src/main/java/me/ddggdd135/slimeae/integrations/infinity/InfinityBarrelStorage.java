package me.ddggdd135.slimeae.integrations.infinity;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.LocationUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.mooy1.infinityexpansion.items.storage.StorageCache;
import io.github.mooy1.infinityexpansion.items.storage.StorageUnit;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.Objects;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InfinityBarrelStorage implements IStorage {
    private StorageCache cache;
    private Location location;
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
        this.location = block.getLocation();
    }

    @Override
    public void pushItem(@Nonnull ItemStackCache itemStackCache) {
        if (!isReadOnly && cache != null) cache.depositAll(new ItemStack[] {itemStackCache.getItemStack()}, true);
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        if (cache == null) return false;
        boolean toReturn = true;
        for (ItemRequest request : requests) {
            if (cache.matches(request.getKey().getItemStack())) {
                int amount = cache.amount();
                toReturn = toReturn && amount >= request.getAmount();
            }
        }
        return toReturn;
    }

    @Nonnull
    @Override
    public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
        if (cache == null) return new ItemStorage();
        ItemStorage toReturn = new ItemStorage();
        for (ItemRequest request : requests) {
            if (cache.matches(request.getKey().getItemStack())) {
                int amount = cache.amount() - 1;
                long toTake = Math.min(amount, request.getAmount());
                if (toTake != 0) {
                    cache.amount((int) (amount + 1 - toTake));
                    toReturn.addItem(new ItemKey(request.getKey().getItemStack()), toTake);
                }
            }
        }
        return toReturn;
    }

    @Override
    public @Nonnull ItemHashMap<Long> getStorageUnsafe() {
        ItemHashMap<Long> storage = new ItemHashMap<>();
        Material material = cache.material();
        ItemMeta meta = cache.meta();

        if (material == null || cache.amount() <= 0) return storage;
        ItemStack itemStack = new ItemStack(material);
        if (meta != null) itemStack.setItemMeta(meta);

        storage.put(itemStack.asOne(), (long) (cache.amount() - 1));
        return storage;
    }

    @Override
    public int getTier(@Nonnull ItemKey itemStack) {
        try {
            if (cache == null || cache.amount() <= 0) return -1;
            if (cache.material() == itemStack.getItemStack().getType()) return 2000;
        } catch (Exception e) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(Level.SEVERE, "在操作无尽存储时发生了错误 方块位置 " + LocationUtils.locationToString(location), e);
        }

        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InfinityBarrelStorage that)) return false;
        return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }
}
