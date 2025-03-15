package me.ddggdd135.slimeae.core.slimefun.buses;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.abstracts.MEBus;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

/**
 * ME存储总线类
 * 用于连接外部存储设备到ME网络
 */
public class MEStorageBus extends MEBus implements IMEStorageObject {
    @Override
    public boolean isSynchronized() {
        return false;
    }

    /**
     * 构造一个新的ME存储总线
     *
     * @param itemGroup 物品所属的组
     * @param item 物品堆
     * @param recipeType 合成配方类型
     * @param recipe 合成配方
     */
    public MEStorageBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    /**
     * 获取输入槽位
     *
     * @return 输入槽位数组
     */
    @Override
    public int[] getInputSlots() {
        return new int[0];
    }

    /**
     * 获取输出槽位
     *
     * @return 输出槽位数组
     */
    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    /**
     * 获取连接的外部存储
     *
     * @param block 方块
     * @return 外部存储接口，如果无法获取则返回null
     */
    @Override
    @Nullable public IStorage getStorage(Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return null;
        BlockFace blockFace = getDirection(blockMenu);
        if (blockFace == BlockFace.SELF) return null;
        Block b = block.getRelative(blockFace);
        return ItemUtils.getStorage(b);
    }

    @Override
    public void onMEBusTick(Block block, SlimefunItem item, SlimefunBlockData data) {
        // ME存储总线不需要每tick处理，保持空实现
    }
}
