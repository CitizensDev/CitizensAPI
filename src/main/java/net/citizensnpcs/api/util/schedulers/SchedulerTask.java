package net.citizensnpcs.api.util.schedulers;

import org.bukkit.plugin.Plugin;

public interface SchedulerTask {
    void cancel();

    Object getOriginalTask();

    Plugin getPlugin();

    boolean isCancelled();

    boolean isRepeating();
}