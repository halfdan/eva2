package eva2.optimization.operator.classification;

/**
 * An interface for classification methods, currently under construction.
 */
public interface InterfaceClassification {
    /**          x
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    public Object clone();

    /**
     * This method will initialize the classificator
     *
     * @param space The double[n][d] space*
     * @param type  The classes [0,1,..]
     */
    public void init(double[][] space, int[] type);

    /**
     * This method allows you to train the classificator based on
     * double[d] values and the class. n gives the number of instances
     * and d gives the dimension of the search space.
     *
     * @param space The double[n][d] space
     * @param type  The int[n] classes [0,1,..]
     */
    public void train(double[][] space, int[] type);

    /**
     * This method will classify a given data point
     *
     * @param point The double[d] data point.
     * @return type     The resulting class.
     */
    public int getClassFor(double[] point);
}
