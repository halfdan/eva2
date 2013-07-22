package eva2.optimization.individuals.codings.ga;


import eva2.tools.math.RNG;

import java.util.BitSet;


/**
 * This gives the gray coding for double with a variable number of bits for coding
 * As far as i recall the least significant bit is to the left.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 18:39:51
 * To change this template use Options | File Templates.
 */
public class GAGrayCodingDouble implements InterfaceGADoubleCoding, java.io.Serializable {

    GAStandardCodingDouble m_HelpingHand = new GAStandardCodingDouble();

    /**
     * This method decodes a part of a given BitSet into a double value. This method may change the contens
     * of the BitSet if it doesn't describe a valid value.
     * The method checks wether or not the value is within the given range.
     *
     * @param refBitSet  The BitSet where the questioned value is stored.
     * @param range      The allowed range of the value.
     * @param locus      The position and length on the BitSet that is to be decoded.
     * @param correction Enable automatic correction is enabled.
     * @return The float value.
     */
    @Override
    public double decodeValue(BitSet refBitSet, double[] range, int[] locus, boolean correction) {
        BitSet tmpBitSet;
        int[] tmpLocus;
        boolean tmpB = false;

        tmpLocus = new int[2];
        tmpLocus[0] = 0;
        tmpLocus[1] = locus[1];
        tmpBitSet = new BitSet(tmpLocus.length);

        for (int i = 0; i < tmpLocus[1]; i++) {
            tmpB = refBitSet.get(locus[0]);
            for (int j = 1; j <= i; j++) {
                tmpB ^= refBitSet.get(locus[0] + j);
            }
            if (tmpB) {
                tmpBitSet.set(i);
            } else {
                tmpBitSet.clear(i);
            }
        }
        return this.m_HelpingHand.decodeValue(tmpBitSet, range, tmpLocus, correction);
    }

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
    @Override
    public void codeValue(double value, double[] range, BitSet refBitSet, int[] locus) {
        BitSet tmpBitSet;
        int[] tmpLocus;

        tmpLocus = new int[2];
        tmpLocus[0] = 0;
        tmpLocus[1] = locus[1];
        tmpBitSet = new BitSet(tmpLocus.length);
        this.m_HelpingHand.codeValue(value, range, tmpBitSet, tmpLocus);
        if (tmpBitSet.get(0)) {
            refBitSet.set(locus[0]);
        } else {
            refBitSet.clear(locus[0]);
        }
        for (int i = 1; i < locus[1]; i++) {
            if (tmpBitSet.get(i) ^ tmpBitSet.get(i - 1)) {
                refBitSet.set(locus[0] + i);
            } else {
                refBitSet.clear(locus[0] + i);
            }
        }
    }

    /**
     * A simple test method
     *
     * @param args no args needed
     */
    public static void main(String[] args) {
        GAGrayCodingDouble t = new GAGrayCodingDouble();

        double value, tmp;
        BitSet tmpBitSet = new BitSet();
        int[] locus = new int[2];
        locus[0] = 0;
        locus[1] = 32;
        double[] range = new double[2];
        range[0] = -10;
        range[1] = 10;
        for (int i = 0; i < 10; i++) {
            value = RNG.randomDouble(range[0], range[1]);
            tmp = value;
            System.out.println("Coding Value : " + value);
            t.codeValue(value, range, tmpBitSet, locus);
            value = t.decodeValue(tmpBitSet, range, locus, false);
            System.out.println("Decoded Value : " + value);
        }
    }

    /**
     * Perhaps a special output is necessary.
     *
     * @param b The BitString.
     * @return A printable String.
     */
    public String printBitSet(BitSet b) {
        return this.printBitSet(b, b.size());
    }

    /**
     * Perhaps a special output is necessary.
     *
     * @param b The BitString.
     * @return A printable String.
     */
    public String printBitSet(BitSet b, int length) {
        String output = "{";

        for (int i = 0; i < length; i++) {
            if (b.get(i)) {
                output += "1";
            } else {
                output += "0";
            }
        }
        output += "}\n";
        return output;
    }
}
