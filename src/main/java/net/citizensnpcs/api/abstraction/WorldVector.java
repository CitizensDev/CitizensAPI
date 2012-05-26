package net.citizensnpcs.api.abstraction;

public interface WorldVector extends Vector {
    World getWorld();

    Chunk getChunk();

    float getPitch();

    float getYaw();
}
