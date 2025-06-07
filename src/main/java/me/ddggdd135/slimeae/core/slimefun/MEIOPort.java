package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.ICardHolder;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.MEStorageCellCache;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.QuantumUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MEIOPort extends TickingBlock implements IMEObject, InventoryBlock, ICardHolder {

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {}

    public void onMEIOPortTick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;

        IStorage networkStorage = info.getStorage();
        ItemStack setting = blockMenu.getItemInSlot(getSettingSlot());
        if (setting == null || setting.getType().isAir()) return;
        if (!InvUtils.fits(
                blockMenu.getInventory(), SlimeAEItems.ME_ITEM_STORAGE_COMPONENT_1K, getMeStorageCellOutputSlots()))
            return;
        if (SlimefunUtils.isItemSimilar(setting, MenuItems.INPUT_MODE, true, false)) {
            for (int slot : getMeStorageCellInputSlots()) {
                ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack != null
                        && !itemStack.getType().isAir()
                        && SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell
                        && MEItemStorageCell.isCurrentServer(itemStack)) {
                    MEStorageCellCache meStorageCellCache = MEItemStorageCell.getStorage(itemStack);
                    if (meStorageCellCache.getStorageUnsafe().isEmpty()) {
                        blockMenu.replaceExistingItem(slot, null);
                        MEItemStorageCell.updateLore(itemStack);
                        blockMenu.pushItem(itemStack, getMeStorageCellOutputSlots());
                        return;
                    }

                    ItemKey[] storage = meStorageCellCache.getStorageUnsafe().keyEntrySet().stream()
                            .filter(x -> x.getValue() > 0)
                            .map(Map.Entry::getKey)
                            .toArray(ItemKey[]::new);
                    if (storage.length == 0) return;

                    ItemHashMap<Long> tmp = meStorageCellCache
                            .takeItem(
                                    new ItemRequest(storage[0], (long) (81920 + (meStorageCellCache.getSize() * 0.01))))
                            .getStorageUnsafe();
                    networkStorage.pushItem(tmp);
                    ItemUtils.trim(tmp);
                    meStorageCellCache.pushItem(tmp);
                    MEItemStorageCell.updateLore(itemStack);
                }

                if (itemStack != null
                        && !itemStack.getType().isAir()
                        && SlimefunItem.getByItem(itemStack) instanceof NetworkQuantumStorage) {
                    QuantumCache quantumCache = QuantumUtils.getQuantumCache(itemStack);

                    if (quantumCache == null) return;
                    if (quantumCache.getItemStack() == null) return;

                    if (quantumCache.getItemStack() == null || quantumCache.getAmount() <= 0) {
                        blockMenu.replaceExistingItem(slot, null);
                        blockMenu.pushItem(itemStack, getMeStorageCellOutputSlots());
                        return;
                    }

                    ItemHashMap<Long> tmp = new ItemHashMap<>();
                    long amount = (long) (quantumCache.getLimit() * 0.01);
                    long current = quantumCache.getAmount();

                    long toTake = Math.min(amount, current);
                    current -= toTake;

                    ItemKey itemKey = new ItemKey(quantumCache.getItemStack());
                    tmp.putKey(itemKey, toTake);
                    networkStorage.pushItem(tmp);
                    ItemUtils.trim(tmp);

                    current += tmp.getOrDefault(itemKey, 0L);
                    quantumCache.setAmount((int) (current));
                    QuantumUtils.setQuantumCache(itemStack, quantumCache);
                }
            }

            return;
        }

        for (int slot : getMeStorageCellInputSlots()) {
            ItemStack itemStack = blockMenu.getItemInSlot(slot);
            if (itemStack != null
                    && !itemStack.getType().isAir()
                    && SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell
                    && MEItemStorageCell.isCurrentServer(itemStack)) {
                MEStorageCellCache meStorageCellCache = MEItemStorageCell.getStorage(itemStack);
                if (meStorageCellCache.getStored() >= meStorageCellCache.getSize()) {
                    blockMenu.replaceExistingItem(slot, null);
                    MEItemStorageCell.updateLore(itemStack);
                    blockMenu.pushItem(itemStack, getMeStorageCellOutputSlots());
                    return;
                }

                ItemKey[] storage = meStorageCellCache
                        .getFilterData()
                        .matches(networkStorage.getStorageUnsafe().keyEntrySet().stream()
                                .filter(x -> x.getValue() > 0)
                                .map(Map.Entry::getKey)
                                .toArray(ItemKey[]::new));

                if (storage.length == 0) return;
                ItemHashMap<Long> tmp = networkStorage
                        .takeItem(new ItemRequest(storage[0], (long) (81920 + (meStorageCellCache.getSize() * 0.01))))
                        .getStorageUnsafe();
                meStorageCellCache.pushItem(tmp);
                ItemUtils.trim(tmp);
                networkStorage.pushItem(tmp);
                MEItemStorageCell.updateLore(itemStack);
            }

            if (itemStack != null
                    && !itemStack.getType().isAir()
                    && SlimefunItem.getByItem(itemStack) instanceof NetworkQuantumStorage) {
                QuantumCache quantumCache = QuantumUtils.getQuantumCache(itemStack);

                if (quantumCache == null) return;
                if (quantumCache.getItemStack() == null) return;

                if (quantumCache.getItemStack() == null || quantumCache.getAmount() >= quantumCache.getLimit()) {
                    blockMenu.replaceExistingItem(slot, null);
                    blockMenu.pushItem(itemStack, getMeStorageCellOutputSlots());
                    return;
                }

                ItemKey itemKey = new ItemKey(quantumCache.getItemStack());

                long amount = (long) (quantumCache.getLimit() * 0.01);
                long current = quantumCache.getAmount();

                long toTake = Math.min(quantumCache.getLimit() - current, amount);
                ItemHashMap<Long> tmp = networkStorage
                        .takeItem(new ItemRequest(itemKey, toTake))
                        .getStorageUnsafe();

                current += tmp.getOrDefault(itemKey, 0L);
                quantumCache.setAmount((int) (current));
                QuantumUtils.setQuantumCache(itemStack, quantumCache);
            }
        }
    }

    public MEIOPort(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu == null) return;

                blockMenu.dropItems(b.getLocation(), getMeStorageCellInputSlots());
                blockMenu.dropItems(b.getLocation(), getMeStorageCellOutputSlots());

                dropCards(blockMenu);
            }
        };
    }

    @Override
    public int[] getInputSlots() {
        return getMeStorageCellInputSlots();
    }

    @Override
    public int[] getOutputSlots() {
        return getMeStorageCellOutputSlots();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@Nonnull BlockMenuPreset preset) {
        preset.drawBackground(getBorderSlots());
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        initCardSlots(menu);

        ItemStack setting = menu.getItemInSlot(getSettingSlot());
        if (setting == null || setting.getType().isAir()) {
            menu.replaceExistingItem(getSettingSlot(), MenuItems.INPUT_MODE);
        }

        menu.addMenuClickHandler(getSettingSlot(), (player, i, itemStack, clickAction) -> {
            ItemStack setting1 = menu.getItemInSlot(getSettingSlot());
            if (SlimefunUtils.isItemSimilar(setting1, MenuItems.INPUT_MODE, true, false)) {
                menu.replaceExistingItem(getSettingSlot(), MenuItems.OUTPUT_MODE);
                return false;
            }

            menu.replaceExistingItem(getSettingSlot(), MenuItems.INPUT_MODE);
            return false;
        });
    }

    @Override
    public int[] getCardSlots() {
        return new int[] {45, 46, 47};
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}

    public int getSettingSlot() {
        return 13;
    }

    @Override
    public void onNetworkTimeConsumingTick(Block block, NetworkInfo networkInfo) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        ItemStack setting = blockMenu.getItemInSlot(getSettingSlot());
        if (setting == null || setting.getType().isAir()) return;

        SlimefunBlockData data = StorageCacheUtils.getBlock(block.getLocation());
        if (data == null) return;
        SlimefunItem slimefunItem = SlimefunItem.getById(data.getSfId());
        if (slimefunItem == null) return;

        tickCards(block, slimefunItem, data);
        onMEIOPortTick(block, slimefunItem, data);
    }

    public int[] getBorderSlots() {
        return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 22, 26, 27, 31, 35, 36, 40, 44, 48, 49, 50, 51, 52, 53};
    }

    public int[] getMeStorageCellInputSlots() {
        return new int[] {10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39};
    }

    public int[] getMeStorageCellOutputSlots() {
        return new int[] {14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43};
    }
}
