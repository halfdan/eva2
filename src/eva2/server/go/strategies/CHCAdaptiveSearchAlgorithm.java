package eva2.server.go.strategies;


import java.util.BitSet;

import wsi.ra.math.RNG;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.selection.SelectBestSingle;
import eva2.server.go.operators.selection.SelectRandom;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/** This is an implementation of the CHC Adaptive Search Algorithm by Eselman. It is
 * limited to binary data and is based on massively distruptive crossover. I'm not
 * shure whether i've implemented this correctly, but i definetly wasn't able to make
 * it competitive to a standard GA.. *sigh*
 * This is a implementation of the CHC Apative Search Algorithm.
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 307 $
 *            $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public class CHCAdaptiveSearchAlgorithm implements InterfaceOptimizer, java.io.Serializable {

    private double                          m_InitialDifferenceThreshold = 0.25;
    private int                             m_DifferenceThreshold;
    private double                          m_DivergenceRate    = 0.35;
    private boolean                         m_UseElitism        = true;
    private int                             m_NumberOfPartners  = 1;
    private Population                      m_Population        = new Population();
    private InterfaceOptimizationProblem    m_Problem           = new B1Problem();
    private InterfaceSelection              m_RecombSelectionOperator = new SelectRandom();
    private InterfaceSelection              m_PopulSelectionOperator = new SelectBestSingle();

    transient private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    public CHCAdaptiveSearchAlgorithm() {
    }

    public CHCAdaptiveSearchAlgorithm(CHCAdaptiveSearchAlgorithm a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_InitialDifferenceThreshold   = a.m_InitialDifferenceThreshold;
        this.m_DifferenceThreshold          = a.m_DifferenceThreshold;
        this.m_DivergenceRate               = a.m_DivergenceRate;
        this.m_NumberOfPartners             = a.m_NumberOfPartners;
        this.m_UseElitism                   = a.m_UseElitism;
        this.m_RecombSelectionOperator      = (InterfaceSelection)a.m_RecombSelectionOperator.clone();
        this.m_PopulSelectionOperator       = (InterfaceSelection)a.m_PopulSelectionOperator.clone();
    }

    public Object clone() {
        return (Object) new CHCAdaptiveSearchAlgorithm(this);
    }

    public void init() {
        this.m_Problem.initPopulation(this.m_Population);
        AbstractEAIndividual tmpIndy = ((AbstractEAIndividual)(this.m_Population.get(0)));
        if (tmpIndy instanceof InterfaceGAIndividual) {
            this.m_DifferenceThreshold = (int)(((InterfaceGAIndividual)tmpIndy).getGenotypeLength()*this.m_InitialDifferenceThreshold);
        } else {
            System.out.println("Problem does not apply InterfaceGAIndividual, which is the only individual type valid for CHC!");
        }

        this.evaluatePopulation(this.m_Population);
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population)pop.clone();
        if (reset) this.m_Population.init();
        AbstractEAIndividual tmpIndy = ((AbstractEAIndividual)(this.m_Population.get(0)));
        if (tmpIndy instanceof InterfaceGAIndividual) {
            this.m_DifferenceThreshold = (int)(((InterfaceGAIndividual)tmpIndy).getGenotypeLength()*this.m_InitialDifferenceThreshold);
        } else {
            System.out.println("Problem does not apply InterfaceGAIndividual, which is the only individual type valid for CHC!");
        }

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
        Population                  result = this.m_Population.cloneWithoutInds(), parents, partners;
        AbstractEAIndividual[]     offSprings;
        AbstractEAIndividual       tmpIndy;

        result.clear();
//        this.m_NormationOperator.computeSelectionProbability(this.m_Population, "Fitness");
        //System.out.println("Population:"+this.m_Population.getSolutionRepresentationFor());
        this.m_PopulSelectionOperator.prepareSelection(this.m_Population);
        this.m_RecombSelectionOperator.prepareSelection(this.m_Population);
        parents     = this.m_PopulSelectionOperator.selectFrom(this.m_Population, this.m_Population.getPopulationSize());
        //System.out.println("Parents:"+parents.getSolutionRepresentationFor());

        for (int i = 0; i < parents.size(); i++) {
            tmpIndy =  ((AbstractEAIndividual)parents.get(i));
            if (tmpIndy == null) System.out.println("Individual null "+i);
            if (this.m_Population == null) System.out.println("population null "+i);

            partners = this.m_RecombSelectionOperator.findPartnerFor(tmpIndy, this.m_Population, this.m_NumberOfPartners);
            if (this.computeHammingDistance(tmpIndy, partners) > this.m_DifferenceThreshold) {
                offSprings = tmpIndy.mateWith(partners);
                for (int j = 0; j < offSprings.length; j++) {
                    offSprings[j].mutate();
                }
                result.add(offSprings[0]);
            }
        }
        return result;
    }

    /** This method computes the Hamming Distance between n-Individuals
     * @param dad
     * @param partners
     * @return The maximal Hamming Distance between dad and the partners
     */
    private int computeHammingDistance(AbstractEAIndividual dad, Population partners) {
        int     result = 0, tmpDist;
        BitSet  tmpB1, tmpB2;

        tmpB1 = ((InterfaceGAIndividual)dad).getBGenotype();
        for (int i = 0; i < partners.size(); i++) {
            tmpB2 = ((InterfaceGAIndividual) partners.get(i)).getBGenotype();
            tmpDist = 0;
            for (int j = 0; j < ((InterfaceGAIndividual)dad).getGenotypeLength(); j++) {
                if (tmpB1.get(j) == tmpB2.get(j)) tmpDist++;
            }
            result = Math.max(result, tmpDist);
        }
        return result;
    }

    /** This method method replaces the current population with copies of the current
     * best individual but all but one are randomized with a very high mutation rate.
     */
    private void diverge() {
        AbstractEAIndividual    best = this.m_Population.getBestEAIndividual();
        InterfaceGAIndividual   mutant;
        BitSet                  tmpBitSet;

        this.m_Population.clear();
        this.m_Population.add(best);
        for (int i = 1; i < this.m_Population.getPopulationSize(); i++) {
            mutant      = (InterfaceGAIndividual)best.clone();
            tmpBitSet   = mutant.getBGenotype();
            for (int j = 0; j < mutant.getGenotypeLength(); j++) {
                if (RNG.flipCoin(this.m_DivergenceRate)) {
                    if (tmpBitSet.get(j)) tmpBitSet.clear(j);
                    else tmpBitSet.set(j);
                }
            }
            mutant.SetBGenotype(tmpBitSet);
            this.m_Population.add(mutant);
        }
        if (best instanceof InterfaceGAIndividual) {
            this.m_DifferenceThreshold = (int)(this.m_DivergenceRate* (1-this.m_DivergenceRate) * ((InterfaceGAIndividual)best).getGenotypeLength());

        }
        this.evaluatePopulation(this.m_Population);
    }

    public void optimize() {
        Population nextGeneration, tmp;
        //AbstractEAIndividual   elite;

        if (this.m_DifferenceThreshold < 0) {
            this.diverge();
        } else {
            nextGeneration = this.generateChildren();
            if (nextGeneration.size() == 0) {
                this.m_DifferenceThreshold--;
            } else {
                this.evaluatePopulation(nextGeneration);
                if (nextGeneration.getWorstEAIndividual().getFitness(0) > this.m_Population.getBestEAIndividual().getFitness(0)) {
                    this.m_DifferenceThreshold--;
                }
            }
            nextGeneration.addPopulation(this.m_Population);
//            this.m_NormationOperator.computeSelectionProbability(nextGeneration, "Fitness");
            this.m_PopulSelectionOperator.prepareSelection(this.m_Population);
            tmp = this.m_PopulSelectionOperator.selectFrom(nextGeneration, this.m_Population.getPopulationSize());
            nextGeneration.clear();
            nextGeneration.addPopulation(tmp);
            this.m_Population = nextGeneration;
        }
        this.firePropertyChangedEvent("NextGenerationPerformed");
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

    /** This method will set the problem that is to be optimized
     * @param problem
     */
    public void SetProblem (InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
    }
    public InterfaceOptimizationProblem getProblem () {
        return this.m_Problem;
    }

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "CHC Adaptive Search Algorithm:\n";
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
        return "This is an implementation of the CHC Adaptive Search Algorithm by Eselman.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "CHC";
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
        return "Edit the properties of the population used.";
    }
    
    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
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

    /** Enable/disable elitism.
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

    /** The number of mating partners needed to create offsprings.
     * @param partners
     */
    public void setNumberOfPartners(int partners) {
        if (partners < 0) partners = 0;
        this.m_NumberOfPartners = partners;
    }
    public int getNumberOfPartners() {
        return this.m_NumberOfPartners;
    }
    public String numberOfPartnersTipText() {
        return "The number of mating partners needed to create offsprings.";
    }
}
