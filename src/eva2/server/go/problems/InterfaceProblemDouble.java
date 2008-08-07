package eva2.server.go.problems;

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
}
