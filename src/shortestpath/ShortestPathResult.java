package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShortestPathResult {
    public static final long UNREACHABLE = Long.MAX_VALUE;

    private final int source;
    private final long[] distance;
    private final int[] parent;
    private final int reachableCount;

    private long edgesExamined;
    private long relaxations;
    private long heapPushes;
    private long heapPolls;
    private long comparisons;

    public ShortestPathResult(int source, long[] distance, int[] parent) {
        this.source = source;
        this.distance = distance;
        this.parent = parent;
        int count = 0;
        for (long d : distance) {
            if (d != UNREACHABLE) {
                count++;
            }
        }
        this.reachableCount = count;
    }

    void setCounters(long edgesExamined, long relaxations, long heapPushes, long heapPolls, long comparisons) {
        this.edgesExamined = edgesExamined;
        this.relaxations = relaxations;
        this.heapPushes = heapPushes;
        this.heapPolls = heapPolls;
        this.comparisons = comparisons;
    }

    public int getSource() {
        return source;
    }

    public boolean isReachable(int vertex) {
        checkVertex(vertex);
        return distance[vertex] != UNREACHABLE;
    }

    public long getDistance(int vertex) {
        checkVertex(vertex);
        return distance[vertex];
    }

    public int getParent(int vertex) {
        checkVertex(vertex);
        return parent[vertex];
    }

    public List<Integer> getPath(int target) {
        checkVertex(target);
        List<Integer> path = new ArrayList<>();
        if (distance[target] == UNREACHABLE) {
            return path;
        }
        for (int v = target; v != -1; v = parent[v]) {
            path.add(v);
        }
        Collections.reverse(path);
        return path;
    }

    public int getReachableCount() {
        return reachableCount;
    }

    public long getEdgesExamined() {
        return edgesExamined;
    }

    public long getRelaxations() {
        return relaxations;
    }

    public long getHeapPushes() {
        return heapPushes;
    }

    public long getHeapPolls() {
        return heapPolls;
    }

    public long getComparisons() {
        return comparisons;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= distance.length) {
            throw new IllegalArgumentException("Vertex " + vertex + " is not in 0.." + (distance.length - 1));
        }
    }
}