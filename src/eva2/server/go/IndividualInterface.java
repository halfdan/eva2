package eva2.server.go;

import eva2.server.go.problems.InterfaceOptimizationProblem;

/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

/**
 * Minimal interface for an EA individual.
 */
public interface IndividualInterface {
	/**
	 * Create a clone of the individual instance.
	 * 
	 * @return  a clone of the individual instance
	 */
	IndividualInterface getClone();

	/**
	 * Get the fitness array of the individual which may be null if none has been set.
	 * 
	 * @return the fitness array of the individual
	 */
	double[] getFitness();

	/**
	 * Set the fitness array to the given array.
	 * 
	 * @param fit new fitness of the individual
	 */
	void setFitness (double[] fit);

	/**
	 * Check whether the instance is dominating the given other individual and return
	 * true in this case.
	 * 
	 * @param other a second individual of the same type
	 * @return	true if the instance dominates the other individual, else false
	 */
	boolean isDominant(double[] fitness);

	/**
	 * Check whether the instance is dominating the given other individual and return
	 * true in this case.
	 * Should behave equally to {@link #isDominant(double[])} if called with the fitness
	 * of the given individual.
	 * 
	 * @param other a second individual of the same type
	 * @return	true if the instance dominates the other individual, else false
	 */
	boolean isDominant(IndividualInterface other);

	/**
	 * Perform a standard mutation operation on the individual. The exact implementation
	 * depends on the implemented genotype. 
	 */
	void defaultMutate();

	/** 
	 * Initialize the genotype randomly, usually in a uniform distribution. Make sure,
	 * if the problem has an initial range (it implements InterfaceHasInitialRange), that this
	 * initial range is used.
	 */
	void defaultInit(InterfaceOptimizationProblem prob);
}