package me.ddggdd135.slimeae.api;

import org.bukkit.inventory.ItemStack;

public class ResultWithItem<T> {
    protected T result;
    protected ItemStack itemStack;

    public ItemStack getItemStack() {
        return itemStack;
    }

    public T getResult() {
        return result;
    }

    public ResultWithItem(T result, ItemStack itemStack) {
        this.result = result;
        this.itemStack = itemStack;
    }
}
