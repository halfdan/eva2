package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;

/**
 * Success rule implementation.
 * 
 * User: streiche
 * Date: 10.05.2005
 * Time: 14:11:49
 * To change this template use File | Settings | File Templates.
 */
public class MutateESSuccessRule extends MutateESStandard implements InterfaceMutation, java.io.Serializable {
/*
 * This is a bit of a cheat as the implementation does only hold some
 * more parameters while the ES strategy really acts on it.
 */
    // it would be quite nice to make this variable static, but in that case
    // no one could runs n independent ES runs in parallel anymore *sigh*
    // protected static double m_MutationStepSize    = 0.2;
//    protected double        m_MutationStepSize    = 0.2; // now in base class
    protected double        m_SuccessRate         = 0.2;
    protected double        m_Alpha               = 1.2;

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

    /** This method allows you to evaluate whether two mutation operators
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
        return "The 1/5 success rule works only together with an ES optimizer.";
    }

    public String mutationStepSizeTipText() {
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