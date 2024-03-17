package me.ddggdd135.slimeae.api.interfaces;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;

public interface IMEStorageObject<TSelf extends SlimefunItem> extends IMEObject<TSelf> {
    @Nonnull
    IStorage getStorage(Block block);
}
