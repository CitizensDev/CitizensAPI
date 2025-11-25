package net.citizensnpcs.api.ai.tree.expr;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.citizensnpcs.api.npc.NPC;

/**
 * Manages signals for behavior trees. Signals allow behavior trees to communicate and wait for events.
 */
public class SignalManager {
    private final Map<UUID, Multimap<String, SignalListener>> listeners = new ConcurrentHashMap<>();

    /**
     * Clears all listeners for a specific NPC.
     *
     * @param npc
     *            the NPC
     */
    public void clearListeners(NPC npc) {
        listeners.remove(npc.getUniqueId());
    }

    /**
     * Emits a signal for a specific NPC. All behaviors waiting for this signal will be notified.
     *
     * @param npc
     *            the NPC emitting the signal
     * @param signal
     *            the signal name
     */
    public void emit(NPC npc, String signal) {
        emitToNPC(npc.getUniqueId(), signal);
    }

    /**
     * Emits a global signal that all NPCs can receive.
     *
     * @param signal
     *            the signal name
     */
    public void emitGlobal(String signal) {
        for (Multimap<String, SignalListener> bySignal : listeners.values()) {
            Collection<SignalListener> signalListeners = bySignal.get(signal);
            if (signalListeners != null) {
                for (SignalListener listener : signalListeners) {
                    listener.signal();
                }
            }
        }
    }

    /**
     * Emits a signal to a specific NPC by ID. All behaviors waiting for this signal will be notified.
     *
     * @param npcId
     *            the NPC ID
     * @param signal
     *            the signal name
     */
    public void emitToNPC(UUID npcId, String signal) {
        Multimap<String, SignalListener> npcListeners = listeners.get(npcId);
        if (npcListeners == null)
            return;

        Collection<SignalListener> signalListeners = npcListeners.get(signal);
        if (signalListeners != null) {
            for (SignalListener listener : signalListeners) {
                listener.signal();
            }
        }
    }

    /**
     * Registers a listener for a signal on a specific NPC.
     *
     * @param npc
     *            the NPC to listen on
     * @param signal
     *            the signal name
     * @param listener
     *            the listener to notify
     */
    public void listen(NPC npc, String signal, SignalListener listener) {
        listeners.computeIfAbsent(npc.getUniqueId(), k -> HashMultimap.create()).put(signal, listener);
    }

    /**
     * Removes a listener.
     *
     * @param npc
     *            the NPC
     * @param signal
     *            the signal name
     * @param listener
     *            the listener to remove
     */
    public void unlisten(NPC npc, String signal, SignalListener listener) {
        Multimap<String, SignalListener> bySignal = listeners.get(npc.getUniqueId());
        if (bySignal == null)
            return;

        Collection<SignalListener> signals = bySignal.get(signal);
        if (signals != null) {
            signals.remove(listener);
        }
        if (bySignal.isEmpty()) {
            listeners.remove(npc.getUniqueId());
        }
    }

    /**
     * Listener interface for signal notifications.
     */
    @FunctionalInterface
    public interface SignalListener {
        void signal();
    }
}
