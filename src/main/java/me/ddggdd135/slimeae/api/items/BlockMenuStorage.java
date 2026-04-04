package me.ddggdd135.slimeae.api.items;

import java.util.Arrays;
import java.util.HashSet;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.items.ItemStackCache;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BlockMenuStorage implements IStorage {
    private final BlockMenu blockMenu;
    private final boolean isReadOnly;

    public BlockMenuStorage(@Nonnull BlockMenu blockMenu, boolean isReadOnly) {
        this.blockMenu = blockMenu;
        this.isReadOnly = isReadOnly;
    }

    @Override
    public void pushItem(@Nonnull ItemStackCache itemStackCache) {
        if (isReadOnly) return;

        ItemStack itemStack = itemStackCache.getItemStack();

        int[] slots =
                blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.INSERT, itemStack);
        ItemStack result = blockMenu.pushItem(itemStack, slots);
        if (result != null && !result.getType().isAir()) itemStack.setAmount(result.getAmount());
        else itemStack.setAmount(0);
        blockMenu.markDirty();
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        return ItemUtils.contains(getStorageUnsafe(), requests);
    }

    @Nonnull
    @Override
    public ItemStorage takeItem(@Nonnull ItemRequest[] requests) {
        ItemHashMap<Long> remaining = ItemUtils.getAmounts(requests);
        ItemStorage found = new ItemStorage();

        for (ItemKey requestKey : new HashSet<>(remaining.sourceKeySet())) {
            long needed = remaining.getOrDefault(requestKey, 0L);
            if (needed <= 0) {
                continue;
            }

            int[] slots = blockMenu
                    .getPreset()
                    .getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.WITHDRAW, requestKey.getItemStack());
            for (int slot : slots) {
                if (needed <= 0) {
                    break;
                }

                ItemStack item = blockMenu.getItemInSlot(slot);
                if (item == null || item.getType().isAir()) {
                    continue;
                }

                ItemKey actualKey = new ItemKey(item);
                if (!actualKey.equals(requestKey)) {
                    continue;
                }

                long taken = Math.min(item.getAmount(), needed);
                found.addItem(actualKey, taken);
                needed -= taken;
                if (taken >= item.getAmount()) {
                    blockMenu.replaceExistingItem(slot, new ItemStack(Material.AIR));
                } else {
                    item.setAmount((int) (item.getAmount() - taken));
                }
            }

            remaining.putKey(requestKey, needed);
        }
        blockMenu.markDirty();
        return found;
    }

    @Override
    public @Nonnull ItemHashMap<Long> getStorageUnsafe() {
        int[] slots;
        try {
            slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.WITHDRAW, null);
        } catch (IllegalArgumentException e) {
            slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
        }

        return ItemUtils.getAmounts(
                Arrays.stream(slots).mapToObj(blockMenu::getItemInSlot).toArray(ItemStack[]::new));
    }
}
