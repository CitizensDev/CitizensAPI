package net.citizensnpcs.api.abstraction;

public interface Equipable {
    public void getEquipment(Equipment slot);

    public void setEquipment(Equipment slot, ItemStack item);
}
