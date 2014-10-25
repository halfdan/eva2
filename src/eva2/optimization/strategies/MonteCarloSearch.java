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
 * The simple random or Monte-Carlo search, simple but useful to evaluate the
 * complexity of the search space. This implements a Random Walk Search using
 * the initialization method of the problem instance, meaning that the random
 * characteristics may be problem dependent.
 */
@Description("The Monte Carlo Search repeatively creates random individuals and stores the best individuals found.")
public class MonteCarloSearch extends AbstractOptimizer implements java.io.Serializable {

    private int multiRuns = 100;
    private int fitnessCalls = 100;
    private int fitnessCallsNeeded = 0;
    private GAIndividualBinaryData bestIndividual;

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
        return new MonteCarloSearch(this);
    }

    /**
     * This method will initialize the MonteCarloSearch
     */
    @Override
    public void initialize() {
        this.optimizationProblem.initializePopulation(this.population);
        this.optimizationProblem.evaluate(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will initialize the optimizer with a given population
     *
     * @param pop   The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
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
     * This method will initialize the HillClimber
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
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "MCS";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }
}