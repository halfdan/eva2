package eva2.optimization.strategies;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.B1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

/**
 * This is a Multi-Start Hill-Climber, here the population size gives the number
 * of multi-starts. Similar to the evolutionary programming strategy this
 * strategy sets the mutation rate temporarily to 1.0.
 */
@Description("The Hill Climber uses the default EA mutation and initializing operators. If the population size is bigger than one a multi-start Hill Climber is performed.")
public class HillClimbing implements InterfaceOptimizer, java.io.Serializable {
    // These variables are necessary for the simple testcase

    private InterfaceOptimizationProblem optimizationProblem = new B1Problem();
    private InterfaceMutation mutator = null;
    // These variables are necessary for the more complex LectureGUI enviroment
    transient private String identifier = "";
    transient private InterfacePopulationChangedEventListener populationChangedEventListener;
    private Population population;

    public HillClimbing() {
        this.population = new Population();
        this.population.setTargetSize(10);
    }

    public HillClimbing(HillClimbing a) {
        this.population = (Population) a.population.clone();
        this.optimizationProblem = (InterfaceOptimizationProblem) a.optimizationProblem.clone();
    }

    @Override
    public Object clone() {
        return new HillClimbing(this);
    }

    /**
     * This method will initialize the HillClimber
     */
    @Override
    public void initialize() {
        this.optimizationProblem.initializePopulation(this.population);
        this.optimizationProblem.evaluate(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        this.population = (Population) pop.clone();
        if (reset) {
            this.population.init();
            this.optimizationProblem.evaluate(this.population);
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    /**
     * This method will optimize
     */
    @Override
    public void optimize() {
        AbstractEAIndividual indy;
        Population original = (Population) this.population.clone();
        double tmpD;
        InterfaceMutation tmpMut;

        for (int i = 0; i < this.population.size(); i++) {
            indy = ((AbstractEAIndividual) this.population.get(i));
            tmpD = indy.getMutationProbability();
            indy.setMutationProbability(1.0);
            if (mutator == null) {
                indy.mutate();
            } else {
                mutator.mutate(indy);
            }
            indy.setMutationProbability(tmpD);
        }
        this.optimizationProblem.evaluate(this.population);
        for (int i = 0; i < this.population.size(); i++) {
            if (((AbstractEAIndividual) original.get(i)).isDominatingDebConstraints(((AbstractEAIndividual) this.population.get(i)))) {
//                this.population.remove(i);
                // throw away mutated one and replace by old one 
                this.population.set(i, original.get(i));
            } else {
                // else: mutation improved the individual, so leave the new one
            }
        }
        this.population.incrGeneration();
//        for (int i = 0; i < this.population.size(); i++) {
//            indy1 = (AbstractEAIndividual) this.population.get(i);
//            indy2 = (AbstractEAIndividual)(indy1).clone();
//            indy2.mutate();
//            this.problem.evaluate((AbstractEAIndividual) indy2);
//            //indy2.SetFitness(0, indy2.evaulateAsMiniBits());
//            this.population.incrFunctionCalls();
//            //if (indy2.getFitness(0) < indy1.getFitness(0)) {
//            if (indy2.isDominating(indy1)) {
//                this.population.remove(i);
//                this.population.add(i, indy2);
//            }
//        }
//        this.population.incrGeneration();
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
        this.optimizationProblem = problem;
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.optimizationProblem;
    }

    /**
     * This method allows you to add the LectureGUI as listener to the Optimizer
     *
     * @param ea
     */
    @Override
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.populationChangedEventListener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (populationChangedEventListener == ea) {
            populationChangedEventListener = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Something has changed
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.populationChangedEventListener != null) {
            this.populationChangedEventListener.registerPopulationStateChanged(this, name);
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
        if (this.population.size() > 1) {
            result += "Multi(" + this.population.size() + ")-Start Hill Climbing:\n";
        } else {
            result += "Hill Climbing:\n";
        }
        result += "Optimization Problem: ";
        result += this.optimizationProblem.getStringRepresentationForProblem(this) + "\n";
        result += this.population.getStringRepresentation();
        return result;
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
     * ********************************************************************************************************************
     * These are for GUI
     */

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "MS-HC";
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
        return new SolutionSet(getPopulation());
    }

    public String populationTipText() {
        return "Change the number of best individuals stored (MS-HC).";
    }
}