package eva2.problems;

import eva2.optimization.enums.PostProcessMethod;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.cluster.ClusteringDensityBased;
import eva2.optimization.operator.cluster.InterfaceClustering;
import eva2.optimization.operator.distancemetric.IndividualDataMetric;
import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.operator.moso.MOSONoConvert;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateESFixedStepSize;
import eva2.optimization.operator.postprocess.PostProcess;
import eva2.optimization.operator.postprocess.SolutionHistogram;
import eva2.optimization.operator.terminators.CombinedTerminator;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.operator.terminators.PhenotypeConvergenceTerminator;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.ChangeTypeEnum;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.DirectionTypeEnum;
import eva2.optimization.operator.terminators.PopulationMeasureTerminator.StagnationTypeEnum;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInterface;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.ToolBox;
import eva2.util.annotation.Parameter;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 *
 */
public abstract class AbstractOptimizationProblem implements InterfaceOptimizationProblem, Serializable {
    class EvalThread extends Thread {
        AbstractOptimizationProblem prob;
        AbstractEAIndividual ind;
        Population population;
        Semaphore semaphore;

        public EvalThread(AbstractOptimizationProblem prob, AbstractEAIndividual ind, Population pop, Semaphore sema) {
            this.ind = ind;
            this.prob = prob;
            this.population = pop;
            this.semaphore = sema;
        }

        @Override
        public void run() {
            prob.evaluate(ind);
            population.incrFunctionCalls();
            semaphore.release();
        }
    }

    /**
     * Tag for data fields concerning a solution to an abstract optimization problem.
     */
    public static final String STAT_SOLUTION_HEADER = "solution";
    /**
     * Store the old fitness array before evaluation.
     */
    public static final String OLD_FITNESS_KEY = "oldFitness";

    private int parallelThreads = 1;

    protected AbstractEAIndividual template = null;

    private double defaultAccuracy = 0.001; // default accuracy for identifying optima.

    protected int problemDimension = 10;

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public abstract Object clone();

    public int getParallelThreads() {
        return parallelThreads;
    }

    public void setParallelThreads(int parallelThreads) {
        this.parallelThreads = parallelThreads;
    }

    public String parallelThreadsTipText() {
        return "Set the number of threaded parallel function evaluations - interesting for slow functions and generational optimizers.";
    }

    /**
     * This method initializes the problem instance.
     * If you override it, make sure to call the super method!
     */
    @Override
    public abstract void initializeProblem();

    /**
     * This method inits a given population
     *
     * @param population The populations that is to be inited
     */
    @Override
    public abstract void initializePopulation(Population population);

    /**
     * This method evaluates a given population and set the fitness values
     * accordingly. It also keeps track of the function call count.
     *
     * @param population The population that is to be evaluated.
     */
    @Override
    public void evaluate(Population population) {
        AbstractEAIndividual tmpIndy;
        evaluatePopulationStart(population);

        if (this.parallelThreads > 1) {
            Semaphore sema = new Semaphore(0);
            ExecutorService pool = Executors.newFixedThreadPool(parallelThreads);
            int cntIndies = 0;
            for (; cntIndies < population.size(); cntIndies++) {
                AbstractEAIndividual tmpindy = (AbstractEAIndividual) population.get(cntIndies);
                tmpindy.resetConstraintViolation();
                EvalThread evalthread = new EvalThread(this, tmpindy, population, sema);
                pool.execute(evalthread);
            }
            try {
                sema.acquire(cntIndies);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Threading error in AbstractOptimizationProblem: " + e.getMessage());
            }
            pool.shutdownNow();
        } else {

            for (int i = 0; i < population.size(); i++) {
                tmpIndy = (AbstractEAIndividual) population.get(i);
                tmpIndy.putData(OLD_FITNESS_KEY, tmpIndy.getFitness());
                synchronized (tmpIndy) {
                    tmpIndy.resetConstraintViolation();
                    this.evaluate(tmpIndy);
                }
                population.incrFunctionCalls();
            }
        }

        evaluatePopulationEnd(population);
    }

