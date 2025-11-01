package org.example.metrics;

import org.apache.commons.csv.*;
import java.io.*;
import java.nio.file.*;

public class CSVWriter {

    private static final String[] HEADER = {
            "graph_id", "vertices", "edges",
            "algorithm", "dfsVisits", "pushes", "pops", "relaxations", "time_ms"
    };

    public static void appendRow(String path, String[] row) throws IOException {
        boolean fileExists = Files.exists(Path.of(path));

        try (BufferedWriter writer = Files.newBufferedWriter(
                Path.of(path),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            CSVPrinter printer = new CSVPrinter(writer,
                    CSVFormat.DEFAULT.withHeader(fileExists ? null : HEADER));
            printer.printRecord((Object[]) row);
            printer.flush();
        }
    }
}

