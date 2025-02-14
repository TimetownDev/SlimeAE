package me.ddggdd135.slimeae.api.interfaces;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;

public interface IBlockDataAdapter<TData extends IBlockData> {
    @Nonnull
    ReadWriteNBT serialize(@Nonnull IBlockData blockData);

    @Nonnull
    TData deserialize(@Nonnull ReadableNBT nbtCompound);
}
