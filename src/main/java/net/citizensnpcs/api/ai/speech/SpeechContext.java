package net.citizensnpcs.api.ai.speech;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.npc.NPC;

/**
 * SpeechContext contains information about a SpeechEvent, including
 * the {@link Talkable} talker, recipients, and a message.
 *
 */
public class SpeechContext implements Iterable<Talkable> {

	private Talkable talker;
	private List<Talkable> recipients;
	private String message;

	public SpeechContext(NPC talker, String message) {
		this.talker = new TalkableEntity(talker);
		this.message = message;
		this.recipients = Collections.emptyList();
	}
	
	public SpeechContext(String message) {
		this(null, message);
	}
	
	public SpeechContext(String message, Talkable recipient) {
		this(null, message, recipient);
	}
	
	public SpeechContext(NPC talker, String message, Talkable recipient) {
		this(talker, message);
		recipients.add(recipient);
	}
	
	/**
	 * Adds a direct {@link Talkable} recipient. The {@link VocalChord} should 
	 * use this information to correctly direct the message. Note: depending 
	 * on the VocalChord, this list may not be inclusive as to who gets the 
	 * message.
	 * 
	 * @param talkable Talkable entity
	 * @returns the speech context
	 * 
	 */
	public SpeechContext addRecipient(Talkable talkable) {
		recipients.add(talkable);
		return this;
	}

	/**
	 * Adds a list of {@link Talkable} recipients. The {@link VocalChord} should 
	 * use this information to correctly direct the message. Note: depending on
	 * the VocalChord, this list may not be inclusive as to who gets the 
	 * message.
	 * 
	 * @param talkable Talkable entity
	 * @returns the Tongue
	 * 
	 */
	public SpeechContext addRecipients(List<Talkable> talkables) {
		recipients.addAll(talkables);
		return this;
	}
	
	/**
	 * Sets the text message sent. Overrides text set with the constructor.
	 * 
	 * @param message
	 * 			The text to send.
	 * 
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
     * Gets the text message sent.
     * 
     */
    public String getMessage() {
    	return message;
    }
    
    /**
     * Gets the talker.
     * 
     * @return NPC doing the talking
     * 
     */
    public Talkable getTalker() {
    	return talker;
    }
    
    /**
     * Sets the talker.
     * 
     * @param talker 
     * 			NPC doing the talking
     * 
     */
    public void setTalker(Talkable talker) {
    	this.talker = talker;
    }
       
    /**
     * Checks if there are any recipients. If none, this {@link SpeechContext}
     * is not targeted.
     * 
     * @returns true if recipients are specified.
     */
    public boolean hasRecipients() {
    	return recipients.isEmpty() ? false : true;
    }

    /**
     * Gets direct recipients, if any.
     * 
     * @return recipients
     * 
     */
 	@Override
	public Iterator<Talkable> iterator() {
        final Iterator<Talkable> itr = recipients.iterator();
        return itr;
	}
 	
 	/**
 	 * @returns number of recipients.
 	 */
 	public int size() {
 		return recipients.size();
 	}
 	
    
}
