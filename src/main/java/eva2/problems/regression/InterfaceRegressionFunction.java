package eva2.problems.regression;

/**
 */
public interface InterfaceRegressionFunction {

    /**
     * This method allows you to get a deep clone
     *
     * @return The clone.
     */
    Object clone();

    /**
     * This method will return the y value for a given x vector
     *
     * @param x Input vector.
     * @return y the function result.
     */
    double evaluateFunction(double[] x);
}
