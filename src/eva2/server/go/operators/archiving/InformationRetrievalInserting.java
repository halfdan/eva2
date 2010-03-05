package eva2.server.go.operators.archiving;

import eva2.server.go.populations.Population;

/** This information retrieval inserts the retrieved
 * solutions, by removing random individuals from the
 * population.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.03.2004
 * Time: 14:55:18
 * To change this template use File | Settings | File Templates.
 */
public class InformationRetrievalInserting implements InterfaceInformationRetrieval, java.io.Serializable {

    public InformationRetrievalInserting() {
    }

    public InformationRetrievalInserting(InformationRetrievalInserting a) {
    }

    public Object clone() {
        return (Object) new InformationRetrievalInserting(this);
    }

    /** This method will allow Information Retrieval from a archive onto
     * an already existing population.
     * @param pop           The population.
     */
    public void retrieveInformationFrom(Population pop) {
        Population archive = pop.getArchive();

        if (archive == null) return;
        if (archive.size() < pop.getTargetSize()) {
            // remove archive size individuals from pop
            pop.removeNIndividuals(archive.size()-(pop.getTargetSize()-pop.size()));
        } else {
            pop.clear();
        }

        pop.addPopulation((Population)archive.getClone());
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This Information Retrieval will insert the archive into current population by replacing random individuals if necessary.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Inserting Information Retrieval";
    }

}
