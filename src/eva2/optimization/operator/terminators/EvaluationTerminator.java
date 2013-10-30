package eva2.optimization.operator.terminators;

import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 * Evaluation Terminator. Terminates the optimization after a certain
 * number of fitness evaluations. Note that this will not terminate after
 * the exact number of fitness calls, since terminators are only once per
 * generation.
 */
@Description(text = "Terminates after the given number of fitness calls.")
public class EvaluationTerminator implements InterfaceTerminator,
        Serializable {
    private String msg = "Not terminated.";
    /**
     * Number of fitness calls on the problem which is optimized.
     */
    protected int maxFitnessCalls = 1000;

    public EvaluationTerminator() {
    }

    @Override
    public void init(InterfaceOptimizationProblem prob) {
        msg = "Not terminated.";
    }

    /**
     * Construct Terminator with a maximum number of fitness calls.
     *
     * @param maximum number of fitness calls
     */
    public EvaluationTerminator(int x) {
        maxFitnessCalls = x;
    }

    @Override
    public boolean isTerminated(InterfaceSolutionSet solSet) {
        return isTerminated(solSet.getCurrentPopulation());
    }

    @Override
    public boolean isTerminated(PopulationInterface population) {
        if (maxFitnessCalls > population.getFunctionCalls()) {
            return false;
        } else {
            msg = maxFitnessCalls + " fitness calls were reached.";
            return true;
        }
    }

    @Override
    public String lastTerminationMessage() {
        return msg;
    }

    @Override
    public String toString() {
        String ret = "EvaluationTerminator,calls=" + maxFitnessCalls;
        return ret;
    }

    public void setFitnessCalls(int x) {
        //System.out.println("setFitnessCalls"+x);
        maxFitnessCalls = x;
    }

    public int getFitnessCalls() {
        //System.out.println("getFitnessCalls"+maxFitnessCalls);
        return maxFitnessCalls;
    }

    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property
     */
    public String fitnessCallsTipText() {
        return "number of calls to fitness function.";
    }

}