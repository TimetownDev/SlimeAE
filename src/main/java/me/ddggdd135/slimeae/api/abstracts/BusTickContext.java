package me.ddggdd135.slimeae.api.abstracts;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BusTickContext {
    private final Block block;
    private final Location cachedLocation;
    private final BlockMenu blockMenu;
    private final NetworkInfo networkInfo;
    private final IStorage networkStorage;
    private final BlockFace direction;
    private final Set<BlockFace> directions;
    private final int chainDistance;
    private final int tickMultiplier;

    private BusTickContext(Builder builder) {
        this.block = builder.block;
        this.cachedLocation = builder.block != null ? builder.block.getLocation() : null;
        this.blockMenu = builder.blockMenu;
        this.networkInfo = builder.networkInfo;
        this.networkStorage = builder.networkStorage;
        this.direction = builder.direction;
        this.directions = builder.directions;
        this.chainDistance = builder.chainDistance;
        this.tickMultiplier = builder.tickMultiplier;
    }

    @Nonnull
    public Block getBlock() {
        return block;
    }

    @Nonnull
    public Location getLocation() {
        return cachedLocation;
    }

    @Nonnull
    public BlockMenu getBlockMenu() {
        return blockMenu;
    }

    @Nullable public NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    @Nullable public IStorage getNetworkStorage() {
        return networkStorage;
    }

    @Nonnull
    public BlockFace getDirection() {
        return direction;
    }

    @Nullable public Set<BlockFace> getDirections() {
        return directions;
    }

    public int getChainDistance() {
        return chainDistance;
    }

    public int getTickMultiplier() {
        return tickMultiplier;
    }

    public boolean isValid() {
        return blockMenu != null && networkInfo != null && networkStorage != null;
    }

    public static class Builder {
        private Block block;
        private BlockMenu blockMenu;
        private NetworkInfo networkInfo;
        private IStorage networkStorage;
        private BlockFace direction = BlockFace.SELF;
        private Set<BlockFace> directions;
        private int chainDistance = 0;
        private int tickMultiplier = 1;

        public Builder block(Block block) {
            this.block = block;
            return this;
        }

        public Builder blockMenu(BlockMenu menu) {
            this.blockMenu = menu;
            return this;
        }

        public Builder networkInfo(NetworkInfo info) {
            this.networkInfo = info;
            if (info != null) this.networkStorage = info.getStorage();
            return this;
        }

        public Builder direction(BlockFace face) {
            this.direction = face;
            return this;
        }

        public Builder directions(Set<BlockFace> faces) {
            this.directions = faces;
            return this;
        }

        public Builder chainDistance(int dist) {
            this.chainDistance = dist;
            return this;
        }

        public Builder tickMultiplier(int multiplier) {
            this.tickMultiplier = multiplier;
            return this;
        }

        public BusTickContext build() {
            return new BusTickContext(this);
        }
    }
}
