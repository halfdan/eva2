package eva2.server.go.operators.crossover;

import java.util.ArrayList;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;


/**
 * Created by IntelliJ IDEA.
 * User: Dante Alighieri
 * Date: 21.05.2005
 * Time: 11:36:38
 * To change this template use File | Settings | File Templates.
 */
public class CrossoverEAMixer implements InterfaceCrossover, java.io.Serializable  {

    private PropertyCrossoverMixer  m_Crossers;
    private boolean                 m_UseSelfAdaption   = false;
    protected double                m_Tau1              = 0.15;
    protected double                m_LowerLimitChance  = 0.05;

    public CrossoverEAMixer() {
        InterfaceCrossover[] tmpList;
        ArrayList<String> crossers = GenericObjectEditor.getClassesFromProperties("eva2.server.go.operators.crossover.InterfaceCrossover");
        tmpList = new InterfaceCrossover[crossers.size()];
         for (int i = 0; i < crossers.size(); i++) {
        	 if (((String)crossers.get(i)).equals(this.getClass().getName())) continue;
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
//
//    private Vector getClassesFromProperties(String mySelf, String myInterface) {
//        Vector classes = new Vector();
//        String typeOptions = EvAClient.getProperty(myInterface);
//        if (typeOptions == null) {
//            System.out.println("Warning: No configuration property found in: "  +EvAClient.EVA_PROPERTY_FILE + " "+"for javaeva.server.oa.go.Operators.Mutation.InterfaceMutation");
//        } else {
//            StringTokenizer st = new StringTokenizer(typeOptions, ", ");
//            while (st.hasMoreTokens()) {
//                String current = st.nextToken().trim();
//                if (!current.equalsIgnoreCase(mySelf)) {
//                    try {
//                    	Class c = Class.forName(current);
//                    	classes.addElement(current);
//                    } catch (Exception ex) {
//                        System.out.println("Couldn't load class with name: " + current);
//                        System.out.println("ex:"+ex.getMessage());
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        }
//        return classes;
//    }


    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new CrossoverEAMixer(this);
    }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof CrossoverEAMixer) {
            CrossoverEAMixer mut = (CrossoverEAMixer)mutator;

            return true;
        } else return false;
    }

    /** This method allows you to init the mutation operator
     * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt){
        InterfaceCrossover[] mutators    = this.m_Crossers.getSelectedCrossers();
        for (int i = 0; i < mutators.length; i++) mutators[i].init(individual, opt);
    }

    /** This method performs crossover on two individuals. If the individuals do
     * not implement InterfaceGAIndividual, then nothing will happen.
     * @param indy1 The first individual
     * @param partners The second individual
     */
    public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
        this.m_Crossers.normalizeWeights();
        double[]            probs       = this.m_Crossers.getWeights();
        if (this.m_UseSelfAdaption) {
            for (int i = 0; i < probs.length; i++) {
                probs[i] = probs[i] * Math.exp(this.m_Tau1 * RNG.gaussianDouble(1));
                if (probs[i] <= this.m_LowerLimitChance) probs[i] = this.m_LowerLimitChance;
                if (probs[i] >= 1) probs[i] = 1;
            }
            this.m_Crossers.normalizeWeights();
        }

        InterfaceCrossover[] crossover    = this.m_Crossers.getSelectedCrossers();
        double pointer                  = RNG.randomFloat(0, 1);
        double dum                      = probs[0];
        int index                       = 0;
        while ((pointer > dum) && (index < probs.length-1)) {
            index++;
            dum += probs[index];
        }
        if (index == probs.length) index = RNG.randomInt(0, probs.length-1);
//        System.out.println("Using : " + mutators[index].getStringRepresentation());
//        for (int i = 0; i < probs.length; i++) {
//            System.out.println(""+mutators[i].getStringRepresentation()+" : "+ probs[i]);
//        }
//        System.out.println("");
        return crossover[index].mate(indy1, partners);
    }


    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
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
    public String globalInfo() {
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
}