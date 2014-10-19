package eva2.optimization.operator.terminators;

import eva2.gui.BeanInspector;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceOptimizationProblem;

import java.io.Serializable;

/**
 *
 */
public class FitnessValueTerminator implements InterfaceTerminator,
        Serializable {
    protected double[] fitnessValue;
    private String msg = "";

    /**
     *
     */
    public FitnessValueTerminator() {
        fitnessValue = new double[]{0.1};
    }

    @Override
    public void init(InterfaceOptimizationProblem prob) {
        msg = "Not terminated.";
    }

    /**
     *
     */
    public static String globalInfo() {
        return "Terminate if a certain fitness value has been reached.";
    }

    /**
     *
     */
    public FitnessValueTerminator(double[] v) {
        fitnessValue = v.clone();
    }

    @Override
    public boolean isTerminated(InterfaceSolutionSet solSet) {
        return isTerminated(solSet.getCurrentPopulation());
    }

    @Override
    public boolean isTerminated(PopulationInterface Pop) {
        double[] fit = Pop.getBestFitness();
        for (int i = 0; i < fit.length; i++) {
            if (fitnessValue[i] < fit[i]) {
                return false;
            }
        }
        msg = "Fitness value reached " + BeanInspector.toString(fitnessValue);
        return true;
    }

    @Override
    public String lastTerminationMessage() {
        return msg;
    }

    /**
     *
     */
    @Override
    public String toString() {
        String ret = "FitnessValueTerminator,fitnessValue=" + fitnessValue;
        return ret;
    }

    /**
     *
     */
    public void setFitnessValue(double[] x) {
        fitnessValue = x;
    }

    /**
     *
     */
    public double[] getFitnessValue() {
        return fitnessValue;
    }

    public String fitnessValueTipText() {
        return "Set the fitness objective value.";
    }
}