package javaeva.server.go.problems;

import java.awt.BorderLayout;
import java.util.BitSet;

import javaeva.server.go.PopulationInterface;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceDataTypeBinary;
import javaeva.server.go.individuals.InterfaceDataTypeDouble;
import javaeva.server.go.individuals.InterfaceDataTypeInteger;
import javaeva.server.go.individuals.InterfaceDataTypePermutation;
import javaeva.server.go.operators.moso.MOSONoConvert;
import javaeva.server.go.populations.Population;
import javaeva.server.go.strategies.InterfaceOptimizer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 29.08.2003
 * Time: 13:40:12
 * To change this template use Options | File Templates.
 */
public abstract class AbstractOptimizationProblem implements InterfaceOptimizationProblem, java.io.Serializable {

    protected 	AbstractEAIndividual      m_Template;

    /** This method returns a deep clone of the problem.
     * @return  the clone
     */
    public abstract Object clone();

    /** This method inits the Problem to log multiruns
     */
    public abstract void initProblem();

    /******************** The most important methods ****************************************/

    /** This method inits a given population
     * @param population    The populations that is to be inited
     */
    public abstract void initPopulation(Population population);

    /** This method evaluates a given population and set the fitness values
     * accordingly
     * @param population    The population that is to be evaluated.
     */
    public void evaluate(Population population) {
        AbstractEAIndividual    tmpIndy;

        // @todo This is the position to implement a granular
        // @todo paralliziation scheme
        evaluatePopulationStart(population);
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = (AbstractEAIndividual) population.get(i);
            tmpIndy.resetConstraintViolation();
            this.evaluate(tmpIndy);
            population.incrFunctionCalls();
        }
        evaluatePopulationEnd(population);
    }
    
    /**
     * Empty thunk for implementation in subclasses. This is called right before a population is evaluated.
     * Made public because some steady-state optimizers do not call evaluate(Population).
     *
     * @param population
     */   
    
    public void evaluatePopulationStart(Population population) {
	}

	/**
     * Empty thunk for implementation in subclasses. This is called after a population was evaluated.
     *
     * @param population
     */
    public void evaluatePopulationEnd(Population population) {
    }
    
    /** This method evaluate a single individual and sets the fitness values
     * @param individual    The individual that is to be evaluated
     */
    public abstract void evaluate(AbstractEAIndividual individual);

    /******************** Some output methods *******************************************/

    /** This method allows you to output a string that describes a found solution
     * in a way that is most suitable for a given problem.
     * @param individual    The individual that is to be shown.
     * @return The description.
     */
    public String getSolutionRepresentationFor(AbstractEAIndividual individual) {
    	return AbstractEAIndividual.getDefaultStringRepresentation(individual);
    }

    /** This method returns a single line representation of the solution
     * @param individual  The individual
     * @return The string
     */
//    public String getSolutionDataFor(IndividualInterface individual) {
//    }

//    /** This method returns a string describing the optimization problem.
//     * @return The description.
//     */
//    public String getStringRepresentationF() {
//        return "AbstractOptimizationProblem: programmer failed to give further details";
//    }

    /** This method returns a double value that will be displayed in a fitness
     * plot. A fitness that is to be minimized with a global min of zero
     * would be best, since log y can be used. But the value can depend on the problem.
     * @param pop   The population that is to be refined.
     * @return Double value
     */
    public Double getDoublePlotValue(Population pop) {
        return new Double(pop.getBestEAIndividual().getFitness(0));
    }

    /** This method returns the header for the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringHeader(PopulationInterface pop) {
        return "Solution";
    }

    /** This method returns the additional data that is to be written into a file
     * @param pop   The population that is to be refined.
     * @return String
     */
    public String getAdditionalFileStringValue(PopulationInterface pop) {
        return AbstractEAIndividual.getDefaultDataString(pop.getBestIndividual());
    }

    /** This method allows you to request a graphical represenation for a given
     * individual.
     * @return JComponent
     */
    public JComponent drawIndividual(AbstractEAIndividual indy) {
        JPanel      result  = new JPanel();
        result.setLayout(new BorderLayout());
        JTextArea   area    = new JTextArea();
        JScrollPane scroll  = new JScrollPane(area);
        area.setText("Best Solution:\n"+this.getSolutionRepresentationFor(indy));
        area.setEditable(false);
        result.add(scroll, BorderLayout.CENTER);
        return result;
    }

    /** This method will report whether or not this optimization problem is truly
     * multi-objective
     * @return True if multi-objective, else false.
     */
    public boolean isMultiObjective() {
        if (this instanceof AbstractMultiObjectiveOptimizationProblem) {
            if (((AbstractMultiObjectiveOptimizationProblem)this).getMOSOConverter() instanceof MOSONoConvert) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /**
     * For some evaluation cases it may be necessary to inform the problem class about the optimizer in use.
     *  
     * @param opt
     */
    public void informAboutOptimizer(InterfaceOptimizer opt) {
    	
    }

    /**
     * This allows "anyone" to access the problem template and set operators etc.
     * Subclasses may have a method getEAIndividual additionally with a more
     * specific interface signature, which makes sense for the GUI which decides
     * on what classes to present to the user based on the interface signature. 
     * 
     * @return
     */
    public AbstractEAIndividual getIndividualTemplate() {
    	return m_Template;
    }
    
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "AbstractOptimizationProblem";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The programmer failed to give further details.";
    }
}
