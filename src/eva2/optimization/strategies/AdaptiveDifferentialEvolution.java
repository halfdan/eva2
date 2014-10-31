package eva2.optimization.strategies;

import eva2.optimization.population.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.F1Problem;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.util.ArrayList;

/**
 * This is an implementation of Differential Evolution with two-level parameter adaption published by
 * Yu, Shen, Chen, et.al. in 2014 (IEEE Transaction on Cybernetics, DOI: 10.1109/TCYB.2013.2279211).
 */
@Description("Differential Evolution with Two-Level Parameter Adaption (Yu, Shen, Chen, et.al., 2014")
public class AdaptiveDifferentialEvolution extends AbstractOptimizer {
    protected Population population;

    @Parameter(name = "groups", description = "Number of sub-groups to use during optimization.")
    protected int nonOverlappingGroups = 5;

    @Parameter(name = "F", description = "Differential Weight")
    protected double differentialWeight = 0.8;

    @Parameter(name = "CR", description = "Crossover Rate")
    protected double crossoverRate = 0.6;


    protected transient Population children = null;
    protected AbstractOptimizationProblem optimizationProblem = new F1Problem();

    private ArrayList<InterfacePopulationChangedEventListener> populationChangedEventListeners;

    public Object clone() {
        return this;
    }

    @Override
    public String getName() {
        return "Adaptive Differential Evolution";
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
     * This method will evaluate the current population using the given problem.
     *
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
        this.optimizationProblem.evaluate(population);
        population.incrGeneration();
    }

    public AbstractEAIndividual generateNewIndividual(Population population, int index) {
        return null;
    }

    @Override
    public void optimize() {
        AbstractEAIndividual indy, orig;

        /**
         * Initialize the child Population or clear it
         */
        if (children == null) {
            children = new Population(population.size());
        } else {
            children.clear();
        }

        /**
         * Cycle through the whole population and generate a new individual
         */
        for (int i = 0; i < this.population.size(); i++) {
            indy = generateNewIndividual(population, i);
            children.add(indy);
        }

        children.setGeneration(population.getGeneration());
        optimizationProblem.evaluate(children);


        for (int i = 0; i < this.population.size(); i++) {
            /**
             * Compare each individual in the child population with the corresponding
             * one from the parent population.
             */
            indy = (AbstractEAIndividual) this.children.get(i);
            orig = (AbstractEAIndividual) this.population.get(i);
            if (indy.isDominatingDebConstraints(orig)) {
                this.population.replaceIndividualAt(i, indy);
            }
        }

        // Estimate the optimization state

        //

        this.population.incrGeneration();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
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

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return null;
    }

    @Override
    public String getStringRepresentation() {
        return null;
    }
}
