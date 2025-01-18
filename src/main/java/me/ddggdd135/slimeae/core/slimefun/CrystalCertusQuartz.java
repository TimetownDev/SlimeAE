package me.ddggdd135.slimeae.core.slimefun;

import javax.annotation.Nonnull;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;

/**
 * 赛特斯石英水晶资源生成器
 * 实现了 GEOResource 接口以支持 GEO Miner 挖掘
 */
public class CrystalCertusQuartz implements GEOResource {

    private final NamespacedKey key;
    private final ItemStack item;

    /**
     * 构造一个新的赛特斯石英水晶资源生成器
     *
     * @param plugin 插件实例
     * @param item 赛特斯石英水晶物品
     */
    public CrystalCertusQuartz(Plugin plugin, ItemStack item) {
        this.key = new NamespacedKey(plugin, "crystal_certus_quartz");
        this.item = item;
    }

    /**
     * 获取资源的唯一标识符
     * @return 资源的 NamespacedKey
     */
    @Override
    @Nonnull
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * 获取资源对应的物品
     * @return 资源物品的副本
     */
    @Override
    @Nonnull
    public ItemStack getItem() {
        return item.clone();
    }

    /**
     * 获取资源的显示名称
     * @return 资源名称
     */
    @Override
    @Nonnull
    public String getName() {
        return "赛特斯石英水晶";
    }

    /**
     * 检查是否可以通过 GEO Miner 获得
     * @return 是否可以通过 GEO Miner 获得
     */
    @Override
    public boolean isObtainableFromGEOMiner() {
        return true;
    }

    /**
     * 获取在指定环境和生物群系中的默认生成量
     *
     * @param environment 世界环境类型
     * @param biome 生物群系
     * @return 默认生成量
     */
    @Override
    public int getDefaultSupply(@Nonnull World.Environment environment, @Nonnull Biome biome) {
        if (environment == World.Environment.NORMAL) {
            return 5;
        } else {
            return 0;
        }
    }

    /**
     * 获取生成量的最大偏差值
     * @return 最大偏差值
     */
    @Override
    public int getMaxDeviation() {
        return 3;
    }
}
