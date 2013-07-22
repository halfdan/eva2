package eva2.optimization.operator.selection.replacement;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;

/**
 * This method replaces the worst parent. Doesn't work,
 * most likely it has the effect of a negative elitism.
 * Good selected parents are replaced by the offspring
 * regardless how bad it is...
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.07.2005
 * Time: 14:45:23
 * To change this template use File | Settings | File Templates.
 */
public class ReplaceWorstParent implements InterfaceReplacement, java.io.Serializable {

    /**
     * The ever present clone method
     */
    @Override
    public Object clone() {
        return new ReplaceRandom();
    }

    /**
     * This method will insert the given individual into the population
     * by replacing a individual either from the population or the given
     * subset
     *
     * @param indy The individual to insert
     * @param pop  The population
     * @param sub  The subset
     */
    @Override
    public void insertIndividual(AbstractEAIndividual indy, Population pop, Population sub) {
        AbstractEAIndividual worst = sub.getWorstEAIndividual();
        if (indy.isDominatingDebConstraints(worst)) {
            if (pop.remove(worst)) {
                pop.addIndividual(indy);
            }
        }
//        if (pop.remove(worst)) {
//            pop.addIndividual(indy);
//        }
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
        return "This method replaces the worst parent, if better.";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Worst Parent Replace";
    }
}
