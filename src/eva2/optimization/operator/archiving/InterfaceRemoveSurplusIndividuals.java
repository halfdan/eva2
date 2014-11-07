package eva2.optimization.operator.archiving;

import eva2.optimization.population.Population;

/**
 * This interface gives the method necessary for an aglorithm
 * which is use to reduce the size of an archive.
 */
public interface InterfaceRemoveSurplusIndividuals {

    /**
     * This method allows you to make a deep clone of
     * the object
     *
     * @return the deep clone
     */
    public Object clone();

    /**
     * This method will remove surplus individuals
     * from a given archive. Note archive will be altered!
     *
     * @param archive
     */
    public void removeSurplusIndividuals(Population archive);
}
