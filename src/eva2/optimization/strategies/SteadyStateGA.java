package eva2.optimization.strategies;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.GAIndividualBinaryData;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectTournament;
import eva2.optimization.operator.selection.replacement.InterfaceReplacement;
import eva2.optimization.operator.selection.replacement.ReplaceWorst;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.B1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

/**
 * A simple implementation of the steady-state GA with variable replacement
 * schemes. To reduce the logging effort population.size() optimization steps
 * are performed each time optimize() is called.
 */
@Description("This is a Steady-State Genetic Algorithm.")
public class SteadyStateGA extends AbstractOptimizer implements java.io.Serializable {

    private InterfaceOptimizationProblem optimizationProblem = new B1Problem();
    private InterfaceSelection parentSelection = new SelectTournament();
    private InterfaceSelection partnerSelection = new SelectTournament();
    private InterfaceReplacement replacementSelection = new ReplaceWorst();
    private int numberOfPartners = 1;

    public SteadyStateGA() {
    }

    public SteadyStateGA(SteadyStateGA a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
        this.numberOfPartners = a.numberOfPartners;
        this.parentSelection = (InterfaceSelection) a.parentSelection.clone();
        this.partnerSelection = (InterfaceSelection) a.partnerSelection.clone();
        this.replacementSelection = (InterfaceReplacement) a.replacementSelection.clone();
    }

    @Override
    public Object clone() {
        return new SteadyStateGA(this);
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
     * This method will assign fitness values to all individual in the current
     * population.
     *
     * @param population The population that is to be evaluated
     */
    private void defaultEvaluatePopulation(Population population) {
        GAIndividualBinaryData tmpIndy;
        for (int i = 0; i < population.size(); i++) {
            tmpIndy = (GAIndividualBinaryData) population.get(i);
            tmpIndy.SetFitness(0, tmpIndy.defaultEvaulateAsMiniBits());
            population.incrFunctionCalls();
        }
        population.incrGeneration();
    }

    /**
     * This method will generate the offspring population from the given
     * population of evaluated individuals.
     */
    private void generateChildren() {
        this.parentSelection.prepareSelection(this.population);
        this.partnerSelection.prepareSelection(this.population);
        Population parents = this.parentSelection.selectFrom(this.population, 1);
        AbstractEAIndividual mother = (AbstractEAIndividual) parents.get(0);
        parents = this.partnerSelection.findPartnerFor(mother, this.population, this.numberOfPartners);
        AbstractEAIndividual[] offSprings = mother.mateWith(parents);
        offSprings[0].mutate();
        this.optimizationProblem.evaluate(offSprings[0]);
        this.replacementSelection.insertIndividual(offSprings[0], this.population, parents);
    }

    @Override
    public void optimize() {
        for (int i = 0; i < this.population.size(); i++) {
            this.generateChildren();
        }
        this.population.incrFunctionCallsBy(this.population.size());
        this.population.incrGeneration();
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
        result += "Genetic Algorithm:\n";
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
        return "SS-GA";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * This method will set the parent selection method that is to be used
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
     * This method will set the number of partners that are needed to create
     * offsprings by mating
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
     * parents
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

    /**
     * Choose a replacement strategy
     *
     * @param selection
     */
    public void setReplacementSelection(InterfaceReplacement selection) {
        this.replacementSelection = selection;
    }

    public InterfaceReplacement getReplacementSelection() {
        return this.replacementSelection;
    }

    public String replacementSelectionTipText() {
        return "Choose a replacement strategy.";
    }
}
