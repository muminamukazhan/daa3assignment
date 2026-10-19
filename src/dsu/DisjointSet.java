package dsu;

public class DisjointSet {
    private final int[] parent;
    private final int[] rank;
    private final boolean usePathCompression;
    private final boolean useUnionByRank;
    private int setCount;

    private long findCalls;
    private long unionCalls;
    private long successfulUnions;
    private long parentSteps;

    public DisjointSet(int size) {
        this(size, true, true);
    }

    public DisjointSet(int size, boolean usePathCompression, boolean useUnionByRank) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be at least 1");
        }
        this.parent = new int[size];
        this.rank = new int[size];
        this.usePathCompression = usePathCompression;
        this.useUnionByRank = useUnionByRank;
        this.setCount = size;
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {
        findCalls++;

        int root = x;
        while (parent[root] != root) {
            root = parent[root];
            parentSteps++;
        }

        if (usePathCompression) {
            while (parent[x] != root) {
                int next = parent[x];
                parent[x] = root;
                x = next;
            }
        }
        return root;
    }

    public boolean union(int a, int b) {
        unionCalls++;
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) {
            return false;
        }

        if (useUnionByRank) {
            if (rank[rootA] < rank[rootB]) {
                parent[rootA] = rootB;
            } else if (rank[rootA] > rank[rootB]) {
                parent[rootB] = rootA;
            } else {
                parent[rootB] = rootA;
                rank[rootA]++;
            }
        } else {
            parent[rootA] = rootB;
        }

        setCount--;
        successfulUnions++;
        return true;
    }

    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    public int getSetCount() {
        return setCount;
    }

    public long getFindCalls() {
        return findCalls;
    }

    public long getUnionCalls() {
        return unionCalls;
    }

    public long getSuccessfulUnions() {
        return successfulUnions;
    }

    public long getParentSteps() {
        return parentSteps;
    }

    public void resetCounters() {
        findCalls = 0;
        unionCalls = 0;
        successfulUnions = 0;
        parentSteps = 0;
    }
}