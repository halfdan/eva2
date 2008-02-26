package javaeva.server.modules;

import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.B1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.strategies.GeneticAlgorithm;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.strategies.MonteCarloSearch;
import javaeva.server.go.strategies.SimulatedAnnealing;
import javaeva.tools.Serializer;

import java.io.Serializable;

/** The class gives access to all SA parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:25:12
 * To change this template use File | Settings | File Templates.
 */
public class SAParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;
    private String          m_Name  ="not defined";
    private long            m_Seed  = (long)100.0;

    // Opt. Algorithms and Parameters
    private InterfaceOptimizer              m_Optimizer         = new SimulatedAnnealing();
    private InterfaceOptimizationProblem    m_Problem           = new B1Problem();
    //private int                             m_FunctionCalls     = 1000;
    private InterfaceTerminator             m_Terminator        = new EvaluationTerminator();
//    private String                          m_OutputFileName    = "none";
    transient private InterfacePopulationChangedEventListener m_Listener;

    /**
     *
     */
    public static SAParameters getInstance() {
        if (TRACE) System.out.println("SAParameters getInstance 1");
        SAParameters Instance = (SAParameters) Serializer.loadObject("SAParameters.ser");
        if (TRACE) System.out.println("SAParameters getInstance 2");
        if (Instance == null) Instance = new SAParameters();
        return Instance;
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("SAParameters.ser",this);
    }
    /**
     *
     */
    public SAParameters() {
        if (TRACE) System.out.println("SAParameters Constructor start");
        this.m_Name="Optimization parameters";
        this.m_Optimizer        = new SimulatedAnnealing();
        this.m_Problem          = new B1Problem();
        //this.m_FunctionCalls    = 1000;
        ((EvaluationTerminator)this.m_Terminator).setFitnessCalls(1000);
        this.m_Optimizer.SetProblem(this.m_Problem);
        if (TRACE) System.out.println("SAParameters Constructor end");
    }

    /**
     *
     */
    private SAParameters(SAParameters Source) {
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
        return new SAParameters(this);
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
        return "This is a simple Simulated Annealing Algorithm.";
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

//    /** This method will set the output filename
//     * @param name
//     */
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
        return ((SimulatedAnnealing)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((SimulatedAnnealing)this.m_Optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Change the number of best individuals stored (MS-SA).";
    }

    /** This methods allow you to set/get the temperatur of the simulated
     * annealing procedure
     * @return The initial temperature.
     */
    public double getInitialTemperature() {
        return ((SimulatedAnnealing)this.m_Optimizer).getInitialTemperature();
    }
    public void setInitialTemperature(double pop){
        ((SimulatedAnnealing)this.m_Optimizer).setInitialTemperature(pop);
    }
    public String initialTemperatureTipText() {
        return "Set the initial temperature.";
    }

    /** This methods allow you to set/get the temperatur of the simulated
     * annealing procedure
     * @return The initial temperature.
     */
    public double getAlpha() {
        return ((SimulatedAnnealing)this.m_Optimizer).getAlpha();
    }
    public void setAlpha(double a){
        if (a > 1) a = 1.0;
        ((SimulatedAnnealing)this.m_Optimizer).setAlpha(a);
    }
    public String alphaTipText() {
        return "Set alpha, which is used to degrade the temperaure.";
    }
}
