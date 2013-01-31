package eva2.server.go.strategies;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;


/** The general interface for optimizers giving the main methods necessary
 * to perform a population based search.
 * This is a simple implementation of Population Based Incremental Learning.
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public interface InterfaceOptimizer {

    /** This method will return deep clone of the optimizer
     * @return The clone
     */
    Object clone();

    /** This method will return a naming String
     * @return The name of the algorithm
     */
    String getName();

    /** 
     * This method allows you to add a listener to the Optimizer.
     * @param ea
     */
    void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea);
    
    /** 
     * This method removes a listener from the Optimizer. It returns true on success,
     * false if the listener could not be found.
     * @param ea
     */
    boolean removePopulationChangedEventListener(InterfacePopulationChangedEventListener ea);
    
    /** This method will init the optimizer
     */
    void init();

    /** 
     * This method will init the optimizer with a given population.
     * 
     * @param pop       The initial population
     * @param reset     If true the population is reinitialized and reevaluated.
     */
    void initByPopulation(Population pop, boolean reset);

    /** This method will optimize for a single iteration, after this step
     * the population should be as big as possible (ie. the size of lambda
     * and not mu) and all individual should be evaluated. This allows more
     * usefull statistics on the population.
     */
    void optimize();

    /** Assuming that all optimizer will store their data in a population
     * we will allow access to this population to query to current state
     * of the optimizer.
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
     * This method allows you to set an identifier for the algorithm
     * @param name      The identifier
     */
    void setIdentifier(String name);
    String getIdentifier();

    /**
     * This method will set the problem that is to be optimized. The problem
     * should be initialized when this method is called.
     *
     * @param problem
     */
    void setProblem (InterfaceOptimizationProblem problem);
    InterfaceOptimizationProblem getProblem ();

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    String getStringRepresentation();
}
