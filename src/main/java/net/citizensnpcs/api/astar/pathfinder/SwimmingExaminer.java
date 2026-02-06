package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.util.SpigotUtil;

public class SwimmingExaminer implements BlockExaminer {
    private boolean canSwimInLava;

    @Override
    public StandableState canStandAt(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material in = source.getMaterialAt(pos);

        if (MinecraftBlockExaminer.isLiquidOrWaterlogged(in, source.getBlockDataAt(pos)))
            return StandableState.STANDABLE;

        return StandableState.IGNORE;
    }

    public boolean canSwimInLava() {
        return canSwimInLava;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        return 0;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector vector = point.getVector();
        if (!MinecraftBlockExaminer.isLiquidOrWaterlogged(source.getMaterialAt(vector), source.getBlockDataAt(vector)))
            return PassableState.IGNORE;

        Vector above = vector.clone().add(UP);
        Material aboveMat = source.getMaterialAt(above);
        return isSwimmableLiquid(aboveMat) || MinecraftBlockExaminer.canStandIn(aboveMat, source.getBlockDataAt(above))
                ? PassableState.PASSABLE
                : PassableState.IMPASSABLE;
    }

    private boolean isSwimmableLiquid(Material material) {
        if (material == Material.LAVA
                || !SpigotUtil.isUsing1_13API() && material == Material.valueOf("STATIONARY_LAVA"))
            return canSwimInLava();
        return material == Material.WATER
                || !SpigotUtil.isUsing1_13API() && material == Material.valueOf("STATIONARY_WATER");
    }

    public void setCanSwimInLava(boolean canSwimInLava) {
        this.canSwimInLava = canSwimInLava;
    }

    private static final Vector UP = new Vector(0, 1, 0);
}
