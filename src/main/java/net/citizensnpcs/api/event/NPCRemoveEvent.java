package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

public class NPCRemoveEvent extends NPCEvent {
    public NPCRemoveEvent(NPC npc) {
        super(npc);
    }

}
