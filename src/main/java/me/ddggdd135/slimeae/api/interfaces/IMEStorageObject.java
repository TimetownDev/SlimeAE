package me.ddggdd135.slimeae.api.interfaces;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IMEStorageObject<TSelf extends SlimefunItem> extends IMEObject<TSelf> {
    @Nullable
    IStorage getStorage(Block block);
}
