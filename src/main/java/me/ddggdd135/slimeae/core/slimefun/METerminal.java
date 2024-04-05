package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.Map;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class METerminal extends SlimefunItem {
    public int[] getBackgroundSlots() {
        return new int[] {17, 26};
    }

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

    public int getInputSlot() {
        return 8;
    }

    public int getChangeSort() {
        return 26;
    }

    public int getFilter() {
        return 35;
    }

    public int getPagePrevious() {
        return 44;
    }

    public int getPageNext() {
        return 53;
    }

    public METerminal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {
            @Override
            public void init() {}

            @Override
            public void newInstance(@Nonnull BlockMenu inv, @Nonnull Block b) {
                for (int slot : getDisplaySlots()) {
                    inv.addItem(slot, MenuItems.Empty, new ChestMenu.AdvancedMenuClickHandler() {
                        @Override
                        public boolean onClick(
                                InventoryClickEvent inventoryClickEvent,
                                Player player,
                                int i,
                                ItemStack cursor,
                                ClickAction clickAction) {
                            NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(b.getLocation());
                            if (info == null) return false;
                            IStorage networkStorage = info.getStorage();
                            Inventory playerInventory = player.getInventory();
                            ItemStack itemStack = inv.getItemInSlot(i);
                            if (!(itemStack == null
                                    || itemStack.getType().isAir()
                                    || SlimefunUtils.isItemSimilar(itemStack, MenuItems.Empty, false, false))) {
                                ItemStack template = ItemUtils.createTemplateItem(itemStack);
                                template.setAmount(template.getMaxStackSize());
                                if (clickAction.isShiftClicked()
                                        && InvUtils.fits(
                                                playerInventory,
                                                template,
                                                IntStream.range(0, playerInventory.getSize())
                                                        .toArray())) {
                                    playerInventory.addItem(networkStorage.tryTakeItem(
                                            new ItemRequest(template, template.getMaxStackSize())));
                                } else {
                                    ItemStack[] gotten = networkStorage.tryTakeItem(new ItemRequest(template, 1));
                                    if (gotten.length != 0) {
                                        ItemStack newCursor = cursor.clone();
                                        newCursor.add(gotten[0].getAmount());
                                        player.setItemOnCursor(newCursor);
                                    }
                                }
                            }
                            updateGui(b);
                            return false;
                        }

                        @Override
                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            return false;
                        }
                    });
                }
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return METerminal.this.canUse(player, false)
                        && Slimefun.getProtectionManager()
                                .hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return new int[0];
            }
        };
    }

    @OverridingMethodsMustInvokeSuper
    protected void constructMenu(BlockMenuPreset preset) {
        for (int slot : getBackgroundSlots()) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
    }

    public static int getPage(Block block) {
        String value = StorageCacheUtils.getData(block.getLocation(), "page");
        if (value == null) return 0;
        return Integer.getInteger(value);
    }

    public static void setPage(Block block, int value) {
        StorageCacheUtils.setData(block.getLocation(), "page", String.valueOf(value));
    }

    public void updateGui(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        if (inv == null) return;
        for (int slot : getDisplaySlots()) {
            inv.replaceExistingItem(slot, MenuItems.Empty);
        }
        NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (info == null) return;
        IStorage networkStorage = info.getStorage();
        Map<ItemStack, Integer> storage = networkStorage.getStorage();
        int page = getPage(block);
        if (page > (storage.keySet().size() / getDisplaySlots().length) - 1) {
            page = storage.keySet().size() / getDisplaySlots().length;
            setPage(block, page);
        }
        ItemStack[] itemStacks = storage.keySet().toArray(new ItemStack[0]);
        for (int i = 0; i < getDisplaySlots().length; i++) {
            if (itemStacks.length - 1 < i) break;
            ItemStack itemStack = itemStacks[i];
            if (itemStack == null || itemStack.getType().isAir()) continue;
            int slot = getDisplaySlots()[i];
            inv.replaceExistingItem(slot, ItemUtils.createDisplayItem(itemStack, storage.get(itemStack)));
        }
    }
}
