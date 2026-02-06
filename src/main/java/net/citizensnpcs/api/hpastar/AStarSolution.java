package net.citizensnpcs.api.hpastar;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.util.Vector;

public class AStarSolution {
    final float cost;
    final List<HPAGraphAStarNode> path;

    public AStarSolution(List<HPAGraphAStarNode> path, float cost) {
        this.path = path;
        this.cost = cost;
    }

    public Collection<Vector> convertToVectors() {
        if (path == null || path.isEmpty())
            return Collections.emptyList();
        List<Vector> vectors = new ArrayList<>(path.size());
        for (HPAGraphAStarNode input : path) {
            HPAGraphNode node = input.node;
            vectors.add(new Vector(node.x, node.y, node.z));
        }
        return vectors;
    }
}
