package net.citizensnpcs.api.event;

import org.bukkit.event.Event;

/**
 * Represents an event thrown by Citizens.
 */
public abstract class CitizensEvent extends Event {
    protected CitizensEvent() {
    }

    protected CitizensEvent(boolean async) {
        super(async);
    }
}