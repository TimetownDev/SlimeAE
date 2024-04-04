package me.ddggdd135.slimeae.api;

import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

public class ItemRequest {
    private ItemStack template;
    private int amount;

    public ItemRequest(ItemStack template, int amount) {
        this.template = ItemUtils.createTemplateItem(template);
        this.amount = amount;
    }

    public ItemStack getTemplate() {
        return template;
    }

    public void setTemplate(ItemStack template) {
        this.template = ItemUtils.createTemplateItem(template);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
