package net.citizensnpcs.api.util.schedulers.adapter;

import org.bukkit.plugin.Plugin;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.citizensnpcs.api.util.schedulers.SchedulerTask;

public class FoliaSchedulerTask implements SchedulerTask {
    private final ScheduledTask task;

    public FoliaSchedulerTask(ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public ScheduledTask getOriginalTask() {
        return task;
    }

    @Override
    public Plugin getPlugin() {
        return task.getOwningPlugin();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

    @Override
    public boolean isRepeating() {
        return task.isRepeatingTask();
    }
}
