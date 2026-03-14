package me.ddggdd135.slimeae.utils;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.altar.AltarRecipe;
import io.github.thebusybiscuit.slimefun4.implementation.items.altar.AncientAltar;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemStackSnapshot;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.abstracts.AbstractMachineBlock;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftTypeRegistry;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.wrappers.CraftCraftingBlock;
import me.ddggdd135.slimeae.api.wrappers.CraftCraftingBlockRecipe;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.core.recipes.SlimefunAERecipeTypes;
import me.ddggdd135.slimeae.integrations.*;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;

public class RecipeUtils {
    public static final Map<RecipeType, SlimefunItem> SUPPORTED_RECIPE_TYPES = new HashMap<>();
    public static final Map<RecipeType, SlimefunItem> CRAFTING_TABLE_TYPES = new HashMap<>();
    public static final Map<RecipeType, SlimefunItem> LARGE_TYPES = new HashMap<>();

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        registerType(
                CraftType.ENHANCED_CRAFTING_TABLE,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                SlimefunItem.getByItem(SlimefunItems.ENHANCED_CRAFTING_TABLE));
        registerType(CraftType.CHARGER, SlimefunAERecipeTypes.CHARGER, SlimefunItem.getByItem(SlimeAEItems.CHARGER));
        registerType(
                CraftType.INSCRIBER, SlimefunAERecipeTypes.INSCRIBER, SlimefunItem.getByItem(SlimeAEItems.INSCRIBER));
        registerType(
                CraftType.MAGIC_WORKBENCH,
                RecipeType.MAGIC_WORKBENCH,
                SlimefunItem.getByItem(SlimefunItems.MAGIC_WORKBENCH));
        registerType(CraftType.ARMOR_FORGE, RecipeType.ARMOR_FORGE, SlimefunItem.getByItem(SlimefunItems.ARMOR_FORGE));
        registerType(CraftType.SMELTERY, RecipeType.SMELTERY, SlimefunItem.getByItem(SlimefunItems.SMELTERY));
        registerType(
                CraftType.ANCIENT_ALTAR, RecipeType.ANCIENT_ALTAR, SlimefunItem.getByItem(SlimefunItems.ANCIENT_ALTAR));
        registerType(CraftType.COMPRESSOR, RecipeType.COMPRESSOR, SlimefunItem.getByItem(SlimefunItems.COMPRESSOR));
        registerType(CraftType.GRIND_STONE, RecipeType.GRIND_STONE, SlimefunItem.getByItem(SlimefunItems.GRIND_STONE));
        registerType(CraftType.JUICER, RecipeType.JUICER, SlimefunItem.getByItem(SlimefunItems.JUICER));
        registerType(CraftType.ORE_CRUSHER, RecipeType.ORE_CRUSHER, SlimefunItem.getByItem(SlimefunItems.ORE_CRUSHER));
        registerType(
                CraftType.PRESSURE_CHAMBER,
                RecipeType.PRESSURE_CHAMBER,
                SlimefunItem.getByItem(SlimefunItems.PRESSURE_CHAMBER));
        registerType(
                CraftType.HEATED_PRESSURE_CHAMBER,
                RecipeType.HEATED_PRESSURE_CHAMBER,
                SlimefunItem.getByItem(SlimefunItems.HEATED_PRESSURE_CHAMBER));

