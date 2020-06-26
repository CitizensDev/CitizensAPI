package net.citizensnpcs.api.ai.tree;

import net.citizensnpcs.api.ai.Goal;

/**
 * The base class for the second iteration of the {@link Goal} API, which can be made backwards compatible by extending
 * {@link BehaviorGoalAdapter}.
 *
 * A behavior is a common term for the parts of a <em>behavior tree</em>, which is a simple directed acyclic graph (DAG)
 * for AI that is easy for designers to use. It is a simple state machine using {@link BehaviorStatus} as the separate
 * states.
 *
 * This can be represented as shouldExecute() returning true -&gt; RUNNING -&gt; FAILURE | SUCCESS. The graph is made up of
 * many {@link Selector}s and {@link Sequence}s, with the leaf nodes being concrete actions.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Behavior_tree_(artificial_intelligence,_robotics_and_control)">https://en.wikipedia.org/wiki/Behavior_tree_(artificial_intelligence,_robotics_and_control)</a>
 */
public interface Behavior {
    /**
     * Resets the behavior and any state it is holding.
     */
    void reset();

    /**
     * Ticks the behavior, optionally changing the state that it is in.
     *
     * @return The new status
     */
    BehaviorStatus run();

    /**
     * @return Whether the behavior is ready to run
     */
    boolean shouldExecute();
}
