package eva2.server.modules;


import java.io.Serializable;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.selection.replacement.InterfaceReplacement;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.strategies.SteadyStateGA;
import eva2.tools.Serializer;

/** The class gives access to all SSGA parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 19.07.2005
 * Time: 15:44:34
 * To change this template use File | Settings | File Templates.
 */
public class SSGAParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;

    /**
     *
     */
    public static SSGAParameters getInstance() {
        if (TRACE) System.out.println("SSGAParameters getInstance 1");
        SSGAParameters Instance = (SSGAParameters) Serializer.loadObject("SSGAParameters.ser");
        if (TRACE) System.out.println("SSGAParameters getInstance 2");
        if (Instance == null) Instance = new SSGAParameters();
        return Instance;
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("SSGAParameters.ser",this);
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

    public Object clone() {
        return new SSGAParameters(this);
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a steady-state GA.";
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
        return ((SteadyStateGA)this.m_Optimizer).getPopulation();
    }
    public void setPopulation(Population pop){
        ((SteadyStateGA)this.m_Optimizer).setPopulation(pop);
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
        ((SteadyStateGA)this.m_Optimizer).setParentSelection(selection);
    }
    public InterfaceSelection getParentSelection() {
        return ((SteadyStateGA)this.m_Optimizer).getParentSelection();
    }
    public String parentSelectionTipText() {
        return "Choose a parent selection method.";
    }

    /** This method will set the number of partners that are needed to create
     * offsprings by mating
     * @param partners
     */
    public void setNumberOfPartners(int partners) {
        if (partners < 0) partners = 0;
        ((SteadyStateGA)this.m_Optimizer).setNumberOfPartners(partners);
    }
    public int getNumberOfPartners() {
        return ((SteadyStateGA)this.m_Optimizer).getNumberOfPartners();
    }
    public String numberOfPartnersTipText() {
        return "The number of mating partners needed to create offsprings.";
    }

    /** Choose a selection method for selecting recombination partners for given parents.
     * @param selection
     */
    public void setPartnerSelection(InterfaceSelection selection) {
        ((SteadyStateGA)this.m_Optimizer).setPartnerSelection(selection);
    }
    public InterfaceSelection getPartnerSelection() {
        return ((SteadyStateGA)this.m_Optimizer).getPartnerSelection();
    }
    public String partnerSelectionTipText() {
        return "Choose a selection method for selecting recombination partners for given parents.";
    }

    /** Choose a replacement strategy.
     * @param s     A InterfaceReplacement strategy.
     */
    public void setReplacementSelection(InterfaceReplacement s) {
        ((SteadyStateGA)this.m_Optimizer).setReplacementSelection(s);
    }
    public InterfaceReplacement getReplacementSelection() {
        return ((SteadyStateGA)this.m_Optimizer).getReplacementSelection();
    }
    public String replacementSelectionTipText() {
        return "Choose a replacement strategy.";
    }
}