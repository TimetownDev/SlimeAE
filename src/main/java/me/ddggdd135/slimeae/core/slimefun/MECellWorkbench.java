package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.MEStorageCellFilterData;
import me.ddggdd135.slimeae.api.items.MEStorageCellCache;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MECellWorkbench extends SlimefunItem implements InventoryBlock {

    public MECellWorkbench(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    public int getStorageCellSlot() {
        return 1;
    }

    public int getReversedSlot() {
        return 17;
    }

    public int getFuzzySlot() {
        return 26;
    }

    public int[] getBorderSlots() {
        return new int[] {0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 25, 34, 35, 43, 44, 52, 53};
    }

    public int[] getSettingSlots() {
        return new int[] {
            18, 19, 20, 21, 22, 23, 24, 27, 28, 29, 30, 31, 32, 33, 36, 37, 38, 39, 40, 41, 42, 45, 46, 47, 48, 49, 50,
            51
        };
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
    public void init(@Nonnull BlockMenuPreset preset) {
        for (int slot : getBorderSlots()) {
            preset.addItem(slot, ChestMenuUtils.getBackground());
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        if (menu.getItemInSlot(getStorageCellSlot()) == null
                || menu.getItemInSlot(getStorageCellSlot()).getType().isAir())
            ItemUtils.setSettingItem(menu.getInventory(), getStorageCellSlot(), MenuItems.STORAGE_CELL);

        ItemStack itemStack = ItemUtils.getSettingItem(menu.getInventory(), getStorageCellSlot());
        if (itemStack != null) ItemUtils.setSettingItem(menu.getInventory(), getStorageCellSlot(), itemStack);

        menu.addMenuClickHandler(getStorageCellSlot(), onStorageCellSlotClick(menu, block));

        updateGUI(block);
    }

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu == null) return;

                ItemStack storageCell = ItemUtils.getSettingItem(blockMenu.getInventory(), getStorageCellSlot());

                if (!SlimefunUtils.isItemSimilar(storageCell, MenuItems.STORAGE_CELL, true, false)) {
                    b.getWorld().dropItem(b.getLocation(), storageCell);
                }
            }
        };
    }

    @Nonnull
    private ChestMenu.AdvancedMenuClickHandler onStorageCellSlotClick(
            @Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        return new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent inventoryClickEvent,
                    Player player,
                    int i,
                    ItemStack cursor,
                    ClickAction clickAction) {
                Inventory inventory = inventoryClickEvent.getClickedInventory();
                ItemStack current = ItemUtils.getSettingItem(inventory, i);
                if (SlimefunUtils.isItemSimilar(current, MenuItems.STORAGE_CELL, true, false)) {
                    if (cursor != null
                            && !cursor.getType().isAir()
                            && SlimefunItem.getByItem(cursor) instanceof MEItemStorageCell) {
                        inventory.setItem(i, cursor);
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(null);
                    }
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(current);
                        inventory.setItem(i, MenuItems.STORAGE_CELL);
                    } else if (SlimefunItem.getByItem(cursor) instanceof MEItemStorageCell) {
                        inventory.setItem(i, cursor);
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(current);
                    }
                }

                updateGUI(block);
                return false;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        };
    }

    public void updateGUI(@Nonnull Block block) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
        if (slimefunBlockData == null) return;

        BlockMenu menu = slimefunBlockData.getBlockMenu();
        if (menu == null) return;

        for (int slot : getSettingSlots()) {
            menu.replaceExistingItem(slot, MenuItems.CANNOT_NOW);
            menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }

        menu.replaceExistingItem(getReversedSlot(), MenuItems.CANNOT_NOW);
        menu.addMenuClickHandler(getReversedSlot(), ChestMenuUtils.getEmptyClickHandler());
        menu.replaceExistingItem(getFuzzySlot(), MenuItems.CANNOT_NOW);
        menu.addMenuClickHandler(getFuzzySlot(), ChestMenuUtils.getEmptyClickHandler());

        ItemStack itemStack = menu.getItemInSlot(getStorageCellSlot());
        if (SlimefunUtils.isItemSimilar(itemStack, MenuItems.STORAGE_CELL, true, false)) return;

        MEStorageCellCache cache = MEItemStorageCell.getStorage(itemStack);
        if (cache == null) return;
        MEStorageCellFilterData data = cache.getFilterData();

        for (int slot : getSettingSlots()) {
            menu.replaceExistingItem(slot, MenuItems.SETTING);
            menu.addMenuClickHandler(slot, new ChestMenu.AdvancedMenuClickHandler() {

                @Override
                public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                    return false;
                }

                @Override
                public boolean onClick(
                        InventoryClickEvent inventoryClickEvent,
                        Player player,
                        int i,
                        ItemStack cursor,
                        ClickAction clickAction) {
                    if (cursor == null || cursor.getType().isAir()) return false;

                    ItemStack itemStack = menu.getItemInSlot(getStorageCellSlot());
                    if (SlimefunUtils.isItemSimilar(itemStack, MenuItems.STORAGE_CELL, true, false)) return false;

                    MEStorageCellCache cache = MEItemStorageCell.getStorage(itemStack);
                    MEStorageCellFilterData data = cache.getFilterData();

                    if (data.getFilters().size() >= getSettingSlots().length
                            || data.getFilters().contains(cursor)) return false;

                    data.getFilters().add(cursor);
                    data.updateItemTypes();

                    SlimeAEPlugin.getStorageCellFilterDataController().markDirty(data);

                    updateGUI(block);
                    return false;
                }
            });
        }

        List<ItemKey> itemKeys = new ArrayList<>(data.getFilters());

        for (int i = 0; i < itemKeys.size(); i++) {
            ItemKey itemKey = itemKeys.get(i);
            menu.replaceExistingItem(getSettingSlots()[i], itemKey.getItemStack());
            menu.addMenuClickHandler(getSettingSlots()[i], (player, i1, cursor, clickAction) -> {
                data.getFilters().remove(itemKey);
                data.updateItemTypes();

                SlimeAEPlugin.getStorageCellFilterDataController().markDirty(data);

                updateGUI(block);
                return false;
            });
        }

        menu.replaceExistingItem(
                getReversedSlot(),
                ItemUtils.withType(
                        MenuItems.REVERSED,
                        data.isReversed() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE));
        menu.addMenuClickHandler(getReversedSlot(), (player, i, itemStack1, clickAction) -> {
            data.setReversed(!data.isReversed());
            SlimeAEPlugin.getStorageCellFilterDataController().markDirty(data);
            updateGUI(block);
            return false;
        });
        menu.replaceExistingItem(
                getFuzzySlot(),
                ItemUtils.withType(
                        MenuItems.FUZZY,
                        data.isFuzzy() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE));
        menu.addMenuClickHandler(getFuzzySlot(), (player, i, itemStack1, clickAction) -> {
            data.setFuzzy(!data.isFuzzy());
            SlimeAEPlugin.getStorageCellFilterDataController().markDirty(data);
            updateGUI(block);
            return false;
        });
    }
}