    /**
     * Empty thunk for implementation in subclasses. This is called right before a population is evaluated.
     * Made public because some steady-state optimizers do not call evaluate(Population).
     *
     * @param population
     */
    public void evaluatePopulationStart(Population population) {}

    /**
     * Empty thunk for implementation in subclasses. This is called after a population was evaluated.
     *
     * @param population
     */
    public void evaluatePopulationEnd(Population population) {}

    /**
     * This method evaluate a single individual and sets the fitness values
     *
     * @param individual The individual that is to be evaluated
     */
    @Override
    public abstract void evaluate(AbstractEAIndividual individual);

    /**
     * The default initialization method. Clone the given template individual, calls its initialize method
     * and add it to the population until the target size is reached. Earlier individuals are removed.
     *
     * @param population
     * @param template
     * @param prob
     */
    public static void defaultInitializePopulation(Population population, AbstractEAIndividual template, InterfaceOptimizationProblem prob) {
        AbstractEAIndividual tmpIndy;
        population.clear();

        for (int i = 0; i < population.getTargetSize(); i++) {
            tmpIndy = (AbstractEAIndividual) template.clone();
            tmpIndy.initialize(prob);
            population.add(tmpIndy);
        }
        // population initialize must be last
        // it set's fitcalls and generation to zero
        population.init();
    }

    /**
     * This method allows you to output a string that describes a found solution
     * in a way that is most suitable for a given problem.
     *
     * @param individual The individual that is to be shown.
     * @return The description.
     */
    @Override
    public String getSolutionRepresentationFor(AbstractEAIndividual individual) {
        return AbstractEAIndividual.getDefaultStringRepresentation(individual);
    }

    /**
     * This method returns a double value that will be displayed in a fitness
     * plot. A fitness that is to be minimized with a global min of zero
     * would be best, since log y can be used. But the value can depend on the problem.
     *
     * @param pop The population that is to be refined.
     * @return Double value
     */
    @Override
    public Double getDoublePlotValue(Population pop) {
        return pop.getBestEAIndividual().getFitness(0);
    }

    /**
     * This method returns the header for the additional data that is to be written into a file
     *
     * @return String
     */
    @Override
    public String[] getAdditionalDataHeader() {
        String[] header = null;
        if (this instanceof InterfaceInterestingHistogram) {
            header = new String[]{STAT_SOLUTION_HEADER, "histogram", "score"};
        } else {
            header = new String[]{STAT_SOLUTION_HEADER};
        }

        header = (String[]) checkAndAppendAdd(0, getIndividualTemplate(), header, null);
        header = (String[]) checkAndAppendAdd(0, getIndividualTemplate().getCrossoverOperator(), header, null);
        header = (String[]) checkAndAppendAdd(0, getIndividualTemplate().getMutationOperator(), header, null);
        return header;
    }

    /**
     * Generic method to append additional information of another object.
     *
     * @param type indicate header (0), info (1), or value (2)
     * @param o    the object to retrieve data from
     * @param dat  the data array to which to append to
     * @param pop  the current population
     * @return
     */
    private static Object[] checkAndAppendAdd(int type, Object o, Object[] dat, PopulationInterface pop) {
        if (o instanceof InterfaceAdditionalPopulationInformer) {
            switch (type) {
                case 0: // header
                    return ToolBox.appendArrays((String[]) dat, ((InterfaceAdditionalPopulationInformer) o).getAdditionalDataHeader());
                case 1: // info
                    return ToolBox.appendArrays((String[]) dat, ((InterfaceAdditionalPopulationInformer) o).getAdditionalDataInfo());
                case 2: // value
                    return ToolBox.appendArrays(dat, ((InterfaceAdditionalPopulationInformer) o).getAdditionalDataValue(pop));
                default:
                    System.err.println("Error, invalid type in AbstractOptimizationProblem.appendAdd");
                    return dat;
            }
        } else {
            return dat;
        }
    }

