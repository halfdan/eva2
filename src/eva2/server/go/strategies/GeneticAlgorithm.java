package eva2.server.go.strategies;

import eva2.gui.BeanInspector;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.operators.mutation.InterfaceMutationGenerational;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.selection.SelectTournament;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/** The traditional genetic algorithms as devised by Holland. To only special here
 * it the plague factor which reduces the population size to tune from a global to
 * a more local search. But you have to be careful with that else the GA might not
 * converge.
 * This is a implementation of Genetic Algorithms.
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 307 $
 *            $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public class GeneticAlgorithm implements InterfaceOptimizer, java.io.Serializable {

        private Population                      m_Population        = new Population();
        private InterfaceOptimizationProblem    m_Problem           = new B1Problem();
        private InterfaceSelection              m_ParentSelection   = new SelectTournament();
        private InterfaceSelection              m_PartnerSelection  = new SelectTournament();
        private boolean                         m_UseElitism        = true;
        private int                             m_Plague            = 0;
        private int                             m_NumberOfPartners  = 1;

        private String                          m_Identifier = "";
        transient private InterfacePopulationChangedEventListener m_Listener;

        public GeneticAlgorithm() {
        }

        public GeneticAlgorithm(GeneticAlgorithm a) {
            this.m_Population                   = (Population)a.m_Population.clone();
            this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
            this.m_Identifier                   = a.m_Identifier;
            this.m_Plague                       = a.m_Plague;
            this.m_NumberOfPartners             = a.m_NumberOfPartners;
            this.m_UseElitism                   = a.m_UseElitism;
            this.m_ParentSelection              = (InterfaceSelection)a.m_ParentSelection.clone();
            this.m_PartnerSelection             = (InterfaceSelection)a.m_PartnerSelection.clone();
        }

        public Object clone() {
            return (Object) new GeneticAlgorithm(this);
        }

        public void init() {
            this.m_Problem.initPopulation(this.m_Population);
            this.evaluatePopulation(this.m_Population);
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }

        /** This method will init the optimizer with a given population
         * @param reset     If true the population is reset.
         */
        public void initByPopulation(Population pop, boolean reset) {
            this.m_Population = (Population)pop.clone();
            if (reset) {
            	this.m_Population.init();
                this.evaluatePopulation(this.m_Population);
                this.firePropertyChangedEvent(Population.nextGenerationPerformed);
            }
        }

        /** This method will evaluate the current population using the
         * given problem.
         * @param population The population that is to be evaluated
         */
        private void evaluatePopulation(Population population) {
            this.m_Problem.evaluate(population);
            population.incrGeneration();
        }

