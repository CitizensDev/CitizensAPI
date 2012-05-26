package net.citizensnpcs.api.abstraction;

public interface EntityController {
    /**
     * Spawns an {@link Entity} at the given location.
     * 
     * @param at
     *            Where to spawn the entity
     * @return The spawned entity
     */
    public void spawn(WorldVector at);

    public void despawn();

    public Entity getEntity();
}
