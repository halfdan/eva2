package eva2.problems;

import eva2.optimization.population.Population;

/**
 *
 */
public interface InterfaceLocalSearchable extends InterfaceOptimizationProblem {

    /**
     * Perform a single local search step on each of the given individuals.
     *
     * @param pop
     */
    void doLocalSearch(Population pop);

    /**
     * Estimate the cost of one local search step -- more precisely the cost of the doLocalSearch call per one individual.
     *
     * @return
     */
    double getLocalSearchStepFunctionCallEquivalent();
}