//        /** This method will assign fitness values to all individual in the
//         * current population.
//         * @param population The population that is to be evaluated
//         */
//        private void defaultEvaluatePopulation(Population population) {
//            GAIndividualBinaryData tmpIndy;
//            for (int i = 0; i < population.size(); i++) {
//                tmpIndy = (GAIndividualBinaryData) population.get(i);
//                tmpIndy.SetFitness(0, tmpIndy.defaultEvaulateAsMiniBits());
//                population.incrFunctionCalls();
//            }
//            population.incrGeneration();
//        }

        /** This method will generate the offspring population from the
         * given population of evaluated individuals.
         */
        private Population generateChildren() {
            Population  result = m_Population.cloneWithoutInds();
            Population 	parents;
            AbstractEAIndividual[]     offSprings;
            AbstractEAIndividual       tmpIndy;

            //this.m_NormationOperator.computeSelectionProbability(this.m_Population, "Fitness");
            //System.out.println("Population:"+this.m_Population.getSolutionRepresentationFor());
            this.m_ParentSelection.prepareSelection(this.m_Population);
            this.m_PartnerSelection.prepareSelection(this.m_Population);
            parents     = this.m_ParentSelection.selectFrom(this.m_Population, this.m_Population.getTargetSize());
//            System.out.println("Parents:"+parents.getStringRepresentation());
//            double[] meas = parents.getPopulationMeasures();

            if (parents.getEAIndividual(0).getMutationOperator() instanceof InterfaceMutationGenerational) {
            	((InterfaceMutationGenerational)parents.getEAIndividual(0).getMutationOperator()).adaptAfterSelection(m_Population, parents);
            }
            
            for (int i = 0; i < parents.size(); i++) {
                tmpIndy =  ((AbstractEAIndividual)parents.get(i));
                if (tmpIndy == null) System.out.println("Individual null " + i + " Population size: "+ parents.size());
                if (this.m_Population == null) System.out.println("population null "+i);

                offSprings = tmpIndy.mateWith(this.m_PartnerSelection.findPartnerFor(tmpIndy, this.m_Population, this.m_NumberOfPartners));
//                for (int j = 0; j < offSprings.length; j++) {
//                    offSprings[j].mutate(); // quite useless if n-1 are thrown away...
//                }
                offSprings[0].mutate();
                result.add(i, offSprings[0]);
            }
            this.evaluatePopulation(result);
            
            if (parents.getEAIndividual(0).getMutationOperator() instanceof InterfaceMutationGenerational) {
            	((InterfaceMutationGenerational)parents.getEAIndividual(0).getMutationOperator()).adaptGenerational(m_Population, parents, result, true);
            }
            return result;
        }

        public void optimize() {
            Population nextGeneration;
            nextGeneration = this.generateChildren();

            if (this.m_UseElitism) {
            	AbstractEAIndividual elite = this.m_Population.getBestEAIndividual();
                if (elite != null) {
                    this.m_Population = nextGeneration;
                    this.m_Population.remove(0);// This implements a random replacement strategy for the elite
                    this.m_Population.add(elite);
                }
            } else {
                this.m_Population = nextGeneration;
            }
            if (this.m_Plague > 0) {
                for (int i = 0; i < this.m_Plague; i++) if (this.m_Population.size() > 2) this.m_Population.remove(this.m_Population.getWorstEAIndividual());
                this.m_Population.setTargetSize(this.m_Population.size());
            }
//            System.out.println("Population size: " + this.m_Population.size());
//            System.out.println("Population: " + m_Population.getStringRepresentation());
//        if (this.m_Population.getArchive() != null) {
//            if (this.m_Population.getArchive().getArchive() != null) {
//                System.out.println("Zwei Archive!");
//                this.m_Population.getArchive().SetArchive(null);
//            }
//        }
//        this.m_Population.incrGeneration();
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }

        /** This method allows you to add the LectureGUI as listener to the Optimizer
         * @param ea
         */
        public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
            this.m_Listener = ea;
        }
    	public boolean removePopulationChangedEventListener(
    			InterfacePopulationChangedEventListener ea) {
    		if (m_Listener==ea) {
    			m_Listener=null;
    			return true;
    		} else return false;
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
            result += "Genetic Algorithm:\n";
            result += "Using:\n";
            result += " Population Size    = " + this.m_Population.getTargetSize() + "/" + this.m_Population.size() + "\n";
            result += " Parent Selection   = " + this.m_ParentSelection.getClass().toString() + "\n";
            result += " Partner Selection  = " + this.m_PartnerSelection.getClass().toString() + "\n";
            result += " Number of Partners = " + this.m_NumberOfPartners + "\n";
            result += " Elitism            = " + this.m_UseElitism + "\n";       
            result += "=> The Optimization Problem: ";
            result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
            //result += this.m_Population.getStringRepresentation();
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
        public static String globalInfo() {
            return "This is a basic generational Genetic Algorithm.";
        }
        /** This method will return a naming String
         * @return The name of the algorithm
         */
        public String getName() {
            return "GA";
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

        /** Choose a parent selection method.
         * @param selection
         */
        public void setParentSelection(InterfaceSelection selection) {
            this.m_ParentSelection = selection;
        }
        public InterfaceSelection getParentSelection() {
            return this.m_ParentSelection;
        }
        public String parentSelectionTipText() {
            return "Choose a parent selection method.";
        }

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

        /** Choose a selection method for selecting recombination partners for given parents.
         * @param selection
         */
        public void setPartnerSelection(InterfaceSelection selection) {
            this.m_PartnerSelection = selection;
        }
        public InterfaceSelection getPartnerSelection() {
            return this.m_PartnerSelection;
        }
        public String partnerSelectionTipText() {
            return "Choose a selection method for selecting recombination partners for given parents.";
        }
}
