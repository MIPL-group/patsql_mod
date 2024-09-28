/**
 * (Foresighter Mod)
 * CEGISLogRecorder
 *
 * This class is responsible for creating and managing log files for CEGIS (Counterexample-Guided Inductive Synthesis)
 * experiments. It generates a CSV log file to record the results of the synthesis process.
 *
 * Log File Structure:
 * - The log file is a CSV with 6 columns:
 *   1. ID: A unique identifier for each benchmark (format: source.group.id)
 *   2. Source: The origin or category of the benchmark
 *   3. SolvingTool: The tool used for solving the synthesis problem
 *   4. SolutionRank: The rank of the solution (1 for the first solution found, 2 for the second, etc.)
 *   5. Solution: The actual synthesized solution in SQL query
 *   6. Runtime: The time taken to find this solution (in milliseconds)
 *
 * - Each row in the CSV file corresponds to one solution found during the synthesis process.
 * - Multiple solutions for the same benchmark are recorded as separate rows with increasing SolutionRank.
 * - Checked for duplicates, cannot add the same solution twice.
 */
package patsql.synth;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CEGISLogRecorder {
    private final String logDir;
    private final String trial;
    private final String source;
    private final String group;
    private final String id;
    private final String filePath;
    private final List<Map<String, String>> solutions;
    private final String[] columns;

    private int num_duplicates;

    public CEGISLogRecorder(String logDir, String trial, String source, String group, String id) {
        this.logDir = logDir;
        this.trial = trial;
        this.source = source;
        this.group = group;
        this.id = id;
        this.filePath = generateFilePath();
        this.solutions = new ArrayList<>();
        this.columns = new String[]{"ID", "Source", "SolvingTool", "SolutionRank", "Solution", "Runtime"};
        // readExistingLog();
        this.num_duplicates = 0;
    }

    private String generateFilePath() {
        String benchmark_id = source + "." + group + "." + id;
        Path dirPath = Paths.get(logDir, trial, source, group, benchmark_id);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dirPath.resolve(trial + "_log.csv").toString();
    }

    private void readExistingLog() {
        Path dirPath = Paths.get(filePath).getParent();
        try {
            List<Path> existingLogs = Files.list(dirPath)
                    .filter(p -> p.getFileName().toString().endsWith("_log.csv"))
                    .sorted()
                    .collect(Collectors.toList());

            if (!existingLogs.isEmpty()) {
                Path latestLog = existingLogs.get(existingLogs.size() - 1);
                try (CSVReader reader = new CSVReader(new FileReader(latestLog.toFile()))) {
                    List<String[]> allRows = reader.readAll();
                    if (allRows.size() > 1) {
                        String[] header = allRows.get(0);
                        for (int i = 1; i < allRows.size(); i++) {
                            String[] row = allRows.get(i);
                            Map<String, String> solution = new HashMap<>();
                            for (int j = 0; j < Math.min(header.length, row.length); j++) {
                                solution.put(header[j], row[j]);
                            }
                            if (solution.keySet().containsAll(Arrays.asList(columns))) {
                                solutions.add(solution);
                            }
                        }
                    }
                } catch (CsvException e) {
                    System.err.println("Error reading CSV file: " + e.getMessage());
                }
                System.out.println("Resumed logging from existing file: " + latestLog);
            }
        } catch (IOException e) {
            System.err.println("Error accessing log directory: " + e.getMessage());
        }
    }

    public void addSolution(String solvingTool, int solutionRank, String solution, long runtime) {
        // For checking duplicate: Create a hash
        String solutionHash = Integer.toString(solution.hashCode());

        // Check if duplicates exist
        if (solutions.stream().anyMatch(s -> s.get("SolutionHash").equals(solutionHash))) {
            // DEBUG
            //System.out.println("Found duplicates");
            num_duplicates++;
            return;
        }

        Map<String, String> solutionMap = new HashMap<>();
        solutionMap.put("ID", String.format("%s.%s.%s", source, group, id));
        solutionMap.put("Source", source);
        solutionMap.put("SolvingTool", solvingTool);
        solutionMap.put("SolutionRank", String.valueOf(solutionRank- num_duplicates));
        solutionMap.put("Solution", solution);
        solutionMap.put("Runtime", String.valueOf(runtime));
        solutionMap.put("SolutionHash", solutionHash);
        solutions.add(solutionMap);
    }


    public void writeCSV() {
        List<Map<String, String>> solutionsCopy;
        synchronized(this) {
            solutionsCopy = new ArrayList<>(this.solutions);
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(columns);
            for (Map<String, String> solution : solutionsCopy) {
                String[] rowData = new String[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    rowData[i] = solution.getOrDefault(columns[i], "");
                }
                writer.writeNext(rowData);
            }
            System.out.println("[[CSV Written]]");
        } catch (IOException e) {
            System.err.println("Error writing CSV: " + e.getMessage());
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public int getSolutionCount() {
        return solutions.size();
    }
}