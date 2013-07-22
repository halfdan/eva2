package eva2.optimization.operator.initialization;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.problems.InterfaceOptimizationProblem;

/**
 * An abstraction over individual initialization methods in analogy
 * to mutation and crossover. It is member of every abstract individual
 * but the specific implementation may depend on the data type of the
 * individual.
 *
 * @author mkron
 */
public interface InterfaceInitialization {

    /**
     * Perform initialization of a given individual, which may potentially depend on
     * a problem instance.
     *
     * @param indy    the target individual to initialize
     * @param problem the problem instance under consideration
     */
    public void initialize(AbstractEAIndividual indy, InterfaceOptimizationProblem problem);

    /**
     * A specific cloning method.
     *
     * @return
     */
    public InterfaceInitialization clone();
}
