package net.citizensnpcs.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityCombustByEntityEvent;

import net.citizensnpcs.api.npc.NPC;

public class NPCCombustByEntityEvent extends NPCCombustEvent {
    private final EntityCombustByEntityEvent event;

    public NPCCombustByEntityEvent(EntityCombustByEntityEvent event, NPC npc) {
        super(event, npc);
        this.event = event;
    }

    /**
     * The combuster can be a WeatherStorm a Blaze, or an Entity holding a FIRE_ASPECT enchanted item.
     *
     * @return the Entity that set the combustee alight.
     */
    public Entity getCombuster() {
        return event.getCombuster();
    }
}
