package me.ddggdd135.slimeae.core.slimefun.buses;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import java.util.List;
import java.util.stream.IntStream;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.ISettingSlotHolder;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MEVanillaExportBus extends MEExportBus {
    public MEVanillaExportBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    public void onExport(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;

        BlockFace direction = getDirection(blockMenu);
        if (direction == BlockFace.SELF) return;

        Block target = block.getRelative(direction);
        if (!(PaperLib.getBlockState(target, false).getState() instanceof Container container)) return;

        IStorage networkStorage = info.getStorage();

        if (!ISettingSlotHolder.cache.containsKey(block.getLocation()))
            ISettingSlotHolder.updateCache(block, this, StorageCacheUtils.getBlock(block.getLocation()));
        List<Pair<ItemKey, Integer>> settings = ISettingSlotHolder.getCache(block.getLocation());
        for (int i = 0; i < getSettingSlots().length; i++) {
            Pair<ItemKey, Integer> setting = settings.get(i);

            if (setting == null) {
                continue;
            }

            ItemStack itemStack = setting.getFirstValue().getItemStack();

            Inventory inventory = container.getInventory();

            int[] inputSlots = IntStream.range(0, inventory.getSize()).toArray();
            if (inputSlots == null || inputSlots.length == 0) continue;

            if (InvUtils.fits(inventory, itemStack.asQuantity(setting.getSecondValue()), inputSlots)) {
                ItemStack[] taken = networkStorage
                        .tryTakeItem(new ItemRequest(setting.getFirstValue(), setting.getSecondValue()))
                        .toItemStacks();
                if (taken.length != 0) {
                    inventory.addItem(taken[0]);
                }
            }
        }
    }

    @Override
    public boolean isSynchronized() {
        return true;
    }
}
