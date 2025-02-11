package me.ddggdd135.slimeae.tasks;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.StorageCollection;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.integrations.networks.QuantumStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

public class NetworkRefreshTask implements Runnable {
    private int tickRate;
    private boolean halted = false;
    private boolean running = false;

    private volatile boolean paused = false;

    public void start(@Nonnull SlimeAEPlugin plugin) {
        this.tickRate = Slimefun.getCfg().getInt("URID.custom-ticker-delay");

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, this, tickRate, tickRate);
    }

    private void reset() {
        running = false;
    }

    @Override
    public void run() {
        if (paused) {
            return;
        }

        try {
            // If this method is actually still running... DON'T
            if (running) {
                return;
            }

            running = true;
            // Run our ticker code
            if (!halted) {

                Set<NetworkInfo> allNetworkData = new HashSet<>(SlimeAEPlugin.getNetworkData().AllNetworkData);

                for (NetworkInfo networkInfo : allNetworkData) {
                    NetworkInfo info = SlimeAEPlugin.getNetworkData().refreshNetwork(networkInfo.getController());
                    IStorage tempStorage = info.getTempStorage();
                    Set<ItemStack> toPush =
                            new HashSet<>(tempStorage.getStorage().keySet());
                    for (ItemStack itemStack : toPush) {
                        ItemStack[] items =
                                tempStorage.tryTakeItem(new ItemRequest(itemStack, Integer.MAX_VALUE, true));
                        info.getStorage().pushItem(items);
                        items = ItemUtils.trimItems(items);
                        tempStorage.pushItem(items);
                    }

                    StorageCollection storageCollection = (StorageCollection) networkInfo.getStorage();
                    for (IStorage storage : storageCollection.getStorages()) {
                        if (storage instanceof QuantumStorage quantumStorage) {
                            quantumStorage.sync();
                        }
                    }
                }
            }
        } catch (Exception | LinkageError x) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(Level.SEVERE, x, () -> "An Exception was caught while refresh Networks for SlimeAE");
        } finally {
            reset();
        }
    }

    public boolean isHalted() {
        return halted;
    }

    public void halt() {
        halted = true;
    }

    public int getTickRate() {
        return tickRate;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
