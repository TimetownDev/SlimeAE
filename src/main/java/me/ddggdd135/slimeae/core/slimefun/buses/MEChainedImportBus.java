package me.ddggdd135.slimeae.core.slimefun.buses;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.ChainedMEBus;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class MEChainedImportBus extends ChainedMEBus {
    @Override
    public boolean isSynchronized() {
        return false;
    }

    public MEChainedImportBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    public void onImport(@Nonnull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;
        BlockFace current = getDirection(blockMenu);
        if (current == BlockFace.SELF) return;

        int distance = getDistance(block.getLocation());
        Block transportBlock = block.getRelative(current);

        for (int i = 0; i < distance; i++) {
            IStorage networkStorage = info.getStorage();
            if (ItemUtils.getStorage(transportBlock) == null) return;

            ItemStack itemStack = ItemUtils.getItemStack(transportBlock);
            if (itemStack == null || itemStack.getType().isAir()) continue;

            networkStorage.pushItem(itemStack);
            transportBlock = transportBlock.getRelative(current);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onMEBusTick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        onImport(data.getLocation().getBlock());
    }
}
