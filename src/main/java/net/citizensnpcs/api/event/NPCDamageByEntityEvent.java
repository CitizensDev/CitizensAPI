package net.citizensnpcs.api.event;

import net.citizensnpcs.api.abstraction.Entity;
import net.citizensnpcs.api.npc.NPC;

public class NPCDamageByEntityEvent extends NPCDamageEvent {
    private final Entity entity;

    public NPCDamageByEntityEvent(NPC npc, int damage, Entity entity) {
        super(npc, damage);
        this.entity = entity;
    }

    public Entity getDamager() {
        return entity;
    }
}
