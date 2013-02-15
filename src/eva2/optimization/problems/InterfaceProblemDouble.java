package eva2.optimization.problems;

import eva2.optimization.individuals.InterfaceDataTypeDouble;

/**
 * A minimal interface for double valued problems.
 * 
 * @author mkron
 *
 */
public interface InterfaceProblemDouble {
	/**
	 * Evaluate a double vector, representing the target function.
	 * 
	 * @param x the vector to evaluate
	 * @return	the target function value
	 */
	public double[] eval(double[] x);
	
	/**
	 * Get the problem dimension.
	 * 
	 * @return the problem dimension
	 */
	public int getProblemDimension();
	
	/**
	 * Create a new range array by using the getRangeLowerBound and getRangeUpperBound methods.
	 * 
	 * @return a range array
	 */
    public double[][] makeRange();
    
    /**
     * Get the EA individual template currently used by the problem.
     * 
     * @return the EA individual template currently used
     */
    public InterfaceDataTypeDouble getEAIndividual();
    
    /**
     * Get the upper bound of the double range in the given dimension. Override
     * this to implement non-symmetric ranges. User setDefaultRange for symmetric ranges.
     * 
     * @see makeRange()
     * @see getRangeLowerBound(int dim)
     * @param dim
     * @return the upper bound of the double range in the given dimension
     */
    public double getRangeUpperBound(int dim);
    
    /**
     * Get the lower bound of the double range in the given dimension. Override
     * this to implement non-symmetric ranges. Use setDefaultRange for symmetric ranges.
     * 
     * @see makeRange()
     * @see getRangeUpperBound(int dim)
     * @param dim
     * @return the lower bound of the double range in the given dimension
     */
    public double getRangeLowerBound(int dim);
}
