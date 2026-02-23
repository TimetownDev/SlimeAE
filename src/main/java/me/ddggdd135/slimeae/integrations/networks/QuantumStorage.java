package me.ddggdd135.slimeae.integrations.networks;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemInfo;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class QuantumStorage implements IStorage {
    private QuantumCache quantumCache;
    private BlockMenu blockMenu;
    private boolean isReadOnly;

    public QuantumStorage(@Nonnull Block block) {
        this(block, false);
    }

    public QuantumStorage(@Nonnull Block block, boolean isReadOnly) {
        if (!(SlimeAEPlugin.getNetworksIntegration().isLoaded()
                || SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded()))
            throw new RuntimeException("Networks is not loaded");

        SlimefunBlockData blockData = StorageCacheUtils.getBlock(block.getLocation());
        if (blockData != null && SlimefunItem.getById(blockData.getSfId()) instanceof NetworkQuantumStorage) {
            blockMenu = blockData.getBlockMenu();
            quantumCache = NetworkQuantumStorage.getCaches().get(block.getLocation());
        }

        this.isReadOnly = isReadOnly;
    }

    @Override
    public void pushItem(@Nonnull ItemStackCache itemStackCache) {
        if (isReadOnly || quantumCache == null) return;
        ItemStack itemStack = itemStackCache.getItemStack();

        if (quantumCache.getItemStack() == null) {
            return;
        }

        if (NetworkQuantumStorage.isBlacklisted(itemStack)) {
            return;
        }
        if (StackUtils.itemsMatch(quantumCache, itemStack)) {
            int leftover = quantumCache.increaseAmount(itemStack.getAmount());
            itemStack.setAmount(leftover);
        }
    }

    @Override
    public void pushItem(@Nonnull ItemInfo itemInfo) {
        if (isReadOnly || quantumCache == null) return;

        ItemKey itemKey = itemInfo.getItemKey();
        ItemStack itemStack = itemKey.getItemStack();

        if (quantumCache.getItemStack() == null) {
            return;
        }

        if (NetworkQuantumStorage.isBlacklisted(itemStack)) {
            return;
        }
        if (StackUtils.itemsMatch(quantumCache, itemStack)) {
            long toDeposit = itemInfo.getAmount();
            long remaining = toDeposit;
            // increaseAmount 接受 int，需要分批推入
            while (remaining > 0) {
                int batch = (int) Math.min(remaining, Integer.MAX_VALUE);
                int leftover = quantumCache.increaseAmount(batch);
                remaining = remaining - batch + leftover;
                // 如果有放不下的，说明已满，停止推入
                if (leftover > 0) {
                    break;
                }
            }
            itemInfo.setAmount(remaining);
        }
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        long stored = quantumCache.getAmount();
        if (quantumCache == null || stored <= 0) return false;

        ItemStack storedItem = quantumCache.getItemStack();
        boolean toReturn = true;
        for (ItemRequest request : requests) {
            if (StackUtils.itemsMatch(request.getKey().getItemStack(), storedItem, true, false)) {
                toReturn = toReturn && stored >= request.getAmount();
            }
        }
        return toReturn;
    }

    @Override
    @Nonnull
    public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
        ItemStorage toReturn = new ItemStorage();
        ItemStack output = blockMenu.getItemInSlot(NetworkQuantumStorage.OUTPUT_SLOT);

        if (output == null || output.getType().isAir()) return toReturn;

        ItemRequest vaild = null;
        for (ItemRequest request : requests) {
            if (StackUtils.itemsMatch(output, request.getKey().getItemStack())) {
                vaild = request;
                break;
            }
        }

        if (vaild == null) return toReturn;

        int amount = (int) vaild.getAmount();
        int gotten;
        if (quantumCache.getAmount() < amount) {
            gotten = (int) Math.min(quantumCache.getAmount(), amount);
            toReturn.addItem(vaild.getKey(), gotten);
            quantumCache.setAmount(0);
        } else {
            quantumCache.setAmount((int) (quantumCache.getAmount() - amount));
            toReturn.addItem(vaild.getKey(), amount);
        }

        return toReturn;
    }

    @Override
    @Nonnull
    public ItemHashMap<Long> getStorageUnsafe() {
        ItemHashMap<Long> storage = new ItemHashMap<>();
        long amount = quantumCache.getAmount();

        if (quantumCache == null || amount <= 0) return storage;
        ItemStack itemStack = quantumCache.getItemStack();
        if (itemStack != null && !itemStack.getType().isAir()) storage.put(itemStack.asOne(), amount);
        return storage;
    }

    @Override
    public int getTier(@Nonnull ItemKey itemStack) {
        ItemStack output = blockMenu.getItemInSlot(NetworkQuantumStorage.OUTPUT_SLOT);
        if (quantumCache == null
                || (quantumCache.getAmount() < 0
                        && (output == null || output.getType().isAir()))) return -1;
        ItemStack storedItem = quantumCache.getItemStack();
        if (storedItem == null || storedItem.getType().isAir()) return -1;
        if (storedItem.getType() == itemStack.getItemStack().getType()) return 2000;

        return 0;
    }

    public void sync() {
        if (quantumCache != null && StorageCacheUtils.getBlock(blockMenu.getLocation()) != null)
            NetworkQuantumStorage.syncBlock(blockMenu.getLocation(), quantumCache);
    }
}
