package me.ddggdd135.slimeae.api.wrappers;

import java.util.ArrayList;
import java.util.List;
import me.ddggdd135.slimeae.utils.ReflectionUtils;

public class CraftCraftingBlock extends CraftInfinityLibObject {
    public CraftCraftingBlock(Object handle) {
        super(handle);
    }

    public List<CraftCraftingBlockRecipe> getRecipes() {
        List<Object> recipes = ReflectionUtils.getField(handle, "recipes");
        List<CraftCraftingBlockRecipe> result = new ArrayList<>(recipes.size());

        for (Object object : recipes) {
            result.add(new CraftCraftingBlockRecipe(object));
        }

        return result;
    }
}
