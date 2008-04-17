package eva2.server.go.operators.mutation;


import java.util.BitSet;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.individuals.InterfaceGPIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.tools.RandomNumberGenerator;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.06.2003
 * Time: 16:52:48
 * To change this template use Options | File Templates.
 */
public class MutateGPAdaptive implements InterfaceMutation, java.io.Serializable {
    protected double      m_MutationStep        = 1;
    protected double      m_Tau1                = 0.15;
    protected double      m_Tau2                = 0.15;
    protected double      m_LowerLimitStepSize  = 0.0000005;

    public MutateGPAdaptive() {

    }
    public MutateGPAdaptive(MutateGPAdaptive mutator) {
        this.m_MutationStep         = mutator.m_MutationStep;
        this.m_Tau1                 = mutator.m_Tau1;
        this.m_Tau2                 = mutator.m_Tau2;
        this.m_LowerLimitStepSize   = mutator.m_LowerLimitStepSize;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateGPAdaptive(this);
    }   

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGPAdaptive) {
            MutateGPAdaptive mut = (MutateGPAdaptive)mutator;
            if (this.m_MutationStep != mut.m_MutationStep) return false;
            if (this.m_Tau1 != mut.m_Tau1) return false;
            if (this.m_Tau2 != mut.m_Tau2) return false;
            if (this.m_LowerLimitStepSize != mut.m_LowerLimitStepSize) return false;
            return true;
        } else return false;
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
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceGPIndividual) {
            this.m_MutationStep = this.m_MutationStep * Math.exp(this.m_Tau1 * RandomNumberGenerator.gaussianDouble(1) + this.m_Tau2 * RandomNumberGenerator.gaussianDouble(1));
            if (this.m_MutationStep < this.m_LowerLimitStepSize) this.m_MutationStep = this.m_LowerLimitStepSize;
            if (this.m_MutationStep > 1) this.m_MutationStep = 1;
            if (RandomNumberGenerator.flipCoin(this.m_MutationStep)) ((InterfaceGPIndividual)individual).defaultMutate();
        }
        //System.out.println("After Mutate:  " +((GAIndividual)individual).getSolutionRepresentationFor());
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
        return "GP adaptive mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GP adaptive mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This mutation replaces a random node with a new random subtree.";
    }

    /** Set the initial mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setMutationStep(double d) {
        if (d < 0) d = this.m_LowerLimitStepSize;
        this.m_MutationStep = d;
    }
    public double getMutationStepSize() {
        return this.m_MutationStep;
    }
    public String mutationStepSizeTipText() {
        return "Choose the initial mutation step size.";
    }

    /** Set the lower limit for the mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setLowerLimitStepSize(double d) {
        if (d < 0) d = 0;
        this.m_LowerLimitStepSize = d;
    }
    public double getLowerLimitStepSize() {
        return this.m_LowerLimitStepSize;
    }
    public String lowerLimitStepSizeTipText() {
        return "Set the lower limit for the mutation step size.";
    }

    /** Set the value for tau1 with this method.
     * @param d   The mutation operator.
     */
    public void setTau1(double d) {
        if (d < 0) d = 0;
        this.m_Tau1 = d;
    }
    public double getTau1() {
        return this.m_Tau1;
    }
    public String tau1TipText() {
        return "Set the value for tau1.";
    }

    /** Set the value for tau2 with this method.
     * @param d   The mutation operator.
     */
    public void setTau2(double d) {
        if (d < 0) d = 0;
        this.m_Tau2 = d;
    }
    public double getTau2() {
        return this.m_Tau2;
    }
    public String tau2TipText() {
        return "Set the value for tau2.";
    }
}
