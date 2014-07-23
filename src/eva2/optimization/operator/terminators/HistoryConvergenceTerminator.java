package eva2.optimization.operator.terminators;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.AbstractEAIndividualComparator;
import eva2.optimization.operator.distancemetric.ObjectiveSpaceMetric;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceOptimizationProblem;

import java.io.Serializable;
import java.util.List;

/**
 * A terminator regarding population history (the archive of a population). Since the population
 * history is not in general accessible from the GUI, this terminator is hidden.
 *
 * @author mkron
 */
public class HistoryConvergenceTerminator implements InterfaceTerminator, Serializable {
    int haltingWindowLen = 15;
    int fitCrit = 0;
    double convergenceThreshold;
    boolean stdDevInsteadOfImprovement;
    AbstractEAIndividualComparator indyImprovementComparator = new AbstractEAIndividualComparator("", -1, true);
    String msg;

    public static final boolean hideFromGOE = true; // hide from GUI

    public HistoryConvergenceTerminator() {
    }

    public HistoryConvergenceTerminator(int windowLen, double convThreshold, int fitnessCrit, boolean stdDevInsteadOfImprovement) {
        haltingWindowLen = windowLen;
        convergenceThreshold = convThreshold;
        fitCrit = fitnessCrit;
        this.stdDevInsteadOfImprovement = stdDevInsteadOfImprovement;
    }

    public static String globalInfo() {
        return "Converge based on a halting window on a population history.";
    }

    @Override
    public void init(InterfaceOptimizationProblem prob) {
        msg = "Not terminated.";
    }

    @Override
    public boolean isTerminated(PopulationInterface pop) {
        int histLen = (((Population) pop).getHistory()).size();
        boolean res = false;
        if (histLen >= haltingWindowLen) {
            List<AbstractEAIndividual> subHist = ((Population) pop).getHistory().subList(histLen - haltingWindowLen, histLen);
            if (stdDevInsteadOfImprovement) { // look at fitness std dev.
                double[] fitMeas = Population.getFitnessMeasures(subHist, fitCrit);
                res = (fitMeas[3] < convergenceThreshold);
                if (res) {
                    msg = "Historic fitness std.dev. below " + convergenceThreshold + " for " + histLen + " generations.";
                }
            } else { // look at improvements
                AbstractEAIndividual historicHWAgo = subHist.get(0);
//              System.out.println("Ref indy: " + historicHWAgo);
//              System.out.println("Best historic: " + ((Population)pop).getBestHistoric());
                res = true;
                for (int i = 1; i < haltingWindowLen; i++) {
                    // if historic[-hW] is worse than historic[-hW+i] return false
                    AbstractEAIndividual historicIter = subHist.get(i);
                    // if the iterated indy (the later one in history) has improved, there is no convergence.
                    boolean improvementHappened = (testSecondForImprovement(historicHWAgo, historicIter));
                    if (improvementHappened) {
                        res = false;
                        break;
                    }
                }
                if (res) {
                    msg = "History did not improve" + (convergenceThreshold > 0 ? (" by more than " + convergenceThreshold) : "") + " for " + haltingWindowLen + " iterations.";
                }
            }
        } else {
            if (haltingWindowLen > ((Population) pop).getMaxHistLength()) {
                System.err.println("Warning, population history length not long enough for window length " + haltingWindowLen + " (HistoryConvergenceTerminator)");
            }
        }
        return res;
    }

    /**
     * Define the criterion by which individual improvement is judged. The original version defined
     * improvement strictly, but for some EA this should be done more laxly. E.g. DE will hardly ever
     * stop improving slightly, so optionally use an epsilon-bound: improvement only counts if it is
     * larger than epsilon in case useEpsilonBound is true.
     *
     * @param firstIndy
     * @param secIndy
     * @return true if the second individual has improved in relation to the first one
     */
    private boolean testSecondForImprovement(AbstractEAIndividual firstIndy, AbstractEAIndividual secIndy) {
        if (convergenceThreshold > 0) {
            double fitDiff = (new ObjectiveSpaceMetric()).distance(firstIndy, secIndy);
            boolean ret = (secIndy.isDominatingDebConstraints(firstIndy));
            ret = ret && (fitDiff > convergenceThreshold);  // there is improvement if the second is dominant and the fitness difference is larger than epsilon
            return ret;
        } else {
            return (indyImprovementComparator.compare(firstIndy, secIndy) > 0);
        }
    }

    @Override
    public boolean isTerminated(InterfaceSolutionSet sols) {
        return isTerminated(sols.getCurrentPopulation());
    }

    @Override
    public String lastTerminationMessage() {
        return msg;
    }

    public int getHaltingWindowLen() {
        return haltingWindowLen;
    }

    public void setHaltingWindowLen(int haltingWindowLen) {
        this.haltingWindowLen = haltingWindowLen;
    }

    public String haltingWindowLenTipText() {
        return "Number of generations regarded back in the history";
    }

    public int getFitCrit() {
        return fitCrit;
    }

    public void setFitCrit(int fitCrit) {
        this.fitCrit = fitCrit;
    }

    public String fitCritTipText() {
        return "The index of the fitness criterion regarded (multi-objective case).";
    }

    public double getConvergenceThreshold() {
        return convergenceThreshold;
    }

    public void setConvergenceThreshold(double convergenceThreshold) {
        this.convergenceThreshold = convergenceThreshold;
    }

    public String convergenceThresholdTipText() {
        return "Threshold below improvements (or deviations) are still seen as stagnation.";
    }

    public boolean isStdDevInsteadOfImprovement() {
        return stdDevInsteadOfImprovement;
    }

    public void setStdDevInsteadOfImprovement(boolean stdDevInsteadOfImprovement) {
        this.stdDevInsteadOfImprovement = stdDevInsteadOfImprovement;
    }

    public String stdDevInsteadOfImprovementTipText() {
        return "Look at the standard deviation of historic fitness values instead of absolute fitness.";
    }

}
