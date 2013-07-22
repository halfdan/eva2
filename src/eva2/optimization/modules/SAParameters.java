package eva2.optimization.modules;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.go.InterfaceTerminator;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.problems.B1Problem;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.SimulatedAnnealing;
import eva2.tools.Serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * The class gives access to all SA parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:25:12
 * To change this template use File | Settings | File Templates.
 */
public class SAParameters extends AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {
    // Opt. Algorithms and Parameters
    private InterfaceOptimizer m_Optimizer = new SimulatedAnnealing();
    private InterfaceOptimizationProblem m_Problem = new B1Problem();
    //private int                             functionCalls     = 1000;
    private InterfaceTerminator m_Terminator = new EvaluationTerminator();
    //    private String                          m_OutputFileName    = "none";
    transient private InterfacePopulationChangedEventListener m_Listener;

    /**
     * Load or create a new instance of the class.
     *
     * @return A loaded (from file) or new instance of the class.
     */
    public static SAParameters getInstance() {
        SAParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream("SAParameters.ser");
            instance = (SAParameters) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new SAParameters();
        }
        return instance;
    }

    /**
     *
     */
    public SAParameters() {
        super(new SimulatedAnnealing(), new B1Problem(), new EvaluationTerminator());
    }

    private SAParameters(SAParameters Source) {
        super(Source);
    }

    @Override
    public Object clone() {
        return new SAParameters(this);
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is a simple Simulated Annealing algorithm.";
    }

    @Override
    public void setOptimizer(InterfaceOptimizer optimizer) {
        // *pff* i'll ignore that!
    }

    /**
     * Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return ((SimulatedAnnealing) this.m_Optimizer).getPopulation();
    }

    public void setPopulation(Population pop) {
        ((SimulatedAnnealing) this.m_Optimizer).setPopulation(pop);
    }

    public String populationTipText() {
        return "Change the number of best individuals stored (MS-SA).";
    }

    /**
     * This methods allow you to set/get the temperatur of the simulated
     * annealing procedure
     *
     * @return The initial temperature.
     */
    public double getInitialTemperature() {
        return ((SimulatedAnnealing) this.m_Optimizer).getInitialTemperature();
    }

    public void setInitialTemperature(double pop) {
        ((SimulatedAnnealing) this.m_Optimizer).setInitialTemperature(pop);
    }

    public String initialTemperatureTipText() {
        return "Set the initial temperature.";
    }

    /**
     * This methods allow you to set/get the temperatur of the simulated
     * annealing procedure
     *
     * @return The initial temperature.
     */
    public double getAlpha() {
        return ((SimulatedAnnealing) this.m_Optimizer).getAlpha();
    }

    public void setAlpha(double a) {
        if (a > 1) {
            a = 1.0;
        }
        ((SimulatedAnnealing) this.m_Optimizer).setAlpha(a);
    }

    public String alphaTipText() {
        return "Set alpha, which is used to degrade the temperaure.";
    }
}
