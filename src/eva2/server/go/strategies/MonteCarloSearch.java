package eva2.server.go.strategies;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/** 
 * The simple random or Monte-Carlo search, simple but useful
 * to evaluate the complexity of the search space.
 * This implements a Random Walk Search using the initialization
 * method of the problem instance, meaning that the random characteristics
 * may be problem dependent.
 * 
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 307 $
 *            $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
public class MonteCarloSearch implements InterfaceOptimizer, java.io.Serializable {
    /**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -751760624411490405L;
	// These variables are necessary for the simple testcase
    private InterfaceOptimizationProblem    m_Problem               = new B1Problem();
    private int                             m_MultiRuns             = 100;
    private int                             m_FitnessCalls          = 100;
    private int                             m_FitnessCallsNeeded    = 0;
    private Population                      m_Population;
    private GAIndividualBinaryData          m_Best, m_Test;

    // These variables are necessary for the more complex LectureGUI enviroment
    transient private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    public MonteCarloSearch() {
        this.m_Population = new Population();
        this.m_Population.setTargetSize(50);
    }

    public MonteCarloSearch(MonteCarloSearch a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
    }

    public Object clone() {
        return (Object) new MonteCarloSearch(this);
    }

    /** This method will init the MonteCarloSearch
     */
    public void init() {
        this.m_Problem.initPopulation(this.m_Population);
        this.m_Problem.evaluate(this.m_Population);
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population)pop.clone();
        if (reset) {
        	this.m_Population.init();
            this.m_Problem.evaluate(this.m_Population);
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }
    }

    /** 
     * This method will optimize without specific operators, by just calling the individual method
     * for initialization.
     */
    public void optimize() {
        Population original = (Population)this.m_Population.clone();

//        this.m_Problem.initPopulation(this.m_Population);
        for (int i=0; i<m_Population.size(); i++) {
        	m_Population.getEAIndividual(i).defaultInit(null);
        }
        
        this.m_Population.SetFunctionCalls(original.getFunctionCalls());
        this.m_Problem.evaluate(this.m_Population);
        for (int i = 0; i < this.m_Population.size(); i++) {
            if (((AbstractEAIndividual)original.get(i)).isDominatingDebConstraints(((AbstractEAIndividual)this.m_Population.get(i)))) {
                this.m_Population.remove(i);
                this.m_Population.add(i, original.get(i));
            }
        }
        this.m_Population.incrGeneration();
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
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

    /** This method will init the HillClimber
     */
    public void defaultInit() {
        this.m_FitnessCallsNeeded = 0;
        this.m_Best = new GAIndividualBinaryData();
        this.m_Best.defaultInit(m_Problem);
    }

    /** This method will optimize
     */
    public void defaultOptimize() {
        for (int i = 0; i < m_FitnessCalls; i++) {
            this.m_Test = new GAIndividualBinaryData();
            this.m_Test.defaultInit(m_Problem);
            if (this.m_Test.defaultEvaulateAsMiniBits() < this.m_Best.defaultEvaulateAsMiniBits()) this.m_Best = this.m_Test;
            this.m_FitnessCallsNeeded = i;
            if (this.m_Best.defaultEvaulateAsMiniBits() == 0) i = this.m_FitnessCalls +1;
        }
    }

    /** This main method will start a simple hillclimber.
     * No arguments necessary.
     * @param args
     */
    public static void main(String[] args) {
        MonteCarloSearch program = new MonteCarloSearch();
        int TmpMeanCalls = 0, TmpMeanFitness = 0;
        for (int i = 0; i < program.m_MultiRuns; i++) {
            program.defaultInit();
            program.defaultOptimize();
            TmpMeanCalls += program.m_FitnessCallsNeeded;
            TmpMeanFitness += program.m_Best.defaultEvaulateAsMiniBits();
        }
        TmpMeanCalls = TmpMeanCalls/program.m_MultiRuns;
        TmpMeanFitness = TmpMeanFitness/program.m_MultiRuns;
        System.out.println("("+program.m_MultiRuns+"/"+program.m_FitnessCalls+") Mean Fitness : " + TmpMeanFitness + " Mean Calls needed: " + TmpMeanCalls);
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

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "Monte-Carlo Search:\n";
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
    public static String globalInfo() {
        return "The Monte Carlo Search repeatively creates random individuals and stores the best individuals found.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "MCS";
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
        return "Change the number of best individuals stored.";
    }

    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
    }
}