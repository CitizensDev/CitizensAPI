package net.citizensnpcs.api.ai;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.ai.tree.Selector;
import net.citizensnpcs.api.ai.tree.Sequence;

public class BehaviorTreeTest {
    private BehaviorController test;

    @Test
    public void failureSelector() {
        final CountedBehavior goal = new CountedBehavior(BehaviorStatus.SUCCESS);
        CountedBehavior goal2 = new CountedBehavior(BehaviorStatus.FAILURE);
        Selector p = Selector.selecting(goal, goal2).selectionFunction(new Function<List<Behavior>, Behavior>() {
            int idx;

            @Override
            public Behavior apply(List<Behavior> input) {
                Behavior b = input.get(idx++);
                if (idx >= input.size()) {
                    idx = 0;
                }
                return b;
            }
        }).retryChildren().build();
        test.addBehavior(p);
        test.run();
        test.run();
        assertThat("Reset count first", goal.resetCount, is(1));
        assertThat("Run count first", goal.runCount, is(1));
        assertThat("Should execute count first", goal.shouldExecuteCount, is(1));
        assertThat("Reset count second", goal2.resetCount, is(1));
        assertThat("Run count second", goal2.runCount, is(1));
        assertThat("Should execute count second", goal2.shouldExecuteCount, is(1));
    }

    @Before
    public void setUp() {
        test = new SimpleBehaviorController();
    }

    @Test
    public void singleSelector() {
        CountedBehavior goal = new CountedBehavior(BehaviorStatus.SUCCESS);
        Selector p = Selector.selecting(goal).build();
        test.addBehavior(p);
        test.run();
        assertThat("Reset count", goal.resetCount, is(1));
        assertThat("Run count", goal.runCount, is(1));
        assertThat("Should execute count", goal.shouldExecuteCount, is(1));
    }

    @Test
    public void singleSequence() {
        CountedBehavior goal = new CountedBehavior(BehaviorStatus.SUCCESS);
        Sequence p = Sequence.createSequence(goal);
        test.addBehavior(p);
        test.run();
        assertThat("Reset count", goal.resetCount, is(1));
        assertThat("Run count", goal.runCount, is(1));
        assertThat("Should execute count", goal.shouldExecuteCount, is(1));
    }

    private static class CountedBehavior implements Behavior {
        public int loggingTag = 0;
        private int resetCount;
        private final BehaviorStatus ret;
        private int runCount;
        private int shouldExecuteCount;

        private CountedBehavior(BehaviorStatus ret) {
            this.ret = ret;
        }

        @Override
        public void reset() {
            if (loggingTag > 0) {
                System.err.println(loggingTag + ": reset");
            }
            resetCount++;
        }

        @Override
        public BehaviorStatus run() {
            if (loggingTag > 0) {
                System.err.println(loggingTag + ": run " + ret);
            }
            runCount++;
            return ret;
        }

        @Override
        public boolean shouldExecute() {
            if (loggingTag > 0) {
                System.err.println(loggingTag + ": shouldExecute");
            }
            shouldExecuteCount++;
            return true;
        }
    }
}
