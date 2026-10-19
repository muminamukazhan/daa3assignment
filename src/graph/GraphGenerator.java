package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GraphGenerator {
    public static final int MIN_WEIGHT = 1;
    public static final int MAX_WEIGHT = 100;

    public static Graph connectedUndirected(int vertices, int edges, long seed) {
        checkSize(vertices, edges, false, vertices - 1);
        Random random = new Random(seed);
        Graph graph = new Graph(vertices, false);

        List<Integer> order = shuffledVertices(vertices, random);
        for (int i = 1; i < vertices; i++) {
            int parent = order.get(random.nextInt(i));
            graph.addEdge(order.get(i), parent, randomWeight(random));
        }

        addRandomEdges(graph, edges, random);
        return graph;
    }

    public static Graph randomUndirected(int vertices, int edges, long seed) {
        checkSize(vertices, edges, false, 0);
        Random random = new Random(seed);
        Graph graph = new Graph(vertices, false);
        addRandomEdges(graph, edges, random);
        return graph;
    }

    public static Graph randomDirected(int vertices, int edges, long seed) {
        checkSize(vertices, edges, true, 0);
        Random random = new Random(seed);
        Graph graph = new Graph(vertices, true);
        addRandomEdges(graph, edges, random);
        return graph;
    }

    public static Graph directedAcyclic(int vertices, int edges, long seed) {
        checkSize(vertices, edges, false, vertices - 1);
        Random random = new Random(seed);
        List<Integer> order = shuffledVertices(vertices, random);
        return buildDagFromOrder(order, edges, random);
    }

    public static Graph directedWithOneCycle(int vertices, int edges, long seed) {
        if (vertices < 2) {
            throw new IllegalArgumentException("A cycle needs at least 2 vertices");
        }
        checkSize(vertices, edges - 1, false, vertices - 1);
        Random random = new Random(seed);
        List<Integer> order = shuffledVertices(vertices, random);
        Graph graph = buildDagFromOrder(order, edges - 1, random);
        graph.addEdge(order.get(vertices - 1), order.get(0), randomWeight(random));
        return graph;
    }

    private static Graph buildDagFromOrder(List<Integer> order, int edges, Random random) {
        int vertices = order.size();
        Graph graph = new Graph(vertices, true);

        for (int i = 0; i + 1 < vertices; i++) {
            graph.addEdge(order.get(i), order.get(i + 1), randomWeight(random));
        }

        while (graph.getEdgeCount() < edges) {
            int a = random.nextInt(vertices);
            int b = random.nextInt(vertices);
            if (a == b) {
                continue;
            }
            int earlier = Math.min(a, b);
            int later = Math.max(a, b);
            graph.addEdge(order.get(earlier), order.get(later), randomWeight(random));
        }
        return graph;
    }

    private static void addRandomEdges(Graph graph, int targetEdges, Random random) {
        int vertices = graph.getVertexCount();
        while (graph.getEdgeCount() < targetEdges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);
            if (from != to) {
                graph.addEdge(from, to, randomWeight(random));
            }
        }
    }

    private static List<Integer> shuffledVertices(int vertices, Random random) {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            order.add(i);
        }
        Collections.shuffle(order, random);
        return order;
    }

    private static int randomWeight(Random random) {
        return MIN_WEIGHT + random.nextInt(MAX_WEIGHT - MIN_WEIGHT + 1);
    }

    private static void checkSize(int vertices, int edges, boolean directed, int minEdges) {
        if (vertices < 1) {
            throw new IllegalArgumentException("Need at least 1 vertex");
        }
        int maxEdges = Graph.maxEdges(vertices, directed);
        if (edges < minEdges || edges > maxEdges) {
            throw new IllegalArgumentException("For " + vertices + " vertices the number of edges must be between "
                    + minEdges + " and " + maxEdges + ", but was " + edges);
        }
    }
}