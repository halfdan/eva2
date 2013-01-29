package eva2.server.go.operators.crossover;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: Dante Alighieri
 * Date: 21.05.2005
 * Time: 11:36:38
 * To change this template use File | Settings | File Templates.
 */
public class CrossoverEAMixer implements InterfaceCrossover, InterfaceEvaluatingCrossoverOperator, java.io.Serializable  {
	public static final String CROSSOVER_EA_MIXER_OPERATOR_KEY = "CrossoverEAMixerOperatorKey";
	
    protected PropertyCrossoverMixer  m_Crossers;
    protected boolean                 m_UseSelfAdaption   = false;
    protected double                m_Tau1              = 0.15;
    protected double                m_LowerLimitChance  = 0.05;
	protected int lastOperatorIndex = -1;

	public CrossoverEAMixer() {
		InterfaceCrossover[] tmpList;
		ArrayList<String> crossers = GenericObjectEditor.getClassesFromProperties(InterfaceCrossover.class.getCanonicalName(), null);
		tmpList = new InterfaceCrossover[crossers.size()];
		for (int i = 0; i < crossers.size(); i++) {
			Class clz=null;
			try {
				clz = (Class)Class.forName((String)crossers.get(i));
			} catch (ClassNotFoundException e1) {
				continue;
			}
			if (clz.isAssignableFrom(this.getClass())) {
			// Do not instanciate this class or its subclasses or die of an infinite loop
//				System.out.println("Skipping " + clz.getClass().getName());
				continue;
			} else {
//				System.out.println("Taking " + clz.getClass().getName());
			}
			try {
				tmpList[i] = (InterfaceCrossover)Class.forName((String)crossers.get(i)).newInstance();
			} catch (java.lang.ClassNotFoundException e) {
				System.out.println("Could not find class for " +(String)crossers.get(i) );
			}  catch (java.lang.InstantiationException k) {
				System.out.println("Instantiation exception for " +(String)crossers.get(i) );
			} catch (java.lang.IllegalAccessException a) {
				System.out.println("Illegal access exception for " +(String)crossers.get(i) );
			}
		}
		this.m_Crossers = new PropertyCrossoverMixer(tmpList);
		tmpList = new InterfaceCrossover[2];
		tmpList[0] = new CrossoverESArithmetical();
		tmpList[1] = new CrossoverESSBX();
		this.m_Crossers.setSelectedCrossers(tmpList);
		this.m_Crossers.normalizeWeights();
		this.m_Crossers.setDescriptiveString("Combining alternative mutation operators, please norm the weights!");
		this.m_Crossers.setWeightsLabel("Weigths");
	}

    public CrossoverEAMixer(CrossoverEAMixer mutator) {
        this.m_Crossers         = (PropertyCrossoverMixer)mutator.m_Crossers.clone();
        this.m_UseSelfAdaption  = mutator.m_UseSelfAdaption;
        this.m_Tau1             = mutator.m_Tau1;
        this.m_LowerLimitChance = mutator.m_LowerLimitChance;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverEAMixer(this);
    }

    /** This method allows you to evaluate whether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof CrossoverEAMixer) {
            CrossoverEAMixer mut = (CrossoverEAMixer)mutator;

            return true;
        } else return false;
    }

    /** This method allows you to init the crossover operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt){
        InterfaceCrossover[] crossers    = this.m_Crossers.getSelectedCrossers();
        for (int i = 0; i < crossers.length; i++) {
            crossers[i].init(individual, opt);
        }
    }

    /** This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     * @param indy1 The first individual
     * @param partners The second individual
     */
    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        this.m_Crossers.normalizeWeights();
        double[]            probs       = this.m_Crossers.getWeights();
        if (this.m_UseSelfAdaption) {
            for (int i = 0; i < probs.length; i++) {
                probs[i] *= Math.exp(this.m_Tau1 * RNG.gaussianDouble(1));
                if (probs[i] <= this.m_LowerLimitChance) probs[i] = this.m_LowerLimitChance;
                if (probs[i] >= 1) probs[i] = 1;
            }
            this.m_Crossers.normalizeWeights();
        }

        InterfaceCrossover[] crossover    = this.m_Crossers.getSelectedCrossers();
        double pointer                  = RNG.randomFloat(0, 1);
        double dum                      = probs[0];
        lastOperatorIndex                       = 0;
        while ((pointer > dum) && (lastOperatorIndex < probs.length-1)) {
        	lastOperatorIndex++;
            dum += probs[lastOperatorIndex];
        }
        if (lastOperatorIndex == probs.length) lastOperatorIndex = RNG.randomInt(0, probs.length-1);
//        System.out.println("Using : " + mutators[index].getStringRepresentation());
//        for (int i = 0; i < probs.length; i++) {
//            System.out.println(""+mutators[i].getStringRepresentation()+" : "+ probs[i]);
//        }
//        System.out.println("");
        
        indy1.putData(CROSSOVER_EA_MIXER_OPERATOR_KEY, lastOperatorIndex);
        for (int i=0; i<partners.size(); i++) {
        	partners.getEAIndividual(i).putData(CROSSOVER_EA_MIXER_OPERATOR_KEY, lastOperatorIndex);
        }
        AbstractEAIndividual[] indies = crossover[lastOperatorIndex].mate(indy1, partners);
        
        maybeAdaptWeights(indies);
        return indies;
    }

    protected void maybeAdaptWeights(AbstractEAIndividual[] indies) {
	}
    
	public int getLastOperatorIndex() {
    	return lastOperatorIndex;
    }
    
    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "EA mutation mixer";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "EA mutation mixer";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This meta-mutation operator allows you to combine multiple alternative mutation operators.";
    }

    /** Choose the set of crossers.
     * @param d   The crossover operators.
     */
    public void setCrossovers(PropertyCrossoverMixer d) {
        this.m_Crossers = d;
    }
    public PropertyCrossoverMixer getCrossovers() {
        return this.m_Crossers;
    }
    public String CrossoversTipText() {
        return "Choose the set of crossover operators.";
    }

    /** Set the lower limit for the mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setUseSelfAdaption(boolean d) {
        this.m_UseSelfAdaption = d;
    }
    public boolean getUseSelfAdaption() {
        return this.m_UseSelfAdaption;
    }
    public String useSelfAdaptionTipText() {
        return "Use my implementation of self-adaption for the mutation mixer.";
    }

    /** Set the lower limit for the mutation step size with this method.
     * @param d   The mutation operator.
     */
    public void setLowerLimitChance(double d) {
        if (d < 0) d = 0;
        this.m_LowerLimitChance = d;
    }
    public double getLowerLimitChance() {
        return this.m_LowerLimitChance;
    }
    public String lowerLimitChanceTipText() {
        return "Set the lower limit for the mutation chance.";
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

    @Override
	public int getEvaluations() {
		int numEvals=0;
        InterfaceCrossover[] crossers    = this.m_Crossers.getSelectedCrossers();
        for (int i = 0; i < crossers.length; i++) {
        	if (crossers[i] instanceof InterfaceEvaluatingCrossoverOperator) {
        		numEvals += ((InterfaceEvaluatingCrossoverOperator)crossers[i]).getEvaluations();
        	}
        }
        return numEvals;
	}

    @Override
	public void resetEvaluations() {
		InterfaceCrossover[] crossers    = this.m_Crossers.getSelectedCrossers();
        for (int i = 0; i < crossers.length; i++) {
        	if (crossers[i] instanceof InterfaceEvaluatingCrossoverOperator) {
        		((InterfaceEvaluatingCrossoverOperator)crossers[i]).resetEvaluations();
        	}
        }
	}
}