package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 10.05.2005
 * Time: 14:11:49
 * To change this template use File | Settings | File Templates.
 */
public class MutateESSuccessRule implements InterfaceMutation, java.io.Serializable {

    // it would be quite nice to make this variable static, but in that case
    // no one could runs n independent ES runs in parallel anymore *sigh*
    // protected static double m_MutationStepSize    = 0.2;
    protected double        m_MutationStepSize    = 0.2;
    protected double        m_SuccessRate         = 0.2;
    protected double        m_Alpha               = 1.2;

    public MutateESSuccessRule() {

    }
    public MutateESSuccessRule(MutateESSuccessRule mutator) {
        this.m_MutationStepSize     = mutator.m_MutationStepSize;
        this.m_SuccessRate          = mutator.m_SuccessRate;
        this.m_Alpha                = mutator.m_Alpha;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateESSuccessRule(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESSuccessRule) {
            MutateESSuccessRule mut = (MutateESSuccessRule)mutator;
            if (this.m_MutationStepSize != mut.m_MutationStepSize) return false;
            if (this.m_SuccessRate != mut.m_SuccessRate) return false;
            if (this.m_Alpha != mut.m_Alpha) return false;
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
            for (int i = 0; i < x.length; i++) {
                x[i] += ((range[i][1] -range[i][0])/2)*RNG.gaussianDouble(this.m_MutationStepSize);
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
        return "ES 1/5 Success Rule mutation";
    }

    /** This method increases the mutation step size.
     */
    public void increaseMutationStepSize() {
        this.m_MutationStepSize = this.m_MutationStepSize * this.m_Alpha;
    }
    /** This method decrease the mutation step size.
     */
    public void decreaseMutationStepSize() {
        this.m_MutationStepSize = this.m_MutationStepSize / this.m_Alpha;
     }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "ES 1/5 Success Rule mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The 1/5 success rule is something special and works only together with an ES optimizer.";
    }

    /** Set the initial mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setInitialMutationStepSize(double d) {
        if (d < 0) d = 0.0000001;
        this.m_MutationStepSize = d;
    }
    public double getInitialMutationStepSize() {
        return this.m_MutationStepSize;
    }
    public String initialMutationStepSizeTipText() {
        return "Choose the initial mutation step size.";
    }

    /** Set success rate (0.2 is default).
     * @param d   The mutation operator.
     */
    public void setSuccessRate(double d) {
        if (d < 0) d = 0;
        if (d > 1) d = 1;
        this.m_SuccessRate = d;
    }
    public double getSuccessRate() {
        return this.m_SuccessRate;
    }
    public String successRateTipText() {
        return "Set success rate (0.2 is default).";
    }

    /** Choose the factor by which the mutation step size is to be increased/decrease.
     * @param d   The mutation operator.
     */
    public void setAlpha(double d) {
        if (d < 1) d = 1;
        this.m_Alpha = d;
    }
    public double getAlpha() {
        return this.m_Alpha;
    }
    public String alphaTipText() {
        return "Choose the factor by which the mutation step size is to be increased/decreased.";
    }
}