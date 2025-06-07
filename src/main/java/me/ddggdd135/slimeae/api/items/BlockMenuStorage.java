package me.ddggdd135.slimeae.api.items;

import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.Arrays;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
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
        ItemHashMap<Long> amounts = ItemUtils.getAmounts(ItemUtils.createItems(requests));
        ItemStorage found = new ItemStorage();

        for (ItemStack itemStack : amounts.keySet()) {
            int[] slots = blockMenu
                    .getPreset()
                    .getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.WITHDRAW, itemStack);
            for (int slot : slots) {
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
    public @Nonnull ItemHashMap<Long> getStorageUnsafe() {
        int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);

        return ItemUtils.getAmounts(
                Arrays.stream(slots).mapToObj(blockMenu::getItemInSlot).toArray(ItemStack[]::new));
    }
}
