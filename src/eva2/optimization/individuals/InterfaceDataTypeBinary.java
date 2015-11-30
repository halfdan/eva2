package eva2.optimization.individuals;

import java.util.BitSet;

/**
 * This interface gives access to a binary phenotype and except
 * for problem specific operators should only be used by the
 * optimization problem.
 */
public interface InterfaceDataTypeBinary {

    /**
     * This method allows you to request a certain amount of binary data
     *
     * @param length The lenght of the BitSet that is to be optimized
     */
    void setBinaryDataLength(int length);

    /**
     * This method returns the length of the binary data set
     *
     * @return The number of bits stored
     */
    int size();

    /**
     * This method allows you to read the binary data
     *
     * @return BitSet representing the binary data.
     */
    BitSet getBinaryData();

    /**
     * This method allows you to read the binary data without
     * an update from the genotype
     *
     * @return BitSet representing the binary data.
     */
    BitSet getBinaryDataWithoutUpdate();

    /**
     * This method allows you to set the binary data.
     *
     * @param binaryData The new binary data.
     */
    void setBinaryPhenotype(BitSet binaryData);

    /**
     * This method allows you to set the binary data, this can be used for
     * memetic algorithms.
     *
     * @param binaryData The new binary data.
     */
    void setBinaryGenotype(BitSet binaryData);
}
