package net.citizensnpcs.api.event;

import net.citizensnpcs.api.abstraction.Player;
import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC is left-clicked by a player.
 */
public class NPCLeftClickEvent extends NPCClickEvent {
    public NPCLeftClickEvent(NPC npc, Player leftClicker) {
        super(npc, leftClicker);
    }
}