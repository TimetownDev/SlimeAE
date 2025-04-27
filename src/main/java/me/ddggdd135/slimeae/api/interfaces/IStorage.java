package me.ddggdd135.slimeae.api.interfaces;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.slimeae.api.annotation.Unsafe;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import org.bukkit.inventory.ItemStack;

/**
 * 存储接口,定义了物品存储的基本操作
 */
public interface IStorage {

    /**
     * 将物品推送到存储中
     * @param itemStackCache 要存储的物品
     */
    void pushItem(@Nonnull ItemStackCache itemStackCache);

    /**
     *
     * @param itemStacks 要存储的物品
     */
    default void pushItem(@Nonnull ItemStackCache[] itemStacks) {
        for (ItemStackCache itemStack : itemStacks) pushItem(itemStack);
    }

    /**
     * 将物品推送到存储中
     * @param itemStack 要存储的物品
     */
    default void pushItem(@Nonnull ItemStack itemStack) {
        pushItem(new ItemStackCache(itemStack));
    }

    /**
     *
     * @param itemStacks 要存储的物品
     */
    default void pushItem(@Nonnull ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) pushItem(itemStack);
    }

    /**
     * 检查是否包含指定的物品请求
     * @param requests 物品请求数组
     * @return 是否包含所有请求的物品
     */
    boolean contains(@Nonnull ItemRequest[] requests);

    /**
     * 检查是否包含单个物品请求
     * @param request 物品请求
     * @return 是否包含请求的物品
     */
    default boolean contains(@Nonnull ItemRequest request) {
        return contains(new ItemRequest[] {request});
    }

    /**
     * 尝试从存储中提取物品
     * @param requests 物品请求数组
     * @return 成功提取的物品数组
     */
    @Nonnull
    ItemStorage takeItem(@Nonnull ItemRequest[] requests);

    /**
     * 尝试从存储中提取单个物品
     * @param request 物品请求
     * @return 成功提取的物品数组
     */
    @Nonnull
    default ItemStorage takeItem(@Nonnull ItemRequest request) {
        return takeItem(new ItemRequest[] {request});
    }

    /**
     * 获取存储中的所有物品及其数量
     * @return 物品到数量的映射
     */
    @Nonnull
    @Unsafe
    ItemHashMap<Long> getStorageUnsafe();

    @Nonnull
    default ItemHashMap<Long> copyStorage() {
        return new ItemHashMap<>(getStorageUnsafe());
    }

    /**
     * 获取物品的存储等级
     * @param key 要检查的物品
     * @return 物品的存储等级
     */
    default int getTier(@Nonnull ItemKey key) {
        return 0;
    }
}
