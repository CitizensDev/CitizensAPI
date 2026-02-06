package net.citizensnpcs.api.hpastar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class HPACluster {
    final int clusterHeight;
    final int clusterSize;
    final int clusterX;
    final int clusterY;
    final int clusterZ;
    private final HPAGraph graph;
    private boolean intraConnected;
    private final int level;
    private final List<HPAGraphNode> nodes = new ArrayList<>();
    private final Long2ObjectOpenHashMap<HPAGraphNode> nodesByPosition = new Long2ObjectOpenHashMap<>();
    private final byte[] walkableCache;

    public HPACluster(HPAGraph graph, int level, int clusterSize, int clusterHeight, int clusterX, int clusterY,
            int clusterZ) {
        this.graph = graph;
        this.level = level;
        this.clusterSize = clusterSize;
        this.clusterHeight = clusterHeight;
        this.clusterX = clusterX;
        this.clusterY = clusterY;
        this.clusterZ = clusterZ;
        walkableCache = new byte[clusterSize * clusterHeight * clusterSize];
        Arrays.fill(walkableCache, (byte) -1);
    }

    private HPAGraphNode[] addEntranceNode(HPAEntrance entrance) {
        // For 2D entrances (walls between clusters)
        if (entrance.minY == entrance.maxY) {
            if (entrance.maxX - entrance.minX > 6)
                return new HPAGraphNode[] { getOrAddNode(entrance.minX, entrance.minY, entrance.minZ),
                        getOrAddNode(entrance.maxX, entrance.minY, entrance.minZ) };
            else if (entrance.maxZ - entrance.minZ > 6)
                return new HPAGraphNode[] { getOrAddNode(entrance.minX, entrance.minY, entrance.minZ),
                        getOrAddNode(entrance.minX, entrance.minY, entrance.maxZ) };
            int x = (int) (entrance.minX == entrance.maxX ? entrance.minX
                    : Math.floor((entrance.minX + entrance.maxX) / 2.0));
            int z = (int) (entrance.minZ == entrance.maxZ ? entrance.minZ
                    : Math.floor((entrance.minZ + entrance.maxZ) / 2.0));
            return new HPAGraphNode[] { getOrAddNode(x, entrance.minY, z) };
        }
        // For vertical entrances (floors/ceilings between clusters)
        int x = (int) Math.floor((entrance.minX + entrance.maxX) / 2.0);
        int y = (int) Math.floor((entrance.minY + entrance.maxY) / 2.0);
        int z = (int) Math.floor((entrance.minZ + entrance.maxZ) / 2.0);
        return new HPAGraphNode[] { getOrAddNode(x, y, z) };
    }

    private void addNodeReference(HPAGraphNode node) {
        long key = packPosition(node.x, node.y, node.z);
        HPAGraphNode old = nodesByPosition.put(key, node);
        if (old != null)
            throw new IllegalStateException();
        nodes.add(node);
    }

    public void buildFrom(List<HPACluster> clusters) {
        for (HPACluster other : clusters) {
            for (HPAGraphNode node : other.nodes) {
                if (node.x == clusterX || node.z == clusterZ || node.y == clusterY
                        || node.x == clusterX + clusterSize - 1 || node.z == clusterZ + clusterSize - 1
                        || node.y == clusterY + clusterHeight - 1) { // border node
                    addNodeReference(node);
                    for (HPAGraphEdge edge : node.getEdges(level - 1)) {
                        if (edge.type == HPAGraphEdge.EdgeType.INTER) {
                            edge.from.connect(level, edge.to, edge.type, edge.weight);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < nodes.size(); i++) {
            HPAGraphNode source = nodes.get(i);
            Long2ObjectOpenHashMap<HPAGraphNode> unresolvedTargets = new Long2ObjectOpenHashMap<>();
            for (int j = i + 1; j < nodes.size(); j++) {
                HPAGraphNode target = nodes.get(j);
                HPAGraphNode old = unresolvedTargets.put(packPosition(target.x, target.y, target.z), target);
                if (old != null)
                    throw new IllegalStateException();
            }
            if (!unresolvedTargets.isEmpty()) {
                connectLowerLevelFromSource(source, unresolvedTargets);
            }
        }
    }

    public void connect(HPACluster other, Direction direction) {
        HPAEntrance entrance = null;
        switch (direction) {
            case EAST:
                for (int y = 0; y < clusterHeight; y++) {
                    for (int z = 0; z < clusterSize; z++) {
                        if (offsetWalkable(clusterSize - 1, y, z) && other.offsetWalkable(0, y, z)) {
                            if (entrance == null) {
                                entrance = new HPAEntrance();
                                entrance.minY = entrance.maxY = y;
                                entrance.minX = entrance.maxX = clusterSize - 1;
                                entrance.minZ = z;
                            }
                            entrance.maxZ = z;
                        } else if (entrance != null) {
                            connectEntrance(other, entrance, 0, 0, entrance.minY, entrance.maxY, entrance.minZ,
                                    entrance.maxZ);
                            entrance = null;
                        }
                    }
                    if (entrance != null) {
                        connectEntrance(other, entrance, 0, 0, entrance.minY, entrance.maxY, entrance.minZ,
                                entrance.maxZ);
                        entrance = null;
                    }
                }
                for (int y = 0; y < clusterHeight; y++) {
                    for (int z = 0; z < clusterSize; z++) {
                        if (!offsetWalkable(clusterSize - 1, y, z))
                            continue;
                        int up = y + 1;
                        if (up < clusterHeight && other.offsetWalkable(0, up, z)) {
                            HPAGraphNode from = getOrAddNode(clusterSize - 1, y, z);
                            HPAGraphNode to = other.getOrAddNode(0, up, z);
                            from.connect(level, to, HPAGraphEdge.EdgeType.INTER, VERTICAL_STEP_WEIGHT);
                        }
                        int down = y - 1;
                        if (down >= 0 && other.offsetWalkable(0, down, z)) {
                            HPAGraphNode from = getOrAddNode(clusterSize - 1, y, z);
                            HPAGraphNode to = other.getOrAddNode(0, down, z);
                            from.connect(level, to, HPAGraphEdge.EdgeType.INTER, VERTICAL_STEP_WEIGHT);
                        }
                    }
                }
                break;
            case WEST:
                for (int y = 0; y < clusterHeight; y++) {
                    for (int z = 0; z < clusterSize; z++) {
                        if (offsetWalkable(0, y, z) && other.offsetWalkable(clusterSize - 1, y, z)) {
                            if (entrance == null) {
                                entrance = new HPAEntrance();
                                entrance.minY = entrance.maxY = y;
                                entrance.minX = entrance.maxX = 0;
                                entrance.minZ = z;
                            }
                            entrance.maxZ = z;
                        } else if (entrance != null) {
                            connectEntrance(other, entrance, clusterSize - 1, clusterSize - 1, entrance.minY,
                                    entrance.maxY, entrance.minZ, entrance.maxZ);
                            entrance = null;
                        }
                    }
                    if (entrance != null) {
                        connectEntrance(other, entrance, clusterSize - 1, clusterSize - 1, entrance.minY,
                                entrance.maxY, entrance.minZ, entrance.maxZ);
                        entrance = null;
                    }
                }
                for (int y = 0; y < clusterHeight; y++) {
                    for (int z = 0; z < clusterSize; z++) {
                        if (!offsetWalkable(0, y, z))
                            continue;
                        int up = y + 1;
                        if (up < clusterHeight && other.offsetWalkable(clusterSize - 1, up, z)) {
                            HPAGraphNode from = getOrAddNode(0, y, z);
                            HPAGraphNode to = other.getOrAddNode(clusterSize - 1, up, z);
                            from.connect(level, to, HPAGraphEdge.EdgeType.INTER, VERTICAL_STEP_WEIGHT);
                        }
                        int down = y - 1;
                        if (down >= 0 && other.offsetWalkable(clusterSize - 1, down, z)) {
                            HPAGraphNode from = getOrAddNode(0, y, z);
                            HPAGraphNode to = other.getOrAddNode(clusterSize - 1, down, z);
                            from.connect(level, to, HPAGraphEdge.EdgeType.INTER, VERTICAL_STEP_WEIGHT);
                        }
                    }
                }
                break;
            case NORTH:
                for (int y = 0; y < clusterHeight; y++) {
                    for (int x = 0; x < clusterSize; x++) {
                        if (offsetWalkable(x, y, clusterSize - 1) && other.offsetWalkable(x, y, 0)) {
                            if (entrance == null) {
                                entrance = new HPAEntrance();
                                entrance.minY = entrance.maxY = y;
                                entrance.minZ = entrance.maxZ = clusterSize - 1;
                                entrance.minX = x;
                            }
                            entrance.maxX = x;
                        } else if (entrance != null) {
                            connectEntrance(other, entrance, entrance.minX, entrance.maxX, entrance.minY,
                                    entrance.maxY, 0, 0);
                            entrance = null;
                        }
                    }
                    if (entrance != null) {
                        connectEntrance(other, entrance, entrance.minX, entrance.maxX, entrance.minY, entrance.maxY,
                                0, 0);
                        entrance = null;
                    }
                }
                for (int y = 0; y < clusterHeight; y++) {
                    for (int x = 0; x < clusterSize; x++) {
                        if (!offsetWalkable(x, y, clusterSize - 1))
                            continue;
                        int up = y + 1;
                        if (up < clusterHeight && other.offsetWalkable(x, up, 0)) {
                            HPAGraphNode from = getOrAddNode(x, y, clusterSize - 1);
                            HPAGraphNode to = other.getOrAddNode(x, up, 0);
                            from.connect(level, to, HPAGraphEdge.EdgeType.INTER, VERTICAL_STEP_WEIGHT);
                        }
                        int down = y - 1;
                        if (down >= 0 && other.offsetWalkable(x, down, 0)) {
                            HPAGraphNode from = getOrAddNode(x, y, clusterSize - 1);
                            HPAGraphNode to = other.getOrAddNode(x, down, 0);
                            from.connect(level, to, HPAGraphEdge.EdgeType.INTER, VERTICAL_STEP_WEIGHT);
                        }
                    }
                }
                break;
            case SOUTH:
                for (int y = 0; y < clusterHeight; y++) {
                    for (int x = 0; x < clusterSize; x++) {
                        if (offsetWalkable(x, y, 0) && other.offsetWalkable(x, y, clusterSize - 1)) {
                            if (entrance == null) {
                                entrance = new HPAEntrance();
                                entrance.minY = entrance.maxY = y;
                                entrance.minZ = entrance.maxZ = 0;
                                entrance.minX = x;
                            }
                            entrance.maxX = x;
                        } else if (entrance != null) {
                            connectEntrance(other, entrance, entrance.minX, entrance.maxX, entrance.minY,
                                    entrance.maxY, clusterSize - 1, clusterSize - 1);
                            entrance = null;
                        }
                    }
                    if (entrance != null) {
                        connectEntrance(other, entrance, entrance.minX, entrance.maxX, entrance.minY, entrance.maxY,
                                clusterSize - 1, clusterSize - 1);
                        entrance = null;
                    }
                }
                for (int y = 0; y < clusterHeight; y++) {
                    for (int x = 0; x < clusterSize; x++) {
                        if (!offsetWalkable(x, y, 0))
                            continue;
                        int up = y + 1;
                        if (up < clusterHeight && other.offsetWalkable(x, up, clusterSize - 1)) {
                            HPAGraphNode from = getOrAddNode(x, y, 0);
                            HPAGraphNode to = other.getOrAddNode(x, up, clusterSize - 1);
                            from.connect(level, to, HPAGraphEdge.EdgeType.INTER, VERTICAL_STEP_WEIGHT);
                        }
                        int down = y - 1;
                        if (down >= 0 && other.offsetWalkable(x, down, clusterSize - 1)) {
                            HPAGraphNode from = getOrAddNode(x, y, 0);
                            HPAGraphNode to = other.getOrAddNode(x, down, clusterSize - 1);
                            from.connect(level, to, HPAGraphEdge.EdgeType.INTER, VERTICAL_STEP_WEIGHT);
                        }
                    }
                }
                break;
            case UP:
                for (int x = 0; x < clusterSize; x++) {
                    for (int z = 0; z < clusterSize; z++) {
                        if (offsetWalkable(x, clusterHeight - 1, z) && other.offsetWalkable(x, 0, z)) {
                            if (entrance == null) {
                                entrance = new HPAEntrance();
                                entrance.minY = entrance.maxY = clusterHeight - 1;
                                entrance.minX = x;
                                entrance.minZ = z;
                            }
                            entrance.maxX = x;
                            entrance.maxZ = z;
                        } else if (entrance != null) {
                            connectEntrance(other, entrance, entrance.minX, entrance.maxX, 0, 0, entrance.minZ,
                                    entrance.maxZ);
                            entrance = null;
                        }
                    }
                }
                if (entrance != null) {
                    connectEntrance(other, entrance, entrance.minX, entrance.maxX, 0, 0, entrance.minZ,
                            entrance.maxZ);
                }
                break;
            case DOWN:
                for (int x = 0; x < clusterSize; x++) {
                    for (int z = 0; z < clusterSize; z++) {
                        if (offsetWalkable(x, 0, z) && other.offsetWalkable(x, clusterHeight - 1, z)) {
                            if (entrance == null) {
                                entrance = new HPAEntrance();
                                entrance.minY = entrance.maxY = 0;
                                entrance.minX = x;
                                entrance.minZ = z;
                            }
                            entrance.maxX = x;
                            entrance.maxZ = z;
                        } else if (entrance != null) {
                            connectEntrance(other, entrance, entrance.minX, entrance.maxX, clusterHeight - 1,
                                    clusterHeight - 1, entrance.minZ, entrance.maxZ);
                            entrance = null;
                        }
                    }
                }
                if (entrance != null) {
                    connectEntrance(other, entrance, entrance.minX, entrance.maxX, clusterHeight - 1,
                            clusterHeight - 1, entrance.minZ, entrance.maxZ);
                }
                break;
        }
    }

    public void connectDiagonal(HPACluster other, int dx, int dz, float weight) {
        int fromX = dx > 0 ? clusterSize - 1 : 0;
        int fromZ = dz > 0 ? clusterSize - 1 : 0;
        int toX = dx > 0 ? 0 : other.clusterSize - 1;
        int toZ = dz > 0 ? 0 : other.clusterSize - 1;
        for (int y = 0; y < clusterHeight; y++) {
            if (!offsetWalkable(fromX, y, fromZ) || !other.offsetWalkable(toX, y, toZ)) {
                continue;
            }
            HPAGraphNode from = getOrAddNode(fromX, y, fromZ);
            HPAGraphNode to = other.getOrAddNode(toX, y, toZ);
            from.connect(level, to, HPAGraphEdge.EdgeType.INTER, weight);
        }
    }

    private void connectEntrance(HPACluster other, HPAEntrance entrance, int toMinX, int toMaxX, int toMinY,
            int toMaxY, int toMinZ, int toMaxZ) {
        HPAGraphNode[] from = addEntranceNode(entrance);
        HPAEntrance destination = new HPAEntrance();
        destination.minX = toMinX;
        destination.maxX = toMaxX;
        destination.minY = toMinY;
        destination.maxY = toMaxY;
        destination.minZ = toMinZ;
        destination.maxZ = toMaxZ;
        HPAGraphNode[] to = other.addEntranceNode(destination);
        for (int i = 0; i < from.length; i++) {
            from[i].connect(level, to[i], HPAGraphEdge.EdgeType.INTER, 1F);
        }
    }

    public void connectIntra() {
        for (int i = 0; i < nodes.size(); i++) {
            HPAGraphNode source = nodes.get(i);
            Long2ObjectOpenHashMap<HPAGraphNode> unresolvedTargets = new Long2ObjectOpenHashMap<>();
            for (int j = i + 1; j < nodes.size(); j++) {
                HPAGraphNode target = nodes.get(j);
                HPAGraphNode old = unresolvedTargets.put(packPosition(target.x, target.y, target.z), target);
                if (old != null)
                    throw new IllegalStateException();
            }
            if (!unresolvedTargets.isEmpty()) {
                connectIntraFromSource(source, unresolvedTargets);
            }
        }
        intraConnected = true;
    }

    private void connectIntraFromSource(HPAGraphNode source, Long2ObjectOpenHashMap<HPAGraphNode> unresolvedTargets) {
        Long2FloatOpenHashMap bestCosts = new Long2FloatOpenHashMap();
        bestCosts.defaultReturnValue(Float.POSITIVE_INFINITY);
        Queue<IntraFrontierNode> frontier = new PriorityQueue<>();

        long sourceKey = packPosition(source.x, source.y, source.z);
        bestCosts.put(sourceKey, 0F);
        frontier.add(new IntraFrontierNode(source.x, source.y, source.z, 0F));

        while (!frontier.isEmpty() && !unresolvedTargets.isEmpty()) {
            IntraFrontierNode current = frontier.poll();
            long currentKey = packPosition(current.x, current.y, current.z);
            if (current.g > bestCosts.get(currentKey))
                continue;

            HPAGraphNode reached = unresolvedTargets.remove(currentKey);
            if (reached != null) {
                source.connect(level, reached, HPAGraphEdge.EdgeType.INTRA, current.g);
                if (unresolvedTargets.isEmpty())
                    break;
            }
            for (int i = 0; i < NEIGHBOUR_OFFSETS.length; i++) {
                int nx = current.x + NEIGHBOUR_OFFSETS[i][0];
                int ny = current.y + NEIGHBOUR_OFFSETS[i][1];
                int nz = current.z + NEIGHBOUR_OFFSETS[i][2];
                if (nx < clusterX || nx >= clusterX + clusterSize || ny < clusterY || ny >= clusterY + clusterHeight
                        || nz < clusterZ || nz >= clusterZ + clusterSize)
                    continue;

                if (!offsetWalkable(nx - clusterX, ny - clusterY, nz - clusterZ))
                    continue;

                float tentativeCost = current.g + NEIGHBOUR_COSTS[i];
                long neighbourKey = packPosition(nx, ny, nz);
                if (tentativeCost >= bestCosts.get(neighbourKey))
                    continue;

                bestCosts.put(neighbourKey, tentativeCost);
                frontier.add(new IntraFrontierNode(nx, ny, nz, tentativeCost));
            }
        }
    }

    private void connectLowerLevelFromSource(HPAGraphNode source,
            Long2ObjectOpenHashMap<HPAGraphNode> unresolvedTargets) {
        Long2FloatOpenHashMap bestCosts = new Long2FloatOpenHashMap();
        bestCosts.defaultReturnValue(Float.POSITIVE_INFINITY);
        Queue<GraphFrontierNode> frontier = new PriorityQueue<>();
        long sourceKey = packPosition(source.x, source.y, source.z);
        bestCosts.put(sourceKey, 0F);
        frontier.add(new GraphFrontierNode(source, 0F));

        while (!frontier.isEmpty() && !unresolvedTargets.isEmpty()) {
            GraphFrontierNode current = frontier.poll();
            long currentKey = packPosition(current.node.x, current.node.y, current.node.z);
            if (current.g > bestCosts.get(currentKey))
                continue;

            HPAGraphNode reached = unresolvedTargets.remove(currentKey);
            if (reached != null) {
                source.connect(level, reached, HPAGraphEdge.EdgeType.INTRA, current.g);
                if (unresolvedTargets.isEmpty())
                    break;
            }
            for (HPAGraphEdge edge : current.node.getEdges(level - 1)) {
                long neighbourKey = packPosition(edge.to.x, edge.to.y, edge.to.z);
                float tentativeCost = current.g + edge.weight;
                if (tentativeCost >= bestCosts.get(neighbourKey))
                    continue;

                bestCosts.put(neighbourKey, tentativeCost);
                frontier.add(new GraphFrontierNode(edge.to, tentativeCost));
            }
        }
    }

    public boolean contains(HPACluster other) {
        return clusterX + clusterSize > other.clusterX && clusterY + clusterHeight > other.clusterY
                && clusterZ + clusterSize > other.clusterZ && other.clusterX >= clusterX && other.clusterY >= clusterY
                && other.clusterZ >= clusterZ;
    }

    public boolean containsPoint(int x, int y, int z) {
        return x >= clusterX && x < clusterX + clusterSize && y >= clusterY && y < clusterY + clusterHeight
                && z >= clusterZ && z < clusterZ + clusterSize;
    }

    private HPAGraphNode getOrAddNode(int x, int y, int z) {
        long key = packPosition(clusterX + x, clusterY + y, clusterZ + z);
        HPAGraphNode existing = nodesByPosition.get(key);
        if (existing != null)
            return existing;

        HPAGraphNode node = new HPAGraphNode(this.clusterX + x, this.clusterY + y, this.clusterZ + z);
        addNodeReference(node);
        if (intraConnected) {
            Long2ObjectOpenHashMap<HPAGraphNode> unresolvedTargets = new Long2ObjectOpenHashMap<>();
            for (HPAGraphNode target : nodes) {
                if (target == node)
                    continue;
                HPAGraphNode old = unresolvedTargets.put(packPosition(target.x, target.y, target.z), target);
                if (old != null)
                    throw new IllegalStateException();
            }
            if (!unresolvedTargets.isEmpty()) {
                connectIntraFromSource(node, unresolvedTargets);
            }
        }
        return node;
    }

    public boolean hasWalkableNodes() {
        for (int x = 0; x < clusterSize; x++) {
            for (int y = 0; y < clusterHeight; y++) {
                for (int z = 0; z < clusterSize; z++) {
                    if (offsetWalkable(x, y, z))
                        return true;
                }
            }
        }
        return false;
    }

    public void insert(HPAGraphNode node) {
        addNodeReference(node);
        Long2ObjectOpenHashMap<HPAGraphNode> unresolvedTargets = new Long2ObjectOpenHashMap<>();
        for (HPAGraphNode target : nodes) {
            if (target == node)
                continue;
            HPAGraphNode old = unresolvedTargets.put(packPosition(target.x, target.y, target.z), target);
            if (old != null)
                throw new IllegalStateException();
        }
        if (!unresolvedTargets.isEmpty()) {
            connectIntraFromSource(node, unresolvedTargets);
        }
    }

    private boolean offsetWalkable(int x, int y, int z) {
        int index = ((y * clusterSize) + z) * clusterSize + x;
        byte cached = walkableCache[index];
        if (cached == -1) {
            cached = (byte) (graph.walkable(clusterX + x, clusterY + y, clusterZ + z) ? 1 : 0);
            walkableCache[index] = cached;
        }
        return cached == 1;
    }

    public void remove(HPAGraphNode... nodes) {
        for (HPAGraphNode node : nodes) {
            List<List<HPAGraphEdge>> edges2 = node.edges;
            for (int i = 0; i < edges2.size(); i++) {
                List<HPAGraphEdge> edges = edges2.get(i);
                for (HPAGraphEdge edge : edges) {
                    if (i >= edge.to.edges.size())
                        continue;

                    edge.to.edges.get(i).removeIf(other -> other.to == node);
                }
                edges.clear();
            }
            removeNodeReference(node);
        }
    }

    private void removeNodeReference(HPAGraphNode node) {
        nodes.remove(node);
        nodesByPosition.remove(packPosition(node.x, node.y, node.z));
    }

    @Override
    public String toString() {
        return "C[" + level + "] (" + clusterX + "," + clusterY + "," + clusterZ + ")->(" + (clusterX + clusterSize - 1)
                + "," + (clusterY + clusterHeight - 1) + "," + (clusterZ + clusterSize - 1) + ")";
    }

    public enum Direction {
        DOWN,
        EAST,
        NORTH,
        SOUTH,
        UP,
        WEST;
    }

    private static class GraphFrontierNode implements Comparable<GraphFrontierNode> {
        final float g;
        final HPAGraphNode node;

        private GraphFrontierNode(HPAGraphNode node, float g) {
            this.node = node;
            this.g = g;
        }

        @Override
        public int compareTo(GraphFrontierNode other) {
            return Float.compare(g, other.g);
        }
    }

    private static class IntraFrontierNode implements Comparable<IntraFrontierNode> {
        final float g;
        final int x;
        final int y;
        final int z;

        private IntraFrontierNode(int x, int y, int z, float g) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.g = g;
        }

        @Override
        public int compareTo(IntraFrontierNode other) {
            return Float.compare(g, other.g);
        }
    }

    private static long packPosition(int x, int y, int z) {
        return (x & 0x3FFFFFFL) << 34 | (z & 0x3FFFFFFL) << 8 | (y & 0xFFL);
    }

    private static final float[] NEIGHBOUR_COSTS;
    private static final int[][] NEIGHBOUR_OFFSETS;
    private static final float VERTICAL_STEP_WEIGHT = (float) Math.sqrt(2);
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
        NEIGHBOUR_OFFSETS = neighbours;

        float[] costs = new float[NEIGHBOUR_OFFSETS.length];
        for (int i = 0; i < NEIGHBOUR_OFFSETS.length; i++) {
            int dx = NEIGHBOUR_OFFSETS[i][0];
            int dy = NEIGHBOUR_OFFSETS[i][1];
            int dz = NEIGHBOUR_OFFSETS[i][2];
            costs[i] = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        NEIGHBOUR_COSTS = costs;
    }
}
