package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.astar.AStarNode;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer.PassableState;

public class VectorNode extends AStarNode implements PathPoint {
    private float blockCost = -1;
    List<PathCallback> callbacks;
    private final PathInfo info;
    Vector location;

    public VectorNode(VectorGoal goal, Location location, BlockSource source, BlockExaminer... examiners) {
        this(null, goal, location.toVector(), source, examiners);
    }

    public VectorNode(VectorNode parent, Vector location, PathInfo info) {
        super(parent);
        this.location = location.setX(location.getBlockX()).setY(location.getBlockY()).setZ(location.getBlockZ());
        this.info = info;
    }

    public VectorNode(VectorNode parent, VectorGoal goal, Vector location, BlockSource source,
            BlockExaminer... examiners) {
        this(parent, location, new PathInfo(source, examiners == null ? new BlockExaminer[] {} : examiners, goal));
    }

    @Override
    public void addCallback(PathCallback callback) {
        if (callbacks == null) {
            callbacks = Lists.newArrayList();
        }
        callbacks.add(callback);
    }

    @Override
    public Plan buildPlan() {
        Iterable<VectorNode> parents = getParents();
        return new Path(parents);
    }

    @Override
    public VectorNode createAtOffset(Vector mod) {
    	VectorNode node = new VectorNode(this, mod, info);
    	node.fixHalfBlocks();
    	return node;
    }

    public float distance(VectorNode to) {
        return (float) location.distance(to.location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VectorNode other = (VectorNode) obj;
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        return true;
    }

    private float getBlockCost() {
        if (blockCost == -1) {
            blockCost = 0;
            for (BlockExaminer examiner : info.examiners) {
                blockCost += examiner.getCost(info.blockSource, this);
            }
        }
        return blockCost;
    }
    
    
    private void fixHalfBlocks() {
        Material type = info.blockSource.getMaterialAt(location);
        if (MinecraftBlockExaminer.isHalfBlock(type)) {
        	location.setY(location.getBlockY() + 0.5);
        }
    }

    @Override
    public Vector getGoal() {
        return info.goal.goal;
    }

    @Override
    public Iterable<AStarNode> getNeighbours() {
        List<PathPoint> neighbours = null;
        for (BlockExaminer examiner : info.examiners) {
            if (examiner instanceof NeighbourGeneratorBlockExaminer) {
                neighbours = ((NeighbourGeneratorBlockExaminer) examiner).getNeighbours(info.blockSource, this);
                break;
            }
        }
        if (neighbours == null) {
            neighbours = getNeighbours(info.blockSource, this);
        }
        List<AStarNode> nodes = Lists.newArrayList();
        for (PathPoint sub : neighbours) {
            if (!isPassable(sub))
                continue;
            nodes.add((AStarNode) sub);
        }
        return nodes;
    }

    public List<PathPoint> getNeighbours(BlockSource source, PathPoint point) {
        List<PathPoint> neighbours = Lists.newArrayList();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    if (x != 0 && z != 0)
                        continue;
                    Vector mod = location.clone().add(new Vector(x, y, z));
                    if (mod.equals(location))
                        continue;
                    neighbours.add(point.createAtOffset(mod));
                }
            }
        }
        return neighbours;
    }

    @Override
    public PathPoint getParentPoint() {
        return (PathPoint) getParent();
    }

    @Override
    public Vector getVector() {
        return location.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime + ((location == null) ? 0 : location.hashCode());
    }

    public float heuristicDistance(Vector goal) {
        return (float) (location.distance(goal) + getBlockCost()) * TIEBREAKER;
    }

    private boolean isPassable(PathPoint mod) {
        boolean passable = false;
        for (BlockExaminer examiner : info.examiners) {
            PassableState state = examiner.isPassable(info.blockSource, mod);
            if (state == PassableState.IGNORE)
                continue;
            passable |= state == PassableState.PASSABLE ? true : false;
        }
        return passable;
    }

    @Override
    public void setVector(Vector vector) {
        this.location = vector;
    }

    private static class PathInfo {
        private final BlockSource blockSource;
        private final BlockExaminer[] examiners;
        private final VectorGoal goal;

        private PathInfo(BlockSource source, BlockExaminer[] examiners, VectorGoal goal) {
            this.blockSource = source;
            this.examiners = examiners;
            this.goal = goal;
        }
    }

    private static final float TIEBREAKER = 1.001f;
}