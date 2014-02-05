package eva2.optimization.individuals.codings.ga;


import eva2.tools.math.RNG;

import java.util.BitSet;


/**
 * The traditional binary coding for integer number, no variable number of bits here, sorry.
 */
public class GAStandardCodingInteger implements InterfaceGAIntegerCoding, java.io.Serializable {

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
        int u_max, u_min;
        int start, mLength;
        long tmpV;
        BitSet tmpBitSet;
        String output = "";

        u_min = range[0];
        u_max = range[1];
        start = locus[0];
        mLength = locus[1];
        tmpBitSet = new BitSet(mLength);
        tmpV = 0;
        for (int i = 0; i < mLength; i++) {
            if (refBitSet.get(start + mLength - 1 - i)) {
                tmpV += Math.pow(2, i);
                tmpBitSet.set(mLength - 1 - i);
                output += "1";
            } else {
                output += "0";
            }
        }
        //System.out.println(tmpV);
        tmpV += u_min;
        //System.out.println("Korrigiere: " + tmpV + " " + u_min + " " + u_max + " " + output);
        if (tmpV > u_max) {
            // this value is invalid
            //System.out.print("Korrigiere: " + tmpV + " > " + u_max);
            if (correction) {
                // a new value within the bounds is generated
                tmpV = RNG.randomInt(u_min, u_max);
                //System.out.println("zu: " + tmpV);
                codeValue((int) tmpV, range, refBitSet, locus);
            } else {
                tmpV = u_max;
                //System.out.println("zu max: " + tmpV);
            }
        }
        return (int) tmpV;
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
        int uMax, uMin, mMax, mMin;
        int start, length, counter = 0;
        long tmpV;
        BitSet tmpBitSet;

        uMin = range[0];
        uMax = range[1];
        start = locus[0];
        length = locus[1];
        mMax = (int) Math.pow(2, length) - 1;
        mMin = 0;
        tmpV = value - uMin;
        long tmpOut = tmpV;// damit ist tmpV im range mMin mMax
        if (tmpV > mMax) {
            tmpV = mMax;
        }
        if (tmpV < mMin) {
            tmpV = mMin;
        }
        tmpBitSet = new BitSet(length);
        while (tmpV >= 1) {
            //System.out.println(tmpV);
            if ((tmpV % 2) == 1) {
                tmpV--;
                tmpBitSet.set(counter);
                //System.out.println("set 1");
            } else {
                tmpBitSet.clear(counter);
                //System.out.println("set 0");
            }
            counter++;
            tmpV /= 2;
            // with this method the least significant bit will be at the lowest position
        }
        // Das sieht bis hierher richtig toll aus, nur jetzt wirds scheisse length war im Arsch
        for (int i = 0; i < length; i++) {
            if (tmpBitSet.get(i)) {
                refBitSet.set(start + length - 1 - i);
            } else {
                refBitSet.clear(start + length - 1 - i);
            }
        }
        for (int i = 0; i < length; i++) {
            if (refBitSet.get(start + length - 1 - i)) {
                tmpBitSet.set(length - 1 - i);
            } else {
                tmpBitSet.clear(start + length - 1 - i);
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
        int result = 0;
        double maxStore = 1. + range[1] - range[0];

        while (Math.pow(2, result) < maxStore) result++;

        return result;
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
