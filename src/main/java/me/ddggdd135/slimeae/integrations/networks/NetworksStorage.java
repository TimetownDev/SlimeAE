package me.ddggdd135.slimeae.integrations.networks;

import io.github.sefiraat.networks.network.NetworkRoot;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import org.bukkit.inventory.ItemStack;

public class NetworksStorage implements IStorage {
    private final NetworkRoot networkRoot;

    public NetworksStorage(NetworkRoot networkRoot) {
        this.networkRoot = networkRoot;
    }

    public NetworkRoot getNetworkRoot() {
        return networkRoot;
    }

    @Override
    public void pushItem(@Nonnull ItemStack[] itemStacks) {
        SlimeAEPlugin.getNetworksExpansionIntegration().doBannedTask(networkRoot, root -> {
            for (ItemStack itemStack : itemStacks) {
                root.addItemStack(itemStack);
            }
        });
    }

    @Override
    public boolean contains(@Nonnull ItemRequest[] requests) {
        io.github.sefiraat.networks.network.stackcaches.ItemRequest[] networkRequests =
                SlimeAEPlugin.getNetworksIntegration().asNetworkRequests(requests);
        return SlimeAEPlugin.getNetworksExpansionIntegration().doBannedTask(networkRoot, root -> {
            for (io.github.sefiraat.networks.network.stackcaches.ItemRequest request : networkRequests) {
                if (!root.contains(request)) return false;
            }

            return true;
        });
    }

    @Override
    @Nonnull
    public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
        io.github.sefiraat.networks.network.stackcaches.ItemRequest[] networkRequests =
                SlimeAEPlugin.getNetworksIntegration().asNetworkRequests(requests);
        return SlimeAEPlugin.getNetworksExpansionIntegration().doBannedTask(networkRoot, root -> {
            return Arrays.stream(networkRequests).map(root::getItemStack).toArray(ItemStack[]::new);
        });
    }

    @Override
    @Nonnull
    public Map<ItemStack, Integer> getStorage() {
        return SlimeAEPlugin.getNetworksExpansionIntegration()
                .doBannedTask(networkRoot, NetworkRoot::getAllNetworkItems);
    }

    @Override
    public int getEmptySlots() {
        return 0;
    }
}
