package eva2.server.go.individuals.codings.ga;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 16:47:06
 * To change this template use Options | File Templates.
 */
public interface InterfaceGADoubleCoding {
    /** This method decodes a part of a given BitSet into a double value. This method may change the contens
     * of the BitSet if it doesn't describe a valid value.
     * The method checks wether or not the value is within the given range.
     * @param refBitSet             The BitSet where the questioned value is stored.
     * @param range                 The allowed range of the value.
     * @param locus                 The position and length on the BitSet that is to be decoded.
     * @param correction            Enable automatic correction is enabled.
     * @return                      The float value.
     */
    public double decodeValue(BitSet refBitSet, double[] range, int[] locus, boolean correction);

    /** This method codes a given int value directly into a BitSet at
     * the position which is specified by locus.
     * The method checks wether or not the value is within the given range.
     * @param value             The value to be coded.
     * @param range             The allowed range of the value.
     * @param refBitSet         The BitSet where the questioned value is stored.
     * @param locus             The position and length on the BitSet that is to be coded.
     */
    public void codeValue(double value, double[] range, BitSet refBitSet, int[] locus);
}
