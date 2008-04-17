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
import eva2.tools.Serializer;

/** The class gives access to all HC parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:07:40
 * To change this template use File | Settings | File Templates.
 */
public class MCParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {
    public static boolean   TRACE   = false;

    /**
     *
     */
    public static MCParameters getInstance() {
        if (TRACE) System.out.println("MCParameters getInstance 1");
        MCParameters Instance = (MCParameters) Serializer.loadObject("MCParameters.ser");
        if (TRACE) System.out.println("MCParameters getInstance 2");
        if (Instance == null) Instance = new MCParameters();
        return Instance;
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("MCParameters.ser",this);
    }
    /**
     *
     */
    public MCParameters() {    
    	super(new MonteCarloSearch(), new B1Problem(), new EvaluationTerminator());
    }

    private MCParameters(MCParameters Source) {
    	super(Source);
    }

    /**
     *
     */
    public Object clone() {
        return new MCParameters(this);
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a simple Monte-Carlo Search, please use big populations sizes for faster drawing.";
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
        return ((MonteCarloSearch)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((MonteCarloSearch)this.m_Optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }
}
