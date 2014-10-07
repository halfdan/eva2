package eva2.optimization.modules;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.Population;
import eva2.problems.B1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.SimulatedAnnealing;
import eva2.tools.Serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;

/**
 *
 */
public class SAParameters extends AbstractOptimizationParameters implements Serializable {
    // Opt. Algorithms and Parameters
    private InterfaceOptimizer optimizer = new SimulatedAnnealing();
    private InterfaceOptimizationProblem problem = new B1Problem();
    private InterfaceTerminator terminator = new EvaluationTerminator();
    transient private InterfacePopulationChangedEventListener changedEventListener;

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
        return ((SimulatedAnnealing) this.optimizer).getPopulation();
    }

    public void setPopulation(Population pop) {
        ((SimulatedAnnealing) this.optimizer).setPopulation(pop);
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
        return ((SimulatedAnnealing) this.optimizer).getInitialTemperature();
    }

    public void setInitialTemperature(double pop) {
        ((SimulatedAnnealing) this.optimizer).setInitialTemperature(pop);
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
        return ((SimulatedAnnealing) this.optimizer).getAlpha();
    }

    public void setAlpha(double a) {
        if (a > 1) {
            a = 1.0;
        }
        ((SimulatedAnnealing) this.optimizer).setAlpha(a);
    }

    public String alphaTipText() {
        return "Set alpha, which is used to degrade the temperaure.";
    }
}
