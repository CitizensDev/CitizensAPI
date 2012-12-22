package net.citizensnpcs.api.ai.speech;

public interface SpeechFactory {
	/**
	 * Registers a {@link VocalChord} class with the SpeechController, making it
	 * available for use within.  Requires a 'name', which should generally
	 * describe the intent of the VocalChord.
	 * 
	 * @param clazz
	 * 			The VocalChord class
	 * @param name
	 * 			The name of the VocalChord
	 * 
	 */
	public void register(Class<? extends VocalChord> clazz, String name);

	/**
	 * Returns the registered name of a VocalChord class
	 * 
	 * @param clazz
	 * 			The VocalChord class
	 * @returns the registered name of the VocalChord class.
	 * 
	 */
	public String getVocalChordName(Class<? extends VocalChord> clazz);
	
	/**
	 * Creates a new instance of a VocalChord
	 * 
	 * @param name
	 * 			The name of the desired VocalChord
	 * @returns a new instance of this VocalChord
	 * 
	 */
	public VocalChord getVocalChord(String name);

	/**
	 * Creates a new instance of a VocalChord
	 * 
	 * @param name
	 * 			The class of the desired VocalChord
	 * @returns a new instance of this VocalChord
	 * 
	 */
	public VocalChord getVocalChord(Class<? extends VocalChord> clazz);
	
}
