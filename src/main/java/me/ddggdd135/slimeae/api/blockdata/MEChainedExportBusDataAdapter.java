package me.ddggdd135.slimeae.api.blockdata;

import static me.ddggdd135.slimeae.api.blockdata.MEBusDataAdapter.DIRECTION_KEY;
import static me.ddggdd135.slimeae.api.blockdata.MEChainedBusDataAdapter.DISTANCE_KEY;
import static me.ddggdd135.slimeae.api.blockdata.MEExportBusDataAdapter.ITEMS_KEY;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class MEChainedExportBusDataAdapter implements IBlockDataAdapter<MEChainedExportBusData> {
    @Override
    @Nonnull
    public ReadWriteNBT serialize(@Nonnull IBlockData blockData) {
        ReadWriteNBT nbt = NBT.createNBTObject();
        if (!(blockData instanceof MEChainedExportBusData data)) return nbt;

        nbt.setEnum(DIRECTION_KEY, data.getDirection());
        nbt.setInteger(DISTANCE_KEY, data.getDistance());
        nbt.setItemStackArray(ITEMS_KEY, data.getItemStacks());

        return nbt;
    }

    @Override
    @Nonnull
    public MEChainedExportBusData deserialize(@Nonnull ReadableNBT nbtCompound) {
        MEChainedExportBusData data = new MEChainedExportBusData();

        BlockFace blockFace = nbtCompound.getEnum(DIRECTION_KEY, BlockFace.class);
        if (blockFace != null) {
            data.setDirection(blockFace);
        }
        data.setDistance(nbtCompound.getInteger(DISTANCE_KEY));

        ItemStack[] itemStacks = nbtCompound.getItemStackArray(ITEMS_KEY);
        if (itemStacks != null) {
            data.setItemStacks(itemStacks);
        }

        return data;
    }
}
