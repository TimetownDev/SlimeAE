package me.ddggdd135.slimeae.api.interfaces;

import com.balugaq.jeg.utils.GuideUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.listeners.JEGCompatibleListener;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Range;

public interface IItemFilterFindableWithGuide {
    default void addJEGFindingButton(@Nonnull BlockMenu blockMenu, @Range(from = 0, to = 53) int slot) {
        if (SlimeAEPlugin.getJustEnoughGuideIntegration().isLoaded()) {
            blockMenu.replaceExistingItem(slot, MenuItems.JEG_FINDING_BUTTON);
            blockMenu.addMenuClickHandler(slot, (player, slot1, item, action) -> {
                openGuide_1(blockMenu, player);
                return false;
            });
        }
    }

    default void openGuide_1(@Nonnull BlockMenu blockMenu, @Nonnull Player player) {
        NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(blockMenu.getLocation());

        if (networkInfo == null) {
            return;
        }

        GuideUtil.openMainMenuAsync(player, SlimefunGuideMode.SURVIVAL_MODE, 1);
        JEGCompatibleListener.addCallback(player.getUniqueId(), ((event, profile) -> {
            BlockMenu actualMenu = StorageCacheUtils.getMenu(blockMenu.getLocation());
            if (actualMenu == null) {
                return;
            }

            if (!actualMenu.getPreset().getID().equals(blockMenu.getPreset().getID())) {
                return;
            }

            setFilter(blockMenu.getBlock(), CMIChatColor.stripColor(ItemUtils.getItemName(event.getClickedItem())));
            event.setCancelled(true);

            player.updateInventory();
            actualMenu.open(player);
        }));
        JEGCompatibleListener.tagGuideOpen(player);
    }

    void setFilter(@Nonnull Block block, @Nonnull String filter);
}
