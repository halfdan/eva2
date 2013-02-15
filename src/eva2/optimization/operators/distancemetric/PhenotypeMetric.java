package eva2.optimization.operators.distancemetric;



import eva2.gui.BeanInspector;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.individuals.InterfaceDataTypeInteger;
import eva2.optimization.individuals.InterfaceDataTypePermutation;
import eva2.optimization.individuals.InterfaceDataTypeProgram;
import java.util.BitSet;

/** 
 * A phenotype metric suited for the most common data types.
 * 
 * User: streiche
 * Date: 19.07.2005
 */
public class PhenotypeMetric implements InterfaceDistanceMetric, java.io.Serializable {
	private static PhenotypeMetric pMetric = null;
	private static GenotypeMetricBitSet bitMetric = null;
	
    public PhenotypeMetric() {
    }

    public PhenotypeMetric(PhenotypeMetric a) {
    }

    @Override
    public Object clone() {
        return (Object) new PhenotypeMetric(this);
    }

    private static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    private static int computeLevenshteinDistance (String s, String t) {
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
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n+1][m+1];

        // Step 2
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3
        for (i = 1; i <= n; i++) {
            s_i = s.charAt (i - 1);
            // Step 4
            for (j = 1; j <= m; j++) {
                t_j = t.charAt (j - 1);
                // Step 5
                if (s_i == t_j) {
                    cost = 0;
                }
                else {
                    // @todo
                    cost = 1;
                }
                // Step 6
                d[i][j] = min(d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);
            }
        }
        // Step 7
        return d[n][m];
    }

    /** 
     * This method allows you to compute the distance between two individuals.
     * Depending on the metric this method may reject some types of individuals.
     * The default return value would be 1.0.
     * @param indy1     The first individual.
     * @param indy2     The second individual.
     * @return double
     */
    @Override
    public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        double      result = 0;
        // results are added up because individuals can implement several data types!
        if ((indy1 instanceof InterfaceDataTypeBinary) && (indy2 instanceof InterfaceDataTypeBinary)) {
            if (bitMetric == null) {
                bitMetric = new GenotypeMetricBitSet();
            }
            result += bitMetric.distance(indy1, indy2);
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
                tmpResult += Math.pow(((d1[i] - r1[i][0])/((double)(r1[i][1]-r1[i][0]))) - ( (d2[i] - r2[i][0])/((double)(r2[i][1]-r2[i][0]))), 2);
                //tmpResult += Math.abs(d1[i] - d2[i])/((double)(r1[i][1]-r1[i][0]));
            }
            result += Math.sqrt(tmpResult);
        }
        if ((indy1 instanceof InterfaceDataTypeDouble) && (indy2 instanceof InterfaceDataTypeDouble)) {
            double[]    d1, d2;
            double[][]  r1, r2;
            double      tmpResult = 0, tmp=0;
            d1 = ((InterfaceDataTypeDouble) indy1).getDoubleData(); // TODO WithoutUpdate would be much quicker - but in which cases is it up to date?
            r1 = ((InterfaceDataTypeDouble) indy1).getDoubleRange();
            d2 = ((InterfaceDataTypeDouble) indy2).getDoubleData();
            r2 = ((InterfaceDataTypeDouble) indy2).getDoubleRange();
            for (int i = 0; (i < d1.length) && (i < d2.length); i++) {
            	tmp=((d1[i] - r1[i][0])/(r1[i][1] - r1[i][0])) - ((d2[i] - r2[i][0])/(r2[i][1] - r2[i][0]));
                tmpResult += (tmp*tmp);
            }
            result += Math.sqrt(tmpResult);
        }
        if ((indy1 instanceof InterfaceDataTypePermutation) && (indy2 instanceof InterfaceDataTypePermutation)) {
            int[]    dIndy1, dIndy2;
            String   s1 = "", s2 = "";
//            double  tmpResult = 0;
            for (int p = 0; p < ((InterfaceDataTypePermutation) indy1).getPermutationData().length; p++) {
              dIndy1 = ((InterfaceDataTypePermutation) indy1).getPermutationData()[p];
              dIndy2 = ((InterfaceDataTypePermutation) indy2).getPermutationData()[p];
              for (int i = 0; i < dIndy1.length; i++) {
                    s1 += dIndy1[i];
                }
              for (int i = 0; i < dIndy2.length; i++) {
                    s2 += dIndy2[i];
                }
              result += PhenotypeMetric.computeLevenshteinDistance(s1, s2)/((double)Math.max(s1.length(), s2.length()));
            }
        }
        if ((indy1 instanceof InterfaceDataTypeProgram) && (indy2 instanceof InterfaceDataTypeProgram)) {
            String  s1, s2;
            int     l1;
            l1 = Math.min(((InterfaceDataTypeProgram)indy1).getProgramData().length, ((InterfaceDataTypeProgram)indy2).getProgramData().length);
            for (int i = 0; i < l1; i++) {
                s1 = ((InterfaceDataTypeProgram)indy1).getProgramData()[i].getStringRepresentation();
                s2 = ((InterfaceDataTypeProgram)indy2).getProgramData()[i].getStringRepresentation();
                result += PhenotypeMetric.computeLevenshteinDistance(s1, s2)/((double)Math.max(s1.length(), s2.length()));
            }
        }
        return result;
    }
    
    /** This method allows you to compute the distance between two individuals.
     * Depending on the metric this method may reject some types of individuals.
     * The default return value would be 1.0.
     * @param indy1     The first individual.
     * @param indy2     The second individual.
     * @return double
     */
    public static double dist(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
    	if (pMetric == null) {
            pMetric = new PhenotypeMetric();
        }
    	return pMetric.distance(indy1, indy2);
    }

    public static double norm(AbstractEAIndividual indy) {
        double      result = 0;
        if (indy instanceof InterfaceDataTypeBinary) {
        	BitSet bs = (BitSet)((InterfaceDataTypeBinary)indy).getBinaryData();
        	for (int i = 0; (i < ((InterfaceDataTypeBinary)indy).size()) ; i++) {
                if (bs.get(i)) {
                        result += 1;
                    }
            }
        	result /= ((InterfaceDataTypeBinary)indy).size();
        	return result;
        }
        if (indy instanceof InterfaceDataTypeInteger) {
            int[]   d1 = ((InterfaceDataTypeInteger) indy).getIntegerData();
            for (int i = 0; i < d1.length; i++) {
				result += d1[i];
			}
            return result/d1.length;
        }
        if (indy instanceof InterfaceDataTypeDouble) {
        	result = norm(((InterfaceDataTypeDouble) indy).getDoubleData());
        	return result;
        }
        if (indy instanceof InterfaceDataTypePermutation) {
        	// TODO hard to find a norm for permutations. As we use the levenshtein distance metric,
        	// the normed distance to the empty permutaton is always one... 
            return 1;
        }
        if (indy instanceof InterfaceDataTypeProgram) {
        	// TODO same as for permutations
            return 1;
        }
        System.err.println("error: unknown individual interface in PhenotypeMetric::norm " + BeanInspector.toString(indy));
        return 0;
    }
    
    /**
     * Calculates the 2 norm of a given vector.
     * @param v1
     * @return
     */
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
    public static String globalInfo() {
        return "This is a phenotype based metric which can be applied to binary, integer, double, permutation, and program data types. For the latter two, the Levenshtein distance is computed. All distance values are normed.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Phenotype Metric";
    }
}
