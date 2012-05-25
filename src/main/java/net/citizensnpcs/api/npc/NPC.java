package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.abstraction.LivingEntity;
import net.citizensnpcs.api.abstraction.WorldVector;
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
     * Permanently destroys this NPC.
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
     * Attempts to spawn this NPC.
     * 
     * @param location
     *            Vector to spawn this NPC
     * @return Whether this NPC was able to spawn at the location
     */
    public boolean spawn(WorldVector location);
}