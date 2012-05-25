package net.citizensnpcs.api.npc;

/**
 * Handles various NPC-related methods.
 */
public interface NPCRegistry extends Iterable<NPC> {
    /**
     * Deregisters the {@link NPC} and removes all data about it from the data
     * store.
     * 
     * @param npc
     */
    public void deregister(NPC npc);

    public NPC getById(int id);

    public int register(NPC npc);
}