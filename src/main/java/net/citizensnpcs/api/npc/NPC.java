package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.abstraction.LivingEntity;
import net.citizensnpcs.api.abstraction.Vector;
import net.citizensnpcs.api.ai.AI;
import net.citizensnpcs.api.attachment.Attachment;

/**
 * Represents an NPC with a Character and separate attachments.
 */
public interface NPC /*metadatable*/{
    public void attach(Class<? extends Attachment> attach);

    /**
     * Despawns this NPC.
     * 
     * @return Whether this NPC was able to despawn
     */
    public boolean despawn();

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
     * Gets the full name of this NPC.
     * 
     * @return Full name of this NPC
     */
    public String getFullName();

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
     * Gets an attachment from the given class.
     * 
     * @param attachment
     *            Attachment to get
     * @return Attachment with the given name
     */
    public <T extends Attachment> T getAttachment(Class<T> attachment);

    /**
     * Checks if this NPC has the given attachment.
     * 
     * @param attachment
     *            Attachment to check
     * @return Whether this NPC has the given attachment
     */
    public boolean isAttached(Class<? extends Attachment> attachment);

    /**
     * Gets whether this NPC is currently spawned.
     * 
     * @return Whether this NPC is spawned
     */
    public boolean isSpawned();

    /**
     * Permanently removes this NPC.
     */
    public void remove();

    /**
     * Removes an attachment from this NPC.
     * 
     * @param attachment
     *            Attachment to remove
     */
    public void detach(Class<? extends Attachment> attachment);

    /**
     * Sets the name of this NPC.
     * 
     * @param name
     *            Name to give this NPC
     */
    public void setName(String name);

    /**
     * Attempts to spawn this NPC.
     * 
     * @param location
     *            Vector to spawn this NPC
     * @return Whether this NPC was able to spawn at the location
     */
    public boolean spawn(Vector location);
}