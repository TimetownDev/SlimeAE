package me.ddggdd135.slimeae.api.interfaces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public interface ICardHolder {
    public int[] getCardSlots();

    public default void tickCards(Block block, SlimefunItem item, SlimefunBlockData data) {
        BlockMenu menu = data.getBlockMenu();
        if (menu == null) return;
        for (int slot : getCardSlots()) {
            ItemStack itemStack = menu.getItemInSlot(slot);
            SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
            if (slimefunItem instanceof Card card) {
                card.onTick(block, item, data);
            }
        }
    }
}
