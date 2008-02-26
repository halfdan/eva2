package javaeva.server.modules;

import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.operators.selection.InterfaceSelection;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.F1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.strategies.DifferentialEvolution;
import javaeva.server.go.strategies.GeneticAlgorithm;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.tools.Serializer;
import javaeva.tools.SelectedTag;

import java.io.Serializable;

/** The class gives access to all DE parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:49:09
 * To change this template use File | Settings | File Templates.
 */
public class DEParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;
    private String          m_Name  ="not defined";
    private long            m_Seed  = (long)100.0;

    // Opt. Algorithms and Parameters
    private InterfaceOptimizer              m_Optimizer         = new DifferentialEvolution();
    private InterfaceOptimizationProblem    m_Problem           = new F1Problem();
    //private int                             m_FunctionCalls     = 1000;
    private InterfaceTerminator             m_Terminator        = new EvaluationTerminator();
//    private String                          m_OutputFileName    = "none";
    transient private InterfacePopulationChangedEventListener m_Listener;

    /**
     *
     */
    public static DEParameters getInstance() {
        if (TRACE) System.out.println("DEParameters getInstance 1");
        DEParameters Instance = (DEParameters) Serializer.loadObject("DEParameters.ser");
        if (TRACE) System.out.println("DEParameters getInstance 2");
        if (Instance == null) Instance = new DEParameters();
        return Instance;
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("DEParameters.ser",this);
    }
    /**
     *
     */
    public DEParameters() {
        if (TRACE) System.out.println("DEParameters Constructor start");
        this.m_Name="Optimization parameters";
        this.m_Optimizer        = new DifferentialEvolution();
        this.m_Problem          = new F1Problem();
        //this.m_FunctionCalls    = 1000;
        ((EvaluationTerminator)this.m_Terminator).setFitnessCalls(1000);
        this.m_Optimizer.SetProblem(this.m_Problem);
        if (TRACE) System.out.println("DEParameters Constructor end");
    }

    /**
     *
     */
    private DEParameters(DEParameters Source) {
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
        return new DEParameters(this);
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
        String ret = "\r\nDE-Parameter:"+this.m_Problem.getStringRepresentationForProblem(this.m_Optimizer)+"\n"+this.m_Optimizer.getStringRepresentation();
        return ret;
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a Differential Evolution optimization method, please limit DE to real-valued genotypes.";
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
    public void setTerminator(InterfaceTerminator term) {
        this.m_Terminator = term;
    }
    public InterfaceTerminator getTerminator() {
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
//    public void setOutputFileName (String name) {
//        this.m_OutputFileName = name;
//    }
//    public String getOutputFileName () {
//        return this.m_OutputFileName;
//    }
//    public String outputFileNameTipText() {
//        return "Set the name for the output file, if 'none' no output file will be created.";
//    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return ((DifferentialEvolution)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((DifferentialEvolution)this.m_Optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    /** This method will set the amplication factor f
     * @param f
     */
    public void setF (double f) {
        ((DifferentialEvolution)this.m_Optimizer).setF(f);
    }
    public double getF() {
        return ((DifferentialEvolution)this.m_Optimizer).getF();
    }
    public String fTipText() {
        return "F is a real and constant factor which controlls the ampllification of the differential variation.";
    }

    /** This method will set the crossover probability
     * @param k
     */
    public void setK(double k) {
        ((DifferentialEvolution)this.m_Optimizer).setK(k);
    }
    public double getK() {
        return ((DifferentialEvolution)this.m_Optimizer).getK();
    }
    public String kTipText() {
        return "Probability of alteration through DE1.";
    }

    /** This method will set greediness to move towards the best
     * @param l
     */
    public void setLambda (double l) {
        ((DifferentialEvolution)this.m_Optimizer).setLambda(l);
    }
    public double getLambda() {
        return ((DifferentialEvolution)this.m_Optimizer).getLambda();
    }
    public String lambdaTipText() {
        return "Enhance greediness through amplification of the differential vector to the best individual for DE2.";
    }

    /** This method allows you to choose the type of Differential Evolution.
     * @param s  The type.
     */
    public void setDEType(SelectedTag s) {
        ((DifferentialEvolution)this.m_Optimizer).setDEType(s);
    }
    public SelectedTag getDEType() {
        return ((DifferentialEvolution)this.m_Optimizer).getDEType();
    }
    public String dETypeTipText() {
        return "Choose the type of Differential Evolution.";
    }
}