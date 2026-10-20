package connectivity;

import java.util.List;

public class TopologicalResult {
    private final List<Integer> order;
    private final List<Integer> blockedVertices;
    private final long edgesExamined;

    public TopologicalResult(List<Integer> order, List<Integer> blockedVertices, long edgesExamined) {
        this.order = order;
        this.blockedVertices = blockedVertices;
        this.edgesExamined = edgesExamined;
    }

    public boolean isDag() {
        return blockedVertices.isEmpty();
    }

    public List<Integer> getOrder() {
        return order;
    }

    public List<Integer> getBlockedVertices() {
        return blockedVertices;
    }

    public long getEdgesExamined() {
        return edgesExamined;
    }
}