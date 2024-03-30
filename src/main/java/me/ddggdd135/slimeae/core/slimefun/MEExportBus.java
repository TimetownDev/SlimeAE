package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.interfaces.MEBus;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.*;
import org.bukkit.inventory.ItemStack;

public class MEExportBus extends MEBus {

    public MEExportBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
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

    private void onExport(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        Set<BlockFace> exportFaces = new HashSet<>(Valid_Faces);
        BlockFace current = getDirection(inv);

        exportFaces.remove(current);
        exportFaces.remove(BlockFace.SELF);

        IStorage storage = getStorage(block.getRelative(current));

        if (storage == null) return;

        for (BlockFace face : exportFaces) {
            Block exportBlock = block.getRelative(face);

            IStorage exportStorage = getStorage(exportBlock);

            BlockMenu exportInv = StorageCacheUtils.getMenu(exportBlock.getLocation());
            Container exportContainer =
                    exportBlock.getState() instanceof Container ? (Container) exportBlock.getState() : null;

            if (exportStorage == null) continue;

            int[] slots;
            if (exportInv != null) {
                slots = exportInv.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.INSERT);
            } else if (exportContainer != null) {
                slots = IntStream.range(0, exportContainer.getInventory().getSize())
                        .toArray();
            } else {
                continue;
            }

            storage.getStorage().forEach((itemStack, integer) -> {
                ItemRequest itemRequest = new ItemRequest(itemStack, integer);
                ItemStack[] stacks = storage.tryTakeItem(itemRequest);
                if (stacks.length == 0) return;
                if (InvUtils.fitAll(inv.getInventory(), stacks, slots)) {
                    exportStorage.pushItem(stacks);
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
        onExport(data.getLocation().getBlock());
    }
}
