package eva2.optimization.stat;

import eva2.optimization.individuals.IndividualInterface;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.optimization.strategies.InterfaceOptimizer;

import java.util.List;

/**
 *
 */
public interface InterfaceStatistics {
    /**
     * Initialize statistics computations.
     */
    void startOptimizationPerformed(String InfoString, int runnumber, Object params, List<InterfaceAdditionalPopulationInformer> informerList); // called from processor

    /**
     * Finalize statistics computations.
     */
    void stopOptimizationPerformed(boolean normal, String stopMessage); // called from processor

    void addDataListener(InterfaceStatisticsListener listener);

    boolean removeDataListener(InterfaceStatisticsListener listener);

    void addTextListener(InterfaceTextListener listener);

    boolean removeTextListener(InterfaceTextListener listener);

    void printToTextListener(String s);

    void createNextGenerationPerformed(PopulationInterface Pop, InterfaceOptimizer opt, List<InterfaceAdditionalPopulationInformer> informerList);

    void createNextGenerationPerformed(double[] bestfit, double[] worstfit, int calls);

    InterfaceStatisticsParameter getStatisticsParameter(); // called from moduleadapter

    IndividualInterface getRunBestSolution(); // return the best fitness of the last run (may not be equal to the last population)

    IndividualInterface getBestSolution(); // returns the best overall solution

    double[] getBestFitness(); // returns the best overall fitness

    void postProcessingPerformed(Population resultPop); // called from processor
}