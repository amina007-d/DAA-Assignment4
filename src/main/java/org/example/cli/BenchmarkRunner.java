package org.example.cli;

import org.example.graph.scc.*;
import org.example.graph.topo.KahnTopoSort;
import org.example.graph.dagsp.DAGPaths;
import org.example.metrics.Metrics;
import org.example.io.JSONLoader;
import org.example.io.JSONWriter;

import java.io.IOException;
import java.util.*;

public class BenchmarkRunner {

    public static void main(String[] args) throws IOException {
        String inputPath = (args.length > 0) ? args[0] : "data/large_graphs.json";
        System.out.println("Loading graphs from " + inputPath + "...");

        List<Graph> graphs = JSONLoader.loadAll(inputPath);
        System.out.println("Loaded " + graphs.size() + " graphs.");

        List<Map<String, Object>> allSccResults = new ArrayList<>();
        List<Map<String, Object>> allTopoResults = new ArrayList<>();
        List<Map<String, Object>> allDagResults = new ArrayList<>();

        int index = 1;
        for (Graph g : graphs) {
            System.out.println("\nProcessing Graph #" + index + " (" + g.size() + " vertices)resu");

            Metrics mScc = new Metrics();
            KosarajuSharirSCC scc = new KosarajuSharirSCC(g, mScc);
            scc.run();

            var dag = CondensationBuilder.build(g.size(), scc.getComponentIds(), g.getAdj(), scc.count());
            var dagEdges = CondensationBuilder.edgesAsList(dag);

            List<List<Integer>> comps = scc.getComponents();
            List<Map<String, Object>> compList = new ArrayList<>();
            for (int i = 0; i < comps.size(); i++) {
                compList.add(Map.of(
                        "id", i,
                        "vertices", comps.get(i),
                        "size", comps.get(i).size()
                ));
            }

            Map<String, Object> sccResult = new LinkedHashMap<>();
            sccResult.put("graph_id", index);
            sccResult.put("n", g.size());
            sccResult.put("components", compList);
            sccResult.put("condensation_edges", dagEdges);
            sccResult.put("metrics", Map.of(
                    "dfsVisits", mScc.getDfsVisits(),
                    "edgesExplored", mScc.getEdgesExplored(),
                    "time_ms", mScc.getTimeMs()
            ));
            allSccResults.add(sccResult);

            Metrics mTopo = new Metrics();
            KahnTopoSort topo = new KahnTopoSort(dag, mTopo);
            List<Integer> topoOrder = topo.order();

            List<Integer> derivedOrder = new ArrayList<>();
            for (int compIndex : topoOrder) {
                List<Integer> verts = comps.get(compIndex);
                verts.sort(Comparator.naturalOrder());
                derivedOrder.addAll(verts);
            }

            Map<String, Object> topoResult = new LinkedHashMap<>();
            topoResult.put("graph_id", index);
            topoResult.put("topo_order_components", topoOrder);
            topoResult.put("derived_task_order", derivedOrder);
            topoResult.put("metrics", Map.of(
                    "pushes", mTopo.getPushes(),
                    "pops", mTopo.getPops(),
                    "time_ms", mTopo.getTimeMs()
            ));
            allTopoResults.add(topoResult);

            List<Edge> weightedEdges = new ArrayList<>();
            Map<String, Double> minWeights = new HashMap<>();


            for (int u = 0; u < g.size(); u++) {
                for (org.example.graph.scc.Edge e : g.getAdj().get(u)) {
                    int compU = scc.getComponentIds()[e.from];
                    int compV = scc.getComponentIds()[e.to];
                    if (compU != compV) {
                        String key = compU + "->" + compV;
                        double w = e.weight;
                        minWeights.put(key, Math.min(minWeights.getOrDefault(key, Double.POSITIVE_INFINITY), w));
                    }
                }
            }

            for (var entry : minWeights.entrySet()) {
                String[] parts = entry.getKey().split("->");
                int from = Integer.parseInt(parts[0]);
                int to = Integer.parseInt(parts[1]);
                double w = entry.getValue();
                weightedEdges.add(new Edge(from, to, w));
            }

            Metrics mDAG = new Metrics();
            DAGPaths dagPaths = new DAGPaths(dag.size(), weightedEdges, mDAG);
            int source = topoOrder.get(0);
            List<Integer> topoCopy = new ArrayList<>(topoOrder);
            var shortest = dagPaths.shortestPaths(source, topoCopy);
            var longest = dagPaths.longestPaths(source, topoCopy);

            double maxDist = Double.NEGATIVE_INFINITY;
            int criticalNode = -1;
            for (int i = 0; i < longest.dist.length; i++) {
                if (longest.dist[i] > maxDist) {
                    maxDist = longest.dist[i];
                    criticalNode = i;
                }
            }
            List<Integer> criticalPath = DAGPaths.reconstructPath(criticalNode, longest.parent);

            Map<String, Object> dagResult = new LinkedHashMap<>();
            dagResult.put("graph_id", index);
            dagResult.put("edges", weightedEdges.stream()
                    .map(e -> Map.of("from", e.from, "to", e.to, "w", e.weight))
                    .toList());
            dagResult.put("source_component", source);
            dagResult.put("shortest_paths", Map.of(
                    "distances", shortest.dist,
                    "path_to_last", DAGPaths.reconstructPath(
                            topoOrder.get(topoOrder.size() - 1), shortest.parent)
            ));
            dagResult.put("longest_paths", Map.of(
                    "distances", longest.dist,
                    "critical_length", maxDist,
                    "critical_path", criticalPath
            ));
            dagResult.put("metrics", Map.of(
                    "relaxations", mDAG.getRelaxations(),
                    "time_ms", mDAG.getTimeMs()
            ));
            allDagResults.add(dagResult);

            System.out.println("Finished Graph #" + index);
            index++;
        }

        JSONWriter.write("results/large_scc.json", Map.of("results", allSccResults));
        JSONWriter.write("results/large_topo.json", Map.of("results", allTopoResults));
        JSONWriter.write("results/large_dagsp.json", Map.of("results", allDagResults));

        System.out.println("\nAll graphs processed successfully! Combined outputs saved.");
    }
}

