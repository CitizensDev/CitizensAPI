package net.citizensnpcs.api.ai.speech;

public interface VocalChord {
    /**
     * Called when an NPC's {@link SpeechController} needs to output some text.
     * 
     * @param tongue
     * 			The {@link Tongue} with talk information
     * 
     */
    public void talk(Tongue tongue);
    
    /**
     * Should return the name of the vocal chord used in the registration process
     * 
     * @returns name of the VocalChord
     */
    public String getName();

}
