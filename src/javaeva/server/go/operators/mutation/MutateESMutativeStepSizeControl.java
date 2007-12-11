package javaeva.server.go.operators.mutation;

import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceESIndividual;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.tools.RandomNumberGenerator;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 10.09.2004
 * Time: 14:23:37
 * To change this template use File | Settings | File Templates.
 */
public class MutateESMutativeStepSizeControl implements InterfaceMutation, java.io.Serializable {
    protected double      m_MutationStepSize    = 0.2;
    protected double      m_Alpha               = 1.2;
    protected double      m_LowerLimitStepSize  = 0.0000005;

    public MutateESMutativeStepSizeControl() {

    }
    public MutateESMutativeStepSizeControl(MutateESMutativeStepSizeControl mutator) {
        this.m_MutationStepSize     = mutator.m_MutationStepSize;
        this.m_Alpha                = mutator.m_Alpha;
        this.m_LowerLimitStepSize   = mutator.m_LowerLimitStepSize;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateESMutativeStepSizeControl(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESMutativeStepSizeControl) {
            MutateESMutativeStepSizeControl mut = (MutateESMutativeStepSizeControl)mutator;
            if (this.m_MutationStepSize != mut.m_MutationStepSize) return false;
            if (this.m_Alpha != mut.m_Alpha) return false;
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
        if (individual instanceof InterfaceESIndividual) {
            double[] x = ((InterfaceESIndividual)individual).getDGenotype();
            double[][] range = ((InterfaceESIndividual)individual).getDoubleRange();
            if (RandomNumberGenerator.flipCoin(0.5))
                this.m_MutationStepSize = this.m_MutationStepSize * this.m_Alpha;
            else
                this.m_MutationStepSize = this.m_MutationStepSize / this.m_Alpha;
            if (this.m_MutationStepSize < this.m_LowerLimitStepSize) this.m_MutationStepSize = this.m_LowerLimitStepSize;
            for (int i = 0; i < x.length; i++) {
                x[i] += ((range[i][1] -range[i][0])/2)*RandomNumberGenerator.gaussianDouble(this.m_MutationStepSize);
                if (range[i][0] > x[i]) x[i] = range[i][0];
                if (range[i][1] < x[i]) x[i] = range[i][1];
            }
            ((InterfaceESIndividual)individual).SetDGenotype(x);

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
        return "ES mutative step size control";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "MSR";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The mutative step size control mutation randomly increases/decreases the current step size.";
    }

    /** Set the initial mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setMutationStepSize(double d) {
        if (d < 0) d = this.m_LowerLimitStepSize;
        this.m_MutationStepSize = d;
    }
    public double getMutationStepSize() {
        return this.m_MutationStepSize;
    }
    public String mutationStepSizeTipText() {
        return "Choose the initial mutation step size.";
    }

    /** Set the lower limit for the mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setLowerLimitStepSize(double d) {
        if (d < 1) d = 1;
        this.m_LowerLimitStepSize = d;
    }
    public double getLowerLimitStepSize() {
        return this.m_LowerLimitStepSize;
    }
    public String lowerLimitStepSizeTipText() {
        return "Set the lower limit for the mutation step size.";
    }

    /** Set the value for Alpha with this method.
     * @param d   The mutation operator.
     */
    public void setAlpha(double d) {
        if (d < 0) d = 0;
        this.m_Alpha = d;
    }
    public double getAlpha() {
        return this.m_Alpha;
    }
    public String alphaTipText() {
        return "Set the value for alpha.";
    }
}
