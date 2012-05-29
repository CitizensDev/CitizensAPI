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
package net.citizensnpcs.api.event;

import net.citizensnpcs.api.abstraction.Cancellable;
import net.citizensnpcs.api.abstraction.Vector;
import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC spawns.
 */
public class NPCSpawnEvent extends NPCEvent implements Cancellable {
    private boolean cancelled = false;
    private final Vector location;

    public NPCSpawnEvent(NPC npc, Vector location) {
        super(npc);
        this.location = location;
    }

    /**
     * Gets the location where the NPC was spawned.
     * 
     * @return Location where the NPC was spawned
     */
    public Vector getLocation() {
        return location;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
