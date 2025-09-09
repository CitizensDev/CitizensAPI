package net.citizensnpcs.api.ai;

public enum PathfinderType {
    CITIZENS,
    CITIZENS_ASYNC,
    MINECRAFT,
    PLUGIN;

    public boolean isCitizens() {
        return name().startsWith("CITIZENS");
    }
}
