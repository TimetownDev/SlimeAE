package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import java.util.List;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.TicingBlock;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class MEController extends TicingBlock implements IMEController {
    @Override
    public boolean isSynchronized() {
        return true;
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        NetworkInfo info = SlimeAEPlugin.getNetworkData().refreshNetwork(block.getLocation());
        if (info != null) {
            info.getChildren().forEach(x -> {
                SlimefunBlockData blockData = StorageCacheUtils.getBlock(x);
                ((IMEObject) SlimefunItem.getById(blockData.getSfId())).onNetworkUpdate(x.getBlock(), info);
            });
        }
    }

    public MEController(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(new BlockBreakHandler(true, true) {
            @Override
            public void onPlayerBreak(
                    @Nonnull BlockBreakEvent blockBreakEvent,
                    @Nonnull ItemStack itemStack,
                    @Nonnull List<ItemStack> list) {
                NetworkInfo info = SlimeAEPlugin.getNetworkData()
                        .getNetworkInfo(blockBreakEvent.getBlock().getLocation());
                if (info != null) {
                    info.dispose();
                }
            }
        });
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}
}
