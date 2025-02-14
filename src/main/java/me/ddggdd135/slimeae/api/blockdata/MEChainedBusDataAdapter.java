package me.ddggdd135.slimeae.api.blockdata;

import static me.ddggdd135.slimeae.api.blockdata.MEBusDataAdapter.DIRECTION_KEY;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import org.bukkit.block.BlockFace;

public class MEChainedBusDataAdapter implements IBlockDataAdapter<MEChainedBusData> {
    public static final String DISTANCE_KEY = "distance";

    @Override
    @Nonnull
    public ReadWriteNBT serialize(@Nonnull IBlockData blockData) {
        ReadWriteNBT nbt = NBT.createNBTObject();
        if (!(blockData instanceof MEChainedBusData data)) return nbt;

        nbt.setEnum(DIRECTION_KEY, data.getDirection());
        nbt.setInteger(DISTANCE_KEY, data.getDistance());

        return nbt;
    }

    @Override
    @Nonnull
    public MEChainedBusData deserialize(@Nonnull ReadableNBT nbtCompound) {
        MEChainedBusData data = new MEChainedBusData();

        BlockFace blockFace = nbtCompound.getEnum(DIRECTION_KEY, BlockFace.class);
        if (blockFace != null) {
            data.setDirection(blockFace);
        }
        data.setDistance(nbtCompound.getInteger(DISTANCE_KEY));

        return data;
    }
}
