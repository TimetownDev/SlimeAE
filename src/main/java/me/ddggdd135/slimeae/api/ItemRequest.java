package me.ddggdd135.slimeae.api;

import org.bukkit.inventory.ItemStack;

public class ItemRequest {
    private ItemStack template;
    private long amount;

    public ItemRequest(ItemStack template, long amount, boolean unsafe) {
        if (unsafe) this.template = template;
        else this.template = template.asOne();
        this.amount = amount;
    }

    public ItemRequest(ItemStack template, long amount) {
        this(template, amount, false);
    }

    public ItemStack getTemplate() {
        return template;
    }

    public void setTemplate(ItemStack template) {
        this.template = template.asOne();
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
