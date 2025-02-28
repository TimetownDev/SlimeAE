package me.ddggdd135.slimeae.utils;

import com.balugaq.netex.api.data.SuperRecipe;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractManualCrafter;
import com.ytdd9527.networksexpansion.implementation.ExpansionItemStacks;
import com.ytdd9527.networksexpansion.implementation.machines.manual.ExpansionWorkbench;
import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.base.items.AssemblyTable;
import io.github.mooy1.infinityexpansion.items.blocks.Blocks;
import io.github.mooy1.infinityexpansion.items.blocks.InfinityWorkbench;
import io.github.mooy1.infinityexpansion.items.mobdata.MobData;
import io.github.mooy1.infinityexpansion.items.mobdata.MobDataInfuser;
import io.github.thebusybiscuit.exoticgarden.ExoticGardenRecipeTypes;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemStackSnapshot;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.abstracts.AbstractMachineBlock;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.core.recipes.SlimefunAERecipeTypes;
import me.lucasgithuber.obsidianexpansion.Items;
import me.lucasgithuber.obsidianexpansion.machines.ObsidianForge;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.sfiguz7.transcendence.lists.TEItems;
import me.sfiguz7.transcendence.lists.TERecipeType;
import org.bukkit.Bukkit;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipeUtils {
    public static final Map<RecipeType, SlimefunItem> SUPPORTED_RECIPE_TYPES = new HashMap<>();
    public static final Map<RecipeType, SlimefunItem> CRAFTING_TABLE_TYPES = new HashMap<>();
    public static final Map<RecipeType, SlimefunItem> LARGE_TYPES = new HashMap<>();

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        return getRecipe(itemStack, SUPPORTED_RECIPE_TYPES);
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack itemStack, Map<RecipeType, SlimefunItem> supported) {
        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
        if (slimefunItem != null) {
            if (supported.containsKey(slimefunItem.getRecipeType())) {
                return new CraftingRecipe(
                        getCraftType(slimefunItem.getRecipeType()),
                        slimefunItem.getRecipe(),
                        slimefunItem.getRecipeOutput());
            } else {
                for (Map.Entry<RecipeType, SlimefunItem> entry : supported.entrySet()) {
                    if (entry.getValue() == null) continue;
                    for (ItemStack[] input : getInputs(entry.getKey())) {
                        ItemStack[] outputs = getOutputs(entry.getKey(), input);
                        if (outputs.length != 1) continue;
                        ItemStack output = outputs[0];
                        if (SlimefunUtils.isItemSimilar(itemStack, output, true, false)) {
                            return new CraftingRecipe(getCraftType(entry.getKey()), input, output);
                        }
                    }
                }
            }

            return new CraftingRecipe(CraftType.COOKING, slimefunItem.getRecipe(), slimefunItem.getRecipeOutput());
        }
        List<Recipe> minecraftRecipe = Bukkit.getRecipesFor(itemStack);
        for (Recipe recipe : minecraftRecipe) {
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                return new CraftingRecipe(
                        CraftType.CRAFTING_TABLE,
                        shapedRecipe.getIngredientMap().values().toArray(ItemStack[]::new),
                        new ItemStack(
                                shapedRecipe.getResult().getType(),
                                shapedRecipe.getResult().getAmount()));
            }
            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                return new CraftingRecipe(
                        CraftType.CRAFTING_TABLE,
                        shapelessRecipe.getIngredientList().toArray(ItemStack[]::new),
                        new ItemStack(
                                shapelessRecipe.getResult().getType(),
                                shapelessRecipe.getResult().getAmount()));
            }
            if (recipe instanceof CookingRecipe cookingRecipe)
                return new CraftingRecipe(
                        CraftType.COOKING,
                        new ItemStack[] {
                            new ItemStack(
                                    cookingRecipe.getInput().getType(),
                                    cookingRecipe.getInput().getAmount())
                        },
                        new ItemStack(
                                cookingRecipe.getResult().getType(),
                                cookingRecipe.getResult().getAmount()));
        }
        return null;
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack[] input) {
        return getRecipe(input, SUPPORTED_RECIPE_TYPES);
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack[] input, Map<RecipeType, SlimefunItem> supported) {
        for (Map.Entry<RecipeType, SlimefunItem> entry : supported.entrySet()) {
            if (entry.getValue() == null) continue;
            in:
            for (ItemStack[] input1 : getInputs(entry.getKey())) {
                for (int i = 0; i < Math.max(input.length, input1.length); i++) {
                    ItemStack x = null;
                    ItemStack y = null;
                    if (input.length > i) {
                        x = input[i];
                    }
                    if (input1.length > i) {
                        y = input1[i];
                    }
                    if (!SlimefunUtils.isItemSimilar(x, y, true, false)) {
                        continue in;
                    }
                }

                return new CraftingRecipe(getCraftType(entry.getKey()), input1, getOutputs(entry.getKey(), input1));
            }
        }

        Recipe minecraftRecipe =
                Bukkit.getCraftingRecipe(input, Bukkit.getWorlds().get(0));
        if (minecraftRecipe instanceof ShapedRecipe shapedRecipe) {
            return new CraftingRecipe(
                    CraftType.CRAFTING_TABLE,
                    input.clone(),
                    new ItemStack(
                            shapedRecipe.getResult().getType(),
                            shapedRecipe.getResult().getAmount()));
        }
        if (minecraftRecipe instanceof ShapelessRecipe shapelessRecipe) {
            return new CraftingRecipe(
                    CraftType.CRAFTING_TABLE,
                    input.clone(),
                    new ItemStack(
                            shapelessRecipe.getResult().getType(),
                            shapelessRecipe.getResult().getAmount()));
        }
        if (minecraftRecipe instanceof CookingRecipe cookingRecipe)
            return new CraftingRecipe(
                    CraftType.COOKING,
                    input.clone(),
                    new ItemStack(
                            cookingRecipe.getResult().getType(),
                            cookingRecipe.getResult().getAmount()));

        return null;
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemStack[] input, @Nonnull ItemStack[] output) {
        return getRecipe(input, output, SUPPORTED_RECIPE_TYPES);
    }

    @Nullable public static CraftingRecipe getRecipe(
            @Nonnull ItemStack[] input, @Nonnull ItemStack[] output, Map<RecipeType, SlimefunItem> supported) {
        for (Map.Entry<RecipeType, SlimefunItem> entry : supported.entrySet()) {
            if (entry.getValue() == null) continue;
            in:
            for (ItemStack[] input1 : getInputs(entry.getKey())) {
                for (int i = 0; i < Math.max(input.length, input1.length); i++) {
                    ItemStack x = null;
                    ItemStack y = null;
                    if (input.length > i) {
                        x = input[i];
                    }
                    if (input1.length > i) {
                        y = input1[i];
                    }
                    if (!SlimefunUtils.isItemSimilar(x, y, true, false)) {
                        continue in;
                    }
                }

                ItemStack[] output1 = getOutputs(entry.getKey(), input1);

                for (int i = 0; i < Math.max(output.length, output1.length); i++) {
                    ItemStack x = null;
                    ItemStack y = null;
                    if (output.length > i) {
                        x = output[i];
                    }
                    if (output1.length > i) {
                        y = output1[i];
                    }
                    if (!SlimefunUtils.isItemSimilar(x, y, true, false)) {
                        continue in;
                    }
                }

                return new CraftingRecipe(getCraftType(entry.getKey()), input1, output1);
            }
        }

        Recipe minecraftRecipe =
                Bukkit.getCraftingRecipe(input, Bukkit.getWorlds().get(0));
        if (minecraftRecipe instanceof ShapedRecipe shapedRecipe) {
            ItemStack out = new ItemStack(
                    shapedRecipe.getResult().getType(), shapedRecipe.getResult().getAmount());
            if (output.length == 1 && SlimefunUtils.isItemSimilar(output[0], out, true, false))
                return new CraftingRecipe(CraftType.CRAFTING_TABLE, input.clone(), output);
        }
        if (minecraftRecipe instanceof ShapelessRecipe shapelessRecipe) {
            ItemStack out = new ItemStack(
                    shapelessRecipe.getResult().getType(),
                    shapelessRecipe.getResult().getAmount());
            if (output.length == 1 && SlimefunUtils.isItemSimilar(output[0], out, true, false))
                return new CraftingRecipe(CraftType.CRAFTING_TABLE, input.clone(), out);
        }
        if (minecraftRecipe instanceof CookingRecipe cookingRecipe) {
            ItemStack out = new ItemStack(
                    cookingRecipe.getResult().getType(),
                    cookingRecipe.getResult().getAmount());
            if (output.length == 1 && SlimefunUtils.isItemSimilar(output[0], out, true, false))
                return new CraftingRecipe(CraftType.COOKING, input.clone(), out);
        }

        if (output.length == 1) return getRecipe(output[0]);

        return null;
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull Map<ItemStack, Long> input, @Nullable ItemStack output) {
        return getRecipe(input, output, SUPPORTED_RECIPE_TYPES);
    }

    @Nullable public static CraftingRecipe getRecipe(
            @Nonnull Map<ItemStack, Long> input, @Nullable ItemStack output, Map<RecipeType, SlimefunItem> supported) {
        for (Map.Entry<RecipeType, SlimefunItem> entry : supported.entrySet()) {
            if (entry.getValue() == null) continue;

            for (ItemStack[] inputItems : getInputs(entry.getKey())) {
                Map<ItemStack, Long> input1 = ItemUtils.getAmounts(inputItems);
                if (!MapUtils.areMapsEqual(input1, input)) continue;
                ItemStack[] output1 = getOutputs(entry.getKey(), inputItems);
                if (output1.length != 1) continue;
                if (!SlimefunUtils.isItemSimilar(output, output1[0], true, false)) continue;

                return new CraftingRecipe(getCraftType(entry.getKey()), inputItems, output1);
            }
        }

        return null;
    }

    public static List<ItemStack[]> getInputs(RecipeType recipeType) {
        SlimefunItem slimefunItem = SUPPORTED_RECIPE_TYPES.get(recipeType);
        if (slimefunItem == null) return new ArrayList<>();
        if (slimefunItem instanceof MultiBlockMachine multiBlockMachine) {
            return RecipeType.getRecipeInputList(multiBlockMachine);
        }

        if (slimefunItem instanceof AContainer aContainer) {
            return aContainer.getMachineRecipes().stream()
                    .map(MachineRecipe::getInput)
                    .toList();
        }
        if (slimefunItem instanceof AbstractMachineBlock abstractMachineBlock) {
            return abstractMachineBlock.getMachineRecipes().stream()
                    .map(MachineRecipe::getInput)
                    .toList();
        }
        if (InfinityLibUtils.isCraftingBlock(slimefunItem)) {
            CraftCraftingBlock craftingBlock = new CraftCraftingBlock(slimefunItem);
            List<CraftCraftingBlockRecipe> recipes = craftingBlock.getRecipes();
            List<ItemStack[]> result = new ArrayList<>(recipes.size());
            for (CraftCraftingBlockRecipe recipe : recipes) {
                ItemStackSnapshot[] in = (ItemStackSnapshot[]) recipe.recipe();
                ItemStack[] re = new ItemStack[in.length];
                for (int j = 0; j < in.length; j++) {
                    if (in[j] == null || in[j].getType().isAir()) {
                        re[j] = null;
                        continue;
                    }
                    re[j] = new ItemStack(in[j]);
                }
                result.add(re);
            }
            return result;
        }
        if (slimefunItem instanceof AbstractManualCrafter abstractManualCrafter) {
            return abstractManualCrafter.getRecipes().stream()
                    .map(SuperRecipe::getInput)
                    .toList();
        }

        return new ArrayList<>();
    }

    public static ItemStack[] getOutputs(RecipeType recipeType, ItemStack[] inputs) {
        SlimefunItem slimefunItem = SUPPORTED_RECIPE_TYPES.get(recipeType);
        if (slimefunItem == null) return new ItemStack[0];
        if (slimefunItem instanceof MultiBlockMachine multiBlockMachine) {
            return new ItemStack[] {RecipeType.getRecipeOutputList(multiBlockMachine, inputs)};
        }

        if (slimefunItem instanceof AContainer aContainer) {
            List<MachineRecipe> recipes = aContainer.getMachineRecipes();
            i:
            for (MachineRecipe recipe : recipes) {
                ItemStack[] in = recipe.getInput();
                for (int i = 0; i < Math.max(in.length, inputs.length); i++) {
                    ItemStack x = null;
                    ItemStack y = null;
                    if (in.length > i) {
                        x = in[i];
                    }
                    if (inputs.length > i) {
                        y = inputs[i];
                    }
                    if (!SlimefunUtils.isItemSimilar(x, y, true, false)) {
                        continue i;
                    }
                }

                return recipe.getOutput();
            }
        }
        if (slimefunItem instanceof AbstractMachineBlock abstractMachineBlock) {
            List<MachineRecipe> recipes = abstractMachineBlock.getMachineRecipes();
            i:
            for (MachineRecipe recipe : recipes) {
                ItemStack[] in = recipe.getInput();
                for (int i = 0; i < Math.max(in.length, inputs.length); i++) {
                    ItemStack x = null;
                    ItemStack y = null;
                    if (in.length > i) {
                        x = in[i];
                    }
                    if (inputs.length > i) {
                        y = inputs[i];
                    }
                    if (!SlimefunUtils.isItemSimilar(x, y, true, false)) {
                        continue i;
                    }
                }

                return recipe.getOutput();
            }
        }
        if (InfinityLibUtils.isCraftingBlock(slimefunItem)) {
            CraftCraftingBlock craftingBlock = new CraftCraftingBlock(slimefunItem);
            List<CraftCraftingBlockRecipe> recipes = craftingBlock.getRecipes();
            i:
            for (CraftCraftingBlockRecipe recipe : recipes) {
                ItemStack[] in = recipe.recipe();
                for (int i = 0; i < Math.max(in.length, inputs.length); i++) {
                    ItemStack x = null;
                    ItemStack y = null;
                    if (in.length > i) {
                        x = in[i];
                    }
                    if (inputs.length > i) {
                        y = inputs[i];
                    }
                    if (x != null && !x.getType().isAir()) x = new ItemStack(x);
                    if (!SlimefunUtils.isItemSimilar(x, y, true, false)) {
                        continue i;
                    }
                }
                ItemStack out = new ItemStack(recipe.output());
                return new ItemStack[] {out};
            }

            return new ItemStack[0];
        }
        if (slimefunItem instanceof AbstractManualCrafter abstractManualCrafter) {
            List<SuperRecipe> recipes = abstractManualCrafter.getRecipes();
            i:
            for (SuperRecipe recipe : recipes) {
                ItemStack[] in = recipe.getInput();
                for (int i = 0; i < Math.max(in.length, inputs.length); i++) {
                    ItemStack x = null;
                    ItemStack y = null;
                    if (in.length > i) {
                        x = in[i];
                    }
                    if (inputs.length > i) {
                        y = inputs[i];
                    }
                    if (!SlimefunUtils.isItemSimilar(x, y, true, false)) {
                        continue i;
                    }
                }

                return recipe.getOutput();
            }
        }

        return new ItemStack[0];
    }

    public static CraftType getCraftType(@Nonnull RecipeType recipeType) {
        if (LARGE_TYPES.containsKey(recipeType)) return CraftType.LARGE;
        if (SUPPORTED_RECIPE_TYPES.containsKey(recipeType)) return CraftType.CRAFTING_TABLE;

        return CraftType.COOKING;
    }

    static {
        SUPPORTED_RECIPE_TYPES.put(
                RecipeType.ENHANCED_CRAFTING_TABLE, SlimefunItem.getByItem(SlimefunItems.ENHANCED_CRAFTING_TABLE));
        SUPPORTED_RECIPE_TYPES.put(SlimefunAERecipeTypes.CHARGER, SlimefunItem.getByItem(SlimefunAEItems.CHARGER));
        SUPPORTED_RECIPE_TYPES.put(SlimefunAERecipeTypes.INSCRIBER, SlimefunItem.getByItem(SlimefunAEItems.INSCRIBER));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.MAGIC_WORKBENCH, SlimefunItem.getByItem(SlimefunItems.MAGIC_WORKBENCH));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.ARMOR_FORGE, SlimefunItem.getByItem(SlimefunItems.ARMOR_FORGE));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.SMELTERY, SlimefunItem.getByItem(SlimefunItems.SMELTERY));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.ANCIENT_ALTAR, SlimefunItem.getByItem(SlimefunItems.ANCIENT_ALTAR));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.COMPRESSOR, SlimefunItem.getByItem(SlimefunItems.COMPRESSOR));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.GRIND_STONE, SlimefunItem.getByItem(SlimefunItems.GRIND_STONE));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.JUICER, SlimefunItem.getByItem(SlimefunItems.JUICER));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.ORE_CRUSHER, SlimefunItem.getByItem(SlimefunItems.ORE_CRUSHER));
        SUPPORTED_RECIPE_TYPES.put(RecipeType.PRESSURE_CHAMBER, SlimefunItem.getByItem(SlimefunItems.PRESSURE_CHAMBER));

        CRAFTING_TABLE_TYPES.put(
                RecipeType.ENHANCED_CRAFTING_TABLE, SlimefunItem.getByItem(SlimefunItems.ENHANCED_CRAFTING_TABLE));

        if (SlimeAEPlugin.getInfinityIntegration().isLoaded()) {
            SUPPORTED_RECIPE_TYPES.put(InfinityWorkbench.TYPE, SlimefunItem.getByItem(Blocks.INFINITY_FORGE));
            LARGE_TYPES.put(InfinityWorkbench.TYPE, SlimefunItem.getByItem(Blocks.INFINITY_FORGE));
            SUPPORTED_RECIPE_TYPES.put(MobDataInfuser.TYPE, SlimefunItem.getByItem(MobData.INFUSER));
            LARGE_TYPES.put(MobDataInfuser.TYPE, SlimefunItem.getByItem(MobData.INFUSER));
        }

        if (SlimeAEPlugin.getGalactifunIntegration().isLoaded()) {
            SUPPORTED_RECIPE_TYPES.put(AssemblyTable.TYPE, SlimefunItem.getByItem(BaseItems.ASSEMBLY_TABLE));
            LARGE_TYPES.put(AssemblyTable.TYPE, SlimefunItem.getByItem(BaseItems.ASSEMBLY_TABLE));
        }

        if (SlimeAEPlugin.getObsidianExpansionIntegration().isLoaded()) {
            SUPPORTED_RECIPE_TYPES.put(ObsidianForge.TYPE, SlimefunItem.getByItem(Items.OBSIDIAN_FORGE));
            LARGE_TYPES.put(ObsidianForge.TYPE, SlimefunItem.getByItem(Items.OBSIDIAN_FORGE));
        }

        if (SlimeAEPlugin.getExoticGardenIntegration().isLoaded()) {
            SUPPORTED_RECIPE_TYPES.put(ExoticGardenRecipeTypes.KITCHEN, SlimefunItem.getById("KITCHEN"));
        }

        if (SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded()) {
            SUPPORTED_RECIPE_TYPES.put(
                    ExpansionWorkbench.TYPE, SlimefunItem.getByItem(ExpansionItemStacks.NETWORKS_EXPANSION_WORKBENCH));
            CRAFTING_TABLE_TYPES.put(
                    ExpansionWorkbench.TYPE, SlimefunItem.getByItem(ExpansionItemStacks.NETWORKS_EXPANSION_WORKBENCH));
        }

        if (SlimeAEPlugin.getTranscEndenceIntegration().isLoaded()) {
            SUPPORTED_RECIPE_TYPES.put(TERecipeType.NANOBOT_CRAFTER, SlimefunItem.getByItem(TEItems.NANOBOT_CRAFTER));
            CRAFTING_TABLE_TYPES.put(TERecipeType.NANOBOT_CRAFTER, SlimefunItem.getByItem(TEItems.NANOBOT_CRAFTER));
        }
    }
}
