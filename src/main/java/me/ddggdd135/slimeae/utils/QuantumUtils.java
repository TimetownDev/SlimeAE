package me.ddggdd135.slimeae.utils;

import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.utils.Keys;
import io.github.sefiraat.networks.utils.datatypes.DataTypeMethods;
import io.github.sefiraat.networks.utils.datatypes.PersistentQuantumStorageType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class QuantumUtils {

    @Nullable public static QuantumCache getQuantumCache(@Nonnull ItemStack itemStack) {
        final ItemMeta meta = itemStack.getItemMeta();

        QuantumCache cache =
                DataTypeMethods.getCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE);

        if (cache == null) {
            cache = DataTypeMethods.getCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE2, PersistentQuantumStorageType.TYPE);
        }

        if (cache == null) {
            cache = DataTypeMethods.getCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE3, PersistentQuantumStorageType.TYPE);
        }

        return cache;
    }

    public static void setQuantumCache(@Nonnull ItemStack itemStack, QuantumCache quantumCache) {
        final ItemMeta meta = itemStack.getItemMeta();

        DataTypeMethods.setCustom(meta, Keys.QUANTUM_STORAGE_INSTANCE, PersistentQuantumStorageType.TYPE, quantumCache);
        quantumCache.updateMetaLore(meta);

        itemStack.setItemMeta(meta);
    }
}
