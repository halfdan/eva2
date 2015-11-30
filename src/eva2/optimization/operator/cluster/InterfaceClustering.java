package eva2.optimization.operator.cluster;

import eva2.optimization.population.Population;

/**
 * This the interface to clustering algorithms, but since there
 * is no true concept on how to calculate a possibly problem
 * specific distance between two individuals, this is still to
 * be considered as under construction.
 */
public interface InterfaceClustering {

    /**
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    Object clone();

    /**
     * This method allows you to search for clusters in a given population. The method
     * returns Number of populations. The first population contains all individuals that
     * could not be associated with any cluster and may be empty.
     * All other populations group individuals into clusters.
     * It should make sure that the returned Population instances are of the same type
     * as the given one, which may be a subclass of Population.
     * If the clustering depends on population measures, a reference set may be given
     * which is the reference population to consider the measures of. This is for cases
     * where, e.g., subsets of a Population are to be clustered using measures of the
     * original population.
     * Note that the clustered individuals should only be shallow instances of the members
     * of the given population pop. The sum of sizes of all returned individuals must be
     * equal to pop.size().
     *
     * @param pop          The population of individuals that is to be clustered.
     * @param referenceSet a reference population for dynamic measures
     * @return Population[]
     */
    Population[] cluster(Population pop, Population referenceSet);

    /**
     * This method allows you to decide if two species are to be merged regarding this clustering algorithm.
     * If the clustering depends on population measures, a reference set may be given
     * which is the reference population to consider the measures of. This is for cases
     * where, e.g., subsets of a Population are to be clustered using measures of the
     * original population.
     *
     * @param species1     The first species.
     * @param species2     The second species.
     * @param referenceSet a reference population for dynamic measures
     * @return True if species converge, else False.
     */
    boolean mergingSpecies(Population species1, Population species2, Population referenceSet);

    /**
     * Do some pre-calculations on a population for clustering. If additional population data
     * is set, return the associated key, otherwise null.
     *
     * @param pop
     */
    String initClustering(Population pop);

    /**
     * Try to associate a set of loners with a given set of species. Return a list
     * of indices assigning loner i with species j for all loners. If no species can
     * be associated, -1 is returned as individual entry.
     * Note that the last cluster threshold is used which may have depended on the last
     * generation.
     * If the clustering depends on population measures, a reference set may be given
     * which is the reference population to consider the measures of. This is for cases
     * where, e.g., subsets of a Population are to be clustered using measures of the
     * original population.
     *
     * @param loners
     * @param species
     * @param referenceSet a reference population for dynamic measures
     * @return associative list matching loners to species.
     */
    int[] associateLoners(Population loners, Population[] species, Population referenceSet);
}
