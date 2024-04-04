package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkData;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.InventoryBlock;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Map;

public class METerminal extends SlimefunItem implements InventoryBlock {
    public int[] getBackgroundSlots() {
        return new int[] {
                17, 26
        };
    }

    public int[] getDisplaySlots() {
        return new int[] {
                0, 1, 2, 3, 4, 5, 6, 7,
                9, 10, 11, 12, 13, 14, 15, 16,
                18, 19, 20, 21, 22, 23, 24, 25,
                27, 28, 29, 30, 31, 32, 33, 34,
                36, 37, 38, 39, 40, 41, 42, 43,
                45, 46, 47, 48, 49, 50, 51, 52
        };
    }

    public int getInputSlot() {
        return 8;
    }

    public int getChangeSort() {
        return 26;
    }

    public int getFilter() {
        return 35;
    }

    public int getPagePrevious() {
        return 44;
    }

    public int getPageNext() {
        return 53;
    }

    public METerminal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this, this.getItem().getItemMeta().getDisplayName(), this::constructMenu);
    }

    @OverridingMethodsMustInvokeSuper
    protected void constructMenu(BlockMenuPreset preset) {
        for (int slot : getBackgroundSlots()) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

    }

    @Override
    public int[] getInputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    public void updateGui(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;
        IStorage networkStorage = info.getStorage();
        Map<ItemStack, Integer> storage = networkStorage.getStorage();
        for (int slot : getDisplaySlots()) {

        }
    }
}
