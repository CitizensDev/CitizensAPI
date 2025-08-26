package net.citizensnpcs.api.util.schedulers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * An abstraction layer for scheduling tasks across different Minecraft server implementations, such as Folia and
 * Spigot.
 * <p>
 * Allows seamless compatibility by abstracting scheduling methods.
 */
public interface SchedulerAdapter {
    /**
     * Executes a task specifically linked to an entity immediately. On Spigot, this defaults to the global thread.
     */
    SchedulerTask runEntityTask(Entity entity, Runnable runnable);

    /**
     * Executes a task specifically linked to an entity after a delay. On Spigot, this defaults to the global thread.
     *
     * @param delayTicks
     *            Delay before execution, measured in ticks.
     */
    SchedulerTask runEntityTaskLater(Entity entity, Runnable runnable, long delayTicks);

    /**
     * Executes a repeating task specifically linked to an entity. On Spigot, this defaults to the global thread.
     *
     * @param delayTicks
     *            Initial delay before the first execution (in ticks).
     * @param periodTicks
     *            Period between each subsequent execution (in ticks).
     */
    SchedulerTask runEntityTaskTimer(Entity entity, Runnable runnable, long delayTicks, long periodTicks);

    /**
     * Executes a task associated with a specific region or chunk. On Spigot, this defaults to the global thread.
     */
    SchedulerTask runRegionTask(Location location, Runnable runnable);

    /**
     * Executes a task associated with a specific region or chunk. On Spigot, this defaults to the global thread.
     */
    SchedulerTask runRegionTask(World world, int chunkX, int chunkZ, Runnable runnable);

    /**
     * Executes a task associated with a specific region or chunk after a delay. On Spigot, this defaults to the global
     * thread.
     *
     * @param delayTicks
     *            Delay before execution, measured in ticks.
     */
    SchedulerTask runRegionTaskLater(Location location, Runnable runnable, long delayTicks);

    /**
     * Executes a task associated with a specific region or chunk after a delay. On Spigot, this defaults to the global
     * thread.
     *
     * @param delayTicks
     *            Delay before execution, measured in ticks.
     */
    SchedulerTask runRegionTaskLater(World world, int chunkX, int chunkZ, Runnable runnable, long delayTicks);

    /**
     * Executes a repeating task associated with a specific region or chunk. On Spigot, this defaults to the global
     * thread.
     *
     * @param delayTicks
     *            Initial delay before the first execution (in ticks).
     * @param periodTicks
     *            Period between each subsequent execution (in ticks).
     */
    SchedulerTask runRegionTaskTimer(Location location, Runnable runnable, long delayTicks, long periodTicks);

    /**
     * Executes a repeating task associated with a specific region or chunk. On Spigot, this defaults to the global
     * thread.
     *
     * @param delayTicks
     *            Initial delay before the first execution (in ticks).
     * @param periodTicks
     *            Period between each subsequent execution (in ticks).
     */
    SchedulerTask runRegionTaskTimer(World world, int chunkX, int chunkZ, Runnable runnable, long delayTicks,
            long periodTicks);

    /**
     * Executes a task on the global server thread.
     */
    SchedulerTask runTask(Runnable runnable);

    /**
     * Executes a task asynchronously immediately, off the main server thread.
     */
    SchedulerTask runTaskAsynchronously(Runnable runnable);

    /**
     * Executes a task on the global server thread after a specified delay.
     *
     * @param delayTicks
     *            Delay before execution, measured in server ticks (1 tick = 50ms).
     */
    SchedulerTask runTaskLater(Runnable runnable, long delayTicks);

    /**
     * Executes an asynchronous task after a specified delay.
     *
     * @param delayTicks
     *            Delay before execution, measured in ticks.
     */
    SchedulerTask runTaskLaterAsynchronously(Runnable runnable, long delayTicks);

    /**
     * Executes a repeating task on the global server thread.
     *
     * @param delayTicks
     *            Initial delay before the first execution (in ticks).
     * @param periodTicks
     *            Period between each subsequent execution (in ticks).
     */
    SchedulerTask runTaskTimer(Runnable runnable, long delayTicks, long periodTicks);

    /**
     * Executes an asynchronous repeating task.
     *
     * @param delayTicks
     *            Initial delay before the first execution (in ticks).
     * @param periodTicks
     *            Period between each subsequent execution (in ticks).
     */
    SchedulerTask runTaskTimerAsynchronously(Runnable runnable, long delayTicks, long periodTicks);
}
