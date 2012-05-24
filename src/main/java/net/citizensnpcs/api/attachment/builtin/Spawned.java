package net.citizensnpcs.api.attachment.builtin;

import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents the spawn state of an NPC. This only determines whether an NPC
 * should spawn onEnable. For checking if an NPC's entity is spawned, use
 * NPC.isSpawned().
 */
public class Spawned extends Attachment {
    private boolean shouldSpawn = true;

    @Override
    public void load(DataKey key) throws NPCLoadException {
        shouldSpawn = key.getBoolean("");
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", shouldSpawn);
    }

    /**
     * Sets whether an NPC should spawn during server starts or reloads.
     * 
     * @param shouldSpawn
     *            Whether an NPC should spawn
     */
    public void setSpawned(boolean shouldSpawn) {
        this.shouldSpawn = shouldSpawn;
    }

    /**
     * Gets whether an NPC should spawn during server starts or reloads.
     * 
     * @return Whether an NPC should spawn
     */
    public boolean shouldSpawn() {
        return shouldSpawn;
    }

    @Override
    public String toString() {
        return "Spawned{" + shouldSpawn + "}";
    }
}