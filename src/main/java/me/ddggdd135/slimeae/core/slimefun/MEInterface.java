package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.abstracts.TicingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MEInterface extends TicingBlock implements IMECraftHolder, InventoryBlock {
    public static final int[] Boarder_Slots =
            new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 27, 28, 29, 30, 31, 32, 33, 34, 35, 45, 46, 47, 48, 49, 50, 51, 52, 53
            };
    public static final int[] Setting_Slots = new int[] {9, 10, 11, 12, 13, 14, 15, 16, 17};
    public static final int[] Item_Slots = new int[] {18, 19, 20, 21, 22, 23, 24, 25, 26};
    public static final int[] Pattern_Slots = new int[] {
        36, 37, 38, 39, 40, 41, 42, 43, 44,
    };
    public static final BlockFace[] VaildBlockFace = new BlockFace[] {
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN
    };

    @Override
    public boolean isSynchronized() {
        return true;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;
        IStorage networkStorage = info.getStorage();
        for (int slot : Item_Slots) {
            int settingSlot = slot - 9;
            ItemStack setting = ItemUtils.getSettingItem(inv.getInventory(), settingSlot);
            ItemStack itemStack = inv.getItemInSlot(slot);
            if (SlimefunUtils.isItemSimilar(setting, MenuItems.Setting, true, false)) {
                if (itemStack != null
                        && !itemStack.getType().isAir()
                        && !SlimefunUtils.isItemSimilar(setting, itemStack, true, false))
                    networkStorage.pushItem(itemStack);
                continue;
            }

            int amount = 0;
            if (itemStack != null && !itemStack.getType().isAir()) amount = itemStack.getAmount();
            if (amount == setting.getAmount()) continue; // 节约性能
            if (amount > setting.getAmount()) {
                ItemStack toPush = itemStack.clone();
                toPush.setAmount(amount - setting.getAmount());
                networkStorage.pushItem(toPush);
                itemStack.setAmount(setting.getAmount() + toPush.getAmount());
                continue;
            }

            ItemStack[] received = networkStorage.tryTakeItem(new ItemRequest(setting, setting.getAmount() - amount));
            if (received.length != 0) {
                if (itemStack != null && !itemStack.getType().isAir())
                    itemStack.setAmount(amount + received[0].getAmount());
                else inv.replaceExistingItem(slot, received[0]);
            }
        }
    }

    public MEInterface(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());

                if (inv != null) {
                    inv.dropItems(b.getLocation(), Item_Slots);
                }
            }
        };
    }

    @Override
    public int[] getInputSlots() {
        return Item_Slots;
    }

    @Override
    public int[] getOutputSlots() {
        return Item_Slots;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@NotNull BlockMenuPreset preset) {
        preset.drawBackground(Boarder_Slots);
        for (int slot : Setting_Slots) {
            preset.addMenuClickHandler(slot, ItemUtils.getSettingSlotClickHandler());
        }
        for (int slot : Pattern_Slots) {
            preset.addMenuClickHandler(slot, ItemUtils.getPatternSlotClickHandler());
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@NotNull BlockMenu menu, @NotNull Block block) {
        for (int slot : Setting_Slots) {
            if (menu.getItemInSlot(slot) == null
                    || menu.getItemInSlot(slot).getType().isAir())
                ItemUtils.setSettingItem(menu.getInventory(), slot, MenuItems.Setting);
        }
        for (int slot : Pattern_Slots) {
            if (menu.getItemInSlot(slot) == null
                    || menu.getItemInSlot(slot).getType().isAir()) menu.replaceExistingItem(slot, MenuItems.Pattern);
        }
    }

    @Nonnull
    @Override
    public Block[] getCraftingDevices(@Nonnull Block block) {
        List<Block> result = new ArrayList<>();
        for (BlockFace blockFace : VaildBlockFace) {
            Block relative = block.getRelative(blockFace);
            SlimefunBlockData blockData =
                    Slimefun.getDatabaseManager().getBlockDataController().getBlockData(relative.getLocation());
            if (blockData == null) continue;
            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof IMECraftDevice) result.add(relative);
        }
        return result.toArray(Block[]::new);
    }

    @Nonnull
    @Override
    public CraftingRecipe[] getSupportedRecipes(@Nonnull Block block) {
        Set<CraftingRecipe> result = new HashSet<>();
        Block[] devices = getCraftingDevices(block);
        for (Block device : devices) {
            SlimefunBlockData blockData =
                    Slimefun.getDatabaseManager().getBlockDataController().getBlockData(device.getLocation());
            if (blockData == null) continue;
            SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
            if (slimefunItem instanceof IMECraftDevice IMECraftDevice) {
                for (CraftingRecipe recipe : getRecipes(block)) {
                    if (result.contains(recipe)) continue;
                    if (IMECraftDevice.isSupport(device, recipe)) result.add(recipe);
                }
            }
        }
        return result.toArray(CraftingRecipe[]::new);
    }

    @Nonnull
    public CraftingRecipe[] getRecipes(@Nonnull Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return new CraftingRecipe[0];
        Set<CraftingRecipe> result = new HashSet<>();
        for (int slot : Pattern_Slots) {
            ItemStack patternItem = inv.getItemInSlot(slot);
            if (patternItem == null || patternItem.getType().isAir()) continue;
            SlimefunItem slimefunItem = SlimefunItem.getByItem(patternItem);
            if (!(slimefunItem instanceof Pattern)) continue;
            CraftingRecipe recipe = Pattern.getRecipe(patternItem);
            if (recipe == null) continue;
            result.add(recipe);
        }
        return result.toArray(CraftingRecipe[]::new);
    }
}
