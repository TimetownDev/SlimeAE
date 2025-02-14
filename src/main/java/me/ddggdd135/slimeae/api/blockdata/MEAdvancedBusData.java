package me.ddggdd135.slimeae.api.blockdata;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;

public class MEAdvancedBusData extends MEBusData implements IBlockData {
    private static final NamespacedKey key = new NamespacedKey(SlimeAEPlugin.getInstance(), "MEAdvancedBus");

    private Set<BlockFace> directions = new HashSet<>(List.of(BlockFace.SELF));

    @Override
    @Nonnull
    public NamespacedKey getNamespacedKey() {
        return key;
    }

    @Nonnull
    public Set<BlockFace> getDirections() {
        return directions;
    }

    public void setDirections(@Nonnull Set<BlockFace> directions) {
        this.directions = directions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MEAdvancedBusData that = (MEAdvancedBusData) o;
        return Objects.equals(directions, that.directions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), directions);
    }
}
