package net.citizensnpcs.api.ai.speech;

import org.bukkit.entity.LivingEntity;

/**
 * Talkable provides an for talking to Players, Entities and NPCs in a single
 * format, with information about the Talker.
 */
public interface Talkable {
	
    /**
     * Called by a {@link VocalChord} when talking to this Talkable Entity
     * to provide a universal method to getting an event/output.
     * 
     * @param talker
     *            The {@link Talkable} entity doing the talking
     * @param text
     *            The text to talk
     * 
     */
    public void talkTo(Tongue context, String text, VocalChord vocalChord);

    /**
     * Called by a {@link VocalChord} when talking near this Talkable Entity
     * to provide a universal method to getting an event/output.
     * 
     * @param talker
     *            The {@link Talkable} entity doing the talking
     * @param text
     *            The text to talk
     * 
     */
    public void talkNear(Tongue context, String text, VocalChord vocalChord);
    
    /**
     * Gets the Bukkit LivingEntity of the Talkable.
     * 
     * @returns a LivingEntity
     * 
     */
    public LivingEntity getEntity();
    
    /**
     * Gets the name of the Talkable LivingEntity
     * 
     * @return name
     */
    public String getName();
    
    
}
