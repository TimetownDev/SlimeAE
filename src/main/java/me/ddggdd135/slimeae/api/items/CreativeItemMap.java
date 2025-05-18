package me.ddggdd135.slimeae.api.items;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CreativeItemMap extends ItemHashMap<Long> {
    public static final ItemHashMap<Long> allItemStacks;
    public static final long amount = Integer.MAX_VALUE;

    public CreativeItemMap(@Nonnull ItemHashMap<Long> items) {}

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
        return allItemStacks.keySet();
    }

    @Nonnull
    public Set<ItemKey> sourceKeySet() {
        return allItemStacks.sourceKeySet();
    }

    @Nonnull
    public Set<Map.Entry<ItemKey, Long>> keyEntrySet() {
        return allItemStacks.keyEntrySet();
    }

    @Nonnull
    public Collection<Long> values() {
        return allItemStacks.values();
    }

    @Nonnull
    public Set<Map.Entry<ItemStack, Long>> entrySet() {
        return allItemStacks.entrySet();
    }

    static {
        allItemStacks = new ItemHashMap<>();
        for (Material material : Material.values()) {
            if (!material.isItem() || material.isAir()) continue;
            allItemStacks.put(new AdvancedCustomItemStack(material), amount);
        }

        for (SlimefunItem slimefunItem : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            allItemStacks.put(slimefunItem.getItem().clone(), amount);
        }
    }
}
