package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.AutoCraftingSession;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public class MEController extends TickingBlock implements IMEController {
    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        SlimeAEPlugin.getNetworkData().AllControllers.put(block.getLocation(), (IMEController) item);
        SlimeAEPlugin.getNetworkData().AllNetworkBlocks.put(block.getLocation(), (IMEObject) item);
        if (SlimeAEPlugin.getSlimefunTickCount() % 4 != 0) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData().refreshNetwork(block.getLocation());
        if (info == null) return;
        info.getChildren().forEach(x -> {
            IMEObject slimefunItem = SlimeAEPlugin.getNetworkData().AllNetworkBlocks.get(x);
            if (slimefunItem == null) return;
            slimefunItem.onNetworkUpdate(x.getBlock(), info);
        });

        // tick autoCrafting
        Set<AutoCraftingSession> sessions = new HashSet<>(info.getCraftingSessions());
        for (AutoCraftingSession session : sessions) {
            if (!session.hasNext()) {
                info.getCraftingSessions().remove(session);
                Slimefun.runSync(() -> {
                    session.getMenu().getInventory().getViewers().forEach(HumanEntity::closeInventory);
                });
            } else session.moveNext(512);
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

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {

    }
}
