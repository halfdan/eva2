package eva2.optimization.operator.selection.replacement;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;

/**
 * Replacement schemes for the steady-state GA.
 */
public interface InterfaceReplacement {

    /**
     * The ever present clone method
     */
    Object clone();

    /**
     * This method will insert the given individual into the population
     * by replacing a individual either from the population or the given
     * subset
     *
     * @param indy The individual to insert
     * @param pop  The population
     * @param sub  The subset
     */
    void insertIndividual(AbstractEAIndividual indy, Population pop, Population sub);
}
