package eva2.server.go.operators.distancemetric;


import java.util.BitSet;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceGAIndividual;

/** Distance based on a bit-set if any.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.04.2003
 * Time: 18:21:01
 * To change this template use Options | File Templates.
 */
public class GenotypeMetricBitSet implements InterfaceDistanceMetric, java.io.Serializable {

    public GenotypeMetricBitSet() {
    }

    public GenotypeMetricBitSet(GenotypeMetricBitSet a) {
    }

    public Object clone() {
        return (Object) new GenotypeMetricBitSet(this);
    }

    /** This method allows you to compute the distance between two individuals.
     * Depending on the metric this method may reject some types of individuals.
     * The default return value would be 1.0.
     * @param indy1     The first individual.
     * @param indy2     The second individual.
     * @return double
     */
    public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
    	return GenotypeMetricBitSet.dist(indy1, indy2);
    }
    
    public static double dist(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        BitSet      dIndy1, dIndy2;
        double      result = 0;
        int         length = 0;

        if ((indy1 instanceof InterfaceGAIndividual) && (indy2 instanceof InterfaceGAIndividual)) {
            dIndy1 = ((InterfaceGAIndividual) indy1).getBGenotype();
            dIndy2 = ((InterfaceGAIndividual) indy2).getBGenotype();
            length = Math.min(((InterfaceGAIndividual) indy1).getGenotypeLength(), ((InterfaceGAIndividual) indy2).getGenotypeLength());
        } else return 1.0;

        for (int i = 0; i < length; i++) {
            if (dIndy1.get(i) == dIndy2.get(i)) result += 0;
            else result += 1;
        }
        return result/(double)length;
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a genotype based distance metric suited for binary data. The hamming distance is computed and normalized by chromosome length.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Binary Genotype Metric";
    }
}
