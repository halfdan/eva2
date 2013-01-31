package eva2.server.go.strategies;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.mutation.InterfaceAdaptOperatorGenerational;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.selection.SelectBestIndividuals;
import eva2.server.go.operators.selection.SelectRandom;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/**
 * Evolution strategies by Rechenberg and Schwefel, but please remember that
 * this only gives the generation strategy and not the coding. But this is the
 * only stategy that is able to utilize the 1/5 success rule mutation.
 * Unfortunately, there is a minor problem with the interpretation of the
 * population size in constrast to the parameters mu and lambda used by
 * Rechenberg and Schwefel. Therefore, i'm afraid that the interpretation of the
 * population size may be subject to future changes. This is a implementation of
 * Evolution Strategies. Copyright: Copyright (c) 2003 Company: University of
 * Tuebingen, Computer Architecture
 *
 * @author Felix Streichert
 * @version: $Revision: 307 $ $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec
 * 2007) $ $Author: mkron $
 */
public class EvolutionStrategies implements InterfaceOptimizer, java.io.Serializable {

    protected int mu = 5;
    protected int lambda = 20;
    protected boolean usePlusStrategy = false;
    protected Population population = new Population();
    protected InterfaceOptimizationProblem optimizationProblem = new B1Problem();
    private InterfaceSelection parentSelection = new SelectRandom();
    private InterfaceSelection partnerSelection = new SelectRandom();
    private InterfaceSelection environmentSelection = new SelectBestIndividuals();
    private int numberOfPartners = 1;
    protected int origPopSize = -1; // especially for CBN
    private boolean forceOrigPopSize = true;// especially for CBN
    transient private String identifier = "";
    transient private InterfacePopulationChangedEventListener changeListener;
    public static final String esMuParam = "EvolutionStrategyMuParameter";
    public static final String esLambdaParam = "EvolutionStrategyLambdaParameter";

    public EvolutionStrategies() {
        this.population.setTargetSize(this.lambda);
    }

    public EvolutionStrategies(int mu, int lambda, boolean usePlus) {
        setMu(mu);
        setLambda(lambda);
        setPlusStrategy(usePlus);
        this.checkPopulationConstraints();
    }

