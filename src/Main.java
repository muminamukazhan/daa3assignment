import connectivity.SccResult;
import connectivity.StronglyConnectedComponents;
import connectivity.TopologicalResult;
import connectivity.TopologicalSort;
import graph.Edge;
import graph.Graph;
import mst.Kruskal;
import mst.MstResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.ShortestPathResult;

import java.util.List;

public class Main {
    static final String[] DISTRICTS = {
            "Center", "Station", "Market", "University", "Park", "Harbor", "Airport", "Hospital"
    };

    public static void main(String[] args) {
        demoMst();
        demoTopologicalSort();
        demoScc();
        demoDijkstra();
    }

    static void demoMst() {
        Graph roads = new Graph(8, false);
        roads.addEdge(0, 1, 4);
        roads.addEdge(0, 2, 3);
        roads.addEdge(0, 3, 8);
        roads.addEdge(1, 2, 5);
        roads.addEdge(1, 6, 9);
        roads.addEdge(2, 4, 6);
        roads.addEdge(3, 4, 2);
        roads.addEdge(3, 7, 7);
        roads.addEdge(4, 5, 10);
        roads.addEdge(5, 6, 3);
        roads.addEdge(5, 7, 12);

        System.out.println("=== Task 1: cheapest road network (MST) ===");
        printMst("Kruskal", Kruskal.run(roads));
        printMst("Prim   ", Prim.run(roads));
        System.out.println();
    }

    static void printMst(String name, MstResult result) {
        System.out.println(name + ": total cost " + result.getTotalWeight()
                + ", connected = " + result.isConnected());
        for (Edge e : result.getEdges()) {
            System.out.println("    " + DISTRICTS[e.from] + " - " + DISTRICTS[e.to] + " (" + e.weight + ")");
        }
    }

    static void demoTopologicalSort() {
        Graph projects = new Graph(8, true);
        projects.addEdge(0, 1, 1);
        projects.addEdge(0, 2, 1);
        projects.addEdge(1, 6, 1);
        projects.addEdge(2, 4, 1);
        projects.addEdge(4, 3, 1);
        projects.addEdge(3, 7, 1);
        projects.addEdge(6, 5, 1);

        System.out.println("=== Task 2: build order (topological sort) ===");
        TopologicalResult order = TopologicalSort.run(projects);
        System.out.println("No cycle: " + order.isDag());
        System.out.println("Order: " + names(order.getOrder()));

        projects.addEdge(7, 0, 1);
        TopologicalResult cyclic = TopologicalSort.run(projects);
        System.out.println("After adding Hospital -> Center: no cycle = " + cyclic.isDag()
                + ", blocked districts: " + names(cyclic.getBlockedVertices()));
        System.out.println();
    }

    static void demoScc() {
        Graph streets = oneWayStreets();
        System.out.println("=== Task 2: strongly connected groups (SCC) ===");
        SccResult scc = StronglyConnectedComponents.run(streets);
        System.out.println("Number of groups: " + scc.getComponentCount());
        for (int i = 0; i < scc.getComponents().size(); i++) {
            System.out.println("    group " + i + ": " + names(scc.getComponents().get(i)));
        }
        System.out.println();
    }

    static void demoDijkstra() {
        Graph streets = oneWayStreets();
        System.out.println("=== Task 3: shortest travel time from Center (Dijkstra) ===");
        ShortestPathResult result = Dijkstra.run(streets, 0);
        for (int v = 0; v < DISTRICTS.length; v++) {
            if (result.isReachable(v)) {
                System.out.println("    " + DISTRICTS[v] + ": " + result.getDistance(v)
                        + " min, path " + names(result.getPath(v)));
            } else {
                System.out.println("    " + DISTRICTS[v] + ": unreachable");
            }
        }
    }

    static Graph oneWayStreets() {
        Graph g = new Graph(8, true);
        g.addEdge(0, 1, 4);
        g.addEdge(1, 2, 3);
        g.addEdge(2, 0, 2);
        g.addEdge(0, 4, 10);
        g.addEdge(2, 4, 5);
        g.addEdge(4, 3, 2);
        g.addEdge(3, 4, 3);
        g.addEdge(3, 7, 6);
        g.addEdge(1, 6, 9);
        g.addEdge(6, 5, 4);
        return g;
    }

    static String names(List<Integer> vertices) {
        StringBuilder text = new StringBuilder("[");
        for (int i = 0; i < vertices.size(); i++) {
            if (i > 0) {
                text.append(" -> ");
            }
            text.append(DISTRICTS[vertices.get(i)]);
        }
        return text.append("]").toString();
    }
}