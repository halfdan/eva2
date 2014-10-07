package eva2.optimization.modules;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.problems.B1Problem;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class gives access to all GA parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:29:34
 * To change this template use File | Settings | File Templates.
 */
public class GAParameters extends AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {

    private static final Logger LOGGER = Logger.getLogger(GAParameters.class.getName());

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
    @Override
    public Object clone() {
        return new GAParameters(this);
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is a Genetic Algorithm, which transforms into a GP or GE if the appropriate problem and genotype is used.";
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
        return ((GeneticAlgorithm) this.optimizer).getPopulation();
    }

    public void setPopulation(Population pop) {
        ((GeneticAlgorithm) this.optimizer).setPopulation(pop);
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

    /**
     * Choose a parent selection method.
     *
     * @param selection
     */
    public void setParentSelection(InterfaceSelection selection) {
        ((GeneticAlgorithm) this.optimizer).setParentSelection(selection);
    }

    public InterfaceSelection getParentSelection() {
        return ((GeneticAlgorithm) this.optimizer).getParentSelection();
    }

    public String parentSelectionTipText() {
        return "Choose a parent selection method.";
    }

    /**
     * Enable/disable elitism.
     *
     * @param elitism
     */
    public void setElitism(boolean elitism) {
        ((GeneticAlgorithm) this.optimizer).setElitism(elitism);
    }

    public boolean getElitism() {
        return ((GeneticAlgorithm) this.optimizer).getElitism();
    }

    public String elitismTipText() {
        return "Enable/disable elitism.";
    }

    /**
     * The number of mating partners needed to create offsprings.
     *
     * @param partners
     */
    public void setNumberOfPartners(int partners) {
        if (partners < 0) {
            partners = 0;
        }
        ((GeneticAlgorithm) this.optimizer).setNumberOfPartners(partners);
    }

    public int getNumberOfPartners() {
        return ((GeneticAlgorithm) this.optimizer).getNumberOfPartners();
    }

    public String numberOfPartnersTipText() {
        return "The number of mating partners needed to create offsprings.";
    }

    /**
     * Choose a selection method for selecting recombination partners for given parents.
     *
     * @param selection
     */
    public void setPartnerSelection(InterfaceSelection selection) {
        ((GeneticAlgorithm) this.optimizer).setPartnerSelection(selection);
    }

    public InterfaceSelection getPartnerSelection() {
        return ((GeneticAlgorithm) this.optimizer).getPartnerSelection();
    }

    public String partnerSelectionTipText() {
        return "Choose a selection method for selecting recombination partners for given parents.";
    }
}
