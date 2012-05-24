package net.citizensnpcs.api.abstraction;

public interface Equipable {
    public void getEquipment(EquipmentSlot slot);

    public void setEquipment(EquipmentSlot slot, ItemStack item);
}
