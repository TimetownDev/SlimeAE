package me.ddggdd135.slimeae.utils;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.core.slimefun.Pattern;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class PatternUtils {

    public static boolean tryAutoFillBlankPattern(@Nonnull BlockMenu menu, int patternSlot, @Nonnull Block block) {
        ItemStack in = menu.getItemInSlot(patternSlot);
        if (in != null && !in.getType().isAir() && ItemUtils.getSlimefunItemFast(in, Pattern.class) != null) {
            return true;
        }
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return false;
        ItemStack[] taken = info.getStorage()
                .takeItem(new ItemRequest(new ItemKey(SlimeAEItems.BLANK_PATTERN), 1))
                .toItemStacks();
        if (taken.length > 0 && taken[0] != null && !taken[0].getType().isAir()) {
            menu.replaceExistingItem(patternSlot, taken[0]);
            return true;
        }
        return false;
    }
}
