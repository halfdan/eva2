package eva2.gui.plot;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.problems.InterfaceOptimizationProblem;

/**
 *
 */
public interface InterfaceDPointWithContent {
    public void setEAIndividual(AbstractEAIndividual indy);

    public AbstractEAIndividual getEAIndividual();

    /**
     * This method allows you to set the according optimization problem
     *
     * @param problem InterfaceOptimizationProblem
     */
    public void setProblem(InterfaceOptimizationProblem problem);

    public InterfaceOptimizationProblem getProblem();

    /**
     * This method allows you to draw additional data of the individual
     */
    public void showIndividual();
}
