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

import net.citizensnpcs.api.npc.NPC;

/**
 * Represents an event thrown by an NPC.
 */
public abstract class NPCEvent extends CitizensEvent {
    private final NPC npc;

    protected NPCEvent(NPC npc) {
        super();
        this.npc = npc;
    }

    /**
     * Get the npc involved in the event.
     * 
     * @return the npc involved in the event
     */
    public NPC getNPC() {
        return npc;
    }
}
