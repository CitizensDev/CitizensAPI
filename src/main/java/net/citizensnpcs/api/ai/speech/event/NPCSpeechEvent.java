package net.citizensnpcs.api.ai.speech.event;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.event.NPCEvent;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Represents an event where an NPC speaks, with {@link SpeechContext}. This event
 * takes place before being sent to the {@link VocalChord}. 
 * 
 */
public class NPCSpeechEvent extends NPCEvent implements Cancellable {
    
	private boolean cancelled = false;

    SpeechContext context;
    String vocalChord;
	
    public NPCSpeechEvent(SpeechContext context, String vocalChordName) {
		super(CitizensAPI.getNPCRegistry().getNPC(context.getTalker().getEntity()));
		this.context = context;
	}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * Returns the tongue that will be sent to the VocalChord.
     * 
     * @returns the Tongue
     */
    public SpeechContext getContext() {
    	return context;
    }
    
    /**
     * Returns the name of the VocalChord that will be used.
     */
    public String getVocalChordName() {
    	return vocalChord;
    }
    
    /**
     * Sets the name of the VocalChord to be used.
     */
    public void setVocalChord(String vocalChordName) {
    	vocalChord = vocalChordName;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}