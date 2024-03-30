package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.interfaces.MEBus;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

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
        Set<BlockFace> importFaces = new HashSet<>(Valid_Faces);
        BlockFace current = getDirection(inv);

        importFaces.remove(current);
        importFaces.remove(BlockFace.SELF);

        Block transportBlock = block.getRelative(current);

        // transport to
        IStorage storage = getStorage(transportBlock);
        BlockMenu transportInv = StorageCacheUtils.getMenu(transportBlock.getLocation());

        if (storage == null) return;

        Inventory inventory;
        if (transportBlock.getState() instanceof Container) {
            inventory = ((Container) transportBlock.getState()).getInventory();
        } else if (transportInv != null) {
            inventory = transportInv.getInventory();
        } else return;

        for (BlockFace face : importFaces) {
            Block importBlock = block.getRelative(face);

            IStorage importStorage = getStorage(importBlock);

            BlockMenu importInv = StorageCacheUtils.getMenu(importBlock.getLocation());
            Container importContainer =
                    importBlock.getState() instanceof Container ? (Container) importBlock.getState() : null;

            if (importStorage == null) continue;

            int[] slots;
            if (importInv != null) {
                slots = importInv.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.INSERT);
            } else if (importContainer != null) {
                slots = IntStream.range(0, importContainer.getInventory().getSize())
                        .toArray();
            } else {
                continue;
            }

            importStorage.getStorage().forEach((itemStack, integer) -> {
                ItemRequest itemRequest = new ItemRequest(itemStack, integer);
                ItemStack[] stacks = importStorage.tryTakeItem(itemRequest);
                if (stacks.length == 0) return;
                if (InvUtils.fitAll(inventory, stacks, slots)) {
                    storage.pushItem(stacks);
                }
            });
        }
    }

    @Nullable public IStorage getStorage(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        BlockFace blockFace = getDirection(inv);
        if (blockFace == BlockFace.SELF) return null;
        Block b = block.getRelative(blockFace);
        if (b.getBlockData().getMaterial().isAir()) return null;
        return ItemUtils.getStorage(b);
    }

    @Override
    public void tick(SlimefunBlockData data) {
        super.tick(data);
        onImport(data.getLocation().getBlock());
    }
}
