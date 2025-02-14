package me.ddggdd135.slimeae.api.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Location;

public interface IDataBlock {
    @Nullable IBlockData getData(@Nonnull Location location);

    void applyData(@Nonnull Location location, @Nullable IBlockData data);

    boolean hasData(@Nonnull Location location);

    boolean canApplyData(@Nonnull Location location, @Nullable IBlockData blockData);

    @Nonnull
    IBlockDataAdapter<?> getAdapter();
}
