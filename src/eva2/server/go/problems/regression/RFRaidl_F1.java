package eva2.server.go.problems.regression;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 30.09.2003
 * Time: 14:02:41
 * To change this template use Options | File Templates.
 */
public class RFRaidl_F1 implements InterfaceRegressionFunction, java.io.Serializable {

    public RFRaidl_F1() {

    }

    public RFRaidl_F1(RFRaidl_F1 b) {

    }

    @Override
    public Object clone() {
        return (Object) new RFRaidl_F1(this);
    }

    /** This method will return the y value for a given x vector
     * @param x     Input vector.
     * @return y the function result.
     */
    @Override
    public double evaluateFunction(double[] x) {
        double result = 0;
        for (int i = 0; i < x.length; i++) {
            result += Math.sin(x[i]);
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
        return "Raidl F1";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This target function is given in Raidl98Hybrid F1.";
    }
}