    /**
     * This method returns the header for the additional data that is to be written into a file
     *
     * @return String
     */
    @Override
    public String[] getAdditionalDataInfo() {
        String[] info = null;
        if (this instanceof InterfaceInterestingHistogram) {
            info = new String[]{"Representation of the current best individual",
                    "Fitness histogram of the current population",
                    "Fitness threshold based score of the current population"};
        } else {
            info = new String[]{"Representation of the current best individual"};
        }

        info = (String[]) checkAndAppendAdd(1, getIndividualTemplate(), info, null);
        info = (String[]) checkAndAppendAdd(1, getIndividualTemplate().getCrossoverOperator(), info, null);
        info = (String[]) checkAndAppendAdd(1, getIndividualTemplate().getMutationOperator(), info, null);
        return info;
    }

    /**
     * This method returns the additional data that is to be written into a file
     *
     * @param pop The population that is to be refined.
     * @return String
     */
    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        String solStr = AbstractEAIndividual.getDefaultDataString(pop.getBestIndividual());
        Object[] vals = null;
        if (this instanceof InterfaceInterestingHistogram) {
            int fitCrit = 0;
            SolutionHistogram hist = ((InterfaceInterestingHistogram) this).getHistogram();
            if (pop.getBestFitness()[fitCrit] < hist.getUpperBound()) {
                Population maybeFiltered = (Population) pop;
                if (pop.size() > 100) { // for efficiency reasons...
                    maybeFiltered = maybeFiltered.filterByFitness(hist.getUpperBound(), fitCrit);
                }
                Population sols = PostProcess.clusterBestUpdateHistogram(maybeFiltered, this, hist, fitCrit, getDefaultAccuracy());
            }
            vals = new Object[]{solStr, hist, hist.getScore()};
        } else {
            vals = new Object[]{solStr};
        }

