package net.citizensnpcs.api.astar.pathfinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.astar.pathfinder.BlockExaminer.ReplacementNeighbourGenerator;
import net.citizensnpcs.api.util.SpigotUtil;

public class SwimmingNeighbourExaminer implements ReplacementNeighbourGenerator {
    private boolean canSwimInLava;

    private void addDiagonalIfPassable(BlockSource source, PathPoint parent, List<PathPoint> out, int bx, int by,
            int bz, int dx, int dz) {
        if (!source.isYWithinBounds(by))
            return;

        if (!isSwimCellPassable(source, bx + dx, by, bz))
            return;
        if (!isSwimCellPassable(source, bx, by, bz + dz))
            return;
        if (!isSwimCellPassable(source, bx + dx, by, bz + dz))
            return;

        out.add(parent.createChild(bx + dx, by, bz + dz));
    }

    private void addIfPassable(BlockSource source, PathPoint parent, List<PathPoint> out, int x, int y, int z) {
        if (!source.isYWithinBounds(y))
            return;
        if (!isSwimCellPassable(source, x, y, z))
            return;
        out.add(parent.createChild(x, y, z));
    }

    @Override
    public StandableState canStandAt(BlockSource source, PathPoint point) {
        return StandableState.STANDABLE;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        return 0;
    }

    @Override
    public List<PathPoint> getNeighbours(BlockSource source, PathPoint point) {
        if (isPassable(source, point) != PassableState.PASSABLE)
            return Collections.emptyList();

        Vector base = point.getVector();
        int bx = base.getBlockX(), by = base.getBlockY(), bz = base.getBlockZ();

        List<PathPoint> out = new ArrayList<>(10);

        addIfPassable(source, point, out, bx + 1, by, bz);
        addIfPassable(source, point, out, bx - 1, by, bz);
        addIfPassable(source, point, out, bx, by, bz + 1);
        addIfPassable(source, point, out, bx, by, bz - 1);
        addIfPassable(source, point, out, bx, by + 1, bz);
        addIfPassable(source, point, out, bx, by - 1, bz);

        // XZ diagonals with corner checks
        addDiagonalIfPassable(source, point, out, bx, by, bz, +1, +1);
        addDiagonalIfPassable(source, point, out, bx, by, bz, +1, -1);
        addDiagonalIfPassable(source, point, out, bx, by, bz, -1, +1);
        addDiagonalIfPassable(source, point, out, bx, by, bz, -1, -1);

        return out;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector v = point.getVector();
        if (!source.isYWithinBounds(v.getBlockY()))
            return PassableState.IMPASSABLE;

        return isSwimCellPassable(source, v.getBlockX(), v.getBlockY(), v.getBlockZ()) ? PassableState.PASSABLE
                : PassableState.IMPASSABLE;
    }

    private boolean isSwimCellPassable(BlockSource source, int x, int y, int z) {
        Material in = source.getMaterialAt(x, y, z);
        BlockData data = source.getBlockDataAt(x, y, z);

        if (!MinecraftBlockExaminer.isLiquidOrWaterlogged(in, data))
            return false;

        if (MinecraftBlockExaminer.isLiquid(in))
            return isSwimmableLiquid(in);

        return true;
    }

    private boolean isSwimmableLiquid(Material material) {
        if (material == Material.LAVA
                || (!SpigotUtil.isUsing1_13API() && material == Material.valueOf("STATIONARY_LAVA"))) {
            return canSwimInLava;
        }
        return material == Material.WATER
                || (!SpigotUtil.isUsing1_13API() && material == Material.valueOf("STATIONARY_WATER"));
    }

    public void setCanSwimInLava(boolean canSwimInLava) {
        this.canSwimInLava = canSwimInLava;
    }
}
