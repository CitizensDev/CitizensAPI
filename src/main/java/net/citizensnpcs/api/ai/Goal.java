package net.citizensnpcs.api.ai;

/**
 * Represents an AI Goal that can be added to a queue of an NPC's goals.
 */
public interface Goal {
    /**
     * Returns whether this and the other {@link Goal} can be run at the same
     * time.
     * 
     * @param other
     *            The goal to check
     * @return Whether this goal is compatible
     */
    public boolean isCompatibleWith(Goal other);

    /**
     * Resets the goal and any resources or state it is holding.
     */
    public void reset();

    /**
     * Sets up the execution of this goal so that it can be updated later.
     * Called initially instead of {@link Goal#update()};
     * 
     * @return Whether the goal was started
     */
    public boolean start();

    /**
     * Updates the goal.
     * 
     * @return Whether the goal is finished.
     */
    public boolean update();
}