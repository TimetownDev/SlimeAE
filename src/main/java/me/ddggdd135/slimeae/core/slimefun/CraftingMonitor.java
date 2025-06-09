package me.ddggdd135.slimeae.core.slimefun;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class CraftingMonitor extends SlimefunItem implements IMEObject {
    public CraftingMonitor(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(onRightClick());
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}

    private BlockUseHandler onRightClick() {
        return e -> {
            Block block = e.getClickedBlock().get();
            if (!(e.getPlayer().hasPermission("slimefun.inventory.bypass")
                    || Slimefun.getProtectionManager()
                            .hasPermission(e.getPlayer(), block.getLocation(), Interaction.INTERACT_BLOCK))) {
                Slimefun.getLocalization().sendMessage(e.getPlayer(), "inventory.no-access", true);
                return;
            }

            NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
            if (info == null) return;
            info.openAutoCraftingSessionsMenu(e.getPlayer());
            e.cancel();
        };
    }
}
