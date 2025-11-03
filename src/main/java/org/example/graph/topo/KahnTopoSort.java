package org.example.graph.topo;

import org.example.metrics.Metrics;
import java.util.*;

public class KahnTopoSort {

    private final List<Set<Integer>> dag; // DAG: component adjacency
    private final Metrics metrics;
    private List<Integer> topoOrder;

    public KahnTopoSort(List<Set<Integer>> dag, Metrics metrics) {
        this.dag = dag;
        this.metrics = metrics;
    }

    public List<Integer> order() {
        metrics.startTimer();

        int n = dag.size();
        int[] inDegree = new int[n];
        for (int u = 0; u < n; u++) {
            for (int v : dag.get(u)) {
                inDegree[v]++;
            }
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
                metrics.incPushes();
            }
        }

        topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.remove();
            metrics.incPops();
            topoOrder.add(u);

            for (int v : dag.get(u)) {
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.add(v);
                    metrics.incPushes();
                }
            }
        }

        metrics.stopTimer();
        return topoOrder;
    }

    public boolean isDag() {
        return topoOrder != null && topoOrder.size() == dag.size();
    }

    public List<Integer> getOrder() {
        return topoOrder;
    }

}
