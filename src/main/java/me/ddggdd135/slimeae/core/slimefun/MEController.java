package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.abstracts.TicingBlock;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.AutoCraftingSession;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.block.Block;
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
            // tick autoCrafting
            Set<AutoCraftingSession> sessions = new HashSet<>(info.getCraftingSessions());
            for (AutoCraftingSession session : sessions) {
                if (!session.hasNext()) info.getCraftingSessions().remove(session);
                else session.moveNext(8);
            }
            info.updateAutoCraftingMenu();
        }
    }

    public MEController(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block block) {
                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                if (info != null) {
                    info.dispose();
                }
            }
        });
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}
}
