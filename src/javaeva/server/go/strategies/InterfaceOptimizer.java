package javaeva.server.go.strategies;

import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.InterfaceOptimizationProblem;

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
    public Object clone();

    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName();

    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea);

    /** This method will init the optimizer
     */
    public void init();

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset);

    /** This method will optimize for a single iteration, after this step
     * the population should be as big as possible (ie. the size of lambda
     * and not mu) and all individual should be evaluated. This allows more
     * usefull statistics on the population.
     */
    public void optimize();

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation();
    public void setPopulation(Population pop);

    /** This method allows you to set an identifier for the algorithm
     * @param name      The indenifier
     */
     public void SetIdentifier(String name);
     public String getIdentifier();

    /**
     * This method will set the problem that is to be optimized. The problem
     * should be initialized when this method is called.
     *
     * @param problem
     */
    public void SetProblem (InterfaceOptimizationProblem problem);
    public InterfaceOptimizationProblem getProblem ();

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation();

    /** This method is required to free the memory on a RMIServer,
     * but there is nothing to implement.
     */
    public void freeWilly();
}
