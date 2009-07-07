package eva2.server.go.operators.mutation;


import java.util.ArrayList;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;
import eva2.tools.SelectedTag;
import eva2.tools.Tag;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 02.04.2003
 * Time: 17:58:30
 * To change this template use Options | File Templates.
 */
public class MutateESLocal implements InterfaceMutation, java.io.Serializable {

    protected double        m_MutationStepSize    = 0.2;
    protected double        m_Tau1                = 0.15;
    protected double        m_LowerLimitStepSize  = 0.0000005;
    private double[]        m_Sigmas;
    protected double        m_Tau2      = 0.15;
    protected SelectedTag   m_CrossoverType;

    public MutateESLocal() {
        this.m_Sigmas = null;
        Tag[] tag = new Tag[3];
        tag[0] = new Tag(0, "None");
        tag[1] = new Tag(1, "Intermediate");
        tag[2] = new Tag(2, "Discrete");
        this.m_CrossoverType = new SelectedTag(0, tag);
    }

    public MutateESLocal(MutateESLocal mutator) {
        if (mutator.m_Sigmas != null) {
            this.m_Sigmas = new double[mutator.m_Sigmas.length];
            for (int i = 0; i < this.m_Sigmas.length; i++) {
                this.m_Sigmas[i] = mutator.m_Sigmas[i];
            }
        }
        this.m_MutationStepSize     = mutator.m_MutationStepSize;
        this.m_Tau1                 = mutator.m_Tau1;
        this.m_Tau2                 = mutator.m_Tau2;
        this.m_LowerLimitStepSize   = mutator.m_LowerLimitStepSize;
        this.m_CrossoverType        = (SelectedTag)mutator.m_CrossoverType.clone();
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateESLocal(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
    	if (mutator==this) return true;
        if (mutator instanceof MutateESLocal) {
            MutateESLocal mut = (MutateESLocal)mutator;
            if (this.m_Tau1 != mut.m_Tau1) return false;
            if (this.m_Tau2 != mut.m_Tau2) return false;
            if (this.m_LowerLimitStepSize != mut.m_LowerLimitStepSize) return false;
            if (this.m_Sigmas != null) {
                for (int i = 0; i < this.m_Sigmas.length; i++) {
                    if (this.m_Sigmas[i] != mut.m_Sigmas[i]) return false;
                }
            } else return false;
            return true;
        } else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        if (individual instanceof InterfaceESIndividual) {
            // init the Sigmas
            this.m_Sigmas = new double[((InterfaceESIndividual)individual).getDGenotype().length];
            for (int i = 0; i < this.m_Sigmas.length; i++) this.m_Sigmas[i] = this.m_MutationStepSize;
        }
    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceESIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());

        if (individual instanceof InterfaceESIndividual) {
            double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
            double[][]  range   = ((InterfaceESIndividual)individual).getDoubleRange();
            double      tmpR    = RNG.gaussianDouble(1);

            for (int i = 0; i < x.length; i++) {
                this.m_Sigmas[i] = this.m_Sigmas[i] * Math.exp(this.m_Tau1 * tmpR + this.m_Tau2 * RNG.gaussianDouble(1));
                if (this.m_Sigmas[i] < this.m_LowerLimitStepSize) this.m_Sigmas[i] = this.m_LowerLimitStepSize;
                x[i] += ((range[i][1] -range[i][0])/2)*RNG.gaussianDouble(this.m_Sigmas[i]);
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
        ArrayList tmpListA = new ArrayList();
        ArrayList tmpListB = new ArrayList();
        if (indy1.getMutationOperator() instanceof MutateESLocal) {
            tmpListA.add(new Double(((MutateESLocal)indy1.getMutationOperator()).m_MutationStepSize));
            tmpListB.add(((MutateESLocal)indy1.getMutationOperator()).m_Sigmas);
        }
        for (int i = 0; i < partners.size(); i++) {
            if (((AbstractEAIndividual)partners.get(i)).getMutationOperator() instanceof MutateESLocal) {
                tmpListA.add(new Double(((MutateESLocal)((AbstractEAIndividual)partners.get(i)).getMutationOperator()).m_MutationStepSize));
                tmpListB.add(((MutateESLocal)((AbstractEAIndividual)partners.get(i)).getMutationOperator()).m_Sigmas);
            }
        }
        double[] listA = new double[tmpListA.size()];
        double[][] listB = new double[tmpListA.size()][];
        for (int i = 0; i < tmpListA.size(); i++) {
            listA[i] = ((Double)tmpListA.get(i)).doubleValue();
            listB[i] = (double[])tmpListB.get(i);
        }
        if (listA.length <= 1) return;
        switch (this.m_CrossoverType.getSelectedTag().getID()) {
            case 1 : {
                this.m_MutationStepSize = 0;
                for (int i = 0; i < this.m_Sigmas.length; i++) this.m_Sigmas[i] = 0;
                for (int i = 0; i < listA.length; i++) {
                    this.m_MutationStepSize += listA[i];
                    for (int j = 0; j < this.m_Sigmas.length; j++) this.m_Sigmas[j] += listB[i][j];
                }
                this.m_MutationStepSize = this.m_MutationStepSize/(double)listA.length;
                for (int i = 0; i < this.m_Sigmas.length; i++) this.m_Sigmas[i] = this.m_Sigmas[i]/(double)listA.length;
                break;
            }
            case 2 : {
                int rn = RNG.randomInt(0, listA.length-1);
                this.m_MutationStepSize = listA[rn];
                for (int i = 0; i < this.m_Sigmas.length; i++) {
                    rn = RNG.randomInt(0, listA.length-1);
                    this.m_Sigmas[i] = listB[rn][i];
                }
                break;
            }
            default : {
                // do nothing
            }
        }
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    public String getStringRepresentation() {
        return "ES local mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "ES local mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The local mutation stores n sigmas for each double attribute.";
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

    /** Set the value for tau1 with this method.
     * @param d   The mutation operator.
     */
    public void setCrossoverType(SelectedTag d) {
        this.m_CrossoverType = d;
    }
    public SelectedTag getCrossoverType() {
        return this.m_CrossoverType;
    }
    public String crossoverTypeTipText() {
        return "Choose the crossover type for the strategy parameters.";
    }
}
