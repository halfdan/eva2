package eva2.optimization.strategies;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.GAIndividualBinaryData;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.optimization.problems.B1Problem;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

/**
 * The simple random or Monte-Carlo search, simple but useful to evaluate the
 * complexity of the search space. This implements a Random Walk Search using
 * the initialization method of the problem instance, meaning that the random
 * characteristics may be problem dependent.
 */
@Description("The Monte Carlo Search repeatively creates random individuals and stores the best individuals found.")
public class MonteCarloSearch implements InterfaceOptimizer, java.io.Serializable {

    /**
     * Generated serial version id.
     */
    private static final long serialVersionUID = -751760624411490405L;
    // These variables are necessary for the simple testcase
    private InterfaceOptimizationProblem optimizationProblem = new B1Problem();
    private int multiRuns = 100;
    private int fitnessCalls = 100;
    private int fitnessCallsNeeded = 0;
    private Population population;
    private GAIndividualBinaryData bestIndividual;
    // These variables are necessary for the more complex LectureGUI enviroment
    transient private String identifier = "";
    transient private InterfacePopulationChangedEventListener populationChangedEventListener;

    public MonteCarloSearch() {
        this.population = new Population();
        this.population.setTargetSize(50);
    }

    public MonteCarloSearch(MonteCarloSearch a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
    }

    @Override
    public Object clone() {
        return (Object) new MonteCarloSearch(this);
    }

    /**
     * This method will init the MonteCarloSearch
     */
    @Override
    public void init() {
        this.optimizationProblem.initializePopulation(this.population);
        this.optimizationProblem.evaluate(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will init the optimizer with a given population
     *
     * @param pop   The initial population
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
    }

    /**
     * This method will optimize without specific operators, by just calling the
     * individual method for initialization.
     */
    @Override
    public void optimize() {
        Population original = (Population) this.population.clone();

//        this.problem.initializePopulation(this.population);
        for (int i = 0; i < population.size(); i++) {
            population.getEAIndividual(i).defaultInit(null);
        }

        this.population.setFunctionCalls(original.getFunctionCalls());
        this.optimizationProblem.evaluate(this.population);
        for (int i = 0; i < this.population.size(); i++) {
            if (((AbstractEAIndividual) original.get(i)).isDominatingDebConstraints(((AbstractEAIndividual) this.population.get(i)))) {
                this.population.remove(i);
                this.population.add(i, original.get(i));
            }
        }
        this.population.incrGeneration();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
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
        GAIndividualBinaryData testIndividial;
        for (int i = 0; i < fitnessCalls; i++) {
            testIndividial = new GAIndividualBinaryData();
            testIndividial.defaultInit(optimizationProblem);
            if (testIndividial.defaultEvaulateAsMiniBits() < this.bestIndividual.defaultEvaulateAsMiniBits()) {
                this.bestIndividual = testIndividial;
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
        MonteCarloSearch program = new MonteCarloSearch();
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
        this.populationChangedEventListener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (populationChangedEventListener == ea) {
            populationChangedEventListener = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Something has changed
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.populationChangedEventListener != null) {
            this.populationChangedEventListener.registerPopulationStateChanged(this, name);
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
        result += "Monte-Carlo Search:\n";
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
        return "MCS";
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
        return "Change the number of best individuals stored.";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }
}