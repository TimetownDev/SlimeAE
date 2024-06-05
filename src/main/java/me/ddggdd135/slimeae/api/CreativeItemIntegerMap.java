package me.ddggdd135.slimeae.api;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CreativeItemIntegerMap implements Map<ItemStack, Integer> {
    public static final Set<ItemStack> allItemStacks;
    public static final Set<Map.Entry<ItemStack, Integer>> allAmount;
    public static final int amount = 64 * 1024;

    public CreativeItemIntegerMap(@Nonnull Map<ItemStack, Integer> items) {}

    public CreativeItemIntegerMap() {}

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean containsKey(Object key) {
        return key instanceof ItemStack;
    }

    public boolean containsValue(Object value) {
        return value instanceof ItemStack;
    }

    public Integer get(Object key) {
        return amount;
    }

    public Integer put(ItemStack key, Integer value) {
        return amount;
    }

    public Integer remove(Object key) {
        if (key instanceof ItemStack) {
            return amount;
        } else {
            return 0;
        }
    }

    public void putAll(@Nonnull Map<? extends ItemStack, ? extends Integer> m) {}

    public void clear() {}

    @Nonnull
    public Set<ItemStack> keySet() {
        return allItemStacks;
    }

    @Nonnull
    public Collection<Integer> values() {
        return new HashSet<>();
    }

    @Nonnull
    public Set<Map.Entry<ItemStack, Integer>> entrySet() {
        return allAmount;
    }

    public static final class Entry implements Map.Entry<ItemStack, Integer> {
        private final ItemStack key;

        @Override
        public ItemStack getKey() {
            return key;
        }

        @Override
        public Integer getValue() {
            return amount;
        }

        @Override
        public Integer setValue(Integer value) {
            return amount;
        }

        public Entry(ItemStack key) {
            this.key = key;
        }
    }

    static {
        allItemStacks = new HashSet<>();
        for (Material material : Material.values()) {
            if (!material.isItem() || material.isAir()) continue;
            allItemStacks.add(new AdvancedCustomItemStack(material));
        }

        for (SlimefunItem slimefunItem : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            allItemStacks.add(slimefunItem.getItem().clone());
        }

        allAmount = new HashSet<>();
        for (ItemStack itemStack : allItemStacks) {
            allAmount.add(new Entry(itemStack));
        }
    }
}
