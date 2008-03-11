package javaeva.server.modules;

import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.operators.selection.InterfaceSelection;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.B1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.strategies.GeneticAlgorithm;
import javaeva.server.go.strategies.HillClimbing;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.tools.Serializer;

import java.io.Serializable;

/** The class gives access to all GA parameters for the JavaEvA
 * top level GUI.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 08.06.2004
 * Time: 21:29:34
 * To change this template use File | Settings | File Templates.
 */
public class GAParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;

    public static GAParameters getInstance() {
        if (TRACE) System.out.println("GAParameters getInstance 1");
        GAParameters Instance = (GAParameters) Serializer.loadObject("GAParameters.ser");
        if (TRACE) System.out.println("GAParameters getInstance 2");
        if (Instance == null) Instance = new GAParameters();
        return Instance;
    }

    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("GAParameters.ser",this);
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
    public String globalInfo() {
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
