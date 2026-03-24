package me.ddggdd135.slimeae.api.operations;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.abstracts.BusTickContext;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class ImportOperation {

    private ImportOperation() {}

    public static void execute(
            @Nonnull BusTickContext context,
            @Nonnull Block target,
            boolean checkNetwork,
            boolean allowVanilla,
            boolean advanced) {
        if (!context.isValid()) return;

        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(target.getLocation());
        if (checkNetwork
                && slimefunBlockData != null
                && SlimefunItem.getById(slimefunBlockData.getSfId()) instanceof IMEObject) {
            return;
        }

        BlockMenu inv = StorageCacheUtils.getMenu(target.getLocation());
        if (inv != null) {
            executeFromBlockMenu(context, inv, advanced);
        } else if (allowVanilla && PaperLib.getBlockState(target, false).getState() instanceof Container container) {
            executeFromVanillaContainer(context, container, advanced);
        }
    }

    private static void executeFromBlockMenu(
            @Nonnull BusTickContext context, @Nonnull BlockMenu inv, boolean advanced) {
        IStorage networkStorage = context.getNetworkStorage();
        int tickMultiplier = context.getTickMultiplier();

        int[] outputSlots;
        try {
            outputSlots = inv.getPreset().getSlotsAccessedByItemTransport(inv, ItemTransportFlow.WITHDRAW, null);
        } catch (IllegalArgumentException e) {
            outputSlots = inv.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
        }
        if (outputSlots == null || outputSlots.length == 0) return;

        if (advanced) {
            int budget = 0;
            for (int slot : outputSlots) {
                ItemStack item = inv.getItemInSlot(slot);
                if (item != null && !item.getType().isAir()) {
                    budget = item.getMaxStackSize() * tickMultiplier;
                    break;
                }
            }
            if (budget <= 0) return;

            int totalPushed = 0;
            for (int slot : outputSlots) {
                if (totalPushed >= budget) break;
                ItemStack sourceRef = inv.getItemInSlot(slot);
                if (sourceRef == null || sourceRef.getType().isAir()) continue;

                int toImport = Math.min(sourceRef.getAmount(), budget - totalPushed);
                ItemStack toPush = sourceRef.asQuantity(toImport);
                networkStorage.pushItem(toPush);
                int pushed = toImport - toPush.getAmount();
                if (pushed > 0) {
                    sourceRef.setAmount(sourceRef.getAmount() - pushed);
                    totalPushed += pushed;
                }
                if (toPush.getAmount() > 0) break;
            }
        } else {
            for (int slot : outputSlots) {
                ItemStack sourceRef = inv.getItemInSlot(slot);
                if (sourceRef == null || sourceRef.getType().isAir()) continue;

                int toImport = Math.min(sourceRef.getAmount(), tickMultiplier);
                ItemStack toPush = sourceRef.asQuantity(toImport);
                networkStorage.pushItem(toPush);
                int pushed = toImport - toPush.getAmount();
                if (pushed > 0) {
                    sourceRef.setAmount(sourceRef.getAmount() - pushed);
                }
                return;
            }
        }
    }

    private static void executeFromVanillaContainer(
            @Nonnull BusTickContext context, @Nonnull Container container, boolean advanced) {
        IStorage networkStorage = context.getNetworkStorage();
        int tickMultiplier = context.getTickMultiplier();

        if (container instanceof Furnace furnace) {
            FurnaceInventory fi = furnace.getInventory();
            ItemStack result = fi.getResult();
            if (result == null || result.getType().isAir()) return;
            int maxImport = advanced ? result.getMaxStackSize() * tickMultiplier : tickMultiplier;
            int toImport = Math.min(result.getAmount(), maxImport);
            ItemStack toPush = result.asQuantity(toImport);
            networkStorage.pushItem(toPush);
            int pushed = toImport - toPush.getAmount();
            if (pushed > 0) {
                result.setAmount(result.getAmount() - pushed);
            }
            return;
        }

        Inventory inventory = container.getInventory();
        int size = inventory.getSize();
        if (size == 0) return;

        if (advanced) {
            int budget = 0;
            for (int i = 0; i < size; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && !item.getType().isAir()) {
                    budget = item.getMaxStackSize() * tickMultiplier;
                    break;
                }
            }
            if (budget <= 0) return;

            int totalPushed = 0;
            for (int i = 0; i < size; i++) {
                if (totalPushed >= budget) break;
                ItemStack sourceRef = inventory.getItem(i);
                if (sourceRef == null || sourceRef.getType().isAir()) continue;

                int toImport = Math.min(sourceRef.getAmount(), budget - totalPushed);
                ItemStack toPush = sourceRef.asQuantity(toImport);
                networkStorage.pushItem(toPush);
                int pushed = toImport - toPush.getAmount();
                if (pushed > 0) {
                    sourceRef.setAmount(sourceRef.getAmount() - pushed);
                    totalPushed += pushed;
                }
                if (toPush.getAmount() > 0) break;
            }
        } else {
            for (int i = 0; i < size; i++) {
                ItemStack sourceRef = inventory.getItem(i);
                if (sourceRef == null || sourceRef.getType().isAir()) continue;

                int toImport = Math.min(sourceRef.getAmount(), tickMultiplier);
                ItemStack toPush = sourceRef.asQuantity(toImport);
                networkStorage.pushItem(toPush);
                int pushed = toImport - toPush.getAmount();
                if (pushed > 0) {
                    sourceRef.setAmount(sourceRef.getAmount() - pushed);
                }
                return;
            }
        }
    }

    public static void executeSingleDirection(
            @Nonnull BusTickContext context, boolean checkNetwork, boolean allowVanilla) {
        if (context.getDirection() == BlockFace.SELF) return;
        Block target = context.getBlock().getRelative(context.getDirection());
        execute(context, target, checkNetwork, allowVanilla, false);
    }

    public static void executeMultiDirection(
            @Nonnull BusTickContext context, boolean checkNetwork, boolean allowVanilla) {
        if (context.getDirections() == null || context.getDirections().isEmpty()) return;
        for (BlockFace face : context.getDirections()) {
            if (face == BlockFace.SELF) continue;
            Block target = context.getBlock().getRelative(face);
            execute(context, target, checkNetwork, allowVanilla, true);
        }
    }

    public static void executeChained(@Nonnull BusTickContext context, boolean checkNetwork, boolean allowVanilla) {
        if (context.getDirection() == BlockFace.SELF) return;
        if (!context.isValid()) return;

        BlockFace dir = context.getDirection();
        int dx = dir.getModX();
        int dy = dir.getModY();
        int dz = dir.getModZ();
        Block origin = context.getBlock();
        World world = origin.getWorld();
        int bx = origin.getX() + dx;
        int by = origin.getY() + dy;
        int bz = origin.getZ() + dz;
        int chainDist = context.getChainDistance();

        for (int i = 0; i < chainDist; i++) {
            Block target = world.getBlockAt(bx + dx * i, by + dy * i, bz + dz * i);
            execute(context, target, checkNetwork, allowVanilla, true);
        }
    }
}
