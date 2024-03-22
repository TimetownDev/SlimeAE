package me.ddggdd135.slimeae.api.interfaces;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Set;

public interface IMEObject<TSelf extends SlimefunItem> {
    Set<BlockFace> Valid_Faces = Set.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN, BlockFace.SELF);

    void onNetworkUpdate(Block block, NetworkInfo networkInfo);
}
