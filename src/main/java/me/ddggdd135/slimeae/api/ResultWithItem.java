package me.ddggdd135.slimeae.api;

import org.bukkit.inventory.ItemStack;

public class ResultWithItem<T> {
    protected T result;
    protected ItemStack itemStack;

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public ResultWithItem(T result, ItemStack itemStack) {
        this.result = result;
        this.itemStack = itemStack;
    }
}
