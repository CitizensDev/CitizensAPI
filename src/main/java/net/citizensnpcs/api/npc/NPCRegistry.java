package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.abstraction.MobType;
import net.citizensnpcs.api.abstraction.WorldVector;

/**
 * A registry for {@link NPC}s, useful for lookup and iteration. Each NPC is
 * given a unique id and can be looked up via {@link NPCRegistry#getById(int)}
 */
public interface NPCRegistry extends Iterable<NPC> {
    /**
     * A convenience method to create, spawn and register an {@link NPC}.
     * 
     * @see NPCRegistry#register(NPC)
     * @see NPC#spawn(WorldVector)
     * 
     * @param name
     *            The name of the NPC
     * @param at
     *            Where to spawn the NPC at
     * @param type
     *            The {@link MobType} of the NPC
     * @return The newly created NPC
     */
    public NPC createAndSpawn(String name, WorldVector at, MobType type);

    /**
     * Deregisters the {@link NPC} and removes all data about it from the data
     * store.
     * 
     * @param npc
     */
    public void deregister(NPC npc);

    /**
     * Finds the {@link NPC} registered with the given ID, which must be at
     * least 0.
     * 
     * @see NPC#getId()
     * @param id
     *            The id to find
     * @return The NPC with the given ID, or null if not found
     */
    public NPC getById(int id);

    /**
     * Registers the given {@link NPC} with this NPCRegistry, generating a
     * unique id and returning it.
     * 
     * @param npc
     *            The NPC to register
     * @return The generated ID for the NPC
     */
    public int register(NPC npc);
}