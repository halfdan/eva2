package eva2.cli;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.problems.InterfaceOptimizationProblem;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        InterfaceOptimizationParameters parameters = OptimizationBuilder.parseOptimizerArguments(args);
        InterfaceStatisticsParameters statisticsParameters = OptimizationBuilder.parseStatisticsArguments(args);

        InterfaceTerminator terminator = parameters.getTerminator();
        InterfaceOptimizer optimizer = parameters.getOptimizer();
        InterfaceOptimizationProblem problem = parameters.getProblem();

        LinkedHashMap<String, Object> optimizationLog = new LinkedHashMap<>();
        // Meta parameters
        optimizationLog.put("population_size", parameters.getOptimizer().getPopulation().getTargetSize());
        optimizationLog.put("number_of_runs", statisticsParameters.getMultiRuns());
        optimizationLog.put("seed", parameters.getRandomSeed());

        // Container for individual runs
        List<LinkedHashMap<String, Object>> runs = new ArrayList<>();
        optimizationLog.put("runs", runs);

        FileWriter fw;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter("derp.yml");
            bw = new BufferedWriter(fw);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        }

        //optimizer.addPopulationChangedEventListener(new OptimizationLogger(parameters, ));
        for (int i = 0; i < statisticsParameters.getMultiRuns(); i++) {

            problem.initializeProblem();
            problem.initializePopulation(optimizer.getPopulation());

            optimizer.setProblem(problem);
            terminator.initialize(problem);

            /**
             * This is the main optimization loop. We keep calling
             * optimize() until a termination criterion is met or
             * the user aborts the optimization manually.
             */
            do {
                optimizer.optimize();
            } while (!terminator.isTerminated(optimizer.getAllSolutions()));

            System.out.println(Arrays.toString(((InterfaceDataTypeDouble)optimizer.getPopulation().getBestEAIndividual()).getDoubleData()));
        }

        try {
            bw.write(new Yaml().dump(optimizationLog));
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
