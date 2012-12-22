package net.citizensnpcs.api.ai.speech;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.event.SpeechBystanderEvent;
import net.citizensnpcs.api.ai.speech.event.SpeechTargetedEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class TalkableEntity implements Talkable {

	LivingEntity entity;
	
	public TalkableEntity(NPC npc) {
		entity = npc.getBukkitEntity();
	}
	
	public TalkableEntity(Player player) {
		entity = (LivingEntity) player;
	}
	
	public TalkableEntity(LivingEntity entity) {
		this.entity = entity;
	}
	
	@Override
	public void talkTo(Tongue context, String text, VocalChord vocalChord) {
		SpeechTargetedEvent event = new SpeechTargetedEvent(context, text, vocalChord);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		else talk(event.getMessage());
	}

	@Override
	public void talkNear(Tongue context, String text, VocalChord vocalChord) {
		SpeechBystanderEvent event = new SpeechBystanderEvent(context, text, vocalChord);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		else talk(event.getMessage());
	}
	
	@Override
	public LivingEntity getEntity() {
		return entity;
	}
	
	private void talk(String message) {
		if (entity instanceof Player 
				&& !CitizensAPI.getNPCRegistry().isNPC(entity)) 
			((Player) entity).sendMessage(message);
	}

	@Override
	public String getName() {
		if (CitizensAPI.getNPCRegistry().isNPC(entity))
			return CitizensAPI.getNPCRegistry().getNPC(entity).getName();
		else if (entity instanceof Player)
			return ((Player) entity).getName();
		else
			return entity.getType().getName().replace("_", " ");
	}

}
