package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Runs each {@link Behavior} in sequence.
 */
public class Sequence extends Composite {
    private Behavior executing;
    private int executingIndex = -1;

    private Sequence(Behavior... behaviors) {
        this(Arrays.asList(behaviors));
    }

    private Sequence(Collection<Behavior> behaviors) {
        super(behaviors);
    }

    @Override
    public void reset() {
        super.reset();
        resetCurrent();
        executingIndex = -1;
    }

    private void resetCurrent() {
        stopExecution(executing);
        executing = null;
    }

    @Override
    public BehaviorStatus run() {
        tickParallel();
        List<Behavior> behaviors = getBehaviors();
        if (executing == null) {
            BehaviorStatus next = selectNext(behaviors);
            if (next != BehaviorStatus.RUNNING) {
                resetCurrent();
                return next;
            }
        }
        BehaviorStatus status = executing.run();
        switch (status) {
            case FAILURE:
                resetCurrent();
                return BehaviorStatus.FAILURE;
            case RESET_AND_REMOVE:
                behaviors.remove(executingIndex--);
                return selectNext(behaviors);
            case SUCCESS:
                resetCurrent();
                return selectNext(behaviors);
            default:
                return status;
        }
    }

    private BehaviorStatus selectNext(List<Behavior> behaviors) {
        if (++executingIndex >= behaviors.size())
            return BehaviorStatus.SUCCESS;
        executing = behaviors.get(executingIndex);
        if (!executing.shouldExecute()) {
            resetCurrent();
            return BehaviorStatus.FAILURE;
        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public String toString() {
        return "Sequence [executing=" + executing + ", executingIndex=" + executingIndex + ", getBehaviors()="
                + getBehaviors() + "]";
    }

    public static Sequence createSequence(Behavior... behaviors) {
        return createSequence(Arrays.asList(behaviors));
    }

    /**
     * Creates sequence that will stop executing if the current {@link Behavior} returns {@link BehaviorStatus#FAILURE}.
     */
    public static Sequence createSequence(Collection<Behavior> behaviors) {
        return new Sequence(behaviors);
    }
}
