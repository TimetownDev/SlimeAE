package me.ddggdd135.slimeae.api.blockdata;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import org.bukkit.inventory.ItemStack;

public class MEInterfaceDataAdapter implements IBlockDataAdapter<MEInterfaceData> {
    public static final String ITEMS_KEY = "items";

    @Override
    @Nonnull
    public ReadWriteNBT serialize(@Nonnull IBlockData blockData) {
        ReadWriteNBT nbt = NBT.createNBTObject();
        if (!(blockData instanceof MEInterfaceData data)) return nbt;

        nbt.setItemStackArray(ITEMS_KEY, data.getItemStacks());

        return nbt;
    }

    @Override
    @Nonnull
    public MEInterfaceData deserialize(@Nonnull ReadableNBT nbtCompound) {
        MEInterfaceData data = new MEInterfaceData();

        ItemStack[] itemStacks = nbtCompound.getItemStackArray(ITEMS_KEY);
        if (itemStacks != null) {
            data.setItemStacks(itemStacks);
        }

        return data;
    }
}
