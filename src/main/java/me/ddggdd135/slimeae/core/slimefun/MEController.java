package me.ddggdd135.slimeae.core.slimefun;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.AutoCraftingSession;
import me.ddggdd135.slimeae.core.NetworkInfo;

public class MEController extends TickingBlock implements IMEController {
    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        NetworkInfo info = null;
        if (SlimeAEPlugin.getSlimefunTickCount() % 32 == 0) {
            info = SlimeAEPlugin.getNetworkData().refreshNetwork(block.getLocation());
            if (info == null) return;
            NetworkInfo finalInfo = info;
            info.getChildren().forEach(x -> {
                SlimefunBlockData blockData = StorageCacheUtils.getBlock(x);
                ((IMEObject) SlimefunItem.getById(blockData.getSfId())).onNetworkUpdate(x.getBlock(), finalInfo);
            });
        }
        if (info == null) info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;
        // tick autoCrafting
        Set<AutoCraftingSession> sessions = new HashSet<>(info.getCraftingSessions());
        for (AutoCraftingSession session : sessions) {
            if (!session.hasNext()) info.getCraftingSessions().remove(session);
            else session.moveNext(8);
        }
        info.updateAutoCraftingMenu();
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
