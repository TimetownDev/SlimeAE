package me.ddggdd135.slimeae.core.slimefun.terminals;

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.text.Collator;
import java.util.*;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.core.managers.PinnedManager;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class METerminal extends TickingBlock implements IMEObject, InventoryBlock {
    public static final Comparator<Map.Entry<ItemStack, Long>> ALPHABETICAL_SORT = Comparator.comparing(
            itemStackIntegerEntry -> CMIChatColor.stripColor(ItemUtils.getItemName(itemStackIntegerEntry.getKey())),
            Collator.getInstance(Locale.CHINA)::compare);

    public static final Comparator<Map.Entry<ItemStack, Long>> NUMERICAL_SORT = Map.Entry.comparingByValue();
    public static final Comparator<Map.Entry<ItemStack, Long>> MATERIAL_SORT = Comparator.comparing(
            itemStackIntegerEntry -> itemStackIntegerEntry.getKey().getType().ordinal(), Integer::compare);
    public static final String PAGE_KEY = "page";
    public static final String SORT_KEY = "sort";
    public static final String FILTER_KEY = "filter";

    public int[] getBorderSlots() {
        return new int[] {17, 26};
    }

    public int[] getDisplaySlots() {
        return new int[] {
            0, 1, 2, 3, 4, 5, 6, 7,
            9, 10, 11, 12, 13, 14, 15, 16,
            18, 19, 20, 21, 22, 23, 24, 25,
            27, 28, 29, 30, 31, 32, 33, 34,
            36, 37, 38, 39, 40, 41, 42, 43,
            45, 46, 47, 48, 49, 50, 51, 52
        };
    }

    public int getInputSlot() {
        return 8;
    }

    public int getChangeSort() {
        return 26;
    }

    public int getFilter() {
        return 35;
    }

    public int getPagePrevious() {
        return 44;
    }

    public int getPageNext() {
        return 53;
    }

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        if (blockMenu.hasViewer()) updateGui(block);
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;
        ItemStack itemStack = blockMenu.getItemInSlot(getInputSlot());
        if (itemStack != null && !itemStack.getType().isAir()) info.getStorage().pushItem(itemStack);
    }

    public METerminal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());

                if (blockMenu != null) {
                    blockMenu.dropItems(b.getLocation(), getInputSlot());
                }
            }
        };
    }

    public int getPage(Block block) {
        String value = StorageCacheUtils.getData(block.getLocation(), PAGE_KEY);
        if (value == null || Integer.parseInt(value) < 0) return 0;
        return Integer.parseInt(value);
    }

    public void setPage(Block block, int value) {
        if (value < 0) {
            StorageCacheUtils.setData(block.getLocation(), PAGE_KEY, "0");
            return;
        }
        StorageCacheUtils.setData(block.getLocation(), PAGE_KEY, String.valueOf(value));
    }

    public Comparator<Map.Entry<ItemStack, Long>> getSort(Block block) {
        String value = StorageCacheUtils.getData(block.getLocation(), SORT_KEY);
        if (value == null) return ALPHABETICAL_SORT;
        return int2Sort(Integer.parseInt(value));
    }

    public void setSort(Block block, int value) {
        if (value < 0 || value > 2) {
            StorageCacheUtils.setData(block.getLocation(), SORT_KEY, "0");
            return;
        }
        StorageCacheUtils.setData(block.getLocation(), SORT_KEY, String.valueOf(value));
    }

    public void updateGui(@Nonnull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        if (!blockMenu.hasViewer()) return;
        ItemStack[] display = new ItemStack[getDisplaySlots().length];

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) {
            // 清空显示槽
            for (int slot : getDisplaySlots()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
            }
            return;
        }

        IStorage networkStorage = info.getStorage();
        ItemHashMap<Long> storage = networkStorage.getStorageUnsafe();

        Player player = (Player) blockMenu.getInventory().getViewers().get(0);

        // 过滤和排序逻辑
        List<Map.Entry<ItemStack, Long>> items = new ArrayList<>(storage.entrySet());
        items.sort(MATERIAL_SORT);

        int pinnedCount = 0;

        PinnedManager pinnedManager = SlimeAEPlugin.getPinnedManager();
        List<ItemStack> pinnedItems = pinnedManager.getPinnedItems(player);
        if (pinnedItems == null) pinnedItems = new ArrayList<>();

        for (ItemStack pinned : pinnedItems) {
            if (!storage.containsKey(pinned)) continue;
            items.add(0, new AbstractMap.SimpleEntry<>(pinned, storage.get(pinned)));
            pinnedCount++;
        }

        // 计算分页
        int page = getPage(block);
        int maxPage = (int) Math.max(0, Math.ceil(items.size() / (double) getDisplaySlots().length) - 1);
        if (page > maxPage) {
            page = maxPage;
            setPage(block, page);
        }

        // 显示当前页的物品
        int startIndex = page * getDisplaySlots().length;
        int endIndex = startIndex + getDisplaySlots().length;

        if (startIndex == endIndex) {
            for (int slot : getDisplaySlots()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
            }
        }

        for (int i = 0; i < getDisplaySlots().length && (i + startIndex) < endIndex; i++) {
            int slot = getDisplaySlots()[i];
            if (i + startIndex >= items.size()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                continue;
            }
            Map.Entry<ItemStack, Long> entry = items.get(i + startIndex);
            ItemStack itemStack = entry.getKey();

            if (itemStack == null || itemStack.getType().isAir()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                continue;
            }

            blockMenu.replaceExistingItem(
                    slot,
                    ItemUtils.createDisplayItem(
                            itemStack, entry.getValue(), true, i < pinnedCount - page * getDisplaySlots().length));
            blockMenu.addMenuClickHandler(slot, handleGuiClick(block, blockMenu, itemStack));
        }
    }

    private ChestMenu.AdvancedMenuClickHandler handleGuiClick(Block block, BlockMenu menu, ItemStack display) {
        return new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent inventoryClickEvent,
                    Player player,
                    int i,
                    ItemStack cursor,
                    ClickAction clickAction) {
                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                if (info == null) return false;
                IStorage networkStorage = info.getStorage();
                Inventory playerInventory = player.getInventory();
                ItemStack itemStack = menu.getItemInSlot(i);
                if (itemStack != null
                        && !itemStack.getType().isAir()
                        && !SlimefunUtils.isItemSimilar(itemStack, MenuItems.EMPTY, true, false)) {
                    ItemStack template = display.asQuantity(display.getMaxStackSize());

                    if (SlimefunUtils.isItemSimilar(cursor, SlimeAEItems.AE_TERMINAL_TOPPER, true, false)) {
                        PinnedManager pinnedManager = SlimeAEPlugin.getPinnedManager();
                        List<ItemStack> pinned = pinnedManager.getPinnedItems(player);
                        if (pinned == null) pinned = new ArrayList<>();
                        if (!pinned.contains(template.asOne())) pinnedManager.addPinned(player, template);
                        else pinnedManager.removePinned(player, template);
                        updateGui(block);
                        return false;
                    }

                    if (clickAction.isShiftClicked()
                            && InvUtils.fits(
                                    playerInventory,
                                    template,
                                    IntStream.range(0, 36).toArray())) {
                        playerInventory.addItem(networkStorage
                                .takeItem(new ItemRequest(new ItemKey(template), template.getMaxStackSize()))
                                .toItemStacks());
                    } else if (!clickAction.isShiftClicked() && cursor.getType().isAir()
                            || (SlimefunUtils.isItemSimilar(template, cursor, true, false)
                                    && cursor.getAmount() + 1 <= cursor.getMaxStackSize())) {
                        ItemStack[] gotten = networkStorage
                                .takeItem(new ItemRequest(new ItemKey(template), 1))
                                .toItemStacks();
                        if (gotten.length != 0) {
                            ItemStack newCursor = gotten[0];
                            newCursor.add(cursor.getAmount());
                            player.setItemOnCursor(newCursor);
                        }
                    }
                }
                updateGui(block);
                return false;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        };
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

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
            preset.addItem(slot, ChestMenuUtils.getBackground());
            preset.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(getJEGFindingButtonSlot(), ChestMenuUtils.getBackground());
        preset.addMenuClickHandler(getJEGFindingButtonSlot(), ChestMenuUtils.getEmptyClickHandler());
        preset.setSize(54);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        menu.replaceExistingItem(getPageNext(), MenuItems.PAGE_NEXT_STACK);
        menu.addMenuClickHandler(getPageNext(), (player, i, cursor, clickAction) -> {
            setPage(block, getPage(block) + 1);
            updateGui(block);
            return false;
        });

        menu.replaceExistingItem(getPagePrevious(), MenuItems.PAGE_PREVIOUS_STACK);
        menu.addMenuClickHandler(getPagePrevious(), (player, i, cursor, clickAction) -> {
            setPage(block, getPage(block) - 1);
            updateGui(block);
            return false;
        });

        menu.replaceExistingItem(getChangeSort(), MenuItems.CHANGE_SORT_STACK);
        menu.addMenuClickHandler(getChangeSort(), (player, i, cursor, clickAction) -> {
            String value = StorageCacheUtils.getData(block.getLocation(), SORT_KEY);
            if (value == null) {
                setSort(block, 0);
                return false;
            }
            setSort(block, Integer.parseInt(value) + 1);
            return false;
        });

        menu.replaceExistingItem(getFilter(), MenuItems.FILTER_STACK);
        menu.addMenuClickHandler(getFilter(), (player, i, cursor, clickAction) -> {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "本服务器不支持过滤器");

            return false;
        });

        for (int slot : getDisplaySlots()) {
            menu.replaceExistingItem(slot, MenuItems.EMPTY);
        }

        if (fastInsert()) {
            menu.addPlayerInventoryClickHandler((p, s, itemStack, a) -> {
                if (!a.isShiftClicked() || a.isRightClicked()) {
                    return true;
                }

                // Shift+Left-click
                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                if (info == null) {
                    return false;
                }

                if (itemStack != null && !itemStack.getType().isAir()) {
                    info.getStorage().pushItem(itemStack);
                }

                return false;
            });
        }
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}

    public static Comparator<Map.Entry<ItemStack, Long>> int2Sort(int id) {
        if (id == 0) return ALPHABETICAL_SORT;
        if (id == 1) return NUMERICAL_SORT;
        if (id == 2) return MATERIAL_SORT;
        return ALPHABETICAL_SORT;
    }

    protected boolean doFilterNoJEG(Map.Entry<ItemStack, Long> x, String filter) {
        String itemType = x.getKey().getType().toString().toLowerCase(Locale.ROOT);
        if (itemType.startsWith(filter)) {
            return false;
        }
        String displayName = ItemStackHelper.getDisplayName(x.getKey());
        String cleanName = ChatColor.stripColor(displayName).toLowerCase(Locale.ROOT);

        return !cleanName.contains(filter);
    }

    protected boolean doFilterWithJEG(Map.Entry<ItemStack, Long> x, List<SlimefunItem> slimefunItems, String filter) {
        ItemStack item = x.getKey();
        if (item.getType().isItem() && SlimefunItem.getOptionalByItem(item).isEmpty()) {
            String displayName = ItemStackHelper.getDisplayName(item);
            String cleanName = ChatColor.stripColor(displayName).toLowerCase(Locale.ROOT);

            String pyName = PinyinHelper.toPinyin(cleanName, PinyinStyleEnum.INPUT, "");
            String pyFirstLetter = PinyinHelper.toPinyin(cleanName, PinyinStyleEnum.FIRST_LETTER, "");
            boolean matches = cleanName.contains(filter)
                    || pyName.contains(filter.toLowerCase())
                    || pyFirstLetter.contains(filter.toLowerCase());
            return !matches;
        }
        Optional<SlimefunItem> sfItem = SlimefunItem.getOptionalByItem(item);
        return sfItem.map(s -> !slimefunItems.contains(s)).orElse(true);
    }

    public boolean fastInsert() {
        return true;
    }

    public int getJEGFindingButtonSlot() {
        return 17;
    }
}
