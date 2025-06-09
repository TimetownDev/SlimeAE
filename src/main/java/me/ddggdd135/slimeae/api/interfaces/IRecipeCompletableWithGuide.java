package me.ddggdd135.slimeae.api.interfaces;

import com.balugaq.jeg.api.objects.events.GuideEvents;
import com.balugaq.netex.utils.GuideUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.listeners.JEGCompatibleListener;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Range;

/*
 * 感谢大香蕉的代码 :)))
 */
public interface IRecipeCompletableWithGuide {
    default void addJEGRecipeButton(@Nonnull BlockMenu blockMenu, @Range(from = 0, to = 53) int slot) {
        if (SlimeAEPlugin.getJustEnoughGuideIntegration().isLoaded()) {
            blockMenu.replaceExistingItem(slot, MenuItems.JEG_RECIPE_BUTTON);
            blockMenu.addMenuClickHandler(slot, (player, slot1, item, action) -> {
                openGuide_0(blockMenu, player);
                return false;
            });
        }
    }

    default void openGuide_0(@Nonnull BlockMenu blockMenu, @Nonnull Player player) {
        NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(blockMenu.getLocation());

        if (networkInfo == null) {
            return;
        }

        GuideUtil.openMainMenuAsync(player, SlimefunGuideMode.SURVIVAL_MODE, 1);
        JEGCompatibleListener.addCallback(player.getUniqueId(), ((event, profile) -> {
            BlockMenu actualMenu = StorageCacheUtils.getMenu(blockMenu.getLocation());
            if (actualMenu == null) {
                return;
            }

            if (!actualMenu.getPreset().getID().equals(blockMenu.getPreset().getID())) {
                return;
            }

            completeRecipeWithGuide(actualMenu, networkInfo, event);

            player.updateInventory();
            actualMenu.open(player);
        }));
        JEGCompatibleListener.tagGuideOpen(player);
    }

    default void completeRecipeWithGuide(
            @Nonnull BlockMenu blockMenu, @Nonnull NetworkInfo networkInfo, GuideEvents.ItemButtonClickEvent event) {
        Player player = event.getPlayer();

        ItemStack clickedItem = event.getClickedItem();
        if (clickedItem == null) {
            return;
        }

        CraftingRecipe recipe = RecipeUtils.getRecipe(clickedItem);
        if (recipe == null) return;

        ItemStack[] input = recipe.getInput();
        for (int i = 0; i < 9; i++) {
            if (i >= input.length) {
                break;
            }

            if (i >= getIngredientSlots().length) {
                break;
            }

            ItemStack existing = blockMenu.getItemInSlot(getIngredientSlots()[i]);
            int existingAmount = 0;
            if (existing != null && !existing.getType().isAir()) {
                existingAmount = existing.getAmount();
                if (existing.getAmount() >= existing.getMaxStackSize()) {
                    continue;
                }

                if (!SlimefunUtils.isItemSimilar(existing, input[i], true, false)) {
                    continue;
                }
            }

            int amount = input[i] == null || input[i].getType().isAir() ? 0 : input[i].getAmount() - existingAmount;
            if (amount <= 0) continue;

            ItemStack received = getItemStack(networkInfo, player, input[i], amount);
            if (received != null && !received.getType().isAir()) {
                blockMenu.pushItem(received, getIngredientSlots()[i]);
            }
        }

        event.setCancelled(true);
    }

    int[] getIngredientSlots();

    @Nullable default ItemStack getItemStack(
            @Nonnull NetworkInfo networkInfo, @Nonnull Player player, @Nullable ItemStack itemStack, int amount) {
        if (itemStack == null) return null;

        ItemStack[] items = networkInfo
                .getStorage()
                .takeItem(new ItemRequest(new ItemKey(itemStack), amount))
                .toItemStacks();

        ItemStack item = null;

        if (items.length > 0) item = items[0];
        if (item != null) {
            return item;
        }

        // get from player inventory
        for (ItemStack itemStack1 : player.getInventory().getContents()) {
            if (itemStack1 != null && itemStack1.getType() != Material.AIR) {
                if (SlimefunUtils.isItemSimilar(itemStack1, itemStack, true, false)) {
                    var clone = itemStack1.asOne();
                    int newAmount = itemStack1.getAmount() - 1;

                    itemStack1.setAmount(newAmount);
                    if (newAmount == 0) {
                        itemStack1.setType(Material.AIR);
                    }

                    return clone;
                }
            }
        }

        return null;
    }
}
