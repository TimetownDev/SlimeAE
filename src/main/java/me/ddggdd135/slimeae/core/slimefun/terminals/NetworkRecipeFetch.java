package me.ddggdd135.slimeae.core.slimefun.terminals;

import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.inventory.ItemStack;

public final class NetworkRecipeFetch {
    private NetworkRecipeFetch() {}

    public static void moveRecipeFromNetwork(
            @Nonnull BlockMenu menu,
            @Nonnull IStorage networkStorage,
            @Nonnull CraftingRecipe recipe,
            @Nonnull int[] inputSlots,
            boolean max) {
        ItemStack[] recipeInputs = recipe.getInput();
        int amount = max ? 64 : 1;

        returnItemsToNetwork(menu, networkStorage, inputSlots);

        for (int i = 0; i < recipeInputs.length && i < inputSlots.length; i++) {
            ItemStack needed = recipeInputs[i];
            if (needed == null || needed.getType().isAir()) continue;

            int requestAmount = needed.getAmount() * amount;
            ItemStack[] taken = networkStorage
                    .takeItem(new ItemRequest(new ItemKey(needed), requestAmount))
                    .toItemStacks();

            if (taken.length > 0 && taken[0] != null && !taken[0].getType().isAir()) {
                menu.replaceExistingItem(inputSlots[i], taken[0]);
            }
        }
    }

    public static void returnItemsToNetwork(
            @Nonnull BlockMenu menu, @Nonnull IStorage networkStorage, @Nonnull int[] slots) {
        for (int slot : slots) {
            ItemStack current = menu.getItemInSlot(slot);
            if (current != null && !current.getType().isAir()) {
                networkStorage.pushItem(current);
                menu.replaceExistingItem(slot, null);
            }
        }
    }
}
