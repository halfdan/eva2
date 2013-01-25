package eva2.server.go.strategies;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/** Simulated Annealing by Nelder and Mead, a simple yet efficient local search
 * method. But to become less prone to premature convergence the cooling rate
 * has to be tuned to the optimization problem at hand. Again the population size
 * gives the number of multi-starts.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.05.2004
 * Time: 10:30:26
 * To change this template use File | Settings | File Templates.
 */
public class SimulatedAnnealing implements InterfaceOptimizer, java.io.Serializable {
    // These variables are necessary for the simple testcase
    private InterfaceOptimizationProblem            m_Problem           = new B1Problem();
    private int             m_MultiRuns             = 100;
    private int             m_FitnessCalls          = 100;
    private int             m_FitnessCallsNeeded    = 0;
    GAIndividualBinaryData  m_Best, m_Test;
    public double           m_InitialTemperature    = 2, m_CurrentTemperature;
    public double           m_Alpha                 = 0.9;

    // These variables are necessary for the more complex LectureGUI enviroment
    transient private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;
    private Population      m_Population;

    public SimulatedAnnealing() {
        this.m_Population = new Population();
        this.m_Population.setTargetSize(10);
    }
    
    public SimulatedAnnealing(SimulatedAnnealing a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_InitialTemperature           = a.m_InitialTemperature;
        this.m_CurrentTemperature           = a.m_CurrentTemperature;
        this.m_Alpha                        = a.m_Alpha;
    }

    public Object clone() {
        return (Object) new SimulatedAnnealing(this);
    }

    /** This method will init the HillClimber
     */
    public void init() {
        this.m_Problem.initPopulation(this.m_Population);
        this.m_Problem.evaluate(this.m_Population);
        this.m_CurrentTemperature = this.m_InitialTemperature;
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population)pop.clone();
        this.m_CurrentTemperature = this.m_InitialTemperature;
        if (reset) {
        	this.m_Population.init();
            this.m_Problem.evaluate(this.m_Population);
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }
    }

    /** This method will optimize
     */
    public void optimize() {
        AbstractEAIndividual    indy;
        Population              original = (Population)this.m_Population.clone();
        double                  delta;

        for (int i = 0; i < this.m_Population.size(); i++) {
            indy = ((AbstractEAIndividual) this.m_Population.get(i));
            double tmpD = indy.getMutationProbability();
            indy.setMutationProbability(1.0);
            indy.mutate();
            indy.setMutationProbability(tmpD);
        }
        this.m_Problem.evaluate(this.m_Population);
        for (int i = 0; i < this.m_Population.size(); i++) {
            if (((AbstractEAIndividual)original.get(i)).isDominatingDebConstraints(((AbstractEAIndividual)this.m_Population.get(i)))) {
                this.m_Population.remove(i);
                this.m_Population.add(i, original.get(i));
            } else {
                delta = this.calculateDelta(((AbstractEAIndividual)original.get(i)), ((AbstractEAIndividual)this.m_Population.get(i)));
                //System.out.println("delta: " + delta);
                if (Math.exp(-delta/this.m_CurrentTemperature) > RNG.randomInt(0,1)) {
                    this.m_Population.remove(i);
                    this.m_Population.add(i, original.get(i));
                }
            }
        }
        this.m_CurrentTemperature = this.m_Alpha * this.m_CurrentTemperature;
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
    public void setProblem (InterfaceOptimizationProblem problem) {
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
        SimulatedAnnealing program = new SimulatedAnnealing();
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
    protected void firePropertyChangedEvent (String name) {
        if (this.m_Listener != null) this.m_Listener.registerPopulationStateChanged(this, name);
    }

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
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
        return "The simulated annealing uses an additional cooling rate instead of a simple dominate criteria to accpect worse solutions by chance.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "MS-SA";
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
        return "Change the number of best individuals stored (MS-SA)).";
    }
    
    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
    }
    /** Set the initial temperature
     * @return The initial temperature.
     */
    public double getInitialTemperature() {
        return this.m_InitialTemperature;
    }
    public void setInitialTemperature(double pop){
        this.m_InitialTemperature = pop;
    }
    public String initialTemperatureTipText() {
        return "Set the initial temperature.";
    }

    /** Set alpha, which is used to degrade the temperaure
     * @return The cooling rate.
     */
    public double getAlpha() {
        return this.m_Alpha;
    }
    public void setAlpha(double a){
        this.m_Alpha = a;
        if (this.m_Alpha > 1) this.m_Alpha = 1.0;
    }
    public String alphaTipText() {
        return "Set alpha, which is used to degrade the temperaure.";
    }
}