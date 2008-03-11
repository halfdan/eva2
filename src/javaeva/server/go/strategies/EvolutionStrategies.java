package javaeva.server.go.strategies;

import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.operators.mutation.MutateESSuccessRule;
import javaeva.server.go.operators.selection.InterfaceSelection;
import javaeva.server.go.operators.selection.SelectBestIndividuals;
import javaeva.server.go.operators.selection.SelectRandom;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.B1Problem;
import javaeva.server.go.problems.InterfaceOptimizationProblem;

/** Evolution strategies by Rechenberg and Schwefel, but please remember that
 * this only gives the generation strategy and not the coding. But this is the
 * only stategies that is able to utilize the 1/5 success rule mutation. Unfortunately,
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
    private int                             m_Myu                       = 5;
    private int                             m_Lambda                    = 20;
    private int                             m_InitialPopulationSize     = 0;
    private boolean                         m_UsePlusStrategy           = false;
    private Population                      m_Population                = new Population();
    private InterfaceOptimizationProblem    m_Problem                   = new B1Problem();
    private InterfaceSelection              m_ParentSelection           = new SelectRandom();
    private InterfaceSelection              m_PartnerSelection          = new SelectRandom();
    private InterfaceSelection              m_EnvironmentSelection      = new SelectBestIndividuals();
    private int                             m_NumberOfPartners          = 1;
    private double[]                        m_FitnessOfParents          = null;

    transient private String                m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    public EvolutionStrategies() {
        this.m_Population.setPopulationSize(this.m_Lambda);
    }
    
    public EvolutionStrategies(int mu, int lambda, boolean usePlus) {
    	setMyu(mu);
    	setLambda(lambda);
    	setPlusStrategy(usePlus);
        this.m_Population.setPopulationSize(this.m_Lambda);
    }
    
    public EvolutionStrategies(EvolutionStrategies a) {
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (InterfaceOptimizationProblem)a.m_Problem.clone();
        this.m_Myu                          = a.m_Myu;
        this.m_Lambda                       = a.m_Lambda;
        this.m_InitialPopulationSize        = a.m_InitialPopulationSize;
        this.m_UsePlusStrategy              = a.m_UsePlusStrategy;
        this.m_NumberOfPartners             = a.m_NumberOfPartners;
        this.m_ParentSelection              = (InterfaceSelection)a.m_ParentSelection.clone();
        this.m_PartnerSelection             = (InterfaceSelection)a.m_PartnerSelection.clone();
        this.m_EnvironmentSelection         = (InterfaceSelection)a.m_EnvironmentSelection.clone();
    }

    public Object clone() {
        return (Object) new EvolutionStrategies(this);
    }

    public void init() {
        // @todo In case of CBN-ES i need to read the population size!?
        // @todo but how!? I guess this will never do...
        int orgPopSize = this.m_Population.getPopulationSize();
        if (this.m_InitialPopulationSize > orgPopSize) {
            this.m_Population.setPopulationSize(this.m_InitialPopulationSize);
        }
        //System.out.println("init");
        this.m_Problem.initPopulation(this.m_Population);
        this.evaluatePopulation(this.m_Population);
        this.m_Population.setPopulationSize(orgPopSize);
        this.firePropertyChangedEvent("NextGenerationPerformed");
//        int myu         = this.m_Population.getPopulationSize();
//        int initPopSize = 0;
//        if (this.m_UsePlusStrategy) initPopSize = myu + (this.m_LambdaRatio * myu);
//        else initPopSize = (this.m_LambdaRatio * myu);
//        // Init with initPopSize individuals
//        this.m_Population.setPopulationSize(initPopSize);
//        this.m_Problem.initPopulation(this.m_Population);
//        this.evaluatePopulation(this.m_Population);
//        this.m_Population.setPopulationSize(myu);
    }


    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population)pop.clone();
        if (reset) this.m_Population.init();
        this.evaluatePopulation(this.m_Population);
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will evaluate the current population using the
     * given problem.
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
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

    /** This method will generate the offspring population from the
     * given population of evaluated individuals.
     */
    private Population generateChildren() {
        Population                  result = (Population)this.m_Population.clone(), parents;
        AbstractEAIndividual[]      offSprings;
        AbstractEAIndividual        tmpIndy;

        result.clear();
        this.m_ParentSelection.prepareSelection(this.m_Population);
        this.m_PartnerSelection.prepareSelection(this.m_Population);
        parents     = this.m_ParentSelection.selectFrom(this.m_Population, this.m_Lambda);
        for (int i = 0; i < parents.size(); i++) {
            tmpIndy =  (AbstractEAIndividual)parents.get(i);
            if (tmpIndy == null) System.out.println("Individual null "+i);
            if (parents == null) System.out.println("parents null "+i);
            if (tmpIndy.getMutationOperator() instanceof MutateESSuccessRule) {
                if (this.m_FitnessOfParents == null) this.m_FitnessOfParents = new double[this.m_Lambda];
                this.m_FitnessOfParents[i] = tmpIndy.getFitness(0);
            }
            offSprings = tmpIndy.mateWith(this.m_PartnerSelection.findPartnerFor(tmpIndy, this.m_Population, this.m_NumberOfPartners));
//            for (int j = 0; j < offSprings.length; j++) {
//                offSprings[j].mutate();
//            }
            offSprings[0].mutate();
            result.add(i, offSprings[0]);
        }
        return result;
    }

    /** The optimize method will compute a 'improved' and evaluated population
     */
    public void optimize() {
        Population  nextGeneration, parents;

//        // calculate myu and lambda from the current population size and settings
//        if (this.m_UsePlusStrategy) {
//            this.m_Myu      = (int)Math.round((this.m_Population.size()/this.m_MyuRatio) - (this.m_Population.size()/Math.pow(this.m_MyuRatio, 2)));
//            this.m_Myu      = Math.max(1, this.m_Myu);
//            this.m_Lambda   = this.m_Population.size() - this.m_Myu;
////            System.out.println("Parameters: (Pop.size:"+this.m_Population.size()+"; MyuRatio:"+this.m_MyuRatio+")");
////            System.out.println("Population Strategy: ("+ this.m_Myu+"+"+this.m_Lambda+")");
//        }
//        else {
//            this.m_Lambda   = this.m_Population.size();
//            this.m_Myu      = (int)Math.round(this.m_Population.size()/this.m_MyuRatio);
//            this.m_Myu      = Math.max(1, this.m_Myu);
////            System.out.println("Parameters: (Pop.size:"+this.m_Population.size()+"; MyuRatio:"+this.m_MyuRatio+")");
////            System.out.println("Population Strategy: ("+ this.m_Myu+","+this.m_Lambda+")");
//        }
       //System.out.println("optimize");
        // first perform the environment selection to select myu parents
        this.m_EnvironmentSelection.prepareSelection(this.m_Population);
        parents = this.m_EnvironmentSelection.selectFrom(this.m_Population, this.m_Myu);
        this.m_Population.clear();
        this.m_Population.addPopulation(parents);
        // now generate the lambda offsprings
        this.m_FitnessOfParents = null;
        nextGeneration = this.generateChildren();
        this.evaluatePopulation(nextGeneration);
        if ((this.m_FitnessOfParents != null) && (((AbstractEAIndividual)parents.get(0)).getMutationOperator() instanceof MutateESSuccessRule)) {
            double              rate = 0;

            for (int i = 0; i < this.m_FitnessOfParents.length; i++) {
                if (((AbstractEAIndividual)nextGeneration.get(i)).getFitness(0) < this.m_FitnessOfParents[i]) rate++;
            }
            this.applySuccessRule((rate/((double)this.m_FitnessOfParents.length)), this.m_Population, nextGeneration);
        }
        if (this.m_UsePlusStrategy) nextGeneration.addPopulation(this.m_Population);
        this.m_Population = nextGeneration;
        //System.out.println("Population size: " + this.m_Population.size());
        //System.out.println("-- Best Fitness " + this.m_Population.getBestFitness()[0]);
        
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method is just a shortcut to set the mutation step size for
     * all individuals of these two populations for the 1/5 Success rule.
     * This is only necessary because i decieded to make the variable
     * non static
     * @param successRate   The success rate
     * @param oldPop        The old population
     * @param newPop        The new population
     */
    private void applySuccessRule(double successRate, Population oldPop, Population newPop) {
        MutateESSuccessRule mutator =  (MutateESSuccessRule)((AbstractEAIndividual)oldPop.get(0)).getMutationOperator();
        boolean success = (successRate < mutator.getSuccessRate());
        // this was the old solution when the mutation step size was still static
//        if (successRate < mutator.getSuccessRate()) {
//            mutator.decreaseMutationStepSize();
//        } else {
//            mutator.increaseMutationStepSize();
//        }
        for (int i = 0; i < oldPop.size(); i++) {
            if (((AbstractEAIndividual)oldPop.get(i)).getMutationOperator() instanceof MutateESSuccessRule) {
                mutator =  (MutateESSuccessRule)((AbstractEAIndividual)oldPop.get(i)).getMutationOperator();
                if (success) mutator.decreaseMutationStepSize();
                else mutator.increaseMutationStepSize();
            }
        }
        for (int i = 0; i < newPop.size(); i++) {
            if (((AbstractEAIndividual)newPop.get(i)).getMutationOperator() instanceof MutateESSuccessRule) {
                mutator =  (MutateESSuccessRule)((AbstractEAIndividual)newPop.get(i)).getMutationOperator();
                if (success) mutator.decreaseMutationStepSize();
                else mutator.increaseMutationStepSize();
            }
        }
        this.m_FitnessOfParents = null;
    }

    /** This is for debugging only
     */
    private String showFitness(Population pop) {
        String result = "";
        AbstractEAIndividual indy;
        double[]    fitness;
        for (int i = 0; i < pop.size(); i++) {
            indy = (AbstractEAIndividual)pop.get(i);
            fitness = indy.getFitness();
            for (int j = 0; j < fitness.length; j++) result += fitness[j] +"; ";
            result += "\n";
        }
        return result;
    }

    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }
    /** Something has changed
     */
    protected void firePropertyChangedEvent (String name) {
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
        this.m_Myu              = myu;
        this.m_Lambda           = lambda;
        this.m_UsePlusStrategy  = plus;
        this.checkPopulationConstraints();
    }

    /** This method will check the population constraints
     * myu <= lambda and will calculate the population size
     * accordingly.
     */
    private void checkPopulationConstraints() {
        if (this.m_Lambda < this.m_Myu) this.m_Lambda = this.m_Myu;
        if (this.m_UsePlusStrategy) this.m_Population.setPopulationSize(this.m_Myu + this.m_Lambda);
        else this.m_Population.setPopulationSize(this.m_Lambda);
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
        return "This is an Evolution Strategy. Note that the population size gives lambda.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "("+getMyu()+(getPlusStrategy() ? "+" : ",")+getLambda()+")-ES";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return this.m_Population;
    }
    public void setPopulation(Population pop){
        this.m_Population = pop;
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }
    
    public Population getAllSolutions() {
    	return getPopulation();
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
        this.checkPopulationConstraints();
    }
    public boolean getPlusStrategy() {
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
    public void setMyu(int myu) {
        this.m_Myu = myu;
        this.checkPopulationConstraints();
    }
    public int getMyu() {
        return this.m_Myu;
    }
    public String myuTipText() {
        return "This is the parent population size.";
    }

    /** This is the children population size lambda
     * @param lambda    The children population size.
     */
    public void setLambda(int lambda) {
        this.m_Lambda = lambda;
        this.checkPopulationConstraints();
    }
    public int getLambda() {
        return this.m_Lambda;
    }
    public String lambdaTipText() {
        return "This is the children population size.";
    }

    /** Set an initial population size (if smaller lambda this is ignored).
     * @param l    The inital population size.
     */
    public void setInitialPopulationSize(int l) {
        this.m_InitialPopulationSize = l;
    }
    public int getInitialPopulationSize() {
        return this.m_InitialPopulationSize;
    }
    public String initialPopulationSizeTipText() {
        return "Set an initial population size (if smaller lambda this is ignored).";
    }
}
