package eva2.problems.regression;

import eva2.util.annotation.Description;

/**
 *
 */
@Description("This target function is given in Raidl98Hybrid F3.")
public class RFRaidl_F3 implements InterfaceRegressionFunction, java.io.Serializable {

    public RFRaidl_F3() {

    }

    public RFRaidl_F3(RFRaidl_F3 b) {

    }

    @Override
    public Object clone() {
        return new RFRaidl_F3(this);
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
            result += Math.log(4 + 2 * Math.sin(x[i] * Math.sin(8 * x[i]))) * Math.exp(Math.cos(3 * x[i]));
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
        return "Raidl F3";
    }
}