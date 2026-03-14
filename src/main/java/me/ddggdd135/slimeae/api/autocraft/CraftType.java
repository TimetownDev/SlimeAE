package me.ddggdd135.slimeae.api.autocraft;

import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum CraftType {
    CRAFTING_TABLE("&e工作台配方", Material.CRAFTING_TABLE, GridSize.SMALL_3x3),
    ENHANCED_CRAFTING_TABLE("&b增强型工作台", Material.CRAFTING_TABLE, GridSize.SMALL_3x3),
    MAGIC_WORKBENCH("&d魔法工作台", Material.BOOKSHELF, GridSize.SMALL_3x3),
    ARMOR_FORGE("&6盔甲锻造台", Material.ANVIL, GridSize.SMALL_3x3),
    SMELTERY("&e冶炼炉", Material.BLAST_FURNACE, GridSize.SMALL_3x3),
    ANCIENT_ALTAR("&c古代祭坛", Material.ENCHANTING_TABLE, GridSize.SMALL_3x3),
    COMPRESSOR("&a压缩机", Material.PISTON, GridSize.SMALL_3x3),
    GRIND_STONE("&7磨石", Material.GRINDSTONE, GridSize.SMALL_3x3),
    JUICER("&5榨汁机", Material.GLASS_BOTTLE, GridSize.SMALL_3x3),
    ORE_CRUSHER("&6矿物粉碎机", Material.IRON_PICKAXE, GridSize.SMALL_3x3),
    PRESSURE_CHAMBER("&f压力机", Material.IRON_BLOCK, GridSize.SMALL_3x3),
    HEATED_PRESSURE_CHAMBER("&c加热压力舱", Material.MAGMA_BLOCK, GridSize.SMALL_3x3),
    CHARGER("&e充能器", Material.LIGHTNING_ROD, GridSize.SMALL_3x3),
    INSCRIBER("&7压印机", Material.IRON_TRAPDOOR, GridSize.SMALL_3x3),
    KITCHEN("&e厨房", Material.CAMPFIRE, GridSize.SMALL_3x3),
    NANOBOT_CRAFTER("&2纳米工作台", Material.SMITHING_TABLE, GridSize.SMALL_3x3),
    NETWORKS_EXPANSION_WORKBENCH("&e网络拓展工作台工作台", Material.CRAFTING_TABLE, GridSize.SMALL_3x3),

    VANILLA_CRAFTING_TABLE("&e原版工作台", Material.CRAFTING_TABLE, GridSize.SMALL_3x3),
    VANILLA_FURNACE("&6原版熔炉", Material.FURNACE, GridSize.PROCESS),
    VANILLA_BLAST_FURNACE("&b原版高炉", Material.BLAST_FURNACE, GridSize.PROCESS),
    VANILLA_SMOKER("&e原版烟熏炉", Material.SMOKER, GridSize.PROCESS),
    VANILLA_STONECUTTER("&7原版切石机", Material.STONECUTTER, GridSize.PROCESS),

    COOKING("&e流程配方", Material.FURNACE, GridSize.PROCESS),

    LARGE("&e大型配方", Material.SMITHING_TABLE, GridSize.LARGE_6x6),
    INFINITY_WORKBENCH("&6无尽工作台", Material.RESPAWN_ANCHOR, GridSize.LARGE_6x6),
    MOB_DATA_INFUSER("&7生物芯片注入器", Material.LODESTONE, GridSize.LARGE_6x6),
    ASSEMBLY_TABLE("&f星系装配台", Material.SMITHING_TABLE, GridSize.LARGE_6x6),
    OBSIDIAN_FORGE("&5黑曜石锻造桌", Material.SMITHING_TABLE, GridSize.LARGE_6x6),
    QUANTUM_WORKBENCH("&b量子工作台", Material.DRIED_KELP_BLOCK, GridSize.SMALL_3x3),
    BUG_CRAFTER(
            "&cBUG工作台",
            PlayerHead.getItemStack(
                    PlayerSkin.fromHashCode("f2fdb5a1477cb38109030fc9e41691668e4fe05c86aad46c6ad01f4ce4dabd52")),
            GridSize.LARGE_6x6),
    MATRIX_CRAFTING_TABLE("&d矩阵合成台", Material.LECTERN, GridSize.LARGE_6x6),
    BEDROCK_CRAFT_TABLE("&c基岩合成台", Material.CRAFTING_TABLE, GridSize.LARGE_6x6);

    private static final Map<String, CraftType> BY_NAME = new HashMap<>();

    static {
        for (CraftType type : values()) {
            BY_NAME.put(type.name(), type);
        }
    }

    private final String displayName;
    private final ItemStack iconItem;
    private final GridSize gridSize;

    CraftType(@Nonnull String displayName, @Nonnull Material icon, @Nonnull GridSize gridSize) {
        this.displayName = displayName;
        this.iconItem = new ItemStack(icon);
        this.gridSize = gridSize;
    }

    CraftType(@Nonnull String displayName, @Nonnull ItemStack iconItem, @Nonnull GridSize gridSize) {
        this.displayName = displayName;
        this.iconItem = iconItem;
        this.gridSize = gridSize;
    }

    @Nonnull
    public String getDisplayName() {
        return displayName;
    }

    @Nonnull
    public Material getIcon() {
        return iconItem.getType();
    }

    @Nonnull
    public ItemStack getIconItem() {
        return iconItem.clone();
    }

    @Nonnull
    public GridSize getGridSize() {
        return gridSize;
    }

    public boolean isSmall() {
        return gridSize == GridSize.SMALL_3x3;
    }

    public boolean isLarge() {
        return gridSize == GridSize.LARGE_6x6;
    }

    public boolean isProcess() {
        return gridSize == GridSize.PROCESS;
    }

    public boolean isVanilla() {
        return this == VANILLA_CRAFTING_TABLE
                || this == VANILLA_FURNACE
                || this == VANILLA_BLAST_FURNACE
                || this == VANILLA_SMOKER
                || this == VANILLA_STONECUTTER;
    }

    @Nullable public static CraftType fromName(@Nonnull String name) {
        return BY_NAME.get(name);
    }

    public enum GridSize {
        SMALL_3x3,
        LARGE_6x6,
        PROCESS
    }
}
