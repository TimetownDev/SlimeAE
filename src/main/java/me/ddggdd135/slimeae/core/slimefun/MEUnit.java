package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.InventoryBlock;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/*
ME单元 相当于AE版本的原版大箱子
测试使用
 */
public class MEUnit extends SlimefunItem implements IMEStorageObject<MEUnit>, InventoryBlock {
    private static final int[] Slots = new int[] {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
        30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
    };

    public MEUnit(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this, this.getItem().getItemMeta().getDisplayName(), this::constructMenu);
        addItemHandler(onBlockBreak());
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
                    inv.dropItems(b.getLocation(), Slots);
                }
            }
        };
    }

    private void constructMenu(BlockMenuPreset preset) {
        preset.setSize(6 * 9);
    }

    @Override
    @Nullable public IStorage getStorage(Block block) {
        return new IStorage() {
            @Override
            public void pushItem(ItemStack[] itemStacks) {
                BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                if (inv == null) return;
                for (ItemStack itemStack : itemStacks) {
                    ItemStack result = inv.pushItem(itemStack, Slots);
                    if (result != null && !result.getType().isAir()) itemStack.setAmount(result.getAmount());
                    else itemStack.setAmount(0);
                }
            }

            @Override
            public boolean contains(ItemRequest[] requests) {
                BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                if (inv == null) return false;
                return ItemUtils.contains(getStorage(), requests);
            }

            @Override
            public ItemStack[] tryTakeItem(ItemRequest[] requests) {
                BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                if (inv == null) return new ItemStack[0];
                Map<ItemStack, Integer> amounts = ItemUtils.getAmounts(ItemUtils.createItems(requests));
                ItemStorage found = new ItemStorage();

                for (ItemStack itemStack : amounts.keySet()) {
                    for (int slot : Slots) {
                        ItemStack item = inv.getItemInSlot(slot);
                        if (item == null || item.getType().isAir()) continue;
                        if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                            if (item.getAmount() > amounts.get(itemStack)) {
                                found.addItem(ItemUtils.createItems(itemStack, amounts.get(itemStack)));
                                int rest = item.getAmount() - amounts.get(itemStack);
                                item.setAmount(rest);
                                break;
                            } else {
                                found.addItem(ItemUtils.createItems(itemStack, item.getAmount()));
                                inv.replaceExistingItem(slot, new ItemStack(Material.AIR));
                                int rest = amounts.get(itemStack) - item.getAmount();
                                if (rest != 0) amounts.put(itemStack, rest);
                                else {
                                    amounts.remove(itemStack);
                                    break;
                                }
                            }
                        }
                    }
                }
                return found.toItemStacks();
            }

            @Override
            public @NotNull Map<ItemStack, Integer> getStorage() {
                BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                if (inv == null) return new HashMap<>();
                return ItemUtils.getAmounts(inv.getContents());
            }

            @Override
            public int getEmptySlots() {
                BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                if (inv == null) return 0;
                int found = 0;
                for (int slot : Slots) {
                    ItemStack itemStack = inv.getItemInSlot(slot);
                    if (itemStack == null || itemStack.getType().isAir()) found += 1;
                }
                return found;
            }

            @Override
            public boolean canHasEmptySlots() {
                return true;
            }
        };
    }

    @Override
    public int[] getInputSlots() {
        return Slots;
    }

    @Override
    public int[] getOutputSlots() {
        return Slots;
    }
}
