package eva2.optimization.strategies;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

/**
 *
 */
@Description(text = "Artificial Bee Colony Optimizer")
public class ArtificialBeeColony implements InterfaceOptimizer {



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
    public void init() {

    }

    @Override
    public void initByPopulation(Population pop, boolean reset) {

    }

    @Override
    public void optimize() {

    }

    @Override
    public Population getPopulation() {
        return null;
    }

    @Override
    public void setPopulation(Population pop) {

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

    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {

    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return null;
    }

    @Override
    public String getStringRepresentation() {
        return this.toString();
    }
}
