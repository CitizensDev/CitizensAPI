package net.citizensnpcs.api.hpastar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        HPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
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
        HPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
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
        HPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
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
        HPAGraph graph = new TestHPAGraph(source, 0, 64, 0);
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

    private static Location loc(int x, int y, int z) {
        return new Location(null, x, y, z);
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
