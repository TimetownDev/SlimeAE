package me.ddggdd135.slimeae.integrations;

import com.balugaq.netex.api.data.SuperRecipe;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractManualCrafter;
import com.ytdd9527.networksexpansion.implementation.ExpansionItemStacks;
import com.ytdd9527.networksexpansion.implementation.machines.manual.ExpansionWorkbench;
import io.github.sefiraat.networks.slimefun.NetworksSlimefunItemStacks;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumWorkbench;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.List;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import org.bukkit.inventory.ItemStack;

public final class NetworksExpansionRecipeRegistration {
    public static void register() {
        RecipeUtils.registerType(
                CraftType.NETWORKS_EXPANSION_WORKBENCH,
                ExpansionWorkbench.TYPE,
                SlimefunItem.getByItem(ExpansionItemStacks.NETWORKS_EXPANSION_WORKBENCH));
    }

    public static void registerQuantumWorkbench() {
        RecipeUtils.registerType(
                CraftType.QUANTUM_WORKBENCH,
                NetworkQuantumWorkbench.TYPE,
                SlimefunItem.getByItem(NetworksSlimefunItemStacks.NETWORK_QUANTUM_WORKBENCH));
    }

    public static boolean isAbstractManualCrafter(SlimefunItem slimefunItem) {
        return slimefunItem instanceof AbstractManualCrafter;
    }

    public static List<ItemStack[]> getInputs(SlimefunItem slimefunItem) {
        AbstractManualCrafter crafter = (AbstractManualCrafter) slimefunItem;
        return crafter.getRecipes().stream().map(SuperRecipe::getInput).toList();
    }

    public static ItemStack[] getOutputs(SlimefunItem slimefunItem, ItemStack[] inputs) {
        AbstractManualCrafter crafter = (AbstractManualCrafter) slimefunItem;
        List<SuperRecipe> recipes = crafter.getRecipes();
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
        return null;
    }
}
