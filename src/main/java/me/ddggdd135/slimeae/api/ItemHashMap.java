package me.ddggdd135.slimeae.api;

import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemHashMap<T> implements Map<ItemStack, T> {
    private HashMap<ItemTemplate, T> hashMap = new HashMap<>();

    public ItemHashMap(@NonNull Map<ItemStack, T> items) {
        putAll(items);
    }

    public ItemHashMap() {}

    @Override
    public int size() {
        return hashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof ItemStack itemStack) return hashMap.containsKey(new ItemTemplate(itemStack));
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return hashMap.containsValue(value);
    }

    @Override
    public T get(Object key) {
        if (key instanceof ItemStack itemStack) return hashMap.get(new ItemTemplate(itemStack));
        throw new IllegalArgumentException();
    }

    @Nullable @Override
    public T put(ItemStack key, T value) {
        return hashMap.put(new ItemTemplate(key), value);
    }

    @Override
    public T remove(Object key) {
        if (key instanceof ItemStack itemStack) return hashMap.remove(new ItemTemplate(itemStack));
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends ItemStack, ? extends T> m) {
        m.forEach((k, v) -> put(k, v));
    }

    @Override
    public void clear() {
        hashMap.clear();
    }

    @NotNull @Override
    public Set<ItemStack> keySet() {
        return hashMap.keySet().stream().map(x -> x.getHandle()).collect(Collectors.toSet());
    }

    @NotNull @Override
    public Collection<T> values() {
        return hashMap.values();
    }

    @NotNull @Override
    public Set<Entry<ItemStack, T>> entrySet() {
        return hashMap.entrySet().stream()
                .map(x -> new Entry<ItemStack, T>() {
                    @Override
                    public ItemStack getKey() {
                        return x.getKey().getHandle();
                    }

                    @Override
                    public T getValue() {
                        return x.getValue();
                    }

                    @Override
                    public T setValue(T value) {
                        return x.setValue(value);
                    }
                })
                .collect(Collectors.toSet());
    }
}
