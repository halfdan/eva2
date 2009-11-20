package eva2.server.go.strategies;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.mutation.InterfaceMutationGenerational;
import eva2.server.go.operators.mutation.MutateESSuccessRule;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.selection.SelectBestIndividuals;
import eva2.server.go.operators.selection.SelectRandom;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.B1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;

/** Evolution strategies by Rechenberg and Schwefel, but please remember that
 * this only gives the generation strategy and not the coding. But this is the
 * only stategy that is able to utilize the 1/5 success rule mutation. Unfortunately,
 * there is a minor problem with the interpretation of the population size in constrast
 * to the parameters mu and lambda used by Rechenberg and Schwefel. Therefore, i'm
 * afraid that the interpretation of the population size may be subject to future
 * changes.
 * This is a implementation of Evolution Strategies.
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 307 $
 *            $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public class EvolutionStrategies implements InterfaceOptimizer, java.io.Serializable {

    //private double                          m_MyuRatio                  = 6;
	protected int                             m_Mu                       = 5;
	protected int                             m_Lambda                    = 20;
	protected boolean                         m_UsePlusStrategy           = false;
    protected Population                      m_Population                = new Population();
    protected InterfaceOptimizationProblem    m_Problem                   = new B1Problem();
    private InterfaceSelection              m_ParentSelection           = new SelectRandom();
    private InterfaceSelection              m_PartnerSelection          = new SelectRandom();
    private InterfaceSelection              m_EnvironmentSelection      = new SelectBestIndividuals();
    private int                             m_NumberOfPartners          = 1;
    private int								origPopSize					= -1; // especially for CBN
//    private double[]                        m_FitnessOfParents          = null;
    private boolean						forceOrigPopSize			= true;// especially for CBN

    transient private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;
    public static final String esMuParam = "EvolutionStrategyMuParameter";

    public EvolutionStrategies() {
        this.m_Population.setTargetSize(this.m_Lambda);
    }
    
    public EvolutionStrategies(int mu, int lambda, boolean usePlus) {
    	setMu(mu);
    	setLambda(lambda);
    	setPlusStrategy(usePlus);
    	this.checkPopulationConstraints();
    }
    
    public EvolutionStrategies(EvolutionStrategies a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_Mu                          = a.m_Mu;
        this.m_Lambda                       = a.m_Lambda;
        this.m_UsePlusStrategy              = a.m_UsePlusStrategy;
        this.m_NumberOfPartners             = a.m_NumberOfPartners;
        this.m_ParentSelection              = (InterfaceSelection)a.m_ParentSelection.clone();
        this.m_PartnerSelection             = (InterfaceSelection)a.m_PartnerSelection.clone();
        this.m_EnvironmentSelection         = (InterfaceSelection)a.m_EnvironmentSelection.clone();
    }

    /**
     * Set to true in CBN, false for any extension which changes the population size during optimization.
     * @param force
     */
    public void setForceOrigPopSize(boolean force) {
    	forceOrigPopSize = force;
    }
    
    public void hideHideable() {
    	GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
    }

    public Object clone() {
        return (Object) new EvolutionStrategies(this);
    }

    public void init() {
        // @todo In case of CBN-ES i need to read the population size!?
//        int orgPopSize = this.m_Population.getPopulationSize();
//        if (this.m_InitialPopulationSize > orgPopSize) {
//            this.m_Population.setPopulationSize(this.m_InitialPopulationSize);
//        }
        //System.out.println("init");
    	checkPopulationConstraints();
    	m_Population.putData(esMuParam, getMu());
        this.m_Problem.initPopulation(this.m_Population);
        this.evaluatePopulation(this.m_Population);
//        this.m_Population.setPopulationSize(orgPopSize);
//        this.firePropertyChangedEvent(Population.nextGenerationPerformed);// not necessary if incrGeneration is called
    }


    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
    	origPopSize = pop.getTargetSize();
//    	System.out.println("ES: orig popsize is " + origPopSize);
        this.m_Population = (Population)pop.clone();
        if (reset) {
        	this.m_Population.init();
            this.evaluatePopulation(this.m_Population);
//            this.firePropertyChangedEvent(Population.nextGenerationPerformed); // not necessary if incrGeneration is called
        }
    }

    /** This method will evaluate the current population using the
     * given problem.
     * @param population The population that is to be evaluated
     */
    protected void evaluatePopulation(Population population) {
        this.m_Problem.evaluate(population);
        population.incrGeneration();
    }

