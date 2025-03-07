package me.ddggdd135.slimeae.api.wrappers;

import me.ddggdd135.slimeae.utils.ReflectionUtils;
import org.bukkit.inventory.ItemStack;

public class CraftCraftingBlockRecipe extends CraftInfinityLibObject {
    public CraftCraftingBlockRecipe(Object handle) {
        super(handle);
    }

    public ItemStack[] recipe() {
        return ReflectionUtils.invokePrivateMethod(handle, "recipe", new Class[0]);
    }

    public ItemStack output() {
        return ReflectionUtils.invokePrivateMethod(handle, "output", new Class[0]);
    }
}
