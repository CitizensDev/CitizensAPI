package net.citizensnpcs.api.ai.tree;

import java.util.List;

/**
 * A behavior that executes multiple behaviors in a single tick.
 */
public class CoalescedBehavior implements Behavior {
    private final List<Behavior> behaviors;

    public CoalescedBehavior(List<Behavior> behaviors) {
        this.behaviors = behaviors;
    }

    @Override
    public void reset() {
        for (Behavior behavior : behaviors) {
            behavior.reset();
        }
    }

    @Override
    public BehaviorStatus run() {
        for (Behavior behavior : behaviors) {
            BehaviorStatus status = behavior.run();
            if (status == BehaviorStatus.FAILURE || status == BehaviorStatus.RESET_AND_REMOVE)
                return status;
        }
        return BehaviorStatus.SUCCESS;
    }

    @Override
    public boolean shouldExecute() {
        for (Behavior behavior : behaviors) {
            if (behavior.shouldExecute())
                return true;
        }
        return false;
    }
}
