package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.interfaces.MEBus;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MEExportBus extends MEBus {

    public static final int[] Setting_Slots = new int[] {3, 4, 5, 12, 13, 14, 21, 22, 23};

    public MEExportBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    private void onExport(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;
        if (getDirection(inv) == BlockFace.SELF) return;
        Block target = block.getRelative(getDirection(inv));
        IStorage networkStorage = info.getStorage();
        BlockMenu targetInv = StorageCacheUtils.getMenu(target.getLocation());
        for (int slot : Setting_Slots) {
            ItemStack setting = ItemUtils.getSettingItem(inv.getInventory(), slot);
            if (SlimefunUtils.isItemSimilar(setting, MenuItems.Setting, true, false)) continue;
            ItemRequest request = new ItemRequest(setting, setting.getAmount());
            if (targetInv != null) {
                int[] inputSlots = targetInv
                        .getPreset()
                        .getSlotsAccessedByItemTransport(targetInv, ItemTransportFlow.INSERT, setting);
                if (inputSlots == null) continue;
                if (targetInv.fits(setting, inputSlots)) {
                    ItemStack[] taken = networkStorage.tryTakeItem(request);
                    if (taken.length != 0) targetInv.pushItem(taken[0], inputSlots);
                }
            } else if (target.getState() instanceof Container container) {
                Inventory inventory = container.getInventory();
                if (InvUtils.fitAll(
                        inventory,
                        new ItemStack[] {setting},
                        IntStream.range(0, inventory.getSize()).toArray())) {
                    ItemStack[] taken = networkStorage.tryTakeItem(request);
                    if (taken.length != 0) inventory.addItem(taken);
                }
            }
        }
    }

    @Nullable public IStorage getStorage(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        BlockFace blockFace = getDirection(inv);
        if (blockFace == BlockFace.SELF) return null;
        Block b = block.getRelative(blockFace);
        if (b.getBlockData().getMaterial().isAir()) return null;
        return ItemUtils.getStorage(b);
    }

    @Override
    public boolean isSynchronized() {
        return true;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        super.tick(block, item, data);
        BlockMenu inv = StorageCacheUtils.getMenu(data.getLocation().getBlock().getLocation());
        if (inv == null) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData()
                .getNetworkInfo(data.getLocation().getBlock().getLocation());
        if (info == null) return;
        for (int slot : Setting_Slots) {
            ItemStack setting = inv.getItemInSlot(slot);
            if (setting == null || setting.getType().isAir()) {
                ItemUtils.setSettingItem(inv.getInventory(), slot, MenuItems.Setting);
            }
        }
        for (int slot : Setting_Slots) {
            ItemStack setting = inv.getItemInSlot(slot);
            if (setting == null || setting.getType().isAir()) {
                ItemUtils.setSettingItem(inv.getInventory(), slot, MenuItems.Setting);
            }
        }
        onExport(data.getLocation().getBlock());
    }

    @Override
    public int getNorthSlot() {
        return 1;
    }

    @Override
    public int getSouthSlot() {
        return 19;
    }

    @Override
    public int getEastSlot() {
        return 11;
    }

    @Override
    public int getWestSlot() {
        return 9;
    }

    @Override
    public int getUpSlot() {
        return 2;
    }

    @Override
    public int getDownSlot() {
        return 20;
    }

    @Override
    public int[] getBackgroundSlots() {
        return new int[] {
            0, 6, 7, 8, 10, 15, 16, 17, 18, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
            43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
        };
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@Nonnull BlockMenuPreset preset) {
        super.init(preset);
        for (int slot : Setting_Slots) {
            preset.addMenuClickHandler(slot, ItemUtils.getSettingSlotClickHandler());
        }
    }
}
