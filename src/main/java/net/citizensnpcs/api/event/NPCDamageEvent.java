package net.citizensnpcs.api.event;

import net.citizensnpcs.api.abstraction.Cancellable;
import net.citizensnpcs.api.npc.NPC;

public class NPCDamageEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;
    private int damage;

    public NPCDamageEvent(NPC npc, int damage) {
        super(npc);
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
