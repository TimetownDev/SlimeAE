package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;
import javax.annotation.Nonnull;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class CrystalCertusQuartz implements GEOResource {

    private final NamespacedKey key;
    private final ItemStack item;

    public CrystalCertusQuartz(Plugin plugin, ItemStack item) {
        this.key = new NamespacedKey(plugin, "crystal_certus_quartz");
        this.item = item;
    }

    @Override
    @Nonnull
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    @Nonnull
    public ItemStack getItem() {
        return item.clone();
    }

    @Override
    @Nonnull
    public String getName() {
        return "赛特斯石英水晶";
    }

    @Override
    public boolean isObtainableFromGEOMiner() {
        return true;
    }

    @Override
    public int getDefaultSupply(@Nonnull World.Environment environment, @Nonnull Biome biome) {

        if (environment == World.Environment.NORMAL) {
            return 5;
        } else {
            return 0;
        }
    }

    @Override
    public int getMaxDeviation() {
        return 3;
    }
}
