package javaeva.server.go.problems.regression;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 15:58:06
 * To change this template use Options | File Templates.
 */
public class RFKoza_GPI_10_2 implements InterfaceRegressionFunction, java.io.Serializable {

    public RFKoza_GPI_10_2() {

    }

    public RFKoza_GPI_10_2(RFKoza_GPI_10_2 b) {

    }

    public Object clone() {
        return (Object) new RFKoza_GPI_10_2(this);
    }

    /** This method will return the y value for a given x vector
     * @param x     Input vector.
     * @return y the function result.
     */
    public double evaulateFunction(double[] x) {
        double result = 0;
        for (int i = 0; i < x.length; i++) result += 3.1416*x[i] + 2.718 * Math.pow(x[i], 2);
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
        return "Koza GP I 10.2";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This target function is given in Koza GP I chapter 10.2.";
    }
}
