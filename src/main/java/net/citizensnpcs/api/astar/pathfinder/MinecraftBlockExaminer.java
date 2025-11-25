package net.citizensnpcs.api.astar.pathfinder;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.SpigotUtil;

public class MinecraftBlockExaminer implements BlockExaminer {
    @Override
    public StandableState canStandAt(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        if (!source.isYWithinBounds(pos.getBlockY()))
            return StandableState.NOT_STANDABLE;

        Material below = source.getMaterialAt(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ());
        Material in = source.getMaterialAt(pos);

        boolean canStand = canStandOn(below,
                source.getBlockDataAt(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ())) || isLiquid(in, below)
                || isClimbable(below);

        if (!canStand)
            return StandableState.NOT_STANDABLE;

        if (!canJumpOn(below)) {
            if (point.getParentPoint() == null)
                return StandableState.NOT_STANDABLE;

            Vector parentPos = point.getParentPoint().getVector();
            if ((parentPos.getX() != pos.getX() || parentPos.getZ() != pos.getZ())
                    && pos.clone().subtract(point.getParentPoint().getVector()).getY() == 1)
                return StandableState.NOT_STANDABLE;
        }
        return StandableState.STANDABLE;
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.getBlockX(), pos.getBlockY() + 1, pos.getBlockZ());
        Material below = source.getMaterialAt(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ());
        Material in = source.getMaterialAt(pos);
        if (above == WEB || in == WEB || below == Material.SOUL_SAND || below == Material.ICE)
            return 2F;
        if (isLiquidOrWaterlogged(source.getMaterialAt(pos), source.getBlockDataAt(pos))) {
            if (in == Material.LAVA)
                return 4F;
            return 2F;
        }
        return 0F; // TODO: add light level-specific costs?
    }

    private boolean isClimbable(Material mat) {
        return CLIMBABLE.contains(mat);
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material in = source.getMaterialAt(pos);
        Material above = source.getMaterialAt(pos.getBlockX(), pos.getBlockY() + 1, pos.getBlockZ());
        Material below = source.getMaterialAt(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ());

        if (isClimbable(in) && (isClimbable(above) || isClimbable(below))) {
            point.addCallback(new LadderClimber());
            return PassableState.PASSABLE;
        }
        if (!canStandIn(in, source.getBlockDataAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()))
                || !canStandIn(above, source.getBlockDataAt(pos.getBlockX(), pos.getBlockY() + 1, pos.getBlockZ())))
            return PassableState.IMPASSABLE;

        return PassableState.PASSABLE;
    }

    private class LadderClimber implements PathCallback {
        boolean added = false;

        @Override
        public void run(final NPC npc, Block point, List<Block> path, int index) {
            if (added || npc.data().<Boolean> get("running-ladder", false)) {
                added = true;
                return;
            }
            npc.getNavigator().getLocalParameters().addRunCallback(new Runnable() {
                Location dummy = new Location(null, 0, 0, 0);
                boolean sneakingForScaffolding;

                private boolean isScaffolding(Material type) {
                    return type.name().contains("SCAFFOLDING");
                }

                @Override
                public void run() {
                    if (index + 1 >= path.size())
                        return;
                    Location loc = npc.getEntity().getLocation(dummy);
                    Material in = loc.getBlock().getType();
                    Block next = path.get(index + 1);
                    Block prev = path.get(index);
                    if (isClimbable(in) || isClimbable(loc.getBlock().getRelative(BlockFace.DOWN).getType())
                            || isScaffolding(next.getType())) {
                        if (next.getY() > prev.getY()) {
                            npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0.3));
                            if (sneakingForScaffolding) {
                                npc.setSneaking(sneakingForScaffolding = false);
                            }
                        } else if (isScaffolding(in) || isScaffolding(next.getType())) {
                            if (loc.distance(next.getLocation().add(0.5, 1, 0.5)) < 0.4) {
                                npc.setSneaking(sneakingForScaffolding = true);
                            }
                        } else if (next.getY() < prev.getY()) {
                            npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(-0.2));
                        }
                    } else if (sneakingForScaffolding) {
                        npc.setSneaking(sneakingForScaffolding = false);
                    }
                }
            });
            npc.getNavigator().getLocalParameters().addSingleUseCallback(cancelReason -> {
                npc.data().set("running-ladder", false);
                npc.setSneaking(false);
            });
            added = true;
        }
    }

    private static boolean canJumpOn(Material mat) {
        return !NOT_JUMPABLE.contains(mat);
    }

    public static boolean canStandIn(Block... blocks) {
        boolean passable = true;
        for (Block block : blocks) {
            passable = canStandIn(block.getType(), block.getBlockData());
            if (!passable)
                break;
        }
        return passable;
    }

    public static boolean canStandIn(Material... mat) {
        boolean passable = true;
        for (Material m : mat) {
            passable &= !m.isSolid();
        }
        return passable;
    }

    public static boolean canStandIn(Material mat, BlockData data) {
        boolean passable = !mat.isSolid();
        if (SpigotUtil.isUsing1_13API()) {
            if (data instanceof Slab) {
                Slab slab = (Slab) data;
                if (slab.getType() != Slab.Type.BOTTOM) {
                    passable = false;
                }
            } else if (data instanceof TrapDoor) {
                TrapDoor trapdoor = (TrapDoor) data;
                passable &= trapdoor.isOpen();
            }
        }
        return passable;
    }

    public static boolean canStandOn(Block block) {
        boolean standable = canStandOn(block.getType(), block.getBlockData());
        if (!standable)
            return false;
        Block up = block.getRelative(BlockFace.UP);
        return canStandIn(up, up.getRelative(BlockFace.UP));
    }

    public static boolean canStandOn(Material mat) {
        return !UNWALKABLE.contains(mat) && mat.isSolid();
    }

    public static boolean canStandOn(Material mat, BlockData data) {
        boolean stand = canStandOn(mat);
        if (!stand && SpigotUtil.isUsing1_13API() && data instanceof TrapDoor) {
            stand = !((TrapDoor) data).isOpen();
        }
        return stand;
    }

    public static Location findRandomValidLocation(Location base, int xrange, int yrange) {
        return findRandomValidLocation(base, xrange, yrange, null, new Random());
    }

    public static Location findRandomValidLocation(Location base, int xrange, int yrange,
            Function<Block, Boolean> filter) {
        return findRandomValidLocation(base, xrange, yrange, filter, new Random());
    }

    public static Location findRandomValidLocation(Location base, int xrange, int yrange,
            Function<Block, Boolean> filter, Random random) {
        for (int i = 0; i < 10; i++) {
            int x = base.getBlockX() + random.nextInt(2 * xrange + 1) - xrange;
            int y = base.getBlockY() + random.nextInt(2 * yrange + 1) - yrange;
            int z = base.getBlockZ() + random.nextInt(2 * xrange + 1) - xrange;
            if (!base.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            Block block = base.getWorld().getBlockAt(x, y, z);
            if (canStandOn(block)) {
                if (filter != null && !filter.apply(block))
                    continue;

                return block.getLocation().add(0, 1, 0);
            }
        }
        return null;
    }

    public static Location findValidLocation(Location location, int radius) {
        return findValidLocation(location, radius, radius);
    }

    public static Location findValidLocation(Location location, int xradius, int yradius) {
        return findValidLocation(location, xradius, yradius, b -> true);
    }

    public static Location findValidLocation(Location location, int xradius, int yradius,
            Function<Block, Boolean> filter) {
        Block base = location.getBlock();
        if (filter.apply(base) && canStandOn(base.getRelative(BlockFace.DOWN)))
            return location;
        for (int y = -yradius; y <= yradius; y++) {
            for (int x = -xradius; x <= xradius; x++) {
                for (int z = -xradius; z <= xradius; z++) {
                    if (!base.getWorld().isChunkLoaded(base.getX() + x >> 4, base.getZ() + z >> 4)) {
                        continue;
                    }
                    Block relative = base.getRelative(x, y, z);
                    if (filter.apply(relative) && canStandOn(relative.getRelative(BlockFace.DOWN)))
                        return relative.getLocation();

                }
            }
        }
        return location;
    }

    public static Location findValidLocationAbove(Location location, int radius) {
        Block base = location.getBlock();
        if (canStandOn(base.getRelative(BlockFace.DOWN)))
            return location;
        for (int y = 0; y <= radius; y++) {
            Block relative = base.getRelative(0, y, 0);
            if (canStandOn(relative.getRelative(BlockFace.DOWN)))
                return relative.getLocation();
        }
        return location;
    }

    public static boolean isDoor(Material in) {
        return in.name().contains("DOOR") && !in.name().contains("TRAPDOOR");
    }

    public static boolean isGate(Material in) {
        return in.name().contains("GATE") && !in.name().contains("GATEWAY");
    }

    public static boolean isLiquid(Material... materials) {
        for (Material mat : materials) {
            if (LIQUIDS.contains(mat))
                return true;
        }
        return false;
    }

    public static boolean isLiquidOrWaterlogged(Block block) {
        return isLiquidOrWaterlogged(block.getType(), block.getBlockData());
    }

    public static boolean isLiquidOrWaterlogged(Material type, BlockData data) {
        if (isLiquid(type))
            return true;
        if (!SpigotUtil.isUsing1_13API())
            return false;
        return data instanceof Waterlogged && ((Waterlogged) data).isWaterlogged();
    }

    private static final Set<Material> CLIMBABLE = EnumSet.of(Material.LADDER, Material.VINE);
    private static final Set<Material> LIQUIDS = EnumSet.of(Material.WATER, Material.LAVA);
    private static final Set<Material> NOT_JUMPABLE = EnumSet.of(Material.SPRUCE_FENCE, Material.BIRCH_FENCE,
            Material.JUNGLE_FENCE, Material.ACACIA_FENCE, Material.DARK_OAK_FENCE);
    private static final Set<Material> UNWALKABLE = EnumSet.of(Material.AIR, Material.CACTUS);
    private static final Material WEB = SpigotUtil.isUsing1_13API() ? Material.COBWEB : Material.valueOf("WEB");

    static {
        if (!SpigotUtil.isUsing1_13API()) {
            LIQUIDS.add(Material.valueOf("STATIONARY_LAVA"));
            LIQUIDS.add(Material.valueOf("STATIONARY_WATER"));
            UNWALKABLE.add(Material.valueOf("STATIONARY_LAVA"));
            NOT_JUMPABLE.addAll(Lists.newArrayList(Material.valueOf("FENCE"), Material.valueOf("IRON_FENCE"),
                    Material.valueOf("NETHER_FENCE"), Material.valueOf("COBBLE_WALL")));
        } else {
            try {
                UNWALKABLE.add(Material.valueOf("CAMPFIRE"));
            } catch (IllegalArgumentException e) {
                // 1.13
            }
            try {
                CLIMBABLE.add(Material.valueOf("SCAFFOLDING"));
            } catch (IllegalArgumentException e) {
            }
            NOT_JUMPABLE.addAll(Lists.newArrayList(Material.valueOf("OAK_FENCE"),
                    Material.valueOf("NETHER_BRICK_FENCE"), Material.valueOf("COBBLESTONE_WALL")));
            try {
                NOT_JUMPABLE.add(Material.valueOf("MANGROVE_FENCE"));
            } catch (IllegalArgumentException e) {
            }
            try {
                NOT_JUMPABLE.add(Material.valueOf("CHERRY_FENCE"));
            } catch (IllegalArgumentException e) {
            }
        }
    }
}
