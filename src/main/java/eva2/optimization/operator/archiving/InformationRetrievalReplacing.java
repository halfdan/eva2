package eva2.optimization.operator.archiving;

import eva2.optimization.population.Population;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * This information retrieval method simply add the retrieved solutions
 * to the current population.
 */
@Description("This Information Retrieval will replace the current population by the archive.")
public class InformationRetrievalReplacing implements InterfaceInformationRetrieval, java.io.Serializable {

    public InformationRetrievalReplacing() {
    }

    public InformationRetrievalReplacing(InformationRetrievalReplacing a) {
    }

    @Override
    public Object clone() {
        return new InformationRetrievalReplacing(this);
    }

    /**
     * This method will allow Information Retrieval from a archive onto
     * an already existing population.
     *
     * @param pop The population.
     */
    @Override
    public void retrieveInformationFrom(Population pop) {
        Population archive = pop.getArchive();
        if (archive == null) {
            return;
        }
        Population tmp = new Population();

        tmp.addPopulation(archive);
        while (tmp.size() < archive.getTargetSize()) {
            tmp.add(pop.get(RNG.randomInt(0, pop.size() - 1)));
        }

        pop.clear();
        pop.addPopulation(tmp);
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Replacing Information Retrieval";
    }

}
