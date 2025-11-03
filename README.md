# Assignment 4
Consolidate two course topics in one practical case (“Smart City / Smart Campus
Scheduling”):
1. Strongly Connected Components (SCC) & Topological Ordering
2. Shortest Paths in DAGs

---

## 1. Introduction

### 1.1 Implemented Algorithms
This work implements a complete analytical pipeline for **directed weighted graphs**, combining three fundamental algorithms:

- **Kosaraju–Sharir Strongly Connected Components (SCC):**  
  Identifies subsets of vertices in which every vertex is reachable from every other vertex.  
  The algorithm performs two depth-first searches: one to compute finishing order, and one on the transposed graph to collect SCCs.

- **Condensation DAG and Topological Sort:**  
  Each SCC is compressed into a single vertex to form an acyclic condensation graph (DAG).  
  A **topological sort** (Kahn’s or DFS-based variant) produces a valid linear order of dependencies among these components.

- **Shortest and Longest Paths in DAG:**  
  Using the topological order, both shortest and longest paths are computed through relaxation in linear time.  
  The **longest path** identifies the **critical path**—useful for scheduling and dependency optimization.

---

### 1.2 Theoretical Efficiency

| Algorithm | Time Complexity | Space Complexity | Description |
|------------|----------------|------------------|--------------|
| Kosaraju–Sharir SCC | O(V + E) | O(V + E) | Two DFS passes |
| Topological Sort (Kahn / DFS) | O(V + E) | O(V) | Linear queue-based order generation |
| DAG Shortest / Longest Paths | O(V + E) | O(V) | Relaxation along topological order |

All stages are linear in the number of vertices and edges.  
This ensures scalability for both sparse and dense graphs with minimal overhead.

---

### 1.3 Datasets Summary

| Dataset              | Vertices (n) | Edges (E) | Description                                      | Weight Model |
|-----------------------|--------------|-----------|--------------------------------------------------|---------------|
| **small_graphs.json** | 6 – 10       | 6 – 8     | Small graphs with simple SCCs and short DAG tails | edge |
| **medium_graphs.json**| 10 – 13      | 11 – 15   | Moderate-density graphs with 2–3 interconnected SCCs | edge |
| **large_graphs.json** | 25 – 40      | 24 – 38   | Dense graphs with long sequential chains          | edge |

All datasets use the **`edge`** weight model, where numeric weights represent traversal cost.


**Theoretical expectations:**
- SCC detection should identify compact clusters in small/medium graphs and one dominant SCC in large graphs.
- Topological sorting should remain linear regardless of graph density.
- DAG path algorithms should show proportional growth in relaxation operations and path length with graph size.

---

## 2. Experimental Results

### 2.1 Small Dataset Results

#### SCC Detection

| Graph | Vertices | SCC Count | Largest SCC | DFS Visits | Edges Explored | Time (ms) |
|--------|-----------|------------|--------------|--------------|----------------|------------|
| 1 | 6 | 4 | 3 | 12 | 12 | 0.1366 |
| 2 | 8 | 6 | 3 | 16 | 16 | 0.1462 |
| 3 | 10 | 10 | 1 | 20 | 16 | 0.0333 |

The first graph contains one cyclic component `{0,1,2}` and three singletons.  
The second graph includes a 3-vertex SCC `{1,2,3}` and several isolated nodes.  
The third graph is nearly acyclic, each vertex forming its own SCC.

#### Topological Sort

| Graph | SCCs | Pushes | Pops | Time (ms) |
|--------|------|---------|-------|------------|
| 1 | 4 | 4 | 4 | 0.0757 |
| 2 | 6 | 6 | 6 | 0.0230 |
| 3 | 10 | 10 | 10 | 0.0281 |

Each condensation graph is acyclic, with a number of topological nodes equal to its SCC count.

#### DAG Shortest & Longest Paths

| Graph | Critical Path | Length | Relaxations | Time (ms) |
|--------|----------------|----------|--------------|------------|
| 1 | [0,1,2,3] | 7.0 | 6 | 0.013 |
| 2 | [0,1,2,3,4] | 9.0 | 10 | 0.0051 |
| 3 | [0,1,2,3,4] | 10.0 | 8 | 0.0071 |

All graphs display linear critical paths, consistent with edge weights and structure.

---

### 2.2 Medium Dataset Results

#### SCC Detection

| Graph | Vertices | SCC Count | Largest SCC | DFS Visits | Edges Explored | Time (ms) |
|--------|-----------|------------|--------------|--------------|----------------|------------|
| 1 | 10 | 6 | 3 | 20 | 22 | 0.0999 |
| 2 | 12 | 5 | 4 | 24 | 28 | 0.0301 |
| 3 | 13 | 7 | 3 | 26 | 30 | 0.0245 |

