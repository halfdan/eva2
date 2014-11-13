package eva2.cli;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.individuals.IndividualInterface;
import eva2.optimization.modules.Processor;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.statistics.InterfaceStatistics;
import eva2.optimization.statistics.InterfaceStatisticsListener;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.InterfaceTextListener;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.problems.InterfaceOptimizationProblem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

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

        Processor optimizationProcessor = new Processor(new YamlStatistics(statisticsParameters), parameters);
        optimizationProcessor.setSaveParams(false);
        optimizationProcessor.startOptimization();


        optimizationProcessor.runOptimizationOnce();
    }
}

final class YamlStatistics implements InterfaceStatistics {
    private static final Logger LOGGER = Logger.getLogger(YamlStatistics.class.getName());
    private InterfaceStatisticsParameters statisticsParameters;

    public YamlStatistics(InterfaceStatisticsParameters statisticsParameters) {
        super();
        this.statisticsParameters = statisticsParameters;
    }

    @Override
    public void startOptimizationPerformed(String infoString, int runNumber, InterfaceOptimizationParameters params, List<InterfaceAdditionalPopulationInformer> informerList) {

    }

    @Override
    public void stopOptimizationPerformed(boolean normal, String stopMessage) {

    }

    @Override
    public void addDataListener(InterfaceStatisticsListener listener) {

    }

    @Override
    public boolean removeDataListener(InterfaceStatisticsListener listener) {
        return false;
    }

    @Override
    public void addTextListener(InterfaceTextListener listener) {

    }

    @Override
    public boolean removeTextListener(InterfaceTextListener listener) {
        return false;
    }

    @Override
    public void printToTextListener(String s) {
        System.out.println(s);
    }

    @Override
    public void createNextGenerationPerformed(PopulationInterface Pop, InterfaceOptimizer opt, List<InterfaceAdditionalPopulationInformer> informerList) {

    }

    @Override
    public void createNextGenerationPerformed(double[] bestFit, double[] worstFit, int calls) {

    }

    @Override
    public InterfaceStatisticsParameters getStatisticsParameters() {
        return statisticsParameters;
    }

    @Override
    public IndividualInterface getRunBestSolution() {
        return null;
    }

    @Override
    public IndividualInterface getBestSolution() {
        return null;
    }

    @Override
    public double[] getBestFitness() {
        return new double[0];
    }

    @Override
    public void postProcessingPerformed(Population resultPop) {

    }
}
