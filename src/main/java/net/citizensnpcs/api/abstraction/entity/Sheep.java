package net.citizensnpcs.api.abstraction.entity;

public interface Sheep extends LandMob {
    boolean isSheared();

    void setSheared(boolean sheared);
}
