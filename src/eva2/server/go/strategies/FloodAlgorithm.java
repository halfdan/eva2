package eva2.server.go.strategies;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/** The flood algorithm, and alternative to the threshold algorithms. No really
 * good but commonly known and sometimes even used. Here the problem is to choose
 * the initial flood peak and the drain rate such that it fits the current optimization
 * problem. But again this is a greedy local search strategy. Similar to the
 * evolutionary programming strategy this strategy sets the mutation rate temporarily
 * to 1.0.
 * The algorithm regards only one-dimensional fitness.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.10.2004
 * Time: 13:46:02
 * To change this template use File | Settings | File Templates.
 */
public class FloodAlgorithm implements InterfaceOptimizer, java.io.Serializable {
    // These variables are necessary for the simple testcase
    private InterfaceOptimizationProblem            m_Problem           = new B1Problem();
    private int             m_MultiRuns             = 100;
    private int             m_FitnessCalls          = 100;
    private int             m_FitnessCallsNeeded    = 0;
    GAIndividualBinaryData  m_Best, m_Test;
    public double           m_InitialFloodPeak      = 2000.0, m_CurrentFloodPeak;
    public double           m_DrainRate             = 1.0;

    // These variables are necessary for the more complex LectureGUI enviroment
    transient private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;
    private Population      m_Population;

    public FloodAlgorithm() {
        this.m_Population = new Population();
        this.m_Population.setTargetSize(10);
    }

    public FloodAlgorithm(FloodAlgorithm a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_InitialFloodPeak             = a.m_InitialFloodPeak;
        this.m_DrainRate                    = a.m_DrainRate;
    }

    @Override
    public Object clone() {
        return (Object) new FloodAlgorithm(this);
    }

    /** This method will init the HillClimber
     */
    @Override
    public void init() {
        this.m_Problem.initPopulation(this.m_Population);
        this.m_Problem.evaluate(this.m_Population);
        this.m_CurrentFloodPeak = this.m_InitialFloodPeak;
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /** This method will init the optimizer with a given population
     * @param reset     If true the population is reset.
     */
    @Override
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population)pop.clone();
        if (reset) {
        	this.m_Population.init();
            this.m_Problem.evaluate(this.m_Population);
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }
        this.m_CurrentFloodPeak = this.m_InitialFloodPeak;
    }

    /** This method will optimize
     */
    @Override
    public void optimize() {
        AbstractEAIndividual    indy;
        Population              original = (Population)this.m_Population.clone();
        double[]                fitness;

        for (int i = 0; i < this.m_Population.size(); i++) {
            indy = ((AbstractEAIndividual) this.m_Population.get(i));
            double tmpD = indy.getMutationProbability();
            indy.setMutationProbability(1.0);
            indy.mutate();
            indy.setMutationProbability(tmpD);
        }
        this.m_Problem.evaluate(this.m_Population);
        for (int i = 0; i < this.m_Population.size(); i++) {
            fitness = ((AbstractEAIndividual)this.m_Population.get(i)).getFitness();
            if (fitness[0] > this.m_CurrentFloodPeak) {
                this.m_Population.remove(i);
                this.m_Population.add(i, original.get(i));
            }
        }
        this.m_CurrentFloodPeak -= this.m_DrainRate;
        this.m_Population.incrGeneration();
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /** This method calculates the difference between the fitness values
     * @param org     The original
     * @param mut     The mutant
     */
    private double calculateDelta(AbstractEAIndividual org, AbstractEAIndividual mut) {
        double      result = 0;
        double[]    fitOrg, fitMut;
        fitOrg = org.getFitness();
        fitMut = mut.getFitness();
        for (int i = 0; i < fitOrg.length; i++) {
            result += fitOrg[i] - fitMut[i];
        }
        return result;
    }

    /** This method will set the problem that is to be optimized
     * @param problem
     */
    @Override
    public void setProblem (InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
    }
    @Override
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
            this.m_Test = (GAIndividualBinaryData)((this.m_Best).clone());
            this.m_Test.defaultMutate();
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
        FloodAlgorithm program = new FloodAlgorithm();
        int TmpMeanCalls = 0, TmpMeanFitness = 0;
        for (int i = 0; i < program.m_MultiRuns; i++) {
            program.defaultInit();
            program.defaultOptimize();
            TmpMeanCalls += program.m_FitnessCallsNeeded;
            TmpMeanFitness += program.m_Best.defaultEvaulateAsMiniBits();
        }
        TmpMeanCalls /= program.m_MultiRuns;
        TmpMeanFitness /= program.m_MultiRuns;
        System.out.println("("+program.m_MultiRuns+"/"+program.m_FitnessCalls+") Mean Fitness : " + TmpMeanFitness + " Mean Calls needed: " + TmpMeanCalls);
    }

    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    @Override
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }
    @Override
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
    @Override
    public String getStringRepresentation() {
        String result = "";
        if (this.m_Population.size() > 1) result += "Multi(" + this.m_Population.size() + ")-Start Hill Climbing:\n";
        else result += "Simulated Annealing:\n";
        result += "Optimization Problem: ";
        result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
        result += this.m_Population.getStringRepresentation();
        return result;
    }
    /** This method allows you to set an identifier for the algorithm
     * @param name      The indenifier
     */
    @Override
     public void setIdentifier(String name) {
        this.m_Identifier = name;
    }
    @Override
     public String getIdentifier() {
         return this.m_Identifier;
     }

    /** This method is required to free the memory on a RMIServer,
     * but there is nothing to implement.
     */
    @Override
    public void freeWilly() {

    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "The flood algorithm uses an declining flood peak to accpect new solutions (*shudder* check inital flood peak and drain very carefully!).";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "MS-FA";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    @Override
    public Population getPopulation() {
        return this.m_Population;
    }
    @Override
    public void setPopulation(Population pop){
        this.m_Population = pop;
    }
    public String populationTipText() {
        return "Change the number of best individuals stored (MS-FA).";
    }

    
    @Override
    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
    }
    /** This methods allow you to set/get the temperatur of the flood
     * algorithm procedure
     * @return The initial flood level.
     */
    public double getInitialFloodPeak() {
        return this.m_InitialFloodPeak;
    }
    public void setInitialFloodPeak(double pop){
        this.m_InitialFloodPeak = pop;
    }
    public String initialFloodPeakTipText() {
        return "Set the initial flood peak.";
    }

    /** This methods allow you to set/get the drain rate of the flood
     * algorithm procedure
     * @return The drain rate.
     */
    public double getDrainRate() {
        return this.m_DrainRate;
    }
    public void setDrainRate(double a){
        this.m_DrainRate = a;
        if (this.m_DrainRate < 0) this.m_DrainRate = 0.0;
    }
    public String drainRateTipText() {
        return "Set the drain rate that reduces the current flood level each generation.";
    }
}