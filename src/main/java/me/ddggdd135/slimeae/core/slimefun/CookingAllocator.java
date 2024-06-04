package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.abstracts.MEBus;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.interfaces.IMECraftDevice;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CookingAllocator extends MEBus implements IMECraftDevice {
    private Map<Block, CraftingRecipe> recipeMap = new HashMap<>();
    private Set<Block> running = new HashSet<>();

    public CookingAllocator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public boolean isSynchronized() {
        return true;
    }

    @Override
    public boolean isSupport(@NotNull Block block, @NotNull CraftingRecipe recipe) {
        return recipe.getCraftType() == CraftType.COOKING;
    }

    @Override
    public boolean canStartCrafting(@NotNull Block block, @NotNull CraftingRecipe recipe) {
        if (!isSupport(block, recipe)) return false;
        if (running.contains(block)) return false;
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return false;

        block = block.getRelative(getDirection(blockMenu));
        blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (block.getBlockData().getMaterial().isAir()) return false;
        if (blockMenu != null) {
            int[] inputSlots =
                    blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.INSERT, null);

            int[] outputSlots =
                    blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.WITHDRAW, null);

            return InvUtils.fitAll(blockMenu.getInventory(), recipe.getInput(), inputSlots)
                    && InvUtils.fitAll(blockMenu.getInventory(), recipe.getOutput(), outputSlots);
        } else if (block.getState() instanceof Container container) {
            Inventory inventory = container.getInventory();
            return InvUtils.fitAll(
                    inventory,
                    recipe.getInput(),
                    IntStream.range(0, inventory.getSize()).toArray());
        }
        return false;
    }

    @Override
    public void startCrafting(@NotNull Block block, @NotNull CraftingRecipe recipe) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        ItemUtils.getStorage(block.getRelative(getDirection(blockMenu)), false, false)
                .pushItem(recipe.getInput());
        running.add(block);
        recipeMap.put(block, recipe);
    }

    @Override
    public boolean isFinished(@NotNull Block block) {
        if (!recipeMap.containsKey(block)) return false;
        if (!running.contains(block)) return false;
        if (!isSupport(block, recipeMap.get(block))) return false;
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return false;

        CraftingRecipe recipe = recipeMap.get(block);
        block = block.getRelative(getDirection(blockMenu));
        if (block.getBlockData().getMaterial().isAir()) return false;
        return ItemUtils.getStorage(block, false, false)
                .contains(ItemUtils.createRequests(ItemUtils.getAmounts(recipe.getOutput())));
    }

    @Nullable @Override
    public CraftingRecipe getFinishedCraftingRecipe(@NotNull Block block) {
        return recipeMap.getOrDefault(block, null);
    }

    @Override
    public void finishCrafting(@NotNull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        ItemUtils.getStorage(block.getRelative(getDirection(blockMenu)), false, true)
                .tryTakeItem(ItemUtils.createRequests(
                        ItemUtils.getAmounts(recipeMap.get(block).getOutput())));
        running.remove(block);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}
}
