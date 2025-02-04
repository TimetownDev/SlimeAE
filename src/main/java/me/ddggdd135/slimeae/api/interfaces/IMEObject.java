package me.ddggdd135.slimeae.api.interfaces;

import java.util.Set;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public interface IMEObject {
    Set<BlockFace> Valid_Faces = Set.of(
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.SELF);

    void onNetworkUpdate(Block block, NetworkInfo networkInfo);

    void onNetworkTick(Block block, NetworkInfo networkInfo);

    default void onNetworkTimeConsumingTick(Block block, NetworkInfo networkInfo) {}
}
