package net.citizensnpcs.api.abstraction;

public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
