package net.citizensnpcs.api.abstraction;

public interface Entity {
    WorldVector getLocation();

    World getWorld();

    void remove();
}
