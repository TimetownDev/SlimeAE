package me.ddggdd135.slimeae.core.slimefun.buses;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.BusTickContext;
import me.ddggdd135.slimeae.api.abstracts.MEChainedBus;
import me.ddggdd135.slimeae.api.operations.ImportOperation;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MEChainedImportBus extends MEChainedBus {
    private static final Set<Location> PENDING_SYNC_LOCATIONS = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean DRAIN_SCHEDULED = new AtomicBoolean(false);

    @Override
    public boolean isSynchronized() {
        return false;
    }

    public MEChainedImportBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public void onNetworkTimeConsumingTick(Block block, NetworkInfo networkInfo) {
        if (!MEChainedImportBus.isAsync()) {
            return;
        }

        PENDING_SYNC_LOCATIONS.add(block.getLocation());
        if (DRAIN_SCHEDULED.compareAndSet(false, true)) {
            Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), MEChainedImportBus::drainPendingSync);
        }
    }

    @Override
    public void onMEBusTick(
            @Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data, BusTickContext context) {
        ImportOperation.executeChained(context, true, false);
    }

    private static void drainPendingSync() {
        try {
            var locations = new ArrayList<>(PENDING_SYNC_LOCATIONS);
            PENDING_SYNC_LOCATIONS.clear();

            for (Location location : locations) {
                SlimefunBlockData data = StorageCacheUtils.getBlock(location);
                if (data == null) {
                    continue;
                }

                SlimefunItem item = SlimefunItem.getById(data.getSfId());
                if (!(item instanceof MEChainedImportBus bus)) {
                    continue;
                }

                BlockMenu blockMenu = data.getBlockMenu();
                if (blockMenu == null) {
                    continue;
                }

                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(location);
                if (info == null) {
                    continue;
                }

                Block block = location.getBlock();
                int totalMultiplier = bus.computeAccelerationMultiplier(block, bus, data);
                BusTickContext context = new BusTickContext.Builder()
                        .block(block)
                        .blockMenu(blockMenu)
                        .networkInfo(info)
                        .direction(bus.getDirection(blockMenu))
                        .chainDistance(bus.getDistance(location))
                        .tickMultiplier(totalMultiplier)
                        .build();
                bus.onMEBusTick(block, bus, data, context);
            }
        } finally {
            DRAIN_SCHEDULED.set(false);
            if (!PENDING_SYNC_LOCATIONS.isEmpty() && DRAIN_SCHEDULED.compareAndSet(false, true)) {
                Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), MEChainedImportBus::drainPendingSync);
            }
        }
    }
}
