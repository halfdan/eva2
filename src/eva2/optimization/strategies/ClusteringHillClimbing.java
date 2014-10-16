package eva2.optimization.strategies;

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.enums.PostProcessMethod;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.operator.mutation.MutateESFixedStepSize;
import eva2.optimization.operator.postprocess.PostProcess;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.population.SolutionSet;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.F1Problem;
import eva2.problems.InterfaceAdditionalPopulationInformer;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.Pair;
import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 * The clustering hill climber is similar to a multi-start hill climber. In
 * addition so optimizing a set of individuals in parallel using a (1+1)
 * strategy, the population is clustered in regular intervals. If several
 * individuals have gathered together in the sense that they are interpreted as
 * a cluster, only a subset of representatives of the cluster is taken over to
 * the next HC step while the rest is discarded. This means that the population
 * size may be reduced.
 * <p/>
 * As soon as the improvement by HC lies below a threshold, the mutation step
 * size is decreased. If the step size is decreased below a certain threshold,
 * the current population is stored to an archive and reinitialized. Thus, the
 * number of optima that may be found and returned by getAllSolutions is higher
 * than the population size.
 *
 */
@Description("Similar to multi-start HC, but clusters the population during optimization to remove redundant individuals for efficiency."
        + "If the local search step does not achieve a minimum improvement, the population may be reinitialized.")
