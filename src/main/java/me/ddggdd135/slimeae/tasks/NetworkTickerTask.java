package me.ddggdd135.slimeae.tasks;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingTask;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.StorageCollection;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.integrations.networks.QuantumStorage;
import org.bukkit.Bukkit;

public class NetworkTickerTask implements Runnable {
    private int tickRate;
    private boolean halted = false;

    private volatile boolean paused = false;
    private volatile long tick = 0;

    public void start(@Nonnull SlimeAEPlugin plugin) {
        this.tickRate = Slimefun.getCfg().getInt("URID.custom-ticker-delay") / 2;
        run();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        run0();

        long elapsed = System.currentTimeMillis() - startTime;
        long nextDelay = Math.max(tickRate * 50L - elapsed, 0) / 50;

        Bukkit.getScheduler().runTaskLaterAsynchronously(SlimeAEPlugin.getInstance(), this, nextDelay);
    }

    public void run0() {
        if (paused) {
            return;
        }

        try {
            // Run our ticker code
            if (!halted) {
                tick++;

                Set<NetworkInfo> allNetworkData = new HashSet<>(SlimeAEPlugin.getNetworkData().AllNetworkData);
                for (NetworkInfo info : allNetworkData) {
                    if (tick % 16 == 0) info = SlimeAEPlugin.getNetworkData().refreshNetwork(info.getController());
                    SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(info.getController());
                    if (slimefunBlockData == null || !info.getController().isChunkLoaded()) {
                        info.dispose();
                        continue;
                    }

                    SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
                    if (!(slimefunItem instanceof IMEController)) {
                        info.dispose();
                    }
                    info.getVirtualCraftingDeviceUsed().clear();
                    info.updateTempStorage();

                    StorageCollection storageCollection = (StorageCollection) info.getStorage();
                    for (IStorage storage : storageCollection.getStorages()) {
                        if (storage instanceof QuantumStorage quantumStorage) {
                            quantumStorage.sync();
                        }
                    }

                    NetworkInfo finalInfo = info;
                    new HashSet<>(info.getChildren()).forEach(x -> {
                        IMEObject imeObject =
                                SlimeAEPlugin.getNetworkData().AllNetworkBlocks.get(x);
                        if (imeObject == null) return;
                        imeObject.onNetworkTick(x.getBlock(), finalInfo);
                    });

                    // tick autoCrafting
                    Set<AutoCraftingTask> tasks = new HashSet<>(info.getAutoCraftingSessions());
                    for (AutoCraftingTask task : tasks) {
                        if (!task.hasNext()) {
                            task.dispose();
                        } else task.moveNext(1024);
                    }
                    info.updateAutoCraftingMenu();
                }
            }
        } catch (Exception | LinkageError x) {
            SlimeAEPlugin.getInstance()
                    .getLogger()
                    .log(
                            Level.SEVERE,
                            x,
                            () -> "An Exception was caught while ticking the Network Tickers Task for SlimeAE");
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
