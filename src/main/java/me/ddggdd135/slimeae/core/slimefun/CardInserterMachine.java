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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.api.interfaces.ICardHolder;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CardInserterMachine extends TickingBlock implements InventoryBlock, IMEObject {
    private static final int[] INPUT_SLOTS = {0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39
    };
    private static final int[] LEFT_BORDER = {4, 13, 22, 31, 40};
    private static final int[] RIGHT_BORDER = {6, 15, 24, 33, 42};
    private static final int MODE_SLOT = 5;
    private static final int SELECT_SLOT = 14;
    private static final int COUNT_SLOT = 23;
    private static final int INSERT_SLOT = 32;
    private static final int TARGET_SLOT = 41;
    private static final int[] RIGHT_BG = {7, 8, 16, 17, 25, 26, 34, 35, 43, 44};
    private static final int[] BOTTOM_BG = {45, 46, 47, 48, 49, 50, 51, 52, 53};

    private static final int[] TARGET_DISPLAY_SLOTS = {
        0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 29, 30, 31, 32,
        33, 34, 36, 37, 38, 39, 40, 41, 42, 43, 45, 46, 47, 48, 49, 50, 51, 52
    };
    private static final int TARGET_PREV_SLOT = 8;
    private static final int TARGET_NEXT_SLOT = 17;
    private static final int TARGET_FILTER_SLOT = 26;
    private static final int[] TARGET_BG_SLOTS = {35, 44};
    private static final int TARGET_BACK_SLOT = 53;

    private static final String MODE_KEY = "ci_mode";
    private static final String SELECT_KEY = "ci_select";
    private static final String COUNT_KEY = "ci_count";
    private static final String TARGET_KEY = "ci_target";
    private static final String TARGET_MODE_KEY = "ci_tmode";
    private static final String TARGET_TYPE_KEY = "ci_ttype";

    private static final String TMODE_SINGLE = "single";
    private static final String TMODE_TYPE = "type";
    private static final String COOLDOWN_KEY = "ci_cd";
    private static final int AUTO_COOLDOWN_MAX = 10;

    private static final ItemStack MODE_MANUAL =
            new AdvancedCustomItemStack(Material.REDSTONE_TORCH, "&e手动模式", "", "&7点击执行按钮插入一张卡", "&7点击切换为自动模式");
    private static final ItemStack MODE_AUTO =
            new AdvancedCustomItemStack(Material.LEVER, "&a自动模式", "", "&7自动补卡至设定数量上限", "&7点击切换为手动模式");
    private static final ItemStack TMODE_SINGLE_ITEM =
            new AdvancedCustomItemStack(Material.COMPASS, "&e目标模式: &f单目标", "", "&7对选中的单个机器塞卡", "&7点击切换为按类型模式");
    private static final ItemStack TMODE_TYPE_ITEM =
            new AdvancedCustomItemStack(Material.ENDER_EYE, "&a目标模式: &f按类型", "", "&7对网络中所有同类型机器塞卡", "&7点击切换为单目标模式");
    private static final ItemStack NO_TARGET =
            new AdvancedCustomItemStack(Material.BARRIER, "&c&l无目标", "", "&7左键执行插入 &8| &7右键打开选择界面");

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}

    public CardInserterMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    @Override
    public int[] getInputSlots() {
        return INPUT_SLOTS;
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Override
    public void init(@Nonnull BlockMenuPreset preset) {
        for (int slot : LEFT_BORDER) {
            preset.addItem(slot, ChestMenuUtils.getBackground());
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : RIGHT_BORDER) {
            preset.addItem(slot, ChestMenuUtils.getBackground());
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : RIGHT_BG) {
            preset.addItem(slot, ChestMenuUtils.getBackground());
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : BOTTOM_BG) {
            preset.addItem(slot, ChestMenuUtils.getBackground());
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        initMode(menu, block);
        initSelect(menu, block);
        initCount(menu, block);
        initTargetMode(menu, block);
        initTarget(menu, block);

        for (int slot : INPUT_SLOTS) {
            menu.addMenuClickHandler(slot, createCardSlotHandler());
        }
    }

    private ChestMenu.AdvancedMenuClickHandler createCardSlotHandler() {
        return new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent event, Player player, int i, ItemStack cursor, ClickAction action) {
                if (cursor != null && !cursor.getType().isAir()) {
                    Card card = ItemUtils.getSlimefunItemFast(cursor, Card.class);
                    if (card == null) return false;
                }
                return true;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction action) {
                return true;
            }
        };
    }

    private void initMode(BlockMenu menu, Block block) {
        boolean auto = isAutoMode(block);
        menu.replaceExistingItem(MODE_SLOT, auto ? MODE_AUTO : MODE_MANUAL);
        menu.addMenuClickHandler(MODE_SLOT, (p, s, item, action) -> {
            toggleMode(block);
            initMode(menu, block);
            return false;
        });
    }

    private void initTargetMode(BlockMenu menu, Block block) {
        boolean isType = isTypeTargetMode(block);
        menu.replaceExistingItem(INSERT_SLOT, isType ? TMODE_TYPE_ITEM : TMODE_SINGLE_ITEM);
        menu.addMenuClickHandler(INSERT_SLOT, (p, s, item, action) -> {
            toggleTargetMode(block);
            initTargetMode(menu, block);
            initTarget(menu, block);
            return false;
        });
    }

    private void initSelect(BlockMenu menu, Block block) {
        int sel = getSelectIndex(block);
        List<CardEntry> cards = collectCards(menu, block);
        ItemStack display;
        if (cards.isEmpty()) {
            display =
                    new AdvancedCustomItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7无可用卡", "", "&7请在左侧放入升级卡或连接到AE网络");
        } else {
            int idx = sel % cards.size();
            CardEntry entry = cards.get(idx);
            display = new AdvancedCustomItemStack(
                    entry.template.getType(),
                    "&e当前选择: &f" + ItemUtils.getItemName(entry.template),
                    "",
                    "&7可用数量: " + entry.count,
                    "&7点击切换下一张卡");
        }
        menu.replaceExistingItem(SELECT_SLOT, display);
        menu.addMenuClickHandler(SELECT_SLOT, (p, s, item, action) -> {
            setSelectIndex(block, getSelectIndex(block) + 1);
            initSelect(menu, block);
            return false;
        });
    }

    private void initCount(BlockMenu menu, Block block) {
        int count = getInsertCount(block);
        ItemStack display = new AdvancedCustomItemStack(
                Material.PAPER, "&e卡数量上限: &f" + count, "", "&7配置目标机器中该卡不超过此数量(不会自动取出)", "&7点击切换 (1-3)");
        display.setAmount(count);
        menu.replaceExistingItem(COUNT_SLOT, display);
        menu.addMenuClickHandler(COUNT_SLOT, (p, s, item, action) -> {
            int next = (count % 3) + 1;
            setInsertCount(block, next);
            initCount(menu, block);
            return false;
        });
    }

    private void initTarget(BlockMenu menu, Block block) {
        if (isTypeTargetMode(block)) {
            initTargetTypeDisplay(menu, block);
        } else {
            initTargetSingleDisplay(menu, block);
        }
        menu.addMenuClickHandler(TARGET_SLOT, createTargetClickHandler(menu, block));
    }

    private ChestMenu.AdvancedMenuClickHandler createTargetClickHandler(BlockMenu menu, Block block) {
        return new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent event, Player player, int i, ItemStack cursor, ClickAction action) {
                if (action.isRightClicked()) {
                    if (isTypeTargetMode(block)) {
                        openTypeSelectionGui(player, block, 0);
                    } else {
                        openTargetSelectionGui(player, block, 0, -1);
                    }
                } else {
                    doManualInsert(block, menu, player);
                }
                return false;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction action) {
                return false;
            }
        };
    }

    private void initTargetSingleDisplay(BlockMenu menu, Block block) {
        TargetInfo target = getSelectedTarget(block);
        if (target == null) {
            menu.replaceExistingItem(TARGET_SLOT, NO_TARGET);
        } else {
            updateSingleTargetDisplay(menu, target);
        }
    }

    private void initTargetTypeDisplay(BlockMenu menu, Block block) {
        String typeId = getTargetType(block);
        if (typeId == null || typeId.isEmpty()) {
            menu.replaceExistingItem(
                    TARGET_SLOT,
                    new AdvancedCustomItemStack(Material.BARRIER, "&c&l未选择类型", "", "&7左键执行插入 &8| &7右键打开类型选择"));
        } else {
            SlimefunItem sfItem = SlimefunItem.getById(typeId);
            String name = sfItem != null ? sfItem.getItemName() : typeId;
            Material mat = sfItem != null ? sfItem.getItem().getType() : Material.FURNACE;
            List<TargetInfo> sameType = findNetworkCardHoldersByType(block, typeId);
            int total = sameType.size();
            int fullCount = 0;
            int maxCount = getInsertCount(block);
            for (TargetInfo t : sameType) {
                if (countUsedCardSlots(t) >= maxCount) fullCount++;
            }
            menu.replaceExistingItem(
                    TARGET_SLOT,
                    new AdvancedCustomItemStack(
                            mat,
                            "&a类型: &f" + CMIChatColor.stripColor(name),
                            "",
                            "&7网络中共 &e" + total + " &7台",
                            "&7已满(≥" + maxCount + "卡): &e" + fullCount + " &7台",
                            "",
                            "&7左键执行插入 &8| &7右键打开类型选择"));
        }
    }

    private void updateSingleTargetDisplay(BlockMenu menu, TargetInfo target) {
        int usedSlots = 0;
        int totalSlots = target.cardSlots.length;
        BlockMenu targetMenu = StorageCacheUtils.getMenu(target.block.getLocation());
        if (targetMenu != null) {
            for (int slot : target.cardSlots) {
                ItemStack item = ItemUtils.getSettingItem(targetMenu.getInventory(), slot);
                if (item != null && ItemUtils.getSlimefunItemFast(item, Card.class) != null) usedSlots++;
            }
        }
        Location loc = target.block.getLocation();
        String locStr = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
        String sfId = target.sfId;
        SlimefunItem sfItem = SlimefunItem.getById(sfId);
        String name = sfItem != null ? sfItem.getItemName() : sfId;
        menu.replaceExistingItem(
                TARGET_SLOT,
                new AdvancedCustomItemStack(
                        Material.LIME_STAINED_GLASS_PANE,
                        "&a目标: &f" + CMIChatColor.stripColor(name),
                        "",
                        "&7位置: " + locStr,
                        "&7卡槽: " + usedSlots + "/" + totalSlots,
                        "",
                        "&7左键执行插入 &8| &7右键打开目标选择"));
    }

    private void openTargetSelectionGui(Player player, Block block, int page, int filterCardCount) {
        List<TargetInfo> allHolders = findAllNetworkCardHolders(block);

        List<TargetInfo> filtered;
        if (filterCardCount < 0) {
            filtered = allHolders;
        } else {
            filtered = new ArrayList<>();
            for (TargetInfo t : allHolders) {
                int used = countUsedCardSlots(t);
                if (used == filterCardCount) {
                    filtered.add(t);
                }
            }
        }

        int totalItems = filtered.size();
        int slotsPerPage = TARGET_DISPLAY_SLOTS.length;
        int maxPage = Math.max(0, (int) Math.ceil(totalItems / (double) slotsPerPage) - 1);
        int safePage = Math.min(Math.max(page, 0), maxPage);

        AEMenu menu = new AEMenu("&e选择目标机器");
        menu.setSize(54);
        for (int i = 0; i < 54; i++) {
            menu.replaceExistingItem(i, null);
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
        }

        if (safePage > 0) {
            menu.replaceExistingItem(
                    TARGET_PREV_SLOT, new AdvancedCustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c上一页"));
            menu.addMenuClickHandler(TARGET_PREV_SLOT, (p, s, item, action) -> {
                openTargetSelectionGui(p, block, safePage - 1, filterCardCount);
                return false;
            });
        } else {
            menu.replaceExistingItem(TARGET_PREV_SLOT, ChestMenuUtils.getBackground());
            menu.addMenuClickHandler(TARGET_PREV_SLOT, ChestMenuUtils.getEmptyClickHandler());
        }

        if (safePage < maxPage) {
            menu.replaceExistingItem(
                    TARGET_NEXT_SLOT, new AdvancedCustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a下一页"));
            menu.addMenuClickHandler(TARGET_NEXT_SLOT, (p, s, item, action) -> {
                openTargetSelectionGui(p, block, safePage + 1, filterCardCount);
                return false;
            });
        } else {
            menu.replaceExistingItem(TARGET_NEXT_SLOT, ChestMenuUtils.getBackground());
            menu.addMenuClickHandler(TARGET_NEXT_SLOT, ChestMenuUtils.getEmptyClickHandler());
        }

        String filterText;
        if (filterCardCount < 0) {
            filterText = "全部";
        } else {
            filterText = filterCardCount + " 张卡";
        }
        menu.replaceExistingItem(
                TARGET_FILTER_SLOT,
                new AdvancedCustomItemStack(
                        Material.HOPPER,
                        "&e筛选: &f" + filterText,
                        "",
                        "&7当前显示: " + filtered.size() + "/" + allHolders.size() + " 台机器",
                        "&7点击切换筛选条件",
                        "",
                        "&7全部 → 0张卡 → 1张卡 → 2张卡 → 3张卡 → 全部"));
        menu.addMenuClickHandler(TARGET_FILTER_SLOT, (p, s, item, action) -> {
            int nextFilter;
            if (filterCardCount < 0) {
                nextFilter = 0;
            } else if (filterCardCount >= 3) {
                nextFilter = -1;
            } else {
                nextFilter = filterCardCount + 1;
            }
            openTargetSelectionGui(p, block, 0, nextFilter);
            return false;
        });

        for (int slot : TARGET_BG_SLOTS) {
            menu.replaceExistingItem(slot, ChestMenuUtils.getBackground());
            menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }

        menu.replaceExistingItem(TARGET_BACK_SLOT, new AdvancedCustomItemStack(Material.BARRIER, "&c&l返回"));
        menu.addMenuClickHandler(TARGET_BACK_SLOT, (p, s, item, action) -> {
            BlockMenu mainMenu = StorageCacheUtils.getMenu(block.getLocation());
            if (mainMenu != null) {
                mainMenu.open(p);
            } else {
                p.closeInventory();
            }
            return false;
        });

        String currentTargetStr = StorageCacheUtils.getData(block.getLocation(), TARGET_KEY);

        int startIndex = safePage * slotsPerPage;
        for (int i = 0; i < slotsPerPage; i++) {
            int slot = TARGET_DISPLAY_SLOTS[i];
            int dataIdx = startIndex + i;
            if (dataIdx >= filtered.size()) {
                menu.replaceExistingItem(slot, null);
                menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                continue;
            }

            TargetInfo target = filtered.get(dataIdx);
            int usedSlots = countUsedCardSlots(target);
            int totalSlots = target.cardSlots.length;
            Location loc = target.block.getLocation();
            String locStr = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();

            SlimefunItem sfItem = SlimefunItem.getById(target.sfId);
            String machineName = sfItem != null ? sfItem.getItemName() : target.sfId;
            Material displayMat = sfItem != null ? sfItem.getItem().getType() : Material.FURNACE;

            String thisLocStr = loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
            boolean isSelected = thisLocStr.equals(currentTargetStr);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7位置: " + locStr);
            lore.add("&7卡槽: " + usedSlots + "/" + totalSlots);

            BlockMenu targetMenu = StorageCacheUtils.getMenu(loc);
            if (targetMenu != null) {
                for (int cs = 0; cs < target.cardSlots.length; cs++) {
                    int cardSlot = target.cardSlots[cs];
                    ItemStack cardItem = ItemUtils.getSettingItem(targetMenu.getInventory(), cardSlot);
                    if (cardItem != null && ItemUtils.getSlimefunItemFast(cardItem, Card.class) != null) {
                        lore.add("  &a▪ &f" + ItemUtils.getItemName(cardItem));
                    } else {
                        lore.add("  &8▪ 空");
                    }
                }
            }

            lore.add("");
            if (isSelected) {
                lore.add("&a&l✔ 当前选中");
            } else {
                lore.add("&e点击选择此目标");
            }

            String title;
            if (isSelected) {
                title = "&a&l✔ " + CMIChatColor.stripColor(machineName);
            } else {
                title = "&f" + CMIChatColor.stripColor(machineName);
            }

            ItemStack displayItem = new AdvancedCustomItemStack(displayMat, title, lore.toArray(new String[0]));
            menu.replaceExistingItem(slot, displayItem);
            menu.addMenuClickHandler(slot, (p, s, item, action) -> {
                StorageCacheUtils.setData(
                        block.getLocation(),
                        TARGET_KEY,
                        loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
                p.sendMessage(CMIChatColor.translate("&a已选择目标: " + CMIChatColor.stripColor(machineName)));
                BlockMenu mainMenu = StorageCacheUtils.getMenu(block.getLocation());
                if (mainMenu != null) {
                    initTarget(mainMenu, block);
                }
                openTargetSelectionGui(p, block, safePage, filterCardCount);
                return false;
            });
        }

        menu.open(player);
    }

    private void openTypeSelectionGui(Player player, Block block, int page) {
        List<TargetInfo> allHolders = findAllNetworkCardHolders(block);
        Map<String, List<TargetInfo>> typeMap = new java.util.LinkedHashMap<>();
        for (TargetInfo t : allHolders) {
            typeMap.computeIfAbsent(t.sfId, k -> new ArrayList<>()).add(t);
        }
        List<String> typeIds = new ArrayList<>(typeMap.keySet());

        int totalItems = typeIds.size();
        int slotsPerPage = TARGET_DISPLAY_SLOTS.length;
        int maxPage = Math.max(0, (int) Math.ceil(totalItems / (double) slotsPerPage) - 1);
        int safePage = Math.min(Math.max(page, 0), maxPage);

        AEMenu menu = new AEMenu("&e选择机器类型");
        menu.setSize(54);
        for (int i = 0; i < 54; i++) {
            menu.replaceExistingItem(i, null);
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
        }

        if (safePage > 0) {
            menu.replaceExistingItem(
                    TARGET_PREV_SLOT, new AdvancedCustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c上一页"));
            menu.addMenuClickHandler(TARGET_PREV_SLOT, (p, s, item, action) -> {
                openTypeSelectionGui(p, block, safePage - 1);
                return false;
            });
        } else {
            menu.replaceExistingItem(TARGET_PREV_SLOT, ChestMenuUtils.getBackground());
            menu.addMenuClickHandler(TARGET_PREV_SLOT, ChestMenuUtils.getEmptyClickHandler());
        }

        if (safePage < maxPage) {
            menu.replaceExistingItem(
                    TARGET_NEXT_SLOT, new AdvancedCustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a下一页"));
            menu.addMenuClickHandler(TARGET_NEXT_SLOT, (p, s, item, action) -> {
                openTypeSelectionGui(p, block, safePage + 1);
                return false;
            });
        } else {
            menu.replaceExistingItem(TARGET_NEXT_SLOT, ChestMenuUtils.getBackground());
            menu.addMenuClickHandler(TARGET_NEXT_SLOT, ChestMenuUtils.getEmptyClickHandler());
        }

        menu.replaceExistingItem(TARGET_FILTER_SLOT, ChestMenuUtils.getBackground());
        menu.addMenuClickHandler(TARGET_FILTER_SLOT, ChestMenuUtils.getEmptyClickHandler());
        for (int slot : TARGET_BG_SLOTS) {
            menu.replaceExistingItem(slot, ChestMenuUtils.getBackground());
            menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }

        menu.replaceExistingItem(TARGET_BACK_SLOT, new AdvancedCustomItemStack(Material.BARRIER, "&c&l返回"));
        menu.addMenuClickHandler(TARGET_BACK_SLOT, (p, s, item, action) -> {
            BlockMenu mainMenu = StorageCacheUtils.getMenu(block.getLocation());
            if (mainMenu != null) {
                mainMenu.open(p);
            } else {
                p.closeInventory();
            }
            return false;
        });

        String currentTypeId = getTargetType(block);

        int startIndex = safePage * slotsPerPage;
        for (int i = 0; i < slotsPerPage; i++) {
            int slot = TARGET_DISPLAY_SLOTS[i];
            int dataIdx = startIndex + i;
            if (dataIdx >= typeIds.size()) {
                menu.replaceExistingItem(slot, null);
                menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                continue;
            }

            String typeId = typeIds.get(dataIdx);
            List<TargetInfo> machines = typeMap.get(typeId);
            SlimefunItem sfItem = SlimefunItem.getById(typeId);
            String machineName = sfItem != null ? sfItem.getItemName() : typeId;
            Material displayMat = sfItem != null ? sfItem.getItem().getType() : Material.FURNACE;
            boolean isSelected = typeId.equals(currentTypeId);

            int totalMachines = machines.size();
            int emptyCount = 0;
            int partialCount = 0;
            int fullCount = 0;
            for (TargetInfo t : machines) {
                int used = countUsedCardSlots(t);
                if (used == 0) emptyCount++;
                else if (used < t.cardSlots.length) partialCount++;
                else fullCount++;
            }

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7网络中共 &e" + totalMachines + " &7台");
            lore.add("  &7空卡槽: &a" + emptyCount + " &7台");
            lore.add("  &7部分插卡: &e" + partialCount + " &7台");
            lore.add("  &7已满: &c" + fullCount + " &7台");
            lore.add("");
            if (isSelected) {
                lore.add("&a&l✔ 当前选中");
            } else {
                lore.add("&e点击选择此类型");
            }

            String title;
            if (isSelected) {
                title = "&a&l✔ " + CMIChatColor.stripColor(machineName);
            } else {
                title = "&f" + CMIChatColor.stripColor(machineName);
            }

            ItemStack displayItem = new AdvancedCustomItemStack(displayMat, title, lore.toArray(new String[0]));
            if (totalMachines > 1 && totalMachines <= 64) {
                displayItem.setAmount(totalMachines);
            }
            menu.replaceExistingItem(slot, displayItem);
            menu.addMenuClickHandler(slot, (p, s, item, action) -> {
                setTargetType(block, typeId);
                p.sendMessage(CMIChatColor.translate(
                        "&a已选择类型: " + CMIChatColor.stripColor(machineName) + " &7(" + totalMachines + "台)"));
                BlockMenu mainMenu = StorageCacheUtils.getMenu(block.getLocation());
                if (mainMenu != null) {
                    initTarget(mainMenu, block);
                }
                openTypeSelectionGui(p, block, safePage);
                return false;
            });
        }

        menu.open(player);
    }

    private int countUsedCardSlots(TargetInfo target) {
        int usedSlots = 0;
        BlockMenu targetMenu = StorageCacheUtils.getMenu(target.block.getLocation());
        if (targetMenu != null) {
            for (int slot : target.cardSlots) {
                ItemStack item = ItemUtils.getSettingItem(targetMenu.getInventory(), slot);
                if (item != null && ItemUtils.getSlimefunItemFast(item, Card.class) != null) usedSlots++;
            }
        }
        return usedSlots;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if (menu == null) return;

        if (menu.hasViewer()) {
            updateTargetStatus(menu, block);
        }

        if (isAutoMode(block)) {
            int cd = getCooldown(block);
            if (cd > 0) {
                setCooldown(block, cd - 1);
                return;
            }
            boolean inserted;
            if (isTypeTargetMode(block)) {
                inserted = doAutoInsertByType(block, menu);
            } else {
                inserted = doAutoInsert(block, menu);
            }
            if (!inserted) {
                setCooldown(block, AUTO_COOLDOWN_MAX);
            }
        }
    }

    private void updateTargetStatus(BlockMenu menu, Block block) {
        if (isTypeTargetMode(block)) {
            initTargetTypeDisplay(menu, block);
        } else {
            TargetInfo target = getSelectedTarget(block);
            if (target == null) {
                menu.replaceExistingItem(TARGET_SLOT, NO_TARGET);
            } else {
                updateSingleTargetDisplay(menu, target);
            }
        }
    }

    private void doManualInsert(Block block, BlockMenu menu, Player player) {
        if (isTypeTargetMode(block)) {
            doManualInsertByType(block, menu, player);
        } else {
            doManualInsertSingle(block, menu, player);
        }
    }

    private void doManualInsertSingle(Block block, BlockMenu menu, Player player) {
        TargetInfo target = getSelectedTarget(block);
        if (target == null) {
            if (player != null) player.sendMessage(CMIChatColor.translate("&c没有选择目标机器, 请右键打开目标选择界面"));
            return;
        }

        BlockMenu targetMenu = StorageCacheUtils.getMenu(target.block.getLocation());
        if (targetMenu == null) return;

        List<CardEntry> cards = collectCards(menu, block);
        if (cards.isEmpty()) {
            if (player != null) player.sendMessage(CMIChatColor.translate("&c没有可用的卡"));
            return;
        }

        int sel = getSelectIndex(block);
        int idx = sel % cards.size();
        CardEntry entry = cards.get(idx);

        int targetSlot = findEmptyCardSlot(targetMenu, target.cardSlots);
        if (targetSlot == -1) {
            if (player != null) player.sendMessage(CMIChatColor.translate("&c目标机器没有空闲卡槽"));
            return;
        }

        ItemStack taken = takeOneCard(menu, block, entry.template);
        if (taken == null) {
            if (player != null) player.sendMessage(CMIChatColor.translate("&c没有可用的卡"));
            return;
        }

        ItemUtils.setSettingItem(targetMenu.getInventory(), targetSlot, taken);

        SlimefunBlockData targetData = StorageCacheUtils.getBlock(target.block.getLocation());
        if (targetData != null) ICardHolder.updateCache(target.block, target.holder, targetData);
        if (player != null) player.sendMessage(CMIChatColor.translate("&a成功插入了 1 张卡"));
        initSelect(menu, block);
    }

    private void doManualInsertByType(Block block, BlockMenu menu, Player player) {
        String typeId = getTargetType(block);
        if (typeId == null || typeId.isEmpty()) {
            if (player != null) player.sendMessage(CMIChatColor.translate("&c没有选择目标类型, 请右键打开类型选择界面"));
            return;
        }

        List<CardEntry> cards = collectCards(menu, block);
        if (cards.isEmpty()) {
            if (player != null) player.sendMessage(CMIChatColor.translate("&c没有可用的卡"));
            return;
        }

        int sel = getSelectIndex(block);
        int idx = sel % cards.size();
        CardEntry entry = cards.get(idx);

        List<TargetInfo> targets = findNetworkCardHoldersByType(block, typeId);
        if (targets.isEmpty()) {
            if (player != null) player.sendMessage(CMIChatColor.translate("&c网络中没有该类型的机器"));
            return;
        }

        int inserted = 0;
        for (TargetInfo target : targets) {
            BlockMenu targetMenu = StorageCacheUtils.getMenu(target.block.getLocation());
            if (targetMenu == null) continue;

            int targetSlot = findEmptyCardSlot(targetMenu, target.cardSlots);
            if (targetSlot == -1) continue;

            ItemStack taken = takeOneCard(menu, block, entry.template);
            if (taken == null) break;

            ItemUtils.setSettingItem(targetMenu.getInventory(), targetSlot, taken);
            SlimefunBlockData targetData = StorageCacheUtils.getBlock(target.block.getLocation());
            if (targetData != null) ICardHolder.updateCache(target.block, target.holder, targetData);
            inserted++;
        }

        if (player != null) {
            if (inserted > 0) {
                player.sendMessage(CMIChatColor.translate("&a成功向 " + inserted + " 台机器各插入了 1 张卡"));
            } else {
                player.sendMessage(CMIChatColor.translate("&c所有目标机器的卡槽都已满"));
            }
        }
        initSelect(menu, block);
    }

    private boolean doAutoInsert(Block block, BlockMenu menu) {
        TargetInfo target = getSelectedTarget(block);
        if (target == null) return false;

        BlockMenu targetMenu = StorageCacheUtils.getMenu(target.block.getLocation());
        if (targetMenu == null) return false;

        int maxCount = getInsertCount(block);
        if (!hasAnyEmptyCardSlot(targetMenu, target.cardSlots, maxCount)) return false;

        List<CardEntry> cards = collectCards(menu, block);
        if (cards.isEmpty()) return false;

        int sel = getSelectIndex(block);
        int idx = sel % cards.size();
        CardEntry entry = cards.get(idx);

        int currentCount = countCardInTarget(targetMenu, target.cardSlots, entry.template);
        if (currentCount >= maxCount) return false;

        int needInsert = maxCount - currentCount;
        int inserted = 0;

        for (int i = 0; i < needInsert; i++) {
            int targetSlot = findEmptyCardSlot(targetMenu, target.cardSlots);
            if (targetSlot == -1) break;

            ItemStack taken = takeOneCard(menu, block, entry.template);
            if (taken == null) break;

            ItemUtils.setSettingItem(targetMenu.getInventory(), targetSlot, taken);
            inserted++;
        }

        if (inserted > 0) {
            SlimefunBlockData targetData = StorageCacheUtils.getBlock(target.block.getLocation());
            if (targetData != null) ICardHolder.updateCache(target.block, target.holder, targetData);
        }
        return inserted > 0;
    }

    private boolean doAutoInsertByType(Block block, BlockMenu menu) {
        String typeId = getTargetType(block);
        if (typeId == null || typeId.isEmpty()) return false;

        int maxCount = getInsertCount(block);
        List<TargetInfo> targets = findNetworkCardHoldersByType(block, typeId);

        boolean anyNeedInsert = false;
        for (TargetInfo target : targets) {
            BlockMenu targetMenu = StorageCacheUtils.getMenu(target.block.getLocation());
            if (targetMenu != null && hasAnyEmptyCardSlot(targetMenu, target.cardSlots, maxCount)) {
                anyNeedInsert = true;
                break;
            }
        }
        if (!anyNeedInsert) return false;

        List<CardEntry> cards = collectCards(menu, block);
        if (cards.isEmpty()) return false;

        int sel = getSelectIndex(block);
        int idx = sel % cards.size();
        CardEntry entry = cards.get(idx);

        boolean didInsert = false;
        for (TargetInfo target : targets) {
            BlockMenu targetMenu = StorageCacheUtils.getMenu(target.block.getLocation());
            if (targetMenu == null) continue;

            int currentCount = countCardInTarget(targetMenu, target.cardSlots, entry.template);
            if (currentCount >= maxCount) continue;

            int needInsert = maxCount - currentCount;
            int inserted = 0;

            for (int i = 0; i < needInsert; i++) {
                int targetSlot = findEmptyCardSlot(targetMenu, target.cardSlots);
                if (targetSlot == -1) break;

                ItemStack taken = takeOneCard(menu, block, entry.template);
                if (taken == null) return didInsert;

                ItemUtils.setSettingItem(targetMenu.getInventory(), targetSlot, taken);
                inserted++;
            }

            if (inserted > 0) {
                didInsert = true;
                SlimefunBlockData targetData = StorageCacheUtils.getBlock(target.block.getLocation());
                if (targetData != null) ICardHolder.updateCache(target.block, target.holder, targetData);
            }
        }
        return didInsert;
    }

    private int countCardInTarget(BlockMenu targetMenu, int[] cardSlots, ItemStack template) {
        int count = 0;
        for (int slot : cardSlots) {
            ItemStack item = ItemUtils.getSettingItem(targetMenu.getInventory(), slot);
            if (item != null && SlimefunUtils.isItemSimilar(item, template, true, false)) count++;
        }
        return count;
    }

    private ItemStack takeOneCard(BlockMenu menu, Block block, ItemStack template) {
        for (int slot : INPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item != null && !item.getType().isAir() && SlimefunUtils.isItemSimilar(item, template, true, false)) {
                ItemStack single = item.asOne();
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    menu.replaceExistingItem(slot, null);
                }
                return single;
            }
        }

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info != null) {
            IStorage storage = info.getStorage();
            ItemStack[] taken =
                    storage.takeItem(new ItemRequest(new ItemKey(template), 1)).toItemStacks();
            if (taken.length > 0) {
                return taken[0];
            }
        }

        return null;
    }

    private int findEmptyCardSlot(BlockMenu menu, int[] slots) {
        for (int slot : slots) {
            ItemStack item = ItemUtils.getSettingItem(menu.getInventory(), slot);
            if (item == null || SlimefunUtils.isItemSimilar(item, MenuItems.CARD, true, false)) return slot;
        }
        return -1;
    }

    private List<CardEntry> collectCards(BlockMenu menu, Block block) {
        Map<ItemKey, CardEntry> map = new HashMap<>();
        for (int slot : INPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getType().isAir()) continue;
            Card card = ItemUtils.getSlimefunItemFast(item, Card.class);
            if (card == null) continue;
            ItemKey key = new ItemKey(item);
            CardEntry ce = map.get(key);
            if (ce == null) {
                ce = new CardEntry(item.asOne(), card, 0);
                map.put(key, ce);
            }
            ce.count += item.getAmount();
        }

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info != null) {
            for (Map.Entry<ItemStack, Long> entry :
                    info.getStorage().getStorageUnsafe().entrySet()) {
                if (entry.getValue() <= 0) continue;
                Card card = ItemUtils.getSlimefunItemFast(entry.getKey(), Card.class);
                if (card == null) continue;
                ItemKey key = new ItemKey(entry.getKey());
                CardEntry ce = map.get(key);
                if (ce == null) {
                    ce = new CardEntry(entry.getKey().asOne(), card, 0);
                    map.put(key, ce);
                }
                ce.count += entry.getValue().intValue();
            }
        }

        return new ArrayList<>(map.values());
    }

    private TargetInfo getSelectedTarget(Block block) {
        String targetStr = StorageCacheUtils.getData(block.getLocation(), TARGET_KEY);
        if (targetStr == null || targetStr.isEmpty()) return null;
        String[] parts = targetStr.split(",");
        if (parts.length != 3) return null;
        try {
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            int z = Integer.parseInt(parts[2].trim());
            Location loc = new Location(block.getWorld(), x, y, z);
            SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
            if (data == null) {
                StorageCacheUtils.setData(block.getLocation(), TARGET_KEY, "");
                return null;
            }
            SlimefunItem sfItem = SlimefunItem.getById(data.getSfId());
            if (sfItem instanceof ICardHolder holder) {
                return new TargetInfo(loc.getBlock(), holder, holder.getCardSlots(), data.getSfId());
            }
            StorageCacheUtils.setData(block.getLocation(), TARGET_KEY, "");
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<TargetInfo> findAllNetworkCardHolders(Block block) {
        List<TargetInfo> result = new ArrayList<>();
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return result;
        for (Location loc : info.getChildren()) {
            if (loc.equals(block.getLocation())) continue;
            SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
            if (data == null) continue;
            SlimefunItem sfItem = SlimefunItem.getById(data.getSfId());
            if (sfItem instanceof ICardHolder holder) {
                result.add(new TargetInfo(loc.getBlock(), holder, holder.getCardSlots(), data.getSfId()));
            }
        }
        return result;
    }

    private List<TargetInfo> findNetworkCardHoldersByType(Block block, String typeId) {
        List<TargetInfo> result = new ArrayList<>();
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return result;
        for (Location loc : info.getChildren()) {
            if (loc.equals(block.getLocation())) continue;
            SlimefunBlockData data = StorageCacheUtils.getBlock(loc);
            if (data == null) continue;
            if (!typeId.equals(data.getSfId())) continue;
            SlimefunItem sfItem = SlimefunItem.getById(data.getSfId());
            if (sfItem instanceof ICardHolder holder) {
                result.add(new TargetInfo(loc.getBlock(), holder, holder.getCardSlots(), data.getSfId()));
            }
        }
        return result;
    }

    private boolean hasAnyEmptyCardSlot(BlockMenu targetMenu, int[] cardSlots, int maxCount) {
        int used = 0;
        for (int slot : cardSlots) {
            ItemStack item = ItemUtils.getSettingItem(targetMenu.getInventory(), slot);
            if (item != null && ItemUtils.getSlimefunItemFast(item, Card.class) != null) {
                used++;
                if (used >= maxCount) return false;
            }
        }
        return true;
    }

    private int getCooldown(Block block) {
        String val = StorageCacheUtils.getData(block.getLocation(), COOLDOWN_KEY);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setCooldown(Block block, int value) {
        StorageCacheUtils.setData(block.getLocation(), COOLDOWN_KEY, String.valueOf(value));
    }

    private boolean isAutoMode(Block block) {
        String val = StorageCacheUtils.getData(block.getLocation(), MODE_KEY);
        return "1".equals(val);
    }

    private void toggleMode(Block block) {
        boolean current = isAutoMode(block);
        StorageCacheUtils.setData(block.getLocation(), MODE_KEY, current ? "0" : "1");
    }

    private int getSelectIndex(Block block) {
        String val = StorageCacheUtils.getData(block.getLocation(), SELECT_KEY);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setSelectIndex(Block block, int index) {
        StorageCacheUtils.setData(block.getLocation(), SELECT_KEY, String.valueOf(index));
    }

    private int getInsertCount(Block block) {
        String val = StorageCacheUtils.getData(block.getLocation(), COUNT_KEY);
        if (val == null) return 1;
        try {
            int v = Integer.parseInt(val);
            return Math.max(1, Math.min(3, v));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void setInsertCount(Block block, int count) {
        StorageCacheUtils.setData(block.getLocation(), COUNT_KEY, String.valueOf(Math.max(1, Math.min(3, count))));
    }

    private boolean isTypeTargetMode(Block block) {
        String val = StorageCacheUtils.getData(block.getLocation(), TARGET_MODE_KEY);
        return TMODE_TYPE.equals(val);
    }

    private void toggleTargetMode(Block block) {
        boolean isType = isTypeTargetMode(block);
        StorageCacheUtils.setData(block.getLocation(), TARGET_MODE_KEY, isType ? TMODE_SINGLE : TMODE_TYPE);
    }

    private String getTargetType(Block block) {
        return StorageCacheUtils.getData(block.getLocation(), TARGET_TYPE_KEY);
    }

    private void setTargetType(Block block, String typeId) {
        StorageCacheUtils.setData(block.getLocation(), TARGET_TYPE_KEY, typeId != null ? typeId : "");
    }

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu == null) return;
                blockMenu.dropItems(b.getLocation(), INPUT_SLOTS);
            }
        };
    }

    private static class CardEntry {
        ItemStack template;
        Card card;
        int count;

        CardEntry(ItemStack template, Card card, int count) {
            this.template = template;
            this.card = card;
            this.count = count;
        }
    }

    private static class TargetInfo {
        Block block;
        ICardHolder holder;
        int[] cardSlots;
        String sfId;

        TargetInfo(Block block, ICardHolder holder, int[] cardSlots, String sfId) {
            this.block = block;
            this.holder = holder;
            this.cardSlots = cardSlots;
            this.sfId = sfId;
        }
    }
}
