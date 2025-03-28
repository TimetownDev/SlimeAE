package me.ddggdd135.slimeae.integrations.networks;

import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.Objects;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class BarrelIdentityToStorageWrapper implements IStorage {
    private final BarrelIdentity barrelIdentity;

    public BarrelIdentityToStorageWrapper(BarrelIdentity barrelIdentity) {
        this.barrelIdentity = barrelIdentity;
    }

    @Override
    public void pushItem(@Nonnull ItemStack itemStack) {
        barrelIdentity.depositItemStack(itemStack);
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        int amount = (int) barrelIdentity.getAmount();
        for (ItemRequest request : requests) {
            if (!SlimefunUtils.isItemSimilar(
                    request.getKey().getItemStack(), barrelIdentity.getItemStack(), true, false)) return false;
            if (request.getAmount() > amount) return false;
            amount += request.getAmount();
        }

        return true;
    }

    @Override
    @Nonnull
    public ItemStorage tryTakeItem(@Nonnull ItemRequest[] requests) {
        ItemStorage storage = new ItemStorage();

        for (ItemRequest request : requests) {
            if (!SlimefunUtils.isItemSimilar(
                    request.getKey().getItemStack(), barrelIdentity.getItemStack(), true, false)) continue;
            long amount = request.getAmount();
            long rest = barrelIdentity.getAmount();
            if (rest > amount) {
                rest -= amount;
                barrelIdentity.setAmount(rest);
                storage.addItem(new ItemKey(barrelIdentity.getItemStack().asOne()), amount);
                continue;
            }

            barrelIdentity.setAmount(0);
            storage.addItem(new ItemKey(barrelIdentity.getItemStack().asOne()), rest);
        }

        return storage;
    }

    @Override
    @Nonnull
    public ItemHashMap<Long> getStorage() {
        ItemHashMap<Long> map = new ItemHashMap<>();
        ItemStack itemStack = barrelIdentity.getItemStack();
        if (itemStack != null && !itemStack.getType().isAir()) {
            map.put(itemStack.asOne(), barrelIdentity.getAmount() - 1);
        }

        return map;
    }

    @Override
    public int getTier(@Nonnull ItemKey itemStack) {
        if (SlimefunUtils.isItemSimilar(itemStack.getItemStack(), barrelIdentity.getItemStack(), true, false))
            return 2000;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarrelIdentityToStorageWrapper that = (BarrelIdentityToStorageWrapper) o;
        return Objects.equals(barrelIdentity, that.barrelIdentity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(barrelIdentity);
    }
}
