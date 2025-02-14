package me.ddggdd135.slimeae.api.blockdata;

import static me.ddggdd135.slimeae.api.blockdata.MEBusDataAdapter.DIRECTION_KEY;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class MEExportBusDataAdapter implements IBlockDataAdapter<MEExportBusData> {
    public static final String ITEMS_KEY = "items";

    @Override
    @Nonnull
    public ReadWriteNBT serialize(@Nonnull IBlockData blockData) {
        ReadWriteNBT nbt = NBT.createNBTObject();
        if (!(blockData instanceof MEExportBusData data)) return nbt;

        nbt.setEnum(DIRECTION_KEY, data.getDirection());
        nbt.setItemStackArray(ITEMS_KEY, data.getItemStacks());

        return nbt;
    }

    @Override
    @Nonnull
    public MEExportBusData deserialize(@Nonnull ReadableNBT nbtCompound) {
        MEExportBusData data = new MEExportBusData();

        BlockFace blockFace = nbtCompound.getEnum(DIRECTION_KEY, BlockFace.class);
        if (blockFace != null) {
            data.setDirection(blockFace);
        }

        ItemStack[] itemStacks = nbtCompound.getItemStackArray(ITEMS_KEY);
        if (itemStacks != null) {
            data.setItemStacks(itemStacks);
        }

        return data;
    }
}
