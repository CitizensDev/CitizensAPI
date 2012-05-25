package net.citizensnpcs.api.abstraction;

public interface LandMob extends LivingEntity { // TODO: better name
    boolean hasDestination();

    boolean hasTarget();

    void setDestination(WorldVector destination);

    void setTarget(LivingEntity target, boolean aggressive);
}
