package eva2.optimization.modules;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.problems.B1Problem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.MonteCarloSearch;
import eva2.tools.Serializer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;

/** The class gives access to all HC parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:07:40
 * To change this template use File | Settings | File Templates.
 */
public class MCParameters extends AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {
    
    /**
     * Load or create a new instance of the class.
     * 
     * @return A loaded (from file) or new instance of the class.
     */
    public static MCParameters getInstance() {
        MCParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream("MCParameters.ser");
            instance = (MCParameters) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new MCParameters();
        }
        return instance;
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
    @Override
    public Object clone() {
        return new MCParameters(this);
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a simple Monte-Carlo Search, use big populations sizes for faster drawing.";
    }

    @Override
    public void setOptimizer(InterfaceOptimizer optimizer) {
        // *pff* i'll ignore that!
    }


    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return ((MonteCarloSearch)this.optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((MonteCarloSearch)this.optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }
}
