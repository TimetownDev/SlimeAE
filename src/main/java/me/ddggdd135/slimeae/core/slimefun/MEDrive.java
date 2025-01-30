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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.MEStorageCellCache;
import me.ddggdd135.slimeae.api.ResultWithItem;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MEDrive extends SlimefunItem implements IMEStorageObject, InventoryBlock {
    public static final int[] Boarder_Slots = new int[] {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 15, 16, 17, 18, 19, 20, 24, 25, 26, 27, 28, 29, 33, 34, 35, 36, 37, 38,
        42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
    };
    public static final int[] MEItemStorageCell_Slots = new int[] {12, 13, 14, 21, 22, 23, 30, 31, 32, 39, 40, 41};

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
                    for (int slot : MEItemStorageCell_Slots) {
                        ItemStack itemStack = blockMenu.getItemInSlot(slot);
                        if (itemStack != null
                                && !itemStack.getType().isAir()
                                && SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell
                                && MEItemStorageCell.isCurrentServer(itemStack)) {
                            MEItemStorageCell.updateLore(itemStack);
                        }
                    }
                    blockMenu.dropItems(b.getLocation(), MEItemStorageCell_Slots);
                }
            }
        };
    }

    @Override
    @Nullable public IStorage getStorage(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return null;
        StorageCollection storageCollection = new StorageCollection();
        for (int slot : MEItemStorageCell_Slots) {
            ItemStack itemStack = blockMenu.getItemInSlot(slot);
            if (itemStack != null
                    && !itemStack.getType().isAir()
                    && SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell
                    && MEItemStorageCell.isCurrentServer(itemStack)) {
                ResultWithItem<MEStorageCellCache> result = MEItemStorageCell.getStorage(itemStack);
                blockMenu.replaceExistingItem(slot, result.getItemStack());
                storageCollection.addStorage(result.getResult());
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
        for (int slot : Boarder_Slots) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        for (int slot : MEItemStorageCell_Slots) {
            menu.addMenuClickHandler(slot, (player, i, cursor, clickAction) -> {
                ItemStack itemStack = menu.getItemInSlot(i);
                if (itemStack != null
                        && !itemStack.getType().isAir()
                        && SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell
                        && MEItemStorageCell.isCurrentServer(itemStack)) {
                    MEItemStorageCell.updateLore(itemStack);
                }
                NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                if (networkInfo == null) return true;
                StorageCollection storageCollection = (StorageCollection) networkInfo.getStorage();
                if (itemStack != null) {
                    storageCollection.removeStorage(
                            MEItemStorageCell.getStorage(itemStack).getResult());
                }
                return true;
            });
        }
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        for (int slot : MEItemStorageCell_Slots) {
            ItemStack itemStack = blockMenu.getItemInSlot(slot);
            if (itemStack != null
                    && !itemStack.getType().isAir()
                    && SlimefunItem.getByItem(itemStack) instanceof MEItemStorageCell
                    && MEItemStorageCell.isCurrentServer(itemStack)) {
                MEItemStorageCell.updateLore(itemStack);
                blockMenu.markDirty();
                Slimefun.getDatabaseManager()
                        .getBlockDataController()
                        .saveBlockInventorySlot(StorageCacheUtils.getBlock(block.getLocation()), slot);
            }
        }
    }
}
