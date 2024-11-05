package net.citizensnpcs.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.citizensnpcs.api.npc.NPC;

public class NPCDamageEntityEvent extends NPCDamageEvent {
    private final Entity damaged;

    public NPCDamageEntityEvent(NPC npc, EntityDamageByEntityEvent event) {
        super(npc, event);
        damaged = event.getEntity();
    }

    public Entity getDamaged() {
        return damaged;
    }
}
