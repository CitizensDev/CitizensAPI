package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.google.common.collect.ImmutableList;

import net.citizensnpcs.api.astar.pathfinder.BlockExaminer.AdditionalNeighbourGenerator;
import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.api.npc.NPC;

public class JumpingExaminer implements AdditionalNeighbourGenerator {
    private final int entityHeight;
    private final float speed;

    public JumpingExaminer(double entityHeight, float speed) {
        this.entityHeight = (int) Math.ceil(entityHeight);
        this.speed = speed;
    }

    @Override
    public void addNeighbours(BlockSource source, PathPoint point, List<PathPoint> neighbours) {
        final Vector base = point.getVector();
        final int minY = base.getBlockY() - 3;

        for (int i = 0; i < 8; i++) {
            double vx = (DX[i] * INV_LEN[i]) * speed;
            double vy = JUMP_VELOCITY;
            double vz = (DZ[i] * INV_LEN[i]) * speed;

            double x = base.getBlockX() + 0.5 + DX[i] * INV_LEN[i] * 0.3;
            double y = base.getBlockY();
            double z = base.getBlockZ() + 0.5 + DZ[i] * INV_LEN[i] * 0.3;

            int px = base.getBlockX(), py = base.getBlockY(), pz = base.getBlockZ();
            loop: for (int tick = 0; tick < 60; tick++) {
                x += vx;
                y += vy;
                z += vz;

                vx *= XZ_DRAG;
                vy = (vy - GRAVITY) * 0.98;
                vz *= XZ_DRAG;

                int bx = floor(x);
                int by = floor(y);
                int bz = floor(z);

                if (bx != px || by != py || bz != pz) {
                    if (by < minY || !source.isYWithinBounds(by) || !isClearColumn(source, bx, by, bz))
                        break;

                    int cx = px, cy = py, cz = pz;
                    while (cx != bx || cy != by || cz != bz) {
                        if (cx != bx) {
                            cx += Integer.compare(bx, cx);
                        }
                        if (cy != by) {
                            cy += Integer.compare(by, cy);
                        }
                        if (cz != bz) {
                            cz += Integer.compare(bz, cz);
                        }
                        if (cy < minY || !source.isYWithinBounds(cy) || !isClearColumn(source, cx, cy, cz))
                            continue loop;

                        if (source.isYWithinBounds(cy - 1)
                                && MinecraftBlockExaminer.canStandOn(source.getMaterialAt(cx, cy - 1, cz))) {
                            Vector jumpPoint = new Vector(cx + 0.5 + DX[i] * INV_LEN[i] * 0.3, cy,
                                    cz + 0.5 + DX[i] * INV_LEN[i] * 0.3);
                            point.addCallback(new JumpCallback(jumpPoint));
                            point.setPathVectors(ImmutableList.of(jumpPoint));
                            neighbours.add(point.createAtOffset(new Vector(cx, cy, cz), 1.5f));
                            continue loop;
                        }
                    }
                    px = bx;
                    py = by;
                    pz = bz;
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

    private boolean isClearColumn(BlockSource source, int x, int y, int z) {
        for (int h = 0; h < entityHeight; h++) {
            if (!MinecraftBlockExaminer.canStandIn(source.getMaterialAt(x, y + h, z)))
                return false;
        }
        return true;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        return PassableState.IGNORE;
    }

    private static class JumpCallback implements PathCallback {
        private final Vector jumpPoint;
        private boolean reached;

        public JumpCallback(Vector jumpPoint) {
            this.jumpPoint = jumpPoint;
        }

        @Override
        public void onReached(NPC npc, Block point) {
            reached = true;
        }

        @Override
        public void run(NPC npc, Block point, List<Block> path, int index) {
            if (!reached || npc.getEntity().getLocation().toVector().distance(jumpPoint) > 0.2)
                return;
            reached = false;
            npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0.42));
        }
    }

    private static int floor(double v) {
        int i = (int) v;
        return (v < i) ? (i - 1) : i;
    }

    private static final int[] DX = { 1, -1, 0, 0, 1, 1, -1, -1 };
    private static final int[] DZ = { 0, 0, 1, -1, 1, -1, 1, -1 };
    private static final double GRAVITY = 0.08;
    private static final double[] INV_LEN = { 1.0, 1.0, 1.0, 1.0, 0.7071067811865476, 0.7071067811865476,
            0.7071067811865476, 0.7071067811865476 };
    private static final double JUMP_VELOCITY = 0.42;
    private static final double XZ_DRAG = 0.91;
}
