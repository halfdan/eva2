package eva2.optimization.strategies;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.optimization.problems.B1Problem;
import eva2.optimization.problems.InterfaceOptimizationProblem;

/**
 * This is a Multi-Start Hill-Climber, here the population size gives the number
 * of multi-starts. Similar to the evolutionary programming strategy this
 * strategy sets the mutation rate temporarily to 1.0. Copyright: Copyright (c)
 * 2003 Company: University of Tuebingen, Computer Architecture
 *
 * @author Felix Streichert
 * @version: $Revision: 307 $ $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec
 * 2007) $ $Author: mkron $
 */
public class HillClimbing implements InterfaceOptimizer, java.io.Serializable {
    // These variables are necessary for the simple testcase

    private InterfaceOptimizationProblem m_Problem = new B1Problem();
    private InterfaceMutation mutator = null;
    //    private int             m_MultiRuns             = 100;
//    private int             m_FitnessCalls          = 100;
//    private int             m_FitnessCallsNeeded    = 0;
//    GAIndividualBinaryData  m_Best, m_Test;
    // These variables are necessary for the more complex LectureGUI enviroment
    transient private String m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;
    private Population m_Population;

    public HillClimbing() {
        this.m_Population = new Population();
        this.m_Population.setTargetSize(10);
    }

    public HillClimbing(HillClimbing a) {
        this.m_Population = (Population) a.m_Population.clone();
        this.m_Problem = (InterfaceOptimizationProblem) a.m_Problem.clone();
    }

    @Override
    public Object clone() {
        return (Object) new HillClimbing(this);
    }

    /**
     * This method will init the HillClimber
     */
    @Override
    public void init() {
        this.m_Problem.initializePopulation(this.m_Population);
        this.m_Problem.evaluate(this.m_Population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    @Override
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population) pop.clone();
        if (reset) {
            this.m_Population.init();
            this.m_Problem.evaluate(this.m_Population);
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    /**
     * This method will optimize
     */
    @Override
    public void optimize() {
        AbstractEAIndividual indy;
        Population original = (Population) this.m_Population.clone();
        double tmpD;
        InterfaceMutation tmpMut;

        for (int i = 0; i < this.m_Population.size(); i++) {
            indy = ((AbstractEAIndividual) this.m_Population.get(i));
            tmpD = indy.getMutationProbability();
            indy.setMutationProbability(1.0);
            if (mutator == null) {
                indy.mutate();
            } else {
                mutator.mutate(indy);
            }
            indy.setMutationProbability(tmpD);
        }
        this.m_Problem.evaluate(this.m_Population);
        for (int i = 0; i < this.m_Population.size(); i++) {
            if (((AbstractEAIndividual) original.get(i)).isDominatingDebConstraints(((AbstractEAIndividual) this.m_Population.get(i)))) {
//                this.m_Population.remove(i); 
                // throw away mutated one and replace by old one 
                this.m_Population.set(i, original.get(i));
            } else {
                // else: mutation improved the individual, so leave the new one
            }
        }
        this.m_Population.incrGeneration();
//        for (int i = 0; i < this.m_Population.size(); i++) {
//            indy1 = (AbstractEAIndividual) this.m_Population.get(i);
//            indy2 = (AbstractEAIndividual)(indy1).clone();
//            indy2.mutate();
//            this.problem.evaluate((AbstractEAIndividual) indy2);
//            //indy2.SetFitness(0, indy2.evaulateAsMiniBits());
//            this.m_Population.incrFunctionCalls();
//            //if (indy2.getFitness(0) < indy1.getFitness(0)) {
//            if (indy2.isDominating(indy1)) {
//                this.m_Population.remove(i);
//                this.m_Population.add(i, indy2);
//            }
//        }
//        this.m_Population.incrGeneration();
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    public InterfaceMutation getMutationOperator() {
        return mutator;
    }

    /**
     * Allows to set a desired mutator by hand, which is used instead of the one
     * in the individuals. Set it to null to use the one in the individuals,
     * which is the default.
     *
     * @param mute
     */
    public void SetMutationOperator(InterfaceMutation mute) {
        mutator = mute;
    }

    /**
     * This method will set the problem that is to be optimized
     *
     * @param problem
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.m_Problem;
    }

//    /** This method will init the HillClimber
//     */
//    public void defaultInit() {
//        this.m_FitnessCallsNeeded = 0;
//        this.m_Best = new GAIndividualBinaryData();
//        this.m_Best.defaultInit();
//    }
//
//    /** This method will optimize
//     */
//    public void defaultOptimize() {
//        for (int i = 0; i < m_FitnessCalls; i++) {
//            this.m_Test = (GAIndividualBinaryData)((this.m_Best).clone());
//            this.m_Test.defaultMutate();
//            if (this.m_Test.defaultEvaulateAsMiniBits() < this.m_Best.defaultEvaulateAsMiniBits()) this.m_Best = this.m_Test;
//            this.m_FitnessCallsNeeded = i;
//            if (this.m_Best.defaultEvaulateAsMiniBits() == 0) i = this.m_FitnessCalls +1;
//        }
//    }
//    /** This main method will start a simple hillclimber.
//     * No arguments necessary.
//     * @param args
//     */
//    public static void main(String[] args) {
//        HillClimbing program = new HillClimbing();
//        int TmpMeanCalls = 0, TmpMeanFitness = 0;
//        for (int i = 0; i < program.m_MultiRuns; i++) {
//            program.defaultInit();
//            program.defaultOptimize();
//            TmpMeanCalls += program.m_FitnessCallsNeeded;
//            TmpMeanFitness += program.m_Best.defaultEvaulateAsMiniBits();
//        }
//        TmpMeanCalls = TmpMeanCalls/program.m_MultiRuns;
//        TmpMeanFitness = TmpMeanFitness/program.m_MultiRuns;
//        System.out.println("("+program.m_MultiRuns+"/"+program.m_FitnessCalls+") Mean Fitness : " + TmpMeanFitness + " Mean Calls needed: " + TmpMeanCalls);
//    }

    /**
     * This method allows you to add the LectureGUI as listener to the Optimizer
     *
     * @param ea
     */
    @Override
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (m_Listener == ea) {
            m_Listener = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Something has changed
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.m_Listener != null) {
            this.m_Listener.registerPopulationStateChanged(this, name);
        }
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
        if (this.m_Population.size() > 1) {
            result += "Multi(" + this.m_Population.size() + ")-Start Hill Climbing:\n";
        } else {
            result += "Hill Climbing:\n";
        }
        result += "Optimization Problem: ";
        result += this.m_Problem.getStringRepresentationForProblem(this) + "\n";
        result += this.m_Population.getStringRepresentation();
        return result;
    }

    /**
     * This method allows you to set an identifier for the algorithm
     *
     * @param name The indenifier
     */
    @Override
    public void setIdentifier(String name) {
        this.m_Identifier = name;
    }

    @Override
    public String getIdentifier() {
        return this.m_Identifier;
    }

    /**
     * ********************************************************************************************************************
     * These are for GUI
     */
    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "The Hill Climber uses the default EA mutation and initializing operators. If the population size is bigger than one a multi-start Hill Climber is performed.";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "MS-HC" + getIdentifier();
    }

    @Override
    public Population getPopulation() {
        return this.m_Population;
    }

    @Override
    public void setPopulation(Population pop) {
        this.m_Population = pop;
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    public String populationTipText() {
        return "Change the number of best individuals stored (MS-HC).";
    }
}