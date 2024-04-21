package me.ddggdd135.slimeae.core;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import org.bukkit.inventory.ItemStack;

public class AutoCraftingSession {
    private CraftingRecipe recipe;
    private NetworkInfo info;
    private int count;
    private List<Pair<CraftingRecipe, Integer>> craftingSteps;
    private final Map<ItemStack[], CraftingRecipe> recipeCache = new HashMap<>();

    public AutoCraftingSession(@Nonnull NetworkInfo info, @Nonnull CraftingRecipe recipe, int count) {
        //        ItemStorage storage = new ItemStorage();
        //        storage.addItem(
        //                ItemUtils.createItems(new AdvancedCustomItemStack(SlimefunAEItems.CRYSTAL_CERTUS_QUARTZ),
        // 5000000));
        //        storage.addItem(ItemUtils.createItems(new AdvancedCustomItemStack(SlimefunAEItems.LOGIC_PROCESSOR),
        // 5000000));
        //        storage.addItem(
        //                ItemUtils.createItems(new AdvancedCustomItemStack(SlimefunAEItems.CALCULATION_PROCESSOR),
        // 5000000));
        //        storage.addItem(ItemUtils.createItems(new ItemStack(Material.IRON_INGOT), 64));
        //        storage.addItem(ItemUtils.createItems(new ItemStack(Material.REDSTONE), 5000000));
        //        storage.addItem(ItemUtils.createItems(new ItemStack(Material.GLASS), 5000000));
        //        List<Pair<CraftingRecipe, Integer>> pairList =
        //                match(getRecipe(new ItemStack(SlimefunAEItems.ME_ITEM_STORAGE_CELL_16M)), 1, storage);
        this.info = info;
        this.recipe = recipe;
        this.count = count;
    }

    private List<Pair<CraftingRecipe, Integer>> match(CraftingRecipe recipe, int count, ItemStorage storage) {
        // if (!info.getRecipes().contains(recipe)) throw new NoEnoughMaterialsException();
        List<Pair<CraftingRecipe, Integer>> result = new ArrayList<>();
        Map<ItemStack, Integer> input = ItemUtils.getAmounts(recipe.getInput());
        for (ItemStack template : input.keySet()) {
            int amount = storage.getStorage().getOrDefault(template, 0);
            int need = input.get(template) * count;
            if (amount == 0) {
                // 计划合成
                CraftingRecipe craftingRecipe = getRecipe(template);
                if (craftingRecipe == null) throw new NoEnoughMaterialsException();
                // 计算需要合成多少次
                int out = ItemUtils.getAmounts(craftingRecipe.getOutput()).get(template);
                int countToCraft = (int) Math.ceil(need / (double) out);
                result.addAll(match(craftingRecipe, countToCraft, storage));
            } else if (amount >= need) {
                storage.tryTakeItem(new ItemRequest(template, need));
            } else {
                storage.tryTakeItem(new ItemRequest(template, amount));
                // 计算还需要多少
                need -= amount;
                // 计划合成
                CraftingRecipe craftingRecipe = getRecipe(template);
                if (craftingRecipe == null) throw new NoEnoughMaterialsException();
                // 计算需要合成多少次
                int out = ItemUtils.getAmounts(craftingRecipe.getOutput()).get(template);
                int countToCraft = (int) Math.ceil(need / (double) out);
                result.addAll(match(craftingRecipe, countToCraft, storage));
            }
        }
        result.add(new ObjectObjectMutablePair<>(recipe, count));
        return result;
    }

    @Nullable private CraftingRecipe getRecipe(ItemStack itemStack) {
        if (recipeCache.containsKey(itemStack)) return recipeCache.get(itemStack);
        CraftingRecipe craftingRecipe = RecipeUtils.getRecipe(itemStack);
        recipeCache.put(new ItemStack[] {itemStack}, craftingRecipe);
        return craftingRecipe;
    }
}
