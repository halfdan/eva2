package eva2.optimization.operator.terminators;

import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceOptimizationProblem;

/**
 * Interface for a termination criterion.
 */
public interface InterfaceTerminator {
    /**
     * Test a given population for convergence with the criterion defined by the instance.
     *
     * @param pop the population to test
     * @return true if the population fulfills the termination criterion, else false
     */
    boolean isTerminated(PopulationInterface pop);

    boolean isTerminated(InterfaceSolutionSet pop);

    @Override
    String toString();

    String lastTerminationMessage();

    void initialize(InterfaceOptimizationProblem prob);
}