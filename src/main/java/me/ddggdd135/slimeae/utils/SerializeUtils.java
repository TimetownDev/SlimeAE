package me.ddggdd135.slimeae.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SerializeUtils {
    public static String object2String(Object object) {
        var stream = new ByteArrayOutputStream();
        try (var bs = new BukkitObjectOutputStream(stream)) {
            bs.writeObject(object);
            return Base64Coder.encodeLines(stream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Object string2Object(String base64Str) {
        if (base64Str == null || base64Str.isEmpty() || base64Str.isBlank()) {
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
}
