package me.ddggdd135.slimeae.api.interfaces;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import javax.annotation.Nullable;
import org.bukkit.block.Block;

public interface IMEStorageObject<TSelf extends SlimefunItem> extends IMEObject<TSelf> {
    @Nullable IStorage getStorage(Block block);
}
