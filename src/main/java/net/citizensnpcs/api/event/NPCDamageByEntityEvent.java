package net.citizensnpcs.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.citizensnpcs.api.npc.NPC;

public class NPCDamageByEntityEvent extends NPCDamageEvent {
    private final Entity damager;

    public NPCDamageByEntityEvent(NPC npc, EntityDamageByEntityEvent event) {
        super(npc, event);
        damager = event.getDamager();
    }

    public Entity getDamager() {
        return damager;
    }
}
