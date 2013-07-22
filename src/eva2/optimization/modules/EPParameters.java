package eva2.optimization.modules;


import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.problems.F1Problem;
import eva2.optimization.strategies.EvolutionaryProgramming;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class gives access to all EP parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:49:09
 * To change this template use File | Settings | File Templates.
 */
public class EPParameters extends AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {

    private static final Logger LOGGER = Logger.getLogger(EPParameters.class.getName());


    /**
     * Load or create a new instance of the class.
     *
     * @return A loaded (from file) or new instance of the class.
     */
    public static EPParameters getInstance() {
        EPParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream("EPParameters.ser");
            instance = (EPParameters) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new EPParameters();
        }
        return instance;
    }

    /**
     *
     */
    public EPParameters() {
        super(new EvolutionaryProgramming(), new F1Problem(), new EvaluationTerminator());
    }

    /**
     *
     */
    private EPParameters(EPParameters Source) {
        super(Source);
    }

    /**
     *
     */
    @Override
    public Object clone() {
        return new EPParameters(this);
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is a Evolutionary Programming optimization method, limit EP to mutation operators only.";
    }

    @Override
    public void setOptimizer(InterfaceOptimizer optimizer) {
        // i'm a Monte Carlo Search Algorithm
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
        return ((EvolutionaryProgramming) this.optimizer).getPopulation();
    }

    public void setPopulation(Population pop) {
        ((EvolutionaryProgramming) this.optimizer).setPopulation(pop);
    }

    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    /**
     * Choose the type of environment selection to use.
     *
     * @param selection
     */
    public void setEnvironmentSelection(InterfaceSelection selection) {
        ((EvolutionaryProgramming) this.optimizer).setEnvironmentSelection(selection);
    }

    public InterfaceSelection getEnvironmentSelection() {
        return ((EvolutionaryProgramming) this.optimizer).getEnvironmentSelection();
    }

    public String environmentSelectionTipText() {
        return "Choose a method for selecting the reduced population.";
    }
}