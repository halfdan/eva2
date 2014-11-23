package eva2.optimization.strategies;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.io.Serializable;

/**
 * Artificial Bee Colony
 *
 * This optimizer implements the original ABC algorithm proposed by Karaboga et.al.
 */
@Description("Artificial Bee Colony")
public class ArtificialBeeColony extends AbstractOptimizer implements Serializable {

    /**
     * A food source which could not be improved through "maxTrials" trials is abandoned by its employed bee.
     */
    protected int maxTrials = 100;

    protected AbstractEAIndividual bestIndividual;


    public ArtificialBeeColony() {

    }

    public ArtificialBeeColony(ArtificialBeeColony copy) {
        this.population = (Population) copy.population.clone();
        this.maxTrials = copy.maxTrials;
    }

    @Override
    public Object clone() {
        return new ArtificialBeeColony(this);
    }

    @Override
    public String getName() {
        return "Artificial Bee Colony";
    }

    @Override
    public void initialize() {
        this.optimizationProblem.initializePopulation(this.population);
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
            this.evaluatePopulation(this.population);
            this.bestIndividual = this.population.getBestEAIndividual();
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

    protected double getFitnessProportion(AbstractEAIndividual indy) {
        double fitness = indy.getFitness(0);
        if (fitness >= 0) {
            fitness = 1.0 / (1.0 + fitness);
        } else {
            fitness = 1.0 + Math.abs(fitness);
        }
        return fitness;
    }

    @Override
    public void optimize() {
        /**
         * Sending employed bees
         */
        sendEmployedBees();

        this.population.incrFunctionCallsBy(this.population.size());

        /**
         * Send onlooker bees to food sources based on fitness proportional probability
         */
        sendOnlookerBees();

        /**
         * Remember best Individual
         */
        if (bestIndividual != null && bestIndividual.getFitness(0) < this.population.getBestEAIndividual().getFitness(0)) {
            bestIndividual = this.population.getBestEAIndividual();
        } else {
            bestIndividual = this.population.getBestEAIndividual();
        }

        /**
         * Send scout bee
         */
        sendScoutBees();

        /**
         * ToDo: This is ugly.
         *
         * incrGeneration increments the age of all indies. Age management however happens
         * in the algorithm itself (for ABC) so we have to -1 all ages.
         */
        this.population.incrGeneration();
        for (Object individual : this.population) {
            ((AbstractEAIndividual) individual).setAge(((AbstractEAIndividual) individual).getAge() - 1);
        }

        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    protected void sendScoutBees() {
        AbstractEAIndividual oldestIndy = getOldestIndividual();
        if (oldestIndy.getAge() > this.maxTrials) {
            oldestIndy.initialize(this.optimizationProblem);
            this.optimizationProblem.evaluate(oldestIndy);
            this.population.incrFunctionCalls();
        }
    }

    protected void sendOnlookerBees() {
        int t = 0, i = 0;
        double sumFitness = 0.0;
        for (AbstractEAIndividual individual : this.population) {
            sumFitness += getFitnessProportion(individual);
        }
        while (t < this.population.size()) {
            double r = RNG.randomDouble();

            /**
             * Choose a food source depending on its probability to be chosen. The probability
             * is proportional to the
             */
            double pI = getFitnessProportion(this.population.getEAIndividual(i))/sumFitness;
            if (r < pI) {
                t++;

                // The current individual to compare to
                AbstractEAIndividual indy = this.population.getEAIndividual(i);

                // The new individual which we are generating
                AbstractEAIndividual newIndividual = (AbstractEAIndividual) indy.getClone();
                double[] indyDoubleData = ((InterfaceDataTypeDouble) newIndividual).getDoubleData();

                int randomParam = RNG.randomInt(0, indyDoubleData.length - 1);
                int neighbour = RNG.randomIntWithout(i, 0, this.population.size() - 1);
                double[] randomIndy = ((InterfaceDataTypeDouble) this.population.get(neighbour)).getDoubleData();

                double phi = RNG.randomDouble(-1.0, 1.0);
                indyDoubleData[randomParam] += (indyDoubleData[randomParam] - randomIndy[randomParam]) * phi;
                // Make sure new indy is in range
                Mathematics.projectToRange(indyDoubleData, ((InterfaceDataTypeDouble) newIndividual).getDoubleRange());

                ((InterfaceDataTypeDouble) newIndividual).setDoubleGenotype(indyDoubleData);
                this.optimizationProblem.evaluate(newIndividual);
                this.population.incrFunctionCalls();

                if (newIndividual.getFitness(0) < indy.getFitness(0)) {
                    newIndividual.setAge(1);
                    this.population.replaceIndividualAt(i, newIndividual);
                } else {
                    // Keep individual but increase the age
                    indy.incrAge();
                }
            }

            i++;
            if (i == this.population.size()) {
                i = 0;
            }
        }
    }

    protected void sendEmployedBees() {
        for(int i = 0; i < this.population.size(); i++) {
            // The current individual to compare to
            AbstractEAIndividual indy = this.population.getEAIndividual(i);

            // The new individual which we are generating
            AbstractEAIndividual newIndividual = (AbstractEAIndividual) indy.getClone();
            double[] indyDoubleData = ((InterfaceDataTypeDouble) newIndividual).getDoubleData();

            int neighbour = RNG.randomIntWithout(i, 0, this.population.size() - 1);
            double[] randomIndy = ((InterfaceDataTypeDouble) this.population.get(neighbour)).getDoubleData();

            int randomParam = RNG.randomInt(0, indyDoubleData.length - 1);
            double r = RNG.randomDouble(-1.0, 1.0);
            indyDoubleData[randomParam] += (indyDoubleData[randomParam] - randomIndy[randomParam]) * r;
            // Make sure new indy is in range
            Mathematics.projectToRange(indyDoubleData, ((InterfaceDataTypeDouble) newIndividual).getDoubleRange());

            ((InterfaceDataTypeDouble) newIndividual).setDoubleGenotype(indyDoubleData);
            this.optimizationProblem.evaluate(newIndividual);

            if (newIndividual.getFitness(0) < indy.getFitness(0)) {
                newIndividual.setAge(0);
                this.population.replaceIndividualAt(i, newIndividual);
            } else {
                // Keep individual but increase the age
                indy.incrAge();
            }
        }
    }

    private AbstractEAIndividual getOldestIndividual() {
        AbstractEAIndividual oldestIndy = this.population.getEAIndividual(0);
        for(int i = 1; i < this.population.size(); i++) {
            if (oldestIndy.getAge() > this.population.getEAIndividual(i).getAge()) {
                oldestIndy = this.population.getEAIndividual(i);
            }
        }
        return oldestIndy;
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        /**
         * ToDo: This should somehow preserve the best found individual.
         */
        return new SolutionSet(this.population);
    }

    @Override
    public String getStringRepresentation() {
        return this.toString();
    }


    @Override
    public void setPopulation(Population pop) {
        this.population = pop;
    }

    @Parameter(name = "trials", description = "Maximum number of trials until bee abandons the food source")
    public void setMaxTrials(int maxTrials) {
        this.maxTrials = maxTrials;
    }

    public int getMaxTrials() {
        return maxTrials;
    }
}
