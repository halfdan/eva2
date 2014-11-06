package eva2.cli;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.problems.InterfaceOptimizationProblem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

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

        OutputStream fileStream = null;
        try {
            fileStream = new FileOutputStream("derp.yml");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        }

        optimizer.addPopulationChangedEventListener(new OptimizationLogger(parameters, fileStream));
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
    }
}
