package eva2.optimization.problems.regression;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 15:55:28
 * To change this template use Options | File Templates.
 */
public class RFKoza_GPI_10_1 implements InterfaceRegressionFunction, java.io.Serializable {

    public RFKoza_GPI_10_1() {

    }

    public RFKoza_GPI_10_1(RFKoza_GPI_10_1 b) {

    }

    @Override
    public Object clone() {
        return (Object) new RFKoza_GPI_10_1(this);
    }

    /** This method will return the y value for a given x vector
     * @param x     Input vector.
     * @return y the function result.
     */
    @Override
    public double evaluateFunction(double[] x) {
        double result = 0;
        for (int i = 0; i < x.length; i++) {
            result += Math.cos(2*x[i]);
        }
        return result;
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Koza GP I 10.1";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This target function is given in Koza GP I chapter 10.1.";
    }
}
