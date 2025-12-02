package net.citizensnpcs.api.ai.tree;

/**
 * Marker interface for behaviors that execute instantly (in one tick). These behaviors complete immediately without
 * needing to return RUNNING.
 *
 * The parser will automatically coalesce consecutive instant behaviors into a single composite that executes them all
 * in one tick.
 */
public interface InstantBehavior extends Behavior {
    @Override
    default void reset() {
    }

    @Override
    default boolean shouldExecute() {
        return true;
    }
}
