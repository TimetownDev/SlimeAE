package me.ddggdd135.slimeae.utils;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SerializeUtils {
    @Nonnull
    public static String object2String(@Nullable Object object) {
        if (object instanceof ItemStack itemStack) {
            String id = getId(itemStack);
            if (id != null) return id;
        }
        var stream = new ByteArrayOutputStream();
        try (var bs = new BukkitObjectOutputStream(stream)) {
            bs.writeObject(object);
            return Base64Coder.encodeLines(stream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Nullable public static Object string2Object(@Nonnull String base64Str) {
        if (base64Str.startsWith("SLIMEFUN_")) {
            String id = base64Str.substring(9);
            SlimefunItem slimefunItem = SlimefunItem.getById(id);
            if (slimefunItem == null) return null;
            return slimefunItem.getItem().clone();
        }
        if (base64Str.startsWith("VANILLA_")) {
            String id = base64Str.substring(8);
            Material material = Material.getMaterial(id);
            if (material == null) return null;

            return new ItemStack(material);
        }
        if (base64Str.isBlank()) {
            return null;
        }

        var stream = new ByteArrayInputStream(Base64Coder.decodeLines(base64Str));
        try (var bs = new BukkitObjectInputStream(stream)) {
            return bs.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long getItemHash(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        return itemStack.getType().hashCode() * 33L + nbtItem.hashCode();
    }

    @Nullable public static String getId(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) return "VANILLA_AIR";
        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
        if (slimefunItem != null && slimefunItem.getItem().asOne().equals(itemStack.asOne()))
            return "SLIMEFUN_" + slimefunItem.getId();

        Material material = itemStack.getType();

        if (new ItemStack(material).equals(itemStack)) return "VANILLA_" + material;

        return null;
    }
}
