package me.ddggdd135.slimeae.utils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactive;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.SlimeAEPlugin;

public class SlimefunItemUtils {

    @CanIgnoreReturnValue
    public static @Nonnull SlimefunItem registerItem(@Nonnull SlimefunItem item) {
        item.register(SlimeAEPlugin.getInstance());
        return item;
    }

    public static void unregisterItem(SlimefunItem item) {
        if (item instanceof Radioactive) {
            Slimefun.getRegistry().getRadioactiveItems().remove(item);
        }

        if (item instanceof GEOResource geor) {
            Slimefun.getRegistry().getGEOResources().remove(geor.getKey());
        }

        Slimefun.getRegistry().getTickerBlocks().remove(item.getId());
        Slimefun.getRegistry().getEnabledSlimefunItems().remove(item);

        Slimefun.getRegistry().getSlimefunItemIds().remove(item.getId());
        Slimefun.getRegistry().getAllSlimefunItems().remove(item);
        Slimefun.getRegistry().getMenuPresets().remove(item.getId());
        Slimefun.getRegistry().getBarteringDrops().remove(item.getItem());
    }

    public static void unregisterAllItems(@Nonnull SlimefunAddon addon) {
        List<SlimefunItem> registeredItems = new ArrayList<>();
        for (SlimefunItem item : Slimefun.getRegistry().getAllSlimefunItems()) {
            if (item.getAddon() == addon) registeredItems.add(item);
        }
        for (SlimefunItem item : registeredItems) {
            unregisterItem(item);
        }
        registeredItems.clear();

        List<SlimefunItem> items = new ArrayList<>(Slimefun.getRegistry().getAllSlimefunItems());
        for (SlimefunItem item : items) {
            if (item.getAddon().getName().equals(SlimeAEPlugin.getInstance().getName())) {
                unregisterItem(item);
            }
        }
    }

    public static void unregisterItemGroups(@Nonnull SlimefunAddon addon) {
        Set<ItemGroup> itemGroups = new HashSet<>();
        for (ItemGroup itemGroup : Slimefun.getRegistry().getAllItemGroups()) {
            if (Objects.equals(itemGroup.getAddon(), addon)) {
                itemGroups.add(itemGroup);
            }
        }
        for (ItemGroup itemGroup : itemGroups) {
            unregisterItemGroup(itemGroup);
        }
    }

    public static void unregisterItemGroup(@Nullable ItemGroup itemGroup) {
        if (itemGroup == null) {
            return;
        }

        Slimefun.getRegistry().getAllItemGroups().remove(itemGroup);
    }
}
