package eva2.optimization.problems.regression;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 15:51:48
 * To change this template use Options | File Templates.
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
