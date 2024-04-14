package me.ddggdd135.slimeae.core.listeners;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.guide.SurvivalSlimefunGuide;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.List;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SCMenu;
import me.ddggdd135.slimeae.api.abstracts.AbstractMachineBlock;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class InventoryListener implements Listener {
    private static final int MACHINE_GUIDE_DISPLAY_SLOT = 16;
    private static final int MACHINE_RECIPE_DISPLAY_SLOT = 4;
    private static final int MENU_SIZE = 54;

    public static final NamespacedKey SF_KEY = new NamespacedKey(Slimefun.getPlugin(Slimefun.class), "slimefun_item");
    public static final NamespacedKey INDEX_KEY = new NamespacedKey(SlimeAEPlugin.getInstance(), "recipe_index");

    @EventHandler
    public void onDualRecipeClick(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        Inventory inventory = e.getClickedInventory();

        if (inventory == null) {
            return;
        }

        ItemStack backButton = inventory.getItem(0);

        if (clickedItem == null || backButton == null) {
            return;
        }

        PersistentDataContainer pdc = backButton.getItemMeta().getPersistentDataContainer();

        if (!pdc.has(SF_KEY, PersistentDataType.STRING)
                || !pdc.get(SF_KEY, PersistentDataType.STRING).equals("_UI_BACK")) {
            return;
        }

        // At this point, it has been confirmed that the player clicked a dual input or output item and is in a sf guide
        Player p = (Player) e.getWhoClicked();
        SlimefunItem machine = SlimefunItem.getByItem(e.getClickedInventory().getItem(MACHINE_GUIDE_DISPLAY_SLOT));
        SCMenu menu = new SCMenu(Slimefun.getLocalization().getMessage(p, "guide" + ".title.main"));
        SurvivalSlimefunGuide guide = new SurvivalSlimefunGuide(false, false);
        if (!(machine instanceof AbstractMachineBlock machineBlock)) {
            return;
        }

        List<MachineRecipe> recipes = machineBlock.getMachineRecipes();
        pdc = clickedItem.getItemMeta().getPersistentDataContainer();
        int index = pdc.get(INDEX_KEY, PersistentDataType.INTEGER);

        menu.addMenuOpeningHandler(pl -> pl.playSound(pl.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1));
        menu.setSize(MENU_SIZE);
        menu.addBackButton(guide, p, PlayerProfile.find(p).get());
        menu.replaceExistingItem(MACHINE_RECIPE_DISPLAY_SLOT, machine.getItem());
        for (int i : machineBlock.getBorderIn()) {
            menu.replaceExistingItem(i, ChestMenuUtils.getInputSlotTexture());
        }
        for (int i : machineBlock.getBorderOut()) {
            menu.replaceExistingItem(i, ChestMenuUtils.getOutputSlotTexture());
        }
        for (ItemStack item : recipes.get(index).getInput()) {
            menu.pushItem(item, machineBlock.getInputSlots());
        }
        for (ItemStack item : recipes.get(index).getOutput()) {
            menu.pushItem(item, machineBlock.getOutputSlots());
        }

        menu.setBackgroundNonClickable(true);
        menu.setPlayerInventoryClickable(false);

        menu.open(p);
    }
}
