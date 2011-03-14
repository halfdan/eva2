package eva2.server.go.operators.mutation;

import eva2.server.go.populations.Population;

/**
 * An interface for a mutation operator which is updated on a generational basis, such as
 * the 1/5-success rule. Mind that not all EA use dogmatic selection and thus not all
 * will inform this interface. So far, only ES and GA will.
 * 
 * @author mkron
 *
 */
public interface InterfaceAdaptOperatorGenerational  {
	
	/**
	 * Perform adaption of the operator based on the selection performed by an EA.
	 * 
	 * @param oldPop  the initial population for the developmental step
	 * @param selectedPop the sup-population selected as parents for the new generation
	 */
	public void adaptAfterSelection(Population oldPop, Population selectedPop);
	
	/**
	 * Perform adaption of the operator based on the developmental step performed by an EA. 
	 * The data provided for this call are the old population, the individuals selected as
	 * parents, and the new population created from the selected individuals.
	 * Usually, only the instances within the new population must be adapted. An additional tag 
	 * indicates whether the instances of the selected population should be adapted as well,
	 * e.g., if they survive as elite (or in an ES "plus" strategy). 
	 * 
	 * @param oldPop the initial population for the developmental step
	 * @param selectedPop the sup-population selected as parents for the new generation
	 * @param newPop the new population created by the EA, should already be evaluated
	 * @param updateSelected if true, the selected population should be adapted as well
	 */
	public void adaptGenerational(Population oldPop, Population selectedPop, Population newPop, boolean updateSelected);
}
