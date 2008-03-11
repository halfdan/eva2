package javaeva.server.modules;

import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.operators.selection.InterfaceSelection;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.F1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.strategies.EvolutionaryProgramming;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.tools.Serializer;

import java.io.Serializable;

/** The class gives access to all EP parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.10.2004
 * Time: 13:49:09
 * To change this template use File | Settings | File Templates.
 */
public class EPParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;

    /**
     *
     */
    public static EPParameters getInstance() {
        if (TRACE) System.out.println("EPParameters getInstance 1");
        EPParameters Instance = (EPParameters) Serializer.loadObject("EPParameters.ser");
        if (TRACE) System.out.println("EPParameters getInstance 2");
        if (Instance == null) Instance = new EPParameters();
        return Instance;
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("EPParameters.ser",this);
    }
    /**
     *
     */
    public EPParameters() {
    	super(new EvolutionaryProgramming(), new F1Problem(), new EvaluationTerminator());
        if (TRACE) System.out.println("EPParameters Constructor start");
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
    public Object clone() {
        return new EPParameters(this);
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a Evolutionary Programming optimization method, please limit EP to mutation operators only.";
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
        return ((EvolutionaryProgramming)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((EvolutionaryProgramming)this.m_Optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    /** Choose the type of environment selection to use.
     * @param selection
     */
    public void setEnvironmentSelection(InterfaceSelection selection) {
        ((EvolutionaryProgramming)this.m_Optimizer).setEnvironmentSelection(selection);
    }
    public InterfaceSelection getEnvironmentSelection() {
        return ((EvolutionaryProgramming)this.m_Optimizer).getEnvironmentSelection();
    }
    public String environmentSelectionTipText() {
        return "Choose a method for selecting the reduced population.";
    }
}