package net.citizensnpcs.api.abstraction;

import org.mozilla.javascript.ContextFactory.Listener;

public interface Server {
    void schedule(Runnable task, long delay);

    void schedule(Runnable task);

    void scheduleRepeating(Runnable task, long initialDelay, long repeatDelay);

    void scheduleRepeating(Runnable task, long delay);

    void registerEvents(Listener trait);

    void unregisterAll(Listener trait);

    void callEvent(Event event);
}
