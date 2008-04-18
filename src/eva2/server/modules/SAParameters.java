package eva2.server.modules;


import java.io.Serializable;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.GeneticAlgorithm;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.strategies.MonteCarloSearch;
import eva2.server.go.strategies.SimulatedAnnealing;
import eva2.tools.Serializer;

/** The class gives access to all SA parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:25:12
 * To change this template use File | Settings | File Templates.
 */
public class SAParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;

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
    	super(new SimulatedAnnealing(),new B1Problem(),new EvaluationTerminator());
    }

    private SAParameters(SAParameters Source) {
    	super(Source);
    }
    public Object clone() {
        return new SAParameters(this);
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a simple Simulated Annealing Algorithm.";
    }

    public void setOptimizer(InterfaceOptimizer optimizer) {
        // *pff* i'll ignore that!
    }

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
