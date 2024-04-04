package me.ddggdd135.slimeae.api.interfaces;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Items.CMIMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class MEBus extends SlimefunItem implements IMEObject {
    protected static final Map<Location, BlockFace> SELECTED_DIRECTION_MAP = new HashMap<>();

    public int getNorthSlot() {
        return 12;
    }

    public int getSouthSlot() {
        return 30;
    }

    public int getEastSlot() {
        return 22;
    }

    public int getWestSlot() {
        return 20;
    }

    public int getUpSlot() {
        return 15;
    }

    public int getDownSlot() {
        return 33;
    }

    public int[] getBackgroundSlots() {
        return new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 16, 17, 18, 19, 21, 23, 24, 25, 26, 27, 28, 29, 31, 32, 34,
            35, 36, 37, 38, 39, 40, 41, 42, 43, 44
        };
    }

    private final BlockFace[] FACES = {
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    };

    private final String dataKey = "direction";
    private final String dataKeyOwner = "owner";

    public MEBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        addItemHandler(
                new BlockPlaceHandler(false) {
                    @Override
                    public void onPlayerPlace(@Nonnull BlockPlaceEvent event) {
                        var blockData =
                                StorageCacheUtils.getBlock(event.getBlock().getLocation());
                        if (blockData != null) {
                            blockData.setData(
                                    dataKeyOwner,
                                    event.getPlayer().getUniqueId().toString());
                            blockData.setData(dataKey, BlockFace.SELF.name());
                        }
                    }
                },
                new BlockTicker() {
                    @Override
                    public boolean isSynchronized() {
                        return true;
                    }

                    @Override
                    public void tick(Block block, SlimefunItem slimefunItem, SlimefunBlockData data) {
                        MEBus.this.tick(data);
                    }
                });
    }

    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {

            @Override
            public void init() {
                drawBackground(getBackgroundSlots());

                if (getOtherBackgroundSlots() != null && getOtherBackgroundStack() != null) {
                    drawBackground(getOtherBackgroundStack(), getOtherBackgroundSlots());
                }

                addItem(
                        getNorthSlot(),
                        getDirectionalSlotPane(BlockFace.NORTH, Material.AIR, false),
                        (player, i, itemStack, clickAction) -> false);
                addItem(
                        getSouthSlot(),
                        getDirectionalSlotPane(BlockFace.SOUTH, Material.AIR, false),
                        (player, i, itemStack, clickAction) -> false);
                addItem(
                        getEastSlot(),
                        getDirectionalSlotPane(BlockFace.EAST, Material.AIR, false),
                        (player, i, itemStack, clickAction) -> false);
                addItem(
                        getWestSlot(),
                        getDirectionalSlotPane(BlockFace.WEST, Material.AIR, false),
                        (player, i, itemStack, clickAction) -> false);
                addItem(
                        getUpSlot(),
                        getDirectionalSlotPane(BlockFace.UP, Material.AIR, false),
                        (player, i, itemStack, clickAction) -> false);
                addItem(
                        getDownSlot(),
                        getDirectionalSlotPane(BlockFace.DOWN, Material.AIR, false),
                        (player, i, itemStack, clickAction) -> false);
            }

            @Override
            public void newInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block b) {
                final BlockFace direction;
                final String string = StorageCacheUtils.getData(blockMenu.getLocation(), dataKey);

                if (string == null) {
                    direction = BlockFace.SELF;
                    StorageCacheUtils.setData(blockMenu.getLocation(), dataKey, BlockFace.SELF.name());
                } else {
                    direction = BlockFace.valueOf(string);
                }

                SELECTED_DIRECTION_MAP.put(blockMenu.getLocation().clone(), direction);

                blockMenu.addMenuClickHandler(
                        getNorthSlot(),
                        (player, i, itemStack, clickAction) ->
                                directionClick(player, clickAction, blockMenu, BlockFace.NORTH));
                blockMenu.addMenuClickHandler(
                        getSouthSlot(),
                        (player, i, itemStack, clickAction) ->
                                directionClick(player, clickAction, blockMenu, BlockFace.SOUTH));
                blockMenu.addMenuClickHandler(
                        getEastSlot(),
                        (player, i, itemStack, clickAction) ->
                                directionClick(player, clickAction, blockMenu, BlockFace.EAST));
                blockMenu.addMenuClickHandler(
                        getWestSlot(),
                        (player, i, itemStack, clickAction) ->
                                directionClick(player, clickAction, blockMenu, BlockFace.WEST));
                blockMenu.addMenuClickHandler(
                        getUpSlot(),
                        (player, i, itemStack, clickAction) ->
                                directionClick(player, clickAction, blockMenu, BlockFace.UP));
                blockMenu.addMenuClickHandler(
                        getDownSlot(),
                        (player, i, itemStack, clickAction) ->
                                directionClick(player, clickAction, blockMenu, BlockFace.DOWN));
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return MEBus.this.canUse(player, false)
                        && Slimefun.getProtectionManager()
                                .hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) {
                    return getInputSlots();
                } else {
                    return getOutputSlots();
                }
            }
        };
    }

    public abstract int[] getInputSlots();

    public abstract int[] getOutputSlots();

    private boolean directionClick(Player player, ClickAction action, BlockMenu blockMenu, BlockFace blockFace) {
        if (action.isShiftClicked()) {
            openDirection(player, blockMenu, blockFace);
        } else {
            setDirection(blockMenu, blockFace);
        }
        return false;
    }

    @Nonnull
    public static ItemStack getDirectionalSlotPane(
            @Nonnull BlockFace blockFace, @Nonnull SlimefunItem slimefunItem, boolean active) {
        final ItemStack displayStack = new CustomItemStack(
                slimefunItem.getItem(),
                "&8设置朝向: " + blockFace.name() + " (" + CMIChatColor.stripColor(slimefunItem.getItemName()) + ")");
        final ItemMeta itemMeta = displayStack.getItemMeta();
        if (active) {
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemMeta.setLore(List.of(
                CMIChatColor.translate("{#e4ed32}左键点击: &8设置朝向"),
                CMIChatColor.translate("{#e4ed32}Shift+左键点击: &8打开目标方块")));
        displayStack.setItemMeta(itemMeta);
        return displayStack;
    }

    @Nonnull
    public static ItemStack getDirectionalSlotPane(
            @Nonnull BlockFace blockFace, @Nonnull Material blockMaterial, boolean active) {
        if (blockMaterial.isItem() && !blockMaterial.isAir()) {
            final ItemStack displayStack = new CustomItemStack(
                    blockMaterial, "&8设置朝向 " + blockFace.name() + " (" + CMIMaterial.get(blockMaterial) + ")");
            final ItemMeta itemMeta = displayStack.getItemMeta();
            if (active) {
                itemMeta.addEnchant(Enchantment.LUCK, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            itemMeta.setLore(List.of(
                    CMIChatColor.translate("{#e4ed32}左键点击: &8设置朝向"),
                    CMIChatColor.translate("{#e4ed32}Shift+左键点击: &8打开目标方块")));
            displayStack.setItemMeta(itemMeta);
            return displayStack;
        } else {
            Material material = active ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            return new CustomItemStack(material, "&8设置朝向: " + blockFace.name());
        }
    }

    @ParametersAreNonnullByDefault
    public void setDirection(BlockMenu blockMenu, BlockFace blockFace) {
        SELECTED_DIRECTION_MAP.put(blockMenu.getLocation().clone(), blockFace);
        StorageCacheUtils.setData(blockMenu.getBlock().getLocation(), dataKey, blockFace.name());
    }

    @ParametersAreNonnullByDefault
    public BlockFace getDirection(BlockMenu blockMenu) {
        if (SELECTED_DIRECTION_MAP.containsKey(blockMenu.getLocation()))
            return SELECTED_DIRECTION_MAP.get(blockMenu.getLocation());
        String blockFace = StorageCacheUtils.getData(blockMenu.getLocation(), dataKey);
        if (blockFace == null) return BlockFace.SELF;
        return BlockFace.valueOf(blockFace);
    }

    private void openDirection(Player player, BlockMenu blockMenu, BlockFace blockFace) {
        Block targetBlock = blockMenu.getBlock().getRelative(blockFace);
        final BlockMenu targetMenu = StorageCacheUtils.getMenu(targetBlock.getLocation());
        final boolean hasPermission = Slimefun.getProtectionManager()
                .hasPermission(player, targetBlock.getLocation(), Interaction.INTERACT_BLOCK);
        if (targetMenu != null) {
            final Location location = targetMenu.getLocation();
            final SlimefunItem item = StorageCacheUtils.getSfItem(location);
            if (item.canUse(player, true) && hasPermission) {
                targetMenu.open(player);
            }
        } else if (targetBlock.getState() instanceof Container container && hasPermission) {
            player.openInventory(container.getInventory());
        }
    }

    private void updateGui(SlimefunBlockData data) {
        BlockMenu blockMenu = data.getBlockMenu();
        if (blockMenu == null || !blockMenu.hasViewer()) {
            return;
        }

        BlockFace direction;
        try {
            direction = BlockFace.valueOf(data.getData(dataKey));
        } catch (IllegalArgumentException e) {
            direction = BlockFace.SELF;
        }

        for (BlockFace blockFace : FACES) {
            final Block block = blockMenu.getBlock().getRelative(blockFace);
            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(block.getLocation());
            if (slimefunItem != null) {
                switch (blockFace) {
                    case NORTH -> blockMenu.replaceExistingItem(
                            getNorthSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                    case SOUTH -> blockMenu.replaceExistingItem(
                            getSouthSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                    case EAST -> blockMenu.replaceExistingItem(
                            getEastSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                    case WEST -> blockMenu.replaceExistingItem(
                            getWestSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                    case UP -> blockMenu.replaceExistingItem(
                            getUpSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                    case DOWN -> blockMenu.replaceExistingItem(
                            getDownSlot(), getDirectionalSlotPane(blockFace, slimefunItem, blockFace == direction));
                    default -> throw new IllegalStateException("Unexpected value: " + blockFace);
                }
            } else {
                final Material material = block.getType();
                switch (blockFace) {
                    case NORTH -> blockMenu.replaceExistingItem(
                            getNorthSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                    case SOUTH -> blockMenu.replaceExistingItem(
                            getSouthSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                    case EAST -> blockMenu.replaceExistingItem(
                            getEastSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                    case WEST -> blockMenu.replaceExistingItem(
                            getWestSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                    case UP -> blockMenu.replaceExistingItem(
                            getUpSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                    case DOWN -> blockMenu.replaceExistingItem(
                            getDownSlot(), getDirectionalSlotPane(blockFace, material, blockFace == direction));
                    default -> throw new IllegalStateException("Unexpected value: " + blockFace);
                }
            }
        }
    }

    protected @Nullable int[] getOtherBackgroundSlots() {
        return null;
    }

    @Nullable protected CustomItemStack getOtherBackgroundStack() {
        return null;
    }

    @OverridingMethodsMustInvokeSuper
    protected void tick(SlimefunBlockData data) {
        // TODO
        if (data.getBlockMenu().hasViewer()) updateGui(data);
    }
}
