package net.citizensnpcs.api.ai.tree;

/**
 * Decorator that forces FAILURE if child doesn't complete within a timeout.
 */
public class TimeoutDecorator implements Behavior {
    private final Behavior child;
    private final int maxTicks;
    private int ticksElapsed;

    public TimeoutDecorator(Behavior child, int maxTicks) {
        this.child = child;
        this.maxTicks = maxTicks;
    }

    @Override
    public void reset() {
        child.reset();
        ticksElapsed = 0;
    }

    @Override
    public BehaviorStatus run() {
        if (ticksElapsed >= maxTicks) {
            child.reset();
            return BehaviorStatus.FAILURE;
        }
        ticksElapsed++;
        BehaviorStatus status = child.run();

        if (status != BehaviorStatus.RUNNING) {
            ticksElapsed = 0;
        }
        return status;
    }

    @Override
    public boolean shouldExecute() {
        return child.shouldExecute();
    }
}
