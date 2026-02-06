package net.citizensnpcs.api.hpastar;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

public class AStarSolution {
    final float cost;
    private final List<ReversableAStarNode> path;

    public AStarSolution(List<ReversableAStarNode> path, float cost) {
        this.path = path;
        this.cost = cost;
    }

    public Collection<Vector> convertToVectors() {
        if (path == null || path.isEmpty())
            return Collections.emptyList();
        return Lists.transform(path, input -> {
            HPAGraphNode node = ((HPAGraphAStarNode) input).node;
            return new Vector(node.x, node.y, node.z);
        });
    }
}
