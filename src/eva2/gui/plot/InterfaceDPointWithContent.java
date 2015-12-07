package eva2.gui.plot;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.problems.InterfaceOptimizationProblem;

/**
 *
 */
public interface InterfaceDPointWithContent {
    void setEAIndividual(AbstractEAIndividual indy);

    AbstractEAIndividual getEAIndividual();

    /**
     * This method allows you to set the according optimization problem
     *
     * @param problem InterfaceOptimizationProblem
     */
    void setProblem(InterfaceOptimizationProblem problem);

    InterfaceOptimizationProblem getProblem();

    /**
     * This method allows you to draw additional data of the individual
     */
    void showIndividual();
}
