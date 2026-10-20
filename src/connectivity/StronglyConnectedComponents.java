package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StronglyConnectedComponents {
    private final Graph graph;
    private final boolean[] visited;
    private final List<Integer> finishOrder = new ArrayList<>();
    private final int[] componentOf;
    private final List<List<Integer>> components = new ArrayList<>();
    private long edgesExamined = 0;

    private StronglyConnectedComponents(Graph graph) {
        this.graph = graph;
        this.visited = new boolean[graph.getVertexCount()];
        this.componentOf = new int[graph.getVertexCount()];
    }

    public static SccResult run(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC needs a directed graph");
        }
        return new StronglyConnectedComponents(graph).compute();
    }

    private SccResult compute() {
        int vertexCount = graph.getVertexCount();

        for (int v = 0; v < vertexCount; v++) {
            if (!visited[v]) {
                firstPass(v);
            }
        }

        Graph reversed = graph.reversed();
        Arrays.fill(visited, false);
        for (int i = vertexCount - 1; i >= 0; i--) {
            int start = finishOrder.get(i);
            if (!visited[start]) {
                List<Integer> component = new ArrayList<>();
                secondPass(reversed, start, component, components.size());
                components.add(component);
            }
        }

        return new SccResult(components, componentOf, edgesExamined);
    }

    private void firstPass(int vertex) {
        visited[vertex] = true;
        for (Edge edge : graph.getNeighbors(vertex)) {
            edgesExamined++;
            if (!visited[edge.to]) {
                firstPass(edge.to);
            }
        }
        finishOrder.add(vertex);
    }

    private void secondPass(Graph reversed, int vertex, List<Integer> component, int componentId) {
        visited[vertex] = true;
        component.add(vertex);
        componentOf[vertex] = componentId;
        for (Edge edge : reversed.getNeighbors(vertex)) {
            edgesExamined++;
            if (!visited[edge.to]) {
                secondPass(reversed, edge.to, component, componentId);
            }
        }
    }
}