package eva2.optimization.individuals;

import eva2.util.annotation.Parameter;

/**
 * This interface gives access to a double phenotype and except
 * for problemspecific operators should only be used by the
 * optimization problem.
 */
public interface InterfaceDataTypeDouble {

    /**
     * This method allows you to request a certain amount of double data
     *
     * @param length The lenght of the double[] that is to be optimized
     */
    void setDoubleDataLength(int length);

    /**
     * This method returns the length of the double data set
     *
     * @return The number of doubles stored
     */
    int size();

    /**
     * This method will set the range of the double attributes.
     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
     * for dimension d.
     *
     * @param range The new range for the double data.
     */
    @Parameter(name = "range", description = "The initialization range for the individual.")
    void setDoubleRange(double[][] range);

    /**
     * This method will return the range for all double attributes.
     *
     * @return The range array.
     */
    double[][] getDoubleRange();

    /**
     * This method allows you to read the double data
     *
     * @return double[] representing the double data.
     */
    double[] getDoubleData();

    /**
     * This method allows you to read the double data without
     * an update from the genotype
     *
     * @return double[] representing the double data.
     */
    double[] getDoubleDataWithoutUpdate();

    /**
     * This method allows you to set the double data, usually the phenotype data. Consider using
     * SetDoubleDataLamarckian to set the genotype data.
     *
     * @param doubleData The new double data.
     */
    void setDoublePhenotype(double[] doubleData);

    /**
     * This method allows you to set the double data, this can be used for
     * memetic algorithms.
     *
     * @param doubleData The new double data.
     */
    void setDoubleGenotype(double[] doubleData);
}
