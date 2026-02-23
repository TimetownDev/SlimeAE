package me.ddggdd135.slimeae.core.slimefun.terminals;

import com.balugaq.jeg.api.groups.SearchGroup;
import com.balugaq.jeg.implementation.JustEnoughGuide;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IItemFilterFindableWithGuide;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.CreativeItemMap;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.core.managers.PinnedManager;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.PinyinCache;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class METerminal extends TickingBlock implements IMEObject, InventoryBlock, IItemFilterFindableWithGuide {
    public static final Comparator<Map.Entry<ItemStack, Long>> ALPHABETICAL_SORT = Comparator.comparing(
            itemStackIntegerEntry -> CMIChatColor.stripColor(ItemUtils.getItemName(itemStackIntegerEntry.getKey())),
            Collator.getInstance(Locale.CHINA)::compare);

    public static final Comparator<Map.Entry<ItemStack, Long>> NUMERICAL_SORT = Map.Entry.comparingByValue();
    public static final Comparator<Map.Entry<ItemStack, Long>> MATERIAL_SORT = Comparator.comparing(
            itemStackIntegerEntry -> itemStackIntegerEntry.getKey().getType().ordinal(), Integer::compare);
    public static final String PAGE_KEY = "page";
    public static final String SORT_KEY = "sort";
    public static final String FILTER_KEY = "filter";

    // === F1: 搜索结果缓存 ===
    private static final Map<String, List<SlimefunItem>> searchCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> searchCacheTime = new ConcurrentHashMap<>();
    private static final long SEARCH_CACHE_TTL = 5000; // 5秒缓存生存期
    private static final int SEARCH_CACHE_MAX_SIZE = 50; // 最多缓存50个不同的filter

    // === F7: 排序结果缓存（每个终端位置一份） ===
    private static final Map<Location, SortedItemsCache> sortedItemsCacheMap = new ConcurrentHashMap<>();

    // === F8: 每个槽位的上一次显示状态缓存 ===
    private static final Map<Location, DisplaySlotCache> displaySlotCacheMap = new ConcurrentHashMap<>();

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
                // 清理缓存，防止内存泄漏
                clearSortedItemsCache(b.getLocation());
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

    @Nonnull
    public String getFilter(@Nonnull Block block) {
        String filter = StorageCacheUtils.getData(block.getLocation(), FILTER_KEY);
        if (filter == null) {
            setFilter(block, "");
            return "";
        }

        return filter;
    }

    public void setFilter(@Nonnull Block block, @Nonnull String filter) {
        StorageCacheUtils.setData(block.getLocation(), FILTER_KEY, filter);
    }

    public void updateGui(@Nonnull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        if (!blockMenu.hasViewer()) return;

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) {
            // 清空显示槽
            for (int slot : getDisplaySlots()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
            }
            return;
        }

        IStorage networkStorage = info.getStorage();
        ItemHashMap<Long> storage = networkStorage.getStorageUnsafe();

        Player player = (Player) blockMenu.getInventory().getViewers().get(0);

        // 获取过滤器
        String filter = getFilter(block).toLowerCase(Locale.ROOT);
        String sortKey = StorageCacheUtils.getData(block.getLocation(), SORT_KEY);
        int sortId = (sortKey != null) ? Integer.parseInt(sortKey) : 0;

        // F5: 计算存储内容的轻量级哈希（size + 总量和）用于脏检测
        Location loc = block.getLocation();
        int storageSize = storage.size();
        long storageTotalAmount = 0;
        for (Map.Entry<ItemStack, Long> e : storage.entrySet()) {
            storageTotalAmount += e.getValue();
        }

        // F7: 检查排序结果缓存
        SortedItemsCache cachedResult = sortedItemsCacheMap.get(loc);
        List<Map.Entry<ItemStack, Long>> items;
        int pinnedCount = 0;

        if (cachedResult != null
                && cachedResult.isValid(
                        filter, sortId, storageSize, storageTotalAmount, storage instanceof CreativeItemMap)) {
            // F5+F7: 缓存命中，数据未变化，复用上次的过滤+排序结果
            items = cachedResult.items;
            pinnedCount = cachedResult.pinnedCount;
        } else {
            // 过滤和排序逻辑
            items = new ArrayList<>(storage.entrySet());
            if (!filter.isEmpty()) {
                if (!SlimeAEPlugin.getJustEnoughGuideIntegration().isLoaded())
                    items.removeIf(x -> doFilterNoJEG(x, filter));
                else {
                    boolean isPinyinSearch = JustEnoughGuide.getConfigManager().isPinyinSearch();
                    // F1: 使用缓存的搜索结果
                    List<SlimefunItem> slimefunItemsList = getCachedFilterItems(player, filter, isPinyinSearch);
                    // F4: List→HashSet 优化 contains 查找为 O(1)
                    Set<SlimefunItem> slimefunItemSet = new HashSet<>(slimefunItemsList);
                    items.removeIf(x -> doFilterWithJEG(x, slimefunItemSet, filter));
                }
            }

            if (storage instanceof CreativeItemMap) items.sort(MATERIAL_SORT);
            else items.sort(getSort(block));

            if (filter.isEmpty()) {
                PinnedManager pinnedManager = SlimeAEPlugin.getPinnedManager();
                List<ItemStack> pinnedItems = pinnedManager.getPinnedItems(player);
                if (pinnedItems == null) pinnedItems = new ArrayList<>();

                for (ItemStack pinned : pinnedItems) {
                    if (!storage.containsKey(pinned)) continue;
                    items.add(0, new AbstractMap.SimpleEntry<>(pinned, storage.get(pinned)));
                    pinnedCount++;
                }
            }

            // 更新缓存
            sortedItemsCacheMap.put(
                    loc,
                    new SortedItemsCache(
                            filter,
                            sortId,
                            storageSize,
                            storageTotalAmount,
                            storage instanceof CreativeItemMap,
                            items,
                            pinnedCount));
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
                blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
            }
        }

        // F8: 获取或创建当前位置的显示槽位缓存
        DisplaySlotCache slotCache = displaySlotCacheMap.computeIfAbsent(loc, k -> new DisplaySlotCache());

        for (int i = 0; i < getDisplaySlots().length && (i + startIndex) < endIndex; i++) {
            int slot = getDisplaySlots()[i];
            if (i + startIndex >= items.size()) {
                // F8: 检查是否已经是空槽
                if (!slotCache.isEmptySlot(slot)) {
                    blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                    blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                    slotCache.markEmpty(slot);
                }
                continue;
            }
            Map.Entry<ItemStack, Long> entry = items.get(i + startIndex);
            ItemStack itemStack = entry.getKey();

            if (itemStack == null || itemStack.getType().isAir()) {
                if (!slotCache.isEmptySlot(slot)) {
                    blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                    blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                    slotCache.markEmpty(slot);
                }
                continue;
            }

            boolean isPinned = i < pinnedCount - page * getDisplaySlots().length;
            long amount = entry.getValue();

            // F8: 检查此槽位的物品和数量是否有变化
            if (slotCache.isUnchanged(slot, itemStack, amount, isPinned)) {
                continue; // 跳过未变化的槽位
            }

            blockMenu.replaceExistingItem(slot, ItemUtils.createDisplayItem(itemStack, amount, true, isPinned));
            blockMenu.addMenuClickHandler(slot, handleGuiClick(block, blockMenu, itemStack));
            slotCache.update(slot, itemStack, amount, isPinned);
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
                        // F7: 置顶变化，使排序缓存失效
                        clearSortedItemsCache(block.getLocation());
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
            if (clickAction.isShiftClicked()) {
                String filter = getFilter(block);
                if (filter.isEmpty()) {
                    player.sendMessage(CMIChatColor.translate("&c&l你还没有设置过滤器"));
                    player.closeInventory();
                    return false;
                }

                player.chat("/sf search " + filter);
                return false;
            }

            if (clickAction.isRightClicked()) {
                setFilter(block, "");
                return false;
            }

            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "请输入你想要过滤的物品名称(显示名)或类型");
            ChatUtils.awaitInput(player, filter -> {
                if (filter.isBlank()) {
                    return;
                }
                setFilter(block, filter.toLowerCase(Locale.ROOT));
                player.sendMessage(ChatColor.GREEN + "已启用过滤器");
                menu.open(player);
            });

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

        addJEGFindingButton(menu, getJEGFindingButtonSlot());
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

    /**
     * 使用 Set 参数的 JEG 过滤方法 (F4: HashSet 优化)
     */
    protected boolean doFilterWithJEG(Map.Entry<ItemStack, Long> x, Set<SlimefunItem> slimefunItems, String filter) {
        ItemStack item = x.getKey();
        if (item.getType().isItem() && SlimefunItem.getOptionalByItem(item).isEmpty()) {
            String displayName = ItemStackHelper.getDisplayName(item);
            String cleanName = ChatColor.stripColor(displayName).toLowerCase(Locale.ROOT);

            // F2: 使用拼音缓存
            String pyName = PinyinCache.toPinyinFull(cleanName);
            String pyFirstLetter = PinyinCache.toPinyinFirstLetter(cleanName);
            boolean matches = cleanName.contains(filter)
                    || pyName.contains(filter.toLowerCase())
                    || pyFirstLetter.contains(filter.toLowerCase());
            return !matches;
        }
        Optional<SlimefunItem> sfItem = SlimefunItem.getOptionalByItem(item);
        // F4: Set.contains() 是 O(1)
        return sfItem.map(s -> !slimefunItems.contains(s)).orElse(true);
    }

    /**
     * F1: 获取缓存的搜索结果。
     * 以 filter 字符串为 key，缓存 filterItems() 返回的 List<SlimefunItem>。
     * 当过滤器未变化时（TTL 内），直接复用缓存。
     */
    protected List<SlimefunItem> getCachedFilterItems(Player player, String filter, boolean isPinyinSearch) {
        long now = System.currentTimeMillis();
        Long cachedTime = searchCacheTime.get(filter);
        if (cachedTime != null && (now - cachedTime) < SEARCH_CACHE_TTL) {
            List<SlimefunItem> cached = searchCache.get(filter);
            if (cached != null) return cached;
        }

        SearchGroup group = new SearchGroup(null, player, filter, isPinyinSearch);
        List<SlimefunItem> result = group.filterItems(player, filter, isPinyinSearch);

        // 缓存大小限制，防止内存泄漏
        if (searchCache.size() >= SEARCH_CACHE_MAX_SIZE) {
            clearSearchCache();
        }

        searchCache.put(filter, result);
        searchCacheTime.put(filter, now);
        return result;
    }

    /**
     * 清空搜索缓存
     */
    public static void clearSearchCache() {
        searchCache.clear();
        searchCacheTime.clear();
    }

    /**
     * 清空指定位置的排序结果缓存和显示槽位缓存
     */
    public static void clearSortedItemsCache(@Nonnull Location loc) {
        sortedItemsCacheMap.remove(loc);
        displaySlotCacheMap.remove(loc);
    }

    /**
     * 清空所有排序结果缓存和显示槽位缓存
     */
    public static void clearAllSortedItemsCache() {
        sortedItemsCacheMap.clear();
        displaySlotCacheMap.clear();
    }

    /**
     * F5+F7: 排序结果缓存内部类
     * 存储上一次的过滤、排序、置顶结果，避免每 tick 重复计算
     * 使用 storage size + total amount + filter + sort 作为脏检测条件
     */
    private static class SortedItemsCache {
        final String filter;
        final int sortId;
        final int storageSize;
        final long storageTotalAmount;
        final boolean isCreative;
        final List<Map.Entry<ItemStack, Long>> items;
        final int pinnedCount;
        final long createTime;
        private static final long CACHE_TTL = 500; // 500ms 缓存超时

        SortedItemsCache(
                String filter,
                int sortId,
                int storageSize,
                long storageTotalAmount,
                boolean isCreative,
                List<Map.Entry<ItemStack, Long>> items,
                int pinnedCount) {
            this.filter = filter;
            this.sortId = sortId;
            this.storageSize = storageSize;
            this.storageTotalAmount = storageTotalAmount;
            this.isCreative = isCreative;
            this.items = items;
            this.pinnedCount = pinnedCount;
            this.createTime = System.currentTimeMillis();
        }

        boolean isValid(String filter, int sortId, int storageSize, long storageTotalAmount, boolean isCreative) {
            if (System.currentTimeMillis() - createTime > CACHE_TTL) return false;
            return this.filter.equals(filter)
                    && this.sortId == sortId
                    && this.storageSize == storageSize
                    && this.storageTotalAmount == storageTotalAmount
                    && this.isCreative == isCreative;
        }
    }

    /**
     * F8: 显示槽位缓存内部类
     * 记录每个槽位上一次显示的物品类型和数量，避免无谓的 replaceExistingItem + createDisplayItem
     */
    private static class DisplaySlotCache {
        // slot -> 编码：物品类型 + 数量 + 是否置顶
        private final Map<Integer, SlotState> slotStates = new HashMap<>();

        boolean isEmptySlot(int slot) {
            SlotState state = slotStates.get(slot);
            return state != null && state.isEmpty;
        }

        void markEmpty(int slot) {
            slotStates.put(slot, SlotState.EMPTY);
        }

        boolean isUnchanged(int slot, @Nonnull ItemStack itemStack, long amount, boolean isPinned) {
            SlotState state = slotStates.get(slot);
            if (state == null || state.isEmpty) return false;
            return state.amount == amount
                    && state.isPinned == isPinned
                    && state.itemType == itemStack.getType()
                    && state.itemHash == itemStack.hashCode();
        }

        void update(int slot, @Nonnull ItemStack itemStack, long amount, boolean isPinned) {
            slotStates.put(slot, new SlotState(itemStack.getType(), itemStack.hashCode(), amount, isPinned));
        }

        private static class SlotState {
            static final SlotState EMPTY = new SlotState(null, 0, 0, false);

            final org.bukkit.Material itemType;
            final int itemHash;
            final long amount;
            final boolean isPinned;
            final boolean isEmpty;

            SlotState(org.bukkit.Material itemType, int itemHash, long amount, boolean isPinned) {
                this.itemType = itemType;
                this.itemHash = itemHash;
                this.amount = amount;
                this.isPinned = isPinned;
                this.isEmpty = (itemType == null);
            }
        }
    }

    public boolean fastInsert() {
        return true;
    }

    public int getJEGFindingButtonSlot() {
        return 17;
    }
}
