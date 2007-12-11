package javaeva.server.go.operators.archiving;

import javaeva.server.go.populations.Population;

/** This interface gives the method necessary for an aglorithm
 * which is use to reduce the size of an archive.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.05.2004
 * Time: 14:35:20
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceRemoveSurplusIndividuals {
    
    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone();

    /** This method will remove surplus individuals
     * from a given archive. Note archive will be altered!
     * @param archive
     */
    public void removeSurplusIndividuals(Population archive);
}
