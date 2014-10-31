package eva2.optimization.strategies;

import eva2.optimization.population.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.mutation.InterfaceAdaptOperatorGenerational;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectRandom;
import eva2.optimization.operator.selection.SelectXProbRouletteWheel;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

import java.util.Vector;

/**
 * The traditional genetic algorithms as devised by Holland. To only special
 * here it the plague factor which reduces the population size to tune from a
 * global to a more local search. But you have to be careful with that else the
 * GA might not converge. This is a implementation of Genetic Algorithms.
 */
@Description(value = "This is a basic generational Genetic Algorithm.")
public class GeneticAlgorithm extends AbstractOptimizer implements java.io.Serializable {

    private InterfaceSelection parentSelection = new SelectXProbRouletteWheel();
    private InterfaceSelection partnerSelection = new SelectRandom();
    private boolean useElitism = true;
    private int plague = 0;
    private int numberOfPartners = 1;
    transient private Vector<InterfacePopulationChangedEventListener> changeListener;

    public GeneticAlgorithm() {
    }

    public GeneticAlgorithm(GeneticAlgorithm ga) {
        this.population = (Population) ga.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) ga.optimizationProblem.clone();
        this.plague = ga.plague;
        this.numberOfPartners = ga.numberOfPartners;
        this.useElitism = ga.useElitism;
        this.parentSelection = (InterfaceSelection) ga.parentSelection.clone();
        this.partnerSelection = (InterfaceSelection) ga.partnerSelection.clone();
    }

    @Override
    public Object clone() {
        return new GeneticAlgorithm(this);
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
     * @param reset If true the population is reset.
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        this.population = (Population) pop.clone();
        if (reset) {
            this.optimizationProblem.initializePopulation(population);
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

    /**
     * This method will generate the offspring population from the given
     * population of evaluated individuals.
     */
    private Population generateChildren() {
        Population result = population.cloneWithoutInds();
        Population parents;
        AbstractEAIndividual[] offSprings;
        AbstractEAIndividual tmpIndy;

        this.parentSelection.prepareSelection(this.population);
        this.partnerSelection.prepareSelection(this.population);
        parents = this.parentSelection.selectFrom(this.population, this.population.getTargetSize());

        if (parents.getEAIndividual(0).getMutationOperator() instanceof InterfaceAdaptOperatorGenerational) {
            ((InterfaceAdaptOperatorGenerational) parents.getEAIndividual(0).getMutationOperator()).adaptAfterSelection(population, parents);
        }
        if (parents.getEAIndividual(0).getCrossoverOperator() instanceof InterfaceAdaptOperatorGenerational) {
            ((InterfaceAdaptOperatorGenerational) parents.getEAIndividual(0).getCrossoverOperator()).adaptAfterSelection(population, parents);
        }

        for (int i = 0; i < parents.size(); i++) {
            tmpIndy = ((AbstractEAIndividual) parents.get(i));
            if (tmpIndy == null) {
                System.out.println("Individual null " + i + " Population size: " + parents.size());
            }
            if (this.population == null) {
                System.out.println("population null " + i);
            }

            // ToDo: tmpIndy can be null. We shouldn't call a method on null..
            offSprings = tmpIndy.mateWith(this.partnerSelection.findPartnerFor(tmpIndy, this.population, this.numberOfPartners));
            offSprings[0].mutate();
            result.add(i, offSprings[0]);
        }
        this.evaluatePopulation(result);

        if (parents.getEAIndividual(0).getMutationOperator() instanceof InterfaceAdaptOperatorGenerational) {
            ((InterfaceAdaptOperatorGenerational) parents.getEAIndividual(0).getMutationOperator()).adaptGenerational(population, parents, result, true);
        }
        if (parents.getEAIndividual(0).getCrossoverOperator() instanceof InterfaceAdaptOperatorGenerational) {
            ((InterfaceAdaptOperatorGenerational) parents.getEAIndividual(0).getCrossoverOperator()).adaptGenerational(population, parents, result, true);
        }
        return result;
    }

    @Override
    public void optimize() {
        Population nextGeneration;
        nextGeneration = this.generateChildren();

        if (this.useElitism) {
            AbstractEAIndividual elite = this.population.getBestEAIndividual();
            if (elite != null) {
                this.population = nextGeneration;
                this.population.remove(0);// This implements a random replacement strategy for the elite
                this.population.add(elite);
            }
        } else {
            this.population = nextGeneration;
        }
        if (this.plague > 0) {
            for (int i = 0; i < this.plague; i++) {
                if (this.population.size() > 2) {
                    this.population.remove(this.population.getWorstEAIndividual());
                }
            }
            this.population.setTargetSize(this.population.size());
        }
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method allows you to add the LectureGUI as listener to the Optimizer
     *
     * @param ea
     */
    @Override
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        if (this.changeListener == null) {
            this.changeListener = new Vector<>();
        }
        this.changeListener.add(ea);
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        return changeListener != null && changeListener.removeElement(ea);
    }

    /**
     * Something has changed
     *
     * @param name
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.changeListener != null) {
            for (int i = 0; i < this.changeListener.size(); i++) {
                this.changeListener.get(i).registerPopulationStateChanged(this, name);
            }
        }
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.optimizationProblem;
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
        result += "Genetic Algorithm:\n";
        result += "Using:\n";
        result += " Population Size    = " + this.population.getTargetSize() + "/" + this.population.size() + "\n";
        result += " Parent Selection   = " + this.parentSelection.getClass().toString() + "\n";
        result += " Partner Selection  = " + this.partnerSelection.getClass().toString() + "\n";
        result += " Number of Partners = " + this.numberOfPartners + "\n";
        result += " Elitism            = " + this.useElitism + "\n";
        result += "=> The Optimization Problem: ";
        result += this.optimizationProblem.getStringRepresentationForProblem(this) + "\n";
        return result;
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "Genetic Algorithm";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * Choose a parent selection method.
     *
     * @param selection
     */
    public void setParentSelection(InterfaceSelection selection) {
        this.parentSelection = selection;
    }

    public InterfaceSelection getParentSelection() {
        return this.parentSelection;
    }

    public String parentSelectionTipText() {
        return "Choose a parent selection method.";
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

    /**
     * Choose a selection method for selecting recombination partners for given
     * parents.
     *
     * @param selection
     */
    public void setPartnerSelection(InterfaceSelection selection) {
        this.partnerSelection = selection;
    }

    public InterfaceSelection getPartnerSelection() {
        return this.partnerSelection;
    }

    public String partnerSelectionTipText() {
        return "Choose a selection method for selecting recombination partners for given parents.";
    }
}
