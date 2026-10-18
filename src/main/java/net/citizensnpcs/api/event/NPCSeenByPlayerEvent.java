package net.citizensnpcs.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class NPCSeenByPlayerEvent extends NPCEvent implements Cancellable {
    private boolean cancel;
    private final boolean firstSeen;
    private final Player player;

    public NPCSeenByPlayerEvent(NPC npc, Player player, boolean firstSeen) {
        super(npc);
        this.player = player;
        this.firstSeen = firstSeen;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    public boolean isFirstSeen() {
        return firstSeen;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();

}
