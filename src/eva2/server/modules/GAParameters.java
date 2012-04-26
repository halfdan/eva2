package eva2.server.modules;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.strategies.GeneticAlgorithm;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/** The class gives access to all GA parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:29:34
 * To change this template use File | Settings | File Templates.
 */
public class GAParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(eva2.EvAInfo.defaultLogger);
    
    /**
     * Load or create a new instance of the class.
     * 
     * @return A loaded (from file) or new instance of the class.
     */
    public static GAParameters getInstance() {
        GAParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream("GAParameters.ser");
            instance = (GAParameters) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new GAParameters();
        }
        return instance;
    }

    /**
     *
     */
    public GAParameters() {
    	super(new GeneticAlgorithm(), new B1Problem(), new EvaluationTerminator());
    }

    /**
     *
     */
    private GAParameters(GAParameters Source) {
    	super(Source);
    }

    /**
     *
     */
    public Object clone() {
        return new GAParameters(this);
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a Genetic Algorithm, which transforms into a GP or GE if the appropriate problem and genotype is used.";
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
        return ((GeneticAlgorithm)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((GeneticAlgorithm)this.m_Optimizer).setPopulation(pop);
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

//    /** This method will set the normation method that is to be used.
//     * @param normation
//     */
//    public void setNormationMethod (InterfaceNormation normation) {
//        this.m_NormationOperator = normation;
//    }
//    public InterfaceNormation getNormationMethod () {
//        return this.m_NormationOperator;
//    }
//    public String normationMethodTipText() {
//        return "Select the normation method.";
//    }

    /** Choose a parent selection method.
     * @param selection
     */
    public void setParentSelection(InterfaceSelection selection) {
        ((GeneticAlgorithm)this.m_Optimizer).setParentSelection(selection);
    }
    public InterfaceSelection getParentSelection() {
        return ((GeneticAlgorithm)this.m_Optimizer).getParentSelection();
    }
    public String parentSelectionTipText() {
        return "Choose a parent selection method.";
    }

    /** Enable/disable elitism.
     * @param elitism
     */
    public void setElitism (boolean elitism) {
        ((GeneticAlgorithm)this.m_Optimizer).setElitism(elitism);
    }
    public boolean getElitism() {
        return ((GeneticAlgorithm)this.m_Optimizer).getElitism();
    }
    public String elitismTipText() {
        return "Enable/disable elitism.";
    }

    /** The number of mating partners needed to create offsprings.
     * @param partners
     */
    public void setNumberOfPartners(int partners) {
        if (partners < 0) partners = 0;
        ((GeneticAlgorithm)this.m_Optimizer).setNumberOfPartners(partners);
    }
    public int getNumberOfPartners() {
        return ((GeneticAlgorithm)this.m_Optimizer).getNumberOfPartners();
    }
    public String numberOfPartnersTipText() {
        return "The number of mating partners needed to create offsprings.";
    }

    /** Choose a selection method for selecting recombination partners for given parents.
     * @param selection
     */
    public void setPartnerSelection(InterfaceSelection selection) {
        ((GeneticAlgorithm)this.m_Optimizer).setPartnerSelection(selection);
    }
    public InterfaceSelection getPartnerSelection() {
        return ((GeneticAlgorithm)this.m_Optimizer).getPartnerSelection();
    }
    public String partnerSelectionTipText() {
        return "Choose a selection method for selecting recombination partners for given parents.";
    }
}
