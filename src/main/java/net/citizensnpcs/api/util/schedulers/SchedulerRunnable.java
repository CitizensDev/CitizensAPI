package net.citizensnpcs.api.util.schedulers;

import net.citizensnpcs.api.util.schedulers.runnables.FoliaSchedulerRunnable;
import net.citizensnpcs.api.util.schedulers.runnables.SpigotSchedulerRunnable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public abstract class SchedulerRunnable implements Runnable {
    private SchedulerTask task;

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public abstract void run();

    public boolean isCancelled() {
        checkScheduled();
        return task.isCancelled();
    }

    public void cancel() {
        checkScheduled();
        task.cancel();
    }

    public SchedulerTask getTask() {
        checkScheduled();
        return task;
    }

    public int getTaskId() {
        checkScheduled();
        return task.getOriginalTask().hashCode();
    }

    protected SchedulerTask setupTask(SchedulerTask task) {
        this.task = task;
        return task;
    }

    private void checkScheduled() {
        if (task == null) throw new IllegalStateException("Task not yet scheduled");
    }

    public SchedulerTask runTask(Plugin plugin) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runTask(plugin) : new SpigotSchedulerRunnableImpl(this).runTask(plugin);
    }

    public SchedulerTask runTaskLater(Plugin plugin, long delayTicks) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runTaskLater(plugin, delayTicks) : new SpigotSchedulerRunnableImpl(this).runTaskLater(plugin, delayTicks);
    }

    public SchedulerTask runTaskTimer(Plugin plugin, long delayTicks, long periodTicks) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runTaskTimer(plugin, delayTicks, periodTicks) : new SpigotSchedulerRunnableImpl(this).runTaskTimer(plugin, delayTicks, periodTicks);
    }

    public SchedulerTask runTaskAsynchronously(Plugin plugin) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runTaskAsynchronously(plugin) : new SpigotSchedulerRunnableImpl(this).runTaskAsynchronously(plugin);
    }

    public SchedulerTask runTaskLaterAsynchronously(Plugin plugin, long delayTicks) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runTaskLaterAsynchronously(plugin, delayTicks) : new SpigotSchedulerRunnableImpl(this).runTaskLaterAsynchronously(plugin, delayTicks);
    }

    public SchedulerTask runTaskTimerAsynchronously(Plugin plugin, long delayTicks, long periodTicks) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runTaskTimerAsynchronously(plugin, delayTicks, periodTicks) : new SpigotSchedulerRunnableImpl(this).runTaskTimerAsynchronously(plugin, delayTicks, periodTicks);
    }

    public SchedulerTask runRegionTask(Plugin plugin, Location location) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runRegionTask(plugin, location) : new SpigotSchedulerRunnableImpl(this).runRegionTask(plugin, location);
    }

    public SchedulerTask runRegionTask(Plugin plugin, World world, int chunkX, int chunkZ) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runRegionTask(plugin, world, chunkX, chunkZ) : new SpigotSchedulerRunnableImpl(this).runRegionTask(plugin, world, chunkX, chunkZ);
    }

    public SchedulerTask runRegionTaskLater(Plugin plugin, Location location, long delayTicks) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runRegionTaskLater(plugin, location, delayTicks) : new SpigotSchedulerRunnableImpl(this).runRegionTaskLater(plugin, location, delayTicks);
    }

    public SchedulerTask runRegionTaskLater(Plugin plugin, World world, int chunkX, int chunkZ, long delayTicks) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runRegionTaskLater(plugin, world, chunkX, chunkZ, delayTicks) : new SpigotSchedulerRunnableImpl(this).runRegionTaskLater(plugin, world, chunkX, chunkZ, delayTicks);
    }

    public SchedulerTask runRegionTaskTimer(Plugin plugin, Location location, long delayTicks, long periodTicks) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runRegionTaskTimer(plugin, location, delayTicks, periodTicks) : new SpigotSchedulerRunnableImpl(this).runRegionTaskTimer(plugin, location, delayTicks, periodTicks);
    }

    public SchedulerTask runRegionTaskTimer(Plugin plugin, World world, int chunkX, int chunkZ, long delayTicks, long periodTicks) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runRegionTaskTimer(plugin, world, chunkX, chunkZ, delayTicks, periodTicks) : new SpigotSchedulerRunnableImpl(this).runRegionTaskTimer(plugin, world, chunkX, chunkZ, delayTicks, periodTicks);
    }

    public SchedulerTask runEntityTask(Plugin plugin, Entity entity, Runnable retired) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runEntityTask(plugin, entity, retired) : new SpigotSchedulerRunnableImpl(this).runEntityTask(plugin, entity, retired);
    }

    public SchedulerTask runEntityTaskLater(Plugin plugin, Entity entity, Runnable retired, long delayTicks) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runEntityTaskLater(plugin, entity, retired, delayTicks) : new SpigotSchedulerRunnableImpl(this).runEntityTaskLater(plugin, entity, retired, delayTicks);
    }

    public SchedulerTask runEntityTaskTimer(Plugin plugin, Entity entity, Runnable retired, long delayTicks, long periodTicks) {
        return isFolia() ? new FoliaSchedulerRunnableImpl(this).runEntityTaskTimer(plugin, entity, retired, delayTicks, periodTicks) : new SpigotSchedulerRunnableImpl(this).runEntityTaskTimer(plugin, entity, retired, delayTicks, periodTicks);
    }

    private static final class FoliaSchedulerRunnableImpl extends FoliaSchedulerRunnable {
        private final SchedulerRunnable delegate;

        private FoliaSchedulerRunnableImpl(SchedulerRunnable delegate) {
            this.delegate = delegate;
        }

        @Override
        protected SchedulerTask setupTask(SchedulerTask task) {
            delegate.setupTask(task);
            return task;
        }

        @Override
        public void run() {
            delegate.run();
        }
    }

    private static final class SpigotSchedulerRunnableImpl extends SpigotSchedulerRunnable {
        private final SchedulerRunnable delegate;

        private SpigotSchedulerRunnableImpl(SchedulerRunnable delegate) {
            this.delegate = delegate;
        }

        @Override
        protected SchedulerTask setupTask(SchedulerTask task) {
            delegate.setupTask(task);
            return task;
        }

        @Override
        public void run() {
            delegate.run();
        }
    }
}
