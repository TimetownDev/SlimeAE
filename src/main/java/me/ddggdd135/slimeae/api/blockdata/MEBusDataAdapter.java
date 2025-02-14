package me.ddggdd135.slimeae.api.blockdata;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import org.bukkit.block.BlockFace;

public class MEBusDataAdapter implements IBlockDataAdapter<MEBusData> {
    public static final String DIRECTION_KEY = "direction";

    @Override
    @Nonnull
    public ReadWriteNBT serialize(@Nonnull IBlockData blockData) {
        ReadWriteNBT nbt = NBT.createNBTObject();
        if (!(blockData instanceof MEBusData data)) return nbt;

        nbt.setEnum(DIRECTION_KEY, data.getDirection());

        return nbt;
    }

    @Override
    @Nonnull
    public MEBusData deserialize(@Nonnull ReadableNBT nbtCompound) {
        MEBusData data = new MEBusData();

        BlockFace blockFace = nbtCompound.getEnum(DIRECTION_KEY, BlockFace.class);
        if (blockFace != null) {
            data.setDirection(blockFace);
        }

        return data;
    }
}
