package me.ddggdd135.slimeae.tasks;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingSession;
import me.ddggdd135.slimeae.api.interfaces.IMEController;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import me.ddggdd135.slimeae.api.items.StorageCollection;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.integrations.networks.QuantumStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

public class NetworkTickerTask implements Runnable {
    private int tickRate;
    private boolean halted = false;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile boolean paused = false;

    public void start(@Nonnull SlimeAEPlugin plugin) {
        this.tickRate = Slimefun.getCfg().getInt("URID.custom-ticker-delay") / 2;

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, this, 100L, tickRate);
    }

    private void reset() {
        running.set(false);
    }

    @Override
    public void run() {
        if (paused) {
            return;
        }

        try {
            // If this method is actually still running... DON'T
            if (!running.compareAndSet(false, true)) {
                return;
            }

            // Run our ticker code
            if (!halted) {
                Set<NetworkInfo> allNetworkData = new HashSet<>(SlimeAEPlugin.getNetworkData().AllNetworkData);
                for (NetworkInfo networkInfo : allNetworkData) {
                    NetworkInfo info = SlimeAEPlugin.getNetworkData().refreshNetwork(networkInfo.getController());
                    if (info == null) continue;

                    SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(info.getController());
                    if (slimefunBlockData == null || !info.getController().isChunkLoaded()) {
                        info.dispose();
                        continue;
                    }

                    SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
                    if (!(slimefunItem instanceof IMEController)) {
                        info.dispose();
                    }

                    ItemStorage tempStorage = info.getTempStorage();
                    Set<ItemStack> toPush =
                            new HashSet<>(tempStorage.getStorage().keySet());
                    for (ItemStack itemStack : toPush) {
                        ItemStack[] items =
                                tempStorage.tryTakeItem(new ItemRequest(itemStack, Integer.MAX_VALUE, true));
                        info.getStorage().pushItem(items);
                        items = ItemUtils.trimItems(items);
                        tempStorage.addItem(items, true);
                    }

                    StorageCollection storageCollection = (StorageCollection) info.getStorage();
                    for (IStorage storage : storageCollection.getStorages()) {
                        if (storage instanceof QuantumStorage quantumStorage) {
                            quantumStorage.sync();
                        }
                    }

                    new HashSet<>(info.getChildren()).forEach(x -> {
                        IMEObject imeObject =
                                SlimeAEPlugin.getNetworkData().AllNetworkBlocks.get(x);
                        if (imeObject == null) return;
                        imeObject.onNetworkTick(x.getBlock(), info);
                    });

                    // tick autoCrafting
                    Set<AutoCraftingSession> sessions = new HashSet<>(info.getCraftingSessions());
                    for (AutoCraftingSession session : sessions) {
                        if (!session.hasNext()) {
                            info.getCraftingSessions().remove(session);
                            Slimefun.runSync(() -> {
                                session.getMenu().getInventory().getViewers().forEach(HumanEntity::closeInventory);
                            });
                        } else session.moveNext(1024);
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
