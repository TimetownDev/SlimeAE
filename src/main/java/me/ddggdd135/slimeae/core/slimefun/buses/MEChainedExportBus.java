package me.ddggdd135.slimeae.core.slimefun.buses;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.BusTickContext;
import me.ddggdd135.slimeae.api.abstracts.MEChainedBus;
import me.ddggdd135.slimeae.api.blockdata.MEChainedExportBusData;
import me.ddggdd135.slimeae.api.blockdata.MEChainedExportBusDataAdapter;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import me.ddggdd135.slimeae.api.interfaces.ISettingSlotHolder;
import me.ddggdd135.slimeae.api.operations.ExportOperation;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MEChainedExportBus extends MEChainedBus implements ISettingSlotHolder {
    private static final Set<Location> PENDING_SYNC_LOCATIONS = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean DRAIN_SCHEDULED = new AtomicBoolean(false);
    private final MEChainedExportBusDataAdapter adapter = new MEChainedExportBusDataAdapter();
    private static final int[] SETTING_SLOTS = {3, 4, 5, 12, 13, 14, 21, 22, 23};
    private static final int[] BORDER_SLOTS = {
        0, 1, 2, 6, 7, 8, 9, 10, 11, 15, 16, 17, 18, 19, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 48, 49, 50, 51, 52, 53
    };

    public MEChainedExportBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block b) {
                cleanupCaches(b.getLocation());
                ISettingSlotHolder.cache.remove(b.getLocation());
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu == null) return;

                for (int slot : getCardSlots()) {
                    ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack != null
                            && itemStack.getType() != Material.AIR
                            && !(SlimefunUtils.isItemSimilar(itemStack, MenuItems.CARD, true, false))) {
                        b.getWorld().dropItemNaturally(b.getLocation(), itemStack);
                    }
                }
            }
        });
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    public void onNetworkTimeConsumingTick(Block block, NetworkInfo networkInfo) {
        if (!MEChainedExportBus.isAsync()) {
            return;
        }

        PENDING_SYNC_LOCATIONS.add(block.getLocation());
        if (DRAIN_SCHEDULED.compareAndSet(false, true)) {
            Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), MEChainedExportBus::drainPendingSync);
        }
    }

    @Override
    public void onMEBusTick(
            @Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data, BusTickContext context) {
        ExportOperation.executeChained(context, block, this);
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
                if (!(item instanceof MEChainedExportBus bus)) {
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
                Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), MEChainedExportBus::drainPendingSync);
            }
        }
    }

    @Override
    public int getNorthSlot() {
        return 1;
    }

    @Override
    public int getSouthSlot() {
        return 19;
    }

    @Override
    public int getEastSlot() {
        return 11;
    }

    @Override
    public int getWestSlot() {
        return 9;
    }

    @Override
    public int getUpSlot() {
        return 2;
    }

    @Override
    public int getDownSlot() {
        return 20;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@Nonnull BlockMenuPreset preset) {
        super.init(preset);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        super.newInstance(menu, block);

        initSettingSlots(menu);
        ISettingSlotHolder.updateCache(block, this, StorageCacheUtils.getBlock(block.getLocation()));
    }

    @Override
    public int[] getBorderSlots() {
        return BORDER_SLOTS;
    }

    public int[] getSettingSlots() {
        return SETTING_SLOTS;
    }

    @Nullable public MEChainedExportBusData getData(@Nonnull Location location) {
        MEChainedExportBusData data = new MEChainedExportBusData();
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return null;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEChainedExportBus)) return null;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return null;

        data.setDirection(getDirection(blockMenu));
        data.setDistance(getDistance(location));

        ItemStack[] itemStacks = new ItemStack[getSettingSlots().length];
        for (int i = 0; i < getSettingSlots().length; i++) {
            int slot = getSettingSlots()[i];
            itemStacks[i] = blockMenu.getItemInSlot(slot);
        }
        data.setItemStacks(itemStacks);

        return data;
    }

    public void applyData(@Nonnull Location location, @Nullable IBlockData data) {
        if (!canApplyData(location, data)) return;
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        MEChainedExportBusData meChainedExportBusData = (MEChainedExportBusData) data;

        setDirection(blockMenu, meChainedExportBusData.getDirection());
        setDistance(location, meChainedExportBusData.getDistance());

        ItemStack[] itemStacks = meChainedExportBusData.getItemStacks();

        for (int i = 0; i < getSettingSlots().length; i++) {
            int slot = getSettingSlots()[i];
            blockMenu.replaceExistingItem(slot, itemStacks[i]);
        }
        ISettingSlotHolder.cache.remove(location);
    }

    public boolean hasData(@Nonnull Location location) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEChainedExportBus)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        return blockMenu != null;
    }

    public boolean canApplyData(@Nonnull Location location, @Nullable IBlockData blockData) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEChainedExportBus)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return false;
        return blockData instanceof MEChainedExportBusData;
    }

    @Nonnull
    public IBlockDataAdapter<?> getAdapter() {
        return adapter;
    }
}
