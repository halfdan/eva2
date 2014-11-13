package eva2.optimization.strategies;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.B1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

/**
 * This is a Multi-Start Hill-Climber, here the population size gives the number
 * of multi-starts. Similar to the evolutionary programming strategy this
 * strategy sets the mutation rate temporarily to 1.0.
 */
@Description("The Hill Climber uses the default EA mutation and initializing operators. If the population size is bigger than one a multi-start Hill Climber is performed.")
public class HillClimbing extends AbstractOptimizer implements java.io.Serializable {
    // These variables are necessary for the simple testcase

    private InterfaceOptimizationProblem optimizationProblem = new B1Problem();
    private InterfaceMutation mutator = null;

    public HillClimbing() {
        this.population = new Population();
        this.population.setTargetSize(10);
    }

    public HillClimbing(HillClimbing a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
    }

    @Override
    public Object clone() {
        return new HillClimbing(this);
    }

    /**
     * This method will initialize the HillClimber
     */
    @Override
    public void initialize() {
        this.optimizationProblem.initializePopulation(this.population);
        this.optimizationProblem.evaluate(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        this.population = (Population) pop.clone();
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
        double tmpD;
        InterfaceMutation tmpMut;

        for (int i = 0; i < this.population.size(); i++) {
            indy = this.population.get(i);
            tmpD = indy.getMutationProbability();
            indy.setMutationProbability(1.0);
            if (mutator == null) {
                indy.mutate();
            } else {
                mutator.mutate(indy);
            }
            indy.setMutationProbability(tmpD);
        }
        this.optimizationProblem.evaluate(this.population);
        for (int i = 0; i < this.population.size(); i++) {
            if (original.get(i).isDominatingDebConstraints(this.population.get(i))) {
                // throw away mutated one and replace by old one 
                this.population.set(i, original.get(i));
            } else {
                // else: mutation improved the individual, so leave the new one
            }
        }
        this.population.incrGeneration();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    public InterfaceMutation getMutationOperator() {
        return mutator;
    }

    /**
     * Allows to set a desired mutator by hand, which is used instead of the one
     * in the individuals. Set it to null to use the one in the individuals,
     * which is the default.
     *
     * @param mute
     */
    public void setMutationOperator(InterfaceMutation mute) {
        mutator = mute;
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
            result += "Hill Climbing:\n";
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
        return "MS-HC";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }
}