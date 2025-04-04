package me.ddggdd135.slimeae.api.blockdata;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import org.bukkit.inventory.ItemStack;

public class MELevelEmitterDataAdapter implements IBlockDataAdapter<MELevelEmitterData> {
    public static final String AMOUNT_KEY = "amount";
    public static final String ITEM_KEY = "item";

    @Override
    @Nonnull
    public ReadWriteNBT serialize(@Nonnull IBlockData blockData) {
        ReadWriteNBT nbt = NBT.createNBTObject();
        if (!(blockData instanceof MELevelEmitterData data)) return nbt;

        nbt.setLong(AMOUNT_KEY, data.getAmount());
        nbt.setItemStack(ITEM_KEY, data.getItemStack());

        return nbt;
    }

    @Override
    @Nonnull
    public MELevelEmitterData deserialize(@Nonnull ReadableNBT nbtCompound) {
        MELevelEmitterData data = new MELevelEmitterData();

        long amount = nbtCompound.getLong(AMOUNT_KEY);
        ItemStack itemStack = nbtCompound.getItemStack(ITEM_KEY);
        data.setAmount(amount);
        data.setItemStack(itemStack);

        return data;
    }
}
