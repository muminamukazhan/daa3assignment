package tests;
import connectivity.SccResult;
import connectivity.StronglyConnectedComponents;
import connectivity.TopologicalResult;
import connectivity.TopologicalSort;
import dsu.DisjointSet;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MstResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.ShortestPathResult;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class AllTests {
    private static int checks = 0;
    static final int[][] SIZES = {{10, 30}, {100, 500}, {1000, 5000}};
    static final long INF = ShortestPathResult.UNREACHABLE;

    public static void main(String[] args) {
        testGraph();
        testGenerators();
        testDsu();
        testMstSpecialGraphs();
        testMstDisconnected();
        testMstRandom();
        testTopologicalSort();
        testGeneratedDagAndCycle();
        testScc();
        testSccRandom();
        testDijkstraKnownGraph();
        testDijkstraAgainstBellmanFord();
        testNegativeWeights();
        System.out.println("All " + checks + " checks passed.");
    }

    static Graph graph(int n, boolean directed, int[]... edges) {
        Graph g = new Graph(n, directed);
        for (int[] e : edges) { g.addEdge(e[0], e[1], e.length > 2 ? e[2] : 1); }
        return g;
    }

    static void testGraph() {
        Graph u = graph(4, false, new int[]{0, 1, 5}, new int[]{1, 2, 3});
        check(!u.addEdge(1, 0, 9), "same undirected edge in the other direction is a duplicate");
        check(u.getEdgeCount() == 2 && u.getNeighbors(1).size() == 2, "edge counted once, stored at both ends");
        check(u.hasEdge(2, 1) && u.getNeighbors(3).isEmpty(), "hasEdge works both ways, isolated vertex");
        Graph d = graph(3, true, new int[]{0, 1}, new int[]{1, 0});
        check(d.getEdgeCount() == 2 && !d.addEdge(0, 1, 7) && !d.hasEdge(0, 2), "directed edges are separate");
        check(throwsIllegalArgument(() -> u.addEdge(1, 1, 1)), "self-loop rejected");
        check(throwsIllegalArgument(() -> u.addEdge(0, 4, 1)), "vertex out of range rejected");
        check(throwsIllegalArgument(() -> new Graph(0, false)), "graph with 0 vertices rejected");
        check(new Graph(1, false).getEdgeCount() == 0, "single-vertex graph");
        Graph r = graph(3, true, new int[]{0, 1, 5}, new int[]{1, 2, 7}).reversed();
        check(r.hasEdge(1, 0) && r.hasEdge(2, 1) && !r.hasEdge(0, 1), "reversed edges point back");
        check(r.getNeighbors(2).get(0).weight == 7, "reversed keeps weights");
        check(throwsIllegalState(() -> u.reversed()), "undirected graph cannot be reversed");
    }

    static void testGenerators() {
        for (int[] s : SIZES) {
            Graph g = GraphGenerator.connectedUndirected(s[0], s[1], 42);
            check(g.getVertexCount() == s[0] && g.getEdgeCount() == s[1] && !g.isDirected(), "undirected size, V=" + s[0]);
            check(countComponents(g) == 1, "generated undirected graph is connected, V=" + s[0]);
            check(GraphGenerator.randomDirected(s[0], s[1], 42).getEdgeCount() == s[1], "random directed edge count");
            for (Edge e : g.getEdges()) {
                check(e.weight >= GraphGenerator.MIN_WEIGHT && e.weight <= GraphGenerator.MAX_WEIGHT, "weight in range");
            }
        }
        String a = GraphGenerator.connectedUndirected(100, 500, 42).getEdges().toString();
        check(a.equals(GraphGenerator.connectedUndirected(100, 500, 42).getEdges().toString()), "same seed, same graph");
        check(!a.equals(GraphGenerator.connectedUndirected(100, 500, 43).getEdges().toString()), "other seed, other graph");
        check(GraphGenerator.connectedUndirected(1, 0, 1).getVertexCount() == 1, "generator makes a single vertex");
    }

    static void testDsu() {
        DisjointSet dsu = new DisjointSet(5);
        check(dsu.getSetCount() == 5 && dsu.find(3) == 3, "5 separate sets at the start");
        check(dsu.union(0, 1) && !dsu.union(1, 0), "union is true for a new merge, false for the same set");
        dsu.union(2, 3);
        dsu.union(1, 3);
        check(dsu.connected(0, 2) && !dsu.connected(0, 4), "connectivity after merges");
        check(dsu.getSetCount() == 2 && dsu.getSuccessfulUnions() == 3 && dsu.getUnionCalls() == 4, "set count and counters");
        dsu.resetCounters();
        check(dsu.getFindCalls() == 0, "counters reset");

        int n = 1000;
        DisjointSet plain = new DisjointSet(n, false, false);
        DisjointSet fast = new DisjointSet(n, true, true);
        DisjointSet compression = new DisjointSet(n, true, false);
        for (int i = 0; i + 1 < n; i++) {
            plain.union(i, i + 1);
            fast.union(i, i + 1);
            compression.union(i, i + 1);
        }
        plain.resetCounters();
        fast.resetCounters();
        plain.find(0);
        fast.find(0);
        check(plain.getParentSteps() > 900, "without optimizations find walks a long chain");
        check(fast.getParentSteps() <= 3, "with optimizations find is short");
        compression.find(0);
        compression.resetCounters();
        compression.find(0);
        check(compression.getParentSteps() <= 1, "after path compression the next find takes 1 step");
    }

    static void testMstSpecialGraphs() {
        Graph example = graph(5, false, new int[]{0, 1, 1}, new int[]{0, 2, 4}, new int[]{1, 2, 2}, new int[]{1, 3, 6},
                new int[]{2, 3, 3}, new int[]{3, 4, 5}, new int[]{2, 4, 7});
        Graph square = graph(4, false, new int[]{0, 1}, new int[]{1, 2}, new int[]{2, 3}, new int[]{3, 0});
        Graph k4 = new Graph(4, false);
        for (int a = 0; a < 4; a++) {
            for (int b = a + 1; b < 4; b++) { k4.addEdge(a, b, 7); }
        }
        for (MstResult r : both(example)) {
            check(r.getTotalWeight() == 11 && r.getEdges().size() == 4, "example: weight 11 with V-1 edges");
            check(r.isConnected() && validForest(example, r), "example: connected, valid tree");
        }
        for (MstResult r : both(square)) { check(r.getTotalWeight() == 3 && validForest(square, r), "multiple valid MSTs: weight 3"); }
        for (MstResult r : both(k4)) { check(r.getTotalWeight() == 21 && validForest(k4, r), "duplicate weights: K4 of 7s gives 21"); }
        for (MstResult r : both(new Graph(1, false))) {
            check(r.isConnected() && r.getTotalWeight() == 0 && r.getEdges().isEmpty(), "single vertex: connected, weight 0");
        }
        Graph directed = new Graph(3, true);
        check(throwsIllegalArgument(() -> Kruskal.run(directed)), "Kruskal rejects a directed graph");
        check(throwsIllegalArgument(() -> Prim.run(directed)), "Prim rejects a directed graph");
    }

    static void testMstDisconnected() {
        Graph g = graph(6, false, new int[]{0, 1, 1}, new int[]{1, 2, 2}, new int[]{0, 2, 3}, new int[]{3, 4, 5});
        for (MstResult r : both(g)) {
            check(!r.isConnected() && r.getComponents() == 3, "disconnected graph detected: 3 components");
            check(r.getTotalWeight() == 8 && r.getEdges().size() == 3 && validForest(g, r), "forest weight 1+2+5 = 8");
        }
        int disconnected = 0;
        for (long seed = 1; seed <= 30; seed++) {
            Graph h = GraphGenerator.randomUndirected(50, 40, seed);
            MstResult k = Kruskal.run(h);
            MstResult p = Prim.run(h);
            check(k.getTotalWeight() == p.getTotalWeight() && k.getComponents() == p.getComponents(), "forests agree, seed " + seed);
            check(k.getComponents() == countComponents(h) && validForest(h, k) && validForest(h, p), "forests valid, seed " + seed);
            if (!k.isConnected()) { disconnected++; }
        }
        check(disconnected > 0, "random test covered disconnected graphs");
    }

    static void testMstRandom() {
        for (int[] s : SIZES) {
            for (long seed = 1; seed <= 5; seed++) {
                Graph g = GraphGenerator.connectedUndirected(s[0], s[1], seed);
                MstResult k = Kruskal.run(g);
                MstResult p = Prim.run(g);
                String where = "V=" + s[0] + ", seed " + seed;
                check(k.getTotalWeight() == p.getTotalWeight(), "Kruskal weight == Prim weight, " + where);
                check(k.isConnected() && p.isConnected() && k.getEdges().size() == s[0] - 1, "connected, V-1 edges, " + where);
                check(validForest(g, k) && validForest(g, p), "both trees valid, " + where);
            }
        }
        Graph g = GraphGenerator.connectedUndirected(100, 500, 42);
        MstResult k = Kruskal.run(g);
        MstResult p = Prim.run(g);
        check(k.getEdgesExamined() >= 99 && k.getEdgesExamined() <= 500 && k.getComparisons() > 0, "Kruskal counters");
        check(k.getDsuFinds() == 2 * k.getDsuUnions(), "each union calls find twice");
        check(p.getEdgesExamined() == 1000 && p.getHeapPolls() == p.getHeapPushes(), "Prim counters: 2E, polls == pushes");
    }

    static void testTopologicalSort() {
        Graph diamond = graph(4, true, new int[]{0, 1}, new int[]{0, 2}, new int[]{1, 3}, new int[]{2, 3});
        TopologicalResult r = TopologicalSort.run(diamond);
        check(r.isDag() && validTopologicalOrder(diamond, r.getOrder()), "diamond has a valid order");
        Graph chain = graph(5, true, new int[]{4, 3}, new int[]{3, 2}, new int[]{2, 1}, new int[]{1, 0});
        check(TopologicalSort.run(chain).getOrder().equals(Arrays.asList(4, 3, 2, 1, 0)), "chain order is 4 3 2 1 0");
        Graph parts = graph(6, true, new int[]{0, 1}, new int[]{2, 3});
        check(validTopologicalOrder(parts, TopologicalSort.run(parts).getOrder()), "disconnected DAG gets a valid order");

        Graph cycle = graph(4, true, new int[]{0, 1}, new int[]{1, 2}, new int[]{2, 1}, new int[]{2, 3});
        TopologicalResult c = TopologicalSort.run(cycle);
        check(!c.isDag() && c.getOrder().isEmpty(), "cycle detected, no order returned");
        check(c.getBlockedVertices().equals(Arrays.asList(1, 2, 3)), "blocked: the cycle and the vertex below it");
        check(!TopologicalSort.run(graph(3, true, new int[]{0, 1}, new int[]{1, 2}, new int[]{2, 0})).isDag(), "3-cycle detected");
        check(TopologicalSort.run(new Graph(1, true)).getOrder().equals(Arrays.asList(0)), "single vertex: order [0]");
        check(throwsIllegalArgument(() -> TopologicalSort.run(new Graph(3, false))), "undirected graph rejected");
        check(TopologicalSort.run(GraphGenerator.directedAcyclic(100, 500, 42)).getEdgesExamined() == 1000, "DAG examines 2E edges");
    }

    static void testGeneratedDagAndCycle() {
        for (int[] s : SIZES) {
            for (long seed : new long[]{1, 2, 3, 42}) {
                String where = "V=" + s[0] + ", seed " + seed;
                Graph dag = GraphGenerator.directedAcyclic(s[0], s[1], seed);
                TopologicalResult r = TopologicalSort.run(dag);
                check(dag.getEdgeCount() == s[1] && r.isDag() && validTopologicalOrder(dag, r.getOrder()), "generated DAG is acyclic, " + where);
                check(StronglyConnectedComponents.run(dag).getComponentCount() == s[0], "DAG: every vertex is its own SCC, " + where);
                Graph cyclic = GraphGenerator.directedWithOneCycle(s[0], s[1], seed);
                check(cyclic.getEdgeCount() == s[1] && !TopologicalSort.run(cyclic).isDag(), "generated graph is cyclic, " + where);
                check(StronglyConnectedComponents.run(cyclic).isStronglyConnected(), "back edge makes one big SCC, " + where);
            }
        }
    }

    static void testScc() {
        Graph g = graph(8, true, new int[]{0, 1}, new int[]{1, 2}, new int[]{2, 0}, new int[]{2, 3}, new int[]{3, 4},
                new int[]{4, 5}, new int[]{5, 3}, new int[]{5, 6}, new int[]{6, 7});
        SccResult r = StronglyConnectedComponents.run(g);
        check(r.getComponentCount() == 4 && r.getLargestComponentSize() == 3, "4 components, largest has 3 vertices");
        check(r.getComponentOf(0) == r.getComponentOf(2) && r.getComponentOf(3) == r.getComponentOf(5), "{0,1,2} and {3,4,5} together");
        check(r.getComponentOf(2) != r.getComponentOf(3) && r.getComponentOf(6) != r.getComponentOf(7), "other vertices apart");
        check(partitionOk(g, r), "every vertex is in exactly one component");
        check(StronglyConnectedComponents.run(new Graph(1, true)).isStronglyConnected(), "single vertex is one component");
        check(StronglyConnectedComponents.run(new Graph(4, true)).getComponentCount() == 4, "no edges: every vertex alone");
        Graph ring = graph(5, true, new int[]{0, 1}, new int[]{1, 2}, new int[]{2, 3}, new int[]{3, 4}, new int[]{4, 0});
        Graph line = graph(5, true, new int[]{0, 1}, new int[]{1, 2}, new int[]{2, 3}, new int[]{3, 4});
        check(StronglyConnectedComponents.run(ring).isStronglyConnected(), "ring is one component");
        check(StronglyConnectedComponents.run(line).getComponentCount() == 5, "one-way line gives 5 components");
        check(throwsIllegalArgument(() -> StronglyConnectedComponents.run(new Graph(3, false))), "undirected graph rejected");
    }

    static void testSccRandom() {
        for (long seed = 1; seed <= 30; seed++) {
            Graph g = GraphGenerator.randomDirected(12, 20, seed);
            SccResult r = StronglyConnectedComponents.run(g);
            boolean[][] reach = reachability(g);
            boolean ok = partitionOk(g, r);
            for (int u = 0; u < 12; u++) {
                for (int v = 0; v < 12; v++) { ok &= (reach[u][v] && reach[v][u]) == (r.getComponentOf(u) == r.getComponentOf(v)); }
            }
            check(ok, "SCC matches mutual reachability, seed " + seed);
        }
        for (int[] s : SIZES) {
            Graph g = GraphGenerator.randomDirected(s[0], s[1], 42);
            SccResult r = StronglyConnectedComponents.run(g);
            check(partitionOk(g, r) && r.getEdgesExamined() == 2L * s[1], "partition valid, 2E edges, V=" + s[0]);
        }
    }

    static void testDijkstraKnownGraph() {
        Graph g = graph(7, true, new int[]{0, 1, 7}, new int[]{0, 2, 9}, new int[]{0, 5, 14}, new int[]{1, 2, 10},
                new int[]{1, 3, 15}, new int[]{2, 3, 11}, new int[]{2, 5, 2}, new int[]{3, 4, 6}, new int[]{5, 4, 9});
        ShortestPathResult r = Dijkstra.run(g, 0);
        long[] expected = {0, 7, 9, 20, 20, 11, INF};
        for (int v = 0; v < 7; v++) { check(r.getDistance(v) == expected[v], "distance to " + v); }
        check(r.getPath(5).equals(Arrays.asList(0, 2, 5)), "path to 5 avoids the direct edge of weight 14");
        check(r.getPath(4).equals(Arrays.asList(0, 2, 5, 4)), "path to 4");
        check(r.getPath(0).equals(Arrays.asList(0)), "path to the source is [source]");
        check(!r.isReachable(6) && r.getPath(6).isEmpty() && r.getParent(6) == -1, "unreachable vertex: empty path, no parent");
        check(r.getReachableCount() == 6 && Dijkstra.run(g, 3).getReachableCount() == 2, "reachable counts");
        check(Dijkstra.run(g, 4).getReachableCount() == 1, "source without outgoing edges reaches only itself");
        ShortestPathResult t = Dijkstra.run(graph(4, false, new int[]{0, 1, 3}), 0);
        check(t.getDistance(1) == 3 && !t.isReachable(2) && !t.isReachable(3), "disconnected undirected graph");
        check(Dijkstra.run(new Graph(1, false), 0).getPath(0).equals(Arrays.asList(0)), "single vertex");
        check(Dijkstra.run(graph(3, true, new int[]{0, 1, 0}, new int[]{1, 2, 0}), 0).getDistance(2) == 0, "zero weights are allowed");
        check(throwsIllegalArgument(() -> Dijkstra.run(g, 7)) && throwsIllegalArgument(() -> r.getDistance(-1)), "bad source or vertex rejected");
    }

    static void testDijkstraAgainstBellmanFord() {
        for (boolean directed : new boolean[]{true, false}) {
            for (int[] s : SIZES) {
                for (long seed = 1; seed <= 5; seed++) {
                    Graph g = directed ? GraphGenerator.randomDirected(s[0], s[1], seed) : GraphGenerator.randomUndirected(s[0], s[1], seed);
                    for (int source : new int[]{0, s[0] - 1}) {
                        ShortestPathResult r = Dijkstra.run(g, source);
                        long[] dist = new long[s[0]];
                        boolean paths = true;
                        long outEdges = 0;
                        for (int v = 0; v < s[0]; v++) {
                            dist[v] = r.getDistance(v);
                            paths &= pathIsValid(g, r, v);
                            outEdges += r.isReachable(v) ? g.getNeighbors(v).size() : 0;
                        }
                        String where = "directed=" + directed + ", V=" + s[0] + ", seed " + seed + ", source " + source;
                        check(Arrays.equals(dist, bellmanFord(g, source)), "Dijkstra == Bellman-Ford, " + where);
                        check(paths, "every path is real and adds up to its distance, " + where);
                        check(r.getEdgesExamined() == outEdges && r.getHeapPushes() == r.getHeapPolls(), "counters, " + where);
                    }
                }
            }
        }
    }

    static void testNegativeWeights() {
        Graph g = graph(4, true, new int[]{0, 1, 4}, new int[]{0, 2, 5}, new int[]{2, 1, -3}, new int[]{1, 3, 1});
        check(throwsIllegalArgument(() -> Dijkstra.run(g, 0)), "Dijkstra rejects negative weights");
        check(Arrays.equals(bellmanFord(g, 0), new long[]{0, 2, 5, 3}), "correct distances: 0->2->1 costs 2");
    }

    static MstResult[] both(Graph g) { return new MstResult[]{Kruskal.run(g), Prim.run(g)}; }

    static boolean validForest(Graph g, MstResult result) {
        DisjointSet dsu = new DisjointSet(g.getVertexCount());
        long sum = 0;
        for (Edge e : result.getEdges()) {
            if (!g.hasEdge(e.from, e.to) || !dsu.union(e.from, e.to)) { return false; }
            sum += e.weight;
        }
        return sum == result.getTotalWeight() && dsu.getSetCount() == countComponents(g);
    }

    static int countComponents(Graph g) {
        DisjointSet dsu = new DisjointSet(g.getVertexCount());
        for (Edge e : g.getEdges()) { dsu.union(e.from, e.to); }
        return dsu.getSetCount();
    }

    static boolean validTopologicalOrder(Graph g, List<Integer> order) {
        int n = g.getVertexCount();
        int[] position = new int[n];
        Arrays.fill(position, -1);
        if (order.size() != n) { return false; }
        for (int i = 0; i < n; i++) {
            int v = order.get(i);
            if (v < 0 || v >= n || position[v] != -1) { return false; }
            position[v] = i;
        }
        for (Edge e : g.getEdges()) {
            if (position[e.from] >= position[e.to]) { return false; }
        }
        return true;
    }

    static boolean partitionOk(Graph g, SccResult r) {
        int[] seen = new int[g.getVertexCount()];
        for (int id = 0; id < r.getComponents().size(); id++) {
            for (int v : r.getComponents().get(id)) {
                seen[v]++;
                if (r.getComponentOf(v) != id) { return false; }
            }
        }
        for (int count : seen) {
            if (count != 1) { return false; }
        }
        return true;
    }

    static boolean[][] reachability(Graph g) {
        int n = g.getVertexCount();
        boolean[][] reach = new boolean[n][n];
        for (int start = 0; start < n; start++) {
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(start);
            reach[start][start] = true;
            while (!queue.isEmpty()) {
                for (Edge e : g.getNeighbors(queue.remove())) {
                    if (!reach[start][e.to]) { reach[start][e.to] = true; queue.add(e.to); }
                }
            }
        }
        return reach;
    }

    static long[] bellmanFord(Graph g, int source) {
        int n = g.getVertexCount();
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        dist[source] = 0;
        for (int round = 0; round < n; round++) {
            for (int u = 0; u < n; u++) {
                if (dist[u] == INF) { continue; }
                for (Edge e : g.getNeighbors(u)) { dist[e.to] = Math.min(dist[e.to], dist[u] + e.weight); }
            }
        }
        return dist;
    }

    static boolean pathIsValid(Graph g, ShortestPathResult r, int target) {
        List<Integer> path = r.getPath(target);
        if (!r.isReachable(target)) { return path.isEmpty(); }
        if (path.isEmpty() || path.get(0) != r.getSource() || path.get(path.size() - 1) != target) { return false; }
        long sum = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            long best = Long.MAX_VALUE;
            for (Edge e : g.getNeighbors(path.get(i))) {
                if (e.to == path.get(i + 1)) { best = Math.min(best, e.weight); }
            }
            if (best == Long.MAX_VALUE) { return false; }
            sum += best;
        }
        return sum == r.getDistance(target);
    }

    static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError("FAILED: " + message); }
        checks++;
    }

    static boolean throwsIllegalArgument(Runnable action) {
        try { action.run(); return false; } catch (IllegalArgumentException e) { return true; }
    }

    static boolean throwsIllegalState(Runnable action) {
        try { action.run(); return false; } catch (IllegalStateException e) { return true; }
    }
}