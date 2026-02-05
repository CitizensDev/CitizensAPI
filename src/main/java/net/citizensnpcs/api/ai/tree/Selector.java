package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * A selector of sub-goals, that chooses a single {@link Behavior} to execute from a list. The default selection
 * function is a random selection but this can be specified in the builder.
 */
public class Selector extends Composite {
    private Behavior executing;
    private final Function<List<Behavior>, Behavior> selectionFunction;

    private Selector(Function<List<Behavior>, Behavior> selectionFunction, Collection<Behavior> behaviors) {
        super(behaviors);
        this.selectionFunction = selectionFunction;
    }

    public Behavior getNextBehavior() {
        return selectionFunction.apply(getBehaviors());
    }

    public Function<List<Behavior>, Behavior> getSelectionFunction() {
        return selectionFunction;
    }

    @Override
    public void reset() {
        super.reset();
        if (executing != null) {
            stopExecution(executing);
        }
        executing = null;
    }

    @Override
    public BehaviorStatus run() {
        tickParallel();
        BehaviorStatus status = null;
        if (executing == null) {
            if ((executing = getNextBehavior()) == null)
                return BehaviorStatus.FAILURE;

            if (!executing.shouldExecute()) {
                status = BehaviorStatus.FAILURE;
            }
        }
        if (status == null) {
            status = executing.run();
        }
        switch (status) {
            case RESET_AND_REMOVE:
                getBehaviors().remove(executing);
                stopExecution(executing);
                executing = null;
                return BehaviorStatus.SUCCESS;
            case SUCCESS:
            case FAILURE:
                stopExecution(executing);
                executing = null;
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "Selector [executing=" + executing + ", selectionFunction=" + selectionFunction + ", getBehaviors()="
                + getBehaviors() + "]";
    }

    public static class Builder {
        private final Collection<Behavior> behaviors;
        private Function<List<Behavior>, Behavior> selectionFunction = RANDOM_SELECTION;

        private Builder(Collection<Behavior> behaviors) {
            this.behaviors = behaviors;
        }

        public Selector build() {
            return new Selector(selectionFunction, behaviors);
        }

        /**
         * Sets the {@link Function} that selects a {@link Behavior} to execute from a list of behaviors, such as a
         * random selection or a priority selection. See {@link Selectors} for some helper methods.
         *
         * @param function
         *            The selection function
         */
        public Builder selectionFunction(Function<List<Behavior>, Behavior> function) {
            selectionFunction = function;
            return this;
        }
    }

    public static Builder selecting(Behavior... behaviors) {
        return selecting(Arrays.asList(behaviors));
    }

    public static Builder selecting(Collection<Behavior> behaviors) {
        return new Builder(behaviors);
    }

    private static final Random RANDOM = new Random();
    private static final Function<List<Behavior>, Behavior> RANDOM_SELECTION = behaviors -> behaviors
            .get(RANDOM.nextInt(behaviors.size()));
}
