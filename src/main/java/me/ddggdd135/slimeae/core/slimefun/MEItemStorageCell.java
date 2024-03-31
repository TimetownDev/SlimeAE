package me.ddggdd135.slimeae.core.slimefun;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.MEItemCellStorage;
import me.ddggdd135.slimeae.api.MEStorageCellCache;
import me.ddggdd135.slimeae.utils.ItemUtils;
import net.Zrips.CMILib.Colors.CMIChatColor;
import org.bukkit.inventory.ItemStack;

public class MEItemStorageCell extends SlimefunItem implements NotPlaceable {
    private final int size;

    public MEItemStorageCell(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int size) {
        super(itemGroup, item, recipeType, recipe);
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public static int getSize(@Nonnull ItemStack itemStack) {
        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
        if (!(slimefunItem instanceof MEItemStorageCell meItemStorageCell)) {
            return 0;
        } else return meItemStorageCell.getSize();
    }

    @Nonnull
    public static MEStorageCellCache getStorage(@Nonnull ItemStack itemStack) {
        if (SlimeAEPlugin.getSlimefunTickCount() % 60 == 0 || SlimeAEPlugin.getSlimefunTickCount() <= 16) {
            saveStorage(itemStack);
            updateStorage(itemStack);
            updateLore(itemStack);
        }
        return MEStorageCellCache.getMEStorageCellCache(itemStack);
    }

    public static void saveStorage(@Nonnull ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack, true);
        if (nbtItem.hasTag("item_storage"))
            nbtItem.removeKey("item_storage");
        NBTCompound data = ItemUtils.toNBT(MEStorageCellCache.getMEStorageCellCache(itemStack).getStorage());
        nbtItem.getOrCreateCompound("item_storage").mergeCompound(data);
    }

    public static void updateLore(@Nonnull ItemStack itemStack) {
        MEStorageCellCache meStorageCellCache = MEStorageCellCache.getMEStorageCellCache(itemStack);
        List<String> lores = new ArrayList<>();
        Map<ItemStack, Integer> storage = meStorageCellCache.getStorage();
        for (ItemStack key : storage.keySet()) {
            lores.add(CMIChatColor.translate(
                    "{#Bright_Sun>}" + ItemUtils.getName(key) + " - " + storage.get(key) + "{#Carrot_Orange<}"));
        }
        itemStack.setLore(lores);
    }

    public static void updateStorage(@Nonnull ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack, true);
        NBTCompound nbt = nbtItem.getOrCreateCompound("item_storage");
        Map<ItemStack, Integer> storage = ItemUtils.toStorage(nbt);
        if (nbtItem.hasTag("item_storage"))
            nbtItem.removeKey("item_storage");
        nbtItem.getOrCreateCompound("item_storage").mergeCompound(ItemUtils.toNBT(storage));
    }
}
