package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/**
 * ME单元类，相当于AE版本的原版大箱子
 * 实现了基本的物品存储功能
 */
public class MEUnit extends SlimefunItem implements IMEStorageObject, InventoryBlock {
    /**
     * ME单元的所有可用槽位
     */
    private static final int[] Slots = new int[] {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
        30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
    };

    /**
     * 构造一个新的ME单元
     *
     * @param itemGroup 物品所属的组
     * @param item 物品堆
     * @param recipeType 合成配方类型
     * @param recipe 合成配方
     */
    public MEUnit(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    /**
     * 当网络更新时调用的方法
     *
     * @param block 方块
     * @param networkInfo 网络信息
     */
    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    /**
     * 获取方块破坏处理器
     *
     * @return 方块破坏处理器
     */
    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());

                if (blockMenu != null) {
                    blockMenu.dropItems(b.getLocation(), Slots);
                }
            }
        };
    }

    @Override
    @Nullable public IStorage getStorage(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        return new IStorage() {
            @Override
            public void pushItem(@Nonnull ItemStackCache itemStackCache) {
                ItemStack itemStack = itemStackCache.getItemStack();

                if (blockMenu == null) return;
                ItemStack result = blockMenu.pushItem(itemStack, Slots);
                if (result != null && !result.getType().isAir()) itemStack.setAmount(result.getAmount());
                else itemStack.setAmount(0);
                blockMenu.markDirty();
            }

            @Override
            public boolean contains(@Nonnull ItemRequest[] requests) {
                if (blockMenu == null) return false;
                return ItemUtils.contains(getStorage(), requests);
            }

            @Nonnull
            @Override
            public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
                if (blockMenu == null) return new ItemStorage();
                ItemHashMap<Long> amounts = ItemUtils.getAmounts(ItemUtils.createItems(requests));
                ItemStorage found = new ItemStorage();

                for (ItemStack itemStack : amounts.keySet()) {
                    for (int slot : Slots) {
                        ItemStack item = blockMenu.getItemInSlot(slot);
                        if (item == null || item.getType().isAir()) continue;
                        if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                            if (item.getAmount() > amounts.get(itemStack)) {
                                found.addItem(ItemUtils.createItems(itemStack, amounts.get(itemStack)));
                                long rest = item.getAmount() - amounts.get(itemStack);
                                item.setAmount((int) rest);
                                break;
                            } else {
                                found.addItem(ItemUtils.createItems(itemStack, item.getAmount()));
                                blockMenu.replaceExistingItem(slot, new ItemStack(Material.AIR));
                                long rest = amounts.get(itemStack) - item.getAmount();
                                if (rest != 0) amounts.put(itemStack, rest);
                                else break;
                            }
                        }
                    }
                }
                blockMenu.markDirty();
                return found;
            }

            @Override
            public @Nonnull ItemHashMap<Long> getStorage() {
                if (blockMenu == null) return new ItemHashMap<>();
                return ItemUtils.getAmounts(blockMenu.getContents());
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

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@Nonnull BlockMenuPreset preset) {
        preset.setSize(6 * 9);
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {}

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}
}