//    /** This method allows you to set myu and lambda
//     * @param myu   The size of the temporary population
//     * @param lambda The size of the offsprings created
//     * @param plus  The general population strategy.
//     */
//    public void setGenerationStrategy(int myu, int lambda, boolean plus) {
//        this.m_UsePlusStrategy  = plus;
//        this.m_Myu              = myu;
//        this.m_Lambda           = lambda;
//        this.m_MyuRatio         = this.m_Lambda/(double)this.m_Myu;
//        if (this.m_UsePlusStrategy) this.m_Population.setPopulationSize(myu+lambda);
//        else this.m_Population.setPopulationSize(lambda);
//    }

    /**
     * This method will generate the offspring population from the
     * given population of evaluated individuals.
     */
    protected Population generateEvalChildren(Population fromPopulation) {
        Population                  result = m_Population.cloneWithoutInds(), parents;

        result.clear();

        parents = generateChildren(fromPopulation, result, this.m_Lambda);
        
        this.evaluatePopulation(result);
        
        if (result.getEAIndividual(0).getMutationOperator() instanceof InterfaceMutationGenerational) {
        	// this seems to be the right moment for the 1/5-success rule
        	// parents and result have the same size and correspond per individual        	
        	((InterfaceMutationGenerational)parents.getEAIndividual(0).getMutationOperator()).adaptGenerational(fromPopulation, parents, result, m_UsePlusStrategy);
        }
        
        return result;
    }
    
    /**
     * Create a new population by parent selection, partner selection, recombination and crossover.
     * The new population is added to the result population, while the selected parent population (after
     * selection but before recombination/crossover) is returned.
     * Returned parents and result population are to have the same size and correspond per individual.
     * 
     * @param fromPopulation
     * @param result
     * @param lambda
     * @return
     */
    protected Population generateChildren(Population fromPopulation, Population result, int lambda) {
        AbstractEAIndividual        tmpIndy;
        AbstractEAIndividual[]      offSprings;
        Population parents;

        this.m_ParentSelection.prepareSelection(fromPopulation);
        this.m_PartnerSelection.prepareSelection(fromPopulation);
        parents     = this.m_ParentSelection.selectFrom(fromPopulation, lambda);

        for (int i = 0; i < parents.size(); i++) {
            tmpIndy =  (AbstractEAIndividual)parents.get(i);
            offSprings = tmpIndy.mateWith(this.m_PartnerSelection.findPartnerFor(tmpIndy, fromPopulation, this.m_NumberOfPartners));
            offSprings[0].mutate();
            result.add(i, offSprings[0]);
        }
        return parents;
    }
    
    protected Population selectParents(Population fromPop, int mu) {
    	this.m_EnvironmentSelection.prepareSelection(fromPop);
    	return this.m_EnvironmentSelection.selectFrom(fromPop, mu);
    }
    
    /** 
     * The optimize method will compute an improved and evaluated population.
     */
    public void optimize() {
        Population  nextGeneration, parents;

       //System.out.println("optimize");
        
        // first perform the environment selection to select myu parents
        parents = selectParents(m_Population, this.m_Mu);
        
//        System.out.println("-- selected avg fit " + BeanInspector.toString(parents.getMeanFitness()) + " from last gen " + BeanInspector.toString(m_Population.getMeanFitness()));
        
        // m_Population / parents are of sizes lambda / mu 
        if (parents.getEAIndividual(0).getMutationOperator() instanceof InterfaceMutationGenerational) {
        	((InterfaceMutationGenerational)parents.getEAIndividual(0).getMutationOperator()).adaptAfterSelection(getPopulation(), parents);
        }
        
        // now generate the lambda offsprings
		nextGeneration = this.generateEvalChildren(parents); // create lambda new ones from mu parents
        
        if (this.isPlusStrategy()) nextGeneration.addPopulation(parents);
       
        setPop(getReplacePop(nextGeneration));
//        System.out.println("Population size: " + this.m_Population.size());
//        System.out.println("-- Best Fitness " + this.m_Population.getBestFitness()[0]);
        
        this.firePropertyChangedEvent(Population.nextGenerationPerformed); // necessary here because evalPop was not called on m_Population
    }        
    
    /**
     * Usually, this just returns the given population.
     * However, in case of CBN this method prepares the next generation according to the species size.
     * 
     * @param nextGeneration
     * @return
     */
    protected Population getReplacePop(Population nextGeneration) {
    	if (forceOrigPopSize && (origPopSize > 0) && (origPopSize < nextGeneration.size())) {
    		// this is especially for CBN: earlier selection to immediately reduce the size of mu+lambda to lambda
    		this.m_EnvironmentSelection.prepareSelection(nextGeneration);
    		Population tmpPop = (Population)nextGeneration.clone();
    		nextGeneration.clear();
    		nextGeneration.addPopulation(this.m_EnvironmentSelection.selectFrom(tmpPop, origPopSize));
//  		System.out.println("ES post selection! " + origPopSize + " from " + tmpPop.size());
    	} else {
    		if ((origPopSize > 0) && (origPopSize != nextGeneration.size())) {
    			System.err.println("Warning in ES! orig: " + origPopSize + " / " + nextGeneration.size());
    		}
    	}
    	return nextGeneration;
    }

    /** This method is just a shortcut to set the mutation step size for
     * all individuals of these two populations for the 1/5 Success rule.
     * This is only necessary because i decided to make the variable
     * non static
     * @param successRate   The success rate
     * @param oldPop        The old population
     * @param newPop        The new population
     */
