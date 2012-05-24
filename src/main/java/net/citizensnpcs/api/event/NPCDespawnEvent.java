package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC despawns.
 */
public class NPCDespawnEvent extends NPCEvent {
    public NPCDespawnEvent(NPC npc) {
        super(npc);
    }
}