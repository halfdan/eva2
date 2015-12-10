package eva2.optimization.operator.selection.replacement;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * The preselection replacement scheme replaces the worst parent in case
 * the offspring is better.
 */
@Description("This method replaces the worst parent if better.")
public class ReplacePreselection implements InterfaceReplacement, java.io.Serializable {

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
        if ((indy.isDominatingDebConstraints(worst)) && (pop.remove(worst))) {
            pop.addIndividual(indy);
        }
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Preselection";
    }
}
