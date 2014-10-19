package eva2.optimization.operator.archiving;

import eva2.optimization.population.Population;

/**
 * This class allows no information retrieval and thus no elitism
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

    @Override
    public Object clone() {
        return new InformationRetrievalNone(this);
    }

    /**
     * This method will allow Information Retrieval from a archive onto
     * an already existing population.
     *
     * @param pop The population.
     */
    @Override
    public void retrieveInformationFrom(Population pop) {
        // no InterfaceInformation Retrieval is performed
        return;
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This implements a deactivated Information Retrieval.";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "No Information Retrieval";
    }
}
