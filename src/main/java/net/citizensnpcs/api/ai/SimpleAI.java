/*
 * CitizensAPI
 * Copyright (C) 2012 CitizensDev <http://citizensnpcs.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.citizensnpcs.api.ai;

import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.npc.NPC;

import com.google.common.collect.Lists;

public class SimpleAI implements AI {
    private final List<GoalEntry> executingGoals = Lists.newArrayList();
    private final List<GoalEntry> goals = Lists.newArrayList();
    private final NPC npc;
    private List<Goal> toRemove = null;

    public SimpleAI(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void addGoal(int priority, Goal goal) {
        if (goals.contains(goal))
            return;
        goals.add(new GoalEntry(priority, goal));
    }

    private boolean isGoalAllowable(GoalEntry test) {
        for (int i = 0; i < goals.size(); ++i) {
            GoalEntry item = goals.get(i);
            if (item == test)
                continue;
            if (test.getPriority() >= item.getPriority()) {
                if (!test.getGoal().isCompatibleWith(item.getGoal()) && executingGoals.contains(item)) {
                    return false;
                }
            } /*
               * else if (executingGoals.contains(item) && !item.goal.requiresUpdates()) { return false; }
               */
        }

        return true;
    }

    @Override
    public void removeGoal(Goal goal) {
        if (toRemove == null)
            toRemove = Lists.newArrayList();
        toRemove.add(goal);
    }

    private void removeGoals() {
        if (toRemove == null)
            return;
        for (Goal goal : toRemove) {
            Iterator<GoalEntry> itr = executingGoals.iterator();
            while (itr.hasNext()) {
                GoalEntry entry = itr.next();
                if (entry.getGoal().equals(goal)) {
                    entry.getGoal().reset();
                    itr.remove();
                }
            }
            itr = goals.iterator();
            while (itr.hasNext()) {
                GoalEntry entry = itr.next();
                if (entry.getGoal().equals(goal))
                    itr.remove();
            }
        }

        toRemove = null;
    }

    public void update() {
        if (!npc.isSpawned()) {
            return;
        }
        removeGoals();
        for (int i = 0; i < goals.size(); ++i) {
            GoalEntry entry = goals.get(i);
            boolean executing = executingGoals.contains(entry);

            if (executing) {
                if (entry.getGoal().update() || !isGoalAllowable(entry)) {
                    entry.getGoal().reset();
                    executingGoals.remove(entry);
                }
            } else if (isGoalAllowable(entry) && entry.getGoal().start()) {
                executingGoals.add(entry);
            }
        }
    }

    public static class GoalEntry implements Comparable<GoalEntry> {
        private final Goal goal;
        private final int priority;

        public GoalEntry(int priority, Goal goal) {
            this.priority = priority;
            this.goal = goal;
        }

        @Override
        public int compareTo(GoalEntry o) {
            return o.priority > priority ? 1 : o.priority < priority ? -1 : 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            GoalEntry other = (GoalEntry) obj;
            if (goal == null) {
                if (other.goal != null) {
                    return false;
                }
            } else if (!goal.equals(other.goal)) {
                return false;
            }
            return true;
        }

        public Goal getGoal() {
            return goal;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public int hashCode() {
            return 31 + ((goal == null) ? 0 : goal.hashCode());
        }
    }
}
