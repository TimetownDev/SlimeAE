package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.interfaces.MEBus;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.*;
import org.bukkit.inventory.ItemStack;

public class MEImportBus extends MEBus {

    public MEImportBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public int[] getInputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    private void onImport(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;
        BlockFace current = getDirection(inv);
        if (current == BlockFace.SELF) return;
        Block transportBlock = block.getRelative(current);
        IStorage storage = ItemUtils.getStorage(transportBlock);
        if (storage == null) return;
        IStorage networkStorage = info.getStorage();
        while (true) {
            ItemStack itemStack = ItemUtils.getItemStack(transportBlock);
            if (itemStack == null || itemStack.getType().isAir()) break;
            networkStorage.pushItem(itemStack);
            if (!(itemStack.getType().isAir())) break;
        }
    }

    @Override
    public void tick(SlimefunBlockData data) {
        super.tick(data);
        onImport(data.getLocation().getBlock());
    }
}