//    private void applySuccessRule(double successRate, Population oldPop, Population newPop) {
//        MutateESSuccessRule mutator =  (MutateESSuccessRule)((AbstractEAIndividual)oldPop.get(0)).getMutationOperator();
//        boolean success = (successRate < mutator.getSuccessRate());
//        // this was the old solution when the mutation step size was still static
////        if (successRate < mutator.getSuccessRate()) {
////            mutator.decreaseMutationStepSize();
////        } else {
////            mutator.increaseMutationStepSize();
////        }
//        if (isPlusStrategy()) for (int i = 0; i < oldPop.size(); i++) { // applied to the old population as well for plus strategy
//            if (((AbstractEAIndividual)oldPop.get(i)).getMutationOperator() instanceof MutateESSuccessRule) {
//                mutator =  (MutateESSuccessRule)((AbstractEAIndividual)oldPop.get(i)).getMutationOperator();
//                if (success) mutator.decreaseMutationStepSize();
//                else mutator.increaseMutationStepSize();
//                System.out.println("old pop step size " + mutator.getSigma()+ " (" + mutator+ ")");
//            }
//        }
//        for (int i = 0; i < newPop.size(); i++) {
//            if (((AbstractEAIndividual)newPop.get(i)).getMutationOperator() instanceof MutateESSuccessRule) {
//                mutator =  (MutateESSuccessRule)((AbstractEAIndividual)newPop.get(i)).getMutationOperator();
//                if (success) mutator.decreaseMutationStepSize();
//                else mutator.increaseMutationStepSize();
//                System.out.println("new pop step size " + mutator.getSigma() + " (" + mutator+ ")");
//            }
//        }
////        this.m_FitnessOfParents = null;
//    }

