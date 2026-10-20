package connectivity;

import java.util.List;

public class SccResult {
    private final List<List<Integer>> components;
    private final int[] componentOf;
    private final long edgesExamined;

    public SccResult(List<List<Integer>> components, int[] componentOf, long edgesExamined) {
        this.components = components;
        this.componentOf = componentOf;
        this.edgesExamined = edgesExamined;
    }

    public List<List<Integer>> getComponents() {
        return components;
    }

    public int getComponentCount() {
        return components.size();
    }

    public int getComponentOf(int vertex) {
        return componentOf[vertex];
    }

    public int getLargestComponentSize() {
        int largest = 0;
        for (List<Integer> component : components) {
            largest = Math.max(largest, component.size());
        }
        return largest;
    }

    public boolean isStronglyConnected() {
        return components.size() == 1;
    }

    public long getEdgesExamined() {
        return edgesExamined;
    }
}