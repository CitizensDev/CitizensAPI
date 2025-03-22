package net.citizensnpcs.api.util.schedulers.runnables;

import net.citizensnpcs.api.util.schedulers.SchedulerRunnable;
import net.citizensnpcs.api.util.schedulers.SchedulerTask;
import net.citizensnpcs.api.util.schedulers.adapter.FoliaSchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public abstract class FoliaSchedulerRunnable extends SchedulerRunnable {

    @Override
    public SchedulerTask runTask(Plugin plugin) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getGlobalRegionScheduler().run(plugin, task -> run())));
    }

    @Override
    public SchedulerTask runTaskLater(Plugin plugin, long delayTicks) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> run(), Math.max(1, delayTicks))));
    }

    @Override
    public SchedulerTask runTaskTimer(Plugin plugin, long delayTicks, long periodTicks) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> run(), Math.max(1, delayTicks), Math.max(1, periodTicks))));
    }

    @Override
    public SchedulerTask runTaskAsynchronously(Plugin plugin) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getAsyncScheduler().runNow(plugin, task -> run())));
    }

    @Override
    public SchedulerTask runTaskLaterAsynchronously(Plugin plugin, long delayTicks) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getAsyncScheduler().runDelayed(plugin, task -> run(), Math.max(1, delayTicks) * 50L, TimeUnit.MILLISECONDS)));
    }

    @Override
    public SchedulerTask runTaskTimerAsynchronously(Plugin plugin, long delayTicks, long periodTicks) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> run(), Math.max(1, delayTicks) * 50L, Math.max(1, periodTicks) * 50L, TimeUnit.MILLISECONDS)));
    }

    @Override
    public SchedulerTask runRegionTask(Plugin plugin, Location location) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getRegionScheduler().run(plugin, location, task -> run())));
    }


    @Override
    public SchedulerTask runRegionTask(Plugin plugin, World world, int chunkX, int chunkZ) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, task -> run())));
    }

    @Override
    public SchedulerTask runRegionTaskLater(Plugin plugin, Location location, long delayTicks) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getRegionScheduler().runDelayed(plugin, location, task -> run(), Math.max(1, delayTicks))));
    }

    @Override
    public SchedulerTask runRegionTaskLater(Plugin plugin, World world, int chunkX, int chunkZ, long delayTicks) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getRegionScheduler().runDelayed(plugin, world, chunkX, chunkZ, task -> run(), Math.max(1, delayTicks))));
    }

    @Override
    public SchedulerTask runRegionTaskTimer(Plugin plugin, World world, int chunkX, int chunkZ, long delayTicks, long periodTicks) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getRegionScheduler().runAtFixedRate(plugin, world, chunkX, chunkZ, task -> run(), Math.max(1, delayTicks), Math.max(1, periodTicks))));
    }

    @Override
    public SchedulerTask runRegionTaskTimer(Plugin plugin, Location location, long delayTicks, long periodTicks) {
        return setupTask(new FoliaSchedulerTask(Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, task -> run(), Math.max(1, delayTicks), Math.max(1, periodTicks))));
    }

    @Override
    public SchedulerTask runEntity(Plugin plugin, Entity entity, Runnable retired) {
        return setupTask(new FoliaSchedulerTask(entity.getScheduler().run(plugin, task -> run(), retired)));
    }

    @Override
    public SchedulerTask runEntityTaskLater(Plugin plugin, Entity entity, Runnable retired, long delayTicks) {
        return setupTask(new FoliaSchedulerTask(entity.getScheduler().runDelayed(plugin, task -> run(), retired, Math.max(1, delayTicks))));
    }

    @Override
    public SchedulerTask runEntityTaskTimer(Plugin plugin, Entity entity, Runnable retired, long delayTicks, long periodTicks) {
        return setupTask(new FoliaSchedulerTask(entity.getScheduler().runAtFixedRate(plugin, task -> run(), retired, Math.max(1, delayTicks), Math.max(1, periodTicks))));
    }
}