//    /** This is for debugging only
//     */
//    private String showFitness(Population pop) {
//        String result = "";
//        AbstractEAIndividual indy;
//        double[]    fitness;
//        for (int i = 0; i < pop.size(); i++) {
//            indy = (AbstractEAIndividual)pop.get(i);
//            fitness = indy.getFitness();
//            for (int j = 0; j < fitness.length; j++) result += fitness[j] +"; ";
//            result += "\n";
//        }
//        return result;
//    }

    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }
	public boolean removePopulationChangedEventListener(
			InterfacePopulationChangedEventListener ea) {
		if (m_Listener==ea) {
			m_Listener=null;
			return true;
		} else return false;
	}
    /** Something has changed
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.m_Listener != null) this.m_Listener.registerPopulationStateChanged(this, name);
    }

    /** This method will set the problem that is to be optimized
     * @param problem
     */
    public void SetProblem (InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
    }
    public InterfaceOptimizationProblem getProblem () {
        return this.m_Problem;
    }

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "Evolution Strategies:\n";
        result += "Optimization Problem: ";
        result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
        result += this.m_Population.getStringRepresentation();
        return result;
    }

    /** This method allows you to set myu and lambda in one step
     * @param myu
     * @param lambda
     * @param plus     True if plus, false if comma strategy
     */
    public void setGenerationStrategy(int myu, int lambda, boolean plus) {
        this.m_Mu              = myu;
        this.m_Lambda           = lambda;
        this.m_UsePlusStrategy  = plus;
        this.checkPopulationConstraints();
    }

    /** 
     * This method will check the population constraints
     * myu <= lambda and will calculate the population size
     * accordingly.
     */
    protected void checkPopulationConstraints() {
        if (this.m_Lambda < this.m_Mu) {
        	System.err.println("Invalid mu/lambda ratio! Setting mu=lambda="+m_Mu);
        	this.m_Lambda = this.m_Mu;
        }
//        if (this.m_UsePlusStrategy) this.m_Population.setTargetSize(this.m_Mu + this.m_Lambda);
//        else this.m_Population.setTargetSize(this.m_Lambda);
		this.m_Population.setTargetSize(this.m_Lambda);
        origPopSize=m_Population.getTargetSize();
    }

    /** This method allows you to set an identifier for the algorithm
     * @param name      The indenifier
     */
     public void SetIdentifier(String name) {
        this.m_Identifier = name;
    }
     public String getIdentifier() {
         return this.m_Identifier;
     }

    /** This method is required to free the memory on a RMIServer,
     * but there is nothing to implement.
     */
    public void freeWilly() {

    }    
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is an Evolution Strategy. Note that the population size depends on mu (number of parents) and lambda (number of offspring).";
    }
    
    public String[] customPropertyOrder() {
    	return new String[]{"mu", "lambda"};
    }
    
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "("+getMu()+(isPlusStrategy() ? "+" : ",")+getLambda()+")-ES";
    }

    /** 
     * Assuming that all optimizer will store their data in a population
     * we will allow access to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return this.m_Population;
    }

    // for internal usage
    protected void setPop(Population pop) {
    	m_Population = pop;
    }
    
    public void setPopulation(Population pop){
    	origPopSize = pop.size();
//    	System.err.println("In ES: orig popsize is " + origPopSize);
        this.m_Population = pop;
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }
    
    public InterfaceSolutionSet getAllSolutions() {
    	return new SolutionSet(getPopulation());
    }
//    /** This method will set the normation method that is to be used.
//     * @param normation
//     */
//    public void setNormationMethod (InterfaceNormation normation) {
//        this.m_NormationOperator = normation;
//    }
//    public InterfaceNormation getNormationMethod () {
//        return this.m_NormationOperator;
//    }
//    public String normationMethodTipText() {
//        return "Select the normation method.";
//    }

    /** This method will set the selection method that is to be used
     * @param selection
     */
    public void setEnvironmentSelection(InterfaceSelection selection) {
        this.m_EnvironmentSelection = selection;
    }
    public InterfaceSelection getEnvironmentSelection() {
        return this.m_EnvironmentSelection;
    }
    public String environmentSelectionTipText() {
        return "Choose a method for selecting the reduced population.";
    }

    /** This method will set the selection method that is to be used to select the partners
     * @param selection
     */
    public void setPartnerSelection(InterfaceSelection selection) {
        this.m_PartnerSelection = selection;
    }
    public InterfaceSelection getPartnerSelection() {
        return this.m_PartnerSelection;
    }
    public String partnerSelectionTipText() {
        return "Choose a selection method for selecting recombination partners.";
    }

    /** This method will set the selection method that is to be used to select the parents
     * @param selection
     */
    public void setParentSelection(InterfaceSelection selection) {
        this.m_ParentSelection = selection;
    }
    public InterfaceSelection getParentSelection() {
        return this.m_ParentSelection;
    }
    public String parentSelectionTipText() {
        return "Choose a selection method for selecting parents.";
    }

    /** This method will toggel between plus and comma selection strategy
     * @param elitism
     */
    public void setPlusStrategy (boolean elitism) {
        this.m_UsePlusStrategy = elitism;
//        this.checkPopulationConstraints(); // do this on init only
    }
    public boolean isPlusStrategy() {
        return this.m_UsePlusStrategy;
    }
    public String plusStrategyTipText() {
        return "Select between plus and comma strategy.";
    }

    /** This method will set the number of partners that are needed to create
     * offsprings by mating
     * @param partners
     */
    public void setNumberOfPartners(int partners) {
        if (partners < 0) partners = 0;
        this.m_NumberOfPartners = partners;
    }
    public int getNumberOfPartners() {
        return this.m_NumberOfPartners;
    }
    public String numberOfPartnersTipText() {
        return "The number of mating partners needed to create offsprings.";
    }

//    /** This method will set the ratio between myu and lambda
//     * @param lambdaratio
//     */
//    public void setLambdaRatio(int lambdaratio) {
//        if (lambdaratio < 1) lambdaratio = 1;
//        this.m_LambdaRatio = lambdaratio;
//    }
//    public int getLambdaRatio() {
//        return this.m_LambdaRatio;
//    }
//    public String lambdaRatioTipText() {
//        return "This is the myu/lambda ratio.";
//    }
    /** This method will set the ratio between myu and lambda
     * @param myuratio
     */
//    public void setMyuRatio(double myuratio) {
//        if (myuratio < 1) myuratio = 1;
//        this.m_MyuRatio = myuratio;
//    }
//    public double getMyuRatio() {
//        return this.m_MyuRatio;
//    }
//    public String myuRatioTipText() {
//        return "This is the lambda/myu ratio.";
//    }

    /** This method allows you to set parent population size myu
     * @param myu   The parent population size.
     */
    public void setMu(int mu) {
        this.m_Mu = mu;
//        this.checkPopulationConstraints(); // do this on init only
    }
    public int getMu() {
        return this.m_Mu;
    }
    public String muTipText() {
        return "This is the parent population size.";
    }

    /** This is the children population size lambda
     * @param lambda    The children population size.
     */
    public void setLambda(int lambda) {
        this.m_Lambda = lambda;
//        this.checkPopulationConstraints(); // do this on init only
    }
    public int getLambda() {
        return this.m_Lambda;
    }
    public String lambdaTipText() {
        return "This is the children population size.";
    }
}
