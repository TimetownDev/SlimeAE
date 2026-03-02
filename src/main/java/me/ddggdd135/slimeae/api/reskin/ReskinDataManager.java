package me.ddggdd135.slimeae.api.reskin;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ReskinDataManager {

    private static final NamespacedKey KEY_RESKIN_TYPE = new NamespacedKey("slimeae", "reskin_type");
    private static final NamespacedKey KEY_RESKIN_VALUE = new NamespacedKey("slimeae", "reskin_value");
    private static final String LEGACY_ZW_PREFIX = "\u200B\u200C\u200D" + ChatColor.RESET;

    @SuppressWarnings("deprecation")
    private static final String LEGACY_LORE_PREFIX = "" + ChatColor.COLOR_CHAR + "x" + ChatColor.COLOR_CHAR + "r"
            + ChatColor.COLOR_CHAR + "e" + ChatColor.COLOR_CHAR + "s" + ChatColor.COLOR_CHAR + "k"
            + ChatColor.COLOR_CHAR + "i" + ChatColor.COLOR_CHAR + "n" + ChatColor.COLOR_CHAR + ":";

    private static final String RESKIN_VISIBLE_PREFIX = ChatColor.GRAY + "外观: ";

    private ReskinDataManager() {}

    public static boolean hasReskinData(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(KEY_RESKIN_TYPE, PersistentDataType.STRING)
                && pdc.has(KEY_RESKIN_VALUE, PersistentDataType.STRING)) {
            return true;
        }

        if (meta.hasLore() && meta.getLore() != null) {
            for (String line : meta.getLore()) {
                if (line != null && isLegacyReskinLine(line)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable public static String[] getReskinData(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String type = pdc.get(KEY_RESKIN_TYPE, PersistentDataType.STRING);
        String value = pdc.get(KEY_RESKIN_VALUE, PersistentDataType.STRING);
        if (type != null && value != null) {
            return new String[] {type, value};
        }

        if (!meta.hasLore() || meta.getLore() == null) return null;
        String legacyType = null;
        String legacyValue = null;
        for (String line : meta.getLore()) {
            if (line == null) continue;
            String data = stripLegacyPrefix(line);
            if (data == null) continue;
            int colonIdx = data.indexOf(':');
            if (colonIdx < 0) continue;
            String key = data.substring(0, colonIdx);
            String val = data.substring(colonIdx + 1);
            if ("type".equals(key)) {
                legacyType = val;
            } else if ("value".equals(key)) {
                legacyValue = val;
            }
        }
        if (legacyType != null && legacyValue != null) {
            return new String[] {legacyType, legacyValue};
        }
        return null;
    }

    public static void addReskinData(@Nonnull ItemStack item, @Nonnull String type, @Nonnull String value) {
        removeReskinData(item);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_RESKIN_TYPE, PersistentDataType.STRING, type);
        pdc.set(KEY_RESKIN_VALUE, PersistentDataType.STRING, value);

        List<String> lore =
                meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String displayName = getDisplayName(type, value);
        lore.add(RESKIN_VISIBLE_PREFIX + displayName);

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static void removeReskinData(@Nonnull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.remove(KEY_RESKIN_TYPE);
        pdc.remove(KEY_RESKIN_VALUE);

        if (meta.hasLore() && meta.getLore() != null) {
            List<String> newLore = new ArrayList<>();
            for (String line : meta.getLore()) {
                if (line == null) continue;
                if (isLegacyReskinLine(line)) continue;
                if (line.startsWith(RESKIN_VISIBLE_PREFIX)) continue;
                newLore.add(line);
            }
            meta.setLore(newLore);
        }

        item.setItemMeta(meta);
    }

    private static boolean isLegacyReskinLine(@Nonnull String line) {
        return line.startsWith(LEGACY_ZW_PREFIX) || line.startsWith(LEGACY_LORE_PREFIX);
    }

    @Nullable private static String stripLegacyPrefix(@Nonnull String line) {
        if (line.startsWith(LEGACY_ZW_PREFIX)) {
            return line.substring(LEGACY_ZW_PREFIX.length());
        }
        if (line.startsWith(LEGACY_LORE_PREFIX)) {
            return line.substring(LEGACY_LORE_PREFIX.length());
        }
        return null;
    }

    @Nonnull
    private static String getDisplayName(@Nonnull String type, @Nonnull String value) {
        if ("skull".equals(type)) {
            return "自定义头颅";
        }
        try {
            Material mat = Material.valueOf(value);
            return mat.name().toLowerCase().replace('_', ' ');
        } catch (IllegalArgumentException e) {
            return value;
        }
    }
}
