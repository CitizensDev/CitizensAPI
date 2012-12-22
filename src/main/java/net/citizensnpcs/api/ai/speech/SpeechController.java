package net.citizensnpcs.api.ai.speech;

import net.citizensnpcs.api.npc.NPC;

/**
 * Represents the NPCs speech abilities using VocalChords registered with
 * the {@link SpeechFactory}. Uses {@link Tongue}s which contain messages
 * and recipients.
 * 
 */
public interface SpeechController {
	
	/**
	 * Sends the speechController's {@link NPC} and {@link Tongue} to the current default
	 * {@link VocalChord} for the NPC. If none, the default {@link ChatVocalChord}
	 * is used.
	 * 
	 * @param message
	 * 			The message to speak
	 */
	public void speak(Tongue message);
	
	/**
	 * Sends the speechController's {@link NPC} and {@link Tongue} to the specified
	 * {@link VocalChord}.
	 * 
	 * @param message
	 * 			The message to speak
	 * @param vocalChord
	 * 			
	 */
	public void speak(Tongue message, Class<VocalChord> vocalChord);
	
}
