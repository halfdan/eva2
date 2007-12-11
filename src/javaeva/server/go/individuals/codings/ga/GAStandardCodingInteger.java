package javaeva.server.go.individuals.codings.ga;

import javaeva.server.go.tools.RandomNumberGenerator;

import java.util.BitSet;

/** The traditional binary coding for integer number, no variable number of bits here, sorry.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 16:38:45
 * To change this template use Options | File Templates.
 */
public class GAStandardCodingInteger implements InterfaceGAIntegerCoding, java.io.Serializable {

    /** This method decodes a part of a given BitSet into a int value. This method may change the contens
     * of the BitSet if it doesn't describe a valid value.
     * The method checks wether or not the value is within the given range.
     * @param refBitSet             The BitSet where the integer value is stored.
     * @param range                 The allowed range of the value.
     * @param locus                 The position and length on the BitSet that is to be decoded.
     * @param correction            Enable automatic correction is enabled.
     * @return The int value.
     */
    public int decodeValue(BitSet refBitSet, int[] range, int[] locus, boolean correction) {
        int     u_max, u_min, m_max, m_min;
        int     m_start, m_length, counter = 0;
        long    tmpV;
        BitSet  tmpBitSet;
        String  output = "";

        u_min       = range[0];
        u_max       = range[1];
        m_start     = locus[0];
        m_length    = locus[1];
        m_max       = (int)Math.pow(2, m_length) - 1;
        m_min       = 0;
        tmpBitSet   = new BitSet(m_length);
        tmpV        = 0;
        for (int i = 0; i < m_length; i++) {
            if (refBitSet.get(m_start + m_length - 1 - i)) {
                tmpV += Math.pow(2, i);
                tmpBitSet.set(m_length - 1 - i);
                output += "1";
            } else {
                output += "0";
            }
        }
        //System.out.println(tmpV);
        tmpV = tmpV + u_min;
        //System.out.println("Korregiere: " + tmpV + " " + u_min + " " + u_max + " " + output);
        if (tmpV > u_max) {
            // this value is invalid
            //System.out.print("Korregiere: " + tmpV + " > " + u_max);
            if (correction) {
                // a new value within the bounds is generated
                tmpV = RandomNumberGenerator.randomInt(u_min, u_max);
                //System.out.println("zu: " + tmpV);
                codeValue((int)tmpV, range, refBitSet, locus);
            } else {
                tmpV = u_max;
                //System.out.println("zu max: " + tmpV);
            }
        }
        //System.out.println("INT Value decoded : " + (int)tmpV + " " + this.printBitSet(tmpBitSet, m_length));
        return (int)tmpV;
    }

    /** This method codes a given int value directly into a BitSet at
     * the position which is specified by locus.
     * The method checks wether or not the value is within the given range.
     * @param value             The value to be coded.
     * @param range             The allowed range of the value.
     * @param refBitSet         The BitSet where the questioned value is stored.
     * @param locus             The position and length on the BitSet that is to be coded.
     */
    public void codeValue(int value, int[] range, BitSet refBitSet, int[] locus) {
        int     u_max, u_min, m_max, m_min;
        int     m_start, m_length, counter = 0;
        long    tmpV;
        BitSet  tmpBitSet;

        u_min       = range[0];
        u_max       = range[1];
        m_start     = locus[0];
        m_length    = locus[1];
        m_max       = (int)Math.pow(2, m_length) - 1;
        m_min       = 0;
        tmpV        = value - u_min;
        long tmpOut = tmpV;// damit ist tmpV im range m_Min m_Max
        if (tmpV > m_max) tmpV = m_max;
        if (tmpV < m_min) tmpV = m_min;
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
            tmpV = tmpV / 2;
            // with this method the least significant bit will be at the lowest position
        }
        //System.out.println("tmpV " + tmpOut + " Range("+m_min+";"+m_max+") "+m_length+" "+this.printBitSet(tmpBitSet,m_length));
        // Das sieht bis hierher richtig toll aus, nur jetzt wirds scheisse m_Length war im Arsch
        for (int i = 0; i < m_length; i++) {
            if (tmpBitSet.get(i)) refBitSet.set(m_start + m_length - 1 - i);
            else refBitSet.clear(m_start + m_length - 1 - i);
        }
        for (int i = 0; i < m_length; i++) {
            if (refBitSet.get(m_start + m_length - 1 - i)) tmpBitSet.set(m_length - 1 - i);
            else tmpBitSet.clear(m_start + m_length - 1 - i);
        }
        //System.out.println("INT Value coded : " + value + " " + this.printBitSet(tmpBitSet, m_length));
    }

    /** This method will calculate how many bits are to be used to code a given value
     * @param range     The range for the value.
     */
    public int calculateNecessaryBits(int[] range) {
        int result = 0;
        int maxStore = 1 + range[1] -range[0];

        while (Math.pow(2, result) < maxStore) result++;

        return result;
    }

    /** Perhaps a special output is necessary.
     * @param b    The BitString.
     * @return          A printable String.
     */
    public String printBitSet(BitSet b) {
        return this.printBitSet(b, b.size());
    }

    /** Perhaps a special output is necessary.
     * @param b    The BitString.
     * @return          A printable String.
     */
    public String printBitSet(BitSet b, int length) {
        String  output = "{";

        for (int i = 0; i < length; i++) {
            if (b.get(i)) output += "1";
            else output += "0";
        }
        output += "}\n";
        return output;
    }
}
