package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

import org.bukkit.util.Vector;

import net.citizensnpcs.api.astar.pathfinder.BlockExaminer.AdditionalNeighbourGenerator;

public class FallingExaminer implements AdditionalNeighbourGenerator {
    private final int maxFallDistance;

    public FallingExaminer(int maxFallDistance) {
        this.maxFallDistance = maxFallDistance;
    }

    @Override
    public void addNeighbours(BlockSource source, PathPoint point, List<PathPoint> neighbours) {
        Vector base = point.getVector();
        if (!source.isYWithinBounds(base.getBlockY() - 1))
            return;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

                int x = base.getBlockX() + dx;
                int z = base.getBlockZ() + dz;

                if (!MinecraftBlockExaminer.canStandIn(source.getMaterialAt(x, base.getBlockY(), z),
                        source.getBlockDataAt(x, base.getBlockY(), z))
                        || MinecraftBlockExaminer.canStandOn(source.getMaterialAt(x, base.getBlockY() - 1, z),
                                source.getBlockDataAt(x, base.getBlockY() - 1, z)))
                    continue;

                for (int dy = 2; dy <= maxFallDistance; dy++) {
                    if (!source.isYWithinBounds(base.getBlockY() - dy))
                        break;

                    if (MinecraftBlockExaminer.canStandIn(source.getMaterialAt(x, base.getBlockY() - dy + 1, z),
                            source.getBlockDataAt(x, base.getBlockY() - dy + 1, z))
                            && MinecraftBlockExaminer.canStandOn(source.getMaterialAt(x, base.getBlockY() - dy, z),
                                    source.getBlockDataAt(x, base.getBlockY() - dy, z))) {
                        // TODO: could use setPathVectors
                        neighbours.add(point.createAtOffset(new Vector(x, base.getBlockY() - dy, z), (dy + 1) * 2.5f));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public StandableState canStandAt(BlockSource source, PathPoint point) {
        return StandableState.IGNORE;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        return 0;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        return PassableState.IGNORE;
    }
}
