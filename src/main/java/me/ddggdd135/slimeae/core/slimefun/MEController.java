package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MEController extends SlimefunItem implements IMEController<MEController> {
    public MEController(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(new BlockBreakHandler(true, true) {
            @Override
            public void onPlayerBreak(BlockBreakEvent blockBreakEvent, ItemStack itemStack, List<ItemStack> list) {
                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(blockBreakEvent.getBlock().getLocation());
                if (info != null) {
                    info.dispose();
                }
            }
        });
        addItemHandler(new BlockTicker() {
            @Override
            public boolean isSynchronized() {
                return false;
            }

            @Override
            public void tick(Block block, SlimefunItem item, SlimefunBlockData data) {
                NetworkInfo info = SlimeAEPlugin.getNetworkData().refreshNetwork(block.getLocation());
                if (info != null) {
                    info.getChildren().forEach(x -> {
                        SlimefunBlockData blockData = Slimefun.getDatabaseManager().getBlockDataController().getBlockData(x);
                        ((IMEObject<?>) SlimefunItem.getById(blockData.getSfId())).onNetworkUpdate(x.getBlock(), info);
                    });
                }
            }
        });
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {

    }
}
