package eva2.problems;

import eva2.optimization.individuals.InterfaceDataTypeDouble;

/**
 * A minimal interface for double valued problems.
 *
 */
public interface InterfaceProblemDouble {
    /**
     * Evaluate a double vector, representing the target function.
     *
     * @param x the vector to evaluate
     * @return the target function value
     */
    public double[] evaluate(double[] x);

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
     * @param dim Dimension
     * @return the upper bound of the double range in the given dimension
     * @see #makeRange()
     * @see #getRangeLowerBound(int dim)
     */
    public double getRangeUpperBound(int dim);

    /**
     * Get the lower bound of the double range in the given dimension. Override
     * this to implement non-symmetric ranges. Use setDefaultRange for symmetric ranges.
     *
     * @param dim Dimension
     * @return the lower bound of the double range in the given dimension
     * @see #makeRange()
     * @see #getRangeUpperBound(int dim)
     */
    public double getRangeLowerBound(int dim);
}
