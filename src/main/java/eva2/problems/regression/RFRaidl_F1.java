package eva2.problems.regression;

import eva2.util.annotation.Description;

/**
 */
@Description("This target function is given in Raidl98Hybrid F1.")
public class RFRaidl_F1 implements InterfaceRegressionFunction, java.io.Serializable {

    public RFRaidl_F1() {

    }

    public RFRaidl_F1(RFRaidl_F1 b) {

    }

    @Override
    public Object clone() {
        return new RFRaidl_F1(this);
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
            result += Math.sin(x[i]);
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
        return "Raidl F1";
    }
}
