package javaeva.server.go.operators.archiving;

import javaeva.server.go.populations.Population;

/** This interface gives the necessary methods for an information
 * retrieval algorithm.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.03.2004
 * Time: 14:54:06
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceInformationRetrieval {

    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone();

    /** This method will allow Information Retrieval from a archive onto
     * an already existing population.
     * @param pop           The population.
     */
    public void retrieveInformationFrom(Population pop);
}

