package javaeva.server.modules;

import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.operators.archiving.InterfaceArchiving;
import javaeva.server.go.operators.archiving.InterfaceInformationRetrieval;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.F1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.problems.TF1Problem;
import javaeva.server.go.strategies.DifferentialEvolution;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.strategies.MultiObjectiveEA;
import javaeva.tools.Serializer;
import javaeva.tools.SelectedTag;

import java.io.Serializable;

/** The class gives access to all MOEA parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:49:09
 * To change this template use File | Settings | File Templates.
 */
public class MOEAParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;
    private String          m_Name  ="not defined";
    private long            m_Seed  = (long)100.0;

    // Opt. Algorithms and Parameters
    private InterfaceOptimizer              m_Optimizer         = new MultiObjectiveEA();
    private InterfaceOptimizationProblem    m_Problem           = new TF1Problem();
    //private int                             m_FunctionCalls     = 1000;
    private InterfaceTerminator             m_Terminator        = new EvaluationTerminator();
//    private String                          m_OutputFileName    = "none";
    transient private InterfacePopulationChangedEventListener m_Listener;

    /**
     *
     */
    public static MOEAParameters getInstance() {
        if (TRACE) System.out.println("MOEAParameters getInstance 1");
        MOEAParameters Instance = (MOEAParameters) Serializer.loadObject("MOEAParameters.ser");
        if (TRACE) System.out.println("MOEAParameters getInstance 2");
        if (Instance == null) Instance = new MOEAParameters();
        return Instance;
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("MOEAParameters.ser",this);
    }
    /**
     *
     */
    public MOEAParameters() {
        if (TRACE) System.out.println("MOEAParameters Constructor start");
        this.m_Name="Optimization parameters";
        this.m_Optimizer        = new MultiObjectiveEA();
        this.m_Problem          = new TF1Problem();
        //this.m_FunctionCalls    = 1000;
        ((EvaluationTerminator)this.m_Terminator).setFitnessCalls(1000);
        this.m_Optimizer.SetProblem(this.m_Problem);
        if (TRACE) System.out.println("MOEAParameters Constructor end");
    }

    /**
     *
     */
    private MOEAParameters(MOEAParameters Source) {
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
        return new MOEAParameters(this);
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
        String ret = "\r\nMOEA-Parameter:"+this.m_Problem.getStringRepresentationForProblem(this.m_Optimizer)+"\n"+this.m_Optimizer.getStringRepresentation();
        return ret;
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a multi-objective evoluationary algorithm, please limit MOEA to multi-objective problems (due to the given framework only the fitness of objective one will be plotted).";
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
        ((MultiObjectiveEA)this.m_Optimizer).SetProblem(this.m_Problem);
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
        return ((MultiObjectiveEA)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((MultiObjectiveEA)this.m_Optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the Population used.";
    }

    /** This method allows you to set/get the optimizing technique to use.
     * @return The current optimizing method
     */
    public InterfaceOptimizer getOptimizer() {
        return ((MultiObjectiveEA)this.m_Optimizer).getOptimizer();
    }
//    public void SetOptimizer(InterfaceOptimizer b){
//        // i'm a MOEA i'll ignore that
//    }
    public void setOptimizer(InterfaceOptimizer b){
        ((MultiObjectiveEA)this.m_Optimizer).setOptimizer(b);
    }
    public String optimizerTipText() {
        return "Choose a population based optimizing technique to use.";
    }

    /** This method allows you to set/get the archiving strategy to use.
     * @return The current optimizing method
     */
    public InterfaceArchiving getArchivingStrategy() {
        return ((MultiObjectiveEA)this.m_Optimizer).getArchivingStrategy();
    }
    public void setArchivingStrategy(InterfaceArchiving b){
        ((MultiObjectiveEA)this.m_Optimizer).setArchivingStrategy(b);
    }
    public String archivingStrategyTipText() {
        return "Choose the archiving strategy.";
    }

    /** This method allows you to set/get the Information Retrieval strategy to use.
     * @return The current optimizing method
     */
    public InterfaceInformationRetrieval getInformationRetrieval() {
        return ((MultiObjectiveEA)this.m_Optimizer).getInformationRetrieval();
    }
    public void setInformationRetrieval(InterfaceInformationRetrieval b){
        ((MultiObjectiveEA)this.m_Optimizer).setInformationRetrieval(b);
    }
    public String informationRetrievalTipText() {
        return "Choose the Information Retrieval strategy.";
    }

    /** This method allows you to set/get the size of the archive.
     * @return The current optimizing method
     */
    public int getArchiveSize() {
        Population archive = ((MultiObjectiveEA)this.m_Optimizer).getPopulation().getArchive();
        if (archive == null) {
            archive = new Population();
            ((MultiObjectiveEA)this.m_Optimizer).getPopulation().SetArchive(archive);
        }
        return ((MultiObjectiveEA)this.m_Optimizer).getArchiveSize();
    }
    public void setArchiveSize(int b){
        Population archive = ((MultiObjectiveEA)this.m_Optimizer).getPopulation().getArchive();
        if (archive == null) {
            archive = new Population();
            ((MultiObjectiveEA)this.m_Optimizer).getPopulation().SetArchive(archive);
        }
        ((MultiObjectiveEA)this.m_Optimizer).getPopulation().getArchive().setPopulationSize(b);
    }
    public String archiveSizeTipText() {
        return "Choose the size of the archive.";
    }
}