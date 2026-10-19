package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Graph {
    private final int vertexCount;
    private final boolean directed;
    private final List<List<Edge>> adjacency = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Set<Long> edgeKeys = new HashSet<>();

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 1) {
            throw new IllegalArgumentException("A graph needs at least 1 vertex");
        }
        this.vertexCount = vertexCount;
        this.directed = directed;
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public boolean addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        if (from == to) {
            throw new IllegalArgumentException("Self-loops are not allowed: " + from);
        }
        if (hasEdge(from, to)) {
            return false;
        }

        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        edgeKeys.add(key(from, to));
        adjacency.get(from).add(edge);

        if (!directed) {
            adjacency.get(to).add(new Edge(to, from, weight));
            edgeKeys.add(key(to, from));
        }
        return true;
    }

    public boolean hasEdge(int from, int to) {
        return edgeKeys.contains(key(from, to));
    }

    public List<Edge> getNeighbors(int vertex) {
        checkVertex(vertex);
        return adjacency.get(vertex);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public Graph reversed() {
        if (!directed) {
            throw new IllegalStateException("Only a directed graph can be reversed");
        }
        Graph result = new Graph(vertexCount, true);
        for (Edge edge : edges) {
            result.addEdge(edge.to, edge.from, edge.weight);
        }
        return result;
    }

    public static int maxEdges(int vertexCount, boolean directed) {
        long pairs = (long) vertexCount * (vertexCount - 1);
        return (int) (directed ? pairs : pairs / 2);
    }

    private void checkVertex(int v) {
        if (v < 0 || v >= vertexCount) {
            throw new IllegalArgumentException("Vertex " + v + " is not in 0.." + (vertexCount - 1));
        }
    }

    private long key(int from, int to) {
        return (long) from * vertexCount + to;
    }
}