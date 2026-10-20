package mst;

import graph.Edge;

import java.util.List;

public class MstResult {
    private final long totalWeight;
    private final List<Edge> edges;
    private final boolean connected;
    private final int components;

    private long edgesExamined;
    private long comparisons;
    private long dsuFinds;
    private long dsuUnions;
    private long heapPushes;
    private long heapPolls;

    public MstResult(long totalWeight, List<Edge> edges, boolean connected, int components) {
        this.totalWeight = totalWeight;
        this.edges = edges;
        this.connected = connected;
        this.components = components;
    }

    void setCounters(long edgesExamined, long comparisons, long dsuFinds, long dsuUnions,
                     long heapPushes, long heapPolls) {
        this.edgesExamined = edgesExamined;
        this.comparisons = comparisons;
        this.dsuFinds = dsuFinds;
        this.dsuUnions = dsuUnions;
        this.heapPushes = heapPushes;
        this.heapPolls = heapPolls;
    }

    public long getTotalWeight() {
        return totalWeight;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getComponents() {
        return components;
    }

    public long getEdgesExamined() {
        return edgesExamined;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getDsuFinds() {
        return dsuFinds;
    }

    public long getDsuUnions() {
        return dsuUnions;
    }

    public long getHeapPushes() {
        return heapPushes;
    }

    public long getHeapPolls() {
        return heapPolls;
    }
}