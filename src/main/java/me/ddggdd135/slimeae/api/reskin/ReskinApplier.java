package me.ddggdd135.slimeae.api.reskin;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class ReskinApplier {

    private ReskinApplier() {}

    /**
     * 将 reskin 应用到世界中的方块
     * @param block 目标方块
     * @param type "material" 或 "skull"
     * @param value Material name 或 base64 hash
     */
    public static void applyReskin(@Nonnull Block block, @Nonnull String type, @Nonnull String value) {
        if ("material".equals(type)) {
            try {
                Material material = Material.valueOf(value);
                block.setType(material, false);
            } catch (IllegalArgumentException ignored) {
            }
        } else if ("skull".equals(type)) {
            block.setType(Material.PLAYER_HEAD, false);
            BlockState state = block.getState();
            if (state instanceof Skull skull) {
                applyBase64ToSkull(skull, value);
            }
        }
    }

    /**
     * 从物品中提取 reskin 信息（材质源提取）
     * @param skinItem 材质源物品
     * @return [type, value] 或 null
     */
    @Nullable public static String[] extractSkinInfo(@Nonnull ItemStack skinItem) {
        if (skinItem.getType() == Material.PLAYER_HEAD) {
            if (skinItem.getItemMeta() instanceof SkullMeta skullMeta) {
                String base64 = extractBase64FromSkull(skullMeta);
                if (base64 != null) {
                    return new String[] {"skull", base64};
                }
            }
            return null;
        }
        if (MaterialValidator.isValidSkinMaterial(skinItem.getType())) {
            return new String[] {"material", skinItem.getType().name()};
        }
        return null;
    }

    /**
     * 从 SkullMeta 中提取 base64 纹理值
     */
    @Nullable private static String extractBase64FromSkull(@Nonnull SkullMeta meta) {
        PlayerProfile profile = meta.getOwnerProfile();
        if (profile == null) return null;

        PlayerTextures textures = profile.getTextures();
        URL skinUrl = textures.getSkin();
        if (skinUrl == null) return null;

        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + skinUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 将 base64 纹理应用到头颅方块
     */
    private static void applyBase64ToSkull(@Nonnull Skull skull, @Nonnull String base64) {
        try {
            String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            // 从 JSON 解析 texture URL
            String url = parseTextureUrl(json);
            if (url == null) return;

            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(url));
            profile.setTextures(textures);
            skull.setOwnerProfile(profile);
            skull.update(true, false);
        } catch (MalformedURLException | IllegalArgumentException ignored) {
        }
    }

    @Nullable private static String parseTextureUrl(@Nonnull String json) {
        String marker = "\"url\":\"";
        int idx = json.indexOf(marker);
        if (idx < 0) return null;
        int start = idx + marker.length();
        int end = json.indexOf('"', start);
        if (end < 0) return null;
        return json.substring(start, end);
    }
}
