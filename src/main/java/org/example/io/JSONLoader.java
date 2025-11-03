package org.example.io;

import com.google.gson.*;
import org.example.graph.scc.Graph;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class JSONLoader {

    public static List<Graph> loadAll(String path) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root;
        try (FileReader reader = new FileReader(path)) {
            root = gson.fromJson(reader, JsonObject.class);
        }

        List<Graph> list = new ArrayList<>();

        if (root.has("graphs")) {
            JsonArray graphs = root.getAsJsonArray("graphs");
            for (JsonElement element : graphs) {
                JsonObject gObj = element.getAsJsonObject();
                int n = gObj.get("n").getAsInt();
                Graph g = new Graph(n);

                JsonArray edges = gObj.getAsJsonArray("edges");
                for (JsonElement e : edges) {
                    JsonObject edge = e.getAsJsonObject();
                    int u = edge.get("u").getAsInt();
                    int v = edge.get("v").getAsInt();

                    double w = edge.has("w") ? edge.get("w").getAsDouble() : 1.0;

                    g.addEdge(u, v, w);
                }

                list.add(g);
            }
        } else {
            int n = root.get("n").getAsInt();
            Graph g = new Graph(n);
            JsonArray edges = root.getAsJsonArray("edges");

            for (JsonElement e : edges) {
                JsonObject edge = e.getAsJsonObject();
                int u = edge.get("u").getAsInt();
                int v = edge.get("v").getAsInt();
                double w = edge.has("w") ? edge.get("w").getAsDouble() : 1.0;
                g.addEdge(u, v, w);
            }

            list.add(g);
        }

        return list;
    }
}
