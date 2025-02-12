package me.ddggdd135.slimeae.api.abstracts;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public abstract class AdvancedMEBus extends MEBus {

    protected static final String DATA_KEY_DIRECTIONS = "directions";

    public AdvancedMEBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent event) {
                var block = event.getBlock();
                Location loc = block.getLocation();
                var blockData = StorageCacheUtils.getBlock(loc);
                if (blockData != null) {
                    Set<BlockFace> directions = new ConcurrentSkipListSet<>();
                    directions.add(BlockFace.SELF);
                    saveDirections(loc, directions);
                }
            }
        });
        addItemHandler(new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block b) {
                Location loc = b.getLocation();
                StorageCacheUtils.removeData(loc, DATA_KEY_DIRECTIONS);
                MULTI_DIRECTION_MAP.remove(loc);
                BlockMenu blockMenu = StorageCacheUtils.getMenu(loc);
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
    public void setDirection(BlockMenu blockMenu, @Nonnull BlockFace blockFace) {
        Location loc = blockMenu.getLocation().clone();
        Set<BlockFace> directions = getDirections(loc);
        if (directions.contains(blockFace)) {
            directions.remove(blockFace);
        } else {
            directions.add(blockFace);
        }
        saveDirections(loc, directions);
        updateGui(StorageCacheUtils.getBlock(loc));
    }

    @Override
    @Deprecated
    public BlockFace getDirection(BlockMenu blockMenu) {
        return getDirections(blockMenu.getLocation()).stream().findFirst().orElse(BlockFace.SELF);
    }

    public Set<BlockFace> getDirections(Location loc) {
        return MULTI_DIRECTION_MAP.getOrDefault(loc, Collections.singleton(BlockFace.SELF));
    }

    private void saveDirections(Location loc, Set<BlockFace> directions) {
        String dirString = String.join(",", directions.stream().map(Enum::name).toArray(String[]::new));
        StorageCacheUtils.setData(loc, DATA_KEY_DIRECTIONS, dirString);
        Set<BlockFace> concurrentSet = ConcurrentHashMap.newKeySet();
        concurrentSet.addAll(directions);
        MULTI_DIRECTION_MAP.put(loc, concurrentSet);
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        Location loc = menu.getLocation().clone();
        super.newInstance(menu, block);

        Set<BlockFace> directions = getDirections(loc);
        if (directions.isEmpty()) {
            directions.add(BlockFace.SELF);
            saveDirections(loc, directions);
        }

        updateGui(StorageCacheUtils.getBlock(loc));
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        Location loc = block.getLocation();
        if (!MULTI_DIRECTION_MAP.containsKey(loc)) {
            // Set<BlockFace> directions = ConcurrentHashMap.newKeySet();
            Set<BlockFace> directions = new ConcurrentSkipListSet<>();
            String dirData = StorageCacheUtils.getData(loc, DATA_KEY_DIRECTIONS);
            if (dirData != null && !dirData.isEmpty()) {
                for (String face : dirData.split(",")) {
                    if (face.trim().isEmpty()) continue;
                    BlockFace blockFace = BlockFace.valueOf(face.trim());
                    directions.add(blockFace);
                }
            }
            if (directions.isEmpty()) {
                directions.add(BlockFace.SELF);
            }
            MULTI_DIRECTION_MAP.put(loc, directions);
        }
        super.tick(block, item, data);
    }

    @Override
    protected void updateGui(SlimefunBlockData data) {
        BlockMenu blockMenu = data.getBlockMenu();
        if (blockMenu == null || !blockMenu.hasViewer()) return;

        Set<BlockFace> directions = getDirections(blockMenu.getLocation());

        for (BlockFace blockFace : FACES) {
            Block targetBlock = blockMenu.getBlock().getRelative(blockFace);
            SlimefunItem sfItem = StorageCacheUtils.getSfItem(targetBlock.getLocation());
            boolean isActive = directions.contains(blockFace);

            ItemStack displayItem;
            if (sfItem != null) {
                displayItem = getDirectionalSlotPane(blockFace, sfItem, isActive);
            } else {
                Material material = targetBlock.getType();
                displayItem = getDirectionalSlotPane(blockFace, material, isActive);
            }

            switch (blockFace) {
                case NORTH -> blockMenu.replaceExistingItem(getNorthSlot(), displayItem);
                case SOUTH -> blockMenu.replaceExistingItem(getSouthSlot(), displayItem);
                case EAST -> blockMenu.replaceExistingItem(getEastSlot(), displayItem);
                case WEST -> blockMenu.replaceExistingItem(getWestSlot(), displayItem);
                case UP -> blockMenu.replaceExistingItem(getUpSlot(), displayItem);
                case DOWN -> blockMenu.replaceExistingItem(getDownSlot(), displayItem);
            }
        }
    }

    protected static final Map<Location, Set<BlockFace>> MULTI_DIRECTION_MAP = new ConcurrentHashMap<>();
    private final BlockFace[] FACES = {
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    };
}
