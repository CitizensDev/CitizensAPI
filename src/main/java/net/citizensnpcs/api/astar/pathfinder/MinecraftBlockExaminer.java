package net.citizensnpcs.api.astar.pathfinder;

import java.util.EnumSet;
import java.util.ListIterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.api.npc.NPC;

public class MinecraftBlockExaminer implements BlockExaminer {
    @Override
    public float getCost(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material below = source.getMaterialAt(pos.clone().add(DOWN));
        Material in = source.getMaterialAt(pos);
        if (above == Material.WEB || in == Material.WEB)
            return 1F;
        if (below == Material.SOUL_SAND || below == Material.ICE)
            return 1F;
        if (isLiquid(above, below, in))
            return 0.5F;
        return 0F; // TODO: add light level-specific costs
    }

    private boolean isClimbable(Material mat) {
        return mat == Material.LADDER || mat == Material.VINE;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material below = source.getMaterialAt(pos.clone().add(DOWN));
        Material in = source.getMaterialAt(pos);
        if (!below.isBlock() || !canStandOn(below)) {
            return PassableState.UNPASSABLE;
        }
        if ((isClimbable(above) && isClimbable(in)) || (isClimbable(in) && isClimbable(below))) {
            point.addCallback(new LadderClimber());
        } else if (!canStandIn(above) || (!canStandIn(in) && !isHalfBlock(in))) {
            return PassableState.UNPASSABLE;
        }
        if (!canJumpOn(below)) {
            if (point.getParentPoint() == null) {
                return PassableState.UNPASSABLE;
            }
            Vector parentPos = point.getParentPoint().getVector();
            if ((parentPos.getX() != pos.getX() || parentPos.getZ() != pos.getZ())
                    && pos.clone().subtract(point.getParentPoint().getVector()).getY() == 1) {
                return PassableState.UNPASSABLE;
            }
        }
        return PassableState.PASSABLE;
    }

    private class LadderClimber implements PathCallback {
        boolean added = false;
        ListIterator<Block> current;

        @Override
        public void run(final NPC npc, Block point, ListIterator<Block> path) {
            current = path;
            if (added || npc.data().<Boolean> get("running-ladder", false)) {
                added = true;
                return;
            }
            Runnable callback = new Runnable() {
                Location dummy = new Location(null, 0, 0, 0);

                @Override
                public void run() {
                    if (npc.getEntity().getLocation(dummy).getBlock().getType() == Material.LADDER
                            && current.next().getY() > current.previous().getY()) {
                        npc.getEntity().setVelocity(npc.getEntity().getVelocity().setY(0.3));
                    }
                }
            };
            npc.getNavigator().getLocalParameters().addSingleUseCallback(new NavigatorCallback() {
                @Override
                public void onCompletion(CancelReason cancelReason) {
                    npc.data().set("running-ladder", false);
                }
            });
            npc.getNavigator().getLocalParameters().addRunCallback(callback);
            added = true;
        }
    }

    private static boolean canJumpOn(Material mat) {
        return !NOT_JUMPABLE.contains(mat);
    }

    public static boolean canStandIn(Material... mat) {
        boolean passable = true;
        for (Material m : mat) {
            passable &= !m.isSolid();
        }
        return passable;
    }

    public static boolean canStandOn(Block block) {
        Block up = block.getRelative(BlockFace.UP);
        return canStandOn(block.getType()) && canStandIn(up.getType())
                && canStandIn(up.getRelative(BlockFace.UP).getType());
    }

    public static boolean canStandOn(Material mat) {
        return !UNWALKABLE.contains(mat) && mat.isSolid();
    }

    private static boolean contains(Material[] search, Material... find) {
        for (Material haystack : search) {
            for (Material needle : find) {
                if (haystack == needle)
                    return true;
            }
        }
        return false;
    }

    public static Location findValidLocation(Location location, int radius) {
        Block base = location.getBlock();
        if (canStandIn(base.getType()) && canStandOn(base.getRelative(BlockFace.DOWN)))
            return location;
        for (int y = 0; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Block relative = base.getRelative(x, y, z);
                    if (canStandIn(relative.getRelative(BlockFace.UP).getType()) && canStandIn(relative.getType())
                            && canStandOn(base.getRelative(BlockFace.DOWN))) {
                        return relative.getLocation();
                    }
                }
            }
        }
        return location;
    }

    public static boolean isDoor(Material in) {
        return DOORS.contains(in);
    }
    
    public static boolean isHalfBlock(Material in) {
    	return HALF_BLOCKS.contains(in);
    }

    public static boolean isLiquid(Material... materials) {
        return contains(materials, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
    }

    public static boolean validPosition(Block in) {
        return canStandIn(in.getType()) && canStandIn(in.getRelative(BlockFace.UP).getType())
                && canStandOn(in.getRelative(BlockFace.DOWN));
    }

    private static final Set<Material> DOORS = EnumSet.of(Material.IRON_DOOR_BLOCK, Material.WOODEN_DOOR,
            Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR,
            Material.DARK_OAK_DOOR);
    private static final Vector DOWN = new Vector(0, -1, 0);
    private static final Set<Material> NOT_JUMPABLE = EnumSet.of(Material.FENCE, Material.IRON_FENCE,
            Material.NETHER_FENCE, Material.COBBLE_WALL, Material.SPRUCE_FENCE, Material.BIRCH_FENCE,
            Material.JUNGLE_FENCE, Material.ACACIA_FENCE, Material.DARK_OAK_FENCE);
    private static final Set<Material> UNWALKABLE = EnumSet.of(Material.AIR, Material.LAVA, Material.STATIONARY_LAVA,
            Material.CACTUS);
    private static final Set<Material> HALF_BLOCKS = EnumSet.of(Material.BED_BLOCK, Material.STEP, Material.STONE_SLAB2, Material.WOOD_STEP);
    private static final Vector UP = new Vector(0, 1, 0);
}
