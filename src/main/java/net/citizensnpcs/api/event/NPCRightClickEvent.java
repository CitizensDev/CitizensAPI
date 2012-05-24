package net.citizensnpcs.api.event;

import net.citizensnpcs.api.abstraction.Player;
import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC is right-clicked by a player.
 */
public class NPCRightClickEvent extends NPCClickEvent {
    public NPCRightClickEvent(NPC npc, Player rightClicker) {
        super(npc, rightClicker);
    }
}