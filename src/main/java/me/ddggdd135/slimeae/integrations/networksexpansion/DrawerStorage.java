package me.ddggdd135.slimeae.integrations.networksexpansion;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.StorageUnitData;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class DrawerStorage implements IStorage {
    private StorageUnitData data;
    private Block block;
    private boolean isReadOnly;

    public DrawerStorage(@Nonnull Block block) {
        this(block, false);
    }

    public DrawerStorage(@Nonnull Block block, boolean isReadOnly) {
        if (!SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded())
            throw new RuntimeException("NetworksExpansion is not loaded");
        this.block = block;
        data = NetworksDrawer.getStorageData(block.getLocation());
        this.isReadOnly = isReadOnly;
    }

    @Override
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        if (!isReadOnly && data != null)
            data.depositItemStack(itemStacks, NetworksDrawer.isLocked(block.getLocation()));
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        if (data == null) return false;
        List<ItemContainer> items = data.getStoredItems();
        for (ItemRequest request : requests) {
            boolean found = false;
            for (ItemContainer itemContainer : items) {
                if (SlimefunUtils.isItemSimilar(request.getTemplate(), itemContainer.getSample(), true, false)) {
                    if (itemContainer.getAmount() < request.getAmount()) return false;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
        if (data == null) return new ItemStack[0];
        io.github.sefiraat.networks.network.stackcaches.ItemRequest[] networksRequests =
                SlimeAEPlugin.getNetworksIntegration().asNetworkRequests(requests);
        ItemStorage storage = new ItemStorage();
        for (io.github.sefiraat.networks.network.stackcaches.ItemRequest request : networksRequests) {
            ItemStack itemStack = data.requestItem(request);
            if (itemStack != null && !itemStack.getType().isAir()) storage.addItem(itemStack);
        }
        return storage.toItemStacks();
    }

    @Override
    @Nonnull
    public Map<ItemStack, Long> getStorage() {
        Map<ItemStack, Long> storage = new HashMap<>();
        if (data == null) return storage;
        for (ItemContainer itemContainer : data.getStoredItems()) {
            storage.put(itemContainer.getSample(), (long) itemContainer.getAmount());
        }

        return storage;
    }

    @Override
    public int getTier(@Nonnull ItemStack itemStack) {
        for (ItemContainer itemContainer : data.getStoredItems()) {
            if (itemStack.getType() == itemContainer.getWrapper().getType()) {
                return 3000;
            }
        }

        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DrawerStorage that)) return false;
        return Objects.equals(data.getId(), that.data.getId());
    }

    @Override
    public int hashCode() {
        if (data == null) return 0;
        return data.getId();
    }
}
