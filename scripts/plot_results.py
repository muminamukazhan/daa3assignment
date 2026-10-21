import csv
import os
import sys
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt


def find_project_root():
    folder = os.path.dirname(os.path.abspath(__file__))
    while True:
        if os.path.isdir(os.path.join(folder, "results", "tables")):
            return folder
        parent = os.path.dirname(folder)
        if parent == folder:
            sys.exit("Could not find results/tables above " + os.path.dirname(os.path.abspath(__file__))
                     + ". Run benchmark.Benchmark first.")
        folder = parent


ROOT = find_project_root()
TABLES = os.path.join(ROOT, "results", "tables")
PLOTS = os.path.join(ROOT, "results", "plots")
os.makedirs(PLOTS, exist_ok=True)


def read(name):
    with open(os.path.join(TABLES, name), newline="") as f:
        return list(csv.DictReader(f))


def series(rows, keep=lambda r: True):
    rows = [r for r in rows if keep(r)]
    return ([int(r["V"]) for r in rows],
            [int(r["E"]) for r in rows],
            [float(r["avg_time_us"]) for r in rows])


def collect():
    mst, scc = read("mst_comparison.csv"), read("scc.csv")
    topo, dijkstra = read("topological_sort.csv"), read("dijkstra.csv")
    return {
        "Kruskal": series(mst, lambda r: r["algorithm"] == "Kruskal"),
        "Prim": series(mst, lambda r: r["algorithm"] == "Prim"),
        "SCC (Kosaraju)": series(scc),
        "Topological sort (DAG)": series(topo, lambda r: r["input"] == "dag"),
        "Dijkstra": series(dijkstra),
    }


def plot(data, x_index, x_label, file_name):
    plt.figure(figsize=(7, 4.5))
    for name, values in data.items():
        plt.plot(values[x_index], values[2], marker="o", label=name)
    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel(x_label)
    plt.ylabel("average time (microseconds, 5 runs)")
    plt.title("Running time vs " + x_label.split(" ")[0])
    plt.grid(True, which="both", alpha=0.3)
    plt.legend()
    plt.tight_layout()
    path = os.path.join(PLOTS, file_name)
    plt.savefig(path, dpi=150)
    plt.close()
    print("Wrote", path)


def plot_dsu():
    rows = read("dsu_optimizations.csv")
    biggest = max(int(x["n"]) for x in rows)
    labels, times = [], []
    for r in rows:
        if int(r["n"]) != biggest:
            continue
        labels.append("compr=%s\nrank=%s" % (r["path_compression"], r["union_by_rank"]))
        times.append(float(r["avg_time_us"]))
    plt.figure(figsize=(7, 4.5))
    plt.bar(labels, times)
    plt.yscale("log")
    plt.ylabel("average time (microseconds)")
    plt.title("DSU chain workload, n = %d" % biggest)
    plt.tight_layout()
    path = os.path.join(PLOTS, "dsu_optimizations.png")
    plt.savefig(path, dpi=150)
    plt.close()
    print("Wrote", path)


if __name__ == "__main__":
    data = collect()
    plot(data, 0, "V (number of vertices)", "time_vs_V.png")
    plot(data, 1, "E (number of edges)", "time_vs_E.png")
    plot_dsu()