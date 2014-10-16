package eva2.optimization.strategies;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectBestIndividuals;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.F1Problem;
import eva2.problems.InterfaceLocalSearchable;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;

import java.util.Hashtable;

/**
 * A memetic algorithm by hannes planatscher. The local search strategy can only
 * be applied to problems which implement the InterfaceLocalSearchable else the
 * local search will not be activated at all.
 *
 */
@Description("This is a basic generational Memetic Algorithm. Local search steps are performed on a selected subset "
        + "of individuals after certain numbers of global search iterations. Note "
        + "that the problem class must implement InterfaceLocalSearchable.")
public class MemeticAlgorithm implements InterfaceOptimizer,
        java.io.Serializable {

    /**
     * serial version uid.
     */
    private static final long serialVersionUID = -1730086430763348568L;
    private int localSearchSteps = 1;
    private int subsetsize = 5;
    private int globalSearchIterations = 1;
    private boolean lamarckism = true;
    // int counter = 0; !?
    // int maxfunctioncalls = 1000; !?
    private boolean TRACE = false;
    private String identifier = "";
    private InterfaceOptimizationProblem optimizationProblem = new F1Problem();
    private InterfaceOptimizer globalOptimizer = new GeneticAlgorithm();
    private InterfaceSelection selectorPlug = new SelectBestIndividuals();
    transient private InterfacePopulationChangedEventListener populationChangedEventListener;

    public MemeticAlgorithm() {
    }

    public MemeticAlgorithm(MemeticAlgorithm a) {
        // this.population = (Population)a.population.clone();
        this.optimizationProblem = (InterfaceLocalSearchable) a.optimizationProblem.clone();
        this.globalOptimizer = (InterfaceOptimizer) a.globalOptimizer;
        this.selectorPlug = (InterfaceSelection) a.selectorPlug;
        this.identifier = a.identifier;
        this.localSearchSteps = a.localSearchSteps;
        this.subsetsize = a.subsetsize;
        this.globalSearchIterations = a.globalSearchIterations;
        this.lamarckism = a.lamarckism;
    }

    @Override
    public Object clone() {
        return new MemeticAlgorithm(this);
    }

    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        this.setPopulation((Population) pop.clone());
        if (reset) {
            this.getPopulation().init();
            this.optimizationProblem.evaluate(this.getPopulation());
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    @Override
    public void initialize() {
        // counter = 0;
        this.globalOptimizer.setProblem(this.optimizationProblem);
        this.globalOptimizer.initialize();
        this.evaluatePopulation(this.globalOptimizer.getPopulation());
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
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

    @Override
    public void optimize() {

        if (TRACE) {
            System.out.println("global search");
        }
        this.globalOptimizer.optimize();

        if ((globalSearchIterations > 0) && (((this.globalOptimizer.getPopulation().getGeneration() % this.globalSearchIterations) == 0))
                && (this.localSearchSteps > 0)
                && (this.optimizationProblem instanceof InterfaceLocalSearchable)) {
            // here the local search is performed
            if (TRACE) {
                System.out.println("Performing local search on " + subsetsize
                        + " individuals.");
            }
            Population gop = this.globalOptimizer.getPopulation();
            Population subset = selectorPlug.selectFrom(gop, subsetsize);
            Population subsetclone = new Population();
            for (int i = 0; i < subset.size(); i++) {
                subsetclone.add(((AbstractEAIndividual) subset.get(i)).clone());
            }
            if (subset.size() != subsetsize) {
                System.err.println("ALERT! identical individual instances in subset");
            }
            Hashtable antilamarckismcache = new Hashtable();
            if (!this.lamarckism) {
                for (int i = 0; i < subset.size(); i++) {
                    AbstractEAIndividual indy = (AbstractEAIndividual) subset.get(i);
                    AbstractEAIndividual indyclone = (AbstractEAIndividual) subsetclone
                            .get(i);
                    antilamarckismcache.put(indy, indyclone);
                }
            }

            // int dosearchsteps = this.localSearchSteps;
            double cost = ((InterfaceLocalSearchable) this.optimizationProblem)
                    .getLocalSearchStepFunctionCallEquivalent();
            // int calls = gop.getFunctionCalls() + (int) Math.round(localSearchSteps
            // * cost * subset.size());
            // nett aber total unn�tig-falsch man kann nicht davon ausgehen, dass man
            // einen Fitnesscall Terminator hat..
            // if (calls > this.maxfunctioncalls) {
            // int remainingfunctioncalls = this.maxfunctioncalls -
            // gop.getFunctionCalls();
            // dosearchsteps = (int)Math.floor(((double) remainingfunctioncalls) /
            // (cost * subsetsize));
            // stopit = true;
            // }
            for (int i = 0; i < localSearchSteps; i++) {
                ((InterfaceLocalSearchable) this.optimizationProblem).doLocalSearch(subsetclone);
            }
            this.optimizationProblem.evaluate(subsetclone);
            if (this.lamarckism) {
                gop.removeAll(subset);
                gop.addPopulation(subsetclone);
            } else {
                for (int i = 0; i < subset.size(); i++) {
                    AbstractEAIndividual indy = (AbstractEAIndividual) subset.get(i);
                    try {
                        AbstractEAIndividual newindy = (AbstractEAIndividual) antilamarckismcache
                                .get(indy);
                        indy.setFitness(newindy.getFitness());
                    } catch (Exception ex) {
                        System.err.println("individual not found in antilamarckismcache");
                    }
                }
            }
            // eigentlich muss hier noch subsetsize drauf, aber lassen wir das
            gop.setFunctionCalls(gop.getFunctionCalls()
                    + (int) Math.round(localSearchSteps * cost * subset.size()));

            if (TRACE) {
                System.out.println("Population size after local search:" + gop.size());
            }

            this.setPopulation(gop);
        }

        if (TRACE) {
            System.out.println("function calls"
                    + this.globalOptimizer.getPopulation().getFunctionCalls());
        }
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method allows you to add the LectureGUI as listener to the Optimizer
     *
     * @param ea
     */
    @Override
    public void addPopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
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
            if (TRACE) {
                System.out.println("firePropertyChangedEvent MA");
            }
            this.populationChangedEventListener.registerPopulationStateChanged(this, name);
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
        this.globalOptimizer.setProblem(this.optimizationProblem);
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
        result += "Memetic Algorithm:\n";
        result += "Optimization Problem: ";
        result += this.optimizationProblem.getStringRepresentationForProblem(this) + "\n";
        result += this.globalOptimizer.getStringRepresentation();
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
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "MemeticAlgorithm";
    }

    /**
     * Assuming that all optimizer will store thier data in a population we will
     * allow acess to this population to query to current state of the
     * optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    @Override
    public Population getPopulation() {
        return this.globalOptimizer.getPopulation();
    }

    @Override
    public void setPopulation(Population pop) {
        this.globalOptimizer.setPopulation(pop);
    }

    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    /**
     * Choose the global optimization strategy to use
     *
     * @param globalOptimizer
     */
    public void setGlobalOptimizer(InterfaceOptimizer globalOptimizer) {
        this.globalOptimizer = globalOptimizer;
        this.globalOptimizer.setProblem(this.getProblem());
        this.initialize();
    }

    public InterfaceOptimizer getGlobalOptimizer() {
        return globalOptimizer;
    }

    public String globalOptimizerTipText() {
        return "Choose the global optimization strategy to use.";
    }

    /**
     * Choose the number of local search steps to perform per selected
     * individual.
     *
     * @param localSearchSteps
     */
    public void setLocalSearchSteps(int localSearchSteps) {
        this.localSearchSteps = localSearchSteps;
    }

    public int getLocalSearchSteps() {
        return localSearchSteps;
    }

    public String localSearchStepsTipText() {
        return "Choose the number of local search steps to perform per selected individual.";
    }

    /**
     * Choose the interval between the application of the local search
     *
     * @param globalSearchSteps
     */
    public void setGlobalSearchIterations(int globalSearchSteps) {
        this.globalSearchIterations = globalSearchSteps;
    }

    public int getGlobalSearchIterations() {
        return globalSearchIterations;
    }

    public String globalSearchIterationsTipText() {
        return "Choose the interval between the application of the local search.";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(getPopulation());
    }

    /**
     * Choose the number of individual to be locally optimized
     *
     * @param subsetsize
     */
    public void setSubsetsize(int subsetsize) {
        this.subsetsize = subsetsize;
    }

    public int getSubsetsize() {
        return subsetsize;
    }

    public String subsetsizeTipText() {
        return "Choose the number of individuals to be locally optimized.";
    }

    /**
     * Toggle between Lamarckism and the Baldwin Effect
     *
     * @param lamarckism
     */
    public void setLamarckism(boolean lamarckism) {
        this.lamarckism = lamarckism;
    }

    public String lamarckismTipText() {
        return "Toggle between Lamarckism and the Baldwin Effect.";
    }

    public boolean isLamarckism() {
        return lamarckism;
    }

    public InterfaceSelection getSubSetSelector() {
        return selectorPlug;
    }

    public void setSubSetSelector(InterfaceSelection selectorPlug) {
        this.selectorPlug = selectorPlug;
    }

    public String subSetSelectorTipText() {
        return "Selection method to select the subset on which local search is to be performed.";
    }
}
