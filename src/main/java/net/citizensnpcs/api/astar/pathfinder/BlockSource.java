package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.util.BoundingBox;

public abstract class BlockSource {
    public abstract BlockData getBlockDataAt(int x, int y, int z);

    public BlockData getBlockDataAt(Vector position) {
        return getBlockDataAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    public abstract BoundingBox getCollisionBox(int x, int y, int z);

    public BoundingBox getCollisionBox(Vector pos) {
        return getCollisionBox(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    public abstract Material getMaterialAt(int x, int y, int z);

    public Material getMaterialAt(Vector pos) {
        return getMaterialAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    public abstract boolean isYWithinBounds(int y);
}