    public EvolutionStrategies(EvolutionStrategies evStrategies) {
        this.mu = evStrategies.mu;
        this.lambda = evStrategies.lambda;
        this.usePlusStrategy = evStrategies.usePlusStrategy;
        this.population = (Population) evStrategies.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) evStrategies.optimizationProblem.clone();
        this.numberOfPartners = evStrategies.numberOfPartners;
        this.parentSelection = (InterfaceSelection) evStrategies.parentSelection.clone();
        this.partnerSelection = (InterfaceSelection) evStrategies.partnerSelection.clone();
        this.environmentSelection = (InterfaceSelection) evStrategies.environmentSelection.clone();
        this.numberOfPartners = evStrategies.numberOfPartners;
        this.origPopSize = evStrategies.origPopSize;
        this.forceOrigPopSize = evStrategies.forceOrigPopSize;
    }

    /**
     * Set to true in CBN, false for any extension which changes the population
     * size during optimization.
     *
     * @param force
     */
    public void setForceOrigPopSize(boolean force) {
        forceOrigPopSize = force;
    }

    public void hideHideable() {
//    	GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
    }

    @Override
    public Object clone() {
        return (Object) new EvolutionStrategies(this);
    }

    @Override
    public void init() {
        checkPopulationConstraints();
        population.putData(esMuParam, getMu());
        population.putData(esLambdaParam, getLambda());
        this.optimizationProblem.initPopulation(this.population);
        this.evaluatePopulation(this.population);
    }

    /**
     * This method will init the optimizer with a given population
     *
     * @param pop The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initByPopulation(Population pop, boolean reset) {
        origPopSize = pop.getTargetSize();
        this.population = (Population) pop.clone();
        if (reset) {
            this.population.init();
            this.evaluatePopulation(this.population);
        }
    }

    /**
     * This method will evaluate the current population using the given problem.
     *
     * @param population The population that is to be evaluated
     */
    protected void evaluatePopulation(Population population) {
        this.optimizationProblem.evaluate(population);
        population.incrGeneration();
    }

    /**
     * This method will generate the offspring population from the given
     * population of evaluated individuals.
     */
    protected Population generateEvalChildren(Population fromPopulation) {
        Population result = population.cloneWithoutInds(), parents;

        result.clear();

        parents = generateChildren(fromPopulation, result, this.lambda);

        this.evaluatePopulation(result);

        if (result.getEAIndividual(0).getMutationOperator() instanceof InterfaceAdaptOperatorGenerational) {
            // this seems to be the right moment for the 1/5-success rule
            // parents and result have the same size and correspond per individual        	
            ((InterfaceAdaptOperatorGenerational) parents.getEAIndividual(0).getMutationOperator()).adaptGenerational(fromPopulation, parents, result, usePlusStrategy);
        }
        if (parents.getEAIndividual(0).getCrossoverOperator() instanceof InterfaceAdaptOperatorGenerational) {
            ((InterfaceAdaptOperatorGenerational) parents.getEAIndividual(0).getCrossoverOperator()).adaptGenerational(fromPopulation, parents, result, usePlusStrategy);
        }
        return result;
    }

    /**
     * Create a new population by parent selection, partner selection,
     * recombination and crossover. The new population is added to the result
     * population, while the selected parent population (after selection but
     * before recombination/crossover) is returned. Returned parents and result
     * population are to have the same size and correspond per individual.
     *
     * @param fromPopulation
     * @param result
     * @param lambda
     * @return
     */
    protected Population generateChildren(Population fromPopulation, Population result, int lambda) {
        AbstractEAIndividual tmpIndy;
        AbstractEAIndividual[] offSprings;
        Population parents;
        this.parentSelection.prepareSelection(fromPopulation);
        this.partnerSelection.prepareSelection(fromPopulation);
        parents = this.parentSelection.selectFrom(fromPopulation, lambda);

        for (int i = 0; i < parents.size(); i++) {
            tmpIndy = (AbstractEAIndividual) parents.get(i);
            offSprings = tmpIndy.mateWith(this.partnerSelection.findPartnerFor(tmpIndy, fromPopulation, this.numberOfPartners));
            offSprings[0].mutate();
            result.add(i, offSprings[0]);
        }
        return parents;
    }

    protected Population selectParents(Population fromPop, int mu) {
        this.environmentSelection.prepareSelection(fromPop);
        return this.environmentSelection.selectFrom(fromPop, mu);
    }

    /**
     * The optimize method will compute an improved and evaluated population.
     */
    @Override
    public void optimize() {
        Population nextGeneration, parents;

        // first perform the environment selection to select myu parents
        parents = selectParents(population, this.mu);

        // population / parents are of sizes lambda / mu 
        if (parents.getEAIndividual(0).getMutationOperator() instanceof InterfaceAdaptOperatorGenerational) {
            ((InterfaceAdaptOperatorGenerational) parents.getEAIndividual(0).getMutationOperator()).adaptAfterSelection(getPopulation(), parents);
        }
        if (parents.getEAIndividual(0).getCrossoverOperator() instanceof InterfaceAdaptOperatorGenerational) {
            ((InterfaceAdaptOperatorGenerational) parents.getEAIndividual(0).getCrossoverOperator()).adaptAfterSelection(getPopulation(), parents);
        }

        // now generate the lambda offsprings
        // create lambda new ones from mu parents
        nextGeneration = this.generateEvalChildren(parents);

        if (this.isPlusStrategy()) {
            nextGeneration.addPopulation(parents);
        }

        setPop(getReplacePop(nextGeneration));

        // necessary here because evalPop was not called on population
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /**
     * Usually, this just returns the given population. However, in case of CBN
     * this method prepares the next generation according to the species size.
     *
     * @param nextGeneration
     * @return
     */
    protected Population getReplacePop(Population nextGeneration) {
        if (forceOrigPopSize && (origPopSize > 0) && (origPopSize < nextGeneration.size())) {
            // this is especially for CBN: earlier selection to immediately reduce the size of mu+lambda to lambda
            this.environmentSelection.prepareSelection(nextGeneration);
            Population tmpPop = (Population) nextGeneration.clone();
            nextGeneration.clear();
            nextGeneration.addPopulation(this.environmentSelection.selectFrom(tmpPop, origPopSize));
        } else {
            if ((origPopSize > 0) && (origPopSize != nextGeneration.size())) {
                System.err.println("Warning in ES! orig: " + origPopSize + " / " + nextGeneration.size());
            }
        }
        return nextGeneration;
    }

    /**
     * This method allows you to add the LectureGUI as listener to the Optimizer
     *
     * @param ea
     */
    @Override
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.changeListener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        if (changeListener == ea) {
            changeListener = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Something has changed
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.changeListener != null) {
            this.changeListener.registerPopulationStateChanged(this, name);
        }
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

    /**
     * This method will return a string describing all properties of the
     * optimizer and the applied methods.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        result += "Evolution Strategies:\n";
        result += "Optimization Problem: ";
        result += this.optimizationProblem.getStringRepresentationForProblem(this) + "\n";
        result += this.population.getStringRepresentation();
        return result;
    }

    /**
     * This method allows you to set myu and lambda in one step
     *
     * @param myu
     * @param lambda
     * @param plus True if plus, false if comma strategy
     */
    public void setGenerationStrategy(int myu, int lambda, boolean plus) {
        this.mu = myu;
        this.lambda = lambda;
        this.usePlusStrategy = plus;
        this.checkPopulationConstraints();
    }

    /**
     * This method will check the population constraints myu <= lambda and will
     * calculate the population size accordingly.
     */
    protected void checkPopulationConstraints() {
        if (this.lambda < this.mu) {
            System.err.println("Invalid mu/lambda ratio! Setting mu=lambda=" + mu);
            this.lambda = this.mu;
        }
        this.population.setTargetSize(this.lambda);
        origPopSize = population.getTargetSize();
    }

    /**
     * This method allows you to set an identifier for the algorithm
     *
     * @param name The indenifier
     */
    @Override
    public void setIdentifier(String name) {
        this.identifier = name;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * These are for GUI
     */
    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is an Evolution Strategy. Note that the population size depends on mu (number of parents) and lambda (number of offspring).";
    }

    public String[] customPropertyOrder() {
        return new String[]{"mu", "lambda"};
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "(" + getMu() + (isPlusStrategy() ? "+" : ",") + getLambda() + ")-ES";
    }

    /**
     * Assuming that all optimizer will store their data in a population we will
     * allow access to this population to query to current state of the
     * optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    @Override
    public Population getPopulation() {
        return this.population;
    }

    // for internal usage
    protected void setPop(Population pop) {
        this.population = pop;
    }

    @Override
    public void setPopulation(Population pop) {
        origPopSize = pop.size();
        this.population = pop;
    }

    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * This method will set the selection method that is to be used
     *
     * @param selection
     */
    public void setEnvironmentSelection(InterfaceSelection selection) {
        this.environmentSelection = selection;
    }

    public InterfaceSelection getEnvironmentSelection() {
        return this.environmentSelection;
    }

    public String environmentSelectionTipText() {
        return "Choose a method for selecting the reduced population.";
    }

    /**
     * This method will set the selection method that is to be used to select
     * the partners
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
        return "Choose a selection method for selecting recombination partners.";
    }

    /**
     * This method will set the selection method that is to be used to select
     * the parents
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
        return "Choose a selection method for selecting parents.";
    }

    /**
     * This method will toggle between plus and comma selection strategy
     *
     * @param elitism
     */
    public void setPlusStrategy(boolean elitism) {
        this.usePlusStrategy = elitism;
    }

    public boolean isPlusStrategy() {
        return this.usePlusStrategy;
    }

    public String plusStrategyTipText() {
        return "Select between plus and comma strategy.";
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
     * This method allows you to set parent population size myu
     *
     * @param myu The parent population size.
     */
    public void setMu(int mu) {
        this.mu = mu;
    }

    public int getMu() {
        return this.mu;
    }

    public String muTipText() {
        return "This is the parent population size.";
    }

    /**
     * This is the children population size lambda
     *
     * @param lambda The children population size.
     */
    public void setLambda(int lambda) {
        this.lambda = lambda;
    }

    public int getLambda() {
        return this.lambda;
    }

    public String lambdaTipText() {
        return "This is the children population size.";
    }
}
