package net.citizensnpcs.api.abstraction;

public interface Player extends LivingEntity, CommandSender, Equipable {

    void setArmor(ItemStack[] armor);
}
