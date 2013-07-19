package eva2.optimization.operator.selection.replacement;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.tools.math.RNG;

/** Random replacement.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.07.2005
 * Time: 14:41:15
 * To change this template use File | Settings | File Templates.
 */
public class ReplaceRandom implements InterfaceReplacement, java.io.Serializable {

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
        int rand = RNG.randomInt(0, pop.size()-1);
        pop.remove(rand);
        pop.addIndividual(indy);
    }
    /**********************************************************************************************************************
     * These are for GUI
     */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This method replaces a random individual.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Random Replace";
    }
}
