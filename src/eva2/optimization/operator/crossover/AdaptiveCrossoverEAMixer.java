package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.mutation.InterfaceAdaptOperatorGenerational;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;

/**
 * A modified version of the CrossoverEAMixer that adapts the weights with which the crossover-methods are chosen
 *
 * @author Alex
 */
public class AdaptiveCrossoverEAMixer extends CrossoverEAMixer implements InterfaceAdaptOperatorGenerational {

    private Population pop = new Population();
    private boolean initialized = false;
    private double lastFitness = Double.MAX_VALUE;
    private int[] used;
    private InterfaceOptimizationProblem opt;

    public AdaptiveCrossoverEAMixer() {
        super();
    }

    public AdaptiveCrossoverEAMixer(AdaptiveCrossoverEAMixer mutator) {
        super(mutator);
        this.pop = (Population) mutator.pop.clone(); // TODO !Make a deep copy!?
        this.initialized = mutator.initialized;
        this.lastFitness = mutator.lastFitness;
        this.used = mutator.used;
        this.opt = mutator.opt;
    }

    /**
     * Create a mutation mixer with equal weights of the given mutation operators.
     *
     * @param mutators
     */
    public AdaptiveCrossoverEAMixer(InterfaceCrossover... crossovers) {
        this.crossoverMixer = new PropertyCrossoverMixer(crossovers);
        this.crossoverMixer.selectedTargets = crossoverMixer.availableTargets.clone();
    }

    @Override
    protected void maybeAdaptWeights(AbstractEAIndividual[] indies) {
        if (initialized) {
            AbstractEAIndividual indy = indies[0];
            this.opt.evaluate(indy);
            this.pop.incrFunctionCalls();
            if (indy.getFitness(0) < this.lastFitness) {
                this.lastFitness = indy.getFitness(0);
                this.used[lastOperatorIndex] += 1;
                int sum = 0;
                for (int i = 0; i < this.used.length; i++) {
                    sum += used[i];
                }
                double[] weights = new double[used.length];
                for (int i = 0; i < weights.length; i++) {
                    weights[i] = ((double) used[i]) / sum;
                }
                getCrossovers().setWeights(weights);
            }
        } else {
            System.err.println("not yet initialized");
        }
    }

    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt, Population pop, double fit) {
        InterfaceCrossover[] mutators = this.crossoverMixer.getSelectedCrossers();
        for (int i = 0; i < mutators.length; i++) {
            mutators[i].init(individual, opt);
        }
        this.pop = pop;
        this.lastFitness = fit;
        this.used = new int[getCrossovers().getWeights().length];
        for (int i = 0; i < this.used.length; i++) {
            this.used[i] = 1;
        }
        this.opt = opt;
        this.initialized = true;
    }

    public void update(AbstractEAIndividual individual, InterfaceOptimizationProblem opt, Population pop, double fit) {
        InterfaceCrossover[] mutators = this.crossoverMixer.getSelectedCrossers();
        for (int i = 0; i < mutators.length; i++) {
            mutators[i].init(individual, opt);
        }
        this.pop = pop;
        this.lastFitness = fit;
        this.opt = opt;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public void adaptAfterSelection(Population oldPop, Population selectedPop) {
        // Nothing to to here

    }

    @Override
    public void adaptGenerational(Population oldPop, Population selectedPop,
                                  Population newPop, boolean updateSelected) {
        // TODO Perform adaption here by checking how often individuals in newPop have used which operator
        // look at CrossoverEAMixer.CROSSOVER_EA_MIXER_OPERATOR_KEY or AbstractOptimizationProblem.OLD_FITNESS_KEY using AbstractEAIndividual.getData(KEY);
        System.out.println("In adaptGenerational");
    }
}
