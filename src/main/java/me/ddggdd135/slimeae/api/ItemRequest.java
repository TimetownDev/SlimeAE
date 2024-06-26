package me.ddggdd135.slimeae.api;

import me.ddggdd135.guguslimefunlib.api.ItemTemplate;
import org.bukkit.inventory.ItemStack;

public class ItemRequest {
    private ItemTemplate template;
    private int amount;

    public ItemRequest(ItemStack template, int amount) {
        this.template = new ItemTemplate(template);
        this.amount = amount;
    }

    public ItemStack getTemplate() {
        return template.getHandle();
    }

    public void setTemplate(ItemStack template) {
        this.template = new ItemTemplate(template);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
