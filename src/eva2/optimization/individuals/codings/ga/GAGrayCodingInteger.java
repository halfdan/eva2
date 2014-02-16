package eva2.optimization.individuals.codings.ga;

import java.util.BitSet;

/**
 * This is a gray coding for integers, sorry no variable number of bits here.
 */
public class GAGrayCodingInteger implements InterfaceGAIntegerCoding, java.io.Serializable {

    GAStandardCodingInteger standardCodingInteger = new GAStandardCodingInteger();

    /**
     * This method decodes a part of a given BitSet into a int value. This method may change the contens
     * of the BitSet if it doesn't describe a valid value.
     * The method checks wether or not the value is within the given range.
     *
     * @param refBitSet  The BitSet where the integer value is stored.
     * @param range      The allowed range of the value.
     * @param locus      The position and length on the BitSet that is to be decoded.
     * @param correction Enable automatic correction is enabled.
     * @return The int value.
     */
    @Override
    public int decodeValue(BitSet refBitSet, int[] range, int[] locus, boolean correction) {
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
        return this.standardCodingInteger.decodeValue(tmpBitSet, range, tmpLocus, correction);
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
    public void codeValue(int value, int[] range, BitSet refBitSet, int[] locus) {
        BitSet tmpBitSet;
        int[] tmpLocus;

        tmpLocus = new int[2];
        tmpLocus[0] = 0;
        tmpLocus[1] = locus[1];
        tmpBitSet = new BitSet(tmpLocus.length);
        this.standardCodingInteger.codeValue(value, range, tmpBitSet, tmpLocus);

//        if (tmpBitSet.get(0)) refBitSet.set(locus[1]);
//        else refBitSet.clear(locus[1]);
//        for (int i = 1; i < locus.length; i++) {
//            if (tmpBitSet.get(i)^tmpBitSet.get(i-1)) refBitSet.set(locus[1] + i);
//            else refBitSet.clear(locus[1] + i);
//        }
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
     * This method will calculate how many bits are to be used to code a given value
     *
     * @param range The range for the value.
     */
    @Override
    public int calculateNecessaryBits(int[] range) {
        return this.standardCodingInteger.calculateNecessaryBits(range);
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
