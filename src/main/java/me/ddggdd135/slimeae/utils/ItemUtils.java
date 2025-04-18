package me.ddggdd135.slimeae.utils;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.github.mooy1.infinityexpansion.items.storage.StorageUnit;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
import java.util.*;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.ItemHashSet;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.api.interfaces.ICardHolder;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.ISettingSlotHolder;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.slimefun.Pattern;
import me.ddggdd135.slimeae.integrations.fluffyMachines.FluffyBarrelStorage;
import me.ddggdd135.slimeae.integrations.infinity.InfinityBarrelStorage;
import me.ddggdd135.slimeae.integrations.networks.QuantumStorage;
import me.ddggdd135.slimeae.integrations.networksexpansion.DrawerStorage;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.inventory.ItemStackHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * 物品操作工具类
 * 提供了一系列处理物品堆、存储和显示的实用方法
 */
public class ItemUtils {
    public static final String DISPLAY_ITEM_KEY = "display_item";
    public static final String SOURCE_LORE_KEY = "source_lore";
    /**
     * 根据模板物品创建指定数量的物品堆数组
     *
     * @param template 模板物品
     * @param amount   总数量
     * @return 物品堆数组，每个物品堆不超过最大堆叠数
     */
    @Nonnull
    public static ItemStack[] createItems(@Nonnull ItemStack template, long amount) {
        ItemStack[] itemStacks =
                new ItemStack[(int) Math.max(1, Math.ceil(amount / (double) template.getMaxStackSize()))];
        long rest = amount;
        for (int i = 0; i < itemStacks.length; i++) {
            if (rest <= template.getMaxStackSize()) {
                itemStacks[i] = template.asQuantity((int) rest);
            } else {
                rest -= template.getMaxStackSize();
                itemStacks[i] = template.asQuantity(template.getMaxStackSize());
            }
        }
        return itemStacks;
    }

    /**
     * 将存储映射转换为物品堆数组
     *
     * @param storage 物品到数量的映射
     * @return 物品堆数组
     */
    @Nonnull
    public static ItemStack[] createItems(@Nonnull ItemHashMap<Long> storage) {
        int len = 0;
        for (Map.Entry<ItemStack, Long> i : storage.entrySet()) {
            len += (int)
                    Math.max(1, Math.ceil(i.getValue() / (double) i.getKey().getMaxStackSize()));
        }
        ItemStack[] itemStacks = new ItemStack[len];

        int c = 0;
        for (Map.Entry<ItemStack, Long> i : storage.entrySet()) {
            long rest = i.getValue();
            int l = (int)
                    Math.max(1, Math.ceil(i.getValue() / (double) i.getKey().getMaxStackSize()));
            ItemStack template = i.getKey();
            for (int j = 0; j < l; j++) {
                if (rest <= template.getMaxStackSize()) {
                    itemStacks[c] = template.asQuantity((int) rest);
                } else {
                    rest -= template.getMaxStackSize();
                    itemStacks[c] = template.asQuantity(template.getMaxStackSize());
                }
                c++;
            }
        }

        return itemStacks;
    }

