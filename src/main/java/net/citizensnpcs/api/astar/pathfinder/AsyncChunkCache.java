package net.citizensnpcs.api.astar.pathfinder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.ImmutableList;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.hpastar.HPAGraph;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.SpigotUtil;

public class AsyncChunkCache {
    private final ScheduledExecutorService evictionExecutor;
    private final Plugin plugin;
    private final Map<ChunkKey, CompletableFuture<ChunkSnapshot>> snapshotCache = new ConcurrentHashMap<>();
    private final Map<ChunkKey, Long> snapshotCacheExpiry = new ConcurrentHashMap<>();
    private final long ttlMillis;
    private final ForkJoinPool workerPool;

    public AsyncChunkCache(Plugin plugin, int workerThreads, long cacheTtlMillis) {
        this.plugin = plugin;
        this.workerPool = new ForkJoinPool(Math.min(Runtime.getRuntime().availableProcessors(), workerThreads));
        this.ttlMillis = cacheTtlMillis;
        if (cacheTtlMillis > 0) {
            evictionExecutor = Executors.newSingleThreadScheduledExecutor(
                    runnable -> new Thread(runnable, "Citizens Async Pathfinder Cache Eviction Thread"));
            evictionExecutor.scheduleAtFixedRate(this::evictStaleChunks, cacheTtlMillis, cacheTtlMillis,
                    TimeUnit.MILLISECONDS);
        } else {
            evictionExecutor = null;
        }
    }

    private CompletableFuture<Path> completeOnMainThread(CompletableFuture<Path> workerFuture) {
        CompletableFuture<Path> result = new CompletableFuture<>();
        workerFuture.whenComplete((res, ex) -> {
            Runnable cb = () -> {
                if (ex != null) {
                    result.completeExceptionally(ex);
                } else {
                    result.complete(res);
                }
            };
            if (Bukkit.isPrimaryThread()) {
                cb.run();
            } else {
                CitizensAPI.getScheduler().runTask(cb);
            }
        });
        return result;
    }

    private BlockSource createSnapshotBlockSource(SnapshotProvider provider, World world) {
        return new BlockSource() {
            @Override
            public BlockData getBlockDataAt(int x, int y, int z) {
                return provider.get(x >> 4, z >> 4).getBlockData(x & 15, y, z & 15);
            }

            @Override
            public BoundingBox getCollisionBox(int x, int y, int z) {
                return null;
            }

            @Override
            public Material getMaterialAt(int x, int y, int z) {
                return provider.get(x >> 4, z >> 4).getBlockType(x & 15, y, z & 15);
            }

            @Override
            public boolean isYWithinBounds(int y) {
                return SpigotUtil.checkYSafe(y, world);
            }
        };
    }

    private void evictStaleChunks() {
        long now = System.currentTimeMillis();
        for (Map.Entry<ChunkKey, Long> e : snapshotCacheExpiry.entrySet()) {
            ChunkKey key = e.getKey();
            long last = e.getValue();
            if (now > last) {
                CompletableFuture<ChunkSnapshot> cf = snapshotCache.get(key);
                if (cf != null && cf.isDone()) {
                    snapshotCache.remove(key, cf);
                    snapshotCacheExpiry.remove(key, last);
                }
            }
        }
    }

    // guaranteed to complete on main thread, but may throw exception on alternate thread
    private CompletableFuture<ChunkSnapshot> fetchChunkSnapshotAsync(World world, int cx, int cz) {
        ChunkKey key = new ChunkKey(world.getUID(), cx, cz);

        CompletableFuture<ChunkSnapshot> existing = snapshotCache.get(key);
        if (existing != null)
            return existing;

        CompletableFuture<ChunkSnapshot> future = new CompletableFuture<>();
        CompletableFuture<ChunkSnapshot> raced = snapshotCache.putIfAbsent(key, future);
        if (raced != null)
            return raced;
        Messaging.debug("AsyncChunkCache: Fetching chunk", world, cx, cz);

        if (WORLD_GET_CHUNK_AT_ASYNC != null) {
            CompletableFuture<Chunk> chunkGetter;
            try {
                chunkGetter = (CompletableFuture<Chunk>) WORLD_GET_CHUNK_AT_ASYNC.invoke(world, cx, cz, true, false);
            } catch (Throwable e) {
                future.completeExceptionally(e.getCause() != null ? e.getCause() : e);
                return future;
            }
            chunkGetter.whenComplete((chunk, ex) -> {
                if (ex != null) {
                    future.completeExceptionally(ex);
                    return;
                }
                // on main thread
                try {
                    snapshotCacheExpiry.put(key, System.currentTimeMillis() + ttlMillis);
                    future.complete(chunk.getChunkSnapshot());
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        } else {
            CitizensAPI.getScheduler().runRegionTask(world, cx, cz, () -> {
                try {
                    snapshotCacheExpiry.put(key, System.currentTimeMillis() + ttlMillis);
                    future.complete(world.getChunkAt(cx, cz).getChunkSnapshot());
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        }
        return future;
    }

    public CompletableFuture<Path> findHPAPathAsync(HPAPathRequest req) {
        CompletableFuture<Void>[] futures = prefetchRegions(req.from.getWorld(),
                getPrefetchRects(req.from, req.to, req.prefetchRadius));
        CompletableFuture<Path> workerFuture = CompletableFuture.allOf(futures).thenCompose(v -> CompletableFuture
                .supplyAsync(() -> runHPAPathfinder(req, new SnapshotProvider(req.from.getWorld())), workerPool));

        return completeOnMainThread(workerFuture);
    }

    /**
     * Returned future completes on the main thread
     */
    public CompletableFuture<Path> findPathAsync(PathRequest req) {
        // TODO: setup work currently happens on main thread. request queue could be made async

        CompletableFuture<Void>[] futures = prefetchRegions(req.from.getWorld(),
                getPrefetchRects(req.from, req.to, req.prefetchRadius));
        CompletableFuture<Path> workerFuture = CompletableFuture.allOf(futures).thenCompose(v -> CompletableFuture
                .supplyAsync(() -> runPathfinder(req, new SnapshotProvider(req.from.getWorld())), workerPool));

        return completeOnMainThread(workerFuture);
    }

    private List<Rect> getPrefetchRects(Location from, Location to, int prefetchRadius) {
        Rect start = new Rect((from.getBlockX() >> 4) - prefetchRadius, (from.getBlockZ() >> 4) - prefetchRadius,
                (from.getBlockX() >> 4) + prefetchRadius, (from.getBlockZ() >> 4) + prefetchRadius);
        Rect end = new Rect((to.getBlockX() >> 4) - prefetchRadius, (to.getBlockZ() >> 4) - prefetchRadius,
                (to.getBlockX() >> 4) + prefetchRadius, (to.getBlockZ() >> 4) + prefetchRadius);

        if (start.overlaps(end)) {
            Rect merged = new Rect(Math.min(start.minX, end.minX), Math.min(start.minZ, end.minZ),
                    Math.max(start.maxX, end.maxX), Math.max(start.maxZ, end.maxZ));
            return ImmutableList.of(merged);
        }
        return ImmutableList.of(start, end);
    }

    private CompletableFuture<Void> prefetchIndividualChunks(World world, Rect rect) {
        List<ChunkFutureRequest> chunkFutures = new ArrayList<>();

        for (int cx = rect.minX; cx <= rect.maxX; cx++) {
            for (int cz = rect.minZ; cz <= rect.maxZ; cz++) {
                ChunkKey key = new ChunkKey(world.getUID(), cx, cz);
                CompletableFuture<ChunkSnapshot> snap = snapshotCache.get(key);
                if (snap != null && snap.isDone())
                    continue;
                try {
                    CompletableFuture<Chunk> chunkFuture = (CompletableFuture<Chunk>) WORLD_GET_CHUNK_AT_ASYNC
                            .invoke(world, cx, cz, true, false);
                    chunkFutures.add(new ChunkFutureRequest(chunkFuture, key));
                } catch (Throwable e) {
                    if (snap != null && !snap.isDone())
                        snap.completeExceptionally(e.getCause() != null ? e.getCause() : e);
                }
            }
        }
        if (chunkFutures.isEmpty())
            return CompletableFuture.completedFuture(null);
        Messaging.debug("AsyncChunkCache: Fetching chunks", world, rect);

        CompletableFuture<Void> all = CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<Void> done = new CompletableFuture<>();
        all.whenComplete((v, ex) -> {
            for (ChunkFutureRequest request : chunkFutures) {
                CompletableFuture<ChunkSnapshot> pending = snapshotCache.get(request.key);
                if (pending == null || pending.isDone())
                    continue;
                // TODO: it's possible that chunk loading takes some ticks and therefore this will double-load chunks.

                try {
                    Chunk chunk = request.future.join();
                    snapshotCacheExpiry.put(request.key, System.currentTimeMillis() + ttlMillis);
                    pending.complete(chunk.getChunkSnapshot());
                } catch (CompletionException ce) {
                    Throwable cause = ce.getCause() != null ? ce.getCause() : ce;
                    pending.completeExceptionally(cause);
                } catch (Throwable t) {
                    pending.completeExceptionally(t);
                }
            }
            done.complete(null);
        });
        return done;

    }

    private CompletableFuture<Void> prefetchRectangle(World world, Rect rect) {
        Messaging.debug("AsyncChunkCache: Fetching chunk rectangle", world, rect);

        if (WORLD_GET_CHUNKS_AT_ASYNC != null) {
            CompletableFuture<Void> result = new CompletableFuture<>();
            Runnable callback = () -> {
                for (int cx = rect.minX; cx <= rect.maxX; cx++) {
                    for (int cz = rect.minZ; cz <= rect.maxZ; cz++) {
                        ChunkKey key = new ChunkKey(world.getUID(), cx, cz);
                        CompletableFuture<ChunkSnapshot> pending = snapshotCache.get(key);
                        if (pending == null || pending.isDone())
                            continue;
                        snapshotCacheExpiry.put(key, System.currentTimeMillis() + ttlMillis);
                        pending.complete(world.getChunkAt(cx, cz).getChunkSnapshot());
                    }
                }
                result.complete(null);
            };

            try {
                WORLD_GET_CHUNKS_AT_ASYNC.invoke(world, rect.minX, rect.minZ, rect.maxX, rect.maxZ, false, callback);
                return result;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (WORLD_GET_CHUNK_AT_ASYNC != null) {
            CompletableFuture<Void> result = new CompletableFuture<>();
            try {
                prefetchIndividualChunks(world, rect).whenComplete((v, ex) -> {
                    if (ex != null) {
                        result.completeExceptionally(ex);
                    } else {
                        result.complete(null);
                    }
                });
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
            return result;
        }
        CompletableFuture<Void> result = new CompletableFuture<>();
        CitizensAPI.getScheduler().runRegionTask(world, rect.minX, rect.maxZ, () -> {
            try {
                for (int cx = rect.minX; cx <= rect.maxX; cx++) {
                    for (int cz = rect.minZ; cz <= rect.maxZ; cz++) {
                        ChunkKey key = new ChunkKey(world.getUID(), cx, cz);
                        CompletableFuture<ChunkSnapshot> pending = snapshotCache.get(key);
                        if (pending == null || pending.isDone())
                            continue;
                        snapshotCacheExpiry.put(key, System.currentTimeMillis());
                        pending.complete(world.getChunkAt(cx, cz).getChunkSnapshot());
                    }
                }
                result.complete(null);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Void>[] prefetchRegions(World world, List<Rect> rects) {
        CompletableFuture<Void>[] futures = new CompletableFuture[rects.size()];
        for (int i = 0; i < rects.size(); i++) {
            Rect rect = rects.get(i);
            int chunkCount = 0;
            for (int cx = rect.minX; cx <= rect.maxX; cx++) {
                for (int cz = rect.minZ; cz <= rect.maxZ; cz++) {
                    CompletableFuture<ChunkSnapshot> old = snapshotCache
                            .putIfAbsent(new ChunkKey(world.getUID(), cx, cz), new CompletableFuture<ChunkSnapshot>());
                    if (old == null || !old.isDone()) {
                        chunkCount++;
                    }
                }
            }
            futures[i] = chunkCount == 0 ? CompletableFuture.completedFuture(null) : prefetchRectangle(world, rect);
        }
        return futures;
    }

    private Path runHPAPathfinder(HPAPathRequest req, SnapshotProvider provider) {
        HPAGraph graph = req.graph != null ? req.graph
                : new HPAGraph(createSnapshotBlockSource(provider, req.from.getWorld()), req.from.getBlockX(),
                        req.from.getBlockY(), req.from.getBlockZ());
        synchronized (graph) {
            graph.applyPendingPatches();
            graph.addClusters(req.from.getBlockX(), req.from.getBlockZ());
            graph.addClusters(req.to.getBlockX(), req.to.getBlockZ());
            Path path = (Path) graph.findPath(req.from, req.to);
            Messaging.debug("Async HPA returning path", req.from, "->", req.to);
            return path;
        }
    }

    private Path runPathfinder(PathRequest req, SnapshotProvider provider) {
        VectorGoal goal = new VectorGoal(req.to, (float) req.parameters.pathDistanceMargin());
        return AStarMachine.<VectorNode, Path> createWithVectorStorage().runFully(goal, new VectorNode(goal, req.from,
                createSnapshotBlockSource(provider, req.from.getWorld()), req.parameters));
    }

    public void shutdown() {
        try {
            workerPool.shutdownNow();
        } catch (Throwable ignored) {
        }
        try {
            if (evictionExecutor != null) {
                evictionExecutor.shutdownNow();
            }
        } catch (Throwable ignored) {
        }
        snapshotCache.clear();
        snapshotCacheExpiry.clear();
    }

    private static class ChunkFutureRequest {
        private final CompletableFuture<Chunk> future;
        private final ChunkKey key;

        public ChunkFutureRequest(CompletableFuture<Chunk> future, ChunkKey key) {
            this.future = future;
            this.key = key;
        }
    }

    private static class ChunkKey {
        public final int cx, cz;
        public final UUID uuid;

        public ChunkKey(UUID uuid, int cx, int cz) {
            this.uuid = uuid;
            this.cx = cx;
            this.cz = cz;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ChunkKey))
                return false;
            ChunkKey k = (ChunkKey) o;
            return cx == k.cx && cz == k.cz && Objects.equals(uuid, k.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, cx, cz);
        }

        @Override
        public String toString() {
            return uuid + "@" + cx + "," + cz;
        }
    }

    private static class CompletableFutureManagedBlocker<T> implements ForkJoinPool.ManagedBlocker {
        private final CompletableFuture<T> cf;

        CompletableFutureManagedBlocker(CompletableFuture<T> cf) {
            this.cf = cf;
        }

        @Override
        public boolean block() throws InterruptedException {
            try {
                cf.join();
            } catch (CompletionException ignored) {
            }
            return true;
        }

        @Override
        public boolean isReleasable() {
            return cf.isDone();
        }
    }

    public static final class HPAPathRequest {
        private final Location from;
        private final HPAGraph graph;
        private final int prefetchRadius;
        private final Location to;

        public HPAPathRequest(HPAGraph graph, Location from, Location to, int prefetchRadius) {
            this.graph = graph;
            this.from = from;
            this.to = to;
            this.prefetchRadius = prefetchRadius;
        }

        public HPAPathRequest(Location from, Location to, int prefetchRadius) {
            this(null, from, to, prefetchRadius);
        }
    }

    public static final class PathRequest {
        private final Location from;
        private final NavigatorParameters parameters;
        private final int prefetchRadius;
        private final Location to;

        public PathRequest(Location from, Location to, int prefetchRadius, NavigatorParameters parameters) {
            this.from = from;
            this.to = to;
            this.parameters = parameters;
            this.prefetchRadius = prefetchRadius;
        }
    }

    private static class Rect {
        final int minX, minZ, maxX, maxZ;

        Rect(int minX, int minZ, int maxX, int maxZ) {
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
        }

        private boolean overlaps(Rect b) {
            return !(maxX < b.minX || b.maxX < minX || maxZ < b.minZ || b.maxZ < minZ);
        }

        @Override
        public String toString() {
            return "[minX=" + minX + ", minZ=" + minZ + ", maxX=" + maxX + ", maxZ=" + maxZ + "]";
        }
    }

    private class SnapshotProvider {
        private final World world;

        SnapshotProvider(World world) {
            this.world = world;
        }

        public ChunkSnapshot get(int cx, int cz) {
            CompletableFuture<ChunkSnapshot> chunk = getAsync(cx, cz);
            if (!chunk.isDone() && Thread.currentThread() instanceof ForkJoinWorkerThread) {
                try {
                    ForkJoinPool.managedBlock(new CompletableFutureManagedBlocker<>(chunk));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CompletionException(e);
                }
            }
            return chunk.join();
        }

        public CompletableFuture<ChunkSnapshot> getAsync(int cx, int cz) {
            return fetchChunkSnapshotAsync(world, cx, cz);
        }
    }

    private static MethodHandle WORLD_GET_CHUNK_AT_ASYNC;
    private static MethodHandle WORLD_GET_CHUNKS_AT_ASYNC;
    static {
        try {
            WORLD_GET_CHUNKS_AT_ASYNC = MethodHandles.lookup().unreflect(World.class.getMethod("getChunksAtAsync",
                    int.class, int.class, int.class, int.class, boolean.class, Runnable.class));
        } catch (Throwable ignored) {
        }
        try {
            WORLD_GET_CHUNK_AT_ASYNC = MethodHandles.lookup().unreflect(
                    World.class.getMethod("getChunkAtAsync", int.class, int.class, boolean.class, boolean.class));
        } catch (Throwable ignored) {
        }
    }
}
