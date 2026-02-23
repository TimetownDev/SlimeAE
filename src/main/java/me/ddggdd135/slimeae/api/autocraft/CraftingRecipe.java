package me.ddggdd135.slimeae.api.autocraft;

import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.slimeae.utils.CraftItemStackUtils;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CraftingRecipe {
    private final CraftType craftType;
    private final ItemStack[] input;
    private final ItemStack[] output;

    public CraftingRecipe(@Nonnull CraftType craftType, @Nonnull ItemStack[] input, @Nonnull ItemStack[] output) {
        this.craftType = craftType;
        this.input = CraftItemStackUtils.asCraftCopy(input);
        this.output = CraftItemStackUtils.asCraftCopy(output);
    }

    public CraftingRecipe(@Nonnull CraftType craftType, @Nonnull ItemStack[] input, @Nonnull ItemStack output) {
        this(craftType, input, new ItemStack[] {output});
    }

    @Nonnull
    public CraftType getCraftType() {
        return craftType;
    }

    @Nonnull
    public ItemStack[] getInput() {
        return input.clone();
    }

    @Nonnull
    public ItemStack[] getOutput() {
        return output.clone();
    }

    @Nonnull
    public ItemHashMap<Long> getInputAmounts() {
        return ItemUtils.getAmounts(toBukkitCopy(input));
    }

    @Nonnull
    public ItemHashMap<Long> getOutputAmounts() {
        return ItemUtils.getAmounts(toBukkitCopy(output));
    }

    /**
     * 将 CraftItemStack 数组转换为纯 Bukkit ItemStack 数组。
     * 避免 ItemKey 内部持有 CraftItemStack 的 NMS 引用，
     * 防止 NMS 对象被外部操作修改导致 ItemKey.equals() 失效。
     */
    private static ItemStack[] toBukkitCopy(ItemStack[] craftStacks) {
        ItemStack[] result = new ItemStack[craftStacks.length];
        for (int i = 0; i < craftStacks.length; i++) {
            ItemStack cs = craftStacks[i];
            if (cs == null || cs.getType().isAir()) {
                result[i] = new ItemStack(Material.AIR);
            } else {
                // 使用 new ItemStack(cs) 创建纯 Bukkit ItemStack 副本，
                // 与 CraftItemStack 完全解耦
                result[i] = new ItemStack(cs);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CraftingRecipe that = (CraftingRecipe) o;
        return craftType == that.craftType
                && ItemUtils.matchesAll(input, that.input, true)
                && ItemUtils.matchesAll(output, that.output, true);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(craftType);
        result = 31 * result + stableArrayHashCode(input);
        result = 31 * result + stableArrayHashCode(output);
        return result;
    }

    /**
     * 计算 ItemStack 数组的稳定 hashCode。
     * 对 AIR 类型统一使用固定 hashCode（与 equals 中 AIR amount=0 的处理保持一致），
     * 避免因 CraftItemStack 的可变 amount 导致 hashCode 不稳定。
     */
    private static int stableArrayHashCode(ItemStack[] array) {
        if (array == null) return 0;
        int h = 1;
        for (ItemStack item : array) {
            if (item == null || item.getType().isAir()) {
                h = 31 * h; // AIR/null 统一为 0
            } else {
                h = 31 * h + item.hashCode();
            }
        }
        return h;
    }
}
