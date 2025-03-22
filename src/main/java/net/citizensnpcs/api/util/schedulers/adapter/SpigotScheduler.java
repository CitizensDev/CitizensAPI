package net.citizensnpcs.api.util.schedulers.adapter;

import net.citizensnpcs.api.util.schedulers.SchedulerAdapter;
import net.citizensnpcs.api.util.schedulers.SchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class SpigotScheduler implements SchedulerAdapter {

    private final Plugin plugin;

    public SpigotScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    private SchedulerTask wrap(BukkitTask task) {
        return new SpigotSchedulerTask(task);
    }

    @Override
    public SchedulerTask runGlobal(Runnable runnable) {
        return wrap(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    @Override
    public SchedulerTask runGlobalLater(Runnable runnable, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks));
    }

    @Override
    public SchedulerTask runGlobalTimer(Runnable runnable, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks));
    }

    @Override
    public SchedulerTask runRegion(Location location, Runnable runnable) {
        return runGlobal(runnable);
    }

    @Override
    public SchedulerTask runRegionLater(Location location, Runnable runnable, long delayTicks) {
        return runGlobalLater(runnable, delayTicks);
    }

    @Override
    public SchedulerTask runRegionTimer(Location location, Runnable runnable, long delayTicks, long periodTicks) {
        return runGlobalTimer(runnable, delayTicks, periodTicks);
    }

    @Override
    public SchedulerTask runRegion(World world, int chunkX, int chunkZ, Runnable runnable) {
        return runGlobal(runnable);
    }

    @Override
    public SchedulerTask runRegionLater(World world, int chunkX, int chunkZ, Runnable runnable, long delayTicks) {
        return runGlobalLater(runnable, delayTicks);
    }

    @Override
    public SchedulerTask runRegionTimer(World world, int chunkX, int chunkZ, Runnable runnable, long delayTicks, long periodTicks) {
        return runGlobalTimer(runnable, delayTicks, periodTicks);
    }

    @Override
    public SchedulerTask runEntity(Entity entity, Runnable runnable) {
        return runGlobal(runnable);
    }

    @Override
    public SchedulerTask runEntityLater(Entity entity, Runnable runnable, long delayTicks) {
        return runGlobalLater(runnable, delayTicks);
    }

    @Override
    public SchedulerTask runEntityTimer(Entity entity, Runnable runnable, long delayTicks, long periodTicks) {
        return runGlobalTimer(runnable, delayTicks, periodTicks);
    }

    @Override
    public SchedulerTask runTaskAsynchronously(Runnable runnable) {
        return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    @Override
    public SchedulerTask runTaskLaterAsynchronously(Runnable runnable, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks));
    }

    @Override
    public SchedulerTask runTaskTimerAsynchronously(Runnable runnable, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks));
    }
}
