package eva2.optimization.modules;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.replacement.InterfaceReplacement;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.problems.B1Problem;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.optimization.strategies.SteadyStateGA;
import eva2.tools.Serializer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;

/** The class gives access to all SSGA parameters for the EvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.07.2005
 * Time: 15:44:34
 * To change this template use File | Settings | File Templates.
 */
public class SSGAParameters extends AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {

    /**
     * Load or create a new instance of the class.
     * 
     * @return A loaded (from file) or new instance of the class.
     */
    public static SSGAParameters getInstance() {
        SSGAParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream("SSGAParameters.ser");
            instance = (SSGAParameters) Serializer.loadObject(fileStream);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new SSGAParameters();
        }
        return instance;
    }
    
    /**
     *
     */
    public SSGAParameters() {    
    	super(new SteadyStateGA(), new B1Problem(), new EvaluationTerminator());
    }
    
    private SSGAParameters(SSGAParameters Source) {
        super(Source);
    }

    @Override
    public Object clone() {
        return new SSGAParameters(this);
    }

    /** 
     * This method returns a global info string.
     * @return description
     */
    public static String globalInfo() {
        return "This is a steady-state GA.";
    }

    @Override
    public void setOptimizer(InterfaceOptimizer optimizer) {
        // *pff* i'll ignore that!
    }

    /** Assuming that all optimizer will store their data in a population
     * we will allow access to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return ((SteadyStateGA) this.optimizer).getPopulation();
    }

    public void setPopulation(Population pop){
        ((SteadyStateGA) this.optimizer).setPopulation(pop);
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
        ((SteadyStateGA) this.optimizer).setParentSelection(selection);
    }

    public InterfaceSelection getParentSelection() {
        return ((SteadyStateGA) this.optimizer).getParentSelection();
    }

    public String parentSelectionTipText() {
        return "Choose a parent selection method.";
    }

    /**
     * This method will set the number of partners that are needed to create
     * offsprings by mating.
     *
     * @param partners Number of partners needed for mating
     */
    public void setNumberOfPartners(int partners) {
        if (partners < 0) {
            partners = 0;
        }
        ((SteadyStateGA) this.optimizer).setNumberOfPartners(partners);
    }
    
    public int getNumberOfPartners() {
        return ((SteadyStateGA) this.optimizer).getNumberOfPartners();
    }
    public String numberOfPartnersTipText() {
        return "The number of mating partners needed to create offsprings.";
    }

    /** Choose a selection method for selecting recombination partners for given parents.
     * @param selection
     */
    public void setPartnerSelection(InterfaceSelection selection) {
        ((SteadyStateGA)this.optimizer).setPartnerSelection(selection);
    }
    public InterfaceSelection getPartnerSelection() {
        return ((SteadyStateGA)this.optimizer).getPartnerSelection();
    }
    public String partnerSelectionTipText() {
        return "Choose a selection method for selecting recombination partners for given parents.";
    }

    /** Choose a replacement strategy.
     * @param s     A InterfaceReplacement strategy.
     */
    public void setReplacementSelection(InterfaceReplacement s) {
        ((SteadyStateGA)this.optimizer).setReplacementSelection(s);
    }
    public InterfaceReplacement getReplacementSelection() {
        return ((SteadyStateGA)this.optimizer).getReplacementSelection();
    }
    public String replacementSelectionTipText() {
        return "Choose a replacement strategy.";
    }
}