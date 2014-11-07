package eva2.optimization.operator.paretofrontmetrics;

import eva2.optimization.population.Population;
import eva2.problems.AbstractMultiObjectiveOptimizationProblem;

/**
 * Interface for general Pareto front methods.
 */
public interface InterfaceParetoFrontMetric {

    /**
     * This method allows you to get a perfect clone
     *
     * @return the clone
     */
    public Object clone();

    /**
     * This method gives a metirc how to evaluate
     * an achieved Pareto-Front
     */
    public double calculateMetricOn(Population pop, AbstractMultiObjectiveOptimizationProblem problem);
}
