package eva2.problems.regression;

/**
 */
public interface InterfaceRegressionFunction {

    /**
     * This mehtod allows you to get a deep clone
     *
     * @return The clone.
     */
    public Object clone();

    /**
     * This method will return the y value for a given x vector
     *
     * @param x Input vector.
     * @return y the function result.
     */
    public double evaluateFunction(double[] x);
}
