package me.ddggdd135.slimeae.api.blockdata;

import java.util.Objects;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import org.bukkit.NamespacedKey;

public class MEChainedBusData extends MEBusData {
    private static final NamespacedKey key = new NamespacedKey(SlimeAEPlugin.getInstance(), "me_chained_bus");
    private int distance = 1;

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MEChainedBusData that = (MEChainedBusData) o;
        return distance == that.distance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), distance);
    }

    @Override
    @Nonnull
    public NamespacedKey getNamespacedKey() {
        return key;
    }
}
