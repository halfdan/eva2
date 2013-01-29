package eva2.server.go.operators.selection.replacement;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;

/** The preselection replacement scheme replaces the worst parent in case
 * the offspring is better.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.07.2005
 * Time: 15:24:03
 * To change this template use File | Settings | File Templates.
 */
public class ReplacePreselection implements InterfaceReplacement, java.io.Serializable {

    /** The ever present clone method
     */
    @Override
    public Object clone() {
        return new ReplaceRandom();
    }

    /** This method will insert the given individual into the population
     * by replacing a individual either from the population or the given
     * subset
     * @param indy      The individual to insert
     * @param pop       The population
     * @param sub       The subset
     */
    @Override
    public void insertIndividual(AbstractEAIndividual indy, Population pop, Population sub) {
        AbstractEAIndividual worst = sub.getWorstEAIndividual();
        if ((indy.isDominatingDebConstraints(worst)) && (pop.remove(worst))) {
            pop.addIndividual(indy);
        }
    }
    /**********************************************************************************************************************
     * These are for GUI
     */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This method replaces the worst parent if better.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Preselection";
    }
}
