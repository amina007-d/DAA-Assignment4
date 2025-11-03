package org.example.metrics;
import com.google.gson.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class CSVWriter {

    public static void convert(String jsonFilePath, String csvFilePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject root;
        try (FileReader reader = new FileReader(jsonFilePath)) {
            root = gson.fromJson(reader, JsonObject.class);
        }

        JsonArray results = root.getAsJsonArray("results");
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("Invalid JSON: missing or empty 'results' array");
        }

        JsonObject first = results.get(0).getAsJsonObject();
        JsonObject metrics = first.getAsJsonObject("metrics");

        try (FileWriter writer = new FileWriter(csvFilePath)) {

            StringBuilder header = new StringBuilder("graph_id");
            for (Map.Entry<String, JsonElement> entry : metrics.entrySet()) {
                header.append(",").append(entry.getKey());
            }
            writer.append(header).append("\n");

            for (JsonElement element : results) {
                JsonObject graphObj = element.getAsJsonObject();
                int graphId = graphObj.get("graph_id").getAsInt();
                JsonObject m = graphObj.getAsJsonObject("metrics");

                StringBuilder line = new StringBuilder(String.valueOf(graphId));
                for (Map.Entry<String, JsonElement> entry : metrics.entrySet()) {
                    JsonElement val = m.get(entry.getKey());
                    line.append(",").append(val != null ? val.getAsString() : "");
                }
                writer.append(line).append("\n");
            }
        }

        System.out.println("CSV created: " + csvFilePath);
    }

    public static void main(String[] args) throws IOException {
        convert("results/small_scc.json", "plots/small_scc_metrics.csv");
        convert("results/small_topo.json", "plots/small_topo_metrics.csv");
        convert("results/small_dagsp.json", "plots/small_dagsp_metrics.csv");
    }
}
