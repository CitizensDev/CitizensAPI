package net.citizensnpcs.api.ai.tree;

import java.util.function.Supplier;

/**
 * A simple if-then-else {@link Behavior} which will execute a different {@link Behavior} depending on an
 * {@link Condition} function.
 */
public class IfElse extends BehaviorGoalAdapter {
    private final Supplier<Boolean> condition;
    private Behavior executing;
    private final Behavior ifBehavior, elseBehavior;

    public IfElse(Supplier<Boolean> condition, Behavior ifBehavior, Behavior elseBehavior) {
        this.condition = condition;
        this.ifBehavior = ifBehavior;
        this.elseBehavior = elseBehavior;
    }

    @Override
    public void reset() {
        if (executing != null) {
            executing.reset();
            executing = null;
        }
    }

    @Override
    public BehaviorStatus run() {
        return executing.run();
    }

    @Override
    public boolean shouldExecute() {
        boolean cond = condition.get();
        if (cond) {
            executing = ifBehavior;
        } else {
            executing = elseBehavior;
        }
        if (executing == null || !executing.shouldExecute()) {
            executing = null;
            return false;
        }
        return true;
    }

    public static IfElse create(Supplier<Boolean> condition, Behavior ifBehavior, Behavior elseBehavior) {
        return new IfElse(condition, ifBehavior, elseBehavior);
    }
}
