package me.ddggdd135.slimeae.api.items;

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

public class CreativeItemMap implements Map<ItemStack, Long> {
    public static final Set<ItemStack> allItemStacks;
    public static final Set<Map.Entry<ItemStack, Long>> allAmount;
    public static final long amount = 64 * 1024;

    public CreativeItemMap(@Nonnull Map<ItemStack, Long> items) {}

    public CreativeItemMap() {}

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

    public Long get(Object key) {
        return amount;
    }

    public Long put(ItemStack key, Long value) {
        return amount;
    }

    public Long remove(Object key) {
        if (key instanceof ItemStack) {
            return amount;
        } else {
            return 0L;
        }
    }

    public void putAll(@Nonnull Map<? extends ItemStack, ? extends Long> m) {}

    public void clear() {}

    @Nonnull
    public Set<ItemStack> keySet() {
        return allItemStacks;
    }

    @Nonnull
    public Collection<Long> values() {
        return new HashSet<>();
    }

    @Nonnull
    public Set<Map.Entry<ItemStack, Long>> entrySet() {
        return allAmount;
    }

    public static final class Entry implements Map.Entry<ItemStack, Long> {
        private final ItemStack key;

        @Override
        public ItemStack getKey() {
            return key;
        }

        @Override
        public Long getValue() {
            return amount;
        }

        @Override
        public Long setValue(Long value) {
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
