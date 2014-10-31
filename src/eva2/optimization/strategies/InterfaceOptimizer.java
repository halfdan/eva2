package eva2.optimization.strategies;

import eva2.optimization.population.InterfacePopulationChangedEventListener;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;


/**
 * The general interface for optimizers giving the main methods necessary
 * to perform a population based search.
 */
public interface InterfaceOptimizer {
    Object clone();
    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    String getName();

    /**
     * This method allows you to add a listener to the Optimizer.
     *
     * @param ea
     */
    void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea);

    /**
     * This method removes a listener from the Optimizer. It returns true on success,
     * false if the listener could not be found.
     *
     * @param ea
     */
    boolean removePopulationChangedEventListener(InterfacePopulationChangedEventListener ea);

    /**
     * This method will initialize the optimizer
     */
    void initialize();

    /**
     * This method will initialize the optimizer with a given population.
     *
     * @param pop   The initial population
     * @param reset If true the population is reinitialized and reevaluated.
     */
    void initializeByPopulation(Population pop, boolean reset);

    /**
     * This method will optimize for a single iteration, after this step
     * the population should be as big as possible (ie. the size of lambda
     * and not mu) and all individual should be evaluated. This allows more
     * useful statistics on the population.
     */
    void optimize();

    /**
     * Assuming that all optimizer will store their data in a population
     * we will allow access to this population to query to current state
     * of the optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    Population getPopulation();

    void setPopulation(Population pop);

    /**
     * Return all found solutions (local optima) if they are not contained in the current population. Be
     * sure to set the Population properties, especially function calls and generation, with respect
     * to the ongoing optimization.
     * May return the the same set as getPopulation if the optimizer makes no distinction, i.e. does
     * not collect solutions outside the current population.
     *
     * @return A solution set of the current population and possibly earlier solutions.
     */
    InterfaceSolutionSet getAllSolutions();

    /**
     * This method will set the problem that is to be optimized. The problem
     * should be initialized when this method is called.
     *
     * @param problem The optimization problem.
     */
    void setProblem(InterfaceOptimizationProblem problem);

    InterfaceOptimizationProblem getProblem();

    /**
     * This method will return a string describing all properties of the optimizer
     * and the applied methods.
     *
     * @return A descriptive string
     */
    String getStringRepresentation();
}
