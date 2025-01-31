package me.ddggdd135.slimeae.integrations.networks;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
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

public class QuantumStorage implements IStorage {
    private QuantumCache quantumCache;
    private BlockMenu blockMenu;
    private Block block;
    private boolean isReadOnly;

    public QuantumStorage(@Nonnull Block block) {
        this(block, false);
    }

    public QuantumStorage(@Nonnull Block block, boolean isReadOnly) {
        if (!(SlimeAEPlugin.getNetworksIntegration().isLoaded()
                || SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded()))
            throw new RuntimeException("Networks is not loaded");
        this.block = block;
        SlimefunBlockData blockData = StorageCacheUtils.getBlock(block.getLocation());
        if (blockData != null
                && SlimefunItem.getById(blockData.getSfId()) instanceof NetworkQuantumStorage networkQuantumStorage) {
            blockMenu = blockData.getBlockMenu();
            quantumCache = NetworkQuantumStorage.getCaches().get(block.getLocation());
        }
        this.isReadOnly = isReadOnly;
    }

    @Override
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        if (!isReadOnly && blockMenu != null && quantumCache != null && quantumCache.getAmount() > 0) {
            int stored = (int) quantumCache.getAmount();
            int size = quantumCache.getLimit();
            if (stored >= size) return;
            ItemStack storedItem = quantumCache.getItemStack();
            for (ItemStack itemStack : itemStacks) {
                if (SlimefunUtils.isItemSimilar(itemStack, storedItem, true, false)) {
                    int toAdd = Math.min(size - stored, itemStack.getAmount());
                    stored += toAdd;
                    itemStack.setAmount(itemStack.getAmount() - toAdd);
                }
            }
            quantumCache.setAmount(stored);
            NetworkQuantumStorage.syncBlock(block.getLocation(), quantumCache);
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        if (blockMenu == null || quantumCache == null || quantumCache.getAmount() <= 0) return false;
        int stored = (int) (quantumCache.getAmount() - 1);
        ItemStack storedItem = quantumCache.getItemStack();
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
        if (blockMenu == null || quantumCache == null || quantumCache.getAmount() <= 0) return new ItemStack[0];
        int stored = (int) (quantumCache.getAmount() - 1);
        ItemStack storedItem = quantumCache.getItemStack();
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
        quantumCache.setAmount(stored + 1);
        NetworkQuantumStorage.syncBlock(block.getLocation(), quantumCache);
        return toReturn.toItemStacks();
    }

    @Override
    public @Nonnull Map<ItemStack, Integer> getStorage() {
        Map<ItemStack, Integer> storage = new HashMap<>();
        if (blockMenu == null || quantumCache == null || quantumCache.getAmount() <= 0) return storage;
        storage.put(quantumCache.getItemStack().asOne(), (int) (quantumCache.getAmount() - 1));
        return storage;
    }

    @Override
    public int getEmptySlots() {
        return 0;
    }

    @Override
    public int getTier(@Nonnull ItemStack itemStack) {
        ItemStack storedItem = quantumCache.getItemStack();
        if (SlimefunUtils.isItemSimilar(itemStack, storedItem, true, false)) {
            return 2000;
        }

        return 0;
    }
}
