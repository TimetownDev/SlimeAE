package me.ddggdd135.slimeae.api.interfaces;

import me.ddggdd135.slimeae.api.ItemRequest;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

public interface IStorage {
    void pushItem(@NonNull ItemStack[] itemStacks);

    default void pushItem(@NonNull ItemStack itemStack) {
        pushItem(new ItemStack[]{itemStack});
    }

    boolean contains(@NonNull ItemRequest[] requests);

    /**
     * 能拿多少拿多少
     */
    @NonNull
    ItemStack[] tryTakeItem(@NonNull ItemRequest[] requests);

    @NonNull
    default ItemStack[] tryTakeItem(@NonNull ItemRequest request) {
        return tryTakeItem(new ItemRequest[]{request});
    }

    @NonNull
    Map<ItemStack, Integer> getStorage();

    /**
     * 获取有能存储多少任意物品
     */
    int getEmptySlots();

    /**
     * 获取是否支持空格运算 如果不 那么代表可以无限存储
     */
    default boolean canHasEmptySlots() {
        return false;
    }
}
