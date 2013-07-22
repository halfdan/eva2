package eva2.optimization.operator.archiving;

import eva2.optimization.population.Population;

/**
 * This Interface give the general methods required for a archiving method.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 05.06.2003
 * Time: 11:27:36
 * To change this template use Options | File Templates.
 */
public interface InterfaceArchiving {

    /**
     * This method allows you to make a deep clone of the object
     *
     * @return the deep clone
     */
    public Object clone();

    /**
     * This method allows you to merge to populations into an archive.
     * This method will add elements from pop to the archive but will also
     * remove elements from the archive if the archive target size is exceeded.
     *
     * @param pop The population that may add Individuals to the archive.
     */
    public void addElementsToArchive(Population pop);
}
