package eva2.optimization.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.InterfaceOptimizer;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 18.03.2003
 * Time: 09:52:52
 * To change this template use Options | File Templates.
 */
public interface InterfaceOptimizationProblem extends InterfaceAdditionalPopulationInformer {

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public Object clone();

    /** This method inits the Problem to log multiruns
     */
    public void initializeProblem();

    /** This method will report whether or not this optimization problem is truly
     * multi-objective
     * @return True if multi-objective, else false.
     */
    public boolean isMultiObjective();    

/******************** The most important methods ****************************************/

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public void initializePopulation(Population population);

    /** This method evaluates a given population and set the fitness values
     * accordingly
     * @param population    The population that is to be evaluated.
     */
    public void evaluate(Population population);

    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evalutated
     */
    public void evaluate(AbstractEAIndividual individual);

    /******************** Some output methods *******************************************/

    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName();  

    /** This method allows you to output a string that describes a found solution
     * in a way that is most suiteable for a given problem.
     * @param individual    The individual that is to be shown.
     * @return The description.
     */
    public String getSolutionRepresentationFor(AbstractEAIndividual individual);

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    public String getStringRepresentationForProblem(InterfaceOptimizer opt);

    /** This method allows you to request a graphical representation for a given
     * individual.
     */
    public JComponent drawIndividual(int generation, int funCalls, AbstractEAIndividual indy);

    /** This method returns a double value that will be displayed in a fitness
     * plot. A fitness that is to be minimized with a global min of zero
     * would be best, since log y can be used. But the value can depend on the problem.
     * @param pop   The population that is to be refined.
     * @return Double value
     */
    public Double getDoublePlotValue(Population pop);

}
