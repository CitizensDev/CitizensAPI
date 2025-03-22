package net.citizensnpcs.api.util.schedulers.adapter;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.citizensnpcs.api.util.schedulers.SchedulerAdapter;
import net.citizensnpcs.api.util.schedulers.SchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class FoliaScheduler implements SchedulerAdapter {

    private final Plugin plugin;
    private final GlobalRegionScheduler globalScheduler;
    private final AsyncScheduler asyncScheduler;
    private final RegionScheduler regionScheduler;

    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
        globalScheduler = Bukkit.getGlobalRegionScheduler();
        asyncScheduler = Bukkit.getAsyncScheduler();
        regionScheduler = Bukkit.getRegionScheduler();
    }

    private long ticksToMillis(long ticks) {
        return ticks * 50L;
    }

    private SchedulerTask wrap(ScheduledTask task) {
        return new FoliaSchedulerTask(task);
    }

    @Override
    public SchedulerTask runGlobal(Runnable runnable) {
        ScheduledTask task = globalScheduler.run(plugin, scheduledTask -> runnable.run());
        return wrap(task);
    }

    @Override
    public SchedulerTask runGlobalLater(Runnable runnable, long delayTicks) {
        ScheduledTask task = globalScheduler.runDelayed(plugin, scheduledTask -> runnable.run(), Math.max(1, delayTicks));
        return wrap(task);
    }

    @Override
    public SchedulerTask runGlobalTimer(Runnable runnable, long delayTicks, long periodTicks) {
        ScheduledTask task = globalScheduler.runAtFixedRate(plugin, scheduledTask -> runnable.run(), Math.max(1, delayTicks), Math.max(1, periodTicks));
        return wrap(task);
    }

    @Override
    public SchedulerTask runRegion(Plugin plugin, Location location, Runnable runnable) {
        ScheduledTask task = regionScheduler.run(plugin, location, scheduledTask -> runnable.run());
        return wrap(task);
    }

    @Override
    public SchedulerTask runRegionLater(Plugin plugin, Location location, Runnable runnable, long delayTicks) {
        ScheduledTask task = regionScheduler.runDelayed(plugin, location, scheduledTask -> runnable.run(), Math.max(1, delayTicks));
        return wrap(task);
    }

    @Override
    public SchedulerTask runRegionTimer(Plugin plugin, Location location, Runnable runnable, long delayTicks, long periodTicks) {
        ScheduledTask task = regionScheduler.runAtFixedRate(plugin, location, scheduledTask -> runnable.run(), Math.max(1, delayTicks), Math.max(1, periodTicks));
        return wrap(task);
    }

    @Override
    public SchedulerTask runRegion(Plugin plugin, World world, int chunkX, int chunkZ, Runnable runnable) {
        ScheduledTask task = regionScheduler.run(plugin, world, chunkX, chunkZ, scheduledTask -> runnable.run());
        return wrap(task);
    }

    @Override
    public SchedulerTask runRegionLater(Plugin plugin, World world, int chunkX, int chunkZ, Runnable runnable, long delayTicks) {
        ScheduledTask task = regionScheduler.runDelayed(plugin, world, chunkX, chunkZ, scheduledTask -> runnable.run(), Math.max(1, delayTicks));
        return wrap(task);
    }

    @Override
    public SchedulerTask runRegionTimer(Plugin plugin, World world, int chunkX, int chunkZ, Runnable runnable, long delayTicks, long periodTicks) {
        ScheduledTask task = regionScheduler.runAtFixedRate(plugin, world, chunkX, chunkZ, scheduledTask -> runnable.run(), Math.max(1, delayTicks), Math.max(1, periodTicks));
        return wrap(task);
    }

    @Override
    public SchedulerTask runEntity(Entity entity, Runnable runnable) {
        ScheduledTask task = entity.getScheduler().run(plugin, scheduledTask -> runnable.run(), null);
        return wrap(task);
    }

    @Override
    public SchedulerTask runEntityLater(Entity entity, Runnable runnable, long delayTicks) {
        ScheduledTask task = entity.getScheduler().runDelayed(plugin, scheduledTask -> runnable.run(), null, Math.max(1, delayTicks));
        return wrap(task);
    }

    @Override
    public SchedulerTask runEntityTimer(Entity entity, Runnable runnable, long delayTicks, long periodTicks) {
        ScheduledTask task = entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> runnable.run(), null, Math.max(1, delayTicks), Math.max(1, periodTicks));
        return wrap(task);
    }

    @Override
    public SchedulerTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        ScheduledTask task = asyncScheduler.runNow(plugin, scheduledTask -> runnable.run());
        return wrap(task);
    }

    @Override
    public SchedulerTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delayTicks) {
        ScheduledTask task = asyncScheduler.runDelayed(plugin, scheduledTask -> runnable.run(), ticksToMillis(Math.max(1, delayTicks)), TimeUnit.MILLISECONDS);
        return wrap(task);
    }

    @Override
    public SchedulerTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        ScheduledTask task = asyncScheduler.runAtFixedRate(plugin, scheduledTask -> runnable.run(), ticksToMillis(Math.max(1, delayTicks)), ticksToMillis(Math.max(1, periodTicks)), TimeUnit.MILLISECONDS);
        return wrap(task);
    }
}
