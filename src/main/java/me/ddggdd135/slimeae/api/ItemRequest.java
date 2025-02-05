package me.ddggdd135.slimeae.api;

import org.bukkit.inventory.ItemStack;

public class ItemRequest {
    private ItemStack template;
    private int amount;

    public ItemRequest(ItemStack template, int amount, boolean unsafe) {
        if (unsafe) this.template = template;
        else this.template = template.asOne();
        this.amount = amount;
    }

    public ItemRequest(ItemStack template, int amount) {
        this(template, amount, false);
    }

    public ItemStack getTemplate() {
        return template;
    }

    public void setTemplate(ItemStack template) {
        this.template = template.asOne();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
