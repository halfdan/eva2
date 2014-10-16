package eva2.problems.simple;

/**
 * A simple interface to easily include new optimization problems in Java into the
 * EvA framework.
 */

public interface InterfaceSimpleProblem<T> {
    /**
     * Evaluate a double vector representing a possible problem solution as
     * part of an individual in the EvA framework. This makes up the
     * target function to be evaluated.
     *
     * @param x a double vector to be evaluated
     * @return the fitness vector assigned to x as to the target function
     */
    public double[] evaluate(T x);

    /**
     * Return the problem dimension.
     *
     * @return the problem dimension
     */
    public int getProblemDimension();

}

