package eva2.problems.regression;

/**
 */
public class RFKoza_GPI_10_1 implements InterfaceRegressionFunction, java.io.Serializable {

    public RFKoza_GPI_10_1() {

    }

    public RFKoza_GPI_10_1(RFKoza_GPI_10_1 b) {

    }

    @Override
    public Object clone() {
        return new RFKoza_GPI_10_1(this);
    }

    /**
     * This method will return the y value for a given x vector
     *
     * @param x Input vector.
     * @return y the function result.
     */
    @Override
    public double evaluateFunction(double[] x) {
        double result = 0;
        for (int i = 0; i < x.length; i++) {
            result += Math.cos(2 * x[i]);
        }
        return result;
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "Koza GP I 10.1";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This target function is given in Koza GP I chapter 10.1.";
    }
}
