package me.ddggdd135.slimeae.api.blockdata;

import static me.ddggdd135.slimeae.api.blockdata.MEAdvancedBusDataAdapter.DIRECTIONS_KEY;
import static me.ddggdd135.slimeae.api.blockdata.MEExportBusDataAdapter.ITEMS_KEY;

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
import org.bukkit.inventory.ItemStack;

public class MEAdvancedExportBusDataAdapter implements IBlockDataAdapter<MEAdvancedExportBusData> {
    @Override
    @Nonnull
    public ReadWriteNBT serialize(@Nonnull IBlockData blockData) {
        ReadWriteNBT nbt = NBT.createNBTObject();
        if (!(blockData instanceof MEAdvancedExportBusData data)) return nbt;

        ReadWriteNBTList<String> directions = nbt.getStringList(DIRECTIONS_KEY);
        directions.clear();

        for (BlockFace direction : data.getDirections()) {
            directions.add(direction.name());
        }

        nbt.setItemStackArray(ITEMS_KEY, data.getItemStacks());

        return nbt;
    }

    @Override
    @Nonnull
    public MEAdvancedExportBusData deserialize(@Nonnull ReadableNBT nbtCompound) {
        MEAdvancedExportBusData data = new MEAdvancedExportBusData();

        Set<BlockFace> directions = new HashSet<>();
        ReadableNBTList<String> strings = nbtCompound.getStringList(DIRECTIONS_KEY);

        for (String direction : strings) {
            directions.add(BlockFace.valueOf(direction));
        }

        data.setDirections(directions);

        ItemStack[] itemStacks = nbtCompound.getItemStackArray(ITEMS_KEY);
        if (itemStacks != null) {
            data.setItemStacks(itemStacks);
        }

        return data;
    }
}
