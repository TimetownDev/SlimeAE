package me.ddggdd135.slimeae.utils;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static Map<ItemStack, Integer> takeItems(@Nonnull Map<ItemStack, Integer> source, @Nonnull Map<ItemStack, Integer> toTake) {
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
    public static Map<ItemStack, Integer> addItems(@Nonnull Map<ItemStack, Integer> source, @Nonnull Map<ItemStack, Integer> toAdd) {
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
    public static Map<ItemStack, Integer> toStorage(NBTCompoundList nbt) {
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
        NBTCompoundList list = container.getCompoundList("items");
        for (ItemStack itemStack : storage.keySet()) {
            ReadWriteNBT compound = list.addCompound();
            compound.setItemStack("item", itemStack);
            compound.setInteger("amount", storage.get(itemStack));
        }
        return list;
    }
}
