package javaeva.server.modules;

import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.TerminatorInterface;
import javaeva.server.go.operators.selection.InterfaceSelection;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.B1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.strategies.MonteCarloSearch;
import javaeva.server.go.strategies.PopulationBasedIncrementalLearning;
import javaeva.tools.Serializer;

import java.io.Serializable;

/** The class gives access to all PBIL parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:53:29
 * To change this template use File | Settings | File Templates.
 */
public class PBILParameters  implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;
    private String          m_Name  ="not defined";
    private long            m_Seed  = (long)100.0;

    // Opt. Algorithms and Parameters
    private InterfaceOptimizer              m_Optimizer         = new PopulationBasedIncrementalLearning();
    private InterfaceOptimizationProblem    m_Problem           = new B1Problem();
    //private int                             m_FunctionCalls     = 1000;
    private TerminatorInterface             m_Terminator        = new EvaluationTerminator();
    private String                          m_OutputFileName    = "none";
    transient private InterfacePopulationChangedEventListener m_Listener;

    /**
     *
     */
    public static PBILParameters getInstance() {
        if (TRACE) System.out.println("PBILParameters getInstance 1");
        PBILParameters Instance = (PBILParameters) Serializer.loadObject("PBILParameters.ser");
        if (TRACE) System.out.println("PBILParameters getInstance 2");
        if (Instance == null) Instance = new PBILParameters();
        return Instance;
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("PBILParameters.ser",this);
    }
    /**
     *
     */
    public PBILParameters() {
        if (TRACE) System.out.println("PBILParameters Constructor start");
        this.m_Name="Optimization parameters";
        this.m_Optimizer        = new PopulationBasedIncrementalLearning();
        this.m_Problem          = new B1Problem();
        //this.m_FunctionCalls    = 1000;
        ((EvaluationTerminator)this.m_Terminator).setFitnessCalls(1000);
        this.m_Optimizer.SetProblem(this.m_Problem);
        if (TRACE) System.out.println("PBILParameters Constructor end");
    }

    /**
     *
     */
    private PBILParameters(PBILParameters Source) {
        this.m_Name             = Source.m_Name;
        this.m_Optimizer        = Source.m_Optimizer;
        this.m_Problem          = Source.m_Problem;
        this.m_Terminator       = Source.m_Terminator;
        //this.m_FunctionCalls    = Source.m_FunctionCalls;
        this.m_Optimizer.SetProblem(this.m_Problem);
        this.m_Seed             = Source.m_Seed;
    }
    /**
     *
     */
    public String getName() {
        return m_Name;
    }
    /**
     *
     */
    public Object clone() {
        return new PBILParameters(this);
    }

    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
        if (this.m_Optimizer != null) this.m_Optimizer.addPopulationChangedEventListener(this.m_Listener);
    }

    /**
     *
     */
    public String toString() {
        String ret = "\r\nGO-Parameter:"+this.m_Problem.getStringRepresentationForProblem(this.m_Optimizer)+"\n"+this.m_Optimizer.getStringRepresentation();
        return ret;
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Please note: This optimizer requires a BitSet as genotype!!";
    }

    /** This methods allow you to set and get the Seed for the Random Number Generator.
     * @param x     Long seed.
     */
    public void setSeed(long x) {
        m_Seed = x;
    }
    public long getSeed() {
        return m_Seed;
    }
    public String seedTipText() {
        return "Random number seed.";
    }

    /** This method allows you to set the current optimizing algorithm
     * @param optimizer The new optimizing algorithm
     */
    public void setOptimizer(InterfaceOptimizer optimizer) {
        // i'm a Monte Carlo Search Algorithm
        // *pff* i'll ignore that!
    }
    public InterfaceOptimizer getOptimizer() {
        return this.m_Optimizer;
    }

    /** This method allows you to choose a termination criteria for the
     * evolutionary algorithm.
     * @param term  The new terminator
     */
    public void setTerminator(TerminatorInterface term) {
        this.m_Terminator = term;
    }
    public TerminatorInterface getTerminator() {
        return this.m_Terminator;
    }
    public String terminatorTipText() {
        return "Choose a termination criterion.";
    }

    /** This method will set the problem that is to be optimized
     * @param problem
     */
    public void setProblem (InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
        this.m_Optimizer.SetProblem(this.m_Problem);
    }
    public InterfaceOptimizationProblem getProblem() {
        return this.m_Problem;
    }
    public String problemTipText() {
        return "Choose the problem that is to optimize and the EA individual parameters.";
    }

    /** This method will set the output filename
     * @param name
     */
    public void setOutputFileName (String name) {
        this.m_OutputFileName = name;
    }
    public String getOutputFileName () {
        return this.m_OutputFileName;
    }
    public String outputFileNameTipText() {
        return "Set the name for the output file, if 'none' no output file will be created.";
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