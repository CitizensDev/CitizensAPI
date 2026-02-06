package net.citizensnpcs.api.hpastar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.util.Vector;

public class HPAGraphNode {
    final List<List<HPAGraphEdge>> edges = new ArrayList<>();
    final int x;
    final int y;
    final int z;

    public HPAGraphNode(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void connect(int level, HPAGraphNode to, HPAGraphEdge.EdgeType type, float weight) {
        while (level >= edges.size()) {
            edges.add(new ArrayList<>());
        }
        while (level >= to.edges.size()) {
            to.edges.add(new ArrayList<>());
        }
        edges.get(level).add(new HPAGraphEdge(this, to, type, weight));
        to.edges.get(level).add(new HPAGraphEdge(to, this, type, weight));
    }

    public double distance(HPAGraphNode dest) {
        int dx = x - dest.x;
        int dy = y - dest.y;
        int dz = z - dest.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        HPAGraphNode other = (HPAGraphNode) obj;
        return x == other.x && y == other.y && z == other.z;
    }

    public List<HPAGraphEdge> getEdges(int level) {
        if (level >= edges.size())
            return Collections.emptyList();
        return edges.get(level);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * (31 + x) + y) + z;
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }
}
