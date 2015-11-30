package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;


/**
 *
 */
public interface InterfaceCrossover {

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    Object clone();

    /**
     * This method performs crossover on two individuals. Note: the genotype of the individuals
     * will be changed, so it would be better to use clones as arguments
     *
     * @param indy1    The first individual
     * @param partners The second individual
     */
    AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners);

    /**
     * This method will allow the crossover operator to be initialized depending on the
     * individual and the optimization problem. The optimization problem is to be stored
     * since it is to be called during crossover to calculate the exogene parameters for
     * the offsprings.
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt);

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    String getStringRepresentation();

    /**
     * This method allows you to evaluate wether two crossover operators
     * are actually the same.
     *
     * @param crossover The other crossover operator
     */
    @Override
    boolean equals(Object crossover);
}
