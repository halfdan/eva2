package eva2.server.go.strategies;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.selection.SelectEPTournaments;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/** Evolutionary programming by Fogel. Works fine but is actually a quite greedy local search
 * strategy solely based on mutation. To prevent any confusion, the mutation rate is temporaily
 * set to 1.0.
 * Potential citation: the PhD thesis of David B. Fogel (1992).
 * 
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 307 $
 *            $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public class EvolutionaryProgramming implements InterfaceOptimizer, java.io.Serializable {

    private int                             m_PopulationSize    = 0;
    private Population                      m_Population        = new Population();
    private InterfaceOptimizationProblem    m_Problem           = new F1Problem();
    private InterfaceSelection              m_EnvironmentSelection = new SelectEPTournaments();

    private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    public EvolutionaryProgramming() {
    }

    public EvolutionaryProgramming(EvolutionaryProgramming a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_Identifier                   = a.m_Identifier;
        this.m_EnvironmentSelection         = (InterfaceSelection)a.m_EnvironmentSelection.clone();
    }

    public Object clone() {
        return (Object) new EvolutionaryProgramming(this);
    }

        public void init() {
            this.m_Problem.initPopulation(this.m_Population);
            this.evaluatePopulation(this.m_Population);
            this.m_PopulationSize = this.m_Population.size();
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

        /** This method will generate the offspring population from the
         * given population of evaluated individuals.
         */
        private Population generateChildren() {
            Population                 result = (Population)this.m_Population.cloneWithoutInds();
            AbstractEAIndividual       mutant;

            result.clear();
            for (int i = 0; i < this.m_Population.size(); i++) {
                mutant = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Population.get(i)).clone();
                double tmpD = mutant.getMutationProbability();
                mutant.setMutationProbability(1.0);
                mutant.mutate();
                mutant.setMutationProbability(tmpD);
                result.add(mutant);
            }
            return result;
        }

        public void optimize() {
            Population nextGeneration, parents;

            this.m_EnvironmentSelection.prepareSelection(this.m_Population);
            parents = this.m_EnvironmentSelection.selectFrom(this.m_Population, this.m_PopulationSize);
            this.m_Population.clear();
            this.m_Population.addPopulation(parents);
            nextGeneration = this.generateChildren();
            this.evaluatePopulation(nextGeneration);
            nextGeneration.addPopulation(this.m_Population);
            this.m_Population = nextGeneration;

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
        result += "Evolutionary Programming:\n";
        result += "Optimization Problem: ";
        result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
        result += this.m_Population.getStringRepresentation();
        return result;
    }
    /** This method allows you to set an identifier for the algorithm
     * @param name      The indenifier
     */
    public void setIdentifier(String name) {
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
        return "This is a basic Evolutionary Programming scheme.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "EP";
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
    /** Choose a method for selecting the reduced population.
     * @param selection
     */
    public void setEnvironmentSelection(InterfaceSelection selection) {
        this.m_EnvironmentSelection = selection;
    }
    public InterfaceSelection getEnvironmentSelection() {
        return this.m_EnvironmentSelection;
    }
    public String environmentSelectionTipText() {
        return "Choose a method for selecting the reduced population.";
    }
}
