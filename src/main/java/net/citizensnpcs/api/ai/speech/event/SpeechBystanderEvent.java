package net.citizensnpcs.api.ai.speech.event;

import net.citizensnpcs.api.ai.speech.Tongue;
import net.citizensnpcs.api.ai.speech.VocalChord;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Represents an event where an NPC speaks with a {@link Tongue}.
 * 
 */
public class SpeechBystanderEvent extends Event implements Cancellable {
    
	private boolean cancelled = false;

    Tongue tongue;
    VocalChord vocalChord;
    String text;
	
    public SpeechBystanderEvent(Tongue tongue, String text, VocalChord vocalChord) {
		this.tongue = tongue;
		this.vocalChord = vocalChord;
	}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * The raw message to be sent to the bystander. This should be already formatted
     * by the VocalChord at this point.
     * 
     * @returns the message to be sent to the {@link Talkable} bystander.
     */
    public String getMessage() {
    	return text;
    }
    
    /**
     * Sets the raw message to be sent to the bystander.
     * 
     * @returns the message to be sent to the {@link Talkable} bystander.
     */
    public void setMessage(String formattedMessage) {
    	this.text = formattedMessage;
    }
    
    /**
     * Gets the {@link Tongue} context.
     * 
     * @returns the Tongue
     */
    public Tongue getTongue() {
    	return tongue;
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