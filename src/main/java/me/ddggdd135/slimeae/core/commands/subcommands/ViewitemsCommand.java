package me.ddggdd135.slimeae.core.commands.subcommands;

import static me.ddggdd135.slimeae.core.slimefun.terminals.METerminal.*;

import com.balugaq.jeg.api.groups.SearchGroup;
import com.balugaq.jeg.implementation.JustEnoughGuide;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.*;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.*;
import me.ddggdd135.slimeae.api.items.CreativeItemMap;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.MEStorageCellCache;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import me.ddggdd135.slimeae.core.slimefun.terminals.METerminal;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ViewitemsCommand extends SubCommand {
    private final Map<UUID, String> filterCache = new HashMap<>();
    private final Map<UUID, Integer> sortCache = new HashMap<>();
    private final Map<UUID, Integer> pageCache = new HashMap<>();

    @Override
    @Nonnull
    public String getName() {
        return "viewitems";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "查看存储元件中的物品";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae viewitems";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!commandSender.hasPermission("slimeae.viewitems")) {
            commandSender.sendMessage(CMIChatColor.translate("&a你没有权限使用这个命令"));
            return false;
        }

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(CMIChatColor.translate("&e只能由玩家运行这个命令"));
            return false;
        } else {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType().isAir()) {
                commandSender.sendMessage(CMIChatColor.translate("&e你是要查看空气吗？"));
                return false;
            }

            MEStorageCellCache data = MEItemStorageCell.getStorage(itemStack);
            if (data == null) {
                commandSender.sendMessage(CMIChatColor.translate("&e你确定你拿着存储元件？"));
                return false;
            }

            filterCache.put(data.getUuid(), "");
            sortCache.put(data.getUuid(), 0);
            pageCache.put(data.getUuid(), 0);

            // ------接下来是抄的METerminal的屎山代码------
            AEMenu menu = new AEMenu("&e存储元件 " + data.getUuid().toString());
            menu.setPlayerInventoryClickable(true);
            menu.setEmptySlotsClickable(true);
            for (int slot : getBorderSlots()) {
                menu.addItem(slot, ChestMenuUtils.getBackground());
                menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
            }
            menu.setSize(54);

            menu.replaceExistingItem(getPageNext(), MenuItems.PAGE_NEXT_STACK);
            menu.addMenuClickHandler(getPageNext(), (ignore, i, cursor, clickAction) -> {
                pageCache.put(data.getUuid(), pageCache.get(data.getUuid()) + 1);
                updateGui(menu, data);
                return false;
            });

            menu.replaceExistingItem(getPagePrevious(), MenuItems.PAGE_PREVIOUS_STACK);
            menu.addMenuClickHandler(getPagePrevious(), (ignore, i, cursor, clickAction) -> {
                pageCache.put(data.getUuid(), pageCache.get(data.getUuid()) - 1);
                updateGui(menu, data);
                return false;
            });

            menu.replaceExistingItem(getChangeSort(), MenuItems.CHANGE_SORT_STACK);
            menu.addMenuClickHandler(getChangeSort(), (ignore, i, cursor, clickAction) -> {
                Comparator<Map.Entry<ItemStack, Long>> sort = METerminal.int2Sort(sortCache.get(data.getUuid()));
                if (sort == ALPHABETICAL_SORT) sortCache.put(data.getUuid(), 1);
                if (sort == NUMERICAL_SORT) sortCache.put(data.getUuid(), 0);
                updateGui(menu, data);
                return false;
            });

            menu.replaceExistingItem(getFilter(), MenuItems.FILTER_STACK);
            menu.addMenuClickHandler(getFilter(), (ignore, i, cursor, clickAction) -> {
                if (clickAction.isRightClicked()) {
                    filterCache.put(data.getUuid(), "");
                    updateGui(menu, data);
                } else {
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "请输入你想要过滤的物品名称(显示名)或类型");
                    ChatUtils.awaitInput(player, filter -> {
                        if (filter.isBlank()) {
                            return;
                        }
                        filterCache.put(data.getUuid(), filter.toLowerCase(Locale.ROOT));
                        player.sendMessage(ChatColor.GREEN + "已启用过滤器");
                        updateGui(menu, data);
                        menu.open(player);
                        createInputThread(menu, data);
                    });
                }
                return false;
            });

            for (int slot : getDisplaySlots()) {
                menu.replaceExistingItem(slot, MenuItems.EMPTY);
                menu.addMenuClickHandler(slot, new ChestMenu.AdvancedMenuClickHandler() {
                    @Override
                    public boolean onClick(
                            InventoryClickEvent inventoryClickEvent,
                            Player player,
                            int i,
                            ItemStack cursor,
                            ClickAction clickAction) {
                        Inventory playerInventory = player.getInventory();
                        ItemStack itemStack = menu.getItemInSlot(i);
                        if (itemStack != null
                                && !itemStack.getType().isAir()
                                && !SlimefunUtils.isItemSimilar(itemStack, MenuItems.EMPTY, true, false)) {
                            ItemStack template = ItemUtils.getDisplayItem(itemStack, true);
                            template.setAmount(template.getMaxStackSize());
                            if (clickAction.isShiftClicked()
                                    && InvUtils.fits(
                                            playerInventory,
                                            template,
                                            IntStream.range(0, 36).toArray())) {
                                playerInventory.addItem(
                                        data.tryTakeItem(new ItemRequest(template, template.getMaxStackSize())));
                            } else if (!clickAction.isShiftClicked()
                                            && cursor.getType().isAir()
                                    || (SlimefunUtils.isItemSimilar(template, cursor, true, false)
                                            && cursor.getAmount() + 1 <= cursor.getMaxStackSize())) {
                                ItemStack[] gotten = data.tryTakeItem(new ItemRequest(template, 1));
                                if (gotten.length != 0) {
                                    ItemStack newCursor = gotten[0];
                                    newCursor.add(cursor.getAmount());
                                    player.setItemOnCursor(newCursor);
                                }
                            }
                        }
                        updateGui(menu, data);
                        return false;
                    }

                    @Override
                    public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                        return false;
                    }
                });
            }

            updateGui(menu, data);
            menu.open(player);
            createInputThread(menu, data);
        }

        return false;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return List.of();
    }

    private void updateGui(AEMenu menu, MEStorageCellCache data) {
        Map<ItemStack, Long> storage = data.getStorage();

        // 获取过滤器
        String filter = filterCache.get(data.getUuid()).toLowerCase(Locale.ROOT);

        // 过滤和排序逻辑
        List<Map.Entry<ItemStack, Long>> items = new ArrayList<>(storage.entrySet());
        if (!filter.isEmpty()) {
            if (!SlimeAEPlugin.getJustEnoughGuideIntegration().isLoaded())
                items.removeIf(x -> {
                    String itemType = x.getKey().getType().toString().toLowerCase(Locale.ROOT);
                    if (itemType.startsWith(filter)) {
                        return false;
                    }
                    String name = CMIChatColor.stripColor(
                            ItemUtils.getItemName(x.getKey()).toLowerCase(Locale.ROOT));

                    return !name.contains(filter);
                });
            else {
                Player player = (Player) menu.getInventory().getViewers().get(0);
                boolean isPinyinSearch = JustEnoughGuide.getConfigManager().isPinyinSearch();
                SearchGroup group = new SearchGroup(null, player, filter, isPinyinSearch);
                List<SlimefunItem> slimefunItems = group.filterItems(player, filter, isPinyinSearch);
                items.removeIf(x -> {
                    SlimefunItem slimefunItem = SlimefunItem.getByItem(x.getKey());
                    if (slimefunItem == null) return true;
                    return !slimefunItems.contains(slimefunItem);
                });
            }
        }

        if (storage instanceof CreativeItemMap) items.sort(MATERIAL_SORT);
        else items.sort(METerminal.int2Sort(sortCache.get(data.getUuid())));

        // 计算分页
        int page = pageCache.get(data.getUuid());
        int maxPage = (int) Math.max(0, Math.ceil(items.size() / (double) getDisplaySlots().length) - 1);
        if (page > maxPage) {
            page = maxPage;
            pageCache.put(data.getUuid(), page);
        }

        if (page < 0) {
            page = 0;
            pageCache.put(data.getUuid(), 0);
        }

        // 显示当前页的物品
        int startIndex = page * getDisplaySlots().length;
        int endIndex = Math.min(startIndex + getDisplaySlots().length, items.size());

        if (startIndex == endIndex) {
            for (int slot : getDisplaySlots()) {
                menu.replaceExistingItem(slot, MenuItems.EMPTY);
            }
        }

        for (int i = 0; i < getDisplaySlots().length && (i + startIndex) < endIndex; i++) {
            int slot = getDisplaySlots()[i];
            if (i + startIndex >= items.size()) {
                menu.replaceExistingItem(slot, MenuItems.EMPTY);
                continue;
            }
            Map.Entry<ItemStack, Long> entry = items.get(i + startIndex);
            ItemStack itemStack = entry.getKey();

            if (itemStack == null || itemStack.getType().isAir()) {
                menu.replaceExistingItem(slot, MenuItems.EMPTY);
                continue;
            }

            menu.replaceExistingItem(slot, ItemUtils.createDisplayItem(itemStack, entry.getValue()));
        }
    }

    private void createInputThread(AEMenu menu, MEStorageCellCache data) {
        new Thread(() -> {
                    while (true) {
                        try {
                            if (menu.getInventory().getViewers().isEmpty()) {
                                return;
                            }
                            ItemStack itemStack = menu.getItemInSlot(getInputSlot());
                            if (itemStack != null && !itemStack.getType().isAir()) {
                                data.pushItem(itemStack);
                                updateGui(menu, data);
                            }
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                })
                .start();
    }

    private int[] getBorderSlots() {
        return new int[] {17, 26};
    }

    private int[] getDisplaySlots() {
        return new int[] {
            0, 1, 2, 3, 4, 5, 6, 7,
            9, 10, 11, 12, 13, 14, 15, 16,
            18, 19, 20, 21, 22, 23, 24, 25,
            27, 28, 29, 30, 31, 32, 33, 34,
            36, 37, 38, 39, 40, 41, 42, 43,
            45, 46, 47, 48, 49, 50, 51, 52
        };
    }

    private int getInputSlot() {
        return 8;
    }

    private int getChangeSort() {
        return 26;
    }

    private int getFilter() {
        return 35;
    }

    private int getPagePrevious() {
        return 44;
    }

    private int getPageNext() {
        return 53;
    }
}
