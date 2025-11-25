package net.citizensnpcs.api.ai.tree;

import java.util.Collection;

/**
 * A composite that runs all parallel children. Returns SUCCESS when all parallel children complete. Individual child
 * status doesn't affect the composite's return value.
 */
public class ParallelComposite extends Composite {
    public ParallelComposite(Collection<Behavior> behaviors) {
        super(behaviors);
    }

    @Override
    public BehaviorStatus run() {
        if (parallelExecuting.isEmpty())
            return BehaviorStatus.SUCCESS;
        tickParallel();
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }
}
