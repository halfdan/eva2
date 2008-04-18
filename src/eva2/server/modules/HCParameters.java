package eva2.server.modules;

import java.io.Serializable;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.HillClimbing;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;


/** The class gives access to all HC parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:19:20
 * To change this template use File | Settings | File Templates.
 */
public class HCParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {
    /**
     *
     */
    public static HCParameters getInstance() {
        if (TRACE) System.out.println("HCParameters getInstance 1");
        HCParameters Instance = (HCParameters) Serializer.loadObject("HCParameters.ser");
        if (TRACE) System.out.println("HCParameters getInstance 2");
        if (Instance == null) Instance = new HCParameters();
        return Instance;
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("HCParameters.ser",this);
    }
    /**
     *
     */
    public HCParameters() {
    	super(new HillClimbing(), new B1Problem(), new EvaluationTerminator());
    }

    /**
     *
     */
    private HCParameters(HCParameters Source) {
        super(Source);
    }
    /**
     *
     */
    public Object clone() {
        return new HCParameters(this);
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a Hill-Climber, use a population size > 1 for a Multi-Start Hill-Climber.";
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
        return ((HillClimbing)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((HillClimbing)this.m_Optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }
}
