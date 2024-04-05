package me.ddggdd135.slimeae.utils;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.DataUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Items.CMIItemStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class ItemUtils {
    public static final NamespacedKey ITEM_STORAGE_KEY = new NamespacedKey(SlimeAEPlugin.getInstance(), "item_storage");

    @Nonnull
    public static ItemStack createTemplateItem(ItemStack item) {
        ItemStack template = item.clone();
        template.setAmount(1);
        return template;
    }

    @Nonnull
    public static ItemStack[] createItems(@Nonnull ItemStack template, int amount) {
        List<ItemStack> itemStacks = new ArrayList<>();
        int rest = amount;
        while (true) {
            if (rest <= template.getMaxStackSize()) {
                ItemStack itemStack = template.clone();
                itemStack.setAmount(rest);
                itemStacks.add(itemStack);
                break;
            } else {
                rest -= template.getMaxStackSize();
                ItemStack itemStack = template.clone();
                itemStack.setAmount(template.getMaxStackSize());
                itemStacks.add(itemStack);
            }
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

    @Nonnull
    public static ItemStack[] createItems(@Nonnull Map<ItemStack, Integer> storage) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemStack itemStack : storage.keySet()) {
            itemStacks.addAll(List.of(createItems(itemStack, storage.get(itemStack))));
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

    @Nonnull
    public static ItemStack[] createItems(@Nonnull ItemRequest[] requests) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemRequest request : requests) {
            itemStacks.addAll(List.of(createItems(request.getTemplate(), request.getAmount())));
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

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

    public static boolean contains(@Nonnull Map<ItemStack, Integer> storage, @Nonnull ItemRequest[] requests) {
        for (ItemRequest request : requests) {
            ItemStack template = ItemUtils.createTemplateItem(request.getTemplate());
            if (!storage.containsKey(template) || storage.get(template) < request.getAmount()) return false;
        }
        return true;
    }

    @Nonnull
    public static ItemRequest[] createRequests(@Nonnull Map<ItemStack, Integer> itemStacks) {
        List<ItemRequest> requests = new ArrayList<>();
        for (ItemStack itemStack : itemStacks.keySet()) {
            requests.add(new ItemRequest(itemStack, itemStacks.get(itemStack)));
        }
        return requests.toArray(new ItemRequest[0]);
    }

    @Nonnull
    public static Map<ItemStack, Integer> getAmounts(@Nonnull ItemStack[] itemStacks) {
        Map<ItemStack, Integer> storage = new HashMap<>();
        for (ItemStack itemStack : itemStacks) {
            ItemStack template = ItemUtils.createTemplateItem(itemStack);
            if (storage.containsKey(template)) {
                storage.put(template, storage.get(template) + itemStack.getAmount());
            } else {
                storage.put(template, itemStack.getAmount());
            }
        }
        return storage;
    }

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

    public static int getItemAmount(BlockMenu inv, int[] slots, ItemStack itemStack) {
        int founded = 0;
        for (int slot : slots) {
            ItemStack item = inv.getItemInSlot(slot);
            if (item == null || item.getType().isAir()) continue;
            if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                founded += item.getAmount();
            }
        }
        return founded;
    }

    @Nonnull
    public static Map<ItemStack, Integer> toStorage(@Nonnull NBTCompoundList nbt) {
        Map<ItemStack, Integer> result = new HashMap<>();
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

    public static String getName(@Nonnull ItemStack itemStack) {
        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
        if (slimefunItem == null) return new CMIItemStack(itemStack).getRealName();
        return slimefunItem.getItemName();
    }

    @Nullable public static IStorage getStorage(@Nonnull Block block) {
        return getStorage(block, true);
    }

    @Nullable public static IStorage getStorage(@Nonnull Block block, boolean checkNetwork) {
        return getStorage(block, checkNetwork, true);
    }

    @Nullable public static IStorage getStorage(@Nonnull Block block, boolean checkNetwork, boolean isReadOnly) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
        if (checkNetwork
                && slimefunBlockData != null
                && SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof IMEObject) {
            return null;
        }
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (block.getBlockData().getMaterial().isAir()) return null;
        if (inv != null) {
            return new IStorage() {
                @Override
                public void pushItem(@NonNull ItemStack[] itemStacks) {
                    if (isReadOnly) return;
                    BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                    if (inv == null) return;
                    for (ItemStack itemStack : itemStacks) {
                        if (itemStack.getType().isAir()) continue;
                        int[] inputSlots = inv.getPreset()
                                .getSlotsAccessedByItemTransport(inv, ItemTransportFlow.INSERT, itemStack);
                        if (inputSlots == null) continue;
                        ItemStack rest = inv.pushItem(itemStack, inputSlots);
                        if (rest != null) itemStack.setAmount(rest.getAmount());
                    }
                }

                @Override
                public boolean contains(ItemRequest[] requests) {
                    BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                    if (inv == null) return false;
                    return ItemUtils.contains(getStorage(), requests);
                }

                @Override
                public ItemStack[] tryTakeItem(ItemRequest[] requests) {
                    BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                    if (inv == null) return new ItemStack[0];
                    Map<ItemStack, Integer> amounts = ItemUtils.getAmounts(ItemUtils.createItems(requests));
                    ItemStorage found = new ItemStorage();

                    for (ItemStack itemStack : amounts.keySet()) {
                        int[] outputSlots = inv.getPreset()
                                .getSlotsAccessedByItemTransport(inv, ItemTransportFlow.WITHDRAW, itemStack);
                        if (outputSlots == null) continue;
                        for (int slot : outputSlots) {
                            ItemStack item = inv.getItemInSlot(slot);
                            if (item == null || item.getType().isAir()) continue;
                            if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                                if (item.getAmount() > amounts.get(itemStack)) {
                                    found.addItem(ItemUtils.createItems(itemStack, amounts.get(itemStack)));
                                    int rest = item.getAmount() - amounts.get(itemStack);
                                    item.setAmount(rest);
                                    break;
                                } else {
                                    found.addItem(ItemUtils.createItems(itemStack, item.getAmount()));
                                    inv.replaceExistingItem(slot, new ItemStack(Material.AIR));
                                    int rest = amounts.get(itemStack) - item.getAmount();
                                    if (rest != 0) amounts.put(itemStack, rest);
                                    else break;
                                }
                            }
                        }
                    }
                    return found.toItemStacks();
                }

                @Override
                public @NotNull Map<ItemStack, Integer> getStorage() {
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
        } else if (block.getState() instanceof Container) {
            return new IStorage() {
                @Override
                public void pushItem(@NotNull @NonNull ItemStack[] itemStacks) {
                    Container container = (Container) block.getState();
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
                public boolean contains(ItemRequest[] requests) {
                    return ItemUtils.contains(getStorage(), requests);
                }

                @Override
                public ItemStack[] tryTakeItem(ItemRequest[] requests) {
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
                public @NotNull Map<ItemStack, Integer> getStorage() {
                    Container container = (Container) block.getState();
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
                        Inventory inventory = ((Chest) block.getState()).getBlockInventory();
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
                    return block.getState() instanceof Chest;
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
        if (block.getBlockData().getMaterial().isAir()) return null;
        if (inv != null) {
            int[] outputSlots = inv.getPreset().getSlotsAccessedByItemTransport(inv, ItemTransportFlow.WITHDRAW, null);
            if (outputSlots == null) return null;
            for (int slot : outputSlots) {
                ItemStack item = inv.getItemInSlot(slot);
                if (item == null || item.getType().isAir()) continue;
                return item;
            }
        } else if (block.getState() instanceof Container) {
            ItemStack[] items = getVanillaItemStacks(block);
            for (ItemStack itemStack : items) {
                if (itemStack != null && !itemStack.getType().isAir()) return itemStack;
            }
        }
        return null;
    }

    @NotNull private static ItemStack[] getVanillaItemStacks(Block block) {
        Container container = (Container) block.getState();
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
        item.getItemMeta().getPersistentDataContainer().set(MenuItems.MENU_ITEM, PersistentDataType.BOOLEAN, true);
        inv.setItem(slot, item);
    }

    @Nullable public static ItemStack getSettingItem(@Nonnull Inventory inv, int slot) {
        ItemStack itemStack = inv.getItem(slot);
        if (itemStack == null || itemStack.getType().isAir()) return null;
        ItemStack item = itemStack.clone();
        item.getItemMeta().getPersistentDataContainer().remove(MenuItems.MENU_ITEM);
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
                    ItemStack itemStack,
                    ClickAction clickAction) {
                Inventory inventory = inventoryClickEvent.getClickedInventory();
                ItemStack current = getSettingItem(inventory, i);
                if (current != null && SlimefunUtils.isItemSimilar(current, MenuItems.Setting, false, false)) {
                    if (itemStack != null && !itemStack.getType().isAir()) {
                        setSettingItem(inventory, i, itemStack);
                    }
                } else {
                    if (itemStack == null || itemStack.getType().isAir()) {
                        inventory.setItem(i, MenuItems.Setting);
                    } else {
                        setSettingItem(inventory, i, itemStack);
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
    public static ItemStack createDisplayItem(@Nonnull ItemStack itemStack, int amount) {
        ItemStack result = itemStack.clone();
        result.getItemMeta()
                .getPersistentDataContainer()
                .set(
                        ITEM_STORAGE_KEY,
                        PersistentDataType.STRING,
                        DataUtils.itemStack2String(createTemplateItem(itemStack)));
        result.setAmount(Math.min(itemStack.getMaxStackSize(), amount));
        List<Component> lore = result.getItemMeta().lore();
        lore.add(Component.text(""));
        lore.add(Component.text(CMIChatColor.translate("{#Bright_Sun>}物品数量 " + amount + "{#Carrot_Orange<}")));
        result.getItemMeta().lore(lore);
        return result;
    }

    @Nullable public static ItemStack getDisplayItem(@Nonnull ItemStack itemStack) {
        PersistentDataContainer dataContainer = itemStack.getItemMeta().getPersistentDataContainer();
        if (!dataContainer.has(ITEM_STORAGE_KEY, PersistentDataType.STRING)) return null;
        String string =
                itemStack.getItemMeta().getPersistentDataContainer().get(ITEM_STORAGE_KEY, PersistentDataType.STRING);
        return DataUtils.string2ItemStack(string);
    }
}
