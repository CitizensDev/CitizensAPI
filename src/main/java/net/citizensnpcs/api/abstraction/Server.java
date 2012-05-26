package net.citizensnpcs.api.abstraction;

import org.mozilla.javascript.ContextFactory.Listener;

public interface Server {
    void callEvent(Event event);

    String getMinecraftVersion();

    void registerEvents(Listener trait);

    void schedule(Runnable task);

    void schedule(Runnable task, long delay);

    void scheduleRepeating(Runnable task, long delay);

    void scheduleRepeating(Runnable task, long initialDelay, long repeatDelay);

    void unregisterAll(Listener trait);
}
