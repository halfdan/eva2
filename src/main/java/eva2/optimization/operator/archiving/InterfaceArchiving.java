package eva2.optimization.operator.archiving;

import eva2.optimization.population.Population;

/**
 * This Interface give the general methods required for an archiving method.
 */
public interface InterfaceArchiving {

    /**
     * This method allows you to make a deep clone of the object
     *
     * @return the deep clone
     */
    Object clone();

    /**
     * This method allows you to merge populations into an archive.
     * This method will add elements from pop to the archive but will also
     * remove elements from the archive if the archive target size is exceeded.
     *
     * @param pop The population that may add Individuals to the archive.
     */
    void addElementsToArchive(Population pop);
}
