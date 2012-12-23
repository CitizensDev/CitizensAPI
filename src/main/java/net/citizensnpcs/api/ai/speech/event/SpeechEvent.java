package net.citizensnpcs.api.ai.speech.event;

import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Represents an event where a {@link Talkable} entity speaks at/near a {@link Talkable} entity.
 * 
 */
public class SpeechEvent extends Event implements Cancellable {
    
	private boolean cancelled = false;

    SpeechContext context;
    VocalChord vocalChord;
    String message;
    Talkable target;
	
    public SpeechEvent(Talkable target, SpeechContext context, String message, VocalChord vocalChord) {
		this.target = target;
    	this.context = context;
		this.vocalChord = vocalChord;
		this.message = message;
	}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * The final message to be sent to the bystander. Note: This may differ from
     * the message contained in the SpeechContext, as formatting may have occurred.
     * 
     * @returns the message to be sent to the {@link Talkable} bystander.
     */
    public String getMessage() {
    	return message;
    }
    
    /**
     * Sets the message to be sent to the bystander. Note: This may differ from
     * the message contained in the SpeechContext, as formatting may have occurred.
     * 
     * @returns the message to be sent
     */
    public void setMessage(String formattedMessage) {
    	this.message = formattedMessage;
    }
    
    /**
     * Gets the {@link SpeechContext} associated with the SpeechEvent.
     * 
     * @returns context
     */
    public SpeechContext getContext() {
    	return context;
    }
    
    /**
     * Returns the name of the VocalChord that called this event.
     */
    public String getVocalChordName() {
    	return vocalChord.getName();
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