public class ClusteringHillClimbing implements InterfacePopulationChangedEventListener,
        InterfaceOptimizer, Serializable, InterfaceAdditionalPopulationInformer {

    transient private InterfacePopulationChangedEventListener populationChangedEventListener;
    public static final boolean TRACE = false;
    transient private String identifier = "";
    private Population population = new Population();
    private transient Population archive = new Population();
    private InterfaceOptimizationProblem optimizationProblem = new F1Problem();
    private int hcEvalCycle = 1000;
    private int initialPopSize = 100;
    private int loopCnt = 0;
    //   	private int								baseEvalCnt = 0;
    private int notifyGuiEvery = 50;
    private double sigmaClust = 0.01;
    private double minImprovement = 0.000001;
    private double stepSizeThreshold = 0.000001;
    private double initialStepSize = 0.1;
    // reduce the step size when there is hardy improvement. 
    private double reduceFactor = 0.2;
    private MutateESFixedStepSize mutator = new MutateESFixedStepSize(0.1);
    private PostProcessMethod localSearchMethod = PostProcessMethod.nelderMead;
    private boolean doReinitialization = true;

    public ClusteringHillClimbing() {
        hideHideable();
    }

    public ClusteringHillClimbing(int initialPopSize, PostProcessMethod lsMethod) {
        this();
        setInitialPopSize(initialPopSize);
        setLocalSearchMethod(lsMethod);
    }

    public ClusteringHillClimbing(ClusteringHillClimbing other) {
        hideHideable();
        population = (Population) other.population.clone();
        optimizationProblem = (InterfaceOptimizationProblem) other.optimizationProblem.clone();

        hcEvalCycle = other.hcEvalCycle;
        initialPopSize = other.initialPopSize;
        notifyGuiEvery = other.notifyGuiEvery;
        sigmaClust = other.sigmaClust;
        minImprovement = other.minImprovement;
        stepSizeThreshold = other.stepSizeThreshold;
        initialStepSize = other.initialStepSize;
        reduceFactor = other.reduceFactor;
        mutator = (MutateESFixedStepSize) other.mutator.clone();
        loopCnt = 0;
    }

    @Override
    public Object clone() {
        return (Object) new ClusteringHillClimbing(this);
    }

    /**
     * Hide the population.
     */
    public void hideHideable() {
        GenericObjectEditor.setHideProperty(getClass(), "population", true);
        setDoReinitialization(isDoReinitialization());
        setLocalSearchMethod(getLocalSearchMethod());
    }

    @Override
    public void setIdentifier(String name) {
        this.identifier = name;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
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

    @Override
    public void initialize() {
        loopCnt = 0;
        mutator = new MutateESFixedStepSize(initialStepSize);
        archive = new Population();
        hideHideable();
        population.setTargetSize(initialPopSize);
        this.optimizationProblem.initializePopulation(this.population);
        population.addPopulationChangedEventListener(null); // noone will be notified directly on pop changes
        this.optimizationProblem.evaluate(this.population);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * This method will initialize the optimizer with a given population
     *
     * @param pop   The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        loopCnt = 0;
        this.population = (Population) pop.clone();
        population.addPopulationChangedEventListener(null);
        if (reset) {
            this.population.init();
            this.optimizationProblem.evaluate(this.population);
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
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

    @Override
    public void optimize() {
        double improvement;

        loopCnt++;
        population.addPopulationChangedEventListener(this);
        population.setNotifyEvalInterval(notifyGuiEvery);
        Pair<Population, Double> popD;
        int funCallsBefore = population.getFunctionCalls();
        int evalsNow, lastOverhead = (population.getFunctionCalls() % hcEvalCycle);
        if (lastOverhead > 0) {
            evalsNow = (2 * hcEvalCycle - (population.getFunctionCalls() % hcEvalCycle));
        } else {
            evalsNow = hcEvalCycle;
        }
        do {
            if (TRACE) {
                System.out.println("evalCycle: " + hcEvalCycle + ", evals now: " + evalsNow);
            }
            popD = PostProcess.clusterLocalSearch(localSearchMethod, population, (AbstractOptimizationProblem) optimizationProblem, sigmaClust, evalsNow, 0.5, mutator);
            //		(population, (AbstractOptimizationProblem)problem, sigmaClust, hcEvalCycle - (population.getFunctionCalls() % hcEvalCycle), 0.5);
            if (popD.head().getFunctionCalls() == funCallsBefore) {
                System.err.println("Bad case, increasing allowed evaluations!");
                evalsNow = Math.max(evalsNow++, (int) (evalsNow * 1.2));
            }
        } while (popD.head().getFunctionCalls() == funCallsBefore);
        improvement = popD.tail();
        population = popD.head();
        if (TRACE) {
            System.out.println("num inds after clusterLS: " + population.size());
        }

        popD.head().setGeneration(population.getGeneration() + 1);

        if (doReinitialization && (improvement < minImprovement)) {
            if (TRACE) {
                System.out.println("improvement below " + minImprovement);
            }
            if ((localSearchMethod != PostProcessMethod.hillClimber) || (mutator.getSigma() < stepSizeThreshold)) { // reinit!
                // is performed for nm and cma, and if hc has too low sigma
                if (TRACE) {
                    System.out.println("REINIT!!");
                }

                if (localSearchMethod == PostProcessMethod.hillClimber) {
                    mutator.setSigma(initialStepSize);
                }

                // store results
                archive.setFunctionCalls(population.getFunctionCalls());
                archive.addPopulation(population);

                Population tmpPop = new Population();
                tmpPop.addPopulationChangedEventListener(null);
                tmpPop.setTargetSize(initialPopSize);
                this.optimizationProblem.initializePopulation(tmpPop);
                tmpPop.setSameParams(population);
                tmpPop.setTargetSize(initialPopSize);
                this.optimizationProblem.evaluate(tmpPop);

                // reset population while keeping function calls etc.
                population.clear();
                population.addPopulation(tmpPop);
                population.incrFunctionCallsBy(tmpPop.size());

            } else {  // decrease step size for hc
                if (localSearchMethod != PostProcessMethod.hillClimber) {
                    System.err.println("Invalid case in ClusteringHillClimbing!");
                }
                mutator.setSigma(mutator.getSigma() * reduceFactor);
                if (TRACE) {
                    System.out.println("mutation stepsize reduced to " + mutator.getSigma());
                }
            }
        }
//		System.out.println("funcalls: " + evalCnt);
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);

    }

    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        // The events of the interim hill climbing population will be caught here 
        if (name.compareTo(Population.FUN_CALL_INTERVAL_REACHED) == 0) {
//			if ((((Population)source).size() % 50) > 0) {
//				System.out.println("bla");
//			}
            // set funcalls to real value
            population.setFunctionCalls(((Population) source).getFunctionCalls());
//			System.out.println("FunCallIntervalReached at " + (((Population)source).getFunctionCalls()));
            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
        // do not react to NextGenerationPerformed
        //else System.err.println("ERROR, event was " + name);

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
        return this.population;
    }

    @Override
    public void setPopulation(Population pop) {
        this.population = pop;
    }

    public String populationTipText() {
        return "Change the number of starting individuals stored (Cluster-HC).";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        Population tmp = new Population();
        tmp.addPopulation(archive);
        tmp.addPopulation(population);
        tmp.setFunctionCalls(population.getFunctionCalls());
        tmp.setGeneration(population.getGeneration());
//    	tmp = PostProcessInterim.clusterBest(tmp, sigma, 0, PostProcessInterim.KEEP_LONERS, PostProcessInterim.BEST_ONLY);
        return new SolutionSet(population, tmp);
    }

    /**
     * This method will return a string describing all properties of the
     * optimizer and the applied methods.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        StringBuffer sbuf = new StringBuffer("Clustering Hill Climbing");
        sbuf.append(", initial pop size: ");
        sbuf.append(getPopulation().getTargetSize());
        sbuf.append("Optimization Problem: ");
        sbuf.append(this.optimizationProblem.getStringRepresentationForProblem(this));
        sbuf.append(this.population.getStringRepresentation());
        return sbuf.toString();
    }

    @Override
    public String getName() {
        return "ClustHC-" + initialPopSize + "-" + localSearchMethod;
    }

    /**
     * @return the hcEvalCycle
     */
    public int getEvalCycle() {
        return hcEvalCycle;
    }

    /**
     * @param hcEvalCycle the hcEvalCycle to set
     */
    public void setEvalCycle(int hcEvalCycle) {
        this.hcEvalCycle = hcEvalCycle;
    }

    public String evalCycleTipText() {
        return "The number of evaluations between two clustering/adaption steps.";
    }

    /**
     * @return the initialPopSize
     */
    public int getInitialPopSize() {
        return initialPopSize;
    }

    /**
     * @param initialPopSize the initialPopSize to set
     */
    public void setInitialPopSize(int initialPopSize) {
        this.initialPopSize = initialPopSize;
    }

    public String initialPopSizeTipText() {
        return "Population size at the start and at reinitialization times.";
    }

    /**
     * @return the sigma
     */
    public double getSigmaClust() {
        return sigmaClust;
    }

    /**
     * @param sigma the sigma to set
     */
    public void setSigmaClust(double sigma) {
        this.sigmaClust = sigma;
    }

    public String sigmaClustTipText() {
        return "Defines the sigma distance parameter for density based clustering.";
    }

    /**
     * @return the notifyGuiEvery
     */
    public int getNotifyGuiEvery() {
        return notifyGuiEvery;
    }

    /**
     * @param notifyGuiEvery the notifyGuiEvery to set
     */
    public void setNotifyGuiEvery(int notifyGuiEvery) {
        this.notifyGuiEvery = notifyGuiEvery;
    }

    public String notifyGuiEveryTipText() {
        return "How often to notify the GUI to plot the fitness etc.";
    }

    /**
     * @return the minImprovement
     */
    public double getMinImprovement() {
        return minImprovement;
    }

    /**
     * @param minImprovement the minImprovement to set
     */
    public void setMinImprovement(double minImprovement) {
        this.minImprovement = minImprovement;
    }

    public String minImprovementTipText() {
        return "Improvement threshold below which the mutation step size is reduced or the population reinitialized.";
    }

    /**
     * @return the reinitForStepSize
     */
    public double getStepSizeThreshold() {
        return stepSizeThreshold;
    }

    /**
     * @param reinitForStepSize the reinitForStepSize to set
     */
    public void setStepSizeThreshold(double reinitForStepSize) {
        this.stepSizeThreshold = reinitForStepSize;
    }

    public String stepSizeThresholdTipText() {
        return "Threshold for the mutation step size below which the population is seen as converged and reinitialized.";
    }

    /**
     * @return the initialStepSize
     */
    public double getStepSizeInitial() {
        return initialStepSize;
    }

    /**
     * @param initialStepSize the initialStepSize to set
     */
    public void setStepSizeInitial(double initialStepSize) {
        this.initialStepSize = initialStepSize;
    }

    public String stepSizeInitialTipText() {
        return "Initial mutation step size for hill climbing, relative to the problem range.";
    }

    public PostProcessMethod getLocalSearchMethod() {
        return localSearchMethod;
    }

    public void setLocalSearchMethod(PostProcessMethod localSearchMethod) {
        this.localSearchMethod = localSearchMethod;
        GenericObjectEditor.setShowProperty(this.getClass(), "stepSizeInitial", localSearchMethod == PostProcessMethod.hillClimber);
        GenericObjectEditor.setShowProperty(this.getClass(), "stepSizeThreshold", localSearchMethod == PostProcessMethod.hillClimber);
    }

    public String localSearchMethodTipText() {
        return "Set the method to be used for the hill climbing as local search";
    }

    @Override
    public String[] getAdditionalDataHeader() {
        return new String[]{"numIndies", "sigma", "numArchived", "archivedMeanDist"};
    }

    @Override
    public String[] getAdditionalDataInfo() {
        return new String[]{"The current population size", "Current step size in case of stochastic HC", "Number of archived solutions", "Mean distance of archived solutions"};
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        return new Object[]{population.size(), mutator.getSigma(), archive.size(), archive.getPopulationMeasures()[0]};
    }

    public boolean isDoReinitialization() {
        return doReinitialization;
    }

    public void setDoReinitialization(boolean doReinitialization) {
        this.doReinitialization = doReinitialization;
        GenericObjectEditor.setShowProperty(this.getClass(), "minImprovement", doReinitialization);
    }

    public String doReinitializationTipText() {
        return "Activate reinitialization if no improvement was achieved.";
    }
}
