package net.citizensnpcs.api.hpastar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import org.junit.Test;

import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.util.BoundingBox;

public class HPAGraphTest {
    @Test
    public void cachesWalkabilityChecksWithinCluster() {
        TestBlockSource source = new TestBlockSource(true);
        CountingHPAGraph graph = new CountingHPAGraph(source, 0, 64, 0);
        HPACluster cluster = new HPACluster(graph, 0, 16, 8, 0, 64, 0);

        assertTrue(cluster.hasWalkableNodes());
        int firstPassCalls = graph.walkableCalls;
        assertTrue("first scan should query walkability", firstPassCalls > 0);

        assertTrue(cluster.hasWalkableNodes());
        assertThat("second scan should hit cache only", graph.walkableCalls - firstPassCalls, is(0));
    }

    @Test
    public void findsPathAcrossOpenGround() {
        TestBlockSource source = new TestBlockSource(true);
        TestHPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);

        List<Vector> path = vectors(graph.findPath(loc(2, 64, 2), loc(20, 64, 20)));

        assertThat(path.isEmpty(), is(false));
        assertVector(path.get(0), 2, 64, 2);
        assertVector(path.get(path.size() - 1), 20, 64, 20);
    }

    @Test
    public void invalidateRegionRebuildsAndBlocksPath() {
        TestBlockSource source = new TestBlockSource(false);
        source.fillWalkableRect(0, 31, 5, 5);
        TestHPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);

        List<Vector> before = vectors(graph.findPath(loc(1, 64, 5), loc(20, 64, 5)));
        assertThat(before.isEmpty(), is(false));

        source.setBlockedAtWalkLevel(10, 5, true);
        graph.invalidateRegion(10, 5);

        List<Vector> after = vectors(graph.findPath(loc(1, 64, 5), loc(20, 64, 5)));
        assertTrue(after.isEmpty());
    }

    @Test
    public void invalidationRebuildsAndBlocksPath() {
        TestBlockSource source = new TestBlockSource(false);
        source.fillWalkableRect(0, 31, 5, 5);
        TestHPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);

        List<Vector> before = vectors(graph.findPath(loc(1, 64, 5), loc(20, 64, 5)));
        assertThat(before.isEmpty(), is(false));

        source.setBlockedAtWalkLevel(10, 5, true);
        graph.invalidateRegion(10, 5);

        List<Vector> after = vectors(graph.findPath(loc(1, 64, 5), loc(20, 64, 5)));
        assertTrue(after.isEmpty());
    }

    @Test
    public void keepsPathOnRequestedWalkLevel() {
        TestBlockSource source = new TestBlockSource(false, 67);
        source.fillWalkableRect(0, 31, 5, 5);
        HPAGraph graph = new TestHPAGraph(source, 0, 67, 0);
        graph.addClusters(0, 0);

        List<Vector> path = vectors(graph.findPath(loc(1, 67, 5), loc(20, 67, 5)));
        assertThat(path.isEmpty(), is(false));
        assertVector(path.get(0), 1, 67, 5);
        assertVector(path.get(path.size() - 1), 20, 67, 5);
        for (Vector vector : path) {
            assertThat(vector.getBlockY(), is(67));
        }
    }

    @Test
    public void navigatesStairsWithObstacleDetour() {
        TestBlockSource source = new TestBlockSource(false);
        for (int x = 1; x <= 10; x++) {
            int walkY = 64 + (x - 1) / 3;
            source.setWalkLevelAt(x, 5, walkY);
            source.setWalkLevelAt(x, 6, walkY);
            source.setBlockedAtWalkLevel(x, 5, false);
            source.setBlockedAtWalkLevel(x, 6, false);
        }
        source.setSolidAt(4, 66, 5, true);
        source.setSolidAt(5, 66, 5, true);

        HPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);

        List<Vector> path = vectors(graph.findPath(loc(1, 64, 5), loc(10, 67, 5)));
        assertThat(path.isEmpty(), is(false));
        assertVector(path.get(0), 1, 64, 5);
        assertVector(path.get(path.size() - 1), 10, 67, 5);

        for (Vector vector : path) {
            int y = vector.getBlockY();
            assertTrue("path should stay in staircase elevation band", y >= 64 && y <= 67);
            assertTrue("path should not include blocked obstacle node",
                    !(vector.getBlockX() == 4 && vector.getBlockY() == 65 && vector.getBlockZ() == 5));
            assertTrue("path should not include blocked obstacle node",
                    !(vector.getBlockX() == 5 && vector.getBlockY() == 65 && vector.getBlockZ() == 5));
        }
    }

    @Test
    public void returnsInfiniteCostForDisconnectedNodes() {
        TestBlockSource source = new TestBlockSource(true);
        HPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        AStarSolution solution = graph.pathfind(new HPAGraphNode(4, 64, 4), new HPAGraphNode(20, 64, 4), 0);
        assertTrue(Float.isInfinite(solution.cost));
        assertTrue(solution.convertToVectors().isEmpty());
    }

    @Test
    public void reusingGraphDoesNotRebuildClustersForSameRegion() {
        TestBlockSource source = new TestBlockSource(true);
        CountingHPAGraph graph = new CountingHPAGraph(source, 0, 64, 0);

        graph.addClusters(0, 0);
        int firstBuildWalkableCalls = graph.walkableCalls;
        int depthZeroClusters = graph.clusters.get(0).size();

        graph.addClusters(0, 0);

        assertThat(graph.clusters.get(0).size(), is(depthZeroClusters));
        assertThat(graph.walkableCalls, is(firstBuildWalkableCalls));
    }

    @Test
    public void supportsDiagonalClusterTransitions() {
        TestBlockSource source = new TestBlockSource(false);
        source.fillWalkableRect(0, 15, 0, 15);
        source.fillWalkableRect(16, 31, 16, 31);

        HPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);

        List<Vector> path = vectors(graph.findPath(loc(2, 64, 2), loc(20, 64, 20)));
        assertThat(path.isEmpty(), is(false));
        assertVector(path.get(0), 2, 64, 2);
        assertVector(path.get(path.size() - 1), 20, 64, 20);
    }

    @Test
    public void returnsExpandedIntraClusterWaypoints() {
        TestBlockSource source = new TestBlockSource(false);
        source.fillWalkableRect(1, 10, 5, 5);

        HPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);

        List<Vector> path = vectors(graph.findPath(loc(1, 64, 5), loc(10, 64, 5)));
        assertThat(path.isEmpty(), is(false));
        assertThat(path.size(), is(10));
        for (int x = 1; x <= 10; x++) {
            assertVector(path.get(x - 1), x, 64, 5);
        }
    }

    @Test
    public void returnsContiguousStepsAfterIntraRefinement() {
        TestBlockSource source = new TestBlockSource(true);
        HPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);

        List<Vector> path = vectors(graph.findPath(loc(2, 64, 2), loc(20, 64, 20)));
        assertThat(path.isEmpty(), is(false));
        for (int i = 1; i < path.size(); i++) {
            Vector previous = path.get(i - 1);
            Vector current = path.get(i);
            int dx = Math.abs(current.getBlockX() - previous.getBlockX());
            int dy = Math.abs(current.getBlockY() - previous.getBlockY());
            int dz = Math.abs(current.getBlockZ() - previous.getBlockZ());
            assertTrue(dx <= 1 && dy <= 1 && dz <= 1);
            assertTrue(dx + dy + dz > 0);
        }
    }

    @Test
    public void refinesIntraEdgesLazilyWhenConsumed() {
        TestBlockSource source = new TestBlockSource(true);
        CountingHPAGraph graph = new CountingHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);

        int walkableCallsBeforeFind = graph.walkableCalls;
        Path path = (Path) graph.findPath(loc(2, 64, 2), loc(14, 64, 14));
        assertThat(graph.walkableCalls, is(walkableCallsBeforeFind));

        path.update(null);
        path.getCurrentVector();
        assertTrue(graph.walkableCalls > walkableCallsBeforeFind);
    }

    @Test
    public void supportsTransitionsIntoLaterLoadedEastRegion() {
        TestBlockSource source = new TestBlockSource(true);
        TestHPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);
        graph.addClusters(64, 0);

        List<Vector> path = vectors(graph.findPath(loc(8, 64, 8), loc(72, 64, 8)));
        assertThat(path.isEmpty(), is(false));
        assertVector(path.get(0), 8, 64, 8);
        assertVector(path.get(path.size() - 1), 72, 64, 8);
    }

    @Test
    public void supportsTransitionsIntoLaterLoadedNorthRegion() {
        TestBlockSource source = new TestBlockSource(true);
        HPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);
        graph.addClusters(0, 64);

        List<Vector> path = vectors(graph.findPath(loc(8, 64, 8), loc(8, 64, 72)));
        assertThat(path.isEmpty(), is(false));
        assertVector(path.get(0), 8, 64, 8);
        assertVector(path.get(path.size() - 1), 8, 64, 72);
    }

    @Test
    public void supportsMultiStairTransitionsAcrossChunkBoundariesInBothDirections() {
        TestBlockSource source = new TestBlockSource(false);
        carveStairCorridor(source, 5, new int[][] {
                { 1, 15, 64 },
                { 16, 31, 65 },
                { 32, 47, 64 },
                { 48, 63, 65 } });

        TestHPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);

        assertStairPathExists(graph, source, 1, 64, 63, 65, 5, 1, 63);
        assertStairPathExists(graph, source, 63, 65, 1, 64, 5, 1, 63);
    }

    @Test
    public void supportsMultiStairTransitionsAcrossLaterLoadedRegionBoundary() {
        TestBlockSource source = new TestBlockSource(false);
        carveStairCorridor(source, 5, new int[][] {
                { 1, 15, 64 },
                { 16, 31, 65 },
                { 32, 47, 64 },
                { 48, 63, 65 },
                { 64, 79, 64 },
                { 80, 95, 65 } });

        TestHPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(0, 0);
        graph.addClusters(64, 0);

        assertStairPathExists(graph, source, 1, 64, 95, 65, 5, 1, 95);
        assertStairPathExists(graph, source, 95, 65, 1, 64, 5, 1, 95);
    }

    @Test
    public void supportsMultiStairTransitionsAcrossEarlierLoadedRegionBoundary() {
        TestBlockSource source = new TestBlockSource(false);
        carveStairCorridor(source, 5, new int[][] {
                { 1, 15, 64 },
                { 16, 31, 65 },
                { 32, 47, 64 },
                { 48, 63, 65 },
                { 64, 79, 64 },
                { 80, 95, 65 } });

        TestHPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
        graph.addClusters(64, 0);
        graph.addClusters(0, 0);

        assertStairPathExists(graph, source, 1, 64, 95, 65, 5, 1, 95);
        assertStairPathExists(graph, source, 95, 65, 1, 64, 5, 1, 95);
    }

    private static class BlockCoord {
        private final int x;
        private final int y;
        private final int z;

        private BlockCoord(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof BlockCoord))
                return false;
            BlockCoord other = (BlockCoord) obj;
            return x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    private static class ColumnCoord {
        private final int x;
        private final int z;

        private ColumnCoord(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof ColumnCoord))
                return false;
            ColumnCoord other = (ColumnCoord) obj;
            return x == other.x && z == other.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    private static class CountingHPAGraph extends TestHPAGraph {
        private int walkableCalls;

        private CountingHPAGraph(TestBlockSource source, int cx, int cy, int cz) {
            super(source, cx, cy, cz);
        }

        @Override
        public boolean walkable(int x, int y, int z) {
            walkableCalls++;
            return super.walkable(x, y, z);
        }
    }

    private static class TestBlockSource extends BlockSource {
        private final Map<BlockCoord, Boolean> blockedOverrides = new HashMap<>();
        private final boolean defaultWalkableAtWalkLevel;
        private final Map<ColumnCoord, Integer> walkLevelOverrides = new HashMap<>();
        private final int walkLevelY;

        private TestBlockSource(boolean defaultWalkableAtWalkLevel) {
            this(defaultWalkableAtWalkLevel, 64);
        }

        private TestBlockSource(boolean defaultWalkableAtWalkLevel, int walkLevelY) {
            this.defaultWalkableAtWalkLevel = defaultWalkableAtWalkLevel;
            this.walkLevelY = walkLevelY;
        }

        private void fillBlockedRect(int minX, int maxX, int minZ, int maxZ) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    setBlockedAtWalkLevel(x, z, true);
                }
            }
        }

        private void fillWalkableRect(int minX, int maxX, int minZ, int maxZ) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    setBlockedAtWalkLevel(x, z, false);
                }
            }
        }

        @Override
        public BlockData getBlockDataAt(int x, int y, int z) {
            return null;
        }

        @Override
        public BoundingBox getCollisionBox(int x, int y, int z) {
            return null;
        }

        @Override
        public org.bukkit.Material getMaterialAt(int x, int y, int z) {
            return null;
        }

        private boolean isSolidAt(int x, int y, int z) {
            Boolean blocked = blockedOverrides.get(new BlockCoord(x, y, z));
            if (blocked != null) {
                return blocked;
            }
            int columnWalkLevel = resolveWalkLevel(x, z);
            if (y > columnWalkLevel + 1) {
                return true;
            }
            if (y <= columnWalkLevel - 1) {
                return true;
            }
            if (y > columnWalkLevel) {
                return false;
            }
            return !defaultWalkableAtWalkLevel;
        }

        @Override
        public boolean isYWithinBounds(int y) {
            return y >= 0 && y < 256;
        }

        private int resolveWalkLevel(int x, int z) {
            return walkLevelOverrides.getOrDefault(new ColumnCoord(x, z), walkLevelY);
        }

        private void setBlockedAtWalkLevel(int x, int z, boolean blocked) {
            blockedOverrides.put(new BlockCoord(x, resolveWalkLevel(x, z), z), blocked);
        }

        private void setSolidAt(int x, int y, int z, boolean solid) {
            blockedOverrides.put(new BlockCoord(x, y, z), solid);
        }

        private void setWalkLevelAt(int x, int z, int y) {
            walkLevelOverrides.put(new ColumnCoord(x, z), y);
        }
    }

    private static class TestHPAGraph extends HPAGraph {
        private final TestBlockSource source;

        private TestHPAGraph(TestBlockSource source, int cx, int cy, int cz) {
            super(source, cx, cy, cz);
            this.source = source;
        }

        @Override
        public boolean walkable(int x, int y, int z) {
            if (y == 0) {
                return false;
            }
            return !source.isSolidAt(x, y, z) && !source.isSolidAt(x, y + 1, z) && source.isSolidAt(x, y - 1, z);
        }
    }

    private static void assertVector(Vector vector, int x, int y, int z) {
        assertThat(vector.getBlockX(), is(x));
        assertThat(vector.getBlockY(), is(y));
        assertThat(vector.getBlockZ(), is(z));
    }

    private static void assertStairPathExists(TestHPAGraph graph, TestBlockSource source, int startX, int startY,
            int goalX,
            int goalY, int z, int minX, int maxX) {
        assertTrue("reference walkability search should find a path",
                hasConventionalPath(graph, startX, startY, z, goalX, goalY, z, minX, maxX, z));

        List<Vector> path = vectors(graph.findPath(loc(startX, startY, z), loc(goalX, goalY, z)));
        assertThat(path.isEmpty(), is(false));
        assertVector(path.get(0), startX, startY, z);
        assertVector(path.get(path.size() - 1), goalX, goalY, z);
        for (int i = 1; i < path.size(); i++) {
            Vector previous = path.get(i - 1);
            Vector current = path.get(i);
            int dx = Math.abs(current.getBlockX() - previous.getBlockX());
            int dy = Math.abs(current.getBlockY() - previous.getBlockY());
            int dz = Math.abs(current.getBlockZ() - previous.getBlockZ());
            assertTrue("path steps should be contiguous: previous=" + previous + " current=" + current,
                    dx <= 1 && dy <= 1 && dz <= 1 && dx + dy + dz > 0);
        }
        for (Vector vector : path) {
            int x = vector.getBlockX();
            int y = vector.getBlockY();
            assertTrue("path should remain in corridor bounds", x >= minX && x <= maxX);
            assertThat(vector.getBlockZ(), is(z));
            assertThat(y, is(source.resolveWalkLevel(x, z)));
        }
    }

    private static void carveStairCorridor(TestBlockSource source, int z, int[][] segments) {
        for (int[] segment : segments) {
            int minX = segment[0];
            int maxX = segment[1];
            int walkY = segment[2];
            for (int x = minX; x <= maxX; x++) {
                source.setWalkLevelAt(x, z, walkY);
                source.setBlockedAtWalkLevel(x, z, false);
            }
        }
    }

    private static Location loc(int x, int y, int z) {
        return new Location(null, x, y, z);
    }

    private static boolean hasConventionalPath(TestHPAGraph graph, int startX, int startY, int startZ, int goalX,
            int goalY, int goalZ, int minX, int maxX, int fixedZ) {
        BlockCoord start = new BlockCoord(startX, startY, startZ);
        BlockCoord goal = new BlockCoord(goalX, goalY, goalZ);
        Queue<BlockCoord> queue = new ArrayDeque<>();
        Set<BlockCoord> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockCoord current = queue.poll();
            if (current.equals(goal))
                return true;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0)
                            continue;
                        int nx = current.x + dx;
                        int ny = current.y + dy;
                        int nz = current.z + dz;
                        if (nx < minX || nx > maxX || nz != fixedZ)
                            continue;
                        BlockCoord neighbour = new BlockCoord(nx, ny, nz);
                        if (visited.contains(neighbour))
                            continue;
                        if (!graph.walkable(nx, ny, nz))
                            continue;
                        visited.add(neighbour);
                        queue.add(neighbour);
                    }
                }
            }
        }
        return false;
    }

    private static List<Vector> vectors(Plan plan) {
        Path path = (Path) plan;
        List<Vector> out = new ArrayList<>();
        for (Vector vector : path.getPath()) {
            out.add(vector);
        }
        return out;
    }
}
