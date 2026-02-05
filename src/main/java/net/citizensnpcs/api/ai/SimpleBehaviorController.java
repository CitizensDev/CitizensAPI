package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.Selector;

public class SimpleBehaviorController implements BehaviorController {
    private boolean executing;
    private boolean paused;
    private final Selector selector = Selector.selecting().build();

    @Override
    public void addBehavior(Behavior behavior) {
        selector.addBehavior(behavior);
    }

    @Override
    public void clear() {
        selector.reset();
        selector.getBehaviors().clear();
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void removeBehavior(Behavior behavior) {
        selector.removeBehavior(behavior);
    }

    @Override
    public void run() {
        if (paused || (!executing && !selector.shouldExecute()))
            return;
        executing = true;
        switch (selector.run()) {
            case FAILURE:
            case SUCCESS:
                selector.reset();
                executing = false;
            default:
                break;
        }
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
