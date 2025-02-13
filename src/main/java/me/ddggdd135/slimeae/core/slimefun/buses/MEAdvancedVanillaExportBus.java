package me.ddggdd135.slimeae.core.slimefun.buses;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import java.util.Set;
import java.util.stream.IntStream;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MEAdvancedVanillaExportBus extends MEAdvancedExportBus {

    public MEAdvancedVanillaExportBus(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    public void onExport(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;

        Set<BlockFace> directions = getDirections(block.getLocation());
        if (directions.isEmpty()) return;

        IStorage networkStorage = info.getStorage();

        for (BlockFace direction : directions) {
            Block target = block.getRelative(direction);
            if (!(PaperLib.getBlockState(target, false).getState() instanceof Container container)) continue;

            for (int slot : getSettingSlots()) {
                ItemStack setting = ItemUtils.getSettingItem(blockMenu.getInventory(), slot);
                if (setting == null || setting.getType().isAir()) {
                    continue;
                }

                Inventory inventory = container.getInventory();

                int[] inputSlots = IntStream.range(0, inventory.getSize()).toArray();
                if (inputSlots == null || inputSlots.length == 0) continue;

                if (InvUtils.fits(inventory, setting, inputSlots)) {
                    ItemStack[] taken = networkStorage.tryTakeItem(new ItemRequest(setting, setting.getAmount()));
                    if (taken.length != 0) {
                        inventory.addItem(taken[0]);
                    }
                }
            }
        }
    }

    @Override
    public boolean isSynchronized() {
        return true;
    }
}
