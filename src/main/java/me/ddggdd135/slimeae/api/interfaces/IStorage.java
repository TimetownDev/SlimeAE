package me.ddggdd135.slimeae.api.interfaces;

import me.ddggdd135.slimeae.api.ItemRequest;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface IStorage {
    void pushItem(ItemStack[] itemStacks);
    boolean contains(ItemRequest[] requests);
    /*
    能拿多少拿多少
     */
    ItemStack[] tryTakeItem(ItemRequest[] requests);
    Map<ItemStack, Integer> getStorage();
    /*
    获取有能存储多少任意物品
     */
    int getEmptySlots();
    /*
    获取是否支持空格运算 如果不 那么代表可以无限存储
     */
    default boolean canHasEmptySlots() {
        return false;
    }
}
