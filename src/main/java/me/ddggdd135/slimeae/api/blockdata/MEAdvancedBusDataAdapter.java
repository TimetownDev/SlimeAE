package me.ddggdd135.slimeae.api.blockdata;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBTList;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBTList;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import org.bukkit.block.BlockFace;

public class MEAdvancedBusDataAdapter implements IBlockDataAdapter<MEAdvancedBusData> {
    public static final String DIRECTIONS_KEY = "directions";

    @Override
    @Nonnull
    public ReadWriteNBT serialize(@Nonnull IBlockData blockData) {
        ReadWriteNBT nbt = NBT.createNBTObject();
        if (!(blockData instanceof MEAdvancedBusData data)) return nbt;

        ReadWriteNBTList<String> directions = nbt.getStringList(DIRECTIONS_KEY);
        directions.clear();

        for (BlockFace direction : data.getDirections()) {
            directions.add(direction.name());
        }

        return nbt;
    }

    @Override
    @Nonnull
    public MEAdvancedBusData deserialize(@Nonnull ReadableNBT nbtCompound) {
        MEAdvancedBusData data = new MEAdvancedBusData();

        Set<BlockFace> directions = new HashSet<>();
        ReadableNBTList<String> strings = nbtCompound.getStringList(DIRECTIONS_KEY);

        for (String direction : strings) {
            directions.add(BlockFace.valueOf(direction));
        }

        data.setDirections(directions);

        return data;
    }
}
