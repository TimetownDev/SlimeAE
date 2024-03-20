package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.InventoryBlock;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class MEInterface extends SlimefunItem implements IMEObject<MEInterface>, InventoryBlock {
    public static final int[] Boarder_Slots = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 27, 28, 29, 30, 31, 32, 33, 34, 35, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    public static final int[] Setting_Slots = new int[] {9, 10, 11, 12, 13, 14, 15, 16, 17};
    public static final int[] Item_Slots = new int[] {18, 19, 20, 21, 22, 23, 24, 25, 26};
    public MEInterface(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this, this.getItem().getItemMeta().getDisplayName(), this::constructMenu);
        addItemHandler(onBlockBreak());
        addItemHandler(new BlockTicker() {
            @Override
            public boolean isSynchronized() {
                return false;
            }

            @Override
            public void tick(Block block, SlimefunItem item, SlimefunBlockData data) {
                BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                for (int slot : Setting_Slots) {
                    ItemStack setting = inv.getItemInSlot(slot);
                    if (setting == null || setting.getType().isAir()) {
                        inv.replaceExistingItem(slot, MenuItems.Setting);
                    }
                }
                NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
                if (info != null) {
                    IStorage networkStorage = info.getStorage();
                    for (int slot : Item_Slots) {
                        int settingSlot = slot - 9;
                        ItemStack setting = inv.getItemInSlot(settingSlot);
                        ItemStack itemStack = inv.getItemInSlot(slot);
                        if (SlimefunUtils.isItemSimilar(setting, MenuItems.Setting, false, false)) {
                            if (itemStack != null && !itemStack.getType().isAir())
                                networkStorage.pushItem(itemStack);
                            continue;
                        }
                        int amount = 0;
                        if (itemStack != null && !itemStack.getType().isAir()) {
                            amount = itemStack.getAmount();
                        }
                        if (amount > setting.getAmount()) {
                            ItemStack toPush = itemStack.clone();
                            toPush.setAmount(amount - setting.getAmount());
                            networkStorage.pushItem(toPush);
                            if (toPush.getAmount() != 0) {
                                itemStack.setAmount(setting.getAmount() + toPush.getAmount());
                            }
                            continue;
                        }

                        ItemStack[] recieved = networkStorage.tryTakeItem(new ItemRequest(setting, setting.getAmount() - amount));
                        if (recieved.length != 0) {
                            if (itemStack != null && !itemStack.getType().isAir())
                                itemStack.setAmount(amount + recieved[0].getAmount());
                            else
                                inv.replaceExistingItem(slot, recieved[0]);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(Block b) {
                BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());

                if (inv != null) {
                    inv.dropItems(b.getLocation(), Item_Slots);
                }
            }
        };
    }

    private void constructMenu(BlockMenuPreset preset) {
        for (int slot : Boarder_Slots) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : Setting_Slots) {
            preset.addMenuClickHandler(slot, new ChestMenu.AdvancedMenuClickHandler() {
                @Override
                public boolean onClick(InventoryClickEvent inventoryClickEvent, Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                    Inventory inventory = inventoryClickEvent.getClickedInventory();
                    ItemStack current = inventory.getItem(slot);
                    if (current != null && SlimefunUtils.isItemSimilar(current, MenuItems.Setting, false, false)) {
                        if (itemStack != null && !itemStack.getType().isAir()) {
                            inventory.setItem(slot, itemStack);
                        }
                    }
                    else {
                        if (itemStack == null || itemStack.getType().isAir()) {
                            inventory.setItem(slot, MenuItems.Setting);
                        }
                        else {
                            inventory.setItem(slot, itemStack);
                        }
                    }

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
    public int[] getInputSlots() {
        return Item_Slots;
    }

    @Override
    public int[] getOutputSlots() {
        return Item_Slots;
    }
}
