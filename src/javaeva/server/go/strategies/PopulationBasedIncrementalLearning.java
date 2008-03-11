package javaeva.server.go.strategies;

import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.operators.selection.InterfaceSelection;
import javaeva.server.go.operators.selection.SelectBestIndividuals;
import javaeva.server.go.populations.PBILPopulation;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.B1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;

/** Population based increamental learning in the PSM by Mommarche
 * version with also allows to simulate ant systems due to the flexible
 * update rule of V. But both are limited to binary gentypes.
 * This is a simple implementation of Population Based Incremental Learning.
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 307 $
 *            $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public class PopulationBasedIncrementalLearning implements InterfaceOptimizer, java.io.Serializable {

    // These variables are necessary for the simple testcase
    private InterfaceOptimizationProblem            m_Problem           = new B1Problem();
    private boolean                                 m_UseElitism        = true;
    private InterfaceSelection                      m_SelectionOperator = new SelectBestIndividuals();
    transient private String                        m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;
    private Population                              m_Population        = new PBILPopulation();
    private double                                  m_LearningRate      = 0.04;
    private double                                  m_MutationRate      = 0.5;
    private double                                  m_MutateSigma       = 0.01;
    private int                                     m_NumberOfPositiveSamples = 1;

    public PopulationBasedIncrementalLearning() {
    }

    public PopulationBasedIncrementalLearning(PopulationBasedIncrementalLearning a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_LearningRate                 = a.m_LearningRate;
        this.m_MutationRate                 = a.m_MutationRate;
        this.m_MutateSigma                  = a.m_MutateSigma;
        this.m_NumberOfPositiveSamples      = a.m_NumberOfPositiveSamples;
        this.m_UseElitism                   = a.m_UseElitism;
        this.m_SelectionOperator            = (InterfaceSelection)a.m_SelectionOperator.clone();
    }

    public Object clone() {
        return (Object) new PopulationBasedIncrementalLearning(this);
    }

    public void init() {
        this.m_Problem.initPopulation(this.m_Population);
        this.evaluatePopulation(this.m_Population);
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = new PBILPopulation();
        if (reset) this.m_Population.init();
        this.m_Population.addPopulation((Population)pop.clone());
        ((PBILPopulation)this.m_Population).buildProbabilityVector();
        this.evaluatePopulation(this.m_Population);
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will evaluate the current population using the
     * given problem.
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
        this.m_Problem.evaluate(population);
        population.incrGeneration();
    }

    /** This method will generate the offspring population from the
     * given population of evaluated individuals.
     */
    private Population generateChildren() {
        PBILPopulation                  result = (PBILPopulation)this.m_Population.clone();
        Population                      examples;

//        this.m_NormationOperator.computeSelectionProbability(this.m_Population, "Fitness");
        //System.out.println("Population:"+this.m_Population.getSolutionRepresentationFor());
        this.m_SelectionOperator.prepareSelection(this.m_Population);
        examples     = this.m_SelectionOperator.selectFrom(this.m_Population, this.m_NumberOfPositiveSamples);
        //System.out.println("Parents:"+parents.getSolutionRepresentationFor());
        result.learnFrom(examples, this.m_LearningRate);
        result.mutateProbabilityVector(this.m_MutationRate, this.m_MutateSigma);
        result.initPBIL();
        return result;
    }

    public void optimize() {
        Population nextGeneration;
        AbstractEAIndividual   elite;

        nextGeneration = this.generateChildren();
        this.evaluatePopulation(nextGeneration);
        if (this.m_UseElitism) {
            elite = this.m_Population.getBestEAIndividual();
            this.m_Population = nextGeneration;
            this.m_Population.add(0, elite);
        } else {
            this.m_Population = nextGeneration;
        }
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will set the problem that is to be optimized
     * @param problem
     */
    public void SetProblem (InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
    }
    public InterfaceOptimizationProblem getProblem () {
        return this.m_Problem;
    }

    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }
    /** Something has changed
     */
    protected void firePropertyChangedEvent (String name) {
        if (this.m_Listener != null) this.m_Listener.registerPopulationStateChanged(this, name);
    }

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "Population Based Incremental Learning:\n";
        result += "Optimization Problem: ";
        result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
        result += this.m_Population.getStringRepresentation();
        return result;
    }
    /** This method allows you to set an identifier for the algorithm
     * @param name      The indenifier
     */
     public void SetIdentifier(String name) {
        this.m_Identifier = name;
    }
     public String getIdentifier() {
         return this.m_Identifier;
     }

    /** This method is required to free the memory on a RMIServer,
     * but there is nothing to implement.
     */
    public void freeWilly() {

    }    
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "The Population based incremental learning is based on a statistical distribution of bit positions. Please note: This optimizer requires a binary genotype!";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "PBIL";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return this.m_Population;
    }
    public void setPopulation(Population pop){
        this.m_Population = pop;
    }
    public String populationTipText() {
        return "Edit the properties of the PBIL population used.";
    }
    
    public Population getAllSolutions() {
    	return getPopulation();
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

    /** This method will set the selection method that is to be used
     * @param selection
     */
    public void setSelectionMethod(InterfaceSelection selection) {
        this.m_SelectionOperator = selection;
    }
    public InterfaceSelection getSelectionMethod() {
        return this.m_SelectionOperator;
    }
    public String selectionMethodTipText() {
        return "Choose a selection method.";
    }

    /** This method will set the problem that is to be optimized
     * @param elitism
     */
    public void setElitism (boolean elitism) {
        this.m_UseElitism = elitism;
    }
    public boolean getElitism() {
        return this.m_UseElitism;
    }
    public String elitismTipText() {
        return "Enable/disable elitism.";
    }

    /** This method will set the learning rate for PBIL
     * @param LearningRate
     */
    public void setLearningRate (double LearningRate) {
        this.m_LearningRate = LearningRate;
        if (this.m_LearningRate < 0) this.m_LearningRate = 0;
    }
    public double getLearningRate() {
        return this.m_LearningRate;
    }
    public String learningRateTipText() {
        return "The learing rate of PBIL.";
    }

    /** This method will set the mutation rate for PBIL
     * @param m
     */
    public void setMutationRate (double m) {
        this.m_MutationRate = m;
        if (this.m_MutationRate < 0) this.m_MutationRate = 0;
        if (this.m_MutationRate > 1) this.m_MutationRate = 1;
     }
    public double getMutationRate() {
        return this.m_MutationRate;
    }
    public String mutationRateTipText() {
        return "The mutation rate of PBIL.";
    }

    /** This method will set the mutation sigma for PBIL
     * @param m
     */
    public void setMutateSigma (double m) {
        this.m_MutateSigma = m;
        if (this.m_MutateSigma < 0) this.m_MutateSigma = 0;
    }
    public double getMutateSigma() {
        return this.m_MutateSigma;
    }
    public String mutateSigmaTipText() {
        return "Set the sigma for the mutation of the probability vector.";
    }

    /** This method will set the number of positive samples for PBIL
     * @param PositiveSamples
     */
    public void setPositiveSamples (int PositiveSamples) {
        this.m_NumberOfPositiveSamples = PositiveSamples;
        if (this.m_NumberOfPositiveSamples < 1) this.m_NumberOfPositiveSamples = 1;
    }
    public int getPositiveSamples() {
        return this.m_NumberOfPositiveSamples;
    }
    public String positiveSamplesTipText() {
        return "The number of positive samples that update the PBIL vector.";
    }
}