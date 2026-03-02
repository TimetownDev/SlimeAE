package me.ddggdd135.slimeae.api.reskin;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.*;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class MaterialValidator {

    private static final Set<Material> TICK_BLACKLIST = new HashSet<>();

    static {
        // 植物/树苗
        addIfExists("OAK_SAPLING", "SPRUCE_SAPLING", "BIRCH_SAPLING", "JUNGLE_SAPLING");
        addIfExists("ACACIA_SAPLING", "DARK_OAK_SAPLING", "CHERRY_SAPLING", "MANGROVE_PROPAGULE");
        addIfExists("BAMBOO_SAPLING", "BAMBOO");

        // 作物
        addIfExists("WHEAT", "CARROTS", "POTATOES", "BEETROOTS", "MELON_STEM", "PUMPKIN_STEM");
        addIfExists("SWEET_BERRY_BUSH", "CAVE_VINES", "CAVE_VINES_PLANT");
        addIfExists("KELP", "KELP_PLANT", "SUGAR_CANE", "CACTUS");
        addIfExists("CHORUS_FLOWER", "CHORUS_PLANT", "COCOA", "NETHER_WART");
        addIfExists("TORCHFLOWER_CROP", "PITCHER_CROP");

        // 红石元件
        addIfExists("REDSTONE_WIRE", "REPEATER", "COMPARATOR", "OBSERVER");
        addIfExists("PISTON", "STICKY_PISTON", "PISTON_HEAD");
        addIfExists("DISPENSER", "DROPPER", "HOPPER", "TNT");
        addIfExists("REDSTONE_LAMP", "DAYLIGHT_DETECTOR");
        addIfExists("SCULK_SENSOR", "CALIBRATED_SCULK_SENSOR", "SCULK_SHRIEKER", "SCULK_CATALYST");
        addIfExists("TARGET");

        // 液体/流体
        addIfExists("WATER", "LAVA");

        // 重力方块
        addIfExists("SAND", "RED_SAND", "GRAVEL");
        addIfExists("ANVIL", "CHIPPED_ANVIL", "DAMAGED_ANVIL");
        addIfExists("DRAGON_EGG", "POINTED_DRIPSTONE");
        addIfExists("SUSPICIOUS_SAND", "SUSPICIOUS_GRAVEL");

        // 物理交互方块
        addIfExists("FIRE", "SOUL_FIRE", "CAMPFIRE", "SOUL_CAMPFIRE");
        addIfExists("FARMLAND", "DIRT_PATH", "SNOW", "ICE", "FROSTED_ICE");
        addIfExists("TURTLE_EGG", "SNIFFER_EGG", "FROGSPAWN");

        // 容器方块
        addIfExists("CHEST", "TRAPPED_CHEST", "ENDER_CHEST", "BARREL", "SHULKER_BOX");
        addIfExists("FURNACE", "BLAST_FURNACE", "SMOKER", "BREWING_STAND");
        addIfExists("ENCHANTING_TABLE", "BEACON", "CONDUIT", "JUKEBOX", "NOTE_BLOCK");
        addIfExists("BEE_NEST", "BEEHIVE", "BELL", "LECTERN", "CHISELED_BOOKSHELF");
        addIfExists("DECORATED_POT", "CRAFTER", "TRIAL_SPAWNER", "VAULT");
        addIfExists("SPAWNER", "COMMAND_BLOCK", "CHAIN_COMMAND_BLOCK", "REPEATING_COMMAND_BLOCK");
        addIfExists("STRUCTURE_BLOCK", "JIGSAW", "END_PORTAL_FRAME", "END_GATEWAY", "NETHER_PORTAL");

        // 不可获取方块
        addIfExists("BEDROCK", "BARRIER", "STRUCTURE_VOID", "LIGHT");
    }

    private static Set<Material> configBlacklist = new HashSet<>();
    private static Set<String> allowedTargetIds = new HashSet<>();
    private static boolean allowSkullTextures = true;

    private MaterialValidator() {}

    private static void addIfExists(String... names) {
        for (String name : names) {
            try {
                TICK_BLACKLIST.add(Material.valueOf(name));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static boolean isValidSkinMaterial(@Nonnull Material material) {
        if (!material.isBlock()) return false;
        if (material.isAir()) return false;
        if (TICK_BLACKLIST.contains(material)) return false;
        if (configBlacklist.contains(material)) return false;
        return true;
    }

    public static boolean isValidSkinItem(@Nonnull ItemStack item) {
        if (item.getType() == Material.PLAYER_HEAD) {
            if (!allowSkullTextures) return false;
            if (item.getItemMeta() instanceof SkullMeta skullMeta) {
                return skullMeta.getOwnerProfile() != null || skullMeta.getOwner() != null;
            }
            return false;
        }
        return isValidSkinMaterial(item.getType());
    }

    public static boolean isValidTarget(@Nonnull String sfId) {
        SlimefunItem sfItem = SlimefunItem.getById(sfId);
        if (sfItem == null) return false;
        if (!(sfItem instanceof IMEObject)) return false;

        if (!allowedTargetIds.isEmpty()) {
            return allowedTargetIds.contains(sfId);
        }

        return true;
    }

    public static void reloadConfig() {
        FileConfiguration config = SlimeAEPlugin.getInstance().getConfig();

        configBlacklist.clear();
        List<String> blacklist = config.getStringList("reskin-machine.material-blacklist");
        for (String name : blacklist) {
            try {
                configBlacklist.add(Material.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                SlimeAEPlugin.getInstance().getLogger().warning("[Reskin] 未知的材质名称: " + name);
            }
        }

        allowedTargetIds.clear();
        List<String> whitelist = config.getStringList("reskin-machine.allowed-targets");
        allowedTargetIds.addAll(whitelist);

        allowSkullTextures = config.getBoolean("reskin-machine.allow-skull-textures", true);
    }

    public static boolean isAllowSkullTextures() {
        return allowSkullTextures;
    }
}
