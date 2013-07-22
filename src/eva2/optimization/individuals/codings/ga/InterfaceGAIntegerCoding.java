package eva2.optimization.individuals.codings.ga;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.05.2004
 * Time: 11:24:43
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceGAIntegerCoding {

    /**
     * This method decodes a part of a given BitSet into a int value. This method may change the contens
     * of the BitSet if it doesn't describe a valid value.
     * The method checks wether or not the value is within the given range.
     *
     * @param refBitSet  The BitSet where the questioned value is stored.
     * @param range      The allowed range of the value.
     * @param locus      The position and length on the BitSet that is to be decoded.
     * @param correction Enable automatic correction is enabled.
     * @return The float value.
     */
    public int decodeValue(BitSet refBitSet, int[] range, int[] locus, boolean correction);

    /**
     * This method codes a given int value directly into a BitSet at
     * the position which is specified by locus.
     * The method checks wether or not the value is within the given range.
     *
     * @param value     The value to be coded.
     * @param range     The allowed range of the value.
     * @param refBitSet The BitSet where the questioned value is stored.
     * @param locus     The position and length on the BitSet that is to be coded.
     */
    public void codeValue(int value, int[] range, BitSet refBitSet, int[] locus);

    /**
     * This method will calculate how many bits are to be used to code a given value
     *
     * @param range The range for the value.
     */
    public int calculateNecessaryBits(int[] range);
}
