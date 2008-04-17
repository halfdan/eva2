package eva2.server.go.operators.mutation;

import java.util.ArrayList;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.tools.RandomNumberGenerator;
import eva2.tools.SelectedTag;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 02.04.2003
 * Time: 16:29:47
 * To change this template use Options | File Templates.
 */
public class MutateESGlobal implements InterfaceMutation, java.io.Serializable {
    protected double      m_MutationStepSize    = 0.2;
    protected double      m_Tau1                = 0.15;
    protected double      m_LowerLimitStepSize  = 0.0000005;
    protected SelectedTag m_CrossoverType;

    public MutateESGlobal() {
        initTags();
    }

    public MutateESGlobal(double mutationStepSize) {
        initTags();
        setMutationStepSize(mutationStepSize);
    }
    
    public MutateESGlobal(MutateESGlobal mutator) {
        this.m_MutationStepSize     = mutator.m_MutationStepSize;
        this.m_Tau1                 = mutator.m_Tau1;
        this.m_LowerLimitStepSize   = mutator.m_LowerLimitStepSize;
        this.m_CrossoverType        = (SelectedTag)mutator.m_CrossoverType.clone();
    }

    protected void initTags() {
        this.m_CrossoverType = new SelectedTag(new String[]{"None", "Intermediate", "Discrete"});
    }
    
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new MutateESGlobal(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESGlobal) {
            MutateESGlobal mut = (MutateESGlobal)mutator;
            if (this.m_MutationStepSize != mut.m_MutationStepSize) return false;
            if (this.m_Tau1 != mut.m_Tau1) return false;
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
     * doesn't implement InterfaceESIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceESIndividual) {
            double[] x = ((InterfaceESIndividual)individual).getDGenotype();
            double[][] range = ((InterfaceESIndividual)individual).getDoubleRange();
            this.m_MutationStepSize = this.m_MutationStepSize * Math.exp(this.m_Tau1 * RandomNumberGenerator.gaussianDouble(1));
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
        ArrayList<Double> tmpList = new ArrayList<Double>();
        if (indy1.getMutationOperator() instanceof MutateESGlobal) tmpList.add(new Double(((MutateESGlobal)indy1.getMutationOperator()).m_MutationStepSize));
        for (int i = 0; i < partners.size(); i++) {
            if (((AbstractEAIndividual)partners.get(i)).getMutationOperator() instanceof MutateESGlobal) tmpList.add(new Double(((MutateESGlobal)((AbstractEAIndividual)partners.get(i)).getMutationOperator()).m_MutationStepSize));
        }
        double[] list = new double[tmpList.size()];
        for (int i = 0; i < tmpList.size(); i++) list[i] = ((Double)tmpList.get(i)).doubleValue();
        if (list.length <= 1) return;
        switch (this.m_CrossoverType.getSelectedTag().getID()) {
            case 1 : {
                this.m_MutationStepSize = 0;
                for (int i = 0; i < list.length; i++) this.m_MutationStepSize += list[i];
                this.m_MutationStepSize = this.m_MutationStepSize/(double)list.length;
                break;
            }
            case 2 : {
                this.m_MutationStepSize = list[RandomNumberGenerator.randomInt(0, list.length-1)];
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
        return "ES global mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "ES global mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The global mutation stores only one sigma for all double attributes.";
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