        vals = checkAndAppendAdd(2, pop.getBestIndividual(), vals, pop);
        vals = checkAndAppendAdd(2, ((AbstractEAIndividual) pop.getBestIndividual()).getCrossoverOperator(), vals, pop);
        vals = checkAndAppendAdd(2, ((AbstractEAIndividual) pop.getBestIndividual()).getMutationOperator(), vals, pop);
        return vals;
    }

    /**
     * Convenience method, draws with undefined generation and function evaluation count.
     *
     * @param indy
     * @return
     * @see #drawIndividual(int, int, AbstractEAIndividual)
     */
    public JComponent drawIndividual(AbstractEAIndividual indy) {
        return drawIndividual(-1, -1, indy);
    }

    /**
     * This method allows you to request a graphical representation for a given individual.
     * The additional informations generation and funCalls are shown if they are >= 0.
     * individual.
     *
     * @param generation generation of the individual or -1
     * @param funCalls   function calls performed or -1
     * @param indy       the individual to display
     * @return JComponent
     */
    @Override
    public JComponent drawIndividual(int generation, int funCalls, AbstractEAIndividual indy) {
        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());
        JTextArea area = new JTextArea();
        JScrollPane scroll = new JScrollPane(area);
        String text = "Best Solution:\n";
        if (generation >= 0) {
            text += " Generation: " + generation + "\n";
        }
        if (funCalls >= 0) {
            text += " Evaluations: " + funCalls + "\n";
        }
        text += this.getSolutionRepresentationFor(indy);
        area.setLineWrap(true);
        area.setText(text);
        area.setEditable(false);
        result.add(scroll, BorderLayout.CENTER);
        return result;
    }

    /**
     * This method will report whether or not this optimization problem is truly
     * multi-objective
     *
     * @return True if multi-objective, else false.
     */
    @Override
    public boolean isMultiObjective() {
        if (this instanceof AbstractMultiObjectiveOptimizationProblem) {
            if (((AbstractMultiObjectiveOptimizationProblem) this).getMOSOConverter() instanceof MOSONoConvert) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * For some evaluation cases it may be necessary to inform the problem class about the optimizer in use.
     *
     * @param opt
     */
    public void informAboutOptimizer(InterfaceOptimizer opt) {

    }

    /**
     * This allows "anyone" to access the problem's individual template and set operators etc.
     * Subclasses may have a method getEAIndividual additionally with a more
     * specific interface signature, which makes sense for the GUI which decides
     * on what classes to present to the user based on the interface signature.
     *
     * @return the problem's individual template
     */
    public AbstractEAIndividual getIndividualTemplate() {
        return template;
    }

    public String individualTemplateTipText() {
        return "Choose the individual representation to use.";
    }

    /**
     * This method extracts the individuals from a given population that are assumed to correspond to local or global optima.
     * Similar individuals are clustered together with a density based clustering method
     *
     * @param pop
     * @param epsilonPhenoSpace maximal allowed improvement of an individual before considered premature (given as distance in the search space)
     * @param epsilonFitConv    if positive: additional absolute convergence criterion (fitness space) as termination criterion of the local search
     * @param clusterSigma      minimum cluster distance
     * @param maxEvalsPerIndy   maximum number of evaluations or -1 to take the maximum
     * @return
     */
    public static Population extractPotentialOptima(AbstractOptimizationProblem prob, Population pop,
                                                    double epsilonPhenoSpace, double epsilonFitConv, double clusterSigma, int maxEvalsPerIndy) {
        Population potOptima = new Population();
        for (int i = 0; i < pop.size(); ++i) {
            AbstractEAIndividual indy = pop.getEAIndividual(i);
            boolean isConverged = AbstractOptimizationProblem.isPotentialOptimumNMS(prob, indy, epsilonPhenoSpace, epsilonFitConv, maxEvalsPerIndy);
            if (isConverged) {
                potOptima.addIndividual(indy);
                if (!indy.hasData(PostProcess.movedDistanceKey)) {
                    System.err.println("Error, missing distance information in individual (AbstractOptimizationProblem.extractPotentialOptimum)");
                }
            }
        }
        // cluster by the converged-to positions instead of the original ones
        InterfaceClustering clustering = new ClusteringDensityBased(clusterSigma, 2, new IndividualDataMetric(PostProcess.movedToPositionKey));
        clustering = new ClusteringDensityBased(clusterSigma, 2);
        if (clusterSigma > 0) {
            return (Population) PostProcess.clusterBest(potOptima, clustering, 0, PostProcess.KEEP_LONERS, PostProcess.BEST_ONLY).clone();
        } else {
            return potOptima;
        }
    }

    /**
     * This method estimates if the given individual is within epsilon of an optimum (local or global).
     * The algorithm tries to improve the given individual locally.
     * If it is possible to improve the individual such that its changed position is further than epsilon,
     * the individual is considered premature.
     * If not, the particle is assumed to correspond to a local or global optimum.
     *
     * @param orig             individual that is to be tested
     * @param epsilon          maximal allowed improvement before considered premature (given as distance in the search space)
     * @param mutationStepSize step size used to mutate the individual in one step
     *                         (if < 0 a default value of 0.0001 is used)
     * @param numOfFailures    number of unsuccessful improvement steps in a row before an individual is considered to be locally unimproveable
     *                         (if < 0 a default value of 100*problem dimensions is used )
     * @return estimation if the given individual is within epsilon of an optimum (local or global)
     */
    public boolean isPotentialOptimum(AbstractEAIndividual orig, double epsilon, double mutationStepSize, int numOfFailures) {
        int stepsCounter = 0; // could be used to limit the maximal number of steps overall

        // if not provided reasonable values use defaults:
        if (mutationStepSize < 0) {
            mutationStepSize = 0.0001;
        }
        if (numOfFailures < 0) {
            numOfFailures = 100 * AbstractEAIndividual.getDoublePositionShallow(this.template).length;
        } // scales the effort with the number of problem dimensions

        AbstractEAIndividual indy = (AbstractEAIndividual) orig.clone();
        this.evaluate(indy); // indy may be evaluated in a normalised way...

        InterfaceDistanceMetric metric = new PhenotypeMetric();
        double overallDist = 0;

        InterfaceMutation mutator = new MutateESFixedStepSize(mutationStepSize);

        for (int i = 0; i < numOfFailures; ++i) {
            // backup
            AbstractEAIndividual old = (AbstractEAIndividual) indy.clone();
            // mutate
            double tmpD = indy.getMutationProbability();
            indy.setMutationProbability(1.0);
            mutator.mutate(indy);
            ++stepsCounter;
            indy.setMutationProbability(tmpD);
            // evaluate
            this.evaluate(indy);

            if (old.isDominatingDebConstraints(indy)) {// indy could not be improved
                indy = (AbstractEAIndividual) old.clone();
            } else { // indy could be improved
                i = 0; // the given number of unsuccessful improvement steps should occur in a row
                overallDist = metric.distance(orig, indy);
                //System.out.println(overallDist);
            }
            if (overallDist > epsilon) {
                return false; // dont waste any more evaluations on this candidate
            }
        }
        return overallDist < epsilon;
    }

    /**
     * Refine a given individual using Nelder-Mead-Simplex local search. Return true, if the refined result is within a given
     * distance from the original individual in phenotype space. The maxEvaluations parameter gives the maximum evaluations
     * for the local search. Using the epsilonFitConv parameter one may define a convergence criterion as PhenotypeConvergenceTerminator
     * based on the given threshold and 100*dim evaluations, which is combined (using OR) with the evaluation counter.
     * If maxEvaluations is smaller than zero, a maximum of 500*dim evaluations is employed.
     * Be aware that this may cost quite some runtime depending on the target
     * function.
     * A double value for the distance by which it was moved is added to the individual using the key PostProcess.movedDistanceKey.
     *
     * @param orig
     * @param epsilonPhenoSpace allowed distance for a solution moved by local search
     * @param epsilonFitConv    if the fitness changes below the threshold, the search is stopped earlier. Set to -1 to deactivate.
     * @param maxEvaluations    maximal number of evaluations or -1
     * @return
     */
    public static boolean isPotentialOptimumNMS(AbstractOptimizationProblem prob, AbstractEAIndividual orig, double epsilonPhenoSpace, double epsilonFitConv, int maxEvaluations) {

        AbstractEAIndividual indy = (AbstractEAIndividual) orig.clone();
        prob.evaluate(indy); // indy may be evaluated in a normalised way...

        InterfaceDistanceMetric metric = new PhenotypeMetric();
        double overallDist = 0;
        double initRelPerturb = -1;
        int dim = -1;
        if (orig instanceof InterfaceDataTypeDouble) {
            initRelPerturb = epsilonPhenoSpace * 0.5;
            dim = ((InterfaceDataTypeDouble) orig).getDoubleRange().length;
            if (maxEvaluations < 0) {
                maxEvaluations = 500 * AbstractEAIndividual.getDoublePositionShallow(prob.template).length;
            } // scales the effort with the number of problem dimensions
        } else {
            System.err.println("Cannot initialize NMS on non-double valued individuals!");
            return false;
        }

        Population pop = new Population(1);
        pop.add(orig);
        InterfaceTerminator term = new EvaluationTerminator(maxEvaluations);
        if (epsilonFitConv > 0) {
            term = new CombinedTerminator(new PhenotypeConvergenceTerminator(epsilonFitConv, 100 * dim, StagnationTypeEnum.fitnessCallBased, ChangeTypeEnum.absoluteChange, DirectionTypeEnum.decrease), term, false);
        }
        int evalsPerf = PostProcess.processSingleCandidatesNMCMA(PostProcessMethod.nelderMead, pop, term, initRelPerturb, prob);
        overallDist = metric.distance(indy, pop.getBestEAIndividual());
        orig.putData(PostProcess.movedDistanceKey, overallDist);
        orig.putData(PostProcess.movedToPositionKey, pop.getBestEAIndividual().getDoublePosition());

        return (overallDist < epsilonPhenoSpace);
    }

    public double getDefaultAccuracy() {
        return defaultAccuracy;
    }

    @Parameter(name = "accuracy", description = "A default threshold to identify optima - e.g. the assumed minimal distance between any two optima.")
    public void setDefaultAccuracy(double defAcc) {
        defaultAccuracy = defAcc;
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "AbstractOptimizationProblem";
    }

    @Override
    public int getProblemDimension() {
        return this.problemDimension;
    }
}
