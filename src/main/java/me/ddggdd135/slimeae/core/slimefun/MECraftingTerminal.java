package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MECraftingTerminal extends METerminal {
    public MECraftingTerminal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public int[] getBackgroundSlots() {
        return new int[] {0, 1, 3, 4, 5, 14, 23, 32, 33, 34, 35, 41, 42, 44, 45, 47, 49, 50, 51, 52, 53};
    }

    @Override
    public int[] getDisplaySlots() {
        return new int[] {9, 10, 11, 12, 13, 18, 19, 20, 21, 22, 27, 28, 29, 30, 31, 36, 37, 38, 39, 40};
    }

    @Override
    public int getInputSlot() {
        return 2;
    }

    @Override
    public int getChangeSort() {
        return 47;
    }

    @Override
    public int getFilter() {
        return 45;
    }

    @Override
    public int getPagePrevious() {
        return 46;
    }

    @Override
    public int getPageNext() {
        return 48;
    }

    public int[] getCraftSlots() {
        return new int[] {6, 7, 8, 15, 16, 17, 24, 25, 26};
    }

    public int getCraftOutputSlot() {
        return 43;
    }

    public int getReturnItemSlot() {
        return 52;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@NotNull BlockMenuPreset preset) {
        super.init(preset);
        preset.addItem(getReturnItemSlot(), MenuItems.PUSH_BACK);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void newInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        super.newInstance(blockMenu, block);
        blockMenu.replaceExistingItem(getCraftOutputSlot(), MenuItems.Empty);
        blockMenu.addMenuClickHandler(getCraftOutputSlot(), new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent inventoryClickEvent,
                    Player player,
                    int i,
                    ItemStack cursor,
                    ClickAction clickAction) {
                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                if (info == null) return false;
                ItemStack matched = matchItem(block);
                if (matched == null || matched.getType().isAir()) return false;
                if (cursor.getType().isAir() || SlimefunUtils.isItemSimilar(matched, cursor, true, false)) {
                    if (inventoryClickEvent.isShiftClick()) {
                        Inventory playerInventory = player.getInventory();
                        while (matched != null
                                && !matched.getType().isAir()
                                && InvUtils.fits(
                                        playerInventory,
                                        matched,
                                        IntStream.range(0, playerInventory.getSize())
                                                .toArray())) {
                            doCraft(block);
                            playerInventory.addItem(matched);
                            matched = matchItem(block);
                        }
                        updateCraftingGui(block);
                    } else if (inventoryClickEvent.isLeftClick()
                            && cursor.getAmount() + matched.getAmount() <= matched.getMaxStackSize()) {
                        if (cursor.getType().isAir()
                                || (SlimefunUtils.isItemSimilar(matched, cursor, true, false)
                                        && cursor.getAmount() + matched.getAmount() <= cursor.getMaxStackSize())) {
                            ItemStack newCursor = matched.clone();
                            newCursor.add(cursor.getAmount());
                            player.setItemOnCursor(newCursor);
                            doCraft(block);
                            updateCraftingGui(block);
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        });
        blockMenu.addMenuClickHandler(getReturnItemSlot(), (player, i, itemStack, clickAction) -> {
            BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
            if (inv == null) return false;
            NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
            if (info == null) return false;
            IStorage networkStorage = info.getStorage();
            for (int slot : getCraftSlots()) {
                ItemStack item = inv.getItemInSlot(slot);
                if (item != null && !item.getType().isAir()) networkStorage.pushItem(item);
            }
            return false;
        });
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        super.tick(block, item, data);
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return;
        if (inv.hasViewer()) updateCraftingGui(block);
    }

    public void updateCraftingGui(@Nonnull Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return;
        ItemStack matched = matchItem(block);
        if (matched == null) {
            inv.replaceExistingItem(getCraftOutputSlot(), MenuItems.Empty);
            return;
        }
        inv.replaceExistingItem(getCraftOutputSlot(), matched);
    }

    public void doCraft(@Nonnull Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return;
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;
        ItemStack matched = matchItem(block);
        if (matched == null || matched.getType().isAir()) return;
        IStorage networkStorage = info.getStorage();
        ItemStack[] recipe = Arrays.copyOf(SlimefunItem.getByItem(matched).getRecipe(), 9);
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inv.getItemInSlot(getCraftSlots()[i]);
            if (itemStack == null || itemStack.getType().isAir()) continue;
            itemStack.setAmount(itemStack.getAmount() - recipe[i].getAmount());
            if (itemStack.getAmount() == 0) {
                ItemStack[] gotten = networkStorage.tryTakeItem(new ItemRequest(recipe[i], recipe[i].getAmount()));
                if (gotten.length != 0) itemStack.setAmount(gotten[0].getAmount());
            }
        }
    }

    @Nullable public ItemStack matchItem(@Nonnull Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return null;
        List<ItemStack> craftingSlots = new ArrayList<>();
        for (int slot : getCraftSlots()) {
            craftingSlots.add(inv.getItemInSlot(slot));
        }
        ItemStack matched = null;
        recipe:
        for (SlimefunItem slimefunItem : Slimefun.getRegistry().getEnabledSlimefunItems().stream()
                .filter(x -> x.getRecipeType() == RecipeType.ENHANCED_CRAFTING_TABLE)
                .toList()) {
            ItemStack[] recipe = Arrays.copyOf(slimefunItem.getRecipe(), 9);
            for (int i = 0; i < 9; i++) {
                ItemStack craftingSlot = craftingSlots.get(i);
                ItemStack recipeItem = recipe[i];
                if (!SlimefunUtils.isItemSimilar(craftingSlot, recipeItem, true, true)) continue recipe;
            }
            matched = slimefunItem.getRecipeOutput();
        }

        return matched;
    }
}
