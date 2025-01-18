package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.DataUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.core.AutoCraftingSession;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MECraftPlanningTerminal extends METerminal {
    public MECraftPlanningTerminal(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void updateGui(@Nonnull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;
        for (int slot : getDisplaySlots()) {
            blockMenu.replaceExistingItem(slot, MenuItems.Empty);
            blockMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;

        CraftingRecipe[] recipes = info.getRecipes().toArray(CraftingRecipe[]::new);
        int page = getPage(block);
        if (page > Math.ceil(recipes.length / (double) getDisplaySlots().length) - 1) {
            page = (int) (Math.ceil(recipes.length / (double) getDisplaySlots().length) - 1);
            if (page < 0) page = 0;
            setPage(block, page);
        }

        ItemStack[] itemStacks =
                Arrays.stream(recipes).map(x -> x.getOutput()[0]).toList().toArray(ItemStack[]::new);
        for (int i = 0; i < getDisplaySlots().length; i++) {
            if (itemStacks.length - 1 < i + page * getDisplaySlots().length) break;
            ItemStack itemStack = itemStacks[i + page * getDisplaySlots().length];
            CraftingRecipe recipe = recipes[i + page * getDisplaySlots().length];
            if (itemStack == null || itemStack.getType().isAir()) continue;
            int slot = getDisplaySlots()[i];
            ItemStack result = itemStack.clone();
            ItemMeta meta = result.getItemMeta();
            meta.getPersistentDataContainer()
                    .set(
                            ItemUtils.ITEM_STORAGE_KEY,
                            PersistentDataType.STRING,
                            DataUtils.itemStack2String(itemStack.asOne()));
            meta.setLore(List.of(CMIChatColor.translate("  &e可合成")));
            result.setItemMeta(meta);
            blockMenu.replaceExistingItem(slot, result);
            blockMenu.addMenuClickHandler(slot, (player, i1, itemStack12, clickAction) -> {
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
                        int[] background = new int[] {45, 46, 48, 49, 50, 52, 53};
                        int acceptSlot = 47;
                        int cancelSlot = 51;
                        for (int slot1 : background) {
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
                        for (Map.Entry<ItemStack, Integer> entry :
                                e.getMissingMaterials().entrySet()) {
                            String itemName = ItemUtils.getItemName(entry.getKey());
                            player.sendMessage(
                                    CMIChatColor.translate("  &e- &f" + itemName + " &cx " + entry.getValue()));
                        }
                    }
                });
                return false;
            });
        }
    }
}
