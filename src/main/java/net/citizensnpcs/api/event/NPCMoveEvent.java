package net.citizensnpcs.api.event;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import net.citizensnpcs.api.npc.NPC;

public class NPCMoveEvent extends Event implements Cancellable {
    private boolean cancelled;

    private Location from;
    private final NPC npc;
    private Location to;

    public NPCMoveEvent(NPC npc, Location from, Location to) {
        this.npc = npc;
        this.from = from;
        this.to = to;
    }

    public Location getFrom() {
        return from;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public NPC getNPC() {
        return npc;
    }

    public Location getTo() {
        return to;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public void setFrom(Location from) {
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(from.getWorld(), "from.getWorld() cannot be null");
        this.from = from;
    }

    public void setTo(Location to) {
        Objects.requireNonNull(to, "to cannot be null");
        Objects.requireNonNull(to.getWorld(), "to.getWorld() cannot be null");
        this.to = to;
    }

    @Override
    public String toString() {
        return "NPCMoveEvent{" + "npc=" + npc + ", from=" + from + ", to=" + to + ", cancelled=" + cancelled + '}';
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
