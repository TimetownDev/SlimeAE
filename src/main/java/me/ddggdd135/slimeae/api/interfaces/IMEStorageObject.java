package me.ddggdd135.slimeae.api.interfaces;

import javax.annotation.Nullable;
import org.bukkit.block.Block;

public interface IMEStorageObject extends IMEObject {
    @Nullable IStorage getStorage(Block block);
}
