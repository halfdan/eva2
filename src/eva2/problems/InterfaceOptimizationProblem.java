package eva2.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;

import javax.swing.*;

/**
 *
 */
public interface InterfaceOptimizationProblem extends InterfaceAdditionalPopulationInformer {

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    Object clone();

    /**
     * This method initializes the Problem to log multiruns
     */
    void initializeProblem();

    /**
     * This method will report whether or not this optimization problem is truly
     * multi-objective
     *
     * @return True if multi-objective, else false.
     */
    boolean isMultiObjective();

    /**
     * This method initialized a given population
     *
     * @param population The populations that is to be initialized.
     */
    void initializePopulation(Population population);

    /**
     * This method evaluates a given population and sets the fitness values
     * accordingly. It also keeps track of the function call count.
     *
     * @param population The population that is to be evaluated.
     */
    void evaluate(Population population);

    /**
     * This method evaluate a single individual and sets the fitness values
     *
     * @param individual The individual that is to be evalutated
     */
    void evaluate(AbstractEAIndividual individual);

    /**
     * This method allows the GenericObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    String getName();

    /**
     * This method allows you to output a string that describes a found solution
     * in a way that is most suitable for a given problem.
     *
     * @param individual The individual that is to be shown.
     * @return The description.
     */
    String getSolutionRepresentationFor(AbstractEAIndividual individual);

    /**
     * This method returns a string describing the optimization problem.
     *
     * @param opt The Optimizer that is used or had been used.
     * @return The description.
     */
    String getStringRepresentationForProblem(InterfaceOptimizer opt);

    /**
     * This method allows you to request a graphical representation for a given
     * individual.
     */
    JComponent drawIndividual(int generation, int funCalls, AbstractEAIndividual indy);

    /**
     * This method returns a double value that will be displayed in a fitness
     * plot. A fitness that is to be minimized with a global min of zero
     * would be best, since log y can be used. But the value can depend on the problem.
     *
     * @param pop The population that is to be refined.
     * @return Double value
     */
    Double getDoublePlotValue(Population pop);

    /**
     * This method returns the dimension of the problem. Some problem implementations
     * may add a setProblemDimension() method, but as some problems have a fixed problem
     * dimension this is not added in this interface.
     */
    int getProblemDimension();
}
