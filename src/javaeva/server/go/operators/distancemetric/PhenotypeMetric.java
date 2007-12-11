package javaeva.server.go.operators.distancemetric;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceDataTypeBinary;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.individuals.InterfaceDataTypeInteger;
import javaeva.server.go.individuals.InterfaceDataTypePermutation;
import javaeva.server.go.individuals.InterfaceDataTypeProgram;
import javaeva.server.go.individuals.codings.gp.InterfaceProgram;


import java.util.BitSet;

/** A phenotype metric suited for some of the most common
 * data types.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.07.2005
 * Time: 14:50:17
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeMetric implements InterfaceDistanceMetric, java.io.Serializable {

    public PhenotypeMetric() {
    }

    public PhenotypeMetric(PhenotypeMetric a) {
    }

    public Object clone() {
        return (Object) new PhenotypeMetric(this);
    }

    private int Minimum (int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    private int computeLevenshteinDistance (String s, String t) {
        int     d[][]; // matrix
        int     n; // length of s
        int     m; // length of t
        int     i; // iterates through s
        int     j; // iterates through t
        char    s_i; // ith character of s
        char    t_j; // jth character of t
        int     cost; // cost

        // Step 1
        n = s.length ();
        m = t.length ();
        if (n == 0) return m;
        if (m == 0) return n;
        d = new int[n+1][m+1];

        // Step 2
        for (i = 0; i <= n; i++) d[i][0] = i;
        for (j = 0; j <= m; j++) d[0][j] = j;

        // Step 3
        for (i = 1; i <= n; i++) {
            s_i = s.charAt (i - 1);
            // Step 4
            for (j = 1; j <= m; j++) {
                t_j = t.charAt (j - 1);
                // Step 5
                if (s_i == t_j) cost = 0;
                else {
                    // @todo
                    cost = 1;
                }
                // Step 6
                d[i][j] = Minimum (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);
            }
        }
        // Step 7
        return d[n][m];
    }

    /** This method allows you to compute the distance between two individuals.
     * Depending on the metric this method may reject some types of individuals.
     * The default return value would be 1.0.
     * @param indy1     The first individual.
     * @param indy2     The second individual.
     * @return double
     */
    public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        double      result = 0;
        if ((indy1 instanceof InterfaceDataTypeBinary) && (indy2 instanceof InterfaceDataTypeBinary)) {
            BitSet  b1, b2;
            int     l1, l2;
            double tmpResult = 0;
            b1 = ((InterfaceDataTypeBinary) indy1).getBinaryData();
            b2 = ((InterfaceDataTypeBinary) indy2).getBinaryData();
            l1 = ((InterfaceDataTypeBinary) indy1).size();
            l2 = ((InterfaceDataTypeBinary) indy2).size();
            for (int i = 0; (i < l1) && (i < l2); i++) {
                if (b1.get(i)==b2.get(i)) tmpResult += 1;
            }
            result += tmpResult/((double)Math.min(l1,l2));
        }
        if ((indy1 instanceof InterfaceDataTypeInteger) && (indy2 instanceof InterfaceDataTypeInteger)) {
            int[]   d1,d2;
            int[][] r1, r2;
            double  tmpResult = 0;
            d1 = ((InterfaceDataTypeInteger) indy1).getIntegerData();
            r1 = ((InterfaceDataTypeInteger) indy1).getIntRange();
            d2 = ((InterfaceDataTypeInteger) indy2).getIntegerData();
            r2 = ((InterfaceDataTypeInteger) indy2).getIntRange();
            for (int i = 0; (i < d1.length) && (i < d2.length); i++) {
                tmpResult += Math.abs(d1[i] - d2[i])/((double)(r1[i][1]-r1[i][0]));
            }
            result += Math.sqrt(tmpResult);
        }
        if ((indy1 instanceof InterfaceDataTypeDouble) && (indy2 instanceof InterfaceDataTypeDouble)) {
            double[]    dIndy1, dIndy2;
            double[][]  range1, range2;
            double      tmpResult = 0;
            dIndy1 = ((InterfaceDataTypeDouble) indy1).getDoubleData();
            range1 = ((InterfaceDataTypeDouble) indy1).getDoubleRange();
            dIndy2 = ((InterfaceDataTypeDouble) indy2).getDoubleData();
            range2 = ((InterfaceDataTypeDouble) indy2).getDoubleRange();
            for (int i = 0; (i < dIndy1.length) && (i < dIndy2.length); i++) {
                tmpResult += Math.pow(((dIndy1[i] - range1[i][0])/(range1[i][1] - range1[i][0])) - ((dIndy2[i] - range2[i][0])/(range2[i][1] - range2[i][0])), 2);
            }
            result += Math.sqrt(tmpResult);
        }
        if ((indy1 instanceof InterfaceDataTypePermutation) && (indy2 instanceof InterfaceDataTypePermutation)) {
            int[]    dIndy1, dIndy2;
            String   s1 = "", s2 = "";
            double  tmpResult = 0;
            for (int p = 0; p < ((InterfaceDataTypePermutation) indy1).getPermutationData().length; p++) {
              dIndy1 = ((InterfaceDataTypePermutation) indy1).getPermutationData()[p];
              dIndy2 = ((InterfaceDataTypePermutation) indy2).getPermutationData()[p];
              for (int i = 0; i < dIndy1.length; i++) s1 += dIndy1[i];
              for (int i = 0; i < dIndy2.length; i++) s2 += dIndy2[i];
              result += this.computeLevenshteinDistance(s1, s2)/((double)Math.max(s1.length(), s2.length()));
            }
        }
        if ((indy1 instanceof InterfaceDataTypeProgram) && (indy2 instanceof InterfaceDataTypeProgram)) {
            String  s1, s2;
            int     l1;
            l1 = Math.min(((InterfaceDataTypeProgram)indy1).getProgramData().length, ((InterfaceDataTypeProgram)indy2).getProgramData().length);
            for (int i = 0; i < l1; i++) {
                s1 = ((InterfaceDataTypeProgram)indy1).getProgramData()[i].getStringRepresentation();
                s2 = ((InterfaceDataTypeProgram)indy2).getProgramData()[i].getStringRepresentation();
                result += this.computeLevenshteinDistance(s1, s2)/((double)Math.max(s1.length(), s2.length()));
            }
        }

        return result;


    }
    
    public static double euclidianDistance(double[] v1, double[] v2) {
        double      result = 0;
        for (int i = 0; (i < v1.length) && (i < v2.length); i++) {
        	result += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(result);
    }
    
    public static double norm(double[] v1) {
        double      result = 0;
        for (int i = 0; i < v1.length; i++) {
        	result += Math.pow(v1[i], 2);
        }
        return Math.sqrt(result);
    }
    
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a phenotype based method suited for double data. Metric is computed on a normalized search space.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Phenotype Metric";
    }
}
