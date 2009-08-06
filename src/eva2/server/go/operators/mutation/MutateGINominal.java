package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGIIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.05.2005
 * Time: 16:02:32
 * To change this template use File | Settings | File Templates.
 */
public class MutateGINominal implements InterfaceMutation, java.io.Serializable {

    int         m_NumberOfMutations = 2;

    public MutateGINominal() {

    }
    public MutateGINominal(MutateGINominal mutator) {
        this.m_NumberOfMutations     = mutator.m_NumberOfMutations;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateGINominal();
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGINominal) {
            MutateGINominal mut = (MutateGINominal)mutator;
            if (this.m_NumberOfMutations != mut.m_NumberOfMutations) return false;
            return true;
        }
        else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt){

    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    public void mutate(AbstractEAIndividual individual) {
        if (individual instanceof InterfaceGIIndividual) {
            int[]       x = ((InterfaceGIIndividual)individual).getIGenotype();
            int[][]     range = ((InterfaceGIIndividual)individual).getIntRange();
            int         mutInd = 0;
            for (int k = 0; k < this.m_NumberOfMutations; k++) {
                try {
                    mutInd = RNG.randomInt(0, x.length-1);
                } catch (java.lang.ArithmeticException e) {
                    System.out.println("x.length " + x.length);
                }
                x[mutInd] = RNG.randomInt(range[mutInd][0], range[mutInd][1]);
            }
            ((InterfaceGIIndividual)individual).SetIGenotype(x);
        }
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
        return "GI nominal mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GI nominal mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The nominal mutation alters n element of the int attributes completely at random.";
    }

    /** This method allows you to set the number of mutations to occur.
     * @param n     The number of mutations
     */
    public void setNumberOfMutations(int n) {
        this.m_NumberOfMutations = n;
    }
    public int getNumberOfMutations() {
        return this.m_NumberOfMutations;
    }
    public String numberOfMutationsTipText() {
        return "Gives the number of mutations.";
    }
}