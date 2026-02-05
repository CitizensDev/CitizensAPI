package net.citizensnpcs.api.ai;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.ai.tree.Selector;
import net.citizensnpcs.api.ai.tree.Sequence;

public class BehaviorTreeTest {
    private BehaviorController test;

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
