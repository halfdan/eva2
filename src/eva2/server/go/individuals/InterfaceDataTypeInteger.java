package eva2.server.go.individuals;

/** This interface gives access to a integer phenotype and except 
 * for problemspecific operators should only be used by the
 * optimization problem.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 15.04.2004
 * Time: 17:16:23
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceDataTypeInteger {

    /** This method allows you to request a certain amount of int data
     * @param length    The lenght of the int[] that is to be optimized
     */
    public void setIntegerDataLength (int length);

    /** This method returns the length of the int data set
     * @return The number of integers stored
     */
    public int size();

    /** This method will set the range of the int attributes.
     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
     * for dimension d.
     * @param range     The new range for the int data.
     */
    public void SetIntRange(int[][] range);

    /** This method will return the range for all int attributes.
     * @return The range array.
     */
    public int[][] getIntRange();

    /** This method allows you to read the int data
     * @return int[] representing the int data.
     */
    public int[] getIntegerData();

    /** This method allows you to read the int data without
     * an update from the genotype
     * @return int[] representing the int data.
     */
    public int[] getIntegerDataWithoutUpdate();

    /** This method allows you to set the int data.
     * @param intData    The new int data.
     */
    public void SetIntegerData(int[] intData);

    /** This method allows you to set the int data, this can be used for
     * memetic algorithms.
     * @param intData    The new int data.
     */
    public void SetIntegerDataLamarckian(int[] intData);
}
