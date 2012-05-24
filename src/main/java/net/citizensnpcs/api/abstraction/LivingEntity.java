package net.citizensnpcs.api.abstraction;

public interface LivingEntity extends Entity {
    MobType getType();

    int getHealth();

    void setHealth(int health);
}
