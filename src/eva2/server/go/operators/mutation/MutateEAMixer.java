package eva2.server.go.operators.mutation;

import java.util.ArrayList;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 20.05.2005
 * Time: 13:53:46
 * To change this template use File | Settings | File Templates.
 */
public class MutateEAMixer implements InterfaceMutation, java.io.Serializable  {

    private PropertyMutationMixer   m_Mutators;
    private boolean                 m_UseSelfAdaption   = false;
    protected double                m_Tau1              = 0.15;
    protected double                m_LowerLimitChance  = 0.05;

    public MutateEAMixer() {
        InterfaceMutation[] tmpList;
        ArrayList<String> mutators = GenericObjectEditor.getClassesFromProperties(InterfaceMutation.class.getCanonicalName(), null);
        tmpList = new InterfaceMutation[mutators.size()];
         for (int i = 0; i < mutators.size(); i++) {
        	 if (((String)mutators.get(i)).equals(this.getClass().getName())) continue;
            try {
                tmpList[i] = (InterfaceMutation)Class.forName((String)mutators.get(i)).newInstance();
            } catch (java.lang.ClassNotFoundException e) {
                System.out.println("Could not find class for " +(String)mutators.get(i) );
            }  catch (java.lang.InstantiationException k) {
                System.out.println("Instantiation exception for " +(String)mutators.get(i) );
            } catch (java.lang.IllegalAccessException a) {
                System.out.println("Illegal access exception for " +(String)mutators.get(i) );
            }
        }
        this.m_Mutators = new PropertyMutationMixer(tmpList, false);
        tmpList = new InterfaceMutation[2];
        tmpList[0] = new MutateGINominal();
        tmpList[1] = new MutateGIOrdinal();
        this.m_Mutators.setSelectedMutators(tmpList);
        this.m_Mutators.normalizeWeights();
        this.m_Mutators.setDescriptiveString("Combining alternative mutation operators, please norm the weights!");
        this.m_Mutators.setWeightsLabel("Weigths");

    }
    
    /**
     * Create a mutation mixer with equal weights of the given mutation operators.
     * @param mutators
     */
    public MutateEAMixer(InterfaceMutation ... mutators) {
        this.m_Mutators         = new PropertyMutationMixer(mutators, true);
    }
    
    public MutateEAMixer(InterfaceMutation m1, InterfaceMutation m2) {
    	this(new InterfaceMutation[] {m1, m2});
    }
    
    public MutateEAMixer(InterfaceMutation m1, InterfaceMutation m2, InterfaceMutation m3) {
    	this(new InterfaceMutation[] {m1, m2, m3});
    }
    
    public MutateEAMixer(MutateEAMixer mutator) {
        this.m_Mutators         = (PropertyMutationMixer)mutator.m_Mutators.clone();
        this.m_UseSelfAdaption  = mutator.m_UseSelfAdaption;
        this.m_Tau1             = mutator.m_Tau1;
        this.m_LowerLimitChance = mutator.m_LowerLimitChance;
    }

    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateEAMixer(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateEAMixer) {
            MutateEAMixer mut = (MutateEAMixer)mutator;

            return true;
        } else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt){
        InterfaceMutation[] mutators    = this.m_Mutators.getSelectedMutators();
        for (int i = 0; i < mutators.length; i++) mutators[i].init(individual, opt);
    }

    /** This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     * @param individual    The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        this.m_Mutators.normalizeWeights();
        double[]            probs       = this.m_Mutators.getWeights();
        if (this.m_UseSelfAdaption) {
            for (int i = 0; i < probs.length; i++) {
                probs[i] = probs[i] * Math.exp(this.m_Tau1 * RNG.gaussianDouble(1));
                if (probs[i] <= this.m_LowerLimitChance) probs[i] = this.m_LowerLimitChance;
                if (probs[i] >= 1) probs[i] = 1;
            }
            this.m_Mutators.normalizeWeights();
        }

        InterfaceMutation[] mutators    = this.m_Mutators.getSelectedMutators();
        double pointer                  = RNG.randomFloat(0, 1);
        double dum                      = probs[0];
        int index                       = 0;
        while ((pointer > dum) && (index < probs.length-1)) {
            index++;
            dum += probs[index];
        }
        if (index == probs.length) index = RNG.randomInt(0, probs.length-1);
        //System.out.println("Using : " + mutators[index].getStringRepresentation());
//        for (int i = 0; i < probs.length; i++) {
//            System.out.println(""+mutators[i].getStringRepresentation()+" : "+ probs[i]);
//        }
//        System.out.println("");
        mutators[index].mutate(individual);
    }

    /** This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     * @param indy1     The original mother
     * @param partners  The original partners
     */
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        for (int i = 0; i < this.m_Mutators.getSelectedMutators().length; i++) {
            this.m_Mutators.getSelectedMutators()[i].crossoverOnStrategyParameters(indy1, partners);
        }
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

    /** Choose the set of mutators.
     * @param d   The mutation operators.
     */
    public void setMutators(PropertyMutationMixer d) {
        this.m_Mutators = d;
    }
    public PropertyMutationMixer getMutators() {
        return this.m_Mutators;
    }
    public String mutatorsTipText() {
        return "Choose the set of mutators.";
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
}
