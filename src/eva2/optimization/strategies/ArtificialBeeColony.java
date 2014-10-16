package eva2.optimization.strategies;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.F1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

/**
 *
 */
@Description(value = "Artificial Bee Colony Optimizer")
public class ArtificialBeeColony implements InterfaceOptimizer {

    protected AbstractOptimizationProblem optimizationProblem = new F1Problem();
    protected Population population;

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

    }

    @Override
    public boolean removePopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        return false;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void initializeByPopulation(Population pop, boolean reset) {

    }

    @Override
    public void optimize() {

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

    @Override
    public void setIdentifier(String name) {

    }

    @Override
    public String getIdentifier() {
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
