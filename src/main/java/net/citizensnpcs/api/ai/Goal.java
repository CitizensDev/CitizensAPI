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

/**
 * Represents an AI Goal that can be added to a queue of an NPC's goals.
 */
public interface Goal {
    /**
     * Returns whether this and the other {@link Goal} can be run at the same
     * time.
     * 
     * @param other
     *            The goal to check
     * @return Whether this goal is compatible
     */
    public boolean isCompatibleWith(Goal other);

    /**
     * Resets the goal and any resources or state it is holding.
     */
    public void reset();

    /**
     * Checks whether the goal can be run and sets up the execution of this goal
     * so that it can be updated later.
     * 
     * @return Whether the goal was started
     */
    public boolean shouldRun();

    /**
     * Updates the goal.
     * 
     * @return Whether the goal is finished.
     */
    public boolean update();
}
