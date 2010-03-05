package eva2.server.go.problems.regression;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 09.10.2003
 * Time: 11:38:20
 * To change this template use Options | File Templates.
 */
public class RFKoza_GPI_7_3_extended implements InterfaceRegressionFunction, java.io.Serializable {

    public RFKoza_GPI_7_3_extended() {

    }

    public RFKoza_GPI_7_3_extended(RFKoza_GPI_7_3_extended b) {

    }

    public Object clone() {
        return (Object) new RFKoza_GPI_7_3_extended(this);
    }

    /** This method will return the y value for a given x vector
     * @param x     Input vector.
     * @return y the function result.
     */
    public double evaluateFunction(double[] x) {
        double result = 0;
        for (int i = 0; i < x.length; i++) result += 0.12345*Math.pow(x[i], 4) + (Math.PI/4)*Math.pow(x[i], 3) + (Math.E/2)*Math.pow(x[i], 2) + 1.23456*Math.pow(x[i], 1);
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
        return "Koza GP I 7.3";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This target function is given in Koza GP I chapter 7.3.";
    }
}