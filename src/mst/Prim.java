package mst;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    private final Graph graph;
    private final boolean[] visited;
    private final long[] comparisons = {0};
    private final PriorityQueue<Edge> heap;

    private long edgesExamined = 0;
    private long heapPushes = 0;
    private long heapPolls = 0;

    private Prim(Graph graph) {
        this.graph = graph;
        this.visited = new boolean[graph.getVertexCount()];
        this.heap = new PriorityQueue<>((a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.weight, b.weight);
        });
    }

    public static MstResult run(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Prim needs an undirected graph");
        }
        return new Prim(graph).compute();
    }

    private MstResult compute() {
        List<Edge> chosen = new ArrayList<>();
        long totalWeight = 0;
        int components = 0;

        for (int start = 0; start < graph.getVertexCount(); start++) {
            if (visited[start]) {
                continue;
            }
            components++;
            visit(start);

            while (!heap.isEmpty()) {
                Edge edge = heap.poll();
                heapPolls++;
                if (visited[edge.to]) {
                    continue;
                }
                chosen.add(edge);
                totalWeight += edge.weight;
                visit(edge.to);
            }
        }

        MstResult result = new MstResult(totalWeight, chosen, components == 1, components);
        result.setCounters(edgesExamined, comparisons[0], 0, 0, heapPushes, heapPolls);
        return result;
    }

    private void visit(int vertex) {
        visited[vertex] = true;
        for (Edge edge : graph.getNeighbors(vertex)) {
            edgesExamined++;
            if (!visited[edge.to]) {
                heap.add(edge);
                heapPushes++;
            }
        }
    }
}