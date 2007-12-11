package javaeva.server.go.operators.archiving;

import javaeva.server.go.populations.Population;

/** This class allows no information retrieval and thus no elitism
 * for MOEAs.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.03.2004
 * Time: 14:54:33
 * To change this template use File | Settings | File Templates.
 */
public class InformationRetrievalNone implements InterfaceInformationRetrieval, java.io.Serializable {

    public InformationRetrievalNone() {
    }

    public InformationRetrievalNone(InformationRetrievalNone a) {
    }

    public Object clone() {
        return (Object) new InformationRetrievalNone(this);
    }

    /** This method will allow Information Retrieval from a archive onto
     * an already existing population.
     * @param pop           The population.
     */
    public void retrieveInformationFrom(Population pop) {
        // no InterfaceInformation Retrieval is performed
        return;
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This implements a deactivated Information Retrieval.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "No Information Retrieval";
    }
}
