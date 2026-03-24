package me.ddggdd135.slimeae.core.slimefun.terminals;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.slimefun.CookingAllocator;
import me.ddggdd135.slimeae.core.slimefun.MEInterface;
import me.ddggdd135.slimeae.core.slimefun.MEPatternInterface;
import me.ddggdd135.slimeae.core.slimefun.Pattern;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MEPatternManagementTerminal extends METerminal {

    private static final int PATTERN_INPUT_SLOT = 8;

    public MEPatternManagementTerminal(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public int getInputSlot() {
        return PATTERN_INPUT_SLOT;
    }

    @Override
    public int[] getDisplaySlots() {
        return new int[] {
            0, 1, 2, 3, 4, 5, 6, 7,
            9, 10, 11, 12, 13, 14, 15, 16,
            18, 19, 20, 21, 22, 23, 24, 25,
            27, 28, 29, 30, 31, 32, 33, 34,
            36, 37, 38, 39, 40, 41, 42, 43,
            45, 46, 47, 48, 49, 50, 51, 52
        };
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        if (blockMenu.hasViewer()) updateGui(block);
        distributePatternFromInput(block, blockMenu);
    }

    @Override
    public void updateGui(@Nonnull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        if (!blockMenu.hasViewer()) return;

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) {
            for (int slot : getDisplaySlots()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
            }
            return;
        }

        List<?> viewers = blockMenu.getInventory().getViewers();
        if (viewers.isEmpty()) return;
        Player player = (Player) viewers.get(0);

        String filter = getFilter(block).toLowerCase(Locale.ROOT);
        updateFilterButton(blockMenu, filter);

        List<PatternEntry> allPatterns = collectPatterns(info);

        if (!filter.isEmpty()) {
            allPatterns.removeIf(entry -> {
                String name = CMIChatColor.stripColor(ItemUtils.getItemName(entry.outputDisplay));
                return name == null || !name.toLowerCase(Locale.ROOT).contains(filter);
            });
        }

        int page = getPage(block);
        int maxPage = (int) Math.max(0, Math.ceil(allPatterns.size() / (double) getDisplaySlots().length) - 1);
        if (page > maxPage) {
            page = maxPage;
            setPage(block, page);
        }

        int startIndex = page * getDisplaySlots().length;
        int endIndex = startIndex + getDisplaySlots().length;

        for (int i = 0; i < getDisplaySlots().length; i++) {
            int slot = getDisplaySlots()[i];
            int idx = i + startIndex;
            if (idx >= allPatterns.size() || idx >= endIndex) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                continue;
            }
            PatternEntry entry = allPatterns.get(idx);
            blockMenu.replaceExistingItem(slot, createPatternDisplayItem(entry));
            blockMenu.addMenuClickHandler(slot, handlePatternClick(entry, info));
        }
    }

    private List<PatternEntry> collectPatterns(NetworkInfo info) {
        List<PatternEntry> result = new ArrayList<>();
        for (Location holderLoc : info.getCraftingHolders()) {
            SlimefunBlockData blockData =
                    Slimefun.getDatabaseManager().getBlockDataController().getBlockData(holderLoc);
            if (blockData == null) continue;
            SlimefunItem sfItem = SlimefunItem.getById(blockData.getSfId());
            if (sfItem == null) continue;

            int[] patternSlots;
            if (sfItem instanceof MEPatternInterface mpi) {
                patternSlots = mpi.getPatternSlots();
            } else if (sfItem instanceof MEInterface mi) {
                if (hasAdjacentCookingAllocator(holderLoc.getBlock())) continue;
                patternSlots = mi.getPatternSlots();
            } else {
                continue;
            }

            BlockMenu holderMenu = blockData.getBlockMenu();
            if (holderMenu == null) continue;

            for (int slot : patternSlots) {
                ItemStack patternItem = holderMenu.getItemInSlot(slot);
                if (patternItem == null || patternItem.getType().isAir()) continue;
                if (SlimefunUtils.isItemSimilar(patternItem, MenuItems.PATTERN, true, false)) continue;
                if (ItemUtils.getSlimefunItemFast(patternItem, Pattern.class) == null) continue;
                try {
                    CraftingRecipe recipe = Pattern.getRecipe(patternItem);
                    if (recipe == null) continue;
                    ItemStack output = recipe.getOutput()[0];
                    result.add(new PatternEntry(holderLoc, slot, patternItem, output, recipe));
                } catch (Exception ignored) {
                }
            }
        }
        result.sort((a, b) -> {
            String na = CMIChatColor.stripColor(ItemUtils.getItemName(a.outputDisplay));
            String nb = CMIChatColor.stripColor(ItemUtils.getItemName(b.outputDisplay));
            if (na == null) na = "";
            if (nb == null) nb = "";
            return na.compareToIgnoreCase(nb);
        });
        return result;
    }

    private boolean hasAdjacentCookingAllocator(Block block) {
        for (BlockFace face : MEInterface.VaildBlockFace) {
            Block relative = block.getRelative(face);
            SlimefunBlockData data = StorageCacheUtils.getBlock(relative.getLocation());
            if (data == null) continue;
            SlimefunItem item = SlimefunItem.getById(data.getSfId());
            if (item instanceof CookingAllocator) return true;
        }
        return false;
    }

    private ItemStack createPatternDisplayItem(PatternEntry entry) {
        ItemStack display = entry.outputDisplay.clone();
        List<String> lore = display.getLore();
        if (lore == null) lore = new ArrayList<>();
        lore.add("");
        lore.add(CMIChatColor.translate("&7样板位置: &f" + formatLocation(entry.holderLocation)));
        lore.add(CMIChatColor.translate("&e点击取出样板"));
        display.setLore(lore);
        return display;
    }

    private String formatLocation(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    private ChestMenu.MenuClickHandler handlePatternClick(PatternEntry entry, NetworkInfo info) {
        return (player, slot, itemStack, action) -> {
            SlimefunBlockData blockData =
                    Slimefun.getDatabaseManager().getBlockDataController().getBlockData(entry.holderLocation);
            if (blockData == null) return false;
            BlockMenu holderMenu = blockData.getBlockMenu();
            if (holderMenu == null) return false;

            ItemStack patternItem = holderMenu.getItemInSlot(entry.slotInHolder);
            if (patternItem == null || patternItem.getType().isAir()) {
                player.sendMessage(CMIChatColor.translate("&c该样板已被移除"));
                return false;
            }

            if (!player.getInventory().addItem(patternItem.clone()).isEmpty()) {
                player.sendMessage(CMIChatColor.translate("&c背包已满"));
                return false;
            }

            holderMenu.replaceExistingItem(entry.slotInHolder, MenuItems.PATTERN);
            info.setNeedsRecipeUpdate(true);
            player.sendMessage(CMIChatColor.translate("&a成功取出样板"));
            return false;
        };
    }

    private void distributePatternFromInput(Block block, BlockMenu blockMenu) {
        ItemStack inputItem = blockMenu.getItemInSlot(getInputSlot());
        if (inputItem == null || inputItem.getType().isAir()) return;
        if (ItemUtils.getSlimefunItemFast(inputItem, Pattern.class) == null) return;
        try {
            CraftingRecipe recipe = Pattern.getRecipe(inputItem);
            if (recipe == null) return;
        } catch (Exception e) {
            return;
        }

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;

        for (Location holderLoc : info.getCraftingHolders()) {
            if (inputItem.getAmount() <= 0) break;
            SlimefunBlockData blockData =
                    Slimefun.getDatabaseManager().getBlockDataController().getBlockData(holderLoc);
            if (blockData == null) continue;
            SlimefunItem sfItem = SlimefunItem.getById(blockData.getSfId());
            if (!(sfItem instanceof MEPatternInterface mpi)) continue;
            BlockMenu holderMenu = blockData.getBlockMenu();
            if (holderMenu == null) continue;
            if (tryPlacePattern(holderMenu, mpi.getPatternSlots(), inputItem)) {
                info.setNeedsRecipeUpdate(true);
                if (inputItem.getAmount() <= 0) return;
            }
        }

        for (Location holderLoc : info.getCraftingHolders()) {
            if (inputItem.getAmount() <= 0) break;
            SlimefunBlockData blockData =
                    Slimefun.getDatabaseManager().getBlockDataController().getBlockData(holderLoc);
            if (blockData == null) continue;
            SlimefunItem sfItem = SlimefunItem.getById(blockData.getSfId());
            if (!(sfItem instanceof MEInterface mi)) continue;
            if (hasAdjacentCookingAllocator(holderLoc.getBlock())) continue;
            BlockMenu holderMenu = blockData.getBlockMenu();
            if (holderMenu == null) continue;
            if (tryPlacePattern(holderMenu, mi.getPatternSlots(), inputItem)) {
                info.setNeedsRecipeUpdate(true);
                if (inputItem.getAmount() <= 0) return;
            }
        }
    }

    private boolean tryPlacePattern(BlockMenu holderMenu, int[] patternSlots, ItemStack pattern) {
        for (int slot : patternSlots) {
            ItemStack existing = holderMenu.getItemInSlot(slot);
            if (existing != null
                    && !existing.getType().isAir()
                    && !SlimefunUtils.isItemSimilar(existing, MenuItems.PATTERN, true, false)) continue;
            ItemStack toPlace = pattern.asOne();
            holderMenu.replaceExistingItem(slot, toPlace);
            pattern.subtract();
            return true;
        }
        return false;
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        super.newInstance(menu, block);
        menu.addMenuClickHandler(getInputSlot(), (player, slot, cursor, clickAction) -> true);
    }

    @Override
    public boolean fastInsert() {
        return false;
    }

    private static class PatternEntry {
        final Location holderLocation;
        final int slotInHolder;
        final ItemStack patternItem;
        final ItemStack outputDisplay;
        final CraftingRecipe recipe;

        PatternEntry(
                Location holderLocation,
                int slotInHolder,
                ItemStack patternItem,
                ItemStack outputDisplay,
                CraftingRecipe recipe) {
            this.holderLocation = holderLocation;
            this.slotInHolder = slotInHolder;
            this.patternItem = patternItem;
            this.outputDisplay = outputDisplay;
            this.recipe = recipe;
        }
    }
}
