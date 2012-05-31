package net.citizensnpcs.api.abstraction.entity;

public interface Creeper extends LandMob {
    boolean isPowered();

    void setPowered(boolean powered);
}
