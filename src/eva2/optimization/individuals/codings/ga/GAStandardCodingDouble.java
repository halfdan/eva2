package eva2.optimization.individuals.codings.ga;

import java.util.BitSet;

/**
 * This is the traditional binary coding for double value, which allows arbitrary number of
 * bits per variable. But i guess currently the GUI only allows to set the number of bits for
 * all variables at the same time.
 */
public class GAStandardCodingDouble implements InterfaceGADoubleCoding, java.io.Serializable {
    private double lastMaxVal = 1;
    private int lastLen = 0;

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
    public double decodeValueOld(BitSet refBitSet, double[] range, int[] locus, boolean correction) {
        double u_max, u_min;
        int mStart, mLength;
        long tmpV;
        double output;
        //BitSet      tmpBitSet;

        u_min = range[0];
        u_max = range[1];
        mStart = locus[0];
        mLength = locus[1];

        if (mLength != lastLen) {
            lastMaxVal = Math.pow(2, mLength) - 1;
            lastLen = mLength;
        }

        tmpV = 0;
        for (int i = 0; i < mLength; i++) {
            if (refBitSet.get(mStart + mLength - 1 - i)) {
                tmpV += Math.pow(2, i);
            }
        }
        output = (((double) tmpV * (u_max - u_min)) / lastMaxVal) + u_min;
        return output;
    }

    /**
     * This method decodes a part of a given BitSet into a double value.
     *
     * @param refBitSet  The BitSet where the questioned value is stored.
     * @param range      The allowed range of the value.
     * @param locus      The position and length on the BitSet that is to be decoded.
     * @param correction Enable automatic correction is enabled.
     * @return The decoded value.
     */
    @Override
    public double decodeValue(BitSet refBitSet, double[] range, int[] locus, boolean correction) {
        long val = (refBitSet.get(locus[0]) ? 1 : 0);
        int mLength = locus[1];

        if (mLength != lastLen) {
            lastMaxVal = Math.pow(2, mLength) - 1;
            lastLen = mLength;
        }

        for (int i = 1 + locus[0]; i < locus[0] + locus[1]; i++) {
            val *= 2.;
            if (refBitSet.get(i)) {
                val += 1.;
            }
        }

        return range[0] + ((range[1] - range[0]) * val) / lastMaxVal;
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
        double uMax, uMin, mMax, mMin;
        int m_start, m_length, counter = 0;
        long tmpV;
        BitSet tmpBitSet;

        uMin = range[0];
        uMax = range[1];
        m_start = locus[0];
        m_length = locus[1];
        mMax = Math.pow(2, m_length) - 1;
        mMin = 0;
        // here the value will be quantified
        tmpV = Math.round((((value - uMin) * mMax / (uMax - uMin))));
        tmpBitSet = new BitSet(m_length);
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
        //System.out.print("FLOAT Value coded : " + value + " " + this.printBitSet(tmpBitSet, m_length));
        //System.out.println(tmpV + "/" + m_max + "*(" + u_max + "-" + u_min + ")+" + u_min +"\n");
        for (int i = 0; i < m_length; i++) {
            if (tmpBitSet.get(i)) {
                refBitSet.set(m_start + m_length - 1 - i);
            } else {
                refBitSet.clear(m_start + m_length - 1 - i);
            }
        }
    }

    /**
     * A simple test function without arguments
     */
    public static void main() {
        GAStandardCodingDouble t = new GAStandardCodingDouble();
        String test = "01100010001000110010000011111101";
        //String  test = "11000000000000000000000000000000";
        //String  test = "11000000000000000000000000000000";
        double value;
        BitSet tmpBitSet = new BitSet();
        int[] locus = new int[2];
        locus[0] = 10;
        locus[1] = 12;
        double[] range = new double[2];
        range[0] = -110;
        range[1] = 1000;
        for (int i = 0; i < test.length(); i++) {
            if (test.charAt(i) == '1') {
                tmpBitSet.set(i);
            } else {
                tmpBitSet.clear(i);
            }
        }
//        value = t.decodeValue(tmpBitSet, range, locus, false);
//        System.out.println("Value: " + value);
//        System.out.println("BitSet: " + t.printBitSet(tmpBitSet, 32));
//        t.codeValue(value, range, tmpBitSet, locus);
//        value = t.decodeValue(tmpBitSet, range, locus, false);
//        System.out.println("Value: " + value);
//        System.out.println("BitSet: " + t.printBitSet(tmpBitSet, 32));
//        t.codeValue(value, range, tmpBitSet, locus);
//        value = t.decodeValue(tmpBitSet, range, locus, false);
//        System.out.println("Value: " + value);
//        System.out.println("BitSet: " + t.printBitSet(tmpBitSet, 32));
        for (int i = 0; i < 1000; i++) {
            value = t.decodeValueOld(tmpBitSet, range, locus, false);
            System.out.println("Value def: " + value);
            value = t.decodeValue(tmpBitSet, range, locus, false);
            System.out.println("Value alt: " + value);
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
