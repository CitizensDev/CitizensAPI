package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.ai.tree.Behavior;

/**
 * Represents a collection of behaviors that are prioritised and executed.
 * <p>
 * The highest priority {@link Behavior} that returns true in {@link Behavior#shouldExecute()} is executed.
 */
public interface BehaviorController extends Runnable {
    /**
     * Registers a {@link Behavior}.
     *
     * @param behavior
     *            The behavior
     */
    void addBehavior(Behavior behavior);

    /**
     * Clears the goal controller of all {@link Behavior}s. Will stop the execution of any current behavior.
     */
    void clear();

    /**
     * @see #setPaused(boolean)
     * @return Whether the controller is currently paused
     */
    boolean isPaused();

    /**
     * Removes the given {@link Behavior} from rotation.
     *
     * @param behavior
     *            The behavior to remove
     */
    void removeBehavior(Behavior behavior);

    /**
     * Sets whether the controller is paused. While paused, no new {@link Behavior}s will be selected and any executing
     * goals will be suspended.
     *
     * @param paused
     *            Whether to pause execution
     */
    void setPaused(boolean paused);
}
