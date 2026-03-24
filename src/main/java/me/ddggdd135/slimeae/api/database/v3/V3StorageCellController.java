package me.ddggdd135.slimeae.api.database.v3;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.MEStorageCellStorageData;
import org.bukkit.inventory.ItemStack;

public class V3StorageCellController {
    private static final Logger logger = Logger.getLogger("SlimeAE-V3Storage");
    private final ConnectionManager connMgr;
    private final ItemKeyTplIdBridge bridge;
    private final DirtyTracker dirtyTracker;

    public V3StorageCellController(ConnectionManager connMgr, ItemKeyTplIdBridge bridge, DirtyTracker dirtyTracker) {
        this.connMgr = connMgr;
        this.bridge = bridge;
        this.dirtyTracker = dirtyTracker;
    }

    public MEStorageCellStorageData loadData(@Nonnull ItemStack itemStack) {
        MEStorageCellStorageData data = new MEStorageCellStorageData(itemStack);
        String cellUUID = data.getUuid().toString();

        List<CellItemRow> rows = queryCellItems(cellUUID);
        if (!rows.isEmpty()) {
            List<Long> tplIds = new ArrayList<>();
            for (CellItemRow row : rows) {
                tplIds.add(row.tplId);
            }
            bridge.preload(tplIds);
        }

        ItemHashMap<Long> storages = data.getStorage();
        long stored = 0;
        for (CellItemRow row : rows) {
            ItemKey key = bridge.resolveKey(row.tplId);
            if (key == null) {
                logger.warning("Cannot resolve tpl_id=" + row.tplId + " for cell " + cellUUID);
                continue;
            }
            storages.putKey(key, row.amount);
            stored += row.amount;
        }
        data.setStored(stored);
        return data;
    }

    public void markDirty(@Nonnull MEStorageCellStorageData data, @Nonnull ItemKey key, long finalAmount) {
        long tplId = bridge.getOrResolve(key);
        dirtyTracker.record(data.getUuid(), tplId, finalAmount, finalAmount > 0 ? 'P' : 'R');
    }

    public void markDirtyBatch(
            @Nonnull MEStorageCellStorageData data, @Nonnull List<Map.Entry<ItemKey, Long>> entries) {
        UUID cellUUID = data.getUuid();
        for (Map.Entry<ItemKey, Long> entry : entries) {
            long tplId = bridge.getOrResolve(entry.getKey());
            dirtyTracker.record(cellUUID, tplId, entry.getValue(), entry.getValue() > 0 ? 'P' : 'R');
        }
    }

    public void markDirtyAll(@Nonnull MEStorageCellStorageData data) {
        ItemHashMap<Long> storages = data.getStorage();
        UUID cellUUID = data.getUuid();
        for (Map.Entry<ItemKey, Long> entry : storages.keyEntrySet()) {
            long tplId = bridge.getOrResolve(entry.getKey());
            dirtyTracker.record(cellUUID, tplId, entry.getValue(), 'P');
        }
    }

    public void delete(@Nonnull MEStorageCellStorageData data) {
        dirtyTracker.recordDeleteCell(data.getUuid());
    }

    public void delete(@Nonnull UUID uuid) {
        dirtyTracker.recordDeleteCell(uuid);
    }

    private List<CellItemRow> queryCellItems(String cellUuid) {
        List<CellItemRow> rows = new ArrayList<>();
        String sql = "SELECT tpl_id, amount FROM ae_v3_cell_items WHERE cell_uuid = ?";
        try (Connection conn = connMgr.getReadConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cellUuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new CellItemRow(rs.getLong("tpl_id"), rs.getLong("amount")));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Load cell items error: " + e.getMessage(), e);
        }
        return rows;
    }

    private record CellItemRow(long tplId, long amount) {}
}
