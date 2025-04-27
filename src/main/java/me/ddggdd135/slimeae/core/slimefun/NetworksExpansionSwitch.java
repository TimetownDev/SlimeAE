package me.ddggdd135.slimeae.core.slimefun;

import com.balugaq.netex.api.enums.FeedbackType;
import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.integrations.networks.NetworksStorage;
import me.ddggdd135.slimeae.integrations.networks.StorageToBarrelWrapper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/*
 * 只能在网络拓展里用
 */
public class NetworksExpansionSwitch extends NetworkObject implements IMEStorageObject {
    private static boolean allowNetworks2AE;
    private static boolean allowAE2Networks;

    public NetworksExpansionSwitch(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe, NodeType.STORAGE_MONITOR);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}

    @Override
    @Nullable public IStorage getStorage(Block block) {
        if (!allowNetworks2AE) return null;
        NodeDefinition definition = NetworkStorage.getNode(block.getLocation());
        if (definition == null || definition.getNode() == null) {
            if (SlimeAEPlugin.getNetworksExpansionIntegration().isLoaded())
                sendFeedback(block.getLocation(), FeedbackType.NO_NETWORK_FOUND);
            return null;
        }

        return new NetworksStorage(definition.getNode().getRoot());
    }

    public Set<BarrelIdentity> wrapIStorageAsBarrelIdentities(@Nonnull Location location, @Nullable IStorage storage) {
        if (storage == null) {
            return new HashSet<>();
        }

        ItemHashMap<Long> itemMap = storage.getStorageUnsafe();

        Set<BarrelIdentity> result = new HashSet<>();
        for (ItemKey key : itemMap.sourceKeySet()) {
            result.add(new StorageToBarrelWrapper(location, storage, key));
        }

        return result;
    }

    public static void reloadConfig() {
        allowNetworks2AE = SlimeAEPlugin.getInstance()
                .getConfig()
                .getBoolean("networks-expansion-switch.allow-networks-to-ae", true);
        allowAE2Networks = SlimeAEPlugin.getInstance()
                .getConfig()
                .getBoolean("networks-expansion-switch.allow-ae-to-networks", true);
    }

    public static boolean isAllowAE2Networks() {
        return allowAE2Networks;
    }

    public static boolean isAllowNetworks2AE() {
        return allowNetworks2AE;
    }
}
