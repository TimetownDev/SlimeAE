package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.MEStorageCellCache;
import me.ddggdd135.slimeae.api.items.StorageCollection;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MEDrive extends SlimefunItem implements IMEStorageObject, InventoryBlock {
    private static final Map<Location, long[]> lastStoredSnapshot = new ConcurrentHashMap<>();

    public MEDrive(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
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

                if (blockMenu != null) {
                    for (int slot : getMEItemStorageCellSlots()) {
                        ItemStack itemStack = blockMenu.getItemInSlot(slot);
                        if (itemStack != null
                                && !itemStack.getType().isAir()
                                && SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell
                                && MEItemStorageCell.isCurrentServer(itemStack)) {
                            MEItemStorageCell.updateLore(itemStack);
                        }
                    }
                    blockMenu.dropItems(b.getLocation(), getMEItemStorageCellSlots());
                }
                lastStoredSnapshot.remove(b.getLocation());
            }
        };
    }

    @Override
    @Nullable public IStorage getStorage(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return null;
        StorageCollection storageCollection = new StorageCollection();
        for (int slot : getMEItemStorageCellSlots()) {
            ItemStack itemStack = blockMenu.getItemInSlot(slot);
            if (itemStack != null
                    && !itemStack.getType().isAir()
                    && SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell) {
                if (!MEItemStorageCell.isCurrentServer(itemStack)) continue;
                MEStorageCellCache result = MEItemStorageCell.getStorage(itemStack);

                storageCollection.addStorage(result);
            }
        }
        return storageCollection;
    }

    @Override
    public int[] getInputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@Nonnull BlockMenuPreset preset) {
        for (int slot : getBorderSlots()) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        int[] cellSlots = getMEItemStorageCellSlots();
        menu.addPlayerInventoryClickHandler(new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent e, Player player, int slot, ItemStack cursor, ClickAction action) {
                if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    return true;
                }
                ItemStack clickedItem = e.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) return true;
                if (!(SlimefunItem.getByItem(clickedItem) instanceof MEItemStorageCell)) return true;

                boolean[] wasCellBefore = new boolean[cellSlots.length];
                for (int idx = 0; idx < cellSlots.length; idx++) {
                    ItemStack slotItem = menu.getItemInSlot(cellSlots[idx]);
                    wasCellBefore[idx] = slotItem != null
                            && !slotItem.getType().isAir()
                            && SlimefunItem.getByItem(slotItem) instanceof MEItemStorageCell;
                }

                Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), () -> {
                    for (int idx = 0; idx < cellSlots.length; idx++) {
                        ItemStack slotItem = menu.getItemInSlot(cellSlots[idx]);
                        boolean isCellNow = slotItem != null
                                && !slotItem.getType().isAir()
                                && SlimefunItem.getByItem(slotItem) instanceof MEItemStorageCell;
                        if (isCellNow != wasCellBefore[idx]) {
                            NetworkInfo networkInfo =
                                    SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                            if (networkInfo != null) {
                                networkInfo.setNeedsStorageUpdate(true);
                            }
                            return;
                        }
                    }
                });

                return true;
            }

            @Override
            public boolean onClick(Player player, int slot, ItemStack item, ClickAction action) {
                return true;
            }
        });

        for (int slot : cellSlots) {
            menu.addMenuClickHandler(slot, (player, i, cursor, clickAction) -> {
                ItemStack itemStack = menu.getItemInSlot(i);
                boolean wasCell = itemStack != null
                        && !itemStack.getType().isAir()
                        && SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell
                        && MEItemStorageCell.isCurrentServer(itemStack);

                if (wasCell) {
                    MEItemStorageCell.updateLore(itemStack);
                    NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                    if (networkInfo == null) return true;
                    StorageCollection storageCollection = (StorageCollection) networkInfo.getStorage();
                    MEStorageCellCache result = MEItemStorageCell.getStorage(itemStack);
                    if (result != null) {
                        storageCollection.removeStorage(result);
                    }
                }

                Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), () -> {
                    ItemStack newItem = menu.getItemInSlot(i);
                    boolean isCell = newItem != null
                            && !newItem.getType().isAir()
                            && SlimefunItem.getByItem(newItem) instanceof MEItemStorageCell
                            && MEItemStorageCell.isCurrentServer(newItem);
                    if (isCell != wasCell) {
                        NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                        if (networkInfo != null) {
                            networkInfo.setNeedsStorageUpdate(true);
                        }
                    }
                });

                return true;
            });
        }
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        if (!blockMenu.hasViewer()) return;

        int[] slots = getMEItemStorageCellSlots();
        long[] lastStored = lastStoredSnapshot.computeIfAbsent(block.getLocation(), k -> new long[slots.length]);
        boolean anyChanged = false;
        boolean[] changedSlots = new boolean[slots.length];

        for (int i = 0; i < slots.length; i++) {
            ItemStack itemStack = blockMenu.getItemInSlot(slots[i]);
            if (itemStack == null || itemStack.getType().isAir()) {
                lastStored[i] = -1;
                continue;
            }
            if (!(SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell)) {
                lastStored[i] = -1;
                continue;
            }
            if (!MEItemStorageCell.isCurrentServer(itemStack)) {
                lastStored[i] = -1;
                continue;
            }

            MEStorageCellCache cache = MEItemStorageCell.getStorage(itemStack);
            if (cache == null) {
                lastStored[i] = -1;
                continue;
            }

            long currentStored = cache.getStored();
            if (currentStored != lastStored[i]) {
                MEItemStorageCell.updateLore(itemStack);
                blockMenu.markDirty();
                lastStored[i] = currentStored;
                anyChanged = true;
                changedSlots[i] = true;
            }
        }

        if (anyChanged) {
            for (int i = 0; i < slots.length; i++) {
                if (changedSlots[i]) {
                    Slimefun.getDatabaseManager()
                            .getBlockDataController()
                            .saveBlockInventorySlot(StorageCacheUtils.getBlock(block.getLocation()), slots[i]);
                }
            }
        }
    }

    public int[] getMEItemStorageCellSlots() {
        return new int[] {12, 13, 14, 21, 22, 23, 30, 31, 32, 39, 40, 41};
    }

    public int[] getBorderSlots() {
        return new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 15, 16, 17, 18, 19, 20, 24, 25, 26, 27, 28, 29, 33, 34, 35, 36, 37,
            38, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
        };
    }
}
