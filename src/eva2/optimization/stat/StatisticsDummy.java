package eva2.optimization.stat;

import eva2.optimization.individuals.IndividualInterface;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;
import eva2.optimization.strategies.InterfaceOptimizer;

import java.util.List;

/**
 * This may be given to a Processor if no further stats are required. It speeds up
 * optimization especially with small populations (e.g. HC as local search operator).
 *
 * @author mkron
 */
public class StatisticsDummy implements InterfaceStatistics, InterfaceTextListener {
    boolean consoleOut = false;
    StatisticsParameter sParams = null;
    AbstractEAIndividual bestCurrentIndividual, bestRunIndy, bestIndividualAllover;

    public StatisticsDummy() {
        bestIndividualAllover = null;
        sParams = new StatisticsParameter();
        sParams.setOutputVerbosityK(StatisticsParameter.VERBOSITY_NONE);
    }

    public StatisticsDummy(boolean doConsoleOut) {
        bestIndividualAllover = null;
        sParams = new StatisticsParameter();
        sParams.setOutputVerbosityK(StatisticsParameter.VERBOSITY_NONE);
        consoleOut = doConsoleOut;
    }

    @Override
    public void addTextListener(InterfaceTextListener listener) {
        System.err.println("addTextListener not provided!");
    }

    @Override
    public void createNextGenerationPerformed(PopulationInterface pop, InterfaceOptimizer opt,
                                              List<InterfaceAdditionalPopulationInformer> informerList) {
        bestCurrentIndividual = (AbstractEAIndividual) pop.getBestIndividual();
        if ((bestIndividualAllover == null) || (AbstractStatistics.secondIsBetter(bestIndividualAllover, bestCurrentIndividual))) {
            bestIndividualAllover = bestCurrentIndividual;
        }
        if ((bestIndividualAllover == null) || (AbstractStatistics.secondIsBetter(bestIndividualAllover, bestCurrentIndividual))) {
            bestIndividualAllover = bestCurrentIndividual;
        }
    }

    @Override
    public void createNextGenerationPerformed(double[] bestfit,
                                              double[] worstfit, int calls) {
    }

    @Override
    public double[] getBestFitness() {
        if (bestIndividualAllover != null) {
            return bestCurrentIndividual.getFitness();
        } else {
            return null;
        }
    }

    @Override
    public IndividualInterface getBestSolution() {
        return bestIndividualAllover;
    }

    @Override
    public IndividualInterface getRunBestSolution() {
        return bestRunIndy;
    }

    @Override
    public InterfaceStatisticsParameter getStatisticsParameter() {
        return sParams;
    }

    @Override
    public void printToTextListener(String s) {
        if (consoleOut) {
            System.out.println(s);
        }
    }

    @Override
    public boolean removeTextListener(InterfaceTextListener listener) {
        System.err.println("removeTextListener not provided!");
        return false;
    }

    @Override
    public void startOptPerformed(String InfoString, int runnumber,
                                  Object params, List<InterfaceAdditionalPopulationInformer> informerList) {
        if (runnumber == 0) {
            bestIndividualAllover = null;
        }
        bestRunIndy = null;
    }

    @Override
    public void stopOptPerformed(boolean normal, String stopMessage) {
    }

    @Override
    public void postProcessingPerformed(Population resultPop) {
    }

    @Override
    public void print(String str) {
        if (consoleOut) {
            System.out.print(str);
        }
    }

    @Override
    public void println(String str) {
        if (consoleOut) {
            System.out.println(str);
        }
    }

    @Override
    public void addDataListener(InterfaceStatisticsListener l) {
        System.err.println("addDataListener not provided!");
    }

    @Override
    public boolean removeDataListener(InterfaceStatisticsListener l) {
        System.err.println("removeDataListener not provided!");
        return false;
    }
}
