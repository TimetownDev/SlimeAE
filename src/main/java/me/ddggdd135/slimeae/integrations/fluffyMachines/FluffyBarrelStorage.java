package me.ddggdd135.slimeae.integrations.fluffyMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
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
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        if (!isReadOnly && blockMenu != null && barrel != null && barrel.getStored(block) > 0) {
            int stored = barrel.getStored(block);
            int size = barrel.getCapacity(block);
            if (stored >= size) return;
            ItemStack storedItem = barrel.getStoredItem(block);
            for (ItemStack itemStack : itemStacks) {
                if (SlimefunUtils.isItemSimilar(itemStack, storedItem, true, false)) {
                    int toAdd = Math.min(size - stored, itemStack.getAmount());
                    stored += toAdd;
                    itemStack.setAmount(itemStack.getAmount() - toAdd);
                }
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
            if (SlimefunUtils.isItemSimilar(request.getTemplate(), storedItem, true, false)) {
                toReturn = toReturn && stored >= request.getAmount();
            }
        }
        return toReturn;
    }

    @Nonnull
    @Override
    public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
        if (blockMenu == null || barrel == null || barrel.getStored(block) <= 0) return new ItemStack[0];
        int stored = barrel.getStored(block) - 1;
        ItemStack storedItem = barrel.getStoredItem(block);
        ItemStorage toReturn = new ItemStorage();
        for (ItemRequest request : requests) {
            if (SlimefunUtils.isItemSimilar(request.getTemplate(), storedItem, true, false)) {
                int toTake = Math.min(stored, request.getAmount());
                if (toTake != 0) {
                    stored -= toTake;
                    toReturn.addItem(ItemUtils.createItems(request.getTemplate(), toTake));
                }
            }
        }
        barrel.setStored(block, stored + 1);
        return toReturn.toItemStacks();
    }

    @Override
    public @Nonnull Map<ItemStack, Integer> getStorage() {
        Map<ItemStack, Integer> storage = new HashMap<>();
        if (blockMenu == null || barrel == null || barrel.getStored(block) <= 0) return storage;
        storage.put(barrel.getStoredItem(block).asOne(), barrel.getStored(block) - 1);
        return storage;
    }

    @Override
    public int getEmptySlots() {
        return 0;
    }

    @Override
    public int getTier(@Nonnull ItemStack itemStack) {
        ItemStack storedItem = barrel.getStoredItem(block);
        if (SlimefunUtils.isItemSimilar(itemStack, storedItem, true, false)) {
            return 1000;
        }

        return 0;
    }
}
