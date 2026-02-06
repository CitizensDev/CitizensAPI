package net.citizensnpcs.api.hpastar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import ch.ethz.globis.phtree.PhTreeSolid;
import ch.ethz.globis.phtree.PhTreeSolid.PhQueryS;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.hpastar.HPACluster.Direction;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;

public class HPAGraph {
    private final BlockSource blockSource;
    public List<List<HPACluster>> clusters = new ArrayList<>();
    private final int cx, cy, cz;
    private final LongOpenHashSet dirtyRegionKeys = new LongOpenHashSet();
    private final LongOpenHashSet loadedRegionKeys = new LongOpenHashSet();
    private final List<PhTreeSolid<HPACluster>> phtrees = new ArrayList<>();

    public HPAGraph(BlockSource blockSource, int cx, int cy, int cz) {
        this.blockSource = blockSource;
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;

        while (clusters.size() <= MAX_DEPTH) {
            clusters.add(new ArrayList<>());
            if (clusters.size() != phtrees.size()) {
                phtrees.add(PhTreeSolid.create(3));
            }
        }
    }

    public void addClusters(int x, int z) {
        int regionX = Math.floorDiv(x - cx, MAX_CLUSTER_SIZE);
        int regionZ = Math.floorDiv(z - cz, MAX_CLUSTER_SIZE);
        long regionKey = packRegionKey(regionX, regionZ);
        if (loadedRegionKeys.contains(regionKey)) {
            Messaging.debug("Clusters already exist for region:", regionX, regionZ);
            return;
        }
        int baseX = regionX * MAX_CLUSTER_SIZE + cx;
        int baseZ = regionZ * MAX_CLUSTER_SIZE + cz;

        if (phtrees.size() == 0) {
            phtrees.add(PhTreeSolid.create(3));
        }
        PhTreeSolid<HPACluster> baseLevel = phtrees.get(0);
        Messaging.debug("Building clusters for:", baseX, baseZ);
        List<HPACluster> delta = new ArrayList<>();

        // build clusters
        int clusterSize = BASE_CLUSTER_SIZE;
        int clusterHeight = BASE_CLUSTER_HEIGHT;
        for (int y = 0; y <= MAX_WORLD_Y; y += clusterHeight) {
            for (int ci = 0; ci < MAX_CLUSTER_SIZE; ci += clusterSize) {
                for (int cj = 0; cj < MAX_CLUSTER_SIZE; cj += clusterSize) {
                    HPACluster cluster = new HPACluster(this, 0, clusterSize, clusterHeight, baseX + ci, y, baseZ + cj);
                    if (!cluster.hasWalkableNodes())
                        continue;

                    delta.add(cluster);
                    baseLevel.put(new long[] { cluster.clusterX, cluster.clusterY, cluster.clusterZ },
                            new long[] { cluster.clusterX + clusterSize, cluster.clusterY + clusterHeight,
                                    cluster.clusterZ + clusterSize },
                            cluster);
                    Messaging.debug(cluster);
                }
            }
        }
        Set<HPACluster> deltaSet = new HashSet<>(delta);
        for (HPACluster cluster : delta) {
            PhQueryS<HPACluster> q = baseLevel.queryIntersect(
                    new long[] { cluster.clusterX - clusterSize, cluster.clusterY - clusterHeight,
                            cluster.clusterZ - clusterSize },
                    new long[] { cluster.clusterX + clusterSize, cluster.clusterY + clusterHeight,
                            cluster.clusterZ + clusterSize });
            while (q.hasNext()) {
                HPACluster neighbour = q.nextValue();
                if (neighbour == cluster)
                    continue;
                // New clusters must always connect to pre-existing clusters regardless of coordinate ordering.
                if (deltaSet.contains(neighbour) && !shouldConnectPair(cluster, neighbour))
                    continue;

                int dx = neighbour.clusterX - cluster.clusterX;
                int dy = neighbour.clusterY - cluster.clusterY;
                int dz = neighbour.clusterZ - cluster.clusterZ;

                int nonZeroCount = (dx != 0 ? 1 : 0) + (dy != 0 ? 1 : 0) + (dz != 0 ? 1 : 0);
                if (nonZeroCount == 1) {
                    Direction direction = null;
                    if (dx > 0) {
                        direction = Direction.EAST;
                    } else if (dx < 0) {
                        direction = Direction.WEST;
                    } else if (dz > 0) {
                        direction = Direction.NORTH;
                    } else if (dz < 0) {
                        direction = Direction.SOUTH;
                    } else if (dy > 0) {
                        direction = Direction.UP;
                    } else if (dy < 0) {
                        direction = Direction.DOWN;
                    }
                    if (direction == null) {
                        continue;
                    }
                    cluster.connect(neighbour, direction);
                    Messaging.debug("CONNECTED", cluster, neighbour);
                } else if (nonZeroCount == 2 && dy == 0) {
                    cluster.connectDiagonal(neighbour, Integer.signum(dx), Integer.signum(dz), DIAGONAL_WEIGHT);
                    Messaging.debug("CONNECTED DIAGONAL", cluster, neighbour);
                }
            }
        }
        for (HPACluster cluster : delta) {
            cluster.connectIntra();
        }
        clusters.get(0).addAll(delta);
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            delta = new ArrayList<>();
            clusterSize = BASE_CLUSTER_SIZE << depth;
            clusterHeight = BASE_CLUSTER_HEIGHT << depth;

            for (int y = 0; y <= MAX_WORLD_Y; y += clusterHeight) {
                for (int ci = 0; ci < MAX_CLUSTER_SIZE; ci += clusterSize) {
                    for (int cj = 0; cj < MAX_CLUSTER_SIZE; cj += clusterSize) {
                        HPACluster cluster = new HPACluster(this, depth, clusterSize, clusterHeight, baseX + ci, y,
                                baseZ + cj);
                        List<HPACluster> parentClusters = Lists.newArrayList(phtrees.get(depth - 1).queryInclude(
                                new long[] { cluster.clusterX, cluster.clusterY, cluster.clusterZ },
                                new long[] { cluster.clusterX + clusterSize, cluster.clusterY + clusterHeight,
                                        cluster.clusterZ + clusterSize }));
                        if (parentClusters.size() == 0)
                            continue;

                        cluster.buildFrom(parentClusters);
                        phtrees.get(depth).put(new long[] { cluster.clusterX, cluster.clusterY, cluster.clusterZ },
                                new long[] { cluster.clusterX + clusterSize, cluster.clusterY + clusterHeight,
                                        cluster.clusterZ + clusterSize },
                                cluster);
                        Messaging.debug(cluster);
                        delta.add(cluster);
                    }
                }
            }
            clusters.get(depth).addAll(delta);
        }
        loadedRegionKeys.add(regionKey);
    }

    public synchronized void applyPendingPatches() {
        if (dirtyRegionKeys.isEmpty())
            return;

        LongOpenHashSet dirty = new LongOpenHashSet(loadedRegionKeys);
        dirty.addAll(dirtyRegionKeys);
        dirtyRegionKeys.clear();
        reloadRegions(dirty);
    }

    private void clearGraphStorage() {
        for (int depth = 0; depth <= MAX_DEPTH; depth++) {
            clusters.set(depth, new ArrayList<>());
            phtrees.set(depth, PhTreeSolid.create(3));
        }
        loadedRegionKeys.clear();
    }

    public Plan findPath(Location start, Location goal) {
        List<HPACluster> clustersToClean = new ArrayList<>();
        HPAGraphNode startNode = new HPAGraphNode(start.getBlockX(), start.getBlockY(), start.getBlockZ()),
                goalNode = new HPAGraphNode(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ());
        HPACluster startCluster = getClusterAt(0, startNode.x, startNode.y, startNode.z);
        HPACluster goalCluster = getClusterAt(0, goalNode.x, goalNode.y, goalNode.z);
        if (startCluster == null || goalCluster == null) {
            Path path = new Path(Collections.emptyList());
            Messaging.debug("HPA returning empty path", start, "->", goal, "(missing cluster)");
            return path;
        }
        startCluster.insert(startNode);
        clustersToClean.add(startCluster);
        if (goalCluster != startCluster) {
            goalCluster.insert(goalNode);
            clustersToClean.add(goalCluster);
        } else if (!goalNode.equals(startNode)) {
            startCluster.insert(goalNode);
        }
        AStarSolution sln = pathfind(startNode, goalNode, 0);
        Messaging.debug("HPA path", start, "->", goal, "@", sln.cost);
        for (HPACluster cluster : clustersToClean) {
            cluster.remove(startNode, goalNode);
        }
        Path path = new LazyHPAPath(sln, this);
        Messaging.debug("HPA returning path", start, "->", goal, "@", sln.cost);
        return path;
    }

    private HPACluster getClusterAt(int depth, int x, int y, int z) {
        PhQueryS<HPACluster> q = phtrees.get(depth).queryIntersect(new long[] { x, y, z }, new long[] { x, y, z });
        return q.hasNext() ? q.next() : null;
    }

    public void invalidateRegion(int x, int z) {
        LongOpenHashSet regionsToReload = new LongOpenHashSet(loadedRegionKeys);
        regionsToReload.add(regionKeyFromBlock(x, z));
        reloadRegions(regionsToReload);
    }

    public synchronized void markDirtyBlock(int x, int z) {
        dirtyRegionKeys.add(regionKeyFromBlock(x, z));
    }

    public synchronized void markDirtyChunk(int chunkX, int chunkZ) {
        markDirtyBlock(chunkX << 4, chunkZ << 4);
    }

    AStarSolution pathfind(HPAGraphNode start, HPAGraphNode dest, int level) {
        if (start.equals(dest))
            return new AStarSolution(Lists.newArrayList(new HPAGraphAStarNode(start, null)), 0);
        Long2FloatOpenHashMap open = new Long2FloatOpenHashMap();
        Long2FloatOpenHashMap closed = new Long2FloatOpenHashMap();
        open.defaultReturnValue(Float.POSITIVE_INFINITY);
        closed.defaultReturnValue(Float.POSITIVE_INFINITY);
        Queue<HPAGraphAStarNode> frontier = new PriorityQueue<>();
        HPAGraphAStarNode startNode = new HPAGraphAStarNode(start, null);
        frontier.add(startNode);
        open.put(packPosition(start.x, start.y, start.z), startNode.g);
        while (!frontier.isEmpty()) {
            HPAGraphAStarNode node = frontier.poll();
            long nodeKey = packPosition(node.node.x, node.node.y, node.node.z);
            float bestOpen = open.get(nodeKey);
            if (!Float.isFinite(bestOpen) || node.g > bestOpen)
                continue;

            List<HPAGraphEdge> edges = node.node.getEdges(level);
            if (start != node.node) {
                closed.put(nodeKey, node.g);
            }
            open.remove(nodeKey);
            for (HPAGraphEdge edge : edges) {
                long neighbourKey = packPosition(edge.to.x, edge.to.y, edge.to.z);
                float tentativeG = node.g + edge.weight;
                float closedG = closed.get(neighbourKey);
                if (tentativeG >= closedG)
                    continue;

                if (Float.isFinite(closedG)) {
                    closed.remove(neighbourKey);
                }
                if (tentativeG >= open.get(neighbourKey))
                    continue;

                HPAGraphAStarNode neighbour = new HPAGraphAStarNode(edge.to, edge);
                neighbour.parent = node;
                neighbour.g = tentativeG;
                neighbour.h = euclidean(edge.to.x, edge.to.y, edge.to.z, dest.x, dest.y, dest.z);
                if (edge.to.equals(dest))
                    return new AStarSolution(neighbour.reconstructSolution(), neighbour.g);
                open.put(neighbourKey, neighbour.g);
                frontier.add(neighbour);
            }
        }
        return new AStarSolution(null, Float.POSITIVE_INFINITY);
    }

    private long regionKeyFromBlock(int x, int z) {
        return packRegionKey(Math.floorDiv(x - cx, MAX_CLUSTER_SIZE), Math.floorDiv(z - cz, MAX_CLUSTER_SIZE));
    }

    private void reloadRegions(LongOpenHashSet regionKeys) {
        clearGraphStorage();
        for (LongIterator it = regionKeys.iterator(); it.hasNext();) {
            long key = it.nextLong();
            addClusters((int) (key >> 32) * MAX_CLUSTER_SIZE + cx, ((int) key) * MAX_CLUSTER_SIZE + cz);
        }
    }

    public boolean walkable(int x, int y, int z) {
        if (!blockSource.isYWithinBounds(y - 1) || !blockSource.isYWithinBounds(y)
                || !blockSource.isYWithinBounds(y + 1))
            return false;
        return MinecraftBlockExaminer.canStandIn(blockSource.getMaterialAt(x, y, z))
                && MinecraftBlockExaminer.canStandIn(blockSource.getMaterialAt(x, y + 1, z))
                && MinecraftBlockExaminer.canStandOn(blockSource.getMaterialAt(x, y - 1, z));
    }

    private static class LazyHPAPath extends Path {
        private int abstractIndex = 1;
        private final List<HPAGraphAStarNode> clusterPath;
        private final List<Vector> concretePath = new ArrayList<>();
        private final HPAGraph graph;
        private int index = 0;

        private LazyHPAPath(AStarSolution solution, HPAGraph graph) {
            super(Collections.emptyList());
            this.graph = graph;
            this.clusterPath = solution.path == null ? Collections.emptyList() : solution.path;
            if (!clusterPath.isEmpty()) {
                concretePath.add(clusterPath.get(0).node.toVector());
            }
        }

        private void appendUnique(Vector vector) {
            Vector last = concretePath.get(concretePath.size() - 1);
            if (!last.equals(vector)) {
                concretePath.add(vector);
            }
        }

        private void concretize() {
            while (abstractIndex < clusterPath.size()) {
                concretizeNextSegment();
            }
        }

        private void concretizeNextSegment() {
            HPAGraphAStarNode current = clusterPath.get(abstractIndex++);
            HPAGraphEdge edge = current.getEdge();
            if (edge != null && edge.type == HPAGraphEdge.EdgeType.INTRA) {
                for (Vector vector : intraPathfind(edge.from, edge.to)) {
                    appendUnique(vector);
                }
            } else {
                appendUnique(current.node.toVector());
            }
        }

        private void concretizeToIndex(int targetIndex) {
            while (concretePath.size() <= targetIndex && abstractIndex < clusterPath.size()) {
                concretizeNextSegment();
            }
        }

        @Override
        public List<Block> getBlocks(World world) {
            concretize();
            return concretePath.stream().map(v -> world.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ()))
                    .collect(Collectors.toList());
        }

        @Override
        public Vector getCurrentVector() {
            concretizeToIndex(index);
            return concretePath.get(index);
        }

        @Override
        public Iterable<Vector> getPath() {
            concretize();
            return new ArrayList<>(concretePath);
        }

        private List<Vector> intraPathfind(HPAGraphNode from, HPAGraphNode to) {
            if (from.equals(to))
                return ImmutableList.of(from.toVector());

            HPACluster cluster = graph.getClusterAt(0, from.x, from.y, from.z);
            if (cluster == null || !cluster.containsPoint(to.x, to.y, to.z)) {
                cluster = graph.getClusterAt(0, to.x, to.y, to.z);
            }
            if (cluster == null || !cluster.containsPoint(from.x, from.y, from.z)
                    || !cluster.containsPoint(to.x, to.y, to.z))
                return ImmutableList.of(from.toVector(), to.toVector());

            Long2FloatOpenHashMap open = new Long2FloatOpenHashMap();
            Long2FloatOpenHashMap closed = new Long2FloatOpenHashMap();
            open.defaultReturnValue(Float.POSITIVE_INFINITY);
            closed.defaultReturnValue(Float.POSITIVE_INFINITY);
            Queue<LocalPathNode> frontier = new PriorityQueue<>();

            LocalPathNode startNode = new LocalPathNode(from.x, from.y, from.z, 0F,
                    euclidean(from.x, from.y, from.z, to.x, to.y, to.z), null);
            frontier.add(startNode);
            open.put(packPosition(from.x, from.y, from.z), 0F);

            while (!frontier.isEmpty()) {
                LocalPathNode node = frontier.poll();
                long nodeKey = packPosition(node.x, node.y, node.z);
                float bestOpen = open.get(nodeKey);
                if (!Float.isFinite(bestOpen) || node.g > bestOpen)
                    continue;

                if (node.x == to.x && node.y == to.y && node.z == to.z) {
                    List<Vector> path = new ArrayList<>();
                    while (node != null) {
                        path.add(new Vector(node.x, node.y, node.z));
                        node = node.parent;
                    }
                    Collections.reverse(path);
                    return path;
                }
                if (node.x != from.x || node.y != from.y || node.z != from.z) {
                    closed.put(nodeKey, node.g);
                }
                open.remove(nodeKey);
                for (int i = 0; i < INTRA_NEIGHBOUR_OFFSETS.length; i++) {
                    int nx = node.x + INTRA_NEIGHBOUR_OFFSETS[i][0];
                    int ny = node.y + INTRA_NEIGHBOUR_OFFSETS[i][1];
                    int nz = node.z + INTRA_NEIGHBOUR_OFFSETS[i][2];
                    if (!cluster.containsPoint(nx, ny, nz))
                        continue;

                    boolean isGoal = nx == to.x && ny == to.y && nz == to.z;
                    if (!isGoal && !graph.walkable(nx, ny, nz))
                        continue;

                    long neighbourKey = packPosition(nx, ny, nz);
                    float tentativeG = node.g + INTRA_NEIGHBOUR_COSTS[i];
                    float closedG = closed.get(neighbourKey);
                    if (tentativeG >= closedG)
                        continue;

                    if (Float.isFinite(closedG)) {
                        closed.remove(neighbourKey);
                    }
                    if (tentativeG >= open.get(neighbourKey))
                        continue;

                    LocalPathNode neighbour = new LocalPathNode(nx, ny, nz, tentativeG,
                            euclidean(nx, ny, nz, to.x, to.y, to.z), node);
                    open.put(neighbourKey, neighbour.g);
                    frontier.add(neighbour);
                }
            }
            return ImmutableList.of(from.toVector(), to.toVector());
        }

        @Override
        public boolean isComplete() {
            concretizeToIndex(index);
            return abstractIndex >= clusterPath.size() && index >= concretePath.size();
        }

        @Override
        public void run(NPC npc) {
        }

        @Override
        public String toString() {
            concretize();
            return concretePath.toString();
        }

        @Override
        public void update(Agent agent) {
            if (isComplete())
                return;
            index++;
        }
    }

    private static class LocalPathNode implements Comparable<LocalPathNode> {
        final float g;
        final float h;
        final LocalPathNode parent;
        final int x;
        final int y;
        final int z;

        private LocalPathNode(int x, int y, int z, float g, float h, LocalPathNode parent) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        @Override
        public int compareTo(LocalPathNode other) {
            return Float.compare(g + h, other.g + other.h);
        }
    }

    private static float euclidean(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        int dz = z1 - z2;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static long packPosition(int x, int y, int z) {
        return (x & 0x3FFFFFFL) << 34 | (z & 0x3FFFFFFL) << 8 | (y & 0xFFL);
    }

    private static long packRegionKey(int regionX, int regionZ) {
        return ((long) regionX << 32) ^ (regionZ & 0xFFFFFFFFL);
    }

    private static boolean shouldConnectPair(HPACluster first, HPACluster second) {
        if (first.clusterX != second.clusterX)
            return first.clusterX < second.clusterX;

        if (first.clusterY != second.clusterY)
            return first.clusterY < second.clusterY;

        return first.clusterZ < second.clusterZ;
    }

    private static final int BASE_CLUSTER_HEIGHT = 8;
    private static final int BASE_CLUSTER_SIZE = 16;
    private static final float DIAGONAL_WEIGHT = (float) Math.sqrt(2);
    private static final float[] INTRA_NEIGHBOUR_COSTS;
    private static final int[][] INTRA_NEIGHBOUR_OFFSETS;
    private static final int MAX_CLUSTER_SIZE = 64;
    private static final int MAX_DEPTH = 3;
    private static final int MAX_WORLD_Y = 255;
    static {
        int[][] neighbours = new int[26][3];
        int index = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    neighbours[index][0] = dx;
                    neighbours[index][1] = dy;
                    neighbours[index][2] = dz;
                    index++;
                }
            }
        }
        INTRA_NEIGHBOUR_OFFSETS = neighbours;

        float[] costs = new float[INTRA_NEIGHBOUR_OFFSETS.length];
        for (int i = 0; i < INTRA_NEIGHBOUR_OFFSETS.length; i++) {
            int dx = INTRA_NEIGHBOUR_OFFSETS[i][0];
            int dy = INTRA_NEIGHBOUR_OFFSETS[i][1];
            int dz = INTRA_NEIGHBOUR_OFFSETS[i][2];
            costs[i] = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        INTRA_NEIGHBOUR_COSTS = costs;
    }
}
