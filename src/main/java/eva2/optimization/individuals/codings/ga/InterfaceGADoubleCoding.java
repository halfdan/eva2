package eva2.optimization.individuals.codings.ga;

import java.util.BitSet;

/**
 *
 */
public interface InterfaceGADoubleCoding {
    /**
     * This method decodes a part of a given BitSet into a double value. This method may change the contens
     * of the BitSet if it doesn't describe a valid value.
     * The method checks whether or not the value is within the given range.
     *
     * @param refBitSet  The BitSet where the questioned value is stored.
     * @param range      The allowed range of the value.
     * @param locus      The position and length on the BitSet that is to be decoded.
     * @param correction Enable automatic correction is enabled.
     * @return The float value.
     */
    double decodeValue(BitSet refBitSet, double[] range, int[] locus, boolean correction);

    /**
     * This method codes a given double value directly into a BitSet at
     * the position which is specified by locus.
     * The method checks whether or not the value is within the given range.
     *
     * @param value     The value to be coded.
     * @param range     The allowed range of the value.
     * @param refBitSet The BitSet where the questioned value is stored.
     * @param locus     The position and length on the BitSet that is to be coded.
     */
    void codeValue(double value, double[] range, BitSet refBitSet, int[] locus);
}
