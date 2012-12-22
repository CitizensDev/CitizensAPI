package net.citizensnpcs.api.ai.speech;

import org.bukkit.Bukkit;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Speech;

public class SimpleSpeechController implements SpeechController {

	NPC npc;
	
	public SimpleSpeechController(NPC npc) {
		this.npc = npc;
	}

	@Override
	public void speak(Tongue tongue) {
		tongue.setTalker(new TalkableEntity(npc));
		NPCSpeechEvent event = new NPCSpeechEvent(tongue, npc.getTrait(Speech.class).getDefaultVocalChord());
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		CitizensAPI.getSpeechFactory().getVocalChord(event.getVocalChordName()).talk(tongue);
	}

	@Override
	public void speak(Tongue tongue, Class<VocalChord> vocalChordClass) {
		tongue.setTalker(new TalkableEntity(npc));
		NPCSpeechEvent event = new NPCSpeechEvent(tongue, CitizensAPI.getSpeechFactory().getVocalChordName(vocalChordClass));
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		CitizensAPI.getSpeechFactory().getVocalChord(event.getVocalChordName()).talk(tongue);		
	}
	
}