        try {
            if (SlimeAEPlugin.getInfinityIntegration().isLoaded()
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("enable_InfinityExpansion", true)) {
                InfinityRecipeRegistration.register();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (SlimeAEPlugin.getGalactifunIntegration().isLoaded()
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("enable_Galactifun", true)) {
                GalactifunRecipeRegistration.register();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (SlimeAEPlugin.getObsidianExpansionIntegration().isLoaded()
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("enable_ObsidianExpansion", true)) {
                ObsidianExpansionRecipeRegistration.register();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (SlimeAEPlugin.getExoticGardenIntegration().isLoaded()
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("enable_ExoticGarden", true)) {
                ExoticGardenRecipeRegistration.register();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded()
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("enable_NetworksExpansion", true)) {
                NetworksExpansionRecipeRegistration.register();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded()
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("enable_NTW_QUANTUM_WORKBENCH", true)) {
                NetworksExpansionRecipeRegistration.registerQuantumWorkbench();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (SlimeAEPlugin.getTranscEndenceIntegration().isLoaded()
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("enable_TranscEndence", true)) {
                TranscEndenceRecipeRegistration.register();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (SlimeAEPlugin.getLogiTechIntegration().isLoaded()
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("enable_LogiTech", true)) {
                LogiTechRecipeRegistration.register();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (SlimeAEPlugin.getFinalTechIntegration().isLoaded()
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("enable_FinalTECH", true)) {
                FinalTechRecipeRegistration.register();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (SlimeAEPlugin.getFinalTechIntegration().isLoaded()
                    && SlimeAEPlugin.getInstance().getConfig().getBoolean("enable_FinalTECH_BedrockCraftTable", true)) {
                FinalTechRecipeRegistration.registerBedrockCraftTable();
            }
        } catch (Throwable ignored) {
        }
    }

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
                        shapedRecipe.getIngredientMap().values().stream()
                                .map(x -> {
                                    if (x == null) return null;
                                    return new ItemStack(x.getType(), x.getAmount());
                                })
                                .toArray(ItemStack[]::new),
                        new ItemStack(
                                shapedRecipe.getResult().getType(),
                                shapedRecipe.getResult().getAmount()));
            }
            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                return new CraftingRecipe(
                        CraftType.CRAFTING_TABLE,
                        shapelessRecipe.getIngredientList().stream()
                                .map(x -> {
                                    if (x == null) return null;
                                    return new ItemStack(x.getType(), x.getAmount());
                                })
                                .toArray(ItemStack[]::new),
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
                    ItemStack x = new ItemStack(Material.AIR);
                    ItemStack y = new ItemStack(Material.AIR);
                    if (input.length > i) {
                        x = input[i];
                        if (x == null) x = new ItemStack(Material.AIR);
                    }
                    if (input1.length > i) {
                        y = input1[i];
                        if (y == null) y = new ItemStack(Material.AIR);
                    }
                    if (!SlimefunUtils.isItemSimilar(x, y, true, true)) {
                        continue in;
                    }
                }

                return new CraftingRecipe(getCraftType(entry.getKey()), input1, getOutputs(entry.getKey(), input1));
            }
        }

        // 校验输入中是否包含粘液物品，粘液物品不应该用原版配方合成
        boolean inputHasSimi =
                Arrays.stream(input).filter(Objects::nonNull).anyMatch(item -> SlimefunItem.getByItem(item) != null);
        if (inputHasSimi) return null;

        ItemStack[] oldInput = input;
        input = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            if (oldInput.length <= i) break;
            input[i] = oldInput[i];
        }
        Recipe minecraftRecipe =
                Bukkit.getCraftingRecipe(input, Bukkit.getWorlds().get(0));
        if (minecraftRecipe instanceof ShapedRecipe shapedRecipe) {
            return new CraftingRecipe(
                    CraftType.CRAFTING_TABLE,
                    getRecipeInputs(shapedRecipe.getChoiceMap().values(), input),
                    new ItemStack(
                            shapedRecipe.getResult().getType(),
                            shapedRecipe.getResult().getAmount()));
        }
        if (minecraftRecipe instanceof ShapelessRecipe shapelessRecipe) {
            return new CraftingRecipe(
                    CraftType.CRAFTING_TABLE,
                    Arrays.stream(ItemUtils.trimItems(input))
                            .map(ItemStack::asOne)
                            .toArray(ItemStack[]::new),
                    new ItemStack(
                            shapelessRecipe.getResult().getType(),
                            shapelessRecipe.getResult().getAmount()));
        }
        if (minecraftRecipe instanceof CookingRecipe cookingRecipe)
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
                if (!ItemUtils.matchesAll(input, input1, true)) continue in;

                ItemStack[] output1 = getOutputs(entry.getKey(), input1);

                if (!ItemUtils.matchesAll(output, output1, true)) continue in;

                return new CraftingRecipe(getCraftType(entry.getKey()), input1, output1);
            }
        }

        // 校验输入中是否包含粘液物品，粘液物品不应该用原版配方合成
        boolean inputHasSimi =
                Arrays.stream(input).filter(Objects::nonNull).anyMatch(item -> SlimefunItem.getByItem(item) != null);
        if (inputHasSimi) return null;

        ItemStack[] oldInput = input;
        input = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            if (oldInput.length <= i) break;
            input[i] = oldInput[i];
        }
        Recipe minecraftRecipe =
                Bukkit.getCraftingRecipe(input, Bukkit.getWorlds().get(0));
        if (minecraftRecipe instanceof ShapedRecipe shapedRecipe) {
            ItemStack out = new ItemStack(
                    shapedRecipe.getResult().getType(), shapedRecipe.getResult().getAmount());
            if (output.length == 1 && SlimefunUtils.isItemSimilar(output[0], out, true, false))
                return new CraftingRecipe(
                        CraftType.CRAFTING_TABLE,
                        Arrays.stream(input)
                                .map(x -> {
                                    if (x == null) return null;
                                    return new ItemStack(x.getType(), x.getAmount());
                                })
                                .toArray(ItemStack[]::new),
                        output);
        }
        if (minecraftRecipe instanceof ShapelessRecipe shapelessRecipe) {
            ItemStack out = new ItemStack(
                    shapelessRecipe.getResult().getType(),
                    shapelessRecipe.getResult().getAmount());
            if (output.length == 1 && SlimefunUtils.isItemSimilar(output[0], out, true, false))
                return new CraftingRecipe(
                        CraftType.CRAFTING_TABLE,
                        Arrays.stream(input)
                                .map(x -> {
                                    if (x == null) return null;
                                    return new ItemStack(x.getType(), x.getAmount());
                                })
                                .toArray(ItemStack[]::new),
                        new ItemStack(
                                shapelessRecipe.getResult().getType(),
                                shapelessRecipe.getResult().getAmount()));
        }
        if (minecraftRecipe instanceof CookingRecipe cookingRecipe) {
            ItemStack out = new ItemStack(
                    cookingRecipe.getResult().getType(),
                    cookingRecipe.getResult().getAmount());
            if (output.length == 1 && SlimefunUtils.isItemSimilar(output[0], out, true, false))
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

        if (output.length == 1) return getRecipe(output[0]);

        return null;
    }

    @Nullable public static CraftingRecipe getRecipe(@Nonnull ItemHashMap<Long> input, @Nullable ItemStack output) {
        return getRecipe(input, output, SUPPORTED_RECIPE_TYPES);
    }

    @Nullable public static CraftingRecipe getRecipe(
            @Nonnull ItemHashMap<Long> input, @Nullable ItemStack output, Map<RecipeType, SlimefunItem> supported) {
        for (Map.Entry<RecipeType, SlimefunItem> entry : supported.entrySet()) {
            if (entry.getValue() == null) continue;

            for (ItemStack[] inputItems : getInputs(entry.getKey())) {
                ItemHashMap<Long> input1 = ItemUtils.getAmounts(inputItems);
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
                    re[j] = ItemStackSnapshotUtils.clone(in[j]);
                }
                result.add(re);
            }
            return result;
        }
        if (SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded()
                && NetworksExpansionRecipeRegistration.isAbstractManualCrafter(slimefunItem)) {
            return NetworksExpansionRecipeRegistration.getInputs(slimefunItem);
        }
        if (slimefunItem instanceof AncientAltar ancientAltar) {
            return ancientAltar.getRecipes().stream()
                    .map(RecipeUtils::getAltarRecipeInput)
                    .toList();
        }

        List<ItemStack[]> fallback = new ArrayList<>();
        for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            if (!item.isDisabled() && recipeType.equals(item.getRecipeType())) {
                fallback.add(item.getRecipe());
            }
        }
        return fallback;
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
                    if (x != null && !x.getType().isAir())
                        x = (x instanceof ItemStackSnapshot snap)
                                ? ItemStackSnapshotUtils.clone(snap)
                                : new ItemStack(x);
                    if (!SlimefunUtils.isItemSimilar(x, y, true, false)) {
                        continue i;
                    }
                }
                ItemStack rawOut = recipe.output();
                ItemStack out = (rawOut instanceof ItemStackSnapshot snap)
                        ? ItemStackSnapshotUtils.clone(snap)
                        : new ItemStack(rawOut);
                return new ItemStack[] {out};
            }

            return new ItemStack[0];
        }
        if (SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded()
                && NetworksExpansionRecipeRegistration.isAbstractManualCrafter(slimefunItem)) {
            ItemStack[] result = NetworksExpansionRecipeRegistration.getOutputs(slimefunItem, inputs);
            if (result != null) return result;
        }
        if (slimefunItem instanceof AncientAltar ancientAltar) {
            List<AltarRecipe> recipes = ancientAltar.getRecipes();
            i:
            for (AltarRecipe recipe : recipes) {
                ItemStack[] in = getAltarRecipeInput(recipe);
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

                return new ItemStack[] {recipe.getOutput()};
            }
        }

        for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            if (!item.isDisabled() && recipeType.equals(item.getRecipeType())) {
                ItemStack[] in = item.getRecipe();
                if (!ItemUtils.matchesAll(inputs, in, true)) continue;
                return new ItemStack[] {item.getRecipeOutput()};
            }
        }

        return new ItemStack[0];
    }

    public static CraftType getCraftType(@Nonnull RecipeType recipeType) {
        return CraftTypeRegistry.getCraftType(recipeType);
    }

    /**
     * 对输入材料和配方进行校验，并给出一个和玩家输入一样的配方矩阵
     * <pre>
     * [ 0 1 2 ]
     * [ 3 4 5 ]
     * [ 6 7 8 ]
     * </pre>
     * @param choices See {@link RecipeChoice}
     * @param playerInputs 玩家输入的配方顺序
     * @return 一个数量为1的配方顺序，注意不是原版配方顺序，而是玩家决定的顺序
     */
    public static ItemStack[] getRecipeInputs(Collection<RecipeChoice> choices, ItemStack[] playerInputs) {
        ItemStack[] result = new ItemStack[9];
        if (choices == null || playerInputs == null || playerInputs.length != 9) {
            return result;
        }

        List<RecipeChoice> choiceList =
                choices.stream().filter(Objects::nonNull).toList();
        int choiceIndex = 0;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack input = playerInputs[slot];
            if (input == null || input.getType().isAir()) continue;

            if (choiceIndex >= choiceList.size()) {
                // 输入材料比配方材料多
                return new ItemStack[0];
            }
            RecipeChoice choice = choiceList.get(choiceIndex);
            ItemStack testItem = input.clone();
            if (choice.test(testItem)) {
                ItemStack normalized = new ItemStack(testItem.getType(), 1);
                if (choice instanceof RecipeChoice.ExactChoice) {
                    normalized.setItemMeta(testItem.getItemMeta());
                }
                result[slot] = normalized;
                choiceIndex++;
            } else {
                // 材料不匹配，提前返回
                return new ItemStack[0];
            }
        }

        if (choiceIndex != choiceList.size()) {
            // 不匹配
            return new ItemStack[0];
        }
        return result;
    }

    public static ItemStack[] getAltarRecipeInput(@Nonnull AltarRecipe altarRecipe) {
        List<ItemStack> input = altarRecipe.getInput();
        ItemStack[] itemStacks = new ItemStack[9];
        itemStacks[0] = input.get(0);
        itemStacks[1] = input.get(1);
        itemStacks[2] = input.get(2);
        itemStacks[5] = input.get(3);

        itemStacks[8] = input.get(4);
        itemStacks[7] = input.get(5);
        itemStacks[6] = input.get(6);
        itemStacks[3] = input.get(7);

        itemStacks[4] = altarRecipe.getCatalyst();

        return itemStacks;
    }

    public static void registerType(CraftType craftType, RecipeType recipeType, SlimefunItem machine) {
        SUPPORTED_RECIPE_TYPES.put(recipeType, machine);
        CraftTypeRegistry.register(craftType, recipeType, machine);
        if (craftType.isSmall()) {
            CRAFTING_TABLE_TYPES.put(recipeType, machine);
        } else if (craftType.isLarge()) {
            LARGE_TYPES.put(recipeType, machine);
        }
    }
}
