package me.ddggdd135.slimeae.core.slimefun.terminals;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingTask;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.core.managers.PinnedManager;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MEAllInOneTerminal extends METerminal {

    public MEAllInOneTerminal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void updateGui(@javax.annotation.Nonnull Block block) {
        BlockMenu blockMenu =
                com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils.getMenu(block.getLocation());
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

        IStorage networkStorage = info.getStorage();
        ItemHashMap<Long> storage = networkStorage.getStorageUnsafe();

        List<?> viewers = blockMenu.getInventory().getViewers();
        if (viewers.isEmpty()) return;
        Player player = (Player) viewers.get(0);

        String filter = getFilter(block).toLowerCase(java.util.Locale.ROOT);
        updateFilterButton(blockMenu, filter);

        LinkedHashMap<ItemKey, Long> merged = new LinkedHashMap<>();
        for (Map.Entry<ItemStack, Long> entry : storage.entrySet()) {
            merged.put(new ItemKey(entry.getKey()), entry.getValue());
        }
        Set<CraftingRecipe> recipes = info.getRecipes();
        java.util.Set<ItemKey> craftableKeys = new java.util.HashSet<>();
        for (CraftingRecipe recipe : recipes) {
            for (ItemStack output : recipe.getOutput()) {
                if (output == null || output.getType().isAir()) continue;
                ItemKey key = new ItemKey(output);
                merged.putIfAbsent(key, 0L);
                craftableKeys.add(key);
            }
        }

        List<Map.Entry<ItemStack, Long>> items = new ArrayList<>();
        for (Map.Entry<ItemKey, Long> entry : merged.entrySet()) {
            items.add(new java.util.AbstractMap.SimpleEntry<>(entry.getKey().getItemStack(), entry.getValue()));
        }

        if (!filter.isEmpty()) {
            if (!SlimeAEPlugin.getJustEnoughGuideIntegration().isLoaded())
                items.removeIf(x -> doFilterNoJEG(x, filter));
            else {
                boolean isPinyinSearch = com.balugaq.jeg.implementation.JustEnoughGuide.getConfigManager()
                        .isPinyinSearch();
                List<io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem> slimefunItemsList =
                        getCachedFilterItems(player, filter, isPinyinSearch);
                java.util.Set<io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem> slimefunItemSet =
                        new java.util.HashSet<>(slimefunItemsList);
                items.removeIf(x -> doFilterWithJEG(x, slimefunItemSet, filter));
            }
        }

        if (storage instanceof me.ddggdd135.slimeae.api.items.CreativeItemMap) items.sort(MATERIAL_SORT);
        else items.sort(getSort(block));

        int pinnedCount = 0;
        if (filter.isEmpty()) {
            PinnedManager pinnedManager = SlimeAEPlugin.getPinnedManager();
            List<ItemStack> pinnedItems = pinnedManager.getPinnedItems(player);
            if (pinnedItems == null) pinnedItems = new ArrayList<>();
            for (ItemStack pinned : pinnedItems) {
                Long amt = merged.get(new ItemKey(pinned));
                if (amt == null) continue;
                items.add(0, new java.util.AbstractMap.SimpleEntry<>(pinned, amt));
                pinnedCount++;
            }
        }

        int page = getPage(block);
        int maxPage = (int) Math.max(0, Math.ceil(items.size() / (double) getDisplaySlots().length) - 1);
        if (page > maxPage) {
            page = maxPage;
            setPage(block, page);
        }

        int startIndex = page * getDisplaySlots().length;
        int endIndex = startIndex + getDisplaySlots().length;

        if (startIndex == endIndex) {
            for (int slot : getDisplaySlots()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
            }
        }

        for (int i = 0; i < getDisplaySlots().length && (i + startIndex) < endIndex; i++) {
            int slot = getDisplaySlots()[i];
            if (i + startIndex >= items.size()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                continue;
            }
            Map.Entry<ItemStack, Long> entry = items.get(i + startIndex);
            ItemStack itemStack = entry.getKey();
            if (itemStack == null || itemStack.getType().isAir()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                continue;
            }
            boolean isPinned = i < pinnedCount - page * getDisplaySlots().length;
            long amount = entry.getValue();
            boolean hasCraftRecipe = craftableKeys.contains(new ItemKey(itemStack));
            blockMenu.replaceExistingItem(
                    slot, ItemUtils.createDisplayItem(itemStack, amount, true, isPinned, hasCraftRecipe));
            blockMenu.addMenuClickHandler(
                    slot, handleAllInOneClick(block, blockMenu, itemStack, amount > 0, hasCraftRecipe));
        }
    }

    private ChestMenu.AdvancedMenuClickHandler handleAllInOneClick(
            Block block, BlockMenu menu, ItemStack display, boolean hasStock, boolean hasCraftRecipe) {
        return new ChestMenu.AdvancedMenuClickHandler() {
            @Override
            public boolean onClick(
                    InventoryClickEvent event, Player player, int i, ItemStack cursor, ClickAction clickAction) {
                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                if (info == null) return false;

                if (SlimefunUtils.isItemSimilar(cursor, SlimeAEItems.AE_TERMINAL_TOPPER, true, false)) {
                    ItemStack template = display.asQuantity(display.getMaxStackSize());
                    PinnedManager pinnedManager = SlimeAEPlugin.getPinnedManager();
                    List<ItemStack> pinned = pinnedManager.getPinnedItems(player);
                    if (pinned == null) pinned = new ArrayList<>();
                    if (!pinned.contains(template.asOne())) pinnedManager.addPinned(player, template);
                    else pinnedManager.removePinned(player, template);
                    clearSortedItemsCache(block.getLocation());
                    return false;
                }

                if (clickAction.isRightClicked()) {
                    if (!hasCraftRecipe) {
                        player.sendMessage(CMIChatColor.translate("&c&l该物品没有可用的自动合成配方"));
                        return false;
                    }
                    CraftingRecipe recipe = info.getRecipeFor(display);
                    if (recipe == null) {
                        player.sendMessage(CMIChatColor.translate("&c&l该物品没有可用的自动合成配方"));
                        return false;
                    }
                    triggerCraftPlanning(player, recipe, info, block);
                    return false;
                }

                if (!hasStock) return false;

                IStorage networkStorage = info.getStorage();
                Inventory playerInventory = player.getInventory();
                ItemStack itemStack = menu.getItemInSlot(i);
                if (itemStack != null
                        && !itemStack.getType().isAir()
                        && !SlimefunUtils.isItemSimilar(itemStack, MenuItems.EMPTY, true, false)) {
                    ItemStack template = display.asQuantity(display.getMaxStackSize());

                    if (clickAction.isShiftClicked()
                            && InvUtils.fits(
                                    playerInventory,
                                    template,
                                    IntStream.range(0, 36).toArray())) {
                        playerInventory.addItem(networkStorage
                                .takeItem(new ItemRequest(new ItemKey(template), template.getMaxStackSize()))
                                .toItemStacks());
                    } else if (cursor.getType().isAir()
                            || (SlimefunUtils.isItemSimilar(template, cursor, true, false)
                                    && cursor.getAmount() + 1 <= cursor.getMaxStackSize())) {
                        ItemStack[] gotten = networkStorage
                                .takeItem(new ItemRequest(new ItemKey(template), 1))
                                .toItemStacks();
                        if (gotten.length != 0) {
                            ItemStack newCursor = gotten[0];
                            newCursor.add(cursor.getAmount());
                            player.setItemOnCursor(newCursor);
                        }
                    }
                }
                clearSortedItemsCache(block.getLocation());
                return false;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        };
    }

    private void triggerCraftPlanning(Player player, CraftingRecipe recipe, NetworkInfo info, Block block) {
        player.closeInventory();
        player.sendMessage(CMIChatColor.translate("&e输入合成数量 &7(输入cancel取消)"));
        ChatUtils.awaitInput(player, msg -> {
            if (!SlimeAEPlugin.getNetworkData().AllNetworkData.contains(info)) return;
            Bukkit.getScheduler().runTaskAsynchronously(SlimeAEPlugin.getInstance(), () -> {
                try {
                    int amount = Integer.parseInt(msg);
                    if (amount > NetworkInfo.getMaxCraftingAmount()) {
                        player.sendMessage(
                                CMIChatColor.translate("&c&l一次最多只能合成" + NetworkInfo.getMaxCraftingAmount() + "个物品"));
                        reopenTerminal(player, block);
                        return;
                    }
                    if (amount <= 0) {
                        player.sendMessage(CMIChatColor.translate("&c&l请输入大于0的数字"));
                        reopenTerminal(player, block);
                        return;
                    }
                    AutoCraftingTask task = new AutoCraftingTask(info, recipe, amount);
                    Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), () -> {
                        task.refreshGUI(45, false);
                        AEMenu menu = task.getMenu();
                        int[] borders = new int[] {45, 46, 48, 49, 50, 52, 53};
                        for (int slot : borders) {
                            menu.replaceExistingItem(slot, ChestMenuUtils.getBackground());
                            menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                        }
                        menu.replaceExistingItem(47, MenuItems.ACCEPT);
                        menu.addMenuClickHandler(47, (p, s, itemStack, action) -> {
                            if (info.getAutoCraftingSessions().size() >= NetworkInfo.getMaxCraftingSessions()) {
                                player.sendMessage(CMIChatColor.translate(
                                        "&c&l这个网络已经有" + NetworkInfo.getMaxCraftingSessions() + "个合成任务了"));
                                task.dispose();
                                return false;
                            }
                            player.sendMessage(CMIChatColor.translate("&a&l成功规划了合成任务"));
                            task.refreshGUI(54);
                            task.start();
                            return false;
                        });
                        menu.replaceExistingItem(51, MenuItems.CANCEL);
                        menu.addMenuClickHandler(51, (p, s, itemStack, action) -> {
                            player.closeInventory();
                            task.dispose();
                            reopenTerminal(player, block);
                            return false;
                        });
                        menu.open(player);
                    });
                } catch (NumberFormatException e) {
                    player.sendMessage(CMIChatColor.translate("&c&l无效的数字"));
                    reopenTerminal(player, block);
                } catch (NoEnoughMaterialsException e) {
                    player.sendMessage(CMIChatColor.translate("&c&l没有足够的材料:"));
                    for (Map.Entry<ItemStack, Long> entry :
                            e.getMissingMaterials().entrySet()) {
                        String itemName = ItemUtils.getItemName(entry.getKey());
                        player.sendMessage(CMIChatColor.translate("  &e- &f" + itemName + " &cx " + entry.getValue()));
                    }
                    reopenTerminal(player, block);
                } catch (Exception e) {
                    player.sendMessage(CMIChatColor.translate("&c&l" + e.getMessage()));
                    reopenTerminal(player, block);
                }
            });
            player.sendMessage(CMIChatColor.translate("&a&l计算中..."));
        });
    }

    private void reopenTerminal(Player player, Block block) {
        Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), () -> {
            BlockMenu menu =
                    com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils.getMenu(block.getLocation());
            if (menu != null) {
                menu.open(player);
            }
        });
    }
}
