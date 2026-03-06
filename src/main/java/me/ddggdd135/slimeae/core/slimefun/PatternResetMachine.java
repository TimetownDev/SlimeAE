package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PatternResetMachine extends SlimefunItem implements InventoryBlock {

    private static final int INPUT_SLOT = 10;
    private static final int ARROW_SLOT = 12;
    private static final int OUTPUT_SLOT = 14;
    private static final int INFO_SLOT = 4;
    private static final int START_BUTTON = 16;

    private static final int[] BORDER_SLOTS = {
        0, 1, 2, 3, 5, 6, 7, 8, 9, 11, 13, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26
    };

    private static final ItemStack INFO_ITEM = createInfoItem();
    private static final ItemStack ARROW_ITEM = createArrowItem();
    private static final ItemStack START_ITEM = createStartItem();

    public PatternResetMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    @Override
    public int[] getInputSlots() {
        return new int[] {INPUT_SLOT};
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] {OUTPUT_SLOT};
    }

    @Override
    public void init(@Nonnull BlockMenuPreset preset) {
        for (int slot : BORDER_SLOTS) {
            preset.addItem(slot, ChestMenuUtils.getBackground());
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(INFO_SLOT, INFO_ITEM);
        preset.addMenuClickHandler(INFO_SLOT, ChestMenuUtils.getEmptyClickHandler());

        preset.addItem(ARROW_SLOT, ARROW_ITEM);
        preset.addMenuClickHandler(ARROW_SLOT, ChestMenuUtils.getEmptyClickHandler());

        preset.addItem(START_BUTTON, START_ITEM);
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        menu.addMenuClickHandler(INPUT_SLOT, new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent e, Player player, int slot, ItemStack cursor, ClickAction action) {
                Inventory inv = e.getClickedInventory();
                ItemStack current = inv != null ? inv.getItem(slot) : null;

                if (current == null || current.getType().isAir()) {
                    if (cursor != null && !cursor.getType().isAir()) {
                        if (isPattern(cursor)) {
                            inv.setItem(slot, cursor.clone());
                            player.setItemOnCursor(null);
                        } else {
                            player.sendMessage(ChatColor.RED + "只能放入样板！");
                        }
                    }
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        player.setItemOnCursor(current);
                        inv.setItem(slot, null);
                    } else if (isPattern(cursor)) {
                        inv.setItem(slot, cursor.clone());
                        player.setItemOnCursor(current);
                    } else {
                        player.sendMessage(ChatColor.RED + "只能放入样板！");
                    }
                }
                return false;
            }

            @Override
            public boolean onClick(Player player, int slot, ItemStack item, ClickAction action) {
                return false;
            }
        });

        menu.addMenuClickHandler(OUTPUT_SLOT, new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent e, Player player, int slot, ItemStack cursor, ClickAction action) {
                Inventory inv = e.getClickedInventory();
                ItemStack current = inv != null ? inv.getItem(slot) : null;

                if (current == null || current.getType().isAir()) {
                    if (cursor != null && !cursor.getType().isAir()) {
                        player.sendMessage(ChatColor.RED + "输出槽不能放入物品！");
                    }
                    return false;
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        player.setItemOnCursor(current);
                        inv.setItem(slot, null);
                    } else if (current.isSimilar(cursor)
                            && cursor.getAmount() + current.getAmount() <= cursor.getMaxStackSize()) {
                        cursor.setAmount(cursor.getAmount() + current.getAmount());
                        inv.setItem(slot, null);
                    }
                    return false;
                }
            }

            @Override
            public boolean onClick(Player player, int slot, ItemStack item, ClickAction action) {
                return false;
            }
        });

        menu.addPlayerInventoryClickHandler(new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent e, Player player, int slot, ItemStack cursor, ClickAction action) {
                if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    return true;
                }
                ItemStack clickedItem = e.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) return false;

                ItemStack inputCurrent = menu.getItemInSlot(INPUT_SLOT);
                if ((inputCurrent == null || inputCurrent.getType().isAir()) && isPattern(clickedItem)) {
                    menu.replaceExistingItem(INPUT_SLOT, clickedItem.clone());
                    clickedItem.setAmount(0);
                    return false;
                }

                return false;
            }

            @Override
            public boolean onClick(Player player, int slot, ItemStack item, ClickAction action) {
                return true;
            }
        });

        menu.addMenuClickHandler(START_BUTTON, (player, slot, item, action) -> {
            onStartClick(menu, player);
            return false;
        });
    }

    private void onStartClick(@Nonnull BlockMenu menu, @Nonnull Player player) {
        ItemStack inputItem = menu.getItemInSlot(INPUT_SLOT);

        if (inputItem == null || inputItem.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "请在输入槽放入编码样板！");
            return;
        }

        if (!isPattern(inputItem)) {
            player.sendMessage(ChatColor.RED + "输入物品不是样板！");
            return;
        }

        ItemStack outputItem = menu.getItemInSlot(OUTPUT_SLOT);
        if (outputItem != null && !outputItem.getType().isAir()) {
            if (outputItem.isSimilar(SlimeAEItems.BLANK_PATTERN)
                    && outputItem.getAmount() + 1 <= outputItem.getMaxStackSize()) {
                outputItem.setAmount(outputItem.getAmount() + 1);
            } else {
                player.sendMessage(ChatColor.RED + "输出槽已满，请先取出物品！");
                return;
            }
        } else {
            menu.replaceExistingItem(OUTPUT_SLOT, SlimeAEItems.BLANK_PATTERN.clone());
        }

        if (inputItem.getAmount() > 1) {
            inputItem.setAmount(inputItem.getAmount() - 1);
        } else {
            menu.replaceExistingItem(INPUT_SLOT, null);
        }

        player.sendMessage(ChatColor.GREEN + "样板已重置为空白样板！");
    }

    private static boolean isPattern(ItemStack item) {
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        return sfItem instanceof Pattern;
    }

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu menu = StorageCacheUtils.getMenu(b.getLocation());
                if (menu == null) return;

                ItemStack input = menu.getItemInSlot(INPUT_SLOT);
                if (input != null && !input.getType().isAir()) {
                    b.getWorld().dropItem(b.getLocation(), input);
                }

                ItemStack output = menu.getItemInSlot(OUTPUT_SLOT);
                if (output != null && !output.getType().isAir()) {
                    b.getWorld().dropItem(b.getLocation(), output);
                }
            }
        };
    }

    private static ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "样板重置机");
            meta.setLore(java.util.List.of("", ChatColor.GRAY + "在输入槽放入任意样板", ChatColor.GRAY + "点击确认按钮将其还原为空白样板"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createArrowItem() {
        ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "→");
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createStartItem() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "确认重置");
            meta.setLore(java.util.List.of("", ChatColor.GRAY + "点击将样板还原为空白样板"));
            item.setItemMeta(meta);
        }
        return item;
    }
}
