package me.ddggdd135.slimeae.api.database.v3;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import me.ddggdd135.guguslimefunlib.GuguSlimefunLib;

public class ExportImportManager {
    private static final Logger logger = Logger.getLogger("SlimeAE-ExportImport");
    private final ConnectionManager connMgr;
    private final ItemTemplateRegistry registry;
    private final DDLProvider ddl;

    public ExportImportManager(ConnectionManager connMgr, ItemTemplateRegistry registry, DDLProvider ddl) {
        this.connMgr = connMgr;
        this.registry = registry;
        this.ddl = ddl;
    }

    public List<File> exportAll(File exportDir) throws Exception {
        List<String> cellUuids = new ArrayList<>();
        try (Connection conn = connMgr.getReadConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT cell_uuid FROM ae_v3_cell_meta")) {
            while (rs.next()) {
                cellUuids.add(rs.getString("cell_uuid"));
            }
        }
        List<File> files = new ArrayList<>();
        for (String uuid : cellUuids) {
            files.add(exportCell(uuid, exportDir));
        }
        return files;
    }

    public int importAll(File exportDir) throws Exception {
        int total = 0;
        File[] files = exportDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) return 0;
        for (File file : files) {
            total += importCell(file);
        }
        return total;
    }

    public File exportCell(String cellUuid, File exportDir) throws Exception {
        exportDir.mkdirs();
        File file = new File(exportDir, cellUuid + ".json");
        StringBuilder sb = new StringBuilder();
        sb.append("{\"format_version\":3,\"exported_at\":")
                .append(System.currentTimeMillis())
                .append(",\"source_server\":\"")
                .append(GuguSlimefunLib.getServerUUID())
                .append("\",");

        String metaJson = exportCellMeta(cellUuid);
        sb.append("\"cell_meta\":").append(metaJson).append(",");

        List<String> items = exportCellItems(cellUuid);
        sb.append("\"items\":[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(items.get(i));
        }
        sb.append("],");

        String checksum = sha256(sb.toString());
        sb.append("\"checksum\":\"sha256:").append(checksum).append("\"}");

        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(sb.toString());
        }
        return file;
    }

    private String exportCellMeta(String cellUuid) throws SQLException {
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT cell_uuid, `size`, `stored`, filter_json FROM ae_v3_cell_meta WHERE cell_uuid = ?")) {
            ps.setString(1, cellUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "{\"uuid\":\"" + escape(rs.getString("cell_uuid")) + "\",\"size\":"
                            + rs.getLong("size") + ",\"stored\":" + rs.getLong("stored")
                            + ",\"filter_json\":"
                            + (rs.getString("filter_json") != null
                                    ? "\"" + escape(rs.getString("filter_json")) + "\""
                                    : "null")
                            + "}";
                }
            }
        }
        return "{\"uuid\":\"" + escape(cellUuid) + "\",\"size\":0,\"stored\":0,\"filter_json\":null}";
    }

    private List<String> exportCellItems(String cellUuid) throws SQLException {
        List<String> items = new ArrayList<>();
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT ci.tpl_id, ci.amount, it.item_id, it.item_data "
                        + "FROM ae_v3_cell_items ci "
                        + "JOIN ae_v3_item_templates it ON ci.tpl_id = it.tpl_id "
                        + "WHERE ci.cell_uuid = ?")) {
            ps.setString(1, cellUuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StringBuilder item = new StringBuilder();
                    item.append("{\"tpl_id\":")
                            .append(rs.getLong("tpl_id"))
                            .append(",\"item_id\":\"")
                            .append(escape(rs.getString("item_id")))
                            .append("\",\"amount\":")
                            .append(rs.getLong("amount"));
                    String itemData = rs.getString("item_data");
                    if (itemData != null) {
                        item.append(",\"item_data\":\"")
                                .append(escape(itemData))
                                .append("\"");
                    }
                    item.append("}");
                    items.add(item.toString());
                }
            }
        }
        return items;
    }

    public int importCell(File file) throws Exception {
        String content;
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            content = sb.toString();
        }

        int itemsStart = content.indexOf("\"items\":[");
        if (itemsStart < 0) throw new IllegalArgumentException("Invalid export format: no items array");

        String cellUuid = extractJsonString(content, "uuid");
        if (cellUuid == null) throw new IllegalArgumentException("Invalid export format: no cell uuid");

        long size = extractJsonLong(content, "size");
        String serverUuid = GuguSlimefunLib.getServerUUID().toString();
        long now = System.currentTimeMillis();

        int count = 0;
        try (Connection conn = connMgr.getWriteConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(ddl.upsertCellMeta())) {
                    ps.setString(1, cellUuid);
                    ps.setString(2, serverUuid);
                    ps.setLong(3, size);
                    ps.setLong(4, 0);
                    String filterJson = extractJsonString(content, "filter_json");
                    ps.setString(5, filterJson);
                    ps.setString(6, null);
                    ps.setLong(7, now);
                    ps.setLong(8, now);
                    ps.executeUpdate();
                }

                int arrStart = content.indexOf('[', itemsStart);
                int arrEnd = content.lastIndexOf(']');
                if (arrStart >= 0 && arrEnd > arrStart) {
                    String arrContent = content.substring(arrStart + 1, arrEnd);
                    List<String> itemJsons = splitJsonArray(arrContent);
                    long totalStored = 0;
                    try (PreparedStatement ps = conn.prepareStatement(ddl.upsertCellItem())) {
                        for (String itemJson : itemJsons) {
                            String itemId = extractJsonString(itemJson, "item_id");
                            String itemData = extractJsonString(itemJson, "item_data");
                            long amount = extractJsonLong(itemJson, "amount");
                            if (itemId == null || amount <= 0) continue;
                            long dataHash = ItemTemplateRegistry.computeItemDataHash(itemId, itemData);
                            long tplId = ensureTemplate(conn, itemId, itemData, dataHash);
                            ps.setString(1, cellUuid);
                            ps.setLong(2, tplId);
                            ps.setLong(3, amount);
                            ps.setInt(4, CRC32Utils.computeCellItem(cellUuid, tplId, amount));
                            ps.setLong(5, now);
                            ps.addBatch();
                            totalStored += amount;
                            count++;
                        }
                        ps.executeBatch();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE ae_v3_cell_meta SET `stored` = ?, updated_at = ? WHERE cell_uuid = ?")) {
                        ps.setLong(1, totalStored);
                        ps.setLong(2, now);
                        ps.setString(3, cellUuid);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return count;
    }

    private long ensureTemplate(Connection conn, String itemId, String itemData, long dataHash) throws SQLException {
        String selectSql = "SELECT tpl_id FROM ae_v3_item_templates WHERE item_id = ? AND item_data_hash = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, itemId);
            ps.setLong(2, dataHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("tpl_id");
            }
        }
        String insertSql = ddl.insertIgnore()
                + " INTO ae_v3_item_templates (item_id, item_data, item_data_hash, crc32, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, itemId);
            ps.setString(2, itemData);
            ps.setLong(3, dataHash);
            ps.setInt(4, CRC32Utils.computeTemplate(itemId, itemData));
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, itemId);
            ps.setLong(2, dataHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("tpl_id");
            }
        }
        throw new SQLException("Failed to ensure template for itemId=" + itemId);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return "error";
        }
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int start = idx + pattern.length();
        int end = start;
        while (end < json.length()) {
            if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
            end++;
        }
        if (end >= json.length()) return null;
        return json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static long extractJsonLong(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx < 0) return 0;
        int start = idx + pattern.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        if (end == start) return 0;
        try {
            return Long.parseLong(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static List<String> splitJsonArray(String content) {
        List<String> results = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    results.add(content.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return results;
    }
}
