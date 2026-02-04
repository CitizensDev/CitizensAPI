package net.citizensnpcs.api.astar;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Supplier;

import org.bukkit.util.Vector;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;

/**
 * A base implementation of {@link AStarStorage} that uses a {@link PriorityQueue} for the frontier and {@link HashMap}s
 * for the open/closed sets.
 */
public class PackedAStarStorage implements AStarStorage {
    private final Long2FloatOpenHashMap closed = new Long2FloatOpenHashMap();
    private final Long2FloatOpenHashMap open = new Long2FloatOpenHashMap();
    private final Queue<AStarNode> queue = new PriorityQueue<>(128);

    private PackedAStarStorage() {
        closed.defaultReturnValue(Float.POSITIVE_INFINITY);
        open.defaultReturnValue(Float.POSITIVE_INFINITY);
    }

    @Override
    public void close(AStarNode node) {
        long key = packPosition((VectorNode) node);
        open.remove(key);
        closed.put(key, node.g);
    }

    @Override
    public AStarNode getBestNode() {
        return queue.peek();
    }

    @Override
    public void open(AStarNode node) {
        long key = packPosition((VectorNode) node);
        queue.offer(node);
        open.put(key, node.g);
        closed.remove(key);
    }

    private long packPosition(VectorNode node) {
        Vector vector = node.getVector();
        return ((long) vector.getBlockX() & 0x3FFFFFF) << 38 | ((long) vector.getBlockZ() & 0x3FFFFFF) << 12
                | ((long) vector.getBlockY() & 0xFFF);
    }

    @Override
    public AStarNode removeBestNode() {
        return queue.poll();
    }

    @Override
    public boolean shouldExamine(AStarNode node) {
        long key = packPosition((VectorNode) node);
        float openG = open.get(key);
        if (!Float.isInfinite(openG) && openG > node.g) {
            open.remove(key);
            openG = Float.POSITIVE_INFINITY;
        }
        float closedG = closed.get(key);
        if (!Float.isInfinite(closedG) && closedG > node.g) {
            closed.remove(key);
            closedG = Float.POSITIVE_INFINITY;
        }
        return Float.isInfinite(closedG) && Float.isInfinite(openG);
    }

    @Override
    public String toString() {
        return "PackedAStarStorage [closed=" + closed + ", open=" + open + "]";
    }

    public static final Supplier<AStarStorage> FACTORY = PackedAStarStorage::new;
}
