package me.ddggdd135.slimeae.api.interfaces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public interface ICardHolder {
    Map<Location, Map<Card, Integer>> cache = new HashMap<>();

    static void updateCache(Block block, ICardHolder item, SlimefunBlockData data) {
        BlockMenu menu = data.getBlockMenu();
        if (menu == null) return;
        Map<Card, Integer> amount = new HashMap<>();
        for (int slot : item.getCardSlots()) {
            ItemStack itemStack = menu.getItemInSlot(slot);
            SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
            if (slimefunItem instanceof Card card) {
                int a = amount.getOrDefault(card, 0);
                a++;
                amount.put(card, a);
            }
        }
        cache.put(block.getLocation(), amount);
    }

    int[] getCardSlots();

    default void tickCards(Block block, SlimefunItem item, SlimefunBlockData data) {
        BlockMenu menu = data.getBlockMenu();
        if (menu == null) return;
        if (!(item instanceof ICardHolder iCardHolder)) return;

        Map<Card, Integer> amount = cache.get(block.getLocation());
        if (amount == null) {
            updateCache(block, iCardHolder, data);
            return;
        }

        for (Map.Entry<Card, Integer> i : amount.entrySet()) {
            Card card = i.getKey();
            int count = i.getValue();
            for (int j = 0; j < count; j++) {
                card.onTick(block, item, data);
            }
        }
    }

    default void initCardSlots(@Nonnull BlockMenu menu) {
        for (int slot : getCardSlots()) {
            if (menu.getItemInSlot(slot) == null
                    || menu.getItemInSlot(slot).getType().isAir()) {
                menu.replaceExistingItem(slot, MenuItems.CARD);
            }

            // 防止刷物
            ItemStack itemStack = ItemUtils.getSettingItem(menu.getInventory(), slot);
            if (itemStack != null) ItemUtils.setSettingItem(menu.getInventory(), slot, itemStack);

            menu.addMenuClickHandler(slot, ItemUtils.getCardSlotClickHandler(menu.getBlock()));
        }
    }

    default void dropCards(@Nonnull BlockMenu menu) {
        Block b = menu.getBlock();

        for (int slot : getCardSlots()) {
            ItemStack itemStack = ItemUtils.getSettingItem(menu.getInventory(), slot);
            if (itemStack != null
                    && itemStack.getType() != Material.AIR
                    && !(SlimefunUtils.isItemSimilar(itemStack, MenuItems.CARD, true, false))) {
                b.getWorld().dropItemNaturally(b.getLocation(), itemStack);
                ItemUtils.setSettingItem(menu.getInventory(), slot, MenuItems.CARD);
            }
        }
    }
}
