/**
 * (Foresighter Mod)
 * The main executable for auto running CEGIS experiments on all benchmarks Under Benchmarks/AllBenchmarks
 *  Usage:
 *  - Distributive Run: java -jar ./benchmarkrunner.jar <instance_id> <num_instances> <timeout> <trail_number>
 *     - instance_id: the id of the current instance
 *     - num_instances: the total number of instances
 *     - timeout: the timeout for each benchmark (in ms)
 *     - trail_number: the trail number for logging
 */

package patsql.synth.cegis;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class BenchmarkRunnerCEGIS {

    static int instance_id = 0;
    static int num_instances = 1;
    // Timeout unit is ms
    static int timeout = 100;
    static String csv_file_path = null;
    static String log_base_dir = "CEGIS_log";
    static String trail = "debug";

    public static void main(String[] args) {

        // Start time
        long startTime = System.currentTimeMillis();

        if (args.length == 2) {
            timeout = Integer.parseInt(args[0]);
            trail = args[1];
        } else if (args.length == 4) {
            instance_id = Integer.parseInt(args[0]);
            num_instances = Integer.parseInt(args[1]);
            timeout = Integer.parseInt(args[2]);
            trail = args[3];
        } else {
            System.err.println("Usage: java -jar ./BenchmarkRunnerCEGIS.jar <instance_id> <num_instances> <timeout> <trail_number>");
            System.err.println("Or: java -jar ./BenchmarkRunnerCEGIS.jar <timeout> <trail_number>");
            System.exit(1);
        }

        System.out.println("Initializing BenchmarkRunner with instance_id: " + instance_id + ", num_instances: " + num_instances
                + ", timeout: " + timeout + ", trail_number: " + trail);

        BenchmarkRunnerCEGIS runner = new BenchmarkRunnerCEGIS();
        runner.runBenchmarks();

        // Print runtime
        long endTime = System.currentTimeMillis();
        System.out.println("Runtime: " + (endTime - startTime) + "ms");

        System.exit(0);
    }

    public void runBenchmarks() {

        patsql.synth.Debug.isDebugMode = false;

        Path startPath = Paths.get("Benchmarks/AllBenchmarks");

        try (Stream<Path> stream = Files.walk(startPath)) {
            stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".md"))
                    .forEach(file -> processFile(file));
        } catch (IOException e) {
            System.err.println("Failed to process files under benchmark path: " + e.getMessage());
        }

        System.out.println("BenchmarkRunner finished");
    }

    private void processFile(Path file) {
        try {
            long fileSize = Files.size(file);
            if (fileSize > 2000000) {
                System.out.println("File: " + file + " is too large, skipped");
                return;
            }
            int fileId = parseFileId(file.getFileName().toString());
            if (fileId % num_instances != instance_id) {
                // Skip the files not belong to this group by unique instance id.
                return;
            }
            System.out.println("Processing file: " + file);
            SUtil.synthesizeFromScytheFileCEGIS(new File(file.toString()), timeout, log_base_dir, trail);
        } catch (Exception e) {
            System.err.println("Error processing file " + file + ": " + e.getMessage());
            logError(file, e);
        }
    }

    private void logError(Path file, Exception e) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("BenchmarksCheck/error.log", true))) {
            writer.println("Error processing file " + file + ": " + e.getMessage());
        } catch (IOException e1) {
            System.err.println("Failed to write error to log file: " + e1.getMessage());
        }
    }

    public static int parseFileId(String fileName) {
        if (fileName == null || !fileName.contains("_")) {
            throw new IllegalArgumentException("Invalid file name (with unique id)");
        }

        int lastUnderscoreIndex = fileName.lastIndexOf('_');
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastUnderscoreIndex == -1 || lastDotIndex == -1 || lastUnderscoreIndex >= lastDotIndex) {
            throw new IllegalArgumentException("Invalid file name (with unique id)");
        }

        String idStr = fileName.substring(lastUnderscoreIndex + 1, lastDotIndex);
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("File ID is not a valid integer", e);
        }
    }
}
