package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Squid;
import org.bukkit.entity.WaterMob;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.SpigotUtil;

public class SwimmingExaminer implements BlockExaminer {
    private boolean canSwimInLava;
    private final NPC npc;

    public SwimmingExaminer(NPC npc) {
        this.npc = npc;
    }

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
        if (!isWaterMob(npc.getEntity()))
            return 0;
        if (!MinecraftBlockExaminer.isLiquidOrWaterlogged(source.getMaterialAt(point.getVector()),
                source.getBlockDataAt(point.getVector())))
            return 1F;
        return 0;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector vector = point.getVector();
        if (!MinecraftBlockExaminer.isLiquidOrWaterlogged(source.getMaterialAt(vector), source.getBlockDataAt(vector)))
            return PassableState.IGNORE;

        if (isWaterMob(npc.getEntity()))
            return PassableState.PASSABLE;

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

    public static boolean isWaterMob(Entity entity) {
        if (entity == null)
            return false;
        if (!SpigotUtil.isUsing1_13API())
            return entity instanceof Squid;
        return entity instanceof WaterMob || entity.getType().name().equals("TURTLE")
                || entity.getType().name().equals("FROG") || entity.getType().name().equals("AXOLOTL");
    }

    private static final Vector UP = new Vector(0, 1, 0);
}
