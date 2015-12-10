package eva2.problems.regression;

import eva2.util.annotation.Description;

/**
 */
@Description("This target function is given in Koza GP I chapter 7.3.")
public class RFKoza_GPI_7_3_extended implements InterfaceRegressionFunction, java.io.Serializable {

    public RFKoza_GPI_7_3_extended() {

    }

    public RFKoza_GPI_7_3_extended(RFKoza_GPI_7_3_extended b) {

    }

    @Override
    public Object clone() {
        return new RFKoza_GPI_7_3_extended(this);
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
            result += 0.12345 * Math.pow(x[i], 4) + (Math.PI / 4) * Math.pow(x[i], 3) + (Math.E / 2) * Math.pow(x[i], 2) + 1.23456 * Math.pow(x[i], 1);
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
        return "Koza GP I 7.3";
    }
}