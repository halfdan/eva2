package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;


/**
 *
 */
public interface InterfaceMutation {

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    public Object clone();

    /**
     * This method allows you to initialize the mutation operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt);

    /**
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement the proper interface nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    public void mutate(AbstractEAIndividual individual);

    /**
     * This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     *
     * @param indy1    The original mother
     * @param partners The original partners
     */
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners);

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    public String getStringRepresentation();

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator);
}
