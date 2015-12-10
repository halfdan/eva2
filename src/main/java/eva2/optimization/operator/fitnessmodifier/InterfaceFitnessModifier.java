package eva2.optimization.operator.fitnessmodifier;

import eva2.optimization.population.Population;

/**
 * The fitness modifier are defunct and are to be moved to
 * the selection operators...
 */
public interface InterfaceFitnessModifier {

    /**
     * This method allows you to modify the fitness of the individuals
     * of a population. Note that by altering the fitness you may require
     * your problem to store the unaltered fitness somewhere else so that
     * you may still fetch it!
     */
    void modifyFitness(Population population);

}
