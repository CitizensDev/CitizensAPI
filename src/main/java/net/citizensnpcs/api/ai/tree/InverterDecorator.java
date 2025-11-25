package net.citizensnpcs.api.ai.tree;

/**
 * Decorator that inverts SUCCESS to FAILURE and vice versa.
 */
public class InverterDecorator implements Behavior {
    private final Behavior child;

    public InverterDecorator(Behavior child) {
        this.child = child;
    }

    @Override
    public void reset() {
        child.reset();
    }

    @Override
    public BehaviorStatus run() {
        BehaviorStatus status = child.run();
        switch (status) {
            case SUCCESS:
                return BehaviorStatus.FAILURE;
            case FAILURE:
                return BehaviorStatus.SUCCESS;
            default:
                return status;
        }
    }

    @Override
    public boolean shouldExecute() {
        return child.shouldExecute();
    }
}
