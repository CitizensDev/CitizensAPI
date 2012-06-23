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

import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.abstraction.entity.EntityFactory;
import net.citizensnpcs.api.abstraction.entity.LivingEntity;
import net.citizensnpcs.api.ai.AI;

/**
 * Represents an NPC.
 */
public interface NPC extends Attachable {
    /**
     * Despawns this NPC.
     * 
     * @return Whether this NPC was able to despawn
     */
    public boolean despawn();

    /**
     * Permanently despawns and destroys this NPC, removing all data.
     */
    public void destroy();

    /**
     * Gets the {@link AI} of this NPC.
     * 
     * @return AI of this NPC
     */
    public AI getAI();

    /**
     * Gets the entity associated with this NPC.
     * 
     * @return Entity associated with this NPC
     */
    public LivingEntity getEntity();

    /**
     * Gets the unique ID of this NPC.
     * 
     * @return ID of this NPC
     */
    public int getId();

    /**
     * Gets the name of this NPC with color codes stripped.
     * 
     * @return Stripped name of this NPC
     */
    public String getName();

    /**
     * Gets whether this NPC is currently spawned.
     * 
     * @return Whether this NPC is spawned
     */
    public boolean isSpawned();

    /**
     * Sets the name of this NPC.
     * 
     * @param name
     *            Name to give this NPC
     */
    public void rename(String name);

    /**
     * Sets the {@link EntityFactory} of this NPC.
     * 
     * @param controller
     *            The new entity factory.
     */
    public void setEntityFactory(EntityFactory factory);

    /**
     * Attempts to spawn this NPC.
     * 
     * @param location
     *            Vector to spawn this NPC
     * @return Whether this NPC was able to spawn at the location
     */
    public boolean spawn(WorldVector location);
}
