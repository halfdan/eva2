package eva2.server.modules;

import java.io.Serializable;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.strategies.PopulationBasedIncrementalLearning;
import eva2.tools.Serializer;


/** The class gives access to all PBIL parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:53:29
 * To change this template use File | Settings | File Templates.
 */
public class PBILParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;

    public static PBILParameters getInstance() {
        if (TRACE) System.out.println("PBILParameters getInstance 1");
        PBILParameters Instance = (PBILParameters) Serializer.loadObject("PBILParameters.ser");
        if (TRACE) System.out.println("PBILParameters getInstance 2");
        if (Instance == null) Instance = new PBILParameters();
        return Instance;
    }

    public void saveInstance() {
        Serializer.storeObject("PBILParameters.ser",this);
    }

    public PBILParameters() {
    	super(new PopulationBasedIncrementalLearning(), new B1Problem(), new EvaluationTerminator(1000));
    }

    private PBILParameters(PBILParameters Source) {
    	super(Source);
    }
    public Object clone() {
        return new PBILParameters(this);
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
    	return ((PopulationBasedIncrementalLearning)this.m_Optimizer).globalInfo();
    }

    public void setOptimizer(InterfaceOptimizer optimizer) {
        // i'm a Monte Carlo Search Algorithm
        // *pff* i'll ignore that!
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return ((PopulationBasedIncrementalLearning)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((PopulationBasedIncrementalLearning)this.m_Optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    /** This method will set the selection method that is to be used
     * @param selection
     */
    public void setSelectionMethod(InterfaceSelection selection) {
        ((PopulationBasedIncrementalLearning)this.m_Optimizer).setSelectionMethod(selection);
    }
    public InterfaceSelection getSelectionMethod() {
        return ((PopulationBasedIncrementalLearning)this.m_Optimizer).getSelectionMethod();
    }
    public String selectionMethodTipText() {
        return "Choose a selection method.";
    }

    /** Enable/disable elitism.
     * @param elitism
     */
    public void setElitism (boolean elitism) {
        ((PopulationBasedIncrementalLearning)this.m_Optimizer).setElitism(elitism);
    }
    public boolean getElitism() {
        return ((PopulationBasedIncrementalLearning)this.m_Optimizer).getElitism();
    }
    public String elitismTipText() {
        return "Enable/disable elitism.";
    }

    /** This method will set the learning rate for PBIL
     * @param LearningRate
     */
    public void setLearningRate (double LearningRate) {
        if (LearningRate < 0) LearningRate = 0;
        ((PopulationBasedIncrementalLearning)this.m_Optimizer).setLearningRate(LearningRate);
    }
    public double getLearningRate() {
        return ((PopulationBasedIncrementalLearning)this.m_Optimizer).getLearningRate();
    }
    public String learningRateTipText() {
        return "The learing rate of PBIL.";
    }

    /** This method will set the mutation rate for PBIL
     * @param m
     */
    public void setMutationRate (double m) {
        if (m < 0) m = 0;
        if (m > 1) m = 1;
        ((PopulationBasedIncrementalLearning)this.m_Optimizer).setMutationRate(m);

     }
    public double getMutationRate() {
        return ((PopulationBasedIncrementalLearning)this.m_Optimizer).getMutationRate();
    }
    public String mutationRateTipText() {
        return "The mutation rate of PBIL.";
    }

    /** This method will set the mutation sigma for PBIL
     * @param m
     */
    public void setMutateSigma (double m) {
        if (m < 0) m = 0;
        ((PopulationBasedIncrementalLearning)this.m_Optimizer).setMutateSigma(m);
    }
    public double getMutateSigma() {
        return ((PopulationBasedIncrementalLearning)this.m_Optimizer).getMutateSigma();
    }
    public String mutateSigmaTipText() {
        return "Set the sigma for the mutation of the probability vector.";
    }

    /** This method will set the number of positive samples for PBIL
     * @param PositiveSamples
     */
    public void setPositiveSamples (int PositiveSamples) {
        if (PositiveSamples < 1) PositiveSamples = 1;
        ((PopulationBasedIncrementalLearning)this.m_Optimizer).setPositiveSamples(PositiveSamples);
    }
    public int getPositiveSamples() {
        return ((PopulationBasedIncrementalLearning)this.m_Optimizer).getPositiveSamples();
    }
    public String positiveSamplesTipText() {
        return "The number of positive samples that update the PBIL vector.";
    }
}