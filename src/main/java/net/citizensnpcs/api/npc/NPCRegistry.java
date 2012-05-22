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
package net.citizensnpcs.api.npc;

/**
 * Registers {@link NPC}'s with Citizens. NPCs in this registry will be saved to disk on server shutdown and loaded on
 * server start.
 */
public interface NPCRegistry {

    /**
     * Creates and spawns an {@link NPC} with the given name.
     * 
     * @param name
     *            Name to give the NPC
     * @return Instance of the NPC
     */
    public NPC createAndSpawnNPC(String name);

    /**
     * Creates an {@link NPC} with the given name.
     * 
     * @param name
     *            Name to give the NPC
     * @return Instance of the NPC
     */
    public NPC createNPC(String name);

    /**
     * Unregisters the given {@link NPC} and removes all data about it from the data store.
     * 
     * @param npc
     *            NPC to unregister
     */
    public void deregister(NPC npc);
}
