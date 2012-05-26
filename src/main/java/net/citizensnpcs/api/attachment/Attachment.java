package net.citizensnpcs.api.attachment;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.Attachable;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents an attachment that can be attached to a given {@link Attachable},
 * loaded and saved.
 */
public abstract class Attachment {
    private String name;

    /**
     * Gets the name of this trait.
     * 
     * @return Name of this trait
     */
    public final String getName() {
        return name;
    }

    /**
     * Loads a trait.
     * 
     * @param key
     *            DataKey to load from
     * @throws NPCLoadException
     *             Thrown if this trait failed to load properly
     */
    public abstract void load(DataKey key) throws NPCLoadException;

    /**
     * Called when this attachment is detached.
     */
    public void onRemove() {
    }

    /**
     * Called when an NPC is spawned. NPCs cannot be physically modified until
     * the entity is created in-game. This is called after the entity has been
     * created.
     */
    public void onSpawn() {
    }

    /**
     * Saves a trait.
     * 
     * @param key
     *            DataKey to save to
     */
    public abstract void save(DataKey key);

    /**
     * Sets the name of this attachment.
     * 
     * @param name
     *            The new name
     */
    public final void setName(String name) {
        if (this.name != null)
            throw new IllegalArgumentException("Cannot change the name of a trait");
        this.name = name;
    }
}