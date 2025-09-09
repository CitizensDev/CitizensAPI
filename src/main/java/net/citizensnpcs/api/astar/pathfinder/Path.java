package net.citizensnpcs.api.astar.pathfinder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.api.npc.NPC;

public class Path implements Plan {
    private List<Block> blockList;
    private int index = 0;
    private final PathEntry[] path;

    public Path(Collection<Vector> vector) {
        this.path = Iterables.toArray(
                Iterables.transform(vector, input -> new PathEntry(input, Collections.<PathCallback> emptyList())),
                PathEntry.class);
    }

    Path(Iterable<VectorNode> unfiltered, Vector goal) {
        List<PathEntry> path = Lists.newArrayList();
        for (VectorNode node : unfiltered) {
            for (Vector vector : node.getPathVectors()) {
                path.add(new PathEntry(vector, node.callbacks));
            }
        }
        PathEntry goalEntry = new PathEntry(goal, path.get(path.size() - 1).callbacks);
        Vector last = path.get(path.size() - 1).vector;
        if (last.getBlockX() == goal.getBlockX() && last.getBlockY() == goal.getBlockY()
                && last.getBlockZ() == goal.getBlockZ()) {
            path.set(path.size() - 1, goalEntry);
        } else {
            path.add(goalEntry);
        }
        path = ramerDouglasPeucker(path, 0.75);

        this.path = path.toArray(new PathEntry[path.size()]);
    }

    public List<Block> getBlocks(World world) {
        return Arrays.asList(path).stream()
                .map(p -> world.getBlockAt(p.vector.getBlockX(), p.vector.getBlockY(), p.vector.getBlockZ()))
                .collect(Collectors.toList());
    }

    public Vector getCurrentVector() {
        return path[index].vector;
    }

    public Iterable<Vector> getPath() {
        return Iterables.transform(Arrays.asList(path), input -> input.vector);
    }

    @Override
    public boolean isComplete() {
        return index >= path.length;
    }

    public boolean isFinalEntry() {
        return index == path.length - 1;
    }

    public void run(NPC npc) {
        path[index].run(npc);
    }

    @Override
    public String toString() {
        return Arrays.toString(path);
    }

    @Override
    public void update(Agent agent) {
        if (isComplete())
            return;
        path[index++].onComplete((NPC) agent);
    }

    private class PathEntry {
        final List<PathCallback> callbacks;
        final Vector vector;

        private PathEntry(Vector vector, List<PathCallback> callbacks) {
            this.vector = vector;
            this.callbacks = callbacks;
        }

        public void onComplete(NPC npc) {
            if (callbacks == null)
                return;
            Block current = npc.getEntity().getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(),
                    vector.getBlockZ());
            for (PathCallback callback : callbacks) {
                callback.onReached(npc, current);
            }
        }

        public void run(final NPC npc) {
            if (callbacks == null)
                return;
            if (blockList == null) {
                blockList = Arrays.stream(path).map(input -> npc.getEntity().getWorld()
                        .getBlockAt(input.vector.getBlockX(), input.vector.getBlockY(), input.vector.getBlockZ()))
                        .collect(Collectors.toList());
            }
            Block current = npc.getEntity().getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(),
                    vector.getBlockZ());
            for (PathCallback callback : callbacks) {
                callback.run(npc, current, blockList, index);
            }
        }

        @Override
        public String toString() {
            return vector.toString();
        }
    }

    private static List<PathEntry> ramerDouglasPeucker(List<PathEntry> points, double epsilon) {
        if (points.size() < 3)
            return points;

        int n = points.size();
        boolean[] keep = new boolean[n];
        keep[0] = true;
        keep[n - 1] = true;

        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[] { 0, n - 1 });

        while (!stack.isEmpty()) {
            int[] range = stack.pop();
            int start = range[0];
            int end = range[1];

            double dmax = 0;
            int found = -1;

            Vector a = points.get(start).vector;
            Vector b = points.get(end).vector;

            double abx = b.getX() - a.getX();
            double aby = b.getY() - a.getY();
            double abz = b.getZ() - a.getZ();

            double length = abx * abx + aby * aby + abz * abz;

            for (int i = start + 1; i < end; i++) {
                Vector p = points.get(i).vector;
                double d;

                if (length < 1e-9) {
                    // Degenerate segment (a == b)
                    double dx = p.getX() - a.getX();
                    double dy = p.getY() - a.getY();
                    double dz = p.getZ() - a.getZ();
                    d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                } else {
                    double apx = p.getX() - a.getX();
                    double apy = p.getY() - a.getY();
                    double apz = p.getZ() - a.getZ();

                    double cx = apy * abz - apz * aby;
                    double cy = apz * abx - apx * abz;
                    double cz = apx * aby - apy * abx;

                    double crossProductLength = cx * cx + cy * cy + cz * cz;

                    if (crossProductLength <= epsilon * epsilon * length)
                        continue;

                    d = Math.sqrt(crossProductLength / length);
                }
                if (d > dmax) {
                    dmax = d;
                    found = i;
                }
            }
            if (dmax > epsilon && found != -1) {
                keep[found] = true;
                stack.push(new int[] { start, found });
                stack.push(new int[] { found, end });
            }
        }
        List<PathEntry> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (keep[i]) {
                result.add(points.get(i));
            }
        }
        return result;
    }
}