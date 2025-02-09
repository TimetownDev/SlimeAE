package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTType;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Pattern extends SlimefunItem implements DistinctiveItem {
    public Pattern(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public boolean canStack(@Nonnull ItemMeta sfItemMeta, @Nonnull ItemMeta itemMeta) {
        List<String> lorea = sfItemMeta.getLore();
        List<String> loreb = itemMeta.getLore();
        if (lorea == null && loreb == null) return true;
        if (lorea == null) lorea = new ArrayList<>();
        if (loreb == null) loreb = new ArrayList<>();
        return lorea.isEmpty() && loreb.isEmpty() && sfItemMeta.equals(itemMeta);
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        return NBT.get(itemStack, x -> {
            if (!x.hasTag("recipe", NBTType.NBTTagCompound)) return null;
            ReadableNBT compound = x.getCompound("recipe");
            return new CraftingRecipe(
                    compound.getEnum("crafting_type", CraftType.class),
                    compound.getItemStackArray("input"),
                    compound.getItemStackArray("output"));
        });
    }

    @Nonnull
    public static void setRecipe(@Nonnull ItemStack itemStack, @Nonnull CraftingRecipe recipe) {
        ItemMeta meta = itemStack.getItemMeta();
        ItemStack[] inputs = recipe.getInput();
        List<String> lore = new ArrayList<>();
        if (recipe.getCraftType() == CraftType.LARGE) {
            lore.add("&e大型配方");
            lore.add("");
            inputs = ItemUtils.createItems(ItemUtils.getAmounts(inputs));
        }
        lore.add("&a输入");
        for (ItemStack input : inputs) {
            if (input == null || input.getType().isAir()) continue;
            lore.add("  &e- &f" + ItemUtils.getItemName(input) + "&f x " + input.getAmount());
        }
        lore.add("&e输出");
        for (ItemStack output : recipe.getOutput()) {
            if (output == null || output.getType().isAir()) continue;
            lore.add("  &e- &f" + ItemUtils.getItemName(output) + "&f x " + output.getAmount());
        }

        meta.setLore(CMIChatColor.translate(lore));
        itemStack.setItemMeta(meta);
        // 先clone meta 不然nbt修改后因为bug会导致名称丢失

        NBT.modify(itemStack, x -> {
            if (x.hasTag("recipe")) x.removeKey("recipe");
            ReadWriteNBT compound = x.getOrCreateCompound("recipe");
            compound.setEnum("crafting_type", recipe.getCraftType());
            compound.setItemStackArray("input", recipe.getInput());
            compound.setItemStackArray("output", recipe.getOutput());
            x.setUUID("uuid", UUID.randomUUID());
        });
    }
}
