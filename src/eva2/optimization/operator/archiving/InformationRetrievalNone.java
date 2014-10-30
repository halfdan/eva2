package eva2.optimization.operator.archiving;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * This class allows no information retrieval and thus no elitism
 * for MOEAs.
 */
@Description("This implements a deactivated Information Retrieval.")
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

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "No Information Retrieval";
    }
}
