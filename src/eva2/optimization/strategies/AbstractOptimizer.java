package eva2.optimization.strategies;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.population.Population;
import eva2.problems.F1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Parameter;

import java.util.ArrayList;

/**
 *
 */
public abstract class AbstractOptimizer implements InterfaceOptimizer {

    @Parameter(name = "Population", description = "Edit the properties of the population used.")
    protected Population population = new Population();

    protected InterfaceOptimizationProblem optimizationProblem = new F1Problem();

    protected ArrayList<InterfacePopulationChangedEventListener> populationChangedEventListeners;

    abstract public Object clone();


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
    public Population getPopulation() {
        return this.population;
    }

    @Override
    public void setPopulation(Population pop) {
        this.population = pop;
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

}
