package eva2.optimization.strategies;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectBestIndividuals;
import eva2.optimization.population.*;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.B1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;
import eva2.util.annotation.Hidden;

import java.util.logging.Logger;

/**
 * Population based incremental learning in the PSM by Monmarche version with
 * also allows to simulate ant systems due to the flexible update rule of V. But
 * both are limited to binary genotypes. This is a eva2.problems.simple implementation of
 * Population Based Incremental Learning.
 * <p>
 * Nicolas Monmarché , Eric Ramat , Guillaume Dromel , Mohamed Slimane , Gilles
 * Venturini: On the similarities between AS, BSC and PBIL: toward the birth of
 * a new meta-heuristic. TecReport 215. Univ. de Tours, 1999.
 */
@Description("The Population based incremental learning is based on a statistical distribution of bit positions. Please note: This optimizer requires a binary genotype!")
public class PopulationBasedIncrementalLearning extends AbstractOptimizer implements java.io.Serializable {

    private final static Logger LOGGER = Logger.getLogger(PopulationBasedIncrementalLearning.class.getName());
    // These variables are necessary for the eva2.problems.simple testcase
    private InterfaceOptimizationProblem optimizationProblem = new B1Problem();
    private boolean useElitism = true;
    private InterfaceSelection selectionOperator = new SelectBestIndividuals();
    transient private String identifier = "";
    transient private InterfacePopulationChangedEventListener populationChangedEventListener;
    private Population population = new PBILPopulation();
    private double learningRate = 0.04;
    private double mutationRate = 0.5;
    private double mutateSigma = 0.01;
    private int numberOfPositiveSamples = 1;
    private double[] initialProbabilities = ((PBILPopulation) population).getProbabilityVector();

    public PopulationBasedIncrementalLearning() {
    }

    public PopulationBasedIncrementalLearning(PopulationBasedIncrementalLearning a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.learningRate = a.learningRate;
        this.mutationRate = a.mutationRate;
        this.mutateSigma = a.mutateSigma;
        this.numberOfPositiveSamples = a.numberOfPositiveSamples;
        this.useElitism = a.useElitism;
        this.selectionOperator = (InterfaceSelection) a.selectionOperator.clone();
    }

    @Override
    public Object clone() {
        return new PopulationBasedIncrementalLearning(this);
    }

    @Override
    public void initialize() {
        this.optimizationProblem.initializePopulation(this.population);
        if ((initialProbabilities != null) && (initialProbabilities.length == ((PBILPopulation) population).getProbabilityVector().length)) {
            ((PBILPopulation) population).setProbabilityVector(initialProbabilities);
        } else {
            if (initialProbabilities != null) {
                System.err.println("Warning: initial probability vector doesnt match in length!");
            }
        }
        this.evaluatePopulation(this.population);
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
        if (!(pop.getEAIndividual(0) instanceof InterfaceGAIndividual)) {
            System.err.println("Error: PBIL only works with GAIndividuals!");
        }
        this.population = new PBILPopulation();
        this.population.addPopulation((Population) pop.clone());
        if (reset) {
            this.population.initialize();
            this.evaluatePopulation(this.population);
        }
        ((PBILPopulation) this.population).buildProbabilityVector();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will evaluate the current population using the given problem.
     *
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
        this.optimizationProblem.evaluate(population);
        population.incrGeneration();
    }

    /**
     * This method will generate the offspring population from the given
     * population of evaluated individuals.
     */
    private Population generateChildren() {
        PBILPopulation result = (PBILPopulation) this.population.clone();
        Population examples;


        this.selectionOperator.prepareSelection(this.population);
        examples = this.selectionOperator.selectFrom(this.population, this.numberOfPositiveSamples);
        result.learnFrom(examples, this.learningRate);
        result.mutateProbabilityVector(this.mutationRate, this.mutateSigma);
        result.initPBIL();
        return result;
    }

    @Override
    public void optimize() {
        Population nextGeneration;
        AbstractEAIndividual elite;

        nextGeneration = this.generateChildren();
        this.evaluatePopulation(nextGeneration);
        if (this.useElitism) {
            elite = this.population.getBestEAIndividual();
            this.population = nextGeneration;
            this.population.add(0, elite);
        } else {
            this.population = nextGeneration;
        }
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will set the problem that is to be optimized
     *
     * @param problem
     */
    @Override
    @Hidden
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.optimizationProblem = problem;
        if (optimizationProblem instanceof AbstractOptimizationProblem) {
            if (!(((AbstractOptimizationProblem) optimizationProblem).getIndividualTemplate() instanceof InterfaceGAIndividual)) {
                LOGGER.warning("PBIL only works with GAIndividuals!");
            }
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
        result += "Population Based Incremental Learning:\n";
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
        return "PBIL";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * This method will set the selection method that is to be used
     *
     * @param selection
     */
    public void setSelectionMethod(InterfaceSelection selection) {
        this.selectionOperator = selection;
    }

    public InterfaceSelection getSelectionMethod() {
        return this.selectionOperator;
    }

    public String selectionMethodTipText() {
        return "Choose a selection method.";
    }

    /**
     * This method will set the problem that is to be optimized
     *
     * @param elitism
     */
    public void setElitism(boolean elitism) {
        this.useElitism = elitism;
    }

    public boolean getElitism() {
        return this.useElitism;
    }

    public String elitismTipText() {
        return "Enable/disable elitism.";
    }

    /**
     * This method will set the learning rate for PBIL
     *
     * @param LearningRate
     */
    public void setLearningRate(double LearningRate) {
        this.learningRate = LearningRate;
        if (this.learningRate < 0) {
            this.learningRate = 0;
        }
    }

    public double getLearningRate() {
        return this.learningRate;
    }

    public String learningRateTipText() {
        return "The learing rate of PBIL.";
    }

    /**
     * This method will set the mutation rate for PBIL
     *
     * @param m
     */
    public void setMutationRate(double m) {
        this.mutationRate = m;
        if (this.mutationRate < 0) {
            this.mutationRate = 0;
        }
        if (this.mutationRate > 1) {
            this.mutationRate = 1;
        }
    }

    public double getMutationRate() {
        return this.mutationRate;
    }

    public String mutationRateTipText() {
        return "The mutation rate of PBIL.";
    }

    /**
     * This method will set the mutation sigma for PBIL
     *
     * @param m
     */
    public void setMutateSigma(double m) {
        this.mutateSigma = m;
        if (this.mutateSigma < 0) {
            this.mutateSigma = 0;
        }
    }

    public double getMutateSigma() {
        return this.mutateSigma;
    }

    public String mutateSigmaTipText() {
        return "Set the sigma for the mutation of the probability vector.";
    }

    /**
     * This method will set the number of positive samples for PBIL
     *
     * @param PositiveSamples
     */
    public void setPositiveSamples(int PositiveSamples) {
        this.numberOfPositiveSamples = PositiveSamples;
        if (this.numberOfPositiveSamples < 1) {
            this.numberOfPositiveSamples = 1;
        }
    }

    public int getPositiveSamples() {
        return this.numberOfPositiveSamples;
    }

    public String positiveSamplesTipText() {
        return "The number of positive samples that update the PBIL vector.";
    }

    public double[] getInitialProbabilities() {
        return initialProbabilities;
    }

    public void setInitialProbabilities(double[] probabilities) {
        initialProbabilities = probabilities;
    }
}