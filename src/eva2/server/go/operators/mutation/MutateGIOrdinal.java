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
 * Time: 15:53:03
 * To change this template use File | Settings | File Templates.
 */
public class MutateGIOrdinal implements InterfaceMutation, java.io.Serializable {

    double      m_StepSize = 0.1;
    int         m_NumberOfMutations = 2;

    public MutateGIOrdinal() {

    }
    public MutateGIOrdinal(MutateGIOrdinal mutator) {
        this.m_StepSize             = mutator.m_StepSize;
        this.m_NumberOfMutations    = mutator.m_NumberOfMutations;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateGIOrdinal();
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGIOrdinal) {
            MutateGIOrdinal mut = (MutateGIOrdinal)mutator;
            if (this.m_StepSize != mut.m_StepSize) return false;
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
            int         mutInd, mut;
            double      mutate;
            for (int k = 0; k < this.m_NumberOfMutations; k++) {
                mutInd = RNG.randomInt(0, x.length-1);
                mutate = RNG.gaussianDouble(this.m_StepSize);
                mutate = mutate * (range[mutInd][1] - range[mutInd][1]);
                mut = (int)Math.round(mutate);
                if (mut == 0) {
                    if (RNG.flipCoin(0.5)) mut = -1;
                    else mut = 1;
                }
                x[mutInd] += mut;
                if (x[mutInd] < range[mutInd][0]) x[mutInd] = range[mutInd][0];
                if (x[mutInd] > range[mutInd][1]) x[mutInd] = range[mutInd][1];
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
        return "GI ordinal mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GI ordinal mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The ordinal mutation alters n element of the int attributes based on an oridinal ordering.";
    }

    /** This method allows you to set the mean step size.
     * @param n     The step size
     */
    public void setStepSize(double n) {
        this.m_StepSize = n;
    }
    public double getStepSize() {
        return this.m_StepSize;
    }
    public String stepSizeTipText() {
        return "Gives the mean step size on a normalized search space.";
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