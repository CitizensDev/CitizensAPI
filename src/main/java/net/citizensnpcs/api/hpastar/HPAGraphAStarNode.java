package net.citizensnpcs.api.hpastar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HPAGraphAStarNode implements Comparable<HPAGraphAStarNode> {
    private final HPAGraphEdge edge;
    float g;
    float h;
    final HPAGraphNode node;
    HPAGraphAStarNode parent;

    public HPAGraphAStarNode(HPAGraphNode node, HPAGraphEdge edge) {
        this.node = node;
        this.edge = edge;
    }

    @Override
    public int compareTo(HPAGraphAStarNode o) {
        return Float.compare(g + h, o.g + o.h);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        HPAGraphAStarNode other = (HPAGraphAStarNode) obj;
        return node.x == other.node.x && node.y == other.node.y && node.z == other.node.z;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * (31 + node.x) + node.y) + node.z;
    }

    HPAGraphEdge getEdge() {
        return edge;
    }

    public List<HPAGraphAStarNode> reconstructSolution() {
        List<HPAGraphAStarNode> parents = new ArrayList<>();
        HPAGraphAStarNode start = this;
        while (start != null) {
            parents.add(start);
            start = start.parent;
        }
        Collections.reverse(parents);
        return parents;
    }

    @Override
    public String toString() {
        return (edge != null ? edge.from.toString() : "") + "->" + node.toString();
    }
}
