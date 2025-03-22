package me.ddggdd135.slimeae.api.interfaces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public interface ISettingSlotHolder {
    Map<Location, List<Pair<ItemKey, Integer>>> cache = new HashMap<>();

    static void updateCache(Block block, ISettingSlotHolder item, SlimefunBlockData data) {
        BlockMenu menu = data.getBlockMenu();
        if (menu == null) return;
        List<Pair<ItemKey, Integer>> items = new ArrayList<>();
        for (int slot : item.getSettingSlots()) {
            ItemStack itemStack = ItemUtils.getSettingItem(menu.getInventory(), slot);
            if (itemStack == null
                    || itemStack.getType().isAir()
                    || SlimefunUtils.isItemSimilar(itemStack, MenuItems.SETTING, true, false)) {
                items.add(null);
                continue;
            }

            items.add(new Pair<>(new ItemKey(itemStack), itemStack.getAmount()));
        }

        cache.put(block.getLocation(), items);
    }

    int[] getSettingSlots();

    static List<Pair<ItemKey, Integer>> getCache(@Nonnull Location location) {
        return cache.get(location);
    }
}
