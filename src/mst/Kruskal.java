package mst;

import dsu.DisjointSet;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

public class Kruskal {

    public static MstResult run(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Kruskal needs an undirected graph");
        }
        int vertexCount = graph.getVertexCount();

        List<Edge> sortedEdges = new ArrayList<>(graph.getEdges());
        long[] comparisons = {0};
        sortedEdges.sort((a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.weight, b.weight);
        });

        DisjointSet dsu = new DisjointSet(vertexCount);
        List<Edge> chosen = new ArrayList<>();
        long totalWeight = 0;
        long edgesExamined = 0;

        for (Edge edge : sortedEdges) {
            if (chosen.size() == vertexCount - 1) {
                break;
            }
            edgesExamined++;
            if (dsu.union(edge.from, edge.to)) {
                chosen.add(edge);
                totalWeight += edge.weight;
            }
        }

        int components = dsu.getSetCount();
        MstResult result = new MstResult(totalWeight, chosen, components == 1, components);
        result.setCounters(edgesExamined, comparisons[0], dsu.getFindCalls(), dsu.getUnionCalls(), 0, 0);
        return result;
    }
}