package eva2.optimization.strategies;

import eva2.optimization.population.Population;

/**
 * An interface for optimizers which are to be notified in case of species
 * based optimization; namely merging and split events.
 *
 * @author mkron
 */
public interface InterfaceSpeciesAware {
    // these can be used to tag a population as explorer or local search population.
    String populationTagKey = "specAwarePopulationTag";
    Integer explorerPopTag = 23;
    Integer localPopTag = 42;

    /**
     * Two species have been merged to the first one.
     *
     * @param p1
     * @param p2
     */
    void mergeToFirstPopulationEvent(Population p1, Population p2);

    /**
     * Notify that a split has occurred separating p2 from p1.
     *
     * @param p1
     * @param p2
     */
    void splitFromFirst(Population p1, Population p2);
}
