package eva2.optimization.operator.selection.replacement;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * This method replaces the worst indvidual in the population.
 */
@Description("This method replaces the worst individual from the population.")
public class ReplaceWorst implements InterfaceReplacement, java.io.Serializable {

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
        AbstractEAIndividual worst = pop.getWorstEAIndividual();
        if (pop.remove(worst)) {
            pop.addIndividual(indy);
        }
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    public String getName() {
        return "Worst Replace";
    }
}
