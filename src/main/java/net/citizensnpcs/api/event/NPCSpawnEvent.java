package net.citizensnpcs.api.event;

import net.citizensnpcs.api.abstraction.Cancellable;
import net.citizensnpcs.api.abstraction.Vector;
import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC spawns.
 */
public class NPCSpawnEvent extends NPCEvent implements Cancellable {
    private boolean cancelled = false;
    private final Vector location;

    public NPCSpawnEvent(NPC npc, Vector location) {
        super(npc);
        this.location = location;
    }

    /**
     * Gets the location where the NPC was spawned.
     * 
     * @return Location where the NPC was spawned
     */
    public Vector getLocation() {
        return location;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}