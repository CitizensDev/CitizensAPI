package net.citizensnpcs.api.trait.trait;

import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents an NPC's mob type.
 */
@TraitName("type")
public class MobType extends Trait {
    @Persist("")
    private EntityType type = EntityType.PLAYER;

    public MobType() {
        super("type");
    }

    /**
     * Gets the type of mob that an NPC is.
     *
     * @return The mob type
     */
    public EntityType getType() {
        return type;
    }

    @Override
    public void load(DataKey key) {
        npc.setBukkitEntityType(type);
    }

    @Override
    public void onSpawn() {
        type = npc.getEntity().getType();
    }

    /**
     * Sets the type of mob that an NPC is.
     *
     * @param type
     *            Mob type to set the NPC as
     */
    public void setType(EntityType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MobType{" + type + "}";
    }
}