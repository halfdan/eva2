/**
 * 
 */
package eva2.server.go.problems;

import eva2.server.go.populations.Population;

/** 
 * A synchronous problem changes in equal intervals. As the EA takes notice of the
 * problem only in evaluations, the smallest reasonable interval is once per evaluation
 * so that the problem changes after every evaluation. This of course under the simplification
 * that an evaluation takes no time, or from a different POV, changes within an evaluation
 * can not be taken into account (or just discretely approximated).
 *
 * @author M.Kronfeld
 *
 * Jan 4, 2007
 **/
public abstract class AbstractSynchronousOptimizationProblem extends
		AbstractDynamicOptimizationProblem {
	
	/**
	 * counts the evaluations since the last change in problem dynamics
	 */
	protected double evalsSinceChange;
	/**
	 * holds the number of evaluations for which the environment regularly remains the same
	 */
	protected double constantProblemEvals;
	/**
	 * measures the "distance" which the problem shifts when it changes 
	 */
	protected double shiftPerChange;
	//protected double timeIntPerEval			= 1.;	// better assume this to be constant

	/**
	 * 
	 */
	public AbstractSynchronousOptimizationProblem() {
		evalsSinceChange = 0.;
		constantProblemEvals = 99999;
		shiftPerChange = 1.;
		
	}
	
	
    public void initProblem() {
    	super.initProblem();
    	evalsSinceChange = 0.;
    	
    }
    
	/* (non-Javadoc)
	 * @see eva2.server.oa.go.OptimizationProblems.AbstractDynamicOptimizationProblem#changeProblemAt(double)
	 */
	@Override
	protected void changeProblemAt(double problemTime) {
		incProblemTime(shiftPerChange);
		if (TRACE) System.out.println("problem shiftet at " + problemTime + " per " + shiftPerChange); 
		evalsSinceChange = 0.;
	}

	/* (non-Javadoc)
	 * @see eva2.server.oa.go.OptimizationProblems.AbstractDynamicOptimizationProblem#problemToChangeAt(double)
	 */
	@Override
	protected boolean problemToChangeAt(double problemTime) {
		return (evalsSinceChange >= constantProblemEvals);
	}

	/** 
	 * Set the number of evaluations for which the problem is static. 
	 * This is declared protected as it usually depends on further parameters of implementations.
	 *
	 * @param constEvals new value to set
	 */
	protected void setConstantProblemEvals(double constEvals) {
		constantProblemEvals = constEvals;
		if (TRACE) System.out.println("setConstantProblemEvals: " + constEvals);
	}

	/* (non-Javadoc)
	 * @see eva2.server.oa.go.OptimizationProblems.AbstractDynamicOptimizationProblem#countEvaluation()
	 */
	@Override
	protected void countEvaluation() {
		evalsSinceChange += 1.;
	}

	/**
	 * Return the number of evaluations for which the problem is static.
	 * This is declared protected as it usually depends on further parameters of implementations.
	 *
	 * @return the number of evaluations for which the problem is static
	 */
	protected double getConstantProblemEvals() {
		return constantProblemEvals;
	}
	
	public void setFrequency(double frequency) {
		super.setFrequency(frequency);
		//if (frequency > 1.) System.out.println("warning, frequency should be <= 1 (setFrequency)");
		if (isFrequencyRelative()) {
			// bogus. this must be reset right before population evaluation
			setConstantProblemEvals(9999999);
		} else {
			// in absolute evals, so 1 means once per individual eval, .5 every second
			setConstantProblemEvals(1/frequency);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see eva2.server.oa.go.OptimizationProblems.AbstractDynamicOptimizationProblem#setFrequencyRelative(boolean)
	 */
	@Override
	public void setFrequencyRelative(boolean frequencyRelative) {
		super.setFrequencyRelative(frequencyRelative);
		setFrequency(getFrequency()); // this looks stupid, but actually recalculates stuff for a changed relative switch
	}
	
	
	public void evaluatePopulationStart(Population population) {
		if (isFrequencyRelative()) {
		// sets the number of evaluations without change when depending on the population size 
			setConstantProblemEvals(population.size()/getFrequency());
		}
	}
}
