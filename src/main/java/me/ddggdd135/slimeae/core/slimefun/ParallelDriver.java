package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class ParallelDriver extends SlimefunItem implements IMEObject, InventoryBlock {

    public ParallelDriver(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}

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

    public int[] getProcessorSlots() {
        return new int[] {10, 11, 12, 19, 20, 21};
    }

    public int[] getBorderSlots() {
        return new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 14, 15, 16, 17, 18, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35
        };
    }

    @Override
    public void init(@Nonnull BlockMenuPreset preset) {
        for (int slot : getBorderSlots()) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        for (int slot : getProcessorSlots()) {
            menu.addMenuClickHandler(slot, (player, i, cursor, clickAction) -> {
                Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), () -> {
                    NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                    if (networkInfo != null) {
                        networkInfo.setNeedsRecipeUpdate(true);
                    }
                });
                return true;
            });
        }
    }

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu != null) {
                    blockMenu.dropItems(b.getLocation(), getProcessorSlots());
                }
            }
        };
    }

    public int getProcessorCount(@Nonnull Block block) {
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if (menu == null) return 0;
        int count = 0;
        for (int slot : getProcessorSlots()) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item != null && !item.getType().isAir()) {
                SlimefunItem sfItem = SlimefunItem.getByItem(item);
                if (sfItem != null && sfItem.getId().equals("PARALLEL_PROCESSOR")) {
                    count += item.getAmount();
                }
            }
        }
        return count;
    }
}
