package eva2.optimization.problems;

import eva2.optimization.population.Population;

/**
 * <p>Title: EvA2</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author planatsc
 * @version 1.0
 */

public interface InterfaceLocalSearchable extends InterfaceOptimizationProblem {

    /**
     * Perform a single local search step on each of the given individuals.
     *
     * @param pop
     */
    public void doLocalSearch(Population pop);

    /**
     * Estimate the cost of one local search step -- more precisely the cost of the doLocalSearch call per one individual.
     *
     * @return
     */
    public double getLocalSearchStepFunctionCallEquivalent();

}
