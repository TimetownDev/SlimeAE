package me.ddggdd135.slimeae.integrations.networks;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class QuantumStorage implements IStorage {
    private QuantumCache quantumCache;
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
        if (blockData != null && SlimefunItem.getById(blockData.getSfId()) instanceof NetworkQuantumStorage) {
            quantumCache = NetworkQuantumStorage.getCaches().get(block.getLocation());
        }
        this.isReadOnly = isReadOnly;
    }

    @Override
    public void pushItem(@Nonnull ItemStackCache itemStackCache) {
        ItemStack itemStack = itemStackCache.getItemStack();

        if (!isReadOnly && quantumCache != null && quantumCache.getAmount() > 0) {
            int stored = (int) quantumCache.getAmount();
            int size = quantumCache.getLimit();
            if (stored >= size && !quantumCache.isVoidExcess()) return;
            ItemStack storedItem = quantumCache.getItemStack();
            if (SlimefunUtils.isItemSimilar(itemStack, storedItem, true, false)) {
                int toAdd = Math.min(size - stored, itemStack.getAmount());
                stored += toAdd;
                itemStack.setAmount(itemStack.getAmount() - toAdd);
            }
            if (!(stored > size)) {
                quantumCache.setAmount(stored);
                // 下面这一行吃性能
                // NetworkQuantumStorage.syncBlock(block.getLocation(), quantumCache);
            }
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        if (quantumCache == null || quantumCache.getAmount() <= 0) return false;
        int stored = (int) (quantumCache.getAmount() - 1);
        ItemStack storedItem = quantumCache.getItemStack();
        boolean toReturn = true;
        for (ItemRequest request : requests) {
            if (SlimefunUtils.isItemSimilar(request.getKey().getItemStack(), storedItem, true, false)) {
                toReturn = toReturn && stored >= request.getAmount();
            }
        }
        return toReturn;
    }

    @Override
    @Nonnull
    public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
        if (quantumCache == null || quantumCache.getAmount() <= 0) return new ItemStorage();
        int stored = (int) (quantumCache.getAmount() - 1);
        ItemStack storedItem = quantumCache.getItemStack();
        ItemStorage toReturn = new ItemStorage();
        for (ItemRequest request : requests) {
            if (SlimefunUtils.isItemSimilar(request.getKey().getItemStack(), storedItem, true, false)) {
                long toTake = Math.min(stored, request.getAmount());
                if (toTake != 0) {
                    stored -= toTake;
                    toReturn.addItem(request.getKey(), toTake);
                }
            }
        }
        quantumCache.setAmount(stored + 1);
        // 下面这一行吃性能
        // NetworkQuantumStorage.syncBlock(block.getLocation(), quantumCache);
        return toReturn;
    }

    @Override
    @Nonnull
    public ItemHashMap<Long> getStorage() {
        ItemHashMap<Long> storage = new ItemHashMap<>();
        if (quantumCache == null || quantumCache.getAmount() <= 0) return storage;
        storage.put(quantumCache.getItemStack().asOne(), quantumCache.getAmount() - 1);
        return storage;
    }

    @Override
    public int getTier(@Nonnull ItemKey itemStack) {
        if (quantumCache == null || quantumCache.getAmount() < 0) return -1;
        ItemStack storedItem = quantumCache.getItemStack();
        if (storedItem == null || storedItem.getType().isAir()) return -1;
        if (storedItem.getType() == itemStack.getItemStack().getType()) return 2000;

        return 0;
    }

    public void sync() {
        NetworkQuantumStorage.syncBlock(block.getLocation(), quantumCache);
    }
}
