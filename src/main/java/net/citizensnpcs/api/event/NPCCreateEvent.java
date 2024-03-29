package net.citizensnpcs.api.event;

import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class NPCCreateEvent extends NPCEvent {
    public NPCCreateEvent(NPC npc) {
        super(npc);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
