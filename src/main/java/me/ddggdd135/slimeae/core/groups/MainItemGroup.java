package me.ddggdd135.slimeae.core.groups;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class MainItemGroup extends FlexItemGroup {
    public final List<DummyItemGroup> itemGroups = new ArrayList<>();
    private static final int[] HEADER = new int[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8
    };
    private static final int[] FOOTER = new int[]{
            45, 46, 47, 48, 49, 50, 51, 52, 53
    };
    private static final int GUIDE_BACK = 1;

    public MainItemGroup(NamespacedKey key, ItemStack item) {
        super(key, item);
    }

    @Override
    public boolean isVisible(Player player, PlayerProfile playerProfile, SlimefunGuideMode slimefunGuideMode) {
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void open(Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        final ChestMenu chestMenu = new ChestMenu("&a能源与运用");

        for (int slot : HEADER) {
            chestMenu.addItem(slot, ChestMenuUtils.getBackground(), (player1, i1, itemStack, clickAction) -> false);
        }

        for (int slot : FOOTER) {
            chestMenu.addItem(slot, ChestMenuUtils.getBackground(), (player1, i1, itemStack, clickAction) -> false);
        }

        chestMenu.setEmptySlotsClickable(false);
        setupPage(p, profile, mode, chestMenu);
        chestMenu.open(p);
    }

    @ParametersAreNonnullByDefault
    private void setupPage(Player player, PlayerProfile profile, SlimefunGuideMode mode, ChestMenu menu) {
        for (int slot : FOOTER) {
            menu.replaceExistingItem(slot, ChestMenuUtils.getBackground());
            menu.addMenuClickHandler(slot, ((player1, i, itemStack, clickAction) -> false));
        }

        // Sound
        menu.addMenuOpeningHandler((p) -> p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F));

        // Back
        menu.replaceExistingItem(
                GUIDE_BACK,
                ChestMenuUtils.getBackButton(
                        player,
                        "",
                        ChatColor.GRAY + Slimefun.getLocalization().getMessage(player, "guide.back.guide")
                )
        );
        menu.addMenuClickHandler(GUIDE_BACK, (player1, slot, itemStack, clickAction) -> {
            SlimefunGuide.openMainMenu(profile, mode, 1);
            return false;
        });
        int nextSlot = 9;
        for (ItemGroup itemGroup : itemGroups) {
            createCursor(menu, itemGroup, player, profile, mode, nextSlot);
            nextSlot++;
        }
    }

    @ParametersAreNonnullByDefault
    public boolean openPage(PlayerProfile profile, ItemGroup itemGroup, SlimefunGuideMode mode, int page) {
        profile.getGuideHistory().add(this, 1);
        SlimefunGuide.openItemGroup(profile, itemGroup, mode, page);
        return false;
    }

    public void addItemGroup(DummyItemGroup itemGroup) {
        itemGroups.add(itemGroup);
    }

    public void createCursor(ChestMenu menu, ItemGroup itemGroup, Player player, PlayerProfile profile, SlimefunGuideMode mode, int slot) {
        menu.replaceExistingItem(slot, itemGroup.getItem(player));
        menu.addMenuClickHandler(slot, (player1, i1, itemStack1, clickAction) ->
                openPage(profile, itemGroup, mode, 1)
        );
    }
}
