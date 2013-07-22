package eva2.optimization.go;

import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.problems.InterfaceOptimizationProblem;

/**
 * Interface for a termination criterion.
 *
 * @author mkron, streiche
 */
public interface InterfaceTerminator {
    /**
     * Test a given population for convergence with the criterion defined by the instance.
     *
     * @param pop the population to test
     * @return true if the population fulfills the termination criterion, else false
     */
    public boolean isTerminated(PopulationInterface pop);

    public boolean isTerminated(InterfaceSolutionSet pop);

    @Override
    public String toString();

    public String lastTerminationMessage();

    public void init(InterfaceOptimizationProblem prob);
}