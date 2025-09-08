package net.citizensnpcs.api.astar.pathfinder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.IdentityHashMap;
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
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.google.common.collect.ImmutableList;

public class AsyncPathfinderService {
    private final ScheduledExecutorService evictionExecutor;
    private final Plugin plugin;
    private final Map<ChunkKey, CompletableFuture<ChunkSnapshot>> snapshotCache = new ConcurrentHashMap<>();
    private final Map<ChunkKey, Long> snapshotCacheExpiry = new ConcurrentHashMap<>();
    private final long ttlMillis;
    private final ForkJoinPool workerPool;

    public AsyncPathfinderService(Plugin plugin, int parallelism, long ttlMillis) {
        this.plugin = plugin;
        this.workerPool = new ForkJoinPool(Math.max(1, parallelism));
        this.ttlMillis = ttlMillis;
        if (ttlMillis > 0) {
            evictionExecutor = Executors.newSingleThreadScheduledExecutor();
            evictionExecutor.scheduleAtFixedRate(this::evictStaleChunks, ttlMillis, ttlMillis, TimeUnit.MILLISECONDS);
        } else {
            evictionExecutor = null;
        }
    }

    private void evictStaleChunks() {
        long now = System.currentTimeMillis();
        for (Map.Entry<ChunkKey, Long> e : snapshotCacheExpiry.entrySet()) {
            ChunkKey key = e.getKey();
            long last = e.getValue();
            if (now - last > ttlMillis) {
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

        if (WORLD_GET_CHUNK_AT_ASYNC != null) {
            CompletableFuture<Chunk> chunkGetter;
            try {
                chunkGetter = (CompletableFuture<Chunk>) WORLD_GET_CHUNK_AT_ASYNC.invoke(world, cx, cz, true, false);
            } catch (Throwable e) {
                future.completeExceptionally(e.getCause() != null ? e.getCause() : e);
                return future;
            }
            if (chunkGetter == null) {
                future.completeExceptionally(new IllegalStateException("getChunkAtAsync returned null"));
                return future;
            }
            chunkGetter.whenComplete((chunk, ex) -> {
                if (ex != null) {
                    future.completeExceptionally(ex);
                    return;
                }
                // on main thread
                try {
                    snapshotCacheExpiry.put(key, System.currentTimeMillis());
                    future.complete(chunk.getChunkSnapshot());
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    Chunk chunk = world.getChunkAt(cx, cz);
                    snapshotCacheExpiry.put(key, System.currentTimeMillis());
                    future.complete(chunk.getChunkSnapshot());
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        }
        return future;
    }

    /**
     * Returned future completes on the main thread
     */
    public CompletableFuture<Path> findPathAsync(PathRequest req) {
        // TODO: setup work currently happens on main thread. request queue could be made async

        Rect start = new Rect((req.from.getBlockX() >> 4) - req.prefetchRadius,
                (req.from.getBlockZ() >> 4) - req.prefetchRadius, (req.from.getBlockX() >> 4) + req.prefetchRadius,
                (req.from.getBlockZ() >> 4) + req.prefetchRadius);
        Rect end = new Rect((req.to.getBlockX() >> 4) - req.prefetchRadius,
                (req.to.getBlockZ() >> 4) - req.prefetchRadius, (req.to.getBlockX() >> 4) + req.prefetchRadius,
                (req.to.getBlockZ() >> 4) + req.prefetchRadius);

        List<Rect> rects;
        if (start.overlaps(end)) {
            Rect merged = new Rect(Math.min(start.minX, end.minX), Math.min(start.minZ, end.minZ),
                    Math.max(start.maxX, end.maxX), Math.max(start.maxZ, end.maxZ));
            rects = ImmutableList.of(merged);
        } else {
            rects = ImmutableList.of(start, end);
        }
        // create keys
        for (Rect rect : rects) {
            for (int cx = rect.minX; cx <= rect.maxX; cx++) {
                for (int cz = rect.minZ; cz <= rect.maxZ; cz++) {
                    snapshotCache.computeIfAbsent(new ChunkKey(req.world.getUID(), cx, cz),
                            k -> new CompletableFuture<>());
                }
            }
        }
        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = new CompletableFuture[rects.size()];
        for (int i = 0; i < rects.size(); i++) {
            futures[i] = prefetchRectangle(req.world, rects.get(i));
        }
        CompletableFuture<Path> workerFuture = CompletableFuture.allOf(futures).thenCompose(v -> CompletableFuture
                .supplyAsync(() -> runPathfinder(req, new SnapshotProvider(req.world)), workerPool));

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
                Bukkit.getScheduler().runTask(plugin, cb);
            }
        });
        return result;
    }

    private CompletableFuture<Void> prefetchIndividualChunks(World world, Rect rect) {
        List<CompletableFuture<Chunk>> chunkFutures = new ArrayList<>();
        Map<CompletableFuture<Chunk>, ChunkKey> mapping = new IdentityHashMap<>();

        for (int cx = rect.minX; cx <= rect.maxX; cx++) {
            for (int cz = rect.minZ; cz <= rect.maxZ; cz++) {
                ChunkKey key = new ChunkKey(world.getUID(), cx, cz);
                CompletableFuture<ChunkSnapshot> snap = snapshotCache.get(key);
                if (snap != null && snap.isDone())
                    continue;
                try {
                    CompletableFuture<Chunk> chunkFuture = (CompletableFuture<Chunk>) WORLD_GET_CHUNK_AT_ASYNC
                            .invoke(world, cx, cz, true, false);
                    if (chunkFuture == null) {
                        if (snap != null && !snap.isDone())
                            snap.completeExceptionally(new IllegalStateException("getChunkAtAsync returned null"));
                        continue;
                    }
                    chunkFutures.add(chunkFuture);
                    mapping.put(chunkFuture, key);
                } catch (Throwable e) {
                    if (snap != null && !snap.isDone())
                        snap.completeExceptionally(e.getCause() != null ? e.getCause() : e);
                }
            }
        }
        if (chunkFutures.isEmpty())
            return CompletableFuture.completedFuture(null);

        CompletableFuture<Void> all = CompletableFuture
                .allOf(chunkFutures.toArray(new CompletableFuture[chunkFutures.size()]));
        CompletableFuture<Void> done = new CompletableFuture<>();
        all.whenComplete((v, ex) -> {
            for (CompletableFuture<Chunk> completed : chunkFutures) {
                ChunkKey key = mapping.get(completed);
                if (key == null)
                    continue;
                CompletableFuture<ChunkSnapshot> pending = snapshotCache.get(key);
                if (pending == null || pending.isDone())
                    continue;
                // TODO: it's possible that chunk loading takes some ticks and therefore this will double-load chunks.

                try {
                    Chunk chunk = completed.join();
                    snapshotCacheExpiry.put(key, System.currentTimeMillis());
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
        if (WORLD_GET_CHUNKS_AT_ASYNC != null) {
            CompletableFuture<Void> result = new CompletableFuture<>();
            Runnable callback = () -> {
                for (int cx = rect.minX; cx <= rect.maxX; cx++) {
                    for (int cz = rect.minZ; cz <= rect.maxZ; cz++) {
                        ChunkKey key = new ChunkKey(world.getUID(), cx, cz);
                        CompletableFuture<ChunkSnapshot> pending = snapshotCache.get(key);
                        if (pending == null || pending.isDone())
                            continue;
                        Chunk chunk = world.getChunkAt(cx, cz);
                        snapshotCacheExpiry.put(key, System.currentTimeMillis());
                        pending.complete(chunk.getChunkSnapshot());
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
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (int cx = rect.minX; cx <= rect.maxX; cx++) {
                    for (int cz = rect.minZ; cz <= rect.maxZ; cz++) {
                        ChunkKey key = new ChunkKey(world.getUID(), cx, cz);
                        CompletableFuture<ChunkSnapshot> pending = snapshotCache.get(key);
                        if (pending == null || pending.isDone())
                            continue;
                        Chunk chunk = world.getChunkAt(cx, cz);
                        snapshotCacheExpiry.put(key, System.currentTimeMillis());
                        pending.complete(chunk.getChunkSnapshot());
                    }
                }
                result.complete(null);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    private Path runPathfinder(PathRequest req, SnapshotProvider provider) {
        throw new UnsupportedOperationException("");
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

    public static final class PathRequest {
        private final Vector from;
        private final int prefetchRadius;
        private final Vector to;
        private final World world;

        public PathRequest(World world, Vector from, Vector to, int prefetchRadius) {
            this.world = world;
            this.from = from;
            this.to = to;
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
    }

    public class SnapshotProvider {
        private final World world;

        SnapshotProvider(World world) {
            this.world = world;
        }

        public ChunkSnapshot get(int cx, int cz) {
            CompletableFuture<ChunkSnapshot> chunk = getAsync(cx, cz);
            if (Thread.currentThread() instanceof ForkJoinWorkerThread) {
                try {
                    ForkJoinPool.managedBlock(new CompletableFutureManagedBlocker<>(chunk));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CompletionException(e);
                }
                return chunk.join();
            } else {
                return chunk.join();
            }
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
