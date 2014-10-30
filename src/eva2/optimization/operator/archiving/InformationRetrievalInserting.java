package eva2.optimization.operator.archiving;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * This information retrieval inserts the retrieved
 * solutions, by removing random individuals from the
 * population.
 */
@Description("This Information Retrieval will insert the archive into current population by replacing random individuals if necessary.")
public class InformationRetrievalInserting implements InterfaceInformationRetrieval, java.io.Serializable {

    public InformationRetrievalInserting() {
    }

    public InformationRetrievalInserting(InformationRetrievalInserting a) {
    }

    @Override
    public Object clone() {
        return new InformationRetrievalInserting(this);
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
        if (archive.size() < pop.getTargetSize()) {
            // remove archive size individuals from pop
            pop.removeNIndividuals(archive.size() - (pop.getTargetSize() - pop.size()));
        } else {
            pop.clear();
        }

        pop.addPopulation((Population) archive.getClone());
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Inserting Information Retrieval";
    }

}
