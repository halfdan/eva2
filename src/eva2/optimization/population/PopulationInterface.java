package eva2.optimization.population;

import eva2.optimization.individuals.IndividualInterface;

/**
 *
 */
public interface PopulationInterface {

    /**
     * Returns the best individual of the population.
     *
     * @return The best individual
     */
    IndividualInterface getBestIndividual();

    /**
     * Returns the worst individual of the population.
     *
     * @return The worst individual
     */
    IndividualInterface getWorstIndividual();

    double[] getBestFitness();

    double[] getWorstFitness();

    double[] getMeanFitness();

    double[] getPopulationMeasures();

    /**
     * Returns the number of function calls.
     *
     * @return The number of function calls
     */
    int getFunctionCalls();

    /**
     * The current generation count.
     *
     * @return int The current generation
     */
    int getGeneration();

    /**
     * This method returns problem specific data
     *
     * @return double[]
     */
    double[] getSpecificData();

    /**
     * This method returns identifiers for the specific data Note:
     * "Pareto-Front" is reserved for multi-crit. Problems string[1] gives the
     * dimension of the fitness values
     *
     * @return String[]
     */
    String[] getSpecificDataNames();


    Object get(int i);

    /**
     * Return the size of the population.
     *
     * @return int size of the population
     */
    int size();

    /**
     * Clear the population.
     */
    void clear();
}