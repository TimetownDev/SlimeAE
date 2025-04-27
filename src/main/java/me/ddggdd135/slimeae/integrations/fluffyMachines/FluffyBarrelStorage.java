package me.ddggdd135.slimeae.integrations.fluffyMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.LocationUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
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
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class FluffyBarrelStorage implements IStorage {
    private Barrel barrel;
    private BlockMenu blockMenu;
    private Block block;
    private boolean isReadOnly;

    public FluffyBarrelStorage(@Nonnull Block block) {
        this(block, false);
    }

    public FluffyBarrelStorage(@Nonnull Block block, boolean isReadOnly) {
        if (!SlimeAEPlugin.getFluffyMachinesIntegration().isLoaded())
            throw new RuntimeException("FluffyMachines is not loaded");
        this.block = block;
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(block.getLocation());
        if (blockData != null && SlimefunItem.getById(blockData.getSfId()) instanceof Barrel barrel) {
            blockMenu = blockData.getBlockMenu();
            this.barrel = barrel;
        }
        this.isReadOnly = isReadOnly;
    }

    @Override
    public void pushItem(@Nonnull ItemStackCache itemStackCache) {
        ItemStack itemStack = itemStackCache.getItemStack();

        if (!isReadOnly && blockMenu != null && barrel != null && barrel.getStored(block) > 0) {
            int stored = barrel.getStored(block);
            int size = barrel.getCapacity(block);
            if (stored >= size) return;
            ItemStack storedItem = barrel.getStoredItem(block);
            if (SlimefunUtils.isItemSimilar(itemStack, storedItem, true, false)) {
                int toAdd = Math.min(size - stored, itemStack.getAmount());
                stored += toAdd;
                itemStack.setAmount(itemStack.getAmount() - toAdd);
            }
            barrel.setStored(block, stored);
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        if (blockMenu == null || barrel == null || barrel.getStored(block) <= 0) return false;
        int stored = barrel.getStored(block) - 1;
        ItemStack storedItem = barrel.getStoredItem(block);
        boolean toReturn = true;
        for (ItemRequest request : requests) {
            if (SlimefunUtils.isItemSimilar(request.getKey().getItemStack(), storedItem, true, false)) {
                toReturn = toReturn && stored >= request.getAmount();
            }
        }
        return toReturn;
    }

    @Nonnull
    @Override
    public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
        if (blockMenu == null || barrel == null || barrel.getStored(block) <= 0) return new ItemStorage();
        int stored = barrel.getStored(block) - 1;
        ItemStack storedItem = barrel.getStoredItem(block);
        ItemStorage toReturn = new ItemStorage();
        for (ItemRequest request : requests) {
            if (SlimefunUtils.isItemSimilar(request.getKey().getItemStack(), storedItem, true, false)) {
                long toTake = Math.min(stored, request.getAmount());
                if (toTake != 0) {
                    stored -= toTake;
                    toReturn.addItem(ItemUtils.createItems(request.getKey().getItemStack(), toTake));
                }
            }
        }
        barrel.setStored(block, stored + 1);
        return toReturn;
    }

    @Override
    public @Nonnull ItemHashMap<Long> getStorageUnsafe() {
        ItemHashMap<Long> storage = new ItemHashMap<>();
        if (blockMenu == null || barrel == null || barrel.getStored(block) <= 0) return storage;
        storage.put(barrel.getStoredItem(block).asOne(), (long) (barrel.getStored(block) - 1));
        return storage;
    }

    @Override
    public int getTier(@Nonnull ItemKey itemStack) {
        try {
            if (blockMenu == null || barrel == null || barrel.getStored(block) <= 0) return -1;
            ItemStack storedItem = barrel.getStoredItem(block);
            if (storedItem == null || storedItem.getType().isAir()) return -1;
            if (itemStack.getItemStack().getType() == storedItem.getType()) return 2000;
        } catch (Exception e) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(Level.SEVERE, "在操作蓬松桶时发生了错误 方块位置 " + LocationUtils.locationToString(block.getLocation()), e);
        }

        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluffyBarrelStorage that)) return false;
        return Objects.equals(block.getLocation(), that.block.getLocation());
    }

    @Override
    public int hashCode() {
        return block.getLocation().hashCode();
    }
}
