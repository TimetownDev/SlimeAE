package me.ddggdd135.slimeae.core.slimefun.terminals;

import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftTypeRegistry;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class CraftTypeSelector {
    private static final int[] ITEM_SLOTS = {
        0, 1, 2, 3, 4, 5, 6, 7, 8,
        9, 10, 11, 12, 13, 14, 15, 16, 17,
        18, 19, 20, 21, 22, 23, 24, 25, 26,
        27, 28, 29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42, 43, 44
    };
    private static final int PREV_SLOT = 48;
    private static final int BACK_SLOT = 49;
    private static final int NEXT_SLOT = 50;
    private static final int[] BORDER_SLOTS = {45, 46, 47, 51, 52, 53};
    private static final int SIZE = 54;

    private CraftTypeSelector() {}

    public static void open(@Nonnull Player player, @Nonnull Consumer<CraftType> callback) {
        open(player, 0, callback);
    }

    private static void open(@Nonnull Player player, int page, @Nonnull Consumer<CraftType> callback) {
        List<CraftType> types = collectTypes();
        int maxPage = Math.max(0, (types.size() - 1) / ITEM_SLOTS.length);
        int safePage = Math.min(Math.max(page, 0), maxPage);

        ChestMenu menu = new ChestMenu("选择配方类型");
        menu.setSize(SIZE);

        for (int slot : BORDER_SLOTS) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        menu.addItem(BACK_SLOT, new AdvancedCustomItemStack(org.bukkit.Material.BARRIER, "&c&l返回"), (p, s, i, a) -> {
            p.closeInventory();
            return false;
        });

        if (safePage > 0) {
            menu.addItem(
                    PREV_SLOT,
                    new AdvancedCustomItemStack(org.bukkit.Material.RED_STAINED_GLASS_PANE, "&c上一页"),
                    (p, s, i, a) -> {
                        open(p, safePage - 1, callback);
                        return false;
                    });
        } else {
            menu.addItem(PREV_SLOT, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        if (safePage < maxPage) {
            menu.addItem(
                    NEXT_SLOT,
                    new AdvancedCustomItemStack(org.bukkit.Material.LIME_STAINED_GLASS_PANE, "&a下一页"),
                    (p, s, i, a) -> {
                        open(p, safePage + 1, callback);
                        return false;
                    });
        } else {
            menu.addItem(NEXT_SLOT, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        int start = safePage * ITEM_SLOTS.length;
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            int slot = ITEM_SLOTS[i];
            int index = start + i;
            if (index < types.size()) {
                CraftType type = types.get(index);
                ItemStack icon = createTypeIcon(type);
                menu.addItem(slot, icon, (p, s, item, a) -> {
                    p.closeInventory();
                    callback.accept(type);
                    return false;
                });
            } else {
                menu.addItem(slot, null, ChestMenuUtils.getEmptyClickHandler());
            }
        }

        menu.open(player);
    }

    @Nonnull
    public static ItemStack createTypeIcon(@Nonnull CraftType type) {
        String modeLore;
        switch (type.getGridSize()) {
            case SMALL_3x3 -> modeLore = "&7模式: &a3×3 网格";
            case LARGE_6x6 -> modeLore = "&7模式: &66×6 大型";
            case PROCESS -> modeLore = "&7模式: &e流程配方";
            default -> modeLore = "&7模式: 未知";
        }
        return new AdvancedCustomItemStack(type.getIconItem(), type.getDisplayName(), "", modeLore, "&7> 单击选择");
    }

    @Nonnull
    private static List<CraftType> collectTypes() {
        List<CraftType> types = new ArrayList<>();
        for (CraftType type : CraftTypeRegistry.getAllRegistered()) {
            if (type == CraftType.CRAFTING_TABLE || type == CraftType.LARGE) {
                continue;
            }
            types.add(type);
        }
        if (!types.contains(CraftType.COOKING)) {
            types.add(CraftType.COOKING);
        }
        return types;
    }
}
