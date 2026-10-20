package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class TopologicalSort {

    public static TopologicalResult run(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort needs a directed graph");
        }
        int vertexCount = graph.getVertexCount();
        long edgesExamined = 0;

        int[] inDegree = new int[vertexCount];
        for (Edge edge : graph.getEdges()) {
            inDegree[edge.to]++;
            edgesExamined++;
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < vertexCount; v++) {
            if (inDegree[v] == 0) {
                queue.add(v);
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            order.add(vertex);
            for (Edge edge : graph.getNeighbors(vertex)) {
                edgesExamined++;
                inDegree[edge.to]--;
                if (inDegree[edge.to] == 0) {
                    queue.add(edge.to);
                }
            }
        }

        if (order.size() == vertexCount) {
            return new TopologicalResult(order, new ArrayList<>(), edgesExamined);
        }

        List<Integer> blocked = new ArrayList<>();
        for (int v = 0; v < vertexCount; v++) {
            if (inDegree[v] > 0) {
                blocked.add(v);
            }
        }
        return new TopologicalResult(new ArrayList<>(), blocked, edgesExamined);
    }
}