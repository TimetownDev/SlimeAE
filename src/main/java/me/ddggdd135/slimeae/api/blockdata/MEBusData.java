package me.ddggdd135.slimeae.api.blockdata;

import java.util.Objects;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;

public class MEBusData implements IBlockData {
    private static final NamespacedKey key = new NamespacedKey(SlimeAEPlugin.getInstance(), "me_bus");

    private BlockFace direction = BlockFace.SELF;

    @Override
    @Nonnull
    public NamespacedKey getNamespacedKey() {
        return key;
    }

    @Nonnull
    public BlockFace getDirection() {
        return direction;
    }

    public void setDirection(@Nonnull BlockFace direction) {
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MEBusData that = (MEBusData) o;
        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction);
    }
}
