package net.citizensnpcs.api.ai.speech;

import java.util.Collections;
import java.util.List;

public class Tongue {

	private Talkable talker;
	private List<Talkable> recipients;
	private String contents;
	
	public Tongue(Talkable talker, String text) {
		this.talker = talker;
		this.contents = text;
		this.recipients = Collections.emptyList();
	}
	
	/**
	 * Adds a direct {@link Talkable} recipient. The {@link VocalChord} should 
	 * use this information to correctly direct the message. Note: depending 
	 * on the VocalChord, this list may not be inclusive as to who gets the 
	 * message.
	 * 
	 * @param talkable Talkable entity
	 * @returns the Tongue
	 * 
	 */
	public Tongue addRecipient(Talkable talkable) {
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
	public Tongue addRecipients(List<Talkable> talkables) {
		recipients.addAll(talkables);
		return this;
	}
	
	/**
	 * Sets the text message sent. Overrides text
	 * set with the constructor.
	 * 
	 * @param text
	 * 			The text to send.
	 * 
	 */
	public void setContents(String text) {
		contents = text;
	}
	
	/**
     * Gets the text message sent.
     * 
     */
    public String getContents() {
    	return contents;
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
     * Gets the talker.
     * 
     * @return NPC doing the talking
     * 
     */
    public void setTalker(Talkable talker) {
    	this.talker = talker;
    }
    
    /**
     * Gets direct recipients, if any.
     * 
     * @return List<Talkable> of direct recipients.
     * 
     */
    public List<Talkable> getRecipients() {
    	return recipients;
    }
    
    /**
     * Checks if there are any recipients. If none, this {@link Tongue}
     * is not targeted.
     * 
     * @returns true if recipients are specified.
     */
    public boolean isTargeted() {
    	return recipients.isEmpty() ? false : true;
    }
    
}
