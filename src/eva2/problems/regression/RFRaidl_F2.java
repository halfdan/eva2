package eva2.problems.regression;

/**
 *
 */
public class RFRaidl_F2 implements InterfaceRegressionFunction, java.io.Serializable {

    public RFRaidl_F2() {

    }

    public RFRaidl_F2(RFRaidl_F2 b) {

    }

    @Override
    public Object clone() {
        return new RFRaidl_F2(this);
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
            result += Math.exp(x[i] / 3) * Math.cos(3 * x[i]) / 2;
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
        return "Raidl F2";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This target function is given in Raidl98Hybrid F2.";
    }
}
