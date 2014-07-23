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
 * Threshold accepting algorithm simliar strategy as the flood algorithm,
 * similar problems.
 */
@Description("The threshold algorithm uses an declining threshold to accpect new solutions.")
public class ThresholdAlgorithm implements InterfaceOptimizer, java.io.Serializable {
    // These variables are necessary for the simple testcase

    private InterfaceOptimizationProblem optimizationProblem = new B1Problem();
    private int multiRuns = 100;
    private int fitnessCalls = 100;
    private int fitnessCallsNeeded = 0;
    GAIndividualBinaryData bestIndividual, testIndividual;
    public double initialT = 2, currentT;
    public double alpha = 0.9;
    // These variables are necessary for the more complex LectureGUI enviroment
    transient private String indentifier = "";
    transient private InterfacePopulationChangedEventListener populationChangedEventListener;
    private Population population;

    public ThresholdAlgorithm() {
        this.population = new Population();
        this.population.setTargetSize(10);
    }

    public ThresholdAlgorithm(ThresholdAlgorithm a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.initialT = a.initialT;
        this.currentT = a.currentT;
        this.alpha = a.alpha;
    }

    @Override
    public Object clone() {
        return (Object) new ThresholdAlgorithm(this);
    }

    /**
     * This method will init the HillClimber
     */
    @Override
    public void init() {
        this.optimizationProblem.initializePopulation(this.population);
        this.optimizationProblem.evaluate(this.population);
        this.currentT = this.initialT;
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
        this.currentT = this.initialT;
        if (reset) {
            this.population.init();
            this.optimizationProblem.evaluate(this.population);
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    /**
     * This method will optimize
     */
    @Override
    public void optimize() {
        AbstractEAIndividual indy;
        Population original = (Population) this.population.clone();
        double delta;

        for (int i = 0; i < this.population.size(); i++) {
            indy = ((AbstractEAIndividual) this.population.get(i));
            double tmpD = indy.getMutationProbability();
            indy.setMutationProbability(1.0);
            indy.mutate();
            indy.setMutationProbability(tmpD);
        }
        this.optimizationProblem.evaluate(this.population);
        for (int i = 0; i < this.population.size(); i++) {
            delta = this.calculateDelta(((AbstractEAIndividual) original.get(i)), ((AbstractEAIndividual) this.population.get(i)));
            if (delta < this.currentT) {
                this.population.remove(i);
                this.population.add(i, original.get(i));
            }
        }
        this.currentT = this.alpha * this.currentT;
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
        ThresholdAlgorithm program = new ThresholdAlgorithm();
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
        if (this.population.size() > 1) {
            result += "Multi(" + this.population.size() + ")-Start Hill Climbing:\n";
        } else {
            result += "Threshold Algorithm:\n";
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
        this.indentifier = name;
    }

    @Override
    public String getIdentifier() {
        return this.indentifier;
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "MS-TA";
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
        return "Change the number of best individuals stored (MS-TA).";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * Set the initial threshold
     *
     * @return The initial temperature.
     */
    public double getInitialT() {
        return this.initialT;
    }

    public void setInitialT(double pop) {
        this.initialT = pop;
    }

    public String initialTTipText() {
        return "Set the initial threshold.";
    }

    /**
     * Set alpha, which is used to degrade the threshold
     *
     * @return The initial temperature.
     */
    public double getAlpha() {
        return this.alpha;
    }

    public void setAlpha(double a) {
        this.alpha = a;
        if (this.alpha > 1) {
            this.alpha = 1.0;
        }
    }

    public String alphaTipText() {
        return "Set alpha, which is used to degrade the threshold.";
    }
}