package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBTType;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Pattern extends SlimefunItem {
    private static final Map<UUID, CraftingRecipe> cache = new HashMap<>();
    public static final String UUID_KEY = "uuid";
    public static final String RECIPE_KEY = "recipe";
    public static final String INPUT_KEY = "input";
    public static final String OUTPUT_KEY = "output";
    public static final String CRAFTING_TYPE_KEY = "crafting_type";

    public Pattern(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        UUID uuid = NBT.get(itemStack, x -> {
            return x.getUUID(UUID_KEY);
        });

        CraftingRecipe craftingRecipe = cache.get(uuid);
        if (craftingRecipe == null) {
            craftingRecipe = NBT.get(itemStack, x -> {
                if (!x.hasTag(RECIPE_KEY, NBTType.NBTTagCompound)) return null;
                ReadableNBT compound = x.getCompound(RECIPE_KEY);
                return new CraftingRecipe(
                        compound.getEnum(CRAFTING_TYPE_KEY, CraftType.class),
                        Arrays.stream(compound.getItemStackArray(INPUT_KEY))
                                .filter(y -> !SlimefunUtils.isItemSimilar(y, MenuItems.EMPTY, false, false))
                                .toArray(ItemStack[]::new),
                        Arrays.stream(compound.getItemStackArray(OUTPUT_KEY))
                                .filter(y -> !SlimefunUtils.isItemSimilar(y, MenuItems.EMPTY, false, false))
                                .toArray(ItemStack[]::new));
            });

            setRecipe(itemStack, craftingRecipe);

            if (craftingRecipe.getCraftType() == CraftType.CRAFTING_TABLE) {
                CraftingRecipe newRecipe = RecipeUtils.getRecipe(craftingRecipe.getInput(), craftingRecipe.getOutput());
                if (newRecipe == null || !(newRecipe.equals(craftingRecipe))) craftingRecipe = null;
            }

            cache.put(uuid, craftingRecipe);
        }

        return craftingRecipe;
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

        UUID uuid = UUID.randomUUID();

        NBT.modify(itemStack, x -> {
            if (x.hasTag(RECIPE_KEY)) x.removeKey(RECIPE_KEY);
            ReadWriteNBT compound = x.getOrCreateCompound(RECIPE_KEY);
            compound.setEnum(CRAFTING_TYPE_KEY, recipe.getCraftType());
            compound.setItemStackArray(INPUT_KEY, recipe.getInput());
            compound.setItemStackArray(OUTPUT_KEY, recipe.getOutput());
            x.setUUID(UUID_KEY, uuid);
        });
        cache.put(uuid, recipe);
    }
}
