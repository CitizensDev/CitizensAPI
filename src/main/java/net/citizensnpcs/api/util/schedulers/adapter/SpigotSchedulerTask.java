package net.citizensnpcs.api.util.schedulers.adapter;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import net.citizensnpcs.api.util.schedulers.SchedulerTask;

public class SpigotSchedulerTask implements SchedulerTask {
    private final BukkitTask task;

    public SpigotSchedulerTask(BukkitTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public BukkitTask getOriginalTask() {
        return task;
    }

    @Override
    public Plugin getPlugin() {
        return task.getOwner();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

    @Override
    public boolean isRepeating() {
        return false;
    }
}
