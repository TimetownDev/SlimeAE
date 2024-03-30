package me.ddggdd135.slimeae.utils;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.Zrips.CMILib.Items.CMIItemStack;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class ItemUtils {
    @Nonnull
    public static ItemStack createTemplateItem(ItemStack item) {
        ItemStack itemStack = item.clone();
        itemStack.setAmount(1);
        return itemStack;
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
            itemStacks.addAll(List.of(createItems(request.getItemStack(), request.getAmount())));
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
            ItemStack template = ItemUtils.createTemplateItem(request.getItemStack());
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
        for (ItemStack itemStack : storage.keySet()) {
            if (itemStack == null || itemStack.getType().isAir() || storage.get(itemStack) <= 0)
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
    public static Map<ItemStack, Integer> toStorage(@Nonnull NBTCompound nbt) {
        Map<ItemStack, Integer> result = new HashMap<>();
        for (String key : nbt.getKeys()) {
            ReadWriteNBT compound = nbt.getCompound(key);
            ItemStack itemStack = compound.getItemStack("item");
            int amount = compound.getInteger("amount");
            result.put(itemStack, amount);
        }
        return result;
    }

    @Nonnull
    public static NBTCompound toNBT(@Nonnull Map<ItemStack, Integer> storage) {
        NBTContainer container = new NBTContainer();
        for (ItemStack itemStack : storage.keySet()) {
            ReadWriteNBT compound = container.getOrCreateCompound(String.valueOf(itemStack.hashCode()));
            compound.setItemStack("item", itemStack);
            compound.setInteger("amount", storage.get(itemStack));
        }
        return container;
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
        SlimefunBlockData slimefunBlockData =
                Slimefun.getDatabaseManager().getBlockDataController().getBlockData(block.getLocation());
        if (checkNetwork && slimefunBlockData != null) {
            if (SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof IMEObject) return null;
        }
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (block.getBlockData().getMaterial().isAir()) return null;
        if (inv != null) {
            return new IStorage() {
                @Override
                public void pushItem(@NotNull @NonNull ItemStack[] itemStacks) {}

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
                                    else {
                                        amounts.remove(itemStack);
                                        break;
                                    }
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
                        ItemStack smelting = furnaceInventory.getSmelting();
                        boolean sameType = smelting == null || smelting.isSimilar(itemStacks[0]);
                        if (sameType) {
                            boolean stackable =
                                    smelting.getAmount() + itemStacks[0].getAmount() <= smelting.getMaxStackSize();
                            if (stackable) {
                                furnaceInventory.addItem(itemStacks[0]);
                                container.update();
                            }
                        }
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
                                    else {
                                        amounts.remove(itemStack);
                                        break;
                                    }
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
}
