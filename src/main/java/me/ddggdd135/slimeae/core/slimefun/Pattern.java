package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTCompound;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTItem;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTType;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Pattern extends SlimefunItem {
    public Pattern(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack, true);
        if (!nbtItem.hasTag("recipe", NBTType.NBTTagCompound)) return null;
        NBTCompound compound = nbtItem.getOrCreateCompound("recipe");
        return new CraftingRecipe(
                compound.getEnum("crafting_type", CraftType.class),
                compound.getItemStackArray("input"),
                compound.getItemStackArray("output"));
    }

    public static void setRecipe(@Nonnull ItemStack itemStack, @Nonnull CraftingRecipe recipe) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasTag("recipe")) nbtItem.removeKey("recipe");
        NBTCompound compound = nbtItem.getOrCreateCompound("recipe");
        compound.setEnum("crafting_type", recipe.getCraftType());
        compound.setItemStackArray("input", recipe.getInput());
        compound.setItemStackArray("output", recipe.getOutput());
        nbtItem.applyNBT(itemStack);
        List<String> lore = new ArrayList<>();
        lore.add("&a输入");
        for (ItemStack input : recipe.getInput()) {
            lore.add("  &e- &f" + ItemUtils.getItemName(input) + "&f x " + input.getAmount());
        }
        lore.add("&e输出");
        for (ItemStack output : recipe.getOutput()) {
            lore.add("  &e- &f" + ItemUtils.getItemName(output) + "&f x " + output.getAmount());
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(CMIChatColor.translate(lore));
        itemStack.setItemMeta(meta);
    }
}
