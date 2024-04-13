package me.ddggdd135.slimeae.api.interfaces;

import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.ItemRequest;
import org.bukkit.inventory.ItemStack;

public interface IStorage {
    void pushItem(@Nonnull ItemStack[] itemStacks);

    default void pushItem(@Nonnull ItemStack itemStack) {
        pushItem(new ItemStack[] {itemStack});
    }

    boolean contains(@Nonnull ItemRequest[] requests);

    default boolean contains(@Nonnull ItemRequest request) {
        return contains(new ItemRequest[] {request});
    }

    /**
     * 能拿多少拿多少
     */
    @Nonnull
    ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests);

    @Nonnull
    default ItemStack[] tryTakeItem(@Nonnull ItemRequest request) {
        return tryTakeItem(new ItemRequest[] {request});
    }

    @Nonnull
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
