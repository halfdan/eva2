package eva2.optimization.operator.distancemetric;


import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 * Distance based on a bit-set if any.
 */
@Description("This is a genotype based distance metric suited for binary data. The hamming distance is computed and normalized by chromosome length.")
public class GenotypeMetricBitSet implements InterfaceDistanceMetric, java.io.Serializable {

    public GenotypeMetricBitSet() {}

    @Override
    public Object clone() {
        return new GenotypeMetricBitSet();
    }

    /**
     * This method allows you to compute the distance between two individuals.
     * Depending on the metric this method may reject some types of individuals.
     * The default return value would be 1.0.
     *
     * @param indy1 The first individual.
     * @param indy2 The second individual.
     * @return double
     */
    @Override
    public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        return GenotypeMetricBitSet.dist(indy1, indy2);
    }

    public static double dist(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        BitSet dIndy1, dIndy2;
        double result = 0;
        int length = 0;

        if ((indy1 instanceof InterfaceGAIndividual) && (indy2 instanceof InterfaceGAIndividual)) {
            dIndy1 = ((InterfaceGAIndividual) indy1).getBGenotype();
            dIndy2 = ((InterfaceGAIndividual) indy2).getBGenotype();
            length = Math.min(((InterfaceGAIndividual) indy1).getGenotypeLength(), ((InterfaceGAIndividual) indy2).getGenotypeLength());
        } else {
            return 1.0;
        }

        for (int i = 0; i < length; i++) {
            if (dIndy1.get(i) == dIndy2.get(i)) {
                result += 0;
            } else {
                result += 1;
            }
        }
        return result / (double) length;
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Binary Genotype Metric";
    }
}
