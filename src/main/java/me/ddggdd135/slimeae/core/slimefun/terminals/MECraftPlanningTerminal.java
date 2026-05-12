package me.ddggdd135.slimeae.core.slimefun.terminals;

import com.balugaq.jeg.implementation.JustEnoughGuide;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.*;
import java.util.HashSet;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingTask;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimeAEItems;
import me.ddggdd135.slimeae.core.managers.PinnedManager;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MECraftPlanningTerminal extends METerminal {

    public MECraftPlanningTerminal(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void updateGui(@Nonnull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) {
            // 清空显示槽
            for (int slot : getDisplaySlots()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
            }
            return;
        }

        List<?> viewers = blockMenu.getInventory().getViewers();
        if (viewers.isEmpty()) return;
        Player player = (Player) viewers.get(0);

        IStorage networkStorage = info.getStorage();
        ItemHashMap<Long> storage = networkStorage.getStorageUnsafe();

        Set<CraftingRecipe> recipes = info.getRecipes();
        ArrayList<RecipeEntry> recipeEntries = createRecipeEntries(recipes, storage);

        String filter = getFilter(block).toLowerCase(Locale.ROOT);
        updateFilterButton(blockMenu, filter);
        filterRecipeEntries(recipeEntries, player, filter);
        recipeEntries.sort(Comparator.comparing(RecipeEntry::getItemStack, getSort(block)));

        if (filter.isEmpty()) applyPinnedItems(player, recipeEntries);
        int page = fuckPage(block, recipeEntries.size());

        displayPage(blockMenu, recipeEntries, page, info, block, storage);
    }

    private void displayPage(
            BlockMenu menu,
            List<RecipeEntry> entries,
            int page,
            NetworkInfo info,
            Block block,
            ItemHashMap<Long> storage) {
        int slotPerPage = getDisplaySlots().length;
        int start = page * slotPerPage;
        int end = Math.min(start + slotPerPage, entries.size());

        for (int i = 0; i < slotPerPage; i++) {
            int slot = getDisplaySlots()[i];
            int entryIndex = start + i;

            if (entryIndex >= end) {
                menu.replaceExistingItem(slot, MenuItems.EMPTY);
                menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                continue;
            }
            RecipeEntry entry = entries.get(entryIndex);
            setupDisplayItem(menu, slot, entry, info, block, storage);
        }
    }

    private void setupDisplayItem(
            BlockMenu menu, int slot, RecipeEntry entry, NetworkInfo info, Block block, ItemHashMap<Long> storage) {
        ItemStack itemStack = entry.getItemStack().getKey();
        if (itemStack == null || itemStack.getType().isAir()) return;
        CraftingRecipe recipe = entry.getRecipe();
        long stock = entry.getItemStack().getValue();
        ItemStack displayItem = ItemUtils.createDisplayItem(itemStack, stock, false, false);
        ItemMeta meta = displayItem.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add("&7当前库存: &f" + stock);
        lore.add("");
        setRecipeLore(recipe, lore, storage);
        lore.add("");
        lore.add("  &e可合成");
        if (entry.isPinned()) lore.add("&e===已置顶===");
        meta.setLore(CMIChatColor.translate(lore));
        displayItem.setItemMeta(meta);
        menu.replaceExistingItem(slot, displayItem);
        menu.addMenuClickHandler(slot, (p, slot1, item, action) -> {
            handleItemClick(p, entry, info, block);
            return false;
        });
    }

    private static void setRecipeLore(CraftingRecipe recipe, List<String> lore, ItemHashMap<Long> storage) {
        Map<ItemStack, Integer> materialCount = new LinkedHashMap<>();
        for (ItemStack stack : recipe.getInput()) {
            if (stack == null || stack.getType().isAir()) continue;
            boolean found = false;
            for (Map.Entry<ItemStack, Integer> existing : materialCount.entrySet()) {
                if (existing.getKey().isSimilar(stack)) {
                    materialCount.put(existing.getKey(), existing.getValue() + stack.getAmount());
                    found = true;
                    break;
                }
            }
            if (!found) materialCount.put(stack, stack.getAmount());
        }
        lore.add("&7材料列表:");
        for (Map.Entry<ItemStack, Integer> entry : materialCount.entrySet()) {
            String itemName = ItemUtils.getItemName(entry.getKey());
            Long matStock = storage.get(entry.getKey());
            long matStockVal = matStock != null ? matStock : 0L;
            lore.add("  &f" + itemName + " &ex " + entry.getValue() + " &7(库存: &f" + matStockVal + "&7)");
        }
    }

    private void handleItemClick(Player player, RecipeEntry recipeEntry, NetworkInfo info, Block block) {
        if (SlimefunUtils.isItemSimilar(player.getItemOnCursor(), SlimeAEItems.AE_TERMINAL_TOPPER, true, false)) {
            ItemStack template = recipeEntry.getItemStack().getKey().asOne();
            PinnedManager pinnedManager = SlimeAEPlugin.getPinnedManager();
            List<ItemStack> pinned = pinnedManager.getPinnedItems(player);
            if (pinned == null) pinned = new ArrayList<>();
            if (!pinned.contains(template)) pinnedManager.addPinned(player, template);
            else pinnedManager.removePinned(player, template);
            updateGui(block);
            return;
        }
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

                    AutoCraftingTask task = new AutoCraftingTask(info, recipeEntry.getRecipe(), amount);
                    Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), () -> {
                        task.refreshGUI(45, false);
                        AEMenu menu = task.getMenu();
                        int[] borders = new int[] {45, 46, 48, 49, 50, 52, 53};
                        int acceptSlot = 47;
                        int cancelSlot = 51;
                        for (int slot1 : borders) {
                            menu.replaceExistingItem(slot1, ChestMenuUtils.getBackground());
                            menu.addMenuClickHandler(slot1, ChestMenuUtils.getEmptyClickHandler());
                        }
                        menu.replaceExistingItem(acceptSlot, MenuItems.ACCEPT);
                        menu.addMenuClickHandler(acceptSlot, (p, s, itemStack1, action) -> {
                            if (info.getAutoCraftingSessions().size() >= NetworkInfo.getMaxCraftingSessions()) {
                                player.sendMessage(CMIChatColor.translate(
                                        "&c&l这个网络已经有" + NetworkInfo.getMaxCraftingSessions() + "个合成任务了"));
                                task.dispose();
                                return false;
                            }
                            if (task.start()) {
                                player.sendMessage(CMIChatColor.translate("&a&l成功规划了合成任务"));
                                task.refreshGUI(54);
                            } else {
                                player.sendMessage(CMIChatColor.translate("&c&l网络状态已变化，合成任务未启动"));
                                task.dispose();
                            }
                            return false;
                        });
                        menu.replaceExistingItem(cancelSlot, MenuItems.CANCEL);
                        menu.addMenuClickHandler(cancelSlot, (p, s, itemStack1, action) -> {
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
            BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
            if (menu != null) {
                menu.open(player);
            }
        });
    }

    private int fuckPage(Block block, int totalSize) {
        int page = getPage(block);
        if (page > Math.ceil((double) totalSize / getDisplaySlots().length) - 1) {
            page = (int) (Math.ceil((double) totalSize / getDisplaySlots().length) - 1);
            if (page < 0) page = 0;
            setPage(block, page);
        }
        return page;
    }

    private void applyPinnedItems(Player player, List<RecipeEntry> entries) {
        List<ItemStack> pinnedItems = SlimeAEPlugin.getPinnedManager().getPinnedItems(player);
        if (pinnedItems == null || pinnedItems.isEmpty()) return;
        HashSet<ItemStack> pinnedSet = new HashSet<>(pinnedItems);

        ArrayList<RecipeEntry> pinnedEntries = new ArrayList<>();

        for (RecipeEntry entry : entries) {
            if (pinnedSet.contains(entry.getItemStack().getKey().asOne())) {
                RecipeEntry pinned = new RecipeEntry(
                        entry.getItemStack().getKey(),
                        entry.getRecipe(),
                        entry.getItemStack().getValue(),
                        true);
                pinnedEntries.add(pinned);
            }
        }

        // 仅显示置顶物品，而不再多余显示在原位置。
        // Iterator<RecipeEntry> iterator = entries.iterator();
        // while (iterator.hasNext()) {
        //     RecipeEntry entry = iterator.next();
        //     if (pinnedSet.contains(entry.getItemStack().getKey().asOne())) {
        //         RecipeEntry pinned = new RecipeEntry(entry.getItemStack().getKey(), entry.getRecipe(), true);
        //         pinnedEntries.add(pinned);
        //         iterator.remove();
        //     }
        // }
        entries.addAll(0, pinnedEntries);
    }

    private void filterRecipeEntries(ArrayList<RecipeEntry> entries, Player player, String filter) {
        if (filter.isEmpty()) return;
        if (SlimeAEPlugin.getJustEnoughGuideIntegration().isLoaded()) {
            boolean isPinyinSearch = JustEnoughGuide.getConfigManager().isPinyinSearch();
            // F1: 使用缓存的搜索结果
            List<SlimefunItem> slimefunItemsList = getCachedFilterItems(player, filter, isPinyinSearch);
            // F4: List→HashSet 优化 contains 查找为 O(1)
            Set<SlimefunItem> slimefunItemSet = new HashSet<>(slimefunItemsList);
            entries.removeIf(entry -> doFilterWithJEG(entry.getItemStack(), slimefunItemSet, filter));
        } else {
            entries.removeIf(entry -> doFilterNoJEG(entry.getItemStack(), filter));
        }
    }

    private ArrayList<RecipeEntry> createRecipeEntries(Set<CraftingRecipe> recipes, ItemHashMap<Long> storage) {
        ArrayList<RecipeEntry> entries = new ArrayList<>();
        for (CraftingRecipe recipe : recipes) {
            ItemStack output = recipe.getOutput()[0];
            Long stock = storage.get(output);
            entries.add(new RecipeEntry(output, recipe, stock != null ? stock : 0L));
        }
        return entries;
    }

    /**
     * 一个用映射配方和 DisplayItem 的类
     */
    private static class RecipeEntry {
        private final AbstractMap.SimpleEntry<ItemStack, Long> itemStack;
        private final CraftingRecipe recipe;
        private boolean isPinned = false;

        private RecipeEntry(ItemStack itemStack, CraftingRecipe recipe, long stock) {
            this.itemStack = new AbstractMap.SimpleEntry<>(itemStack, stock);
            this.recipe = recipe;
        }

        private RecipeEntry(ItemStack itemStack, CraftingRecipe recipe, long stock, boolean isPinned) {
            this.itemStack = new AbstractMap.SimpleEntry<>(itemStack, stock);
            this.recipe = recipe;
            this.isPinned = isPinned;
        }

        public AbstractMap.SimpleEntry<ItemStack, Long> getItemStack() {
            return itemStack;
        }

        public CraftingRecipe getRecipe() {
            return recipe;
        }

        public boolean isPinned() {
            return isPinned;
        }
    }

    @Override
    public boolean fastInsert() {
        return super.fastInsert();
    }
}
