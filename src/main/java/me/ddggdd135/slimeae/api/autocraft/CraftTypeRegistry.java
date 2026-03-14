package me.ddggdd135.slimeae.api.autocraft;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CraftTypeRegistry {
    private static final Map<RecipeType, CraftType> RECIPE_TYPE_TO_CRAFT_TYPE = new ConcurrentHashMap<>();
    private static final Map<CraftType, RecipeType> CRAFT_TYPE_TO_RECIPE_TYPE = new ConcurrentHashMap<>();
    private static final Map<CraftType, SlimefunItem> CRAFT_TYPE_TO_MACHINE = new ConcurrentHashMap<>();

    private CraftTypeRegistry() {}

    public static void register(
            @Nonnull CraftType craftType, @Nonnull RecipeType recipeType, @Nullable SlimefunItem machine) {
        RECIPE_TYPE_TO_CRAFT_TYPE.put(recipeType, craftType);
        CRAFT_TYPE_TO_RECIPE_TYPE.put(craftType, recipeType);
        if (machine != null) {
            CRAFT_TYPE_TO_MACHINE.put(craftType, machine);
        }
    }

    @Nonnull
    public static CraftType getCraftType(@Nonnull RecipeType recipeType) {
        return RECIPE_TYPE_TO_CRAFT_TYPE.getOrDefault(recipeType, CraftType.COOKING);
    }

    public static boolean isRegistered(@Nonnull RecipeType recipeType) {
        return RECIPE_TYPE_TO_CRAFT_TYPE.containsKey(recipeType);
    }

    public static boolean isRegistered(@Nonnull CraftType craftType) {
        return CRAFT_TYPE_TO_RECIPE_TYPE.containsKey(craftType);
    }

    @Nullable public static RecipeType getRecipeType(@Nonnull CraftType craftType) {
        return CRAFT_TYPE_TO_RECIPE_TYPE.get(craftType);
    }

    @Nullable public static SlimefunItem getMachine(@Nonnull CraftType craftType) {
        return CRAFT_TYPE_TO_MACHINE.get(craftType);
    }

    @Nonnull
    public static List<CraftType> getSmallTypes() {
        List<CraftType> result = new ArrayList<>();
        for (CraftType type : CRAFT_TYPE_TO_RECIPE_TYPE.keySet()) {
            if (type.isSmall()) {
                result.add(type);
            }
        }
        return result;
    }

    @Nonnull
    public static List<CraftType> getLargeTypes() {
        List<CraftType> result = new ArrayList<>();
        for (CraftType type : CRAFT_TYPE_TO_RECIPE_TYPE.keySet()) {
            if (type.isLarge()) {
                result.add(type);
            }
        }
        return result;
    }

    @Nonnull
    public static List<CraftType> getAllRegistered() {
        return new ArrayList<>(CRAFT_TYPE_TO_RECIPE_TYPE.keySet());
    }

    @Nonnull
    public static Map<RecipeType, SlimefunItem> getSupportedRecipeTypes() {
        Map<RecipeType, SlimefunItem> result = new LinkedHashMap<>();
        for (Map.Entry<RecipeType, CraftType> entry : RECIPE_TYPE_TO_CRAFT_TYPE.entrySet()) {
            SlimefunItem machine = CRAFT_TYPE_TO_MACHINE.get(entry.getValue());
            result.put(entry.getKey(), machine);
        }
        return result;
    }

    @Nonnull
    public static Map<RecipeType, SlimefunItem> getLargeRecipeTypes() {
        Map<RecipeType, SlimefunItem> result = new LinkedHashMap<>();
        for (Map.Entry<RecipeType, CraftType> entry : RECIPE_TYPE_TO_CRAFT_TYPE.entrySet()) {
            if (entry.getValue().isLarge()) {
                SlimefunItem machine = CRAFT_TYPE_TO_MACHINE.get(entry.getValue());
                result.put(entry.getKey(), machine);
            }
        }
        return result;
    }
}
