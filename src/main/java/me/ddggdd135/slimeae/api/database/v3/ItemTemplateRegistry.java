package me.ddggdd135.slimeae.api.database.v3;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemTemplateRegistry {
    private static final Logger logger = Logger.getLogger("SlimeAE-Templates");
    private final ConcurrentHashMap<String, Long> compositeKeyToTplId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ItemTemplate> tplIdToTemplate = new ConcurrentHashMap<>();
    private final ConnectionManager connMgr;
    private final DDLProvider ddl;

    public record ItemTemplate(long tplId, String itemId, String itemData, long itemDataHash) {}

    public ItemTemplateRegistry(ConnectionManager connMgr, DDLProvider ddl) {
        this.connMgr = connMgr;
        this.ddl = ddl;
    }

    private static String compositeKey(String itemId, long dataHash) {
        return itemId + "|" + dataHash;
    }

    public static long computeItemDataHash(@Nonnull String itemId, @Nullable String itemData) {
        String input = itemId + "|" + (itemData != null ? itemData : "");
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        return murmurHash3_128(bytes);
    }

    private static long murmurHash3_128(byte[] data) {
        int len = data.length;
        long h1 = 0;
        long h2 = 0;
        long c1 = 0x87c37b91114253d5L;
        long c2 = 0x4cf5ad432745937fL;
        int nblocks = len / 16;
        for (int i = 0; i < nblocks; i++) {
            long k1 = getLittleEndianLong(data, i * 16);
            long k2 = getLittleEndianLong(data, i * 16 + 8);
            k1 *= c1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= c2;
            h1 ^= k1;
            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;
            k2 *= c2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= c1;
            h2 ^= k2;
            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }
        int tail = nblocks * 16;
        long k1 = 0;
        long k2 = 0;
        switch (len - tail) {
            case 15:
                k2 ^= (long) (data[tail + 14] & 0xff) << 48;
            case 14:
                k2 ^= (long) (data[tail + 13] & 0xff) << 40;
            case 13:
                k2 ^= (long) (data[tail + 12] & 0xff) << 32;
            case 12:
                k2 ^= (long) (data[tail + 11] & 0xff) << 24;
            case 11:
                k2 ^= (long) (data[tail + 10] & 0xff) << 16;
            case 10:
                k2 ^= (long) (data[tail + 9] & 0xff) << 8;
            case 9:
                k2 ^= (long) (data[tail + 8] & 0xff);
                k2 *= c2;
                k2 = Long.rotateLeft(k2, 33);
                k2 *= c1;
                h2 ^= k2;
            case 8:
                k1 ^= (long) (data[tail + 7] & 0xff) << 56;
            case 7:
                k1 ^= (long) (data[tail + 6] & 0xff) << 48;
            case 6:
                k1 ^= (long) (data[tail + 5] & 0xff) << 40;
            case 5:
                k1 ^= (long) (data[tail + 4] & 0xff) << 32;
            case 4:
                k1 ^= (long) (data[tail + 3] & 0xff) << 24;
            case 3:
                k1 ^= (long) (data[tail + 2] & 0xff) << 16;
            case 2:
                k1 ^= (long) (data[tail + 1] & 0xff) << 8;
            case 1:
                k1 ^= (long) (data[tail] & 0xff);
                k1 *= c1;
                k1 = Long.rotateLeft(k1, 31);
                k1 *= c2;
                h1 ^= k1;
        }
        h1 ^= len;
        h2 ^= len;
        h1 += h2;
        h2 += h1;
        h1 = fmix64(h1);
        h2 = fmix64(h2);
        h1 += h2;
        return h1;
    }

    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }

    private static long getLittleEndianLong(byte[] data, int offset) {
        return ((long) data[offset] & 0xff)
                | (((long) data[offset + 1] & 0xff) << 8)
                | (((long) data[offset + 2] & 0xff) << 16)
                | (((long) data[offset + 3] & 0xff) << 24)
                | (((long) data[offset + 4] & 0xff) << 32)
                | (((long) data[offset + 5] & 0xff) << 40)
                | (((long) data[offset + 6] & 0xff) << 48)
                | (((long) data[offset + 7] & 0xff) << 56);
    }

    public long getOrRegister(@Nonnull ItemStack itemStack) {
        String itemId = SerializeUtils.getId(itemStack);
        String itemData = null;
        if (itemId == null) {
            itemId = "CUSTOM";
            itemData = SerializeUtils.object2String(itemStack);
        }
        long dataHash = computeItemDataHash(itemId, itemData);
        String cacheKey = compositeKey(itemId, dataHash);

        Long cached = compositeKeyToTplId.get(cacheKey);
        if (cached != null) return cached;

        Long dbTplId = queryTplIdFromDb(itemId, dataHash, itemData);
        if (dbTplId != null) {
            compositeKeyToTplId.put(cacheKey, dbTplId);
            return dbTplId;
        }

        long newTplId = insertTemplateIgnoreConflict(itemId, itemData, dataHash);
        if (newTplId > 0) {
            compositeKeyToTplId.put(cacheKey, newTplId);
            tplIdToTemplate.put(newTplId, new ItemTemplate(newTplId, itemId, itemData, dataHash));
            return newTplId;
        }

        dbTplId = queryTplIdFromDb(itemId, dataHash, itemData);
        if (dbTplId != null) {
            compositeKeyToTplId.put(cacheKey, dbTplId);
            return dbTplId;
        }

        throw new IllegalStateException("Cannot register item template: itemId=" + itemId + ", dataHash=" + dataHash);
    }

    private long insertTemplateIgnoreConflict(String itemId, String itemData, long dataHash) {
        String sql = ddl.insertIgnore()
                + " INTO ae_v3_item_templates (item_id, item_data, item_data_hash, crc32, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, itemId);
            ps.setString(2, itemData);
            ps.setLong(3, dataHash);
            ps.setInt(4, CRC32Utils.computeTemplate(itemId, itemData));
            ps.setLong(5, System.currentTimeMillis());
            int affected = ps.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "INSERT template error: " + e.getMessage(), e);
        }
        return -1;
    }

    @Nullable private Long queryTplIdFromDb(String itemId, long dataHash, String expectedItemData) {
        String sql = "SELECT tpl_id, item_data FROM ae_v3_item_templates WHERE item_id = ? AND item_data_hash = ?";
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            ps.setLong(2, dataHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbItemData = rs.getString("item_data");
                    long tplId = rs.getLong("tpl_id");
                    if (Objects.equals(dbItemData, expectedItemData)) {
                        return tplId;
                    }
                    logger.warning("Hash collision detected for itemId=" + itemId);
                    return queryTplIdByFullData(itemId, expectedItemData);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Query template error: " + e.getMessage(), e);
        }
        return null;
    }

    @Nullable private Long queryTplIdByFullData(String itemId, String itemData) {
        String sql = "SELECT tpl_id, item_data FROM ae_v3_item_templates WHERE item_id = ?";
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (Objects.equals(rs.getString("item_data"), itemData)) {
                        return rs.getLong("tpl_id");
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Full data query error: " + e.getMessage(), e);
        }
        return null;
    }

    @Nullable public ItemStack resolveItem(long tplId) {
        ItemTemplate tpl = tplIdToTemplate.get(tplId);
        if (tpl == null) {
            tpl = loadTemplateFromDb(tplId);
            if (tpl == null) return null;
            tplIdToTemplate.put(tplId, tpl);
        }
        return templateToItemStack(tpl);
    }

    @Nullable public static ItemStack templateToItemStack(ItemTemplate tpl) {
        if (tpl.itemId().startsWith("SLIMEFUN_")) {
            String sfId = tpl.itemId().substring("SLIMEFUN_".length());
            SlimefunItem sfItem = SlimefunItem.getById(sfId);
            if (sfItem == null) return null;
            return sfItem.getItem().clone();
        }
        if (tpl.itemId().startsWith("VANILLA_")) {
            String matName = tpl.itemId().substring("VANILLA_".length());
            Material mat = Material.getMaterial(matName);
            if (mat == null) return null;
            return new ItemStack(mat);
        }
        if ("CUSTOM".equals(tpl.itemId()) && tpl.itemData() != null) {
            Object obj = SerializeUtils.string2Object(tpl.itemData());
            return obj instanceof ItemStack is ? is : null;
        }
        return null;
    }

    @Nullable private ItemTemplate loadTemplateFromDb(long tplId) {
        String sql = "SELECT tpl_id, item_id, item_data, item_data_hash FROM ae_v3_item_templates WHERE tpl_id = ?";
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tplId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ItemTemplate(
                            rs.getLong("tpl_id"),
                            rs.getString("item_id"),
                            rs.getString("item_data"),
                            rs.getLong("item_data_hash"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Load template error: " + e.getMessage(), e);
        }
        return null;
    }

    public void preloadAll() {
        String sql = "SELECT tpl_id, item_id, item_data, item_data_hash FROM ae_v3_item_templates";
        try (Connection conn = connMgr.getReadConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                long tplId = rs.getLong("tpl_id");
                String itemId = rs.getString("item_id");
                String itemData = rs.getString("item_data");
                long dataHash = rs.getLong("item_data_hash");
                tplIdToTemplate.put(tplId, new ItemTemplate(tplId, itemId, itemData, dataHash));
                compositeKeyToTplId.put(compositeKey(itemId, dataHash), tplId);
                count++;
            }
            logger.info("Preloaded " + count + " item templates");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Preload templates failed: " + e.getMessage(), e);
        }
    }

    @Nullable public ItemTemplate getTemplate(long tplId) {
        return tplIdToTemplate.get(tplId);
    }
}
