package eva2.optimization.operators.mutation;

import eva2.optimization.go.PopulationInterface;
import eva2.optimization.enums.MutateESCrossoverTypeEnum;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceESIndividual;
import eva2.optimization.populations.Population;
import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 02.04.2003
 * Time: 16:29:47
 * To change this template use Options | File Templates.
 */
public class MutateESGlobal implements InterfaceMutation, java.io.Serializable, InterfaceAdditionalPopulationInformer {
    protected double      m_MutationStepSize    = 0.2;
    protected double      m_Tau1                = 0.15;
    protected double      m_LowerLimitStepSize  = 0.0000005;
    protected MutateESCrossoverTypeEnum m_CrossoverType = MutateESCrossoverTypeEnum.none;

    public MutateESGlobal() {
    }

    /**
     * Use given mutation step size and no crossover on strategy params.
     * 
     * @param mutationStepSize
     */
    public MutateESGlobal(double mutationStepSize) {
    	this(mutationStepSize, MutateESCrossoverTypeEnum.none);
    }

    /**
     * Use given mutation step size and given crossover type on strategy params.
     * 
     * @param mutationStepSize
     */
    public MutateESGlobal(double mutationStepSize, MutateESCrossoverTypeEnum coType) {
        setMutationStepSize(mutationStepSize);
        setCrossoverType(coType);
    }
    
    public MutateESGlobal(MutateESGlobal mutator) {
        this.m_MutationStepSize     = mutator.m_MutationStepSize;
        this.m_Tau1                 = mutator.m_Tau1;
        this.m_LowerLimitStepSize   = mutator.m_LowerLimitStepSize;
        this.m_CrossoverType        = mutator.m_CrossoverType;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateESGlobal(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateESGlobal) {
            MutateESGlobal mut = (MutateESGlobal)mutator;
            if (this.m_MutationStepSize != mut.m_MutationStepSize) {
                return false;
            }
            if (this.m_Tau1 != mut.m_Tau1) {
                return false;
            }
            if (this.m_LowerLimitStepSize != mut.m_LowerLimitStepSize) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceESIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
        if (individual instanceof InterfaceESIndividual) {
            double[] x = ((InterfaceESIndividual)individual).getDGenotype();
            double[][] range = ((InterfaceESIndividual)individual).getDoubleRange();
            this.m_MutationStepSize *= Math.exp(this.m_Tau1 * RNG.gaussianDouble(1));
            if (this.m_MutationStepSize < this.m_LowerLimitStepSize) {
                this.m_MutationStepSize = this.m_LowerLimitStepSize;
            }
            for (int i = 0; i < x.length; i++) {
                x[i] += ((range[i][1] -range[i][0])/2)*RNG.gaussianDouble(this.m_MutationStepSize);
                if (range[i][0] > x[i]) {
                    x[i] = range[i][0];
                }
                if (range[i][1] < x[i]) {
                    x[i] = range[i][1];
                }
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
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
    	if (m_CrossoverType!=MutateESCrossoverTypeEnum.none) {
    		ArrayList<Double> tmpList = new ArrayList<Double>();
    		if (indy1.getMutationOperator() instanceof MutateESGlobal) {
                tmpList.add(new Double(((MutateESGlobal)indy1.getMutationOperator()).m_MutationStepSize));
            }
    		for (int i = 0; i < partners.size(); i++) {
    			if (((AbstractEAIndividual)partners.get(i)).getMutationOperator() instanceof MutateESGlobal) {
                        tmpList.add(new Double(((MutateESGlobal)((AbstractEAIndividual)partners.get(i)).getMutationOperator()).m_MutationStepSize));
                    }
    		}
    		double[] list = new double[tmpList.size()];
    		for (int i = 0; i < tmpList.size(); i++) {
                list[i] = ((Double)tmpList.get(i)).doubleValue();
            }
    		if (list.length <= 1) {
                return;
            }

    		switch (this.m_CrossoverType) {
    		case intermediate : 
    			this.m_MutationStepSize = 0;
    			for (int i = 0; i < list.length; i++) {
                this.m_MutationStepSize += list[i];
            }
    			this.m_MutationStepSize /= (double)list.length;
    			break;
    		case discrete : 
    			this.m_MutationStepSize = list[RNG.randomInt(0, list.length-1)];
    			break;
    		case none : // do nothing
    		break;
    		}
    	}
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    @Override
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
    public static String globalInfo() {
        return "The global mutation stores only one sigma for all double attributes.";
    }

    /** Set the initial mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setMutationStepSize(double d) {
        if (d < 0) {
            d = this.m_LowerLimitStepSize;
        }
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
        if (d < 0) {
            d = 0;
        }
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
        if (d < 0) {
            d = 0;
        }
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
    public void setCrossoverType(MutateESCrossoverTypeEnum d) {
        this.m_CrossoverType = d;
    }
    public MutateESCrossoverTypeEnum getCrossoverType() {
        return this.m_CrossoverType;
    }
    public String crossoverTypeTipText() {
        return "Choose the crossover type for the strategy parameters.";
    }

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.problems.InterfaceAdditionalPopulationInformer#getAdditionalDataHeader()
	 */
    @Override
	public String[] getAdditionalDataHeader() {
		return new String[] {"sigma"};
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.problems.InterfaceAdditionalPopulationInformer#getAdditionalDataInfo()
	 */
    @Override
	public String[] getAdditionalDataInfo() {
		return new String[] {"The ES global mutation step size."};
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.go.problems.InterfaceAdditionalPopulationInformer#getAdditionalDataValue(eva2.server.go.PopulationInterface)
	 */
    @Override
	public Object[] getAdditionalDataValue(PopulationInterface pop) {
		return new Object[]{m_MutationStepSize};
	}
}
