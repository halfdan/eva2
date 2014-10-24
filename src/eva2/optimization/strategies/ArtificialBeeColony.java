package eva2.optimization.strategies;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.F1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

import java.util.ArrayList;

/**
 *
 */
@Description(value = "Artificial Bee Colony Optimizer")
public class ArtificialBeeColony implements InterfaceOptimizer {

    protected AbstractOptimizationProblem optimizationProblem = new F1Problem();
    protected Population population;

    private ArrayList<InterfacePopulationChangedEventListener> populationChangedEventListeners;

    public ArtificialBeeColony() {

    }

    public ArtificialBeeColony(ArtificialBeeColony copy) {

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
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        if (populationChangedEventListeners == null) {
            populationChangedEventListeners = new ArrayList<>();
        }
        populationChangedEventListeners.add(ea);
    }

    @Override
    public boolean removePopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        return populationChangedEventListeners != null && populationChangedEventListeners.remove(ea);
    }

    @Override
    public void initialize() {

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
            this.evaluatePopulation(this.population);
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    /**
     * Something has changed
     *
     * @param name Event name
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.populationChangedEventListeners != null) {
            for (InterfacePopulationChangedEventListener listener : this.populationChangedEventListeners) {
                listener.registerPopulationStateChanged(this, name);
            }
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

    @Override
    public void optimize() {
        /**
         *
         */

    }

    @Override
    public Population getPopulation() {
        return this.population;
    }

    @Override
    public void setPopulation(Population pop) {
        this.population = pop;
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return null;
    }

    /**
     * This method will set the problem that is to be optimized
     *
     * @param problem
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.optimizationProblem = (AbstractOptimizationProblem) problem;
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.optimizationProblem;
    }

    @Override
    public String getStringRepresentation() {
        return this.toString();
    }
}
