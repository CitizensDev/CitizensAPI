package net.citizensnpcs.api.ai.tree;

/**
 * Wraps a behavior to run in parallel with other behaviors.
 * Implements ParallelBehavior marker interface to signal to composite parents
 * that this behavior should run alongside others.
 */
public class ParallelBehaviorWrapper implements Behavior, ParallelBehavior {
    private final Behavior wrapped;

    public ParallelBehaviorWrapper(Behavior wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void reset() {
        wrapped.reset();
    }

    @Override
    public BehaviorStatus run() {
        return wrapped.run();
    }

    @Override
    public boolean shouldExecute() {
        return wrapped.shouldExecute();
    }

    public Behavior getWrapped() {
        return wrapped;
    }
}
