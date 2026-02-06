package net.citizensnpcs.api.astar.pathfinder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.astar.pathfinder.BlockExaminer.ReplacementNeighbourGenerator;
import net.citizensnpcs.api.util.SpigotUtil;

public class FlyingBlockExaminer implements ReplacementNeighbourGenerator {
    @Override
    public StandableState canStandAt(BlockSource source, PathPoint point) {
        return StandableState.STANDABLE;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material in = source.getMaterialAt(pos);
        if (above == WEB || in == WEB)
            return 2F;
        return 0F;
    }

    @Override
    public List<PathPoint> getNeighbours(BlockSource source, PathPoint point) {
        List<PathPoint> neighbours = new ArrayList<>(26);
        Vector base = point.getVector();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (!source.isYWithinBounds(base.getBlockY() + y))
                    continue;
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;

                    neighbours.add(point.createChild(base.getBlockX() + x, base.getBlockY() + y, base.getBlockZ() + z));
                }
            }
        }
        return neighbours;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material in = source.getMaterialAt(pos);
        if (MinecraftBlockExaminer.isLiquid(above, in))
            return PassableState.IMPASSABLE;

        return MinecraftBlockExaminer.canStandIn(above, in) ? PassableState.PASSABLE : PassableState.IMPASSABLE;
    }

    private static final Vector UP = new Vector(0, 1, 0);
    private static final Material WEB = SpigotUtil.isUsing1_13API() ? Material.COBWEB : Material.valueOf("WEB");
}