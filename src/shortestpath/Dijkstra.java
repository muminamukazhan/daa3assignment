package shortestpath;

import graph.Edge;
import graph.Graph;

import java.util.Arrays;
import java.util.PriorityQueue;

public class Dijkstra {

    public static ShortestPathResult run(Graph graph, int source) {
        int vertexCount = graph.getVertexCount();
        if (source < 0 || source >= vertexCount) {
            throw new IllegalArgumentException("Source " + source + " is not in 0.." + (vertexCount - 1));
        }
        for (Edge edge : graph.getEdges()) {
            if (edge.weight < 0) {
                throw new IllegalArgumentException(
                        "Dijkstra does not support negative edge weights, but found " + edge);
            }
        }

        long[] distance = new long[vertexCount];
        Arrays.fill(distance, ShortestPathResult.UNREACHABLE);
        int[] parent = new int[vertexCount];
        Arrays.fill(parent, -1);
        boolean[] settled = new boolean[vertexCount];

        long[] comparisons = {0};
        PriorityQueue<QueueEntry> heap = new PriorityQueue<>((a, b) -> {
            comparisons[0]++;
            return Long.compare(a.distance, b.distance);
        });

        long edgesExamined = 0;
        long relaxations = 0;
        long heapPushes = 0;
        long heapPolls = 0;

        distance[source] = 0;
        heap.add(new QueueEntry(source, 0));
        heapPushes++;

        while (!heap.isEmpty()) {
            QueueEntry entry = heap.poll();
            heapPolls++;
            int vertex = entry.vertex;
            if (settled[vertex]) {
                continue;
            }
            settled[vertex] = true;

            for (Edge edge : graph.getNeighbors(vertex)) {
                edgesExamined++;
                long candidate = distance[vertex] + edge.weight;
                if (candidate < distance[edge.to]) {
                    distance[edge.to] = candidate;
                    parent[edge.to] = vertex;
                    heap.add(new QueueEntry(edge.to, candidate));
                    heapPushes++;
                    relaxations++;
                }
            }
        }

        ShortestPathResult result = new ShortestPathResult(source, distance, parent);
        result.setCounters(edgesExamined, relaxations, heapPushes, heapPolls, comparisons[0]);
        return result;
    }

    private static class QueueEntry {
        final int vertex;
        final long distance;

        QueueEntry(int vertex, long distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }
}