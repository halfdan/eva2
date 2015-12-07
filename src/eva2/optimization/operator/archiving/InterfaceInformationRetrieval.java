package eva2.optimization.operator.archiving;

import eva2.optimization.population.Population;

/**
 * This interface gives the necessary methods for an information
 * retrieval algorithm.
 */
public interface InterfaceInformationRetrieval {

    /**
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    Object clone();

    /**
     * This method will allow Information Retrieval from a archive onto
     * an already existing population.
     *
     * @param pop The population.
     */
    void retrieveInformationFrom(Population pop);
}

