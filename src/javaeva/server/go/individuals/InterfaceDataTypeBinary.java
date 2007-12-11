package javaeva.server.go.individuals;

import java.util.BitSet;

/** This interface gives access to a binary phenotype and except
 * for problemspecific operators should only be used by the
 * optimization problem.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 10:57:31
 * To change this template use Options | File Templates.
 */
public interface InterfaceDataTypeBinary {

    /** This method allows you to request a certain amount of binary data
     * @param length    The lenght of the BitSet that is to be optimized
     */
    public void setBinaryDataLength (int length);

    /** This method returns the length of the binary data set
     * @return The number of bits stored
     */
    public int size();

    /** This method allows you to read the binary data
     * @return BitSet representing the binary data.
     */
    public BitSet getBinaryData();

    /** This method allows you to read the binary data without
     * an update from the genotype
     * @return BitSet representing the binary data.
     */
    public BitSet getBinaryDataWithoutUpdate();

    /** This method allows you to set the binary data.
     * @param binaryData    The new binary data.
     */
    public void SetBinaryData(BitSet binaryData);

    /** This method allows you to set the binary data, this can be used for
     * memetic algorithms.
     * @param binaryData    The new binary data.
     */
    public void SetBinaryDataLamarkian(BitSet binaryData);
}
