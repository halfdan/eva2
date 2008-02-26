package javaeva.server.modules;

import javaeva.gui.BeanInspector;
import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.problems.B1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.strategies.GeneticAlgorithm;
import javaeva.server.go.strategies.HillClimbing;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.tools.Serializer;

import java.io.Serializable;
import java.io.BufferedWriter;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
public class GOParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;
    private String          m_Name  ="not defined";
    private long            m_Seed  = (long)100.0;

    // Opt. Algorithms and Parameters
    private InterfaceOptimizer              m_Optimizer         = new GeneticAlgorithm();
    private InterfaceOptimizationProblem    m_Problem           = new B1Problem();
    //private int                             m_FunctionCalls     = 1000;
    private InterfaceTerminator             m_Terminator        = new EvaluationTerminator();
//    private String                          m_OutputFileName    = "none";
    transient private InterfacePopulationChangedEventListener m_Listener;

    /**
     *
     */
    public static GOParameters getInstance() {
        if (TRACE) System.out.println("GOParameters getInstance 1");
        GOParameters Instance = (GOParameters) Serializer.loadObject("GOParameters.ser");
        if (TRACE) System.out.println("GOParameters getInstance 2");
        if (Instance == null) Instance = new GOParameters();
        return Instance;
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer(m_Name);
    	sb.append("\n");
    	sb.append("seed=");
    	sb.append(m_Seed);
    	sb.append("\nProblem: ");
    	sb.append(BeanInspector.toString(m_Problem));
    	sb.append("\nOptimizer: ");
    	sb.append(BeanInspector.toString(m_Optimizer));
    	sb.append("\nTerminator: ");
    	sb.append(BeanInspector.toString(m_Terminator));
    	sb.append("\n");
//    	sb.append(m_N)
    	return sb.toString();
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("GOParameters.ser",this);
    }
    /**
     *
     */
    public GOParameters() {
        if (TRACE) System.out.println("GOParameters Constructor start");
        this.m_Name="Optimization parameters";
        this.m_Optimizer        = new GeneticAlgorithm();
        this.m_Problem          = new B1Problem();
        //this.m_FunctionCalls    = 1000;
        ((EvaluationTerminator)this.m_Terminator).setFitnessCalls(1000);
        this.m_Optimizer.SetProblem(this.m_Problem);
        if (TRACE) System.out.println("GOParameters Constructor end");
    }

    /**
     *
     */
    private GOParameters(GOParameters Source) {
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
        return new GOParameters(this);
    }

    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
        if (this.m_Optimizer != null) this.m_Optimizer.addPopulationChangedEventListener(this.m_Listener);
    }

//    /**
//     *
//     */
//    public String toString() {
//        String ret = "";//"\r\nGO-Parameter:"+this.m_Problem.getStringRepresentationForProblem()+"\n"+this.m_Optimizer.getStringRepresentation();
//        return ret;
//    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Select the optimization parameters.";
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
        return "Random number seed, set to zero to use current system time.";
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

//    /** This method allows you to set the current optimizing algorithm
//     * @param optimizer The new optimizing algorithm
//     */
//    public void SetOptimizer(InterfaceOptimizer optimizer) {
//        this.m_Optimizer = optimizer;
//        this.m_Optimizer.SetProblem(this.m_Problem);
//        if (this.m_Listener != null) this.m_Optimizer.addPopulationChangedEventListener(this.m_Listener);
//    }
    
    public void setOptimizer(InterfaceOptimizer optimizer) {
        this.m_Optimizer = optimizer;
        this.m_Optimizer.SetProblem(this.m_Problem);
        if (this.m_Listener != null) this.m_Optimizer.addPopulationChangedEventListener(this.m_Listener);
    }
    public InterfaceOptimizer getOptimizer() {
        return this.m_Optimizer;
    }
    public String optimizerTipText() {
        return "Choose an optimizing strategy.";
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
}
