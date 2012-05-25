package net.citizensnpcs.api.ai;

/**
 * Represents AI that can be attached to an NPC.
 */
public interface AI {
    /**
     * Registers a {@link Goal} with a given priority.
     * 
     * @param priority
     *            The priority of the goal
     * @param goal
     *            The goal
     */
    public void addGoal(int priority, Goal goal);

    /**
     * Removes a previously registered {@link Goal}.
     * 
     * @param goal
     *            The goal to remove
     */
    public void removeGoal(Goal goal);
}