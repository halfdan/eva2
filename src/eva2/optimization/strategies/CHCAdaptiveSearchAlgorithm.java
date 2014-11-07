package eva2.optimization.strategies;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectBestSingle;
import eva2.optimization.operator.selection.SelectRandom;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

import java.util.BitSet;

/**
 * This is an implementation of the CHC Adaptive Search Algorithm by Eshelman.
 * It is limited to binary data and is based on massively disruptive crossover.
 * I'm not sure whether i've implemented this correctly, but i definitely wasn't
 * able to make it competitive to a standard GA.. *sigh* This is a
 * implementation of the CHC Adaptive Search Algorithm (Cross generational
 * elitist selection, Heterogeneous recombination and Cataclysmic mutation).
 *
 * ToDo: Check implementation for correctness.
 */
@eva2.util.annotation.Description("This is an implementation of the CHC Adaptive Search Algorithm by Eselman.")
public class CHCAdaptiveSearchAlgorithm extends AbstractOptimizer implements java.io.Serializable {

    private double initialDifferenceThreshold = 0.25;
    private int differenceThreshold;
    private double divergenceRate = 0.35;
    private boolean useElitism = true;
    private int numberOfPartners = 1;
    private InterfaceSelection recombSelectionOperator = new SelectRandom();
    private InterfaceSelection populationSelectionOperator = new SelectBestSingle();

    public CHCAdaptiveSearchAlgorithm() {
    }

    public CHCAdaptiveSearchAlgorithm(CHCAdaptiveSearchAlgorithm a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.initialDifferenceThreshold = a.initialDifferenceThreshold;
        this.differenceThreshold = a.differenceThreshold;
        this.divergenceRate = a.divergenceRate;
        this.numberOfPartners = a.numberOfPartners;
        this.useElitism = a.useElitism;
        this.recombSelectionOperator = (InterfaceSelection) a.recombSelectionOperator.clone();
        this.populationSelectionOperator = (InterfaceSelection) a.populationSelectionOperator.clone();
    }

    @Override
    public Object clone() {
        return new CHCAdaptiveSearchAlgorithm(this);
    }

    @Override
    public void initialize() {
        this.optimizationProblem.initializePopulation(this.population);
        AbstractEAIndividual tmpIndy = ((AbstractEAIndividual) (this.population.get(0)));
        if (tmpIndy instanceof InterfaceGAIndividual) {
            this.differenceThreshold = (int) (((InterfaceGAIndividual) tmpIndy).getGenotypeLength() * this.initialDifferenceThreshold);
        } else {
            System.out.println("Problem does not apply InterfaceGAIndividual, which is the only individual type valid for CHC!");
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
        this.population = (Population) pop.clone();
        if (reset) {
            this.population.initialize();
        }
        AbstractEAIndividual tmpIndy = ((AbstractEAIndividual) (this.population.get(0)));
        if (tmpIndy instanceof InterfaceGAIndividual) {
            this.differenceThreshold = (int) (((InterfaceGAIndividual) tmpIndy).getGenotypeLength() * this.initialDifferenceThreshold);
        } else {
            System.out.println("Problem does not apply InterfaceGAIndividual, which is the only individual type valid for CHC!");
        }

        if (reset) {
            this.evaluatePopulation(this.population);
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
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
        Population result = this.population.cloneWithoutInds(), parents, partners;
        AbstractEAIndividual[] offSprings;
        AbstractEAIndividual tmpIndy;

        result.clear();
        this.populationSelectionOperator.prepareSelection(this.population);
        this.recombSelectionOperator.prepareSelection(this.population);
        parents = this.populationSelectionOperator.selectFrom(this.population, this.population.getTargetSize());
        //System.out.println("Parents:"+parents.getSolutionRepresentationFor());

        for (int i = 0; i < parents.size(); i++) {
            tmpIndy = ((AbstractEAIndividual) parents.get(i));
            if (tmpIndy == null) {
                System.out.println("Individual null " + i);
            }
            if (this.population == null) {
                System.out.println("population null " + i);
            }

            partners = this.recombSelectionOperator.findPartnerFor(tmpIndy, this.population, this.numberOfPartners);
            if (this.computeHammingDistance(tmpIndy, partners) > this.differenceThreshold) {
                offSprings = tmpIndy.mateWith(partners);
                for (int j = 0; j < offSprings.length; j++) {
                    offSprings[j].mutate();
                }
                result.add(offSprings[0]);
            }
        }
        return result;
    }

    /**
     * This method computes the Hamming Distance between n-Individuals
     *
     * @param dad
     * @param partners
     * @return The maximal Hamming Distance between dad and the partners
     */
    private int computeHammingDistance(AbstractEAIndividual dad, Population partners) {
        int result = 0, tmpDist;
        BitSet tmpB1, tmpB2;

        tmpB1 = ((InterfaceGAIndividual) dad).getBGenotype();
        for (int i = 0; i < partners.size(); i++) {
            tmpB2 = ((InterfaceGAIndividual) partners.get(i)).getBGenotype();
            tmpDist = 0;
            for (int j = 0; j < ((InterfaceGAIndividual) dad).getGenotypeLength(); j++) {
                if (tmpB1.get(j) == tmpB2.get(j)) {
                    tmpDist++;
                }
            }
            result = Math.max(result, tmpDist);
        }
        return result;
    }

    /**
     * This method method replaces the current population with copies of the
     * current best individual but all but one are randomized with a very high
     * mutation rate.
     */
    private void diverge() {
        AbstractEAIndividual best = this.population.getBestEAIndividual();
        InterfaceGAIndividual mutant;
        BitSet tmpBitSet;

        this.population.clear();
        this.population.add(best);
        for (int i = 1; i < this.population.getTargetSize(); i++) {
            mutant = (InterfaceGAIndividual) best.clone();
            tmpBitSet = mutant.getBGenotype();
            for (int j = 0; j < mutant.getGenotypeLength(); j++) {
                if (RNG.flipCoin(this.divergenceRate)) {
                    if (tmpBitSet.get(j)) {
                        tmpBitSet.clear(j);
                    } else {
                        tmpBitSet.set(j);
                    }
                }
            }
            mutant.setBGenotype(tmpBitSet);
            this.population.add((AbstractEAIndividual) mutant);
        }
        if (best instanceof InterfaceGAIndividual) {
            this.differenceThreshold = (int) (this.divergenceRate * (1 - this.divergenceRate) * ((InterfaceGAIndividual) best).getGenotypeLength());

        }
        this.evaluatePopulation(this.population);
    }

    @Override
    public void optimize() {
        Population nextGeneration, tmp;
        //AbstractEAIndividual   elite;

        if (this.differenceThreshold < 0) {
            this.diverge();
        } else {
            nextGeneration = this.generateChildren();
            if (nextGeneration.size() == 0) {
                this.differenceThreshold--;
            } else {
                this.evaluatePopulation(nextGeneration);
                if (nextGeneration.getWorstEAIndividual().getFitness(0) > this.population.getBestEAIndividual().getFitness(0)) {
                    this.differenceThreshold--;
                }
            }
            nextGeneration.addPopulation(this.population);
            this.populationSelectionOperator.prepareSelection(this.population);
            tmp = this.populationSelectionOperator.selectFrom(nextGeneration, this.population.getTargetSize());
            nextGeneration.clear();
            nextGeneration.addPopulation(tmp);
            this.population = nextGeneration;
        }
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
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
        result += "CHC Adaptive Search Algorithm:\n";
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
        return "CHC";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }


    /**
     * Enable/disable elitism.
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
     * The number of mating partners needed to create offsprings.
     *
     * @param partners
     */
    public void setNumberOfPartners(int partners) {
        if (partners < 0) {
            partners = 0;
        }
        this.numberOfPartners = partners;
    }

    public int getNumberOfPartners() {
        return this.numberOfPartners;
    }

    public String numberOfPartnersTipText() {
        return "The number of mating partners needed to create offsprings.";
    }
}
