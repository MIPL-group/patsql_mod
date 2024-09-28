## Foresighter Modification

This modification of PATSQL is made for experimental usage of  Foresighter - a SQL synthesizing research project. We have chosen PATSQL as one of our baselines for this study and want to make some miner modification to support our CEGIS evaluation simulatoin.

### Modification Details

Our modifications are aimed at running the synthesizer tool in a simulated CEGIS (Counter-Example Guided Inductive Synthesis) loop. This setup requires the tool to output all solutions it can find within a given timeout as soon as possible to a log file. These logs will later be evaluated using our fuzzing evaluation tools, regardless of the solution's ranking or costs.

### Key Changes

**Continuous Solving**: We've modified PATSQL to support continuous solving, allowing it to generate multiple solutions within a given timeframe.

**Logging Support**: We've implemented a comprehensive logging system that records all found solutions, their runtime, and other relevant metadata. This is crucial for our experimental setup and subsequent analysis.

**CEGIS Simulation**: While not implementing a full CEGIS loop, our modifications allow PATSQL to operate in a manner that simulates aspects of CEGIS, particularly in terms of solution generation and logging.

 It's important to note that we have kept the fundamental algorithm of PATSQL unchanged. Our modifications are primarily focused on the output and logging mechanisms.

All modifications made for the Foresighter project are clearly marked in the code with comments starting with `//(Foresighter Mod)`.

### Usage of Modified Version

To use this modified version of PATSQL for Foresighter experiments:

1. Build the project using Maven as described in the original installation instructions.
```
mvn install -DskipTests
```
2. Ensure benchmark file of markdown format labelel with header Source/Group/ID is placed under `Benchmarks/AllBenchmarks`
3. Run the `BenchmarkRunnerCEGIS` executable from the target, which is the entry point for our modified CEGIS-like execution.
For distributive running, for each instance we can run:
```
java -jar ./BenchmarkRunnerCEGIS.jar <instance_id> <num_instances> <timeout> <trail_number>
```


4. Results will be logged in CSV format under `CEGIS_log`



 This modification is an extension of the original project and is only used for Foresighter experiments. For the original PATSQL's readme file, refer to `README_original.md`.