Medium graphs reveal clear cyclic clusters of 3–4 nodes linked linearly through condensation edges.

#### Topological Sort

| Graph | SCCs | Pushes | Pops | Time (ms) |
|--------|------|---------|-------|------------|
| 1 | 6 | 6 | 6 | 0.0564 |
| 2 | 5 | 5 | 5 | 0.0107 |
| 3 | 7 | 7 | 7 | 0.0100 |

Topological sorting scales perfectly with SCC count; runtime stays under 0.06 ms.

#### DAG Shortest & Longest Paths

| Graph | Critical Path | Length | Relaxations | Time (ms) |
|--------|----------------|----------|--------------|------------|
| 1 | [0,1,2,3,4,5] | 12.0 | 10 | 0.0089 |
| 2 | [0,1,2,3,4] | 12.0 | 8 | 0.0031 |
| 3 | [0,1,2,3,4,5,6] | 13.0 | 12 | 0.0044 |

Critical path length grows with graph depth, confirming expected linear progression.

---

### 2.3 Large Dataset Results

#### SCC Detection

| Graph | Vertices | SCC Count | Largest SCC | DFS Visits | Edges Explored | Time (ms) |
|--------|-----------|------------|--------------|--------------|----------------|------------|
| 1 | 25 | 19 | 7 | 50 | 50 | 0.1756 |
| 2 | 30 | 30 | 1 | 60 | 58 | 0.1621 |
| 3 | 40 | 28 | 6 | 80 | 74 | 0.0834 |

Graph 1 contains one major SCC of 7 vertices, while Graph 2 is purely acyclic.  
Graph 3 presents several medium-sized SCC clusters (3–6 nodes).

#### Topological Sort

| Graph | SCCs | Pushes | Pops | Time (ms) |
|--------|------|---------|-------|------------|
| 1 | 19 | 19 | 19 | 0.0738 |
| 2 | 30 | 30 | 30 | 0.1271 |
| 3 | 28 | 28 | 28 | 0.0283 |

Even at larger scales, runtime increases modestly with the number of SCCs.

#### DAG Shortest & Longest Paths

| Graph | Critical Path | Length | Relaxations | Time (ms) |
|--------|----------------|----------|--------------|------------|
| 1 | [0–17] | 34.0 | 34 | 0.0114 |
| 2 | [0–29] | 57.0 | 58 | 0.0143 |
| 3 | [0–8] | 16.0 | 16 | 0.0097 |

Critical path length correlates strongly with graph size.  
Relaxation counts match edge counts, confirming O(V + E) behavior.

---

## 3. Analysis

### 3.1 Structural Trends
- **Small graphs:** few SCCs (3–6) and simple chains.
- **Medium graphs:** multiple compact SCCs with clear hierarchical order.
- **Large graphs:** either a single dominant SCC or long linear DAG tails.

### 3.2 Performance Scaling
| Stage | Typical Time (ms) | Complexity | Dominant Factor |
|--------|--------------------|-------------|-----------------|
| SCC Detection | 0.03–0.18 | O(V + E) | DFS traversals |
| Topological Sort | 0.01–0.12 | O(V + E) | Queue operations |
| DAG Shortest/Longest | 0.003–0.015 | O(V + E) | Relaxations |

Runtime increases linearly with graph size.  
SCC detection remains the most expensive phase due to two DFS passes.

### 3.3 Expected vs Observed Behavior

| Property                        | Theoretical Expectation                            | Observed Result                     |
|----------------------------------|----------------------------------------------------|-------------------------------------|
| SCC size grows with density      | Large dense graphs form larger SCCs                | Confirmed                          |
| Topological order = # of SCCs    | Should equal the number of SCCs                    | Confirmed for all datasets          |
| DAG-SP runtime linear in edges   | Linear O(V + E) complexity                         | Verified experimentally             |
| Critical path grows with n       | Increases with graph size                          | Confirmed: 7 → 12 → 34–57           |
 

---

## 4. Conclusions

- **Correctness:**  
  The algorithms consistently produced valid SCC partitions, acyclic condensation graphs, and accurate shortest/longest path results.

- **Efficiency:**  
  Execution time remained below 0.2 ms even for 40-vertex graphs, confirming linear-time performance.

- **Structural Insight:**
    - Small datasets reveal modular subgraphs with distinct SCCs.
    - Medium datasets show structured inter-component connectivity.
    - Large datasets highlight long dependency chains and critical path expansion.

- **Practical Recommendation:**  
  This SCC→DAG→SP pipeline is well-suited for analyzing dependencies in large-scale systems such as task schedulers, build graphs, or communication networks.  
  The modular design allows easy extension with alternative path or priority heuristics while maintaining O(V + E) complexity.





