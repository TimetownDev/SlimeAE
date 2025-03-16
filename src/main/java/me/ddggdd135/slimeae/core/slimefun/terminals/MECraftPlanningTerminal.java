package me.ddggdd135.slimeae.core.slimefun.terminals;

import com.balugaq.jeg.api.groups.SearchGroup;
import com.balugaq.jeg.implementation.JustEnoughGuide;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingSession;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.core.managers.PinnedManager;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

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
            }
            return;
        }

        CraftingRecipe[] recipes = info.getRecipes().toArray(CraftingRecipe[]::new);
        ArrayList<ItemStack> keyItemArray = new ArrayList<>();

        // 映射对应的合成表，避免被置顶打乱顺序。
        HashMap<ItemStack, CraftingRecipe> itemRecipeMap = new HashMap<>();
        for (CraftingRecipe recipe : recipes) {
            ItemStack orignalItemStack = recipe.getOutput()[0];
            ItemStack keyItem = itemSafeClone(orignalItemStack);
            itemRecipeMap.put(keyItem, recipe);
            keyItemArray.add(keyItem);
        }
        ItemStack[] itemStacks = keyItemArray.toArray(new ItemStack[0]);

        Player player0 = (Player) blockMenu.getInventory().getViewers().get(0);

        // 获取过滤器
        String filter = getFilter(block).toLowerCase(Locale.ROOT);

        // 过滤和排序逻辑
        List<Map.Entry<ItemStack, Long>> items = new ArrayList<>(Arrays.stream(itemStacks)
                .map(x -> new AbstractMap.SimpleEntry<>(x, 0L))
                .toList());
        if (!filter.isEmpty()) {
            if (!SlimeAEPlugin.getJustEnoughGuideIntegration().isLoaded())
                items.removeIf(x -> doFilterNoJEG(x, filter));
            else {
                boolean isPinyinSearch = JustEnoughGuide.getConfigManager().isPinyinSearch();
                SearchGroup group = new SearchGroup(null, player0, filter, isPinyinSearch);
                List<SlimefunItem> slimefunItems = group.filterItems(player0, filter, isPinyinSearch);
                items.removeIf(x -> doFilterWithJEG(x, slimefunItems, filter));
            }
        }
        // 对配方排序以对应排序后的items
        ArrayList<CraftingRecipe> orderedRecipe = new ArrayList<>();
        for (Map.Entry<ItemStack, Long> entry : items) {
            orderedRecipe.add(itemRecipeMap.get(entry.getKey()));
        }

        List<ItemStack> storage = List.of(itemStacks);
        PinnedManager pinnedManager = SlimeAEPlugin.getPinnedManager();
        List<ItemStack> pinnedItems = pinnedManager.getPinnedItems(player0);
        if (pinnedItems == null) pinnedItems = new ArrayList<>();
        final Set<ItemStack> PINNED_IN_PLANNING = Collections.newSetFromMap(new IdentityHashMap<>());
        if (filter.isEmpty()) {
            for (ItemStack pinned : pinnedItems) {
                int index;
                if((index = storage.indexOf(pinned)) == -1) continue;
                ItemStack pinnedClone = itemSafeClone(storage.get(index));
                PINNED_IN_PLANNING.add(pinnedClone);
                items.add(0, new AbstractMap.SimpleEntry<>(pinnedClone, 0L));
                orderedRecipe.add(0, itemRecipeMap.get(storage.get(index)));
//                for (ItemStack item : storage) {
//                    if (!item.equals(pinned)) continue;
//                    ItemStack pinnedClone = itemSafeClone(item);
//                    PINNED_IN_PLANNING.add(pinnedClone);
//                    items.add(0, new AbstractMap.SimpleEntry<>(pinnedClone, 0L));
//                    orderedRecipe.add(0, itemRecipeMap.get(item));
//                }
            }
        }

        itemStacks = items.stream().map(Map.Entry::getKey).toArray(ItemStack[]::new);

        int page = getPage(block);
        if (page > Math.ceil(recipes.length / (double) getDisplaySlots().length) - 1) {
            page = (int) (Math.ceil(recipes.length / (double) getDisplaySlots().length) - 1);
            if (page < 0) page = 0;
            setPage(block, page);
        }

        int startIndex = page * getDisplaySlots().length;
        int endIndex = startIndex + getDisplaySlots().length;

        if (startIndex == endIndex) {
            for (int slot : getDisplaySlots()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
            }
        }

        for (int i = 0; i < getDisplaySlots().length && (i + startIndex) < endIndex; i++) {
            int slot = getDisplaySlots()[i];
            if (i + startIndex >= items.size()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                continue;
            }

            ItemStack itemStack = itemStacks[i + startIndex];
            CraftingRecipe recipe = orderedRecipe.get(i + startIndex);

            if (itemStack == null || itemStack.getType().isAir()) {
                blockMenu.replaceExistingItem(slot, MenuItems.EMPTY);
                blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                continue;
            }

            ItemStack result = ItemUtils.createDisplayItem(itemStack, 1, false, false);

            ItemMeta meta = result.getItemMeta();
            List<String> lore = new ArrayList<>();

            // 添加材料提示用于终端区分多种相同输出的配方。
            setRecipeLore(recipe, lore);

            lore.add("");
            lore.add("  &e可合成");
            if (PINNED_IN_PLANNING.contains(itemStack)) lore.add("&e===已置顶===");
            meta.setLore(CMIChatColor.translate(lore));
            result.setItemMeta(meta);

            blockMenu.replaceExistingItem(slot, result);
            blockMenu.addMenuClickHandler(slot, (player, i1, itemStack12, clickAction) -> {
                if (SlimefunUtils.isItemSimilar(
                        player.getItemOnCursor(), SlimefunAEItems.AE_TERMINAL_TOPPER, true, false)) {
                    ItemStack template = itemStack.asOne();
                    List<ItemStack> pinned = pinnedManager.getPinnedItems(player);
                    if (pinned == null) pinned = new ArrayList<>();
                    if (!pinned.contains(template)) pinnedManager.addPinned(player, template);
                    else pinnedManager.removePinned(player, template);
                    updateGui(block);
                    return false;
                }
                player.closeInventory();
                player.sendMessage(CMIChatColor.translate("&e输入合成数量"));
                ChatUtils.awaitInput(player, msg -> {
                    if (!SlimeAEPlugin.getNetworkData().AllNetworkData.contains(info)) return;
                    try {
                        int amount = Integer.parseInt(msg);
                        if (amount > NetworkInfo.getMaxCraftingAmount()) {
                            player.sendMessage(CMIChatColor.translate(
                                    "&c&l一次最多只能合成" + NetworkInfo.getMaxCraftingAmount() + "个物品"));
                            return;
                        }
                        if (amount <= 0) {
                            player.sendMessage(CMIChatColor.translate("&c&l请输入大于0的数字"));
                            return;
                        }

                        AutoCraftingSession session = new AutoCraftingSession(info, recipe, amount);
                        session.refreshGUI(45, false);
                        AEMenu menu = session.getMenu();
                        int[] borders = new int[] {45, 46, 48, 49, 50, 52, 53};
                        int acceptSlot = 47;
                        int cancelSlot = 51;
                        for (int slot1 : borders) {
                            menu.replaceExistingItem(slot1, ChestMenuUtils.getBackground());
                            menu.addMenuClickHandler(slot1, ChestMenuUtils.getEmptyClickHandler());
                        }
                        menu.replaceExistingItem(acceptSlot, MenuItems.ACCEPT);
                        menu.addMenuClickHandler(acceptSlot, (p, s, itemStack1, action) -> {
                            if (info.getCraftingSessions().size() >= NetworkInfo.getMaxCraftingSessions()) {
                                player.sendMessage(CMIChatColor.translate(
                                        "&c&l这个网络已经有" + NetworkInfo.getMaxCraftingSessions() + "个合成任务了"));
                                return false;
                            }
                            player.sendMessage(CMIChatColor.translate("&a&l成功规划了合成任务"));
                            session.refreshGUI(54);
                            session.start();
                            return false;
                        });
                        menu.replaceExistingItem(cancelSlot, MenuItems.CANCEL);
                        menu.addMenuClickHandler(cancelSlot, (p, s, itemStack1, action) -> {
                            player.closeInventory();
                            return false;
                        });
                        menu.open(player);
                    } catch (NumberFormatException e) {
                        player.sendMessage(CMIChatColor.translate("&c&l无效的数字"));
                    } catch (NoEnoughMaterialsException e) {
                        player.sendMessage(CMIChatColor.translate("&c&l没有足够的材料:"));
                        for (Map.Entry<ItemStack, Long> entry :
                                e.getMissingMaterials().entrySet()) {
                            String itemName = ItemUtils.getItemName(entry.getKey());
                            player.sendMessage(
                                    CMIChatColor.translate("  &e- &f" + itemName + " &cx " + entry.getValue()));
                        }
                    } catch (Exception e) {
                        player.sendMessage(CMIChatColor.translate("&c&l" + e.getMessage()));
                    }
                });
                return false;
            });
        }
    }

    private static @NotNull ItemStack itemSafeClone(ItemStack orignalItemStack) {
        ItemStack keyItem = new ItemStack(orignalItemStack.getType());
        keyItem.setAmount(orignalItemStack.getAmount());
        keyItem.setItemMeta(orignalItemStack.getItemMeta());
        return keyItem;
    }

    private static void setRecipeLore(CraftingRecipe recipe, List<String> lore) {
        Map<String, Integer> materialCount = new HashMap<>();
        for (ItemStack stack : recipe.getInput()) {
            if (stack == null || stack.getType().isAir()) continue;
            String itemName = ItemUtils.getItemName(stack);
            materialCount.put(itemName, materialCount.getOrDefault(itemName, 0) + stack.getAmount());
        }
        lore.add("&7材料列表:");
        for (Map.Entry<String, Integer> entry : materialCount.entrySet()) {
            lore.add("  &f" + entry.getKey() + " &ex " + entry.getValue());
        }
    }
}
