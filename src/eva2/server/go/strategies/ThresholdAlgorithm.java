package eva2.server.go.strategies;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/** Threshold accepting algorithm simliar strategy as the flood
 * algorithm, similar problems.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.10.2004
 * Time: 13:35:49
 * To change this template use File | Settings | File Templates.
 */
public class ThresholdAlgorithm implements InterfaceOptimizer, java.io.Serializable {
    // These variables are necessary for the simple testcase
    private InterfaceOptimizationProblem            m_Problem           = new B1Problem();
    private int             m_MultiRuns             = 100;
    private int             m_FitnessCalls          = 100;
    private int             m_FitnessCallsNeeded    = 0;
    GAIndividualBinaryData  m_Best, m_Test;
    public double           m_InitialT   = 2, m_CurrentT;
    public double           m_Alpha         = 0.9;

    // These variables are necessary for the more complex LectureGUI enviroment
    transient private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;
    private Population      m_Population;

    public ThresholdAlgorithm() {
        this.m_Population = new Population();
        this.m_Population.setTargetSize(10);
    }

    public ThresholdAlgorithm(ThresholdAlgorithm a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_InitialT                     = a.m_InitialT;
        this.m_CurrentT                     = a.m_CurrentT;
        this.m_Alpha                        = a.m_Alpha;
    }

    @Override
    public Object clone() {
        return (Object) new ThresholdAlgorithm(this);
    }

    /** This method will init the HillClimber
     */
    @Override
    public void init() {
        this.m_Problem.initPopulation(this.m_Population);
        this.m_Problem.evaluate(this.m_Population);
        this.m_CurrentT = this.m_InitialT;
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    @Override
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population)pop.clone();
        this.m_CurrentT = this.m_InitialT;
        if (reset) {
        	this.m_Population.init();
            this.m_Problem.evaluate(this.m_Population);
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }
    }

    /** This method will optimize
     */
    @Override
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
            delta = this.calculateDelta(((AbstractEAIndividual)original.get(i)), ((AbstractEAIndividual)this.m_Population.get(i)));
            if (delta < this.m_CurrentT) {
                this.m_Population.remove(i);
                this.m_Population.add(i, original.get(i));
            }
        }
        this.m_CurrentT = this.m_Alpha * this.m_CurrentT;
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
            if (this.m_Test.defaultEvaulateAsMiniBits() < this.m_Best.defaultEvaulateAsMiniBits()) {
                this.m_Best = this.m_Test;
            }
            this.m_FitnessCallsNeeded = i;
            if (this.m_Best.defaultEvaulateAsMiniBits() == 0) {
                i = this.m_FitnessCalls +1;
            }
        }
    }

    /** This main method will start a simple hillclimber.
     * No arguments necessary.
     * @param args
     */
    public static void main(String[] args) {
        ThresholdAlgorithm program = new ThresholdAlgorithm();
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
		} else {
                                return false;
                            }
	}
    protected void firePropertyChangedEvent (String name) {
        if (this.m_Listener != null) {
            this.m_Listener.registerPopulationStateChanged(this, name);
        }
    }

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        if (this.m_Population.size() > 1) {
            result += "Multi(" + this.m_Population.size() + ")-Start Hill Climbing:\n";
        }
        else {
            result += "Threshold Algorithm:\n";
        }
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
        return "The threshold algorithm uses an declining threshold to accpect new solutions.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "MS-TA";
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
        return "Change the number of best individuals stored (MS-TA).";
    }
    
    @Override
    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
    }
    /** Set the initial threshold
     * @return The initial temperature.
     */
    public double getInitialT() {
        return this.m_InitialT;
    }
    public void setInitialT(double pop){
        this.m_InitialT = pop;
    }
    public String initialTTipText() {
        return "Set the initial threshold.";
    }

    /** Set alpha, which is used to degrade the threshold
     * @return The initial temperature.
     */
    public double getAlpha() {
        return this.m_Alpha;
    }
    public void setAlpha(double a){
        this.m_Alpha = a;
        if (this.m_Alpha > 1) {
            this.m_Alpha = 1.0;
        }
    }
    public String alphaTipText() {
        return "Set alpha, which is used to degrade the threshold.";
    }
}