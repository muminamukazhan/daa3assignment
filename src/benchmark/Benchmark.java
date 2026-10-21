package benchmark;

import connectivity.SccResult;
import connectivity.StronglyConnectedComponents;
import connectivity.TopologicalResult;
import connectivity.TopologicalSort;
import dsu.DisjointSet;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MstResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.ShortestPathResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Benchmark {
    static final int[][] SIZES = {{10, 30}, {100, 500}, {1000, 5000}};
    static final long SEED = 42;
    static final int WARMUP_RUNS = 1;
    static final int TIMED_RUNS = 5;
    static final int[] DSU_SIZES = {100, 1000};
    static final Path TABLES = Paths.get("results", "tables");

    private static volatile long sink;

    public static void main(String[] args) throws IOException {
        Files.createDirectories(TABLES);
        mstComparison();
        dsuOptimizations();
        sccGrowth();
        topologicalSort();
        dijkstraSizes();
        System.out.println("Tables written to " + TABLES.toAbsolutePath());
    }

    static void mstComparison() throws IOException {
        Csv csv = new Csv("V,E,algorithm,avg_time_us,total_weight,connected,"
                + "edges_examined,comparisons,dsu_finds,dsu_unions,heap_pushes,heap_polls");
        for (int[] size : SIZES) {
            Graph g = GraphGenerator.connectedUndirected(size[0], size[1], SEED);

            MstResult kruskal = Kruskal.run(g);
            double kruskalTime = averageMicros(() -> sink = Kruskal.run(g).getTotalWeight());
            csv.add(size[0], size[1], "Kruskal", fmt(kruskalTime), kruskal.getTotalWeight(),
                    kruskal.isConnected(), kruskal.getEdgesExamined(), kruskal.getComparisons(),
                    kruskal.getDsuFinds(), kruskal.getDsuUnions(),
                    kruskal.getHeapPushes(), kruskal.getHeapPolls());

            MstResult prim = Prim.run(g);
            double primTime = averageMicros(() -> sink = Prim.run(g).getTotalWeight());
            csv.add(size[0], size[1], "Prim", fmt(primTime), prim.getTotalWeight(),
                    prim.isConnected(), prim.getEdgesExamined(), prim.getComparisons(),
                    prim.getDsuFinds(), prim.getDsuUnions(),
                    prim.getHeapPushes(), prim.getHeapPolls());

            if (kruskal.getTotalWeight() != prim.getTotalWeight()) {
                throw new IllegalStateException("Kruskal and Prim disagree for V=" + size[0]);
            }
        }
        csv.write("mst_comparison.csv");
    }

    static long dsuChainWorkload(int n, boolean compression, boolean rank) {
        DisjointSet dsu = new DisjointSet(n, compression, rank);
        for (int i = 0; i + 1 < n; i++) {
            dsu.union(i, i + 1);
        }
        for (int i = 0; i < n; i++) {
            dsu.find(i);
        }
        return dsu.getParentSteps();
    }

    static void dsuOptimizations() throws IOException {
        Csv csv = new Csv("n,path_compression,union_by_rank,avg_time_us,parent_steps");
        boolean[] options = {false, true};
        for (int n : DSU_SIZES) {
            for (boolean compression : options) {
                for (boolean rank : options) {
                    long steps = dsuChainWorkload(n, compression, rank);
                    double time = averageMicros(() -> sink = dsuChainWorkload(n, compression, rank));
                    csv.add(n, compression, rank, fmt(time), steps);
                }
            }
        }
        csv.write("dsu_optimizations.csv");
    }

    static void sccGrowth() throws IOException {
        Csv csv = new Csv("V,E,avg_time_us,components,largest_component,edges_examined");
        for (int[] size : SIZES) {
            Graph g = GraphGenerator.randomDirected(size[0], size[1], SEED);
            SccResult r = StronglyConnectedComponents.run(g);
            double time = averageMicros(() -> sink = StronglyConnectedComponents.run(g).getComponentCount());
            csv.add(size[0], size[1], fmt(time), r.getComponentCount(),
                    r.getLargestComponentSize(), r.getEdgesExamined());
        }
        csv.write("scc.csv");
    }

    static void topologicalSort() throws IOException {
        Csv csv = new Csv("V,E,input,avg_time_us,is_dag,ordered_vertices,blocked_vertices,edges_examined");
        for (int[] size : SIZES) {
            String[] names = {"dag", "one_cycle", "random_directed"};
            Graph[] graphs = {
                    GraphGenerator.directedAcyclic(size[0], size[1], SEED),
                    GraphGenerator.directedWithOneCycle(size[0], size[1], SEED),
                    GraphGenerator.randomDirected(size[0], size[1], SEED)
            };
            for (int i = 0; i < graphs.length; i++) {
                Graph g = graphs[i];
                TopologicalResult r = TopologicalSort.run(g);
                double time = averageMicros(() -> sink = TopologicalSort.run(g).getEdgesExamined());
                csv.add(size[0], size[1], names[i], fmt(time), r.isDag(), r.getOrder().size(),
                        r.getBlockedVertices().size(), r.getEdgesExamined());
            }
        }
        csv.write("topological_sort.csv");
    }

    static void dijkstraSizes() throws IOException {
        Csv csv = new Csv("V,E,avg_time_us,reachable,edges_examined,relaxations,"
                + "heap_pushes,heap_polls,comparisons");
        for (int[] size : SIZES) {
            Graph g = GraphGenerator.randomDirected(size[0], size[1], SEED);
            ShortestPathResult r = Dijkstra.run(g, 0);
            double time = averageMicros(() -> sink = Dijkstra.run(g, 0).getReachableCount());
            csv.add(size[0], size[1], fmt(time), r.getReachableCount(), r.getEdgesExamined(),
                    r.getRelaxations(), r.getHeapPushes(), r.getHeapPolls(), r.getComparisons());
        }
        csv.write("dijkstra.csv");
    }

    static double averageMicros(Runnable action) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            action.run();
        }
        long total = 0;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long start = System.nanoTime();
            action.run();
            total += System.nanoTime() - start;
        }
        return total / (double) TIMED_RUNS / 1000.0;
    }

    static String fmt(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static class Csv {
        private final List<String> lines = new ArrayList<>();

        Csv(String header) {
            lines.add(header);
        }

        void add(Object... values) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    line.append(',');
                }
                line.append(values[i]);
            }
            lines.add(line.toString());
        }

        void write(String fileName) throws IOException {
            Path file = TABLES.resolve(fileName);
            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(file))) {
                for (String line : lines) {
                    out.println(line);
                }
            }
            System.out.println("Wrote " + file);
        }
    }
}