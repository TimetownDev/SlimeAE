package me.ddggdd135.slimeae.api.database.v3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.GuguSlimefunLib;
import me.ddggdd135.guguslimefunlib.api.ItemHashSet;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.MEStorageCellFilterData;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.inventory.ItemStack;

public class V3FilterController {
    private static final Logger logger = Logger.getLogger("SlimeAE-V3Filter");
    private final ConnectionManager connMgr;
    private final DDLProvider ddl;

    public V3FilterController(ConnectionManager connMgr, DDLProvider ddl) {
        this.connMgr = connMgr;
        this.ddl = ddl;
    }

    public MEStorageCellFilterData loadData(@Nonnull ItemStack itemStack) {
        MEStorageCellFilterData filterData = new MEStorageCellFilterData(itemStack);
        String cellUuid = filterData.getUuid().toString();

        String sql = "SELECT filter_json FROM ae_v3_cell_meta WHERE cell_uuid = ?";
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cellUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("filter_json");
                    if (json != null && !json.isEmpty()) {
                        parseFilterJson(filterData, json);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Load filter data error: " + e.getMessage(), e);
        }

        filterData.updateItemTypes();
        return filterData;
    }

    public void markDirty(@Nonnull MEStorageCellFilterData data) {
        saveMeta(data);
    }

    public void delete(@Nonnull MEStorageCellFilterData data) {
        deleteMeta(data.getUuid().toString());
    }

    public void delete(@Nonnull UUID uuid) {
        deleteMeta(uuid.toString());
    }

    private void saveMeta(@Nonnull MEStorageCellFilterData data) {
        String cellUuid = data.getUuid().toString();
        String filterJson = buildFilterJson(data);
        long now = System.currentTimeMillis();
        String updateSql = ddl.updateFilterJsonOnly();
        try (Connection conn = connMgr.getWriteConnection()) {
            int updated;
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, filterJson);
                ps.setLong(2, now);
                ps.setString(3, cellUuid);
                updated = ps.executeUpdate();
            }

            if (updated == 0) {
                String serverUuid = GuguSlimefunLib.getServerUUID().toString();
                String upsertSql = ddl.upsertCellMeta();
                try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
                    ps.setString(1, cellUuid);
                    ps.setString(2, serverUuid);
                    ps.setLong(3, 0);
                    ps.setLong(4, 0);
                    ps.setString(5, filterJson);
                    ps.setString(6, null);
                    ps.setLong(7, now);
                    ps.setLong(8, now);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Save filter meta error: " + e.getMessage(), e);
        }
    }

    private void deleteMeta(String cellUuid) {
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM ae_v3_cell_meta WHERE cell_uuid = ?")) {
            ps.setString(1, cellUuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Delete filter meta error: " + e.getMessage(), e);
        }
    }

    public void ensureMeta(
            @Nonnull String cellUuid, @Nonnull String serverUuid, long size, @Nonnull MEStorageCellFilterData filter) {
        String filterJson = buildFilterJson(filter);
        long now = System.currentTimeMillis();

        String sql = ddl.upsertCellMeta();
        try (Connection conn = connMgr.getWriteConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cellUuid);
            ps.setString(2, serverUuid);
            ps.setLong(3, size);
            ps.setLong(4, 0);
            ps.setString(5, filterJson);
            ps.setString(6, null);
            ps.setLong(7, now);
            ps.setLong(8, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Ensure meta error: " + e.getMessage(), e);
        }
    }

    private static String buildFilterJson(@Nonnull MEStorageCellFilterData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"filters\":[");
        boolean first = true;
        for (ItemKey key : data.getFilters()) {
            if (!first) sb.append(',');
            first = false;
            String id = SerializeUtils.getId(key.getItemStack());
            if (id == null) id = SerializeUtils.object2String(key.getItemStack());
            sb.append('"').append(escapeJson(id)).append('"');
        }
        sb.append("],\"reversed\":").append(data.isReversed());
        sb.append(",\"fuzzy\":").append(data.isFuzzy());
        sb.append('}');
        return sb.toString();
    }

    private static void parseFilterJson(@Nonnull MEStorageCellFilterData data, @Nonnull String json) {
        try {
            int filtersStart = json.indexOf("[");
            int filtersEnd = json.indexOf("]");
            if (filtersStart < 0 || filtersEnd < 0) return;

            String filtersStr = json.substring(filtersStart + 1, filtersEnd).trim();
            if (!filtersStr.isEmpty()) {
                ItemHashSet filters = data.getFilters();
                String[] parts = splitJsonArray(filtersStr);
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                        trimmed = unescapeJson(trimmed.substring(1, trimmed.length() - 1));
                    }
                    if (trimmed.isEmpty()) continue;
                    ItemStack item = (ItemStack) SerializeUtils.string2Object(trimmed);
                    if (item != null) {
                        filters.add(item);
                    }
                }
            }

            String rest = json.substring(filtersEnd + 1);
            if (rest.contains("\"reversed\":true")) {
                data.setReversed(true);
            }
            if (rest.contains("\"fuzzy\":true")) {
                data.setFuzzy(true);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Parse filter JSON error: " + e.getMessage(), e);
        }
    }

    private static String[] splitJsonArray(String s) {
        java.util.List<String> result = new java.util.ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                i++;
                while (i < s.length() && s.charAt(i) != '"') {
                    if (s.charAt(i) == '\\') i++;
                    i++;
                }
            } else if (c == ',' && depth == 0) {
                result.add(s.substring(start, i));
                start = i + 1;
            }
        }
        if (start < s.length()) {
            result.add(s.substring(start));
        }
        return result.toArray(new String[0]);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescapeJson(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
