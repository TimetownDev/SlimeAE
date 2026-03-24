package me.ddggdd135.slimeae.api.database.v3;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class CRC32Utils {
    private CRC32Utils() {}

    public static int compute(String... parts) {
        CRC32 crc = new CRC32();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append('|');
            sb.append(parts[i] != null ? parts[i] : "");
        }
        crc.update(sb.toString().getBytes(StandardCharsets.UTF_8));
        return (int) crc.getValue();
    }

    public static int computeCellItem(String cellUuid, long tplId, long amount) {
        return compute(cellUuid, String.valueOf(tplId), String.valueOf(amount));
    }

    public static int computeJournal(String cellUuid, char op, Long tplId, Long newAmount) {
        return compute(
                cellUuid,
                String.valueOf(op),
                tplId != null ? String.valueOf(tplId) : "",
                newAmount != null ? String.valueOf(newAmount) : "");
    }

    public static int computeTemplate(String itemId, String itemData) {
        return compute(itemId, itemData != null ? itemData : "");
    }

    public static int computeReskin(String world, int x, int y, int z, String type, String value) {
        return compute(world, String.valueOf(x), String.valueOf(y), String.valueOf(z), type, value);
    }
}
