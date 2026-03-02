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
import me.ddggdd135.slimeae.api.reskin.MaterialValidator;
import me.ddggdd135.slimeae.api.reskin.ReskinApplier;
import me.ddggdd135.slimeae.api.reskin.ReskinDataManager;
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

public class MaterialReskinMachine extends SlimefunItem implements InventoryBlock {

    private static final int TARGET_SLOT = 10;
    private static final int ARROW_SLOT = 12;
    private static final int SKIN_SLOT = 14;
    private static final int PREVIEW_SLOT = 16;
    private static final int INFO_SLOT = 4;
    private static final int START_BUTTON = 31;
    private static final int RESET_BUTTON = 34;

    private static final int[] BORDER_SLOTS = {
        0, 1, 2, 3, 5, 6, 7, 8, 9, 11, 13, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 32, 33, 35, 36,
        37, 38, 39, 40, 41, 42, 43, 44
    };

    private static final ItemStack INFO_ITEM = createInfoItem();
    private static final ItemStack ARROW_ITEM = createArrowItem();
    private static final ItemStack START_ITEM = createStartItem();
    private static final ItemStack RESET_ITEM = createResetItem();
    private static final ItemStack PREVIEW_PLACEHOLDER = createPreviewPlaceholder();

    public MaterialReskinMachine(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    @Override
    public int[] getInputSlots() {
        return new int[] {TARGET_SLOT, SKIN_SLOT};
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
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

        preset.addItem(PREVIEW_SLOT, PREVIEW_PLACEHOLDER);
        preset.addMenuClickHandler(PREVIEW_SLOT, ChestMenuUtils.getEmptyClickHandler());

        preset.addItem(START_BUTTON, START_ITEM);
        preset.addItem(RESET_BUTTON, RESET_ITEM);
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        menu.addMenuClickHandler(TARGET_SLOT, new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent e, Player player, int slot, ItemStack cursor, ClickAction action) {
                Inventory inv = e.getClickedInventory();
                ItemStack current = inv != null ? inv.getItem(slot) : null;

                if (isPlaceholder(current)) {
                    if (cursor != null && !cursor.getType().isAir()) {
                        if (isValidTargetItem(cursor)) {
                            inv.setItem(slot, cursor.clone());
                            player.setItemOnCursor(null);
                        } else {
                            player.sendMessage(ChatColor.RED + "该物品不是有效的SlimeAE方块物品！");
                        }
                    }
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        player.setItemOnCursor(current);
                        inv.setItem(slot, null);
                    } else if (isValidTargetItem(cursor)) {
                        inv.setItem(slot, cursor.clone());
                        player.setItemOnCursor(current);
                    }
                }
                updatePreview(menu);
                return false;
            }

            @Override
            public boolean onClick(Player player, int slot, ItemStack item, ClickAction action) {
                return false;
            }
        });

        menu.addMenuClickHandler(SKIN_SLOT, new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent e, Player player, int slot, ItemStack cursor, ClickAction action) {
                Inventory inv = e.getClickedInventory();
                ItemStack current = inv != null ? inv.getItem(slot) : null;

                if (isPlaceholder(current)) {
                    if (cursor != null && !cursor.getType().isAir()) {
                        if (MaterialValidator.isValidSkinItem(cursor)) {
                            inv.setItem(slot, cursor.clone());
                            player.setItemOnCursor(null);
                        } else {
                            player.sendMessage(ChatColor.RED + "该物品不是有效的材质方块！");
                        }
                    }
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        player.setItemOnCursor(current);
                        inv.setItem(slot, null);
                    } else if (MaterialValidator.isValidSkinItem(cursor)) {
                        inv.setItem(slot, cursor.clone());
                        player.setItemOnCursor(current);
                    }
                }
                updatePreview(menu);
                return false;
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

                ItemStack targetCurrent = menu.getItemInSlot(TARGET_SLOT);
                if (isPlaceholder(targetCurrent) && isValidTargetItem(clickedItem)) {
                    menu.replaceExistingItem(TARGET_SLOT, clickedItem.clone());
                    clickedItem.setAmount(0);
                    updatePreview(menu);
                    return false;
                }

                ItemStack skinCurrent = menu.getItemInSlot(SKIN_SLOT);
                if (isPlaceholder(skinCurrent) && MaterialValidator.isValidSkinItem(clickedItem)) {
                    menu.replaceExistingItem(SKIN_SLOT, clickedItem.clone());
                    clickedItem.setAmount(0);
                    updatePreview(menu);
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

        menu.addMenuClickHandler(RESET_BUTTON, (player, slot, item, action) -> {
            onResetClick(menu, player);
            return false;
        });
    }

    private void onStartClick(@Nonnull BlockMenu menu, @Nonnull Player player) {
        ItemStack targetItem = menu.getItemInSlot(TARGET_SLOT);
        ItemStack skinItem = menu.getItemInSlot(SKIN_SLOT);

        if (targetItem == null || targetItem.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "请放入需要转换外观的SlimeAE方块物品！");
            return;
        }
        if (skinItem == null || skinItem.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "请放入材质方块作为外观源！");
            return;
        }

        SlimefunItem sfItem = SlimefunItem.getByItem(targetItem);
        if (sfItem == null || !MaterialValidator.isValidTarget(sfItem.getId())) {
            player.sendMessage(ChatColor.RED + "该物品不支持材质转换！");
            return;
        }

        String[] skinInfo = ReskinApplier.extractSkinInfo(skinItem);
        if (skinInfo == null) {
            player.sendMessage(ChatColor.RED + "无法从该物品提取材质信息！");
            return;
        }

        ReskinDataManager.addReskinData(targetItem, skinInfo[0], skinInfo[1]);

        updatePreview(menu);

        player.sendMessage(ChatColor.GREEN + "材质转换成功！取出物品后放置即可看到新外观。");
    }

    private void onResetClick(@Nonnull BlockMenu menu, @Nonnull Player player) {
        ItemStack targetItem = menu.getItemInSlot(TARGET_SLOT);

        if (targetItem == null || targetItem.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "请放入需要重置外观的物品！");
            return;
        }

        if (!ReskinDataManager.hasReskinData(targetItem)) {
            player.sendMessage(ChatColor.RED + "该物品没有自定义外观数据！");
            return;
        }

        ReskinDataManager.removeReskinData(targetItem);
        updatePreview(menu);

        player.sendMessage(ChatColor.GREEN + "材质已重置为默认外观。");
    }

    private void updatePreview(@Nonnull BlockMenu menu) {
        ItemStack targetItem = menu.getItemInSlot(TARGET_SLOT);
        ItemStack skinItem = menu.getItemInSlot(SKIN_SLOT);

        if (targetItem == null || targetItem.getType().isAir()) {
            menu.replaceExistingItem(PREVIEW_SLOT, PREVIEW_PLACEHOLDER);
            return;
        }

        String[] existingReskin = ReskinDataManager.getReskinData(targetItem);
        if (existingReskin != null) {
            ItemStack preview = createPreviewFromReskin(targetItem, existingReskin[0], existingReskin[1]);
            menu.replaceExistingItem(PREVIEW_SLOT, preview);
            return;
        }

        if (skinItem != null && !skinItem.getType().isAir()) {
            String[] skinInfo = ReskinApplier.extractSkinInfo(skinItem);
            if (skinInfo != null) {
                ItemStack preview = createPreviewFromReskin(targetItem, skinInfo[0], skinInfo[1]);
                menu.replaceExistingItem(PREVIEW_SLOT, preview);
                return;
            }
        }

        ItemStack preview = targetItem.clone();
        preview.setAmount(1);
        menu.replaceExistingItem(PREVIEW_SLOT, preview);
    }

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu menu = StorageCacheUtils.getMenu(b.getLocation());
                if (menu == null) return;

                ItemStack target = menu.getItemInSlot(TARGET_SLOT);
                if (target != null && !target.getType().isAir()) {
                    b.getWorld().dropItem(b.getLocation(), target);
                }

                ItemStack skin = menu.getItemInSlot(SKIN_SLOT);
                if (skin != null && !skin.getType().isAir()) {
                    b.getWorld().dropItem(b.getLocation(), skin);
                }
            }
        };
    }

    private static boolean isValidTargetItem(ItemStack item) {
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (sfItem == null) return false;
        return MaterialValidator.isValidTarget(sfItem.getId());
    }

    private static boolean isPlaceholder(ItemStack item) {
        return item == null || item.getType().isAir();
    }

    private static ItemStack createPreviewFromReskin(ItemStack targetItem, String type, String value) {
        ItemStack preview = targetItem.clone();
        preview.setAmount(1);
        if ("material".equals(type)) {
            try {
                Material mat = Material.valueOf(value);
                ItemMeta meta = preview.getItemMeta();
                if (meta != null) {
                    preview = new ItemStack(mat);
                    preview.setItemMeta(meta);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return preview;
    }

    private static ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "ME材质转换机");
            meta.setLore(java.util.List.of(
                    "",
                    ChatColor.GRAY + "左侧放入SlimeAE机器",
                    ChatColor.GRAY + "右侧放入材质方块或头颅",
                    ChatColor.GRAY + "点击确认按钮进行转换",
                    "",
                    ChatColor.GRAY + "转换后的物品放置后会显示新外观"));
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
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "确认转换");
            meta.setLore(java.util.List.of("", ChatColor.GRAY + "点击将材质应用到目标物品"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createResetItem() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "重置外观");
            meta.setLore(java.util.List.of("", ChatColor.GRAY + "点击清除目标物品的自定义外观"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createPreviewPlaceholder() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "预览");
            meta.setLore(java.util.List.of("", ChatColor.GRAY + "放入物品后显示转换效果"));
            item.setItemMeta(meta);
        }
        return item;
    }
}
