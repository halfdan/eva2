package eva2.optimization.strategies;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.GAIndividualBinaryData;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * Simulated Annealing by Nelder and Mead, a eva2.problems.simple yet efficient local search
 * method. But to become less prone to premature convergence the cooling rate
 * has to be tuned to the optimization problem at hand. Again the population
 * size gives the number of multi-starts.
 */
@Description("The simulated annealing uses an additional cooling rate instead of a simple dominate criteria to accept worse solutions by chance.")
public class SimulatedAnnealing extends AbstractOptimizer implements java.io.Serializable {
    private int multiRuns = 100;
    private int fitnessCalls = 100;
    private int fitnessCallsNeeded = 0;
    GAIndividualBinaryData bestIndividual, testIndividual;
    public double initialTemperature = 2, currentTemperature;
    public double alpha = 0.9;

    public SimulatedAnnealing() {
        this.population = new Population();
        this.population.setTargetSize(10);
    }

    public SimulatedAnnealing(SimulatedAnnealing a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.initialTemperature = a.initialTemperature;
        this.currentTemperature = a.currentTemperature;
        this.alpha = a.alpha;
    }

    @Override
    public Object clone() {
        return new SimulatedAnnealing(this);
    }

    /**
     * This method will initialize the HillClimber
     */
    @Override
    public void initialize() {
        this.optimizationProblem.initializePopulation(this.population);
        this.optimizationProblem.evaluate(this.population);
        this.currentTemperature = this.initialTemperature;
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
        this.currentTemperature = this.initialTemperature;
        if (reset) {
            this.population.initialize();
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
            indy = this.population.get(i);
            double tmpD = indy.getMutationProbability();
            indy.setMutationProbability(1.0);
            indy.mutate();
            indy.setMutationProbability(tmpD);
        }
        this.optimizationProblem.evaluate(this.population);
        for (int i = 0; i < this.population.size(); i++) {
            if (original.get(i).isDominatingDebConstraints(this.population.get(i))) {
                this.population.remove(i);
                this.population.add(i, original.get(i));
            } else {
                delta = this.calculateDelta(original.get(i), this.population.get(i));
                //System.out.println("delta: " + delta);
                if (Math.exp(-delta / this.currentTemperature) > RNG.randomInt(0, 1)) {
                    this.population.remove(i);
                    this.population.add(i, original.get(i));
                }
            }
        }
        this.currentTemperature = this.alpha * this.currentTemperature;
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
     * This main method will start a eva2.problems.simple hillclimber. No arguments necessary.
     *
     * @param args
     */
    public static void main(String[] args) {
        SimulatedAnnealing program = new SimulatedAnnealing();
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
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "MS-SA";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * Set the initial temperature
     *
     * @return The initial temperature.
     */
    public double getInitialTemperature() {
        return this.initialTemperature;
    }

    public void setInitialTemperature(double pop) {
        this.initialTemperature = pop;
    }

    public String initialTemperatureTipText() {
        return "Set the initial temperature.";
    }

    /**
     * Set alpha, which is used to degrade the temperaure
     *
     * @return The cooling rate.
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
        return "Set alpha, which is used to degrade the temperaure.";
    }
}