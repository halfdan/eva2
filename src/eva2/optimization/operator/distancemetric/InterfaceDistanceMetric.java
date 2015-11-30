package eva2.optimization.operator.distancemetric;


import eva2.optimization.individuals.AbstractEAIndividual;

/**
 * This metric methods is experimental and should be moved to the
 * optimization problem to allow problem specific distance metrics
 * between individuals.
 */
public interface InterfaceDistanceMetric {

    /**
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    Object clone();

    /**
     * This method allows you to compute the distance between two individuals.
     * Depending on the metric this method may reject some types of individuals.
     * The default return value would be one.
     *
     * @param indy1 The first individual.
     * @param indy2 The second individual.
     * @return double
     */
    double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2);
}