    /**
     * 根据物品请求创建物品堆数组
     *
     * @param requests 物品请求数组
     * @return 物品堆数组
     */
    @Nonnull
    public static ItemStack[] createItems(@Nonnull ItemRequest[] requests) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemRequest request : requests) {
            itemStacks.addAll(List.of(createItems(request.getKey().getItemStack(), request.getAmount())));
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

    /**
     * 移除数量为0的物品堆
     *
     * @param itemStacks 要处理的物品堆数组
     * @return 处理后的物品堆数组
     */
    @Nonnull
    public static ItemStack[] trimItems(@Nonnull ItemStack[] itemStacks) {
        List<ItemStack> itemStackList = new ArrayList<>(itemStacks.length);
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType().isAir()) continue;
            if (itemStack.getAmount() > 0) {
                itemStackList.add(itemStack);
            }
        }
        return itemStackList.toArray(new ItemStack[0]);
    }

    /**
     * 检查存储中是否包含所有请求的物品
     *
     * @param storage  物品存储
     * @param requests 物品请求数组
     * @return 是否包含所有请求的物品
     */
    public static boolean contains(@Nonnull ItemHashMap<Long> storage, @Nonnull ItemRequest[] requests) {
        for (ItemRequest request : requests) {
            if (!storage.containsKey(request.getKey()) || storage.getKey(request.getKey()) < request.getAmount())
                return false;
        }
        return true;
    }

    /**
     * 检查存储中是否包含指定的物品请求
     *
     * @param storage 物品存储
     * @param request 物品请求
     * @return 是否包含请求的物品
     */
    public static boolean contains(@Nonnull ItemHashMap<Long> storage, @Nonnull ItemRequest request) {
        return storage.containsKey(request.getKey()) && storage.getKey(request.getKey()) >= request.getAmount();
    }

    /**
     * 将物品存储映射转换为物品请求数组
     *
     * @param itemStacks 物品存储映射
     * @return 物品请求数组
     */
    @Nonnull
    public static ItemRequest[] createRequests(@Nonnull ItemHashMap<Long> itemStacks) {
        List<ItemRequest> requests = new ArrayList<>();
        for (ItemKey key : itemStacks.sourceKeySet()) {
            requests.add(new ItemRequest(key, itemStacks.getKey(key)));
        }
        return requests.toArray(new ItemRequest[0]);
    }

    /**
     * 获取物品堆数组中每种物品的数量
     *
     * @param itemStacks 物品堆数组
     * @return 物品到数量的映射
     */
    @Nonnull
    public static ItemHashMap<Long> getAmounts(@Nonnull ItemStack[] itemStacks) {
        ItemHashMap<Long> storage = new ItemHashMap<>();
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType().isAir()) continue;
            ItemStack template = itemStack.asOne();
            if (storage.containsKey(template)) {
                storage.put(template, storage.get(template) + itemStack.getAmount());
            } else {
                storage.put(template, (long) itemStack.getAmount());
            }
        }
        return storage;
    }

    @Nonnull
    public static ItemHashMap<Long> getAmounts(@Nonnull ItemRequest[] requests) {
        ItemHashMap<Long> storage = new ItemHashMap<>();
        for (ItemRequest request : requests) {
            ItemKey key = request.getKey();
            if (storage.containsKey(key)) {
                storage.putKey(key, storage.getKey(key) + request.getAmount());
            } else {
                storage.putKey(key, request.getAmount());
            }
        }
        return storage;
    }

    /**
     * 从源存储中移除指定的物品
     *
     * @param source 源存储
     * @param toTake 要移除的物品及数量
     * @return 更新后的存储映射
     */
    @Nonnull
    public static ItemHashMap<Long> takeItems(@Nonnull ItemHashMap<Long> source, @Nonnull ItemHashMap<Long> toTake) {
        ItemHashMap<Long> storage = new ItemHashMap<>(source);
        for (Map.Entry<ItemKey, Long> data : toTake.keyEntrySet()) {
            ItemKey key = data.getKey();
            if (storage.containsKey(key)) {
                storage.putKey(key, storage.getKey(key) - data.getValue());
            } else {
                storage.putKey(key, -data.getValue());
            }
        }
        return storage;
    }

    /**
     * 向源存储中添加物品
     *
     * @param source 源存储
     * @param toAdd  要添加的物品及数量
     * @return 更新后的存储映射
     */
    @Nonnull
    public static ItemHashMap<Long> addItems(@Nonnull ItemHashMap<Long> source, @Nonnull ItemHashMap<Long> toAdd) {
        ItemHashMap<Long> storage = new ItemHashMap<>(source);
        for (Map.Entry<ItemKey, Long> data : toAdd.keyEntrySet()) {
            ItemKey key = data.getKey();
            if (storage.containsKey(key)) {
                storage.putKey(key, storage.getKey(key) + data.getValue());
            } else {
                storage.putKey(key, data.getValue());
            }
        }
        return storage;
    }

    /**
     * 移除存储中数量为0或空的物品
     *
     * @param storage 要清理的存储映射
     */
    public static void trim(@Nonnull ItemHashMap<Long> storage) {
        for (Iterator<Map.Entry<ItemKey, Long>> it = storage.keyEntrySet().iterator(); it.hasNext(); ) {
            Map.Entry<ItemKey, Long> data = it.next();
            ItemKey key = data.getKey();
            if (key == null
                    || key.getItemStack().getType().isAir()
                    || key.getItemStack().getType().isAir()
                    || storage.getKey(key) <= 0) it.remove();
        }
    }

    public static boolean contains(BlockMenu inv, int[] slots, ItemStack[] itemStacks) {
        ItemHashMap<Long> toTake = getAmounts(itemStacks);

        for (ItemStack itemStack : toTake.keySet()) {
            if (toTake.get(itemStack) > getItemAmount(inv, slots, itemStack)) {
                return false;
            }
        }
        return true;
    }

    public static boolean contains(Inventory inv, int[] slots, ItemStack[] itemStacks) {
        ItemHashMap<Long> toTake = getAmounts(itemStacks);

        for (ItemStack itemStack : toTake.keySet()) {
            if (toTake.get(itemStack) > getItemAmount(inv, slots, itemStack)) {
                return false;
            }
        }
        return true;
    }

    public static int getItemAmount(BlockMenu blockMenu, int[] slots, ItemStack itemStack) {
        int founded = 0;
        for (int slot : slots) {
            ItemStack item = blockMenu.getItemInSlot(slot);
            if (item == null || item.getType().isAir()) continue;
            if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                founded += item.getAmount();
            }
        }
        return founded;
    }

    public static int getItemAmount(Inventory inv, int[] slots, ItemStack itemStack) {
        int founded = 0;
        for (int slot : slots) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                founded += item.getAmount();
            }
        }
        return founded;
    }

    @Nullable public static IStorage getStorage(@Nonnull Block block) {
        return getStorage(block, true);
    }

    @Nullable public static IStorage getStorage(@Nonnull Block block, boolean checkNetwork) {
        return getStorage(block, checkNetwork, true);
    }

    @Nullable public static IStorage getStorage(@Nonnull Block block, boolean checkNetwork, boolean isReadOnly) {
        return getStorage(block, checkNetwork, isReadOnly, false);
    }

    /**
     * 获取方块菜单中指定槽位的物品存储
     *
     * @param block        目标方块
     * @param checkNetwork 是否检查网络
     * @param isReadOnly   是否只读
     * @param allowVanilla 是否允许原版容器
     * @return 存储接口，如果无法获取则返回null
     */
    @Nullable public static IStorage getStorage(
            @Nonnull Block block, boolean checkNetwork, boolean isReadOnly, boolean allowVanilla) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
        if (slimefunBlockData != null) {
            SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());

            if (checkNetwork && slimefunItem instanceof IMEObject) {
                return null;
            }
            if (SlimeAEPlugin.getInfinityIntegration().isLoaded()) {
                if (SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof StorageUnit) {
                    return new InfinityBarrelStorage(block);
                }
            }
            if (SlimeAEPlugin.getFluffyMachinesIntegration().isLoaded()) {
                if (SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof Barrel) {
                    return new FluffyBarrelStorage(block);
                }
            }
            if (SlimeAEPlugin.getNetworksIntegration().isLoaded()
                    || SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded()) {
                if (SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof NetworkQuantumStorage) {
                    return new QuantumStorage(block);
                }
            }
            if (SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded()) {
                if (SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof NetworksDrawer
                        && NetworksDrawer.getStorageData(block.getLocation()) != null) {
                    return new DrawerStorage(block);
                }
            }
        }

        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());

        if (inv != null) {
            boolean finalIsReadOnly = isReadOnly;
            return new IStorage() {
                @Override
                public void pushItem(@Nonnull ItemStackCache itemStackCache) {
                    ItemStack itemStack = itemStackCache.getItemStack();

                    if (finalIsReadOnly) return;
                    BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                    if (blockMenu == null) return;
                    if (itemStack.getType().isAir()) return;
                    int[] inputSlots = blockMenu
                            .getPreset()
                            .getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.INSERT, itemStack);
                    if (inputSlots == null) return;
                    ItemStack rest = blockMenu.pushItem(itemStack, inputSlots);
                    if (rest != null) itemStack.setAmount(rest.getAmount());
                    blockMenu.markDirty();
                }

                @Override
                public boolean contains(@Nonnull ItemRequest[] requests) {
                    BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                    if (blockMenu == null) return false;
                    return ItemUtils.contains(getStorage(), requests);
                }

                @Nonnull
                @Override
                public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
                    BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                    if (blockMenu == null) return new ItemStorage();
                    ItemHashMap<Long> amounts = ItemUtils.getAmounts(requests);
                    ItemStorage found = new ItemStorage();

                    for (Map.Entry<ItemStack, Long> data : amounts.entrySet()) {
                        ItemStack itemStack = data.getKey();
                        int[] outputSlots = blockMenu
                                .getPreset()
                                .getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.WITHDRAW, itemStack);
                        if (outputSlots == null) continue;
                        for (int slot : outputSlots) {
                            ItemStack item = blockMenu.getItemInSlot(slot);
                            if (item == null || item.getType().isAir()) continue;
                            if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                                if (item.getAmount() > data.getValue()) {
                                    found.addItem(new ItemKey(itemStack), data.getValue());
                                    long rest = item.getAmount() - data.getValue();
                                    item.setAmount((int) rest);
                                    break;
                                } else {
                                    found.addItem(new ItemKey(itemStack), item.getAmount());
                                    blockMenu.replaceExistingItem(slot, new ItemStack(Material.AIR));
                                    long rest = data.getValue() - item.getAmount();
                                    if (rest != 0) amounts.put(itemStack, rest);
                                    else break;
                                }
                            }
                        }
                    }
                    blockMenu.markDirty();
                    return found;
                }

                @Override
                @Nonnull
                public ItemHashMap<Long> getStorage() {
                    BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                    if (inv == null) return new ItemHashMap<>();
                    int[] outputSlots =
                            inv.getPreset().getSlotsAccessedByItemTransport(inv, ItemTransportFlow.WITHDRAW, null);
                    if (outputSlots == null) return new ItemHashMap<>();
                    ItemStorage storage = new ItemStorage();
                    for (int slot : outputSlots) {
                        ItemStack itemStack = inv.getItemInSlot(slot);
                        if (itemStack != null && !itemStack.getType().isAir()) storage.addItem(itemStack);
                    }
                    return storage.getStorage();
                }
            };
        } else if (allowVanilla && PaperLib.getBlockState(block, false).getState() instanceof Container container) {
            return new IStorage() {
                @Override
                public void pushItem(@Nonnull ItemStackCache itemStackCache) {
                    ItemStack itemStack = itemStackCache.getItemStack();
                    if (container instanceof Furnace furnace) {
                        FurnaceInventory furnaceInventory = furnace.getInventory();
                        furnaceInventory.addItem(itemStack);
                    } else if (container instanceof Chest chest) {
                        Inventory inventory = chest.getBlockInventory();
                        if (InvUtils.fitAll(
                                inventory,
                                new ItemStack[] {itemStack},
                                IntStream.range(0, inventory.getSize()).toArray())) {
                            inventory.addItem(itemStack);
                        }
                    }
                }

                @Override
                public boolean contains(@Nonnull ItemRequest[] requests) {
                    return ItemUtils.contains(getStorage(), requests);
                }

                @Nonnull
                @Override
                public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
                    ItemStack[] items = getVanillaItemStacks(block);
                    ItemHashMap<Long> amounts = ItemUtils.getAmounts(requests);
                    ItemStorage found = new ItemStorage();

                    for (Map.Entry<ItemStack, Long> data : amounts.entrySet()) {
                        ItemStack itemStack = data.getKey();

                        for (ItemStack item : items) {
                            if (item == null || item.getType().isAir()) continue;
                            if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                                if (item.getAmount() > data.getValue()) {
                                    found.addItem(new ItemKey(itemStack), data.getValue());
                                    long rest = item.getAmount() - data.getValue();
                                    item.setAmount((int) rest);
                                    break;
                                } else {
                                    found.addItem(new ItemKey(itemStack), item.getAmount());
                                    long rest = data.getValue() - item.getAmount();
                                    item.setAmount(0);
                                    if (rest != 0) amounts.put(itemStack, rest);
                                    else break;
                                }
                            }
                        }
                    }
                    return found;
                }

                @Override
                @Nonnull
                public ItemHashMap<Long> getStorage() {
                    Container container =
                            (Container) PaperLib.getBlockState(block, false).getState();
                    ItemStack[] items = new ItemStack[0];
                    if (container instanceof Furnace furnace) {
                        items = new ItemStack[] {furnace.getInventory().getResult()};
                    } else if (container instanceof Chest chest) {
                        items = chest.getInventory().getContents();
                    }
                    return ItemUtils.getAmounts(items);
                }
            };
        }
        return null;
    }

    @Nullable public static ItemStack getItemStack(@Nonnull Block block) {
        return getItemStack(block, true);
    }

    @Nullable public static ItemStack getItemStack(@Nonnull Block block, boolean checkNetwork) {
        return getItemStack(block, checkNetwork, false);
    }

    @Nullable public static ItemStack getItemStack(@Nonnull Block block, boolean checkNetwork, boolean allowVanilla) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
        if (checkNetwork
                && slimefunBlockData != null
                && SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof IMEObject) {
            return null;
        }
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv != null) {
            int[] outputSlots = inv.getPreset().getSlotsAccessedByItemTransport(inv, ItemTransportFlow.WITHDRAW, null);
            if (outputSlots == null) return null;
            for (int slot : outputSlots) {
                ItemStack item = inv.getItemInSlot(slot);
                if (item == null || item.getType().isAir()) continue;
                return item;
            }
        } else if (allowVanilla && PaperLib.getBlockState(block, false).getState() instanceof Container) {
            ItemStack[] items = getVanillaItemStacks(block);
            for (ItemStack itemStack : items) {
                if (itemStack != null && !itemStack.getType().isAir()) return itemStack;
            }
        }

        return null;
    }

    @Nonnull
    private static ItemStack[] getVanillaItemStacks(Block block) {
        Container container = (Container) PaperLib.getBlockState(block, false).getState();
        ItemStack[] items = new ItemStack[0];
        if (container instanceof Furnace furnace) {
            FurnaceInventory furnaceInventory = furnace.getInventory();
            items = new ItemStack[] {furnaceInventory.getResult()};
        } else if (container instanceof Chest chest) {
            Inventory inventory = chest.getBlockInventory();
            items = inventory.getContents();
        }
        return items;
    }

    public static void setSettingItem(@Nonnull Inventory inv, int slot, @Nonnull ItemStack itemStack) {
        ItemStack item = itemStack.clone();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(MenuItems.MENU_ITEM, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    @Nullable public static ItemStack getSettingItem(@Nonnull Inventory inv, int slot) {
        ItemStack itemStack = inv.getItem(slot);
        if (itemStack == null || itemStack.getType().isAir()) return null;
        ItemStack item = itemStack.clone();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(MenuItems.MENU_ITEM);
        item.setItemMeta(meta);
        return item;
    }

    @Nonnull
    public static ChestMenu.MenuClickHandler getSettingSlotClickHandler(@Nonnull Block block) {
        return new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent inventoryClickEvent,
                    Player player,
                    int i,
                    ItemStack cursor,
                    ClickAction clickAction) {
                Inventory inventory = inventoryClickEvent.getClickedInventory();
                ItemStack current = getSettingItem(inventory, i);
                if (current != null && SlimefunUtils.isItemSimilar(current, MenuItems.SETTING, true, false)) {
                    if (cursor != null && !cursor.getType().isAir()) {
                        setSettingItem(inventory, i, cursor);
                    }
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        inventory.setItem(i, MenuItems.SETTING);
                    } else {
                        setSettingItem(inventory, i, cursor);
                    }
                }
                SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
                if (slimefunBlockData == null) return false;
                SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
                if (!(slimefunItem instanceof ISettingSlotHolder)) return false;
                ISettingSlotHolder.cache.remove(block.getLocation());

                return false;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        };
    }

    @Nonnull
    public static ChestMenu.MenuClickHandler getPatternSlotClickHandler() {
        return new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent inventoryClickEvent,
                    Player player,
                    int i,
                    ItemStack cursor,
                    ClickAction clickAction) {
                Inventory inventory = inventoryClickEvent.getClickedInventory();
                ItemStack current = inventory.getItem(i);
                if (current != null && SlimefunUtils.isItemSimilar(current, MenuItems.PATTERN, true, false)) {
                    if (cursor != null
                            && !cursor.getType().isAir()
                            && SlimefunItem.getByItem(cursor) instanceof Pattern) {
                        inventory.setItem(i, cursor);
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(null);
                    }
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(current);
                        inventory.setItem(i, MenuItems.PATTERN);
                    } else if (SlimefunItem.getByItem(cursor) instanceof Pattern) {
                        inventory.setItem(i, cursor);
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(current);
                    }
                }

                return false;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        };
    }

    @Nonnull
    public static ChestMenu.MenuClickHandler getCardSlotClickHandler(@Nonnull Block block) {
        return new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent inventoryClickEvent,
                    Player player,
                    int i,
                    ItemStack cursor,
                    ClickAction clickAction) {
                Inventory inventory = inventoryClickEvent.getClickedInventory();
                ItemStack current = getSettingItem(inventory, i);
                if (current != null && SlimefunUtils.isItemSimilar(current, MenuItems.CARD, true, false)) {
                    if (cursor != null && !cursor.getType().isAir() && SlimefunItem.getByItem(cursor) instanceof Card) {
                        setSettingItem(inventory, i, cursor);
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(null);
                    }
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(current);
                        setSettingItem(inventory, i, MenuItems.CARD);
                    } else if (SlimefunItem.getByItem(cursor) instanceof Card) {
                        setSettingItem(inventory, i, cursor);
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(current);
                    }
                }
                SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
                if (slimefunBlockData == null) return false;
                SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
                if (!(slimefunItem instanceof ICardHolder)) return false;
                ICardHolder.cache.remove(block.getLocation());

                return false;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        };
    }

    @Nonnull
    public static ChestMenu.MenuClickHandler getDistanceSlotClickHandler() {
        return new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent inventoryClickEvent,
                    Player player,
                    int i,
                    ItemStack cursor,
                    ClickAction clickAction) {
                Inventory inventory = inventoryClickEvent.getClickedInventory();
                ItemStack current = inventory.getItem(i);
                if (current != null && SlimefunUtils.isItemSimilar(current, MenuItems.DISTANCE, true, false)) {
                    if (cursor != null && !cursor.getType().isAir()) {
                        inventory.setItem(i, MenuItems.DISTANCE.asQuantity(cursor.getAmount()));
                    }
                } else {
                    inventory.setItem(i, MenuItems.DISTANCE);
                }

                return false;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        };
    }

    public static ItemStack createDisplayItem(@Nonnull ItemStack itemStack, long amount) {
        return createDisplayItem(itemStack, amount, true);
    }

    /**
     * 创建用于显示的物品堆
     *
     * @param itemStack 原始物品堆
     * @param amount    显示数量
     * @param addLore   是否添加描述
     * @return 用于显示的物品堆
     */
    @Nonnull
    public static ItemStack createDisplayItem(@Nonnull ItemStack itemStack, long amount, boolean addLore) {
        return createDisplayItem(itemStack, amount, addLore, false);
    }

    /**
     * 创建用于显示的物品堆
     *
     * @param itemStack     原始物品堆
     * @param amount        显示数量
     * @param addLore       是否添加描述
     * @param addPinnedLore 是否添加置顶提示
     * @return 用于显示的物品堆
     */
    @Nonnull
    public static ItemStack createDisplayItem(
            @Nonnull ItemStack itemStack, long amount, boolean addLore, boolean addPinnedLore) {
        ItemStack result = itemStack.clone();

        result.setAmount((int) Math.min(itemStack.getMaxStackSize(), Math.max(1, amount)));
        if (addLore) {
            List<String> lore = result.getLore();
            if (lore != null) {
                List<String> finalLore = lore;
                NBT.modify(result, x -> {
                    x.getStringList(SOURCE_LORE_KEY).clear();
                    x.getStringList(SOURCE_LORE_KEY).addAll(finalLore);
                });
            }

            if (lore == null) lore = new ArrayList<>();

            lore.add("");
            lore.add("&e物品数量 " + amount);
            if (addPinnedLore) lore.add("&e===已置顶===");
            result.setLore(CMIChatColor.translate(lore));
        }

        NBT.modify(result, x -> {
            x.setBoolean(DISPLAY_ITEM_KEY, true);
        });
        return result;
    }

    @Nonnull
    public static ItemStack getDisplayItem(@Nonnull ItemStack itemStack) {
        ItemStack result = itemStack.asOne();
        List<String> lore = NBT.get(result, x -> {
            if (x.hasTag(SOURCE_LORE_KEY))
                return x.getStringList(SOURCE_LORE_KEY).toListCopy();

            return null;
        });
        result.setLore(lore);

        NBT.modify(result, x -> {
            x.removeKey(DISPLAY_ITEM_KEY);
            x.removeKey(SOURCE_LORE_KEY);
        });
        return result;
    }

    public static <T extends SlimefunItem> T setRecipeOutput(@Nonnull T item, @Nonnull ItemStack output) {
        item.setRecipeOutput(output);
        return item;
    }

    public static String getItemName(ItemStack itemStack) {
        String displayName = itemStack.getItemMeta().getDisplayName();
        if (!displayName.isEmpty()) return displayName;
        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
        if (slimefunItem != null) {
            return slimefunItem.getItemName();
        } else {
            return ItemStackHelper.getName(itemStack);
        }
    }

    public static ItemStack[] takeItems(Inventory inventory, int[] slots, ItemRequest[] requests) {
        ItemStack[] items = Arrays.stream(slots).mapToObj(inventory::getItem).toArray(ItemStack[]::new);
        ItemHashMap<Long> amounts = ItemUtils.getAmounts(requests);
        ItemStorage found = new ItemStorage();

        for (ItemStack itemStack : amounts.keySet()) {
            for (ItemStack item : items) {
                if (item == null || item.getType().isAir()) continue;
                if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                    if (item.getAmount() > amounts.get(itemStack)) {
                        found.addItem(new ItemKey(itemStack), amounts.get(itemStack));
                        long rest = item.getAmount() - amounts.get(itemStack);
                        item.setAmount((int) rest);
                        break;
                    } else {
                        found.addItem(new ItemKey(itemStack), item.getAmount());
                        long rest = amounts.get(itemStack) - item.getAmount();
                        item.setAmount(0);
                        if (rest != 0) amounts.put(itemStack, rest);
                        else break;
                    }
                }
            }
        }
        return found.toItemStacks();
    }

    @Nonnull
    public static ItemStack[] removeAll(@Nonnull ItemStack[] itemStacks, @Nonnull ItemHashSet toRemove) {
        List<ItemStack> result = new ArrayList<>(itemStacks.length);
        for (ItemStack itemStack : itemStacks) {
            if (toRemove.contains(itemStack.asOne())) continue;
            result.add(itemStack);
        }

        return result.toArray(ItemStack[]::new);
    }
}
