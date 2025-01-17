package me.ddggdd135.slimeae.api;

import me.ddggdd135.guguslimefunlib.api.ItemTemplate;
import org.bukkit.inventory.ItemStack;

public class ItemRequest {
    private ItemStack template;
    private int amount;

    public ItemRequest(ItemStack template, int amount) {
        this.template = template.asOne();
        this.amount = amount;
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
