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
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTCompoundList;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTContainer;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.slimefun.MEInterface;
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
    public static final String ITEM_STORAGE = "item_storage";
    public static final String DISPLAY_ITEM = "display_item";
    /**
     * 根据模板物品创建指定数量的物品堆数组
     *
     * @param template 模板物品
     * @param amount 总数量
     * @return 物品堆数组，每个物品堆不超过最大堆叠数
     */
    @Nonnull
    public static ItemStack[] createItems(@Nonnull ItemStack template, int amount) {
        List<ItemStack> itemStacks = new ArrayList<>();
        int rest = amount;
        while (true) {
            if (rest <= template.getMaxStackSize()) {
                ItemStack itemStack = template.asQuantity(rest);
                itemStacks.add(itemStack);
                break;
            } else {
                rest -= template.getMaxStackSize();
                ItemStack itemStack = template.asQuantity(template.getMaxStackSize());
                itemStacks.add(itemStack);
            }
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

    /**
     * 将存储映射转换为物品堆数组
     *
     * @param storage 物品到数量的映射
     * @return 物品堆数组
     */
    @Nonnull
    public static ItemStack[] createItems(@Nonnull Map<ItemStack, Integer> storage) {
        return storage.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .flatMap(e -> Stream.of(createItems(e.getKey(), e.getValue())))
                .toArray(ItemStack[]::new);
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
            itemStacks.addAll(List.of(createItems(request.getTemplate(), request.getAmount())));
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
        List<ItemStack> itemStackList = new ArrayList<>();
        for (ItemStack itemStack : itemStacks) {
            if (itemStack.getAmount() > 0) {
                itemStackList.add(itemStack);
            }
        }
        return itemStackList.toArray(new ItemStack[0]);
    }

    /**
     * 检查存储中是否包含所有请求的物品
     *
     * @param storage 物品存储
     * @param requests 物品请求数组
     * @return 是否包含所有请求的物品
     */
    public static boolean contains(@Nonnull Map<ItemStack, Integer> storage, @Nonnull ItemRequest[] requests) {
        for (ItemRequest request : requests) {
            if (!storage.containsKey(request.getTemplate()) || storage.get(request.getTemplate()) < request.getAmount())
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
    public static boolean contains(@Nonnull Map<ItemStack, Integer> storage, @Nonnull ItemRequest request) {
        return storage.containsKey(request.getTemplate()) && storage.get(request.getTemplate()) >= request.getAmount();
    }

    /**
     * 将物品存储映射转换为物品请求数组
     *
     * @param itemStacks 物品存储映射
     * @return 物品请求数组
     */
    @Nonnull
    public static ItemRequest[] createRequests(@Nonnull Map<ItemStack, Integer> itemStacks) {
        List<ItemRequest> requests = new ArrayList<>();
        for (ItemStack itemStack : itemStacks.keySet()) {
            requests.add(new ItemRequest(itemStack, itemStacks.get(itemStack)));
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
    public static Map<ItemStack, Integer> getAmounts(@Nonnull ItemStack[] itemStacks) {
        Map<ItemStack, Integer> storage = new HashMap<>();
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType().isAir()) continue;
            ItemStack template = itemStack.asOne();
            if (storage.containsKey(template)) {
                storage.put(template, storage.get(template) + itemStack.getAmount());
            } else {
                storage.put(template, itemStack.getAmount());
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
    public static Map<ItemStack, Integer> takeItems(
            @Nonnull Map<ItemStack, Integer> source, @Nonnull Map<ItemStack, Integer> toTake) {
        Map<ItemStack, Integer> storage = new HashMap<>(source);
        for (ItemStack itemStack : toTake.keySet()) {
            if (storage.containsKey(itemStack)) {
                storage.put(itemStack, storage.get(itemStack) - toTake.get(itemStack));
            } else {
                storage.put(itemStack, -toTake.get(itemStack));
            }
        }
        return storage;
    }

    /**
     * 向源存储中添加物品
     *
     * @param source 源存储
     * @param toAdd 要添加的物品及数量
     * @return 更新后的存储映射
     */
    @Nonnull
    public static Map<ItemStack, Integer> addItems(
            @Nonnull Map<ItemStack, Integer> source, @Nonnull Map<ItemStack, Integer> toAdd) {
        Map<ItemStack, Integer> storage = new HashMap<>(source);
        for (ItemStack itemStack : toAdd.keySet()) {
            if (storage.containsKey(itemStack)) {
                storage.put(itemStack, storage.get(itemStack) + toAdd.get(itemStack));
            } else {
                storage.put(itemStack, toAdd.get(itemStack));
            }
        }
        return storage;
    }

    /**
     * 移除存储中数量为0或空的物品
     *
     * @param storage 要清理的存储映射
     */
    public static void trim(@Nonnull Map<ItemStack, Integer> storage) {
        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack itemStack : storage.keySet()) {
            if (itemStack == null || itemStack.getType().isAir() || storage.get(itemStack) <= 0)
                toRemove.add(itemStack);
        }
        for (ItemStack itemStack : toRemove) {
            storage.remove(itemStack);
        }
    }

    public static boolean contains(BlockMenu inv, int[] slots, ItemStack[] itemStacks) {
        Map<ItemStack, Integer> toTake = getAmounts(itemStacks);

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

    @Nonnull
    public static Map<ItemStack, Integer> toStorage(@Nonnull NBTCompoundList nbt) {
        Map<ItemStack, Integer> result = new ItemHashMap<>();
        for (ReadWriteNBT compound : nbt) {
            ItemStack itemStack = compound.getItemStack("item");
            int amount = compound.getInteger("amount");
            result.put(itemStack, amount);
        }
        return result;
    }

    @Nonnull
    public static NBTCompoundList toNBT(@Nonnull Map<ItemStack, Integer> storage) {
        NBTContainer container = new NBTContainer();
        NBTCompoundList list = container.getCompoundList("item_storage");
        for (ItemStack itemStack : storage.keySet()) {
            ReadWriteNBT compound = new NBTContainer();
            compound.setItemStack("item", itemStack);
            compound.setInteger("amount", storage.get(itemStack));
            list.addCompound(compound);
        }
        return list;
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
     * @param block 目标方块
     * @param checkNetwork 是否检查网络
     * @param isReadOnly 是否只读
     * @param allowVanilla 是否允许原版容器
     * @return 存储接口，如果无法获取则返回null
     */
    @Nullable public static IStorage getStorage(
            @Nonnull Block block, boolean checkNetwork, boolean isReadOnly, boolean allowVanilla) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());

        if (slimefunBlockData != null) {
            if (checkNetwork && slimefunItem instanceof IMEObject) {
                if (!(slimefunItem instanceof MEInterface)) return null;
                else isReadOnly = true;
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
                    || SlimeAEPlugin.getNetworksIntegration().isLoaded()) {
                if (SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof NetworkQuantumStorage) {
                    return new QuantumStorage(block);
                }
            }
            if (SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded()) {
                if (SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof NetworksDrawer) {
                    return new DrawerStorage(block);
                }
            }
        }
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (block.getBlockData().getMaterial().isAir()) return null;
        if (inv != null) {
            boolean finalIsReadOnly = isReadOnly;
            return new IStorage() {
                @Override
                public void pushItem(@Nonnull ItemStack[] itemStacks) {
                    if (finalIsReadOnly) return;
                    BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                    if (blockMenu == null) return;
                    for (ItemStack itemStack : itemStacks) {
                        if (itemStack.getType().isAir()) continue;
                        int[] inputSlots = blockMenu
                                .getPreset()
                                .getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.INSERT, itemStack);
                        if (inputSlots == null) continue;
                        ItemStack rest = blockMenu.pushItem(itemStack, inputSlots);
                        if (rest != null) itemStack.setAmount(rest.getAmount());
                    }
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
                public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
                    BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                    if (blockMenu == null) return new ItemStack[0];
                    Map<ItemStack, Integer> amounts = ItemUtils.getAmounts(ItemUtils.createItems(requests));
                    ItemStorage found = new ItemStorage();

                    for (ItemStack itemStack : amounts.keySet()) {
                        int[] outputSlots = blockMenu
                                .getPreset()
                                .getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.WITHDRAW, itemStack);
                        if (outputSlots == null) continue;
                        for (int slot : outputSlots) {
                            ItemStack item = blockMenu.getItemInSlot(slot);
                            if (item == null || item.getType().isAir()) continue;
                            if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                                if (item.getAmount() > amounts.get(itemStack)) {
                                    found.addItem(ItemUtils.createItems(itemStack, amounts.get(itemStack)));
                                    int rest = item.getAmount() - amounts.get(itemStack);
                                    item.setAmount(rest);
                                    break;
                                } else {
                                    found.addItem(ItemUtils.createItems(itemStack, item.getAmount()));
                                    blockMenu.replaceExistingItem(slot, new ItemStack(Material.AIR));
                                    int rest = amounts.get(itemStack) - item.getAmount();
                                    if (rest != 0) amounts.put(itemStack, rest);
                                    else break;
                                }
                            }
                        }
                    }
                    blockMenu.markDirty();
                    return found.toItemStacks();
                }

                @Override
                @Nonnull
                public Map<ItemStack, Integer> getStorage() {
                    BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                    if (inv == null) return new HashMap<>();
                    int[] outputSlots =
                            inv.getPreset().getSlotsAccessedByItemTransport(inv, ItemTransportFlow.WITHDRAW, null);
                    if (outputSlots == null) return new HashMap<>();
                    ItemStorage storage = new ItemStorage();
                    for (int slot : outputSlots) {
                        ItemStack itemStack = inv.getItemInSlot(slot);
                        if (itemStack != null && !itemStack.getType().isAir()) storage.addItem(itemStack);
                    }
                    return storage.getStorage();
                }

                @Override
                public int getEmptySlots() {
                    return 0;
                }
            };
        } else if (allowVanilla && PaperLib.getBlockState(block, false).getState() instanceof Container container) {
            return new IStorage() {
                @Override
                public void pushItem(@Nonnull ItemStack[] itemStacks) {
                    if (container instanceof Furnace furnace) {
                        FurnaceInventory furnaceInventory = furnace.getInventory();
                        furnaceInventory.addItem(itemStacks);
                    } else if (container instanceof Chest chest) {
                        Inventory inventory = chest.getBlockInventory();
                        if (InvUtils.fitAll(
                                inventory,
                                itemStacks,
                                IntStream.range(0, inventory.getSize()).toArray())) {
                            inventory.addItem(itemStacks);
                        }
                    }
                }

                @Override
                public boolean contains(@Nonnull ItemRequest[] requests) {
                    return ItemUtils.contains(getStorage(), requests);
                }

                @Nonnull
                @Override
                public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
                    ItemStack[] items = getVanillaItemStacks(block);
                    Map<ItemStack, Integer> amounts = ItemUtils.getAmounts(ItemUtils.createItems(requests));
                    ItemStorage found = new ItemStorage();

                    for (ItemStack itemStack : amounts.keySet()) {
                        for (ItemStack item : items) {
                            if (item == null || item.getType().isAir()) continue;
                            if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                                if (item.getAmount() > amounts.get(itemStack)) {
                                    found.addItem(ItemUtils.createItems(itemStack, amounts.get(itemStack)));
                                    int rest = item.getAmount() - amounts.get(itemStack);
                                    item.setAmount(rest);
                                    break;
                                } else {
                                    found.addItem(ItemUtils.createItems(itemStack, item.getAmount()));
                                    int rest = amounts.get(itemStack) - item.getAmount();
                                    item.setAmount(0);
                                    if (rest != 0) amounts.put(itemStack, rest);
                                    else break;
                                }
                            }
                        }
                    }
                    return found.toItemStacks();
                }

                @Override
                @Nonnull
                public Map<ItemStack, Integer> getStorage() {
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

                @Override
                public int getEmptySlots() {
                    if (!canHasEmptySlots()) return 0;
                    else {
                        Inventory inventory =
                                ((Chest) PaperLib.getBlockState(block, false).getState()).getBlockInventory();
                        int slots = 0;
                        for (int i = 0; i < 27; i++) {
                            ItemStack itemStack = inventory.getItem(i);
                            if (itemStack == null || itemStack.getType().isAir()) slots++;
                        }
                        return slots;
                    }
                }

                @Override
                public boolean canHasEmptySlots() {
                    return PaperLib.getBlockState(block, false).getState() instanceof Chest;
                }
            };
        }
        return null;
    }

    @Nullable public static ItemStack getItemStack(@Nonnull Block block) {
        return getItemStack(block, true);
    }

    @Nullable public static ItemStack getItemStack(@Nonnull Block block, boolean checkNetwork) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
        if (checkNetwork
                && slimefunBlockData != null
                && SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof IMEObject) {
            return null;
        }
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        // 下面这一行太花费性能
        // if (block.getBlockData().getMaterial().isAir()) return null;
        if (inv != null) {
            int[] outputSlots = inv.getPreset().getSlotsAccessedByItemTransport(inv, ItemTransportFlow.WITHDRAW, null);
            if (outputSlots == null) return null;
            for (int slot : outputSlots) {
                ItemStack item = inv.getItemInSlot(slot);
                if (item == null || item.getType().isAir()) continue;
                return item;
            }
            //        } else if (PaperLib.getBlockState(block, false).getState() instanceof Container) {
            //            ItemStack[] items = getVanillaItemStacks(block);
            //            for (ItemStack itemStack : items) {
            //                if (itemStack != null && !itemStack.getType().isAir()) return itemStack;
            //            }
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
    public static ChestMenu.MenuClickHandler getSettingSlotClickHandler() {
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
                if (current != null && SlimefunUtils.isItemSimilar(current, MenuItems.Setting, true, false)) {
                    if (cursor != null && !cursor.getType().isAir()) {
                        setSettingItem(inventory, i, cursor);
                    }
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        inventory.setItem(i, MenuItems.Setting);
                    } else {
                        setSettingItem(inventory, i, cursor);
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
                if (current != null && SlimefunUtils.isItemSimilar(current, MenuItems.Pattern, true, false)) {
                    if (cursor != null
                            && !cursor.getType().isAir()
                            && SlimefunItem.getByItem(cursor) instanceof Pattern) {
                        inventory.setItem(i, cursor);
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(null);
                    }
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(current);
                        inventory.setItem(i, MenuItems.Pattern);
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
    public static ChestMenu.MenuClickHandler getCardSlotClickHandler() {
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
                if (current != null && SlimefunUtils.isItemSimilar(current, MenuItems.Card, true, false)) {
                    if (cursor != null && !cursor.getType().isAir() && SlimefunItem.getByItem(cursor) instanceof Card) {
                        inventory.setItem(i, cursor);
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(null);
                    }
                } else {
                    if (cursor == null || cursor.getType().isAir()) {
                        inventoryClickEvent.getWhoClicked().setItemOnCursor(current);
                        inventory.setItem(i, MenuItems.Card);
                    } else if (SlimefunItem.getByItem(cursor) instanceof Card) {
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

    public static ItemStack createDisplayItem(@Nonnull ItemStack itemStack, int amount) {
        return createDisplayItem(itemStack, amount, true);
    }
    /**
     * 创建用于显示的物品堆
     *
     * @param itemStack 原始物品堆
     * @param amount 显示数量
     * @param addLore 是否添加描述
     * @return 用于显示的物品堆
     */
    @Nonnull
    public static ItemStack createDisplayItem(@Nonnull ItemStack itemStack, int amount, boolean addLore) {
        ItemStack result = new ItemStack(itemStack.getType());
        ItemMeta meta = itemStack.getItemMeta();
        result.setAmount(Math.min(itemStack.getMaxStackSize(), Math.max(1, amount)));
        if (addLore) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add("");
            lore.add(CMIChatColor.translate("&e物品数量 " + amount));
            meta.setLore(lore);
        }
        result.setItemMeta(meta);
        NBT.modify(result, x -> {
            x.setBoolean(DISPLAY_ITEM, true);
        });
        return result;
    }

    @Nullable public static ItemStack getDisplayItem(@Nonnull ItemStack itemStack, boolean hasLore) {
        ItemStack result = itemStack.asOne();
        ItemMeta meta = result.getItemMeta();
        if (hasLore) {
            List<String> lore = meta.getLore();
            lore.remove(lore.size() - 1);
            lore.remove(lore.size() - 1);
            meta.setLore(lore);
        }
        result.setItemMeta(meta);

        NBT.modify(result, x -> {
            x.removeKey(DISPLAY_ITEM);
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
}
