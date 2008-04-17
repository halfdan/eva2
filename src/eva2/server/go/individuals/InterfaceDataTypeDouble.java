package eva2.server.go.individuals;

/** This interface gives access to a double phenotype and except 
 * for problemspecific operators should only be used by the
 * optimization problem.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 10:57:39
 * To change this template use Options | File Templates.
 */
public interface InterfaceDataTypeDouble {

    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    public void setDoubleDataLength (int length);

    /** This method returns the length of the double data set
     * @return The number of doubles stored
     */
    public int size();

    /** This method will set the range of the double attributes.
     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
     * for dimension d.
     * @param range     The new range for the double data.
     */
    public void SetDoubleRange(double[][] range);

    /** This method will return the range for all double attributes.
     * @return The range array.
     */
    public double[][] getDoubleRange();

    /** This method allows you to read the double data
     * @return double[] representing the double data.
     */
    public double[] getDoubleData();

    /** This method allows you to read the double data without
     * an update from the genotype
     * @return double[] representing the double data.
     */
    public double[] getDoubleDataWithoutUpdate();

    /** This method allows you to set the double data, usually the phenotype data. Consider using
     * SetDoubleDataLamarkian to set the genotype data.
     * @param doubleData    The new double data.
     */
    public void SetDoubleData(double[] doubleData);

    /** This method allows you to set the double data, this can be used for
     * memetic algorithms.
     * @param doubleData    The new double data.
     */
    public void SetDoubleDataLamarkian(double[] doubleData);
}
