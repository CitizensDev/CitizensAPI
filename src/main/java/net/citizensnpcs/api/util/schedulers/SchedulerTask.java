package net.citizensnpcs.api.util.schedulers;

import org.bukkit.plugin.Plugin;

public interface SchedulerTask {

    Plugin getPlugin();

    boolean isCancelled();

    boolean isRepeating();

    void cancel();

    Object getOriginalTask();
}