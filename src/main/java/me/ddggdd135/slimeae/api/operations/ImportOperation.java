package me.ddggdd135.slimeae.api.operations;

import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.api.abstracts.BusTickContext;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public final class ImportOperation {

    private ImportOperation() {}

    public static void execute(
            @Nonnull BusTickContext context, @Nonnull Block target, boolean checkNetwork, boolean allowVanilla) {
        if (!context.isValid()) return;
        IStorage networkStorage = context.getNetworkStorage();

        ItemStack sourceRef = ItemUtils.getItemStack(target, checkNetwork, allowVanilla);
        if (sourceRef == null || sourceRef.getType().isAir()) return;

        int originalAmount = sourceRef.getAmount();
        ItemStack toPush = sourceRef.clone();

        networkStorage.pushItem(toPush);

        int pushed = originalAmount - toPush.getAmount();
        if (pushed > 0) {
            int newAmount = sourceRef.getAmount() - pushed;
            if (newAmount <= 0) {
                sourceRef.setAmount(0);
            } else {
                sourceRef.setAmount(newAmount);
            }
        }
    }

    public static void executeSingleDirection(
            @Nonnull BusTickContext context, boolean checkNetwork, boolean allowVanilla) {
        if (context.getDirection() == BlockFace.SELF) return;
        Block target = context.getBlock().getRelative(context.getDirection());
        execute(context, target, checkNetwork, allowVanilla);
    }

    public static void executeMultiDirection(
            @Nonnull BusTickContext context, boolean checkNetwork, boolean allowVanilla) {
        if (context.getDirections() == null || context.getDirections().isEmpty()) return;
        for (BlockFace face : context.getDirections()) {
            if (face == BlockFace.SELF) continue;
            Block target = context.getBlock().getRelative(face);
            execute(context, target, checkNetwork, allowVanilla);
        }
    }

    public static void executeChained(@Nonnull BusTickContext context, boolean checkNetwork, boolean allowVanilla) {
        if (context.getDirection() == BlockFace.SELF) return;
        Block transportBlock = context.getBlock().getRelative(context.getDirection());

        for (int i = 0; i < context.getChainDistance(); i++) {
            if (ItemUtils.getStorage(transportBlock, checkNetwork) == null) {
                transportBlock = transportBlock.getRelative(context.getDirection());
                continue;
            }
            execute(context, transportBlock, checkNetwork, allowVanilla);
            transportBlock = transportBlock.getRelative(context.getDirection());
        }
    }
}
