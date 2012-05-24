package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.abstraction.MobType;

/**
 * Handles various NPC-related methods.
 */
public interface NPCRegistry extends Iterable<NPC> {

    /**
     * Creates an NPC with no attached character. This does not spawn the NPC.
     * 
     * @param type
     *            Entity type to assign to the NPC
     * @param name
     *            Name to give the NPC
     * @return Created NPC
     */
    public NPC createNPC(MobType type, String name);

    /**
     * Deregisters the {@link NPC} and removes all data about it from the data
     * store.
     * 
     * @param npc
     */
    public void deregister(NPC npc);

    public NPC getById(int id);
}