package eva2.optimization.strategies;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.GAIndividualBinaryData;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.B1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

/**
 * The flood algorithm, and alternative to the threshold algorithms. No really
 * good but commonly known and sometimes even used. Here the problem is to
 * choose the initial flood peak and the drain rate such that it fits the
 * current optimization problem. But again this is a greedy local search
 * strategy. Similar to the evolutionary programming strategy this strategy sets
 * the mutation rate temporarily to 1.0. The algorithm regards only
 * one-dimensional fitness.
 */
@Description("The flood algorithm uses an declining flood peak to accpect new solutions (*shudder* check inital flood peak and drain very carefully!).")
public class FloodAlgorithm implements InterfaceOptimizer, java.io.Serializable {
    // These variables are necessary for the simple testcase

    private InterfaceOptimizationProblem optimizationProblem = new B1Problem();
    private int multiRuns = 100;
    private int fitnessCalls = 100;
    private int fitnessCallsNeeded = 0;
    GAIndividualBinaryData bestIndividual, testIndividual;
    public double initialFloodPeak = 2000.0, currentFloodPeak;
    public double drainRate = 1.0;
    // These variables are necessary for the more complex LectureGUI enviroment
    transient private String identifier = "";
    transient private InterfacePopulationChangedEventListener listener;
    private Population population;

    public FloodAlgorithm() {
        this.population = new Population();
        this.population.setTargetSize(10);
    }

    public FloodAlgorithm(FloodAlgorithm a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.initialFloodPeak = a.initialFloodPeak;
        this.drainRate = a.drainRate;
    }

    @Override
    public Object clone() {
        return (Object) new FloodAlgorithm(this);
    }

    /**
     * This method will init the HillClimber
     */
    @Override
    public void init() {
        this.optimizationProblem.initializePopulation(this.population);
        this.optimizationProblem.evaluate(this.population);
        this.currentFloodPeak = this.initialFloodPeak;
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will init the optimizer with a given population
     *
     * @param reset If true the population is reset.
     */
    @Override
    public void initByPopulation(Population pop, boolean reset) {
        this.population = (Population) pop.clone();
        if (reset) {
            this.population.init();
            this.optimizationProblem.evaluate(this.population);
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
        this.currentFloodPeak = this.initialFloodPeak;
    }

    /**
     * This method will optimize
     */
    @Override
    public void optimize() {
        AbstractEAIndividual indy;
        Population original = (Population) this.population.clone();
        double[] fitness;

        for (int i = 0; i < this.population.size(); i++) {
            indy = ((AbstractEAIndividual) this.population.get(i));
            double tmpD = indy.getMutationProbability();
            indy.setMutationProbability(1.0);
            indy.mutate();
            indy.setMutationProbability(tmpD);
        }
        this.optimizationProblem.evaluate(this.population);
        for (int i = 0; i < this.population.size(); i++) {
            fitness = ((AbstractEAIndividual) this.population.get(i)).getFitness();
            if (fitness[0] > this.currentFloodPeak) {
                this.population.remove(i);
                this.population.add(i, original.get(i));
            }
        }
        this.currentFloodPeak -= this.drainRate;
        this.population.incrGeneration();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method calculates the difference between the fitness values
     *
     * @param org The original
     * @param mut The mutant
     */
    private double calculateDelta(AbstractEAIndividual org, AbstractEAIndividual mut) {
        double result = 0;
        double[] fitOrg, fitMut;
        fitOrg = org.getFitness();
        fitMut = mut.getFitness();
        for (int i = 0; i < fitOrg.length; i++) {
            result += fitOrg[i] - fitMut[i];
        }
        return result;
    }

    /**
     * This method will set the problem that is to be optimized
     *
     * @param problem
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.optimizationProblem = problem;
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.optimizationProblem;
    }

    /**
     * This method will init the HillClimber
     */
    public void defaultInit() {
        this.fitnessCallsNeeded = 0;
        this.bestIndividual = new GAIndividualBinaryData();
        this.bestIndividual.defaultInit(optimizationProblem);
    }

    /**
     * This method will optimize
     */
    public void defaultOptimize() {
        for (int i = 0; i < fitnessCalls; i++) {
            this.testIndividual = (GAIndividualBinaryData) ((this.bestIndividual).clone());
            this.testIndividual.defaultMutate();
            if (this.testIndividual.defaultEvaulateAsMiniBits() < this.bestIndividual.defaultEvaulateAsMiniBits()) {
                this.bestIndividual = this.testIndividual;
            }
            this.fitnessCallsNeeded = i;
            if (this.bestIndividual.defaultEvaulateAsMiniBits() == 0) {
                i = this.fitnessCalls + 1;
            }
        }
    }

    /**
     * This main method will start a simple hillclimber. No arguments necessary.
     *
     * @param args
     */
    public static void main(String[] args) {
        FloodAlgorithm program = new FloodAlgorithm();
        int TmpMeanCalls = 0, TmpMeanFitness = 0;
        for (int i = 0; i < program.multiRuns; i++) {
            program.defaultInit();
            program.defaultOptimize();
            TmpMeanCalls += program.fitnessCallsNeeded;
            TmpMeanFitness += program.bestIndividual.defaultEvaulateAsMiniBits();
        }
        TmpMeanCalls /= program.multiRuns;
        TmpMeanFitness /= program.multiRuns;
        System.out.println("(" + program.multiRuns + "/" + program.fitnessCalls + ") Mean Fitness : " + TmpMeanFitness + " Mean Calls needed: " + TmpMeanCalls);
    }

    /**
     * This method allows you to add the LectureGUI as listener to the Optimizer
     *
     * @param ea
     */
    @Override
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.listener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (listener == ea) {
            listener = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Something has changed
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.listener != null) {
            this.listener.registerPopulationStateChanged(this, name);
        }
    }

    /**
     * This method will return a string describing all properties of the
     * optimizer and the applied methods.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        if (this.population.size() > 1) {
            result += "Multi(" + this.population.size() + ")-Start Hill Climbing:\n";
        } else {
            result += "Simulated Annealing:\n";
        }
        result += "Optimization Problem: ";
        result += this.optimizationProblem.getStringRepresentationForProblem(this) + "\n";
        result += this.population.getStringRepresentation();
        return result;
    }

    /**
     * This method allows you to set an identifier for the algorithm
     *
     * @param name The indenifier
     */
    @Override
    public void setIdentifier(String name) {
        this.identifier = name;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "MS-FA";
    }

    /**
     * Assuming that all optimizer will store thier data in a population we will
     * allow acess to this population to query to current state of the
     * optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    @Override
    public Population getPopulation() {
        return this.population;
    }

    @Override
    public void setPopulation(Population pop) {
        this.population = pop;
    }

    public String populationTipText() {
        return "Change the number of best individuals stored (MS-FA).";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * This methods allow you to set/get the temperatur of the flood algorithm
     * procedure
     *
     * @return The initial flood level.
     */
    public double getInitialFloodPeak() {
        return this.initialFloodPeak;
    }

    public void setInitialFloodPeak(double pop) {
        this.initialFloodPeak = pop;
    }

    public String initialFloodPeakTipText() {
        return "Set the initial flood peak.";
    }

    /**
     * This methods allow you to set/get the drain rate of the flood algorithm
     * procedure
     *
     * @return The drain rate.
     */
    public double getDrainRate() {
        return this.drainRate;
    }

    public void setDrainRate(double a) {
        this.drainRate = a;
        if (this.drainRate < 0) {
            this.drainRate = 0.0;
        }
    }

    public String drainRateTipText() {
        return "Set the drain rate that reduces the current flood level each generation.";
    }
}