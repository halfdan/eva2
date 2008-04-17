package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGPIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 16:38:31
 * To change this template use Options | File Templates.
 */
public class MutateGPDefault implements InterfaceMutation, java.io.Serializable {

    public MutateGPDefault() {

    }
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateGPDefault();
    }
    
    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGPDefault) return true;
        else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    public void mutate(AbstractEAIndividual individual) {
//        System.out.println("Before Mutate: " +((InterfaceGPIndividual)individual).getPGenotype()[0].getStringRepresentation());
//        System.out.println("Length:        " +((InterfaceGPIndividual)individual).getPGenotype()[0].getNumberOfNodes());
        if (individual instanceof InterfaceGPIndividual) ((InterfaceGPIndividual)individual).defaultMutate();
//        System.out.println("After Mutate:  " +((InterfaceGPIndividual)individual).getPGenotype()[0].getStringRepresentation());
//        System.out.println("Length:        " +((InterfaceGPIndividual)individual).getPGenotype()[0].getNumberOfNodes());
    }

    /** This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     * @param indy1     The original mother
     * @param partners  The original partners
     */
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    public String getStringRepresentation() {
        return "GP default mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GP default mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The default mutation replaces a random subtree using init, also called headless chicken mutation.";
    }
}
