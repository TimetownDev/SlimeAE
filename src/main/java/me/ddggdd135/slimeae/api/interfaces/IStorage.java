package me.ddggdd135.slimeae.api.interfaces;

import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.ItemRequest;
import org.bukkit.inventory.ItemStack;

/**
 * 存储接口,定义了物品存储的基本操作
 */
public interface IStorage {

    /**
     * 将物品推送到存储中
     * @param itemStacks 要存储的物品数组
     */
    void pushItem(@Nonnull ItemStack[] itemStacks);

    // 添加boolean
//    default void pushItem(@Nonnull ItemStack itemStack) {
//        pushItem(new ItemStack[] {itemStack});
//    }

    /**
     * 将单个物品推送到存储中
     *
     * @param itemStack 要存储的物品
     * @return
     */
    default void pushItem(@Nonnull ItemStack itemStack) {
        pushItem(new ItemStack[] {itemStack});
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
    ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests);

    /**
     * 尝试从存储中提取单个物品
     * @param request 物品请求
     * @return 成功提取的物品数组
     */
    @Nonnull
    default ItemStack[] tryTakeItem(@Nonnull ItemRequest request) {
        return tryTakeItem(new ItemRequest[] {request});
    }

    /**
     * 获取存储中的所有物品及其数量
     * @return 物品到数量的映射
     */
    @Nonnull
    Map<ItemStack, Integer> getStorage();

    /**
     * 获取存储中的空槽位数量
     * @return 可用的空槽位数量
     */
    int getEmptySlots();

    /**
     * 检查存储是否支持空槽位计算
     * @return 是否支持空槽位计算
     */
    default boolean canHasEmptySlots() {
        return false;
    }

    /**
     * 获取物品的存储等级
     * @param itemStack 要检查的物品
     * @return 物品的存储等级
     */
    default int getTier(@Nonnull ItemStack itemStack) {
        return 0;
    }
}
