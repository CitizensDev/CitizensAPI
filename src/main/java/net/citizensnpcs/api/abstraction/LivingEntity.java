package net.citizensnpcs.api.abstraction;

public interface LivingEntity extends Entity {
    int getHealth();

    MobType getType();

    void setHealth(int health);
}
