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
    public static final String DEPRECATED_KEY = "deprecated";
    public static final String INPUT_KEY = "input";
    public static final String OUTPUT_KEY = "output";
    public static final String CRAFTING_TYPE_KEY = "crafting_type";
    private static final String DEPRECATED_LORE = "&c已废弃：配方不存在";

    public Pattern(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        UUID uuid = NBT.get(itemStack, x -> {
            return x.getUUID(UUID_KEY);
        });

        if (uuid != null && cache.containsKey(uuid)) {
            return cache.get(uuid);
        }

        boolean deprecated = NBT.get(itemStack, x -> {
            return x.hasTag(DEPRECATED_KEY, NBTType.NBTTagByte);
        });
        if (deprecated) {
            if (uuid != null) {
                cache.put(uuid, null);
            }
            return null;
        }

        CraftingRecipe craftingRecipe = cache.get(uuid);
        if (craftingRecipe == null) {
            craftingRecipe = NBT.get(itemStack, x -> {
                if (!x.hasTag(RECIPE_KEY, NBTType.NBTTagCompound)) return null;
                ReadableNBT compound = x.getCompound(RECIPE_KEY);
                String typeName = compound.getString(CRAFTING_TYPE_KEY);
                CraftType craftType = CraftType.fromName(typeName);
                if (craftType == null) {
                    craftType = CraftType.COOKING;
                }
                ItemStack[] parsedInput = Arrays.stream(compound.getItemStackArray(INPUT_KEY))
                        .filter(y -> !SlimefunUtils.isItemSimilar(y, MenuItems.EMPTY, false, false))
                        .toArray(ItemStack[]::new);
                ItemStack[] parsedOutput = Arrays.stream(compound.getItemStackArray(OUTPUT_KEY))
                        .filter(y -> !SlimefunUtils.isItemSimilar(y, MenuItems.EMPTY, false, false))
                        .toArray(ItemStack[]::new);
                return new CraftingRecipe(craftType, parsedInput, parsedOutput);
            });

            if (craftingRecipe == null) {
                markDeprecated(itemStack, uuid);
                return null;
            }

            if (craftingRecipe != null) {
                if (!craftingRecipe.getCraftType().isVanilla() && craftingRecipe.getCraftType() != CraftType.COOKING) {
                    CraftingRecipe newRecipe =
                            RecipeUtils.getRecipe(craftingRecipe.getInput(), craftingRecipe.getOutput());
                    if (newRecipe == null) {
                        markDeprecated(itemStack, uuid);
                        return null;
                    } else if (!newRecipe.equals(craftingRecipe)) {
                        craftingRecipe = newRecipe;
                        setRecipe(itemStack, craftingRecipe);
                        uuid = NBT.get(itemStack, x -> {
                            return x.getUUID(UUID_KEY);
                        });
                    }
                }
            }

            if (uuid != null) {
                cache.put(uuid, craftingRecipe);
            }
        }

        return craftingRecipe;
    }

    private static void markDeprecated(@Nonnull ItemStack itemStack, @Nullable UUID oldUuid) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
            boolean hasDeprecatedLore =
                    lore.stream().anyMatch(line -> CMIChatColor.stripColor(line).contains("已废弃"));
            if (!hasDeprecatedLore) {
                lore.add(DEPRECATED_LORE);
                meta.setLore(CMIChatColor.translate(lore));
                itemStack.setItemMeta(meta);
            }
        }

        NBT.modify(itemStack, x -> {
            x.setBoolean(DEPRECATED_KEY, true);
            if (x.hasTag(RECIPE_KEY)) {
                x.removeKey(RECIPE_KEY);
            }
        });

        UUID currentUuid = NBT.get(itemStack, x -> {
            return x.getUUID(UUID_KEY);
        });
        if (currentUuid != null) {
            cache.put(currentUuid, null);
        } else if (oldUuid != null) {
            cache.put(oldUuid, null);
        }
    }

    @Nonnull
    public static void setRecipe(@Nonnull ItemStack itemStack, @Nonnull CraftingRecipe recipe) {
        ItemMeta meta = itemStack.getItemMeta();
        ItemStack[] inputs = recipe.getInput();
        List<String> lore = new ArrayList<>();
        if (recipe.getCraftType().isLarge()) {
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

        ItemStack[] compactInput = compactItemStacks(recipe.getInput());
        ItemStack[] compactOutput = compactItemStacks(recipe.getOutput());

        NBT.modify(itemStack, x -> {
            if (x.hasTag(DEPRECATED_KEY)) x.removeKey(DEPRECATED_KEY);
            if (x.hasTag(RECIPE_KEY)) x.removeKey(RECIPE_KEY);
            ReadWriteNBT compound = x.getOrCreateCompound(RECIPE_KEY);
            compound.setString(CRAFTING_TYPE_KEY, recipe.getCraftType().name());
            compound.setItemStackArray(INPUT_KEY, compactInput);
            compound.setItemStackArray(OUTPUT_KEY, compactOutput);
            x.setUUID(UUID_KEY, uuid);
        });
        cache.put(uuid, recipe);
    }

    @Nonnull
    private static ItemStack[] compactItemStacks(@Nonnull ItemStack[] items) {
        ItemStack[] result = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            result[i] = compactItemStack(items[i]);
        }
        return result;
    }

    @Nullable private static ItemStack compactItemStack(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return item;
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (sfItem != null) {
            ItemStack canonical = sfItem.getItem().clone();
            canonical.setAmount(item.getAmount());
            return canonical;
        }
        return item;
    }
}
