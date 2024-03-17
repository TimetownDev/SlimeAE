package me.ddggdd135.slimeae.api;

import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class ItemRequest {
    private ItemStack itemStack;
    private int amount;

    public ItemRequest(ItemStack itemStack, int amount) {
        this.itemStack = ItemUtils.createTemplateItem(itemStack);
        this.amount = amount;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = ItemUtils.createTemplateItem(itemStack);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
