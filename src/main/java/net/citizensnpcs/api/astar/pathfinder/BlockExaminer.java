package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

public interface BlockExaminer {
    /**
     * Determines if the entity can stand at this position (checks block below for support). First STANDABLE result wins
     * during evaluation.
     *
     * @return STANDABLE if position has valid support, NOT_STANDABLE if not, IGNORE to defer
     */
    default StandableState canStandAt(BlockSource source, PathPoint point) {
        return StandableState.IGNORE;
    }

    float getCost(BlockSource source, PathPoint point);

    /**
     * Determines if the entity can pass through the block at this position. First PASSABLE result wins during
     * evaluation.
     */
    PassableState isPassable(BlockSource source, PathPoint point);

    public static interface NeighbourGeneratorBlockExaminer extends BlockExaminer {
        public List<PathPoint> getNeighbours(BlockSource source, PathPoint point);
    }

    public enum PassableState {
        IGNORE,
        IMPASSABLE,
        PASSABLE;
    }

    public enum StandableState {
        IGNORE,
        NOT_STANDABLE,
        STANDABLE;
    }

}