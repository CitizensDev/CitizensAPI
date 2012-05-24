package net.citizensnpcs.api.event;

import net.citizensnpcs.api.abstraction.CommandSender;
import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC is selected by a player.
 */
public class NPCSelectEvent extends NPCEvent {
    private final CommandSender sender;

    public NPCSelectEvent(NPC npc, CommandSender sender) {
        super(npc);
        this.sender = sender;
    }

    /**
     * Gets the selector of the NPC.
     * 
     * @return CommandSender that selected an NPC
     */
    public CommandSender getSelector() {
        return sender;
    }
}