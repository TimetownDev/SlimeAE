package me.ddggdd135.slimeae.core.recipes;

import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.utils.AdvancedCustomItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class SlimefunAERecipeTypes {
    public static final BlockDestroyRecipeType BLOCK_DESTROY = new BlockDestroyRecipeType(
            new NamespacedKey(SlimeAEPlugin.getInstance(), "block_destroy"),
            new AdvancedCustomItemStack(Material.IRON_PICKAXE, "{#ffffff}挖掘"));
    public static final BlockDestroyRecipeType WORLD_GENERATING = new BlockDestroyRecipeType(
            new NamespacedKey(SlimeAEPlugin.getInstance(), "world_generating"),
            new AdvancedCustomItemStack(Material.IRON_ORE, "&e世界自然生成"));
    public static final RecipeType INSCRIBER =
            new RecipeType(new NamespacedKey(SlimeAEPlugin.getInstance(), "inscriber"), SlimefunAEItems.INSCRIBER);
}
