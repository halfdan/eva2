package eva2;

import eva2.optimization.enums.DEType;
import eva2.optimization.enums.MutateESCrossoverType;
import eva2.optimization.enums.PSOTopology;
import eva2.optimization.enums.PostProcessMethod;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.*;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.operator.archiving.ArchivingNSGAII;
import eva2.optimization.operator.archiving.InformationRetrievalInserting;
import eva2.optimization.operator.archiving.InterfaceArchiving;
import eva2.optimization.operator.archiving.InterfaceInformationRetrieval;
import eva2.optimization.operator.cluster.ClusteringDensityBased;
import eva2.optimization.operator.cluster.InterfaceClustering;
import eva2.optimization.operator.crossover.CrossoverESDefault;
import eva2.optimization.operator.crossover.InterfaceCrossover;
import eva2.optimization.operator.crossover.NoCrossover;
import eva2.optimization.operator.distancemetric.IndividualDataMetric;
import eva2.optimization.operator.mutation.*;
import eva2.optimization.operator.postprocess.InterfacePostProcessParams;
import eva2.optimization.operator.postprocess.PostProcessParams;
import eva2.optimization.operator.selection.InterfaceSelection;
import eva2.optimization.operator.selection.SelectBestIndividuals;
import eva2.optimization.operator.terminators.CombinedTerminator;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.PBILPopulation;
import eva2.optimization.population.Population;
import eva2.problems.AbstractOptimizationProblem;
import eva2.optimization.statistics.InterfaceStatistics;
import eva2.optimization.strategies.*;
import eva2.tools.math.RNG;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * <p> The OptimizerFactory allows quickly creating some optimizers without
 * thinking much about parameters. You can access a runnable Optimization thread
 * and directly start it, or access its fully prepared GOParameter instance,
 * change some parameters, and start it then. </p> <p> On the other hand this
 * class provides an almost complete list of all currently available
 * optimization procedures in EvA2. The arguments passed to the methods
 * initialize the respective optimization procedure. To perform an optimization
 * one has to do the following:
 * <code>
 * InterfaceOptimizer optimizer = OptimizerFactory.createCertainOptimizer(arguments);
 * EvaluationTerminator terminator = new EvaluationTerminator(numOfFitnessCalls);
 * while (!terminator.isTerminated(optimizer.getPopulation())) optimizer.optimize();
 * </code> </p>
 *
 * @author mkron
 * @author Andreas Dr&auml;ger
 * @version 0.1
 * @date 17.04.2007
 * @since 2.0
 */
public class OptimizerFactory {

    private static InterfaceTerminator userTerm = null;
    public final static int STD_ES = 1;
    public final static int CMA_ES = 2;
    public final static int STD_GA = 3;
    public final static int PSO = 4;
    public final static int DE = 5;
    public final static int TRIBES = 6;
    public final static int RANDOM = 7;
    public final static int HILLCL = 8;
    public final static int CBN_ES = 9;
    public final static int CL_HILLCL = 10;
    public final static int CMA_ES_IPOP = 11;
    public final static int CBN_GA = 12;
    public final static int PBIL = 13;
    public final static int MOGA = 14;
    public final static int defaultFitCalls = 10000;
    public final static int randSeed = 0;
    private static OptimizerRunnable lastRunnable = null;
    private static final int cbnDefaultHaltingWindowLength = new ClusterBasedNichingEA().getHaltingWindow();
    private static final double cbnDefaultHaltingWindowEpsilon = new ClusterBasedNichingEA().getEpsilonBound();
    private static final double cbnDefaultClusterSigma = 0.1;
    private static final int cbnDefaultMinGroupSize = 5;
    private static final int cbnDefaultMaxGroupSize = -1;

    /**
     * This method optimizes the given problem using differential evolution.
     *
     * @param problem
     * @param popsize
     * @param f
     * @param CR
     * @param lambda
     * @param listener
     * @return An optimization algorithm that performs differential evolution.
     */
    public static DifferentialEvolution createDifferentialEvolution(
            AbstractOptimizationProblem problem, int popsize, double f,
            double lambda, double CR,
            InterfacePopulationChangedEventListener listener) {

        problem.initializeProblem();

        setTemplateOperators(problem, new NoMutation(), 0, new NoCrossover(), 0);

        DifferentialEvolution de = new DifferentialEvolution();
        de.setProblem(problem);
        de.getPopulation().setTargetSize(popsize);
        de.setDEType(DEType.DE2_CurrentToBest);
        de.setDifferentialWeight(f);
        de.setCrossoverRate(CR);
        de.setLambda(lambda);
        de.addPopulationChangedEventListener(listener);
        de.initialize();

        if (listener != null) {
            listener.registerPopulationStateChanged(de.getPopulation(), "");
        }

        return de;
    }

    /**
     * This method performs the optimization using an Evolution strategy.
     *
     * @param mu
     * @param lambda
     * @param plus              if true this operator uses elitism otherwise a comma
     *                          strategy.
     * @param mutationoperator
     * @param pm
     * @param crossoveroperator
     * @param pc
     * @param selection         environmental selection operator
     * @param problem
     * @param listener
     * @return An optimization algorithm that employs an evolution strategy.
     */
    public static EvolutionStrategies createEvolutionStrategy(int mu,
                                                                    int lambda, boolean plus, InterfaceMutation mutationoperator,
                                                                    double pm, InterfaceCrossover crossoveroperator, double pc,
                                                                    InterfaceSelection selection, AbstractOptimizationProblem problem,
                                                                    InterfacePopulationChangedEventListener listener) {
        return createES(new EvolutionStrategies(mu, lambda, plus), mutationoperator, pm, crossoveroperator, pc, selection, problem, listener);
    }

    /**
     * This method initializes the optimization using an Evolution strategy with
     * increasing population size.
     *
     * @param mu
     * @param lambda
     * @param plus              if true this operator uses elitism otherwise a comma
     *                          strategy.
     * @param mutationoperator
     * @param pm
     * @param crossoveroperator
     * @param pc
     * @param incPopSizeFact    factor by which to inrease lambda ro restart,
     *                          default is 2
     * @param stagThresh        if the fitness changes below this value during a
     *                          stagnation phase, a restart is initiated
     * @param problem
     * @param listener
     * @return An optimization algorithm that employs an IPOP-ES.
     */
    public static EvolutionStrategyIPOP createEvolutionStrategyIPOP(int mu,
                                                                          int lambda, boolean plus, InterfaceMutation mutationoperator,
                                                                          double pm, InterfaceCrossover crossoveroperator, double pc, double incPopSizeFact, double stagThresh,
                                                                          AbstractOptimizationProblem problem, InterfacePopulationChangedEventListener listener) {
        EvolutionStrategyIPOP esIPOP = (EvolutionStrategyIPOP) createES(new EvolutionStrategyIPOP(mu, lambda, plus), mutationoperator, pm, crossoveroperator, pc, new SelectBestIndividuals(), problem, listener);
        esIPOP.setIncPopSizeFact(incPopSizeFact);
//		esIPOP.setStagnationGenerations(stagTimeGens);
        esIPOP.setStagThreshold(stagThresh);
        return esIPOP;
    }

    private static EvolutionStrategies createES(EvolutionStrategies theES, InterfaceMutation mutationoperator,
                                                      double pm, InterfaceCrossover crossoveroperator, double pc,
                                                      InterfaceSelection selection, AbstractOptimizationProblem problem,
                                                      InterfacePopulationChangedEventListener listener) {

        problem.initializeProblem();

        AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
        AbstractEAIndividual.setOperators(tmpIndi, mutationoperator, pm, crossoveroperator, pc);

        theES.addPopulationChangedEventListener(listener);
//		theES.setParentSelection(selection);
//		theES.setPartnerSelection(selection);
        theES.setEnvironmentSelection(selection);
        theES.setProblem(problem);
        theES.initialize();

        if (listener != null) {
            listener.registerPopulationStateChanged(theES.getPopulation(), "");
        }

        return theES;
    }

    /**
     * This method performs a Genetic Algorithm.
     *
     * @param mut
     * @param pm
     * @param cross
     * @param pc
     * @param select
     * @param popsize
     * @param problem
     * @param listener
     * @return An optimization algorithm that employes an genetic algorithm.
     */
    public static GeneticAlgorithm createGeneticAlgorithm(
            InterfaceMutation mut, double pm, InterfaceCrossover cross,
            double pc, InterfaceSelection select, int popsize,
            AbstractOptimizationProblem problem,
            InterfacePopulationChangedEventListener listener) {

        problem.initializeProblem();

        setTemplateOperators(problem, mut, pm, cross, pc);

        GeneticAlgorithm ga = new GeneticAlgorithm();
        ga.setProblem(problem);
        ga.getPopulation().setTargetSize(popsize);
        ga.setParentSelection(select);
        ga.setPartnerSelection(select);
        ga.addPopulationChangedEventListener(listener);
        ga.initialize();

        if (listener != null) {
            listener.registerPopulationStateChanged(ga.getPopulation(), "");
        }

        return ga;
    }

    /**
     * This method creates a multi-objective EA optimizer. Remember to set a
     * multi-objective selection method within the specific optimizer. This uses
     * a standard archiving strategy (NSGAII) and InformationRetrievalInserting.
     *
     * @param subOpt      the specific optimizer to use
     * @param archiveSize maximum size of the archive
     * @param problem
     * @param listener
     * @return An optimization algorithm that employs a multi-objective
     *         optimizer
     */
    public static MultiObjectiveEA createMultiObjectiveEA(
            InterfaceOptimizer subOpt, int archiveSize,
            AbstractOptimizationProblem problem,
            InterfacePopulationChangedEventListener listener) {

        return createMultiObjectiveEA(subOpt, new ArchivingNSGAII(),
                archiveSize, new InformationRetrievalInserting(), problem,
                listener);
    }

    /**
     * This method creates a multi-objective EA optimizer.
     *
     * @param problem
     * @return
     */
    public static OptimizationParameters standardMOGA(AbstractOptimizationProblem problem) {
        OptimizationParameters gaParams = standardGA(problem);
        int archiveSize = 100;
        int popSize = 100;
        MultiObjectiveEA moga = createMultiObjectiveEA(gaParams.getOptimizer(), archiveSize, problem, null);
        return makeParams(moga, popSize, problem, randSeed, makeDefaultTerminator());
    }

    /**
     * This method creates a multi-objective EA optimizer. Remember to set a
     * multi-objective selection method within the specific optimizer.
     *
     * @param subOpt        the specific optimizer to use
     * @param archiving     the archiving strategy collecting the pareto front
     * @param archiveSize   maximum size of the archive
     * @param infoRetrieval information retrieval strategy
     * @param problem
     * @param listener
     * @return An optimization algorithm that employs a multi-objective
     *         optimizer
     */
    public static MultiObjectiveEA createMultiObjectiveEA(
            InterfaceOptimizer subOpt, InterfaceArchiving archiving,
            int archiveSize, InterfaceInformationRetrieval infoRetrieval,
            AbstractOptimizationProblem problem,
            InterfacePopulationChangedEventListener listener) {

        problem.initializeProblem();
        subOpt.setProblem(problem);

        return new MultiObjectiveEA(subOpt, archiving, archiveSize,
                infoRetrieval, problem);
    }

    /**
     * This starts a Gradient Descent.
     *
     * @param problem
     * @return An optimization algorithm that performs gradient descent.
     */
    public static GradientDescentAlgorithm createGradientDescent(
            AbstractOptimizationProblem problem) {

        System.err.println("Currently not implemented!");

        problem.initializeProblem();

        AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
        tmpIndi.setCrossoverOperator(new NoCrossover());
        tmpIndi.setCrossoverProbability(0.0);

        GradientDescentAlgorithm gd = new GradientDescentAlgorithm();

        // TODO implement!

        return gd;
    }

    /**
     * This method creates a Hill Climber algorithm with a default fixed-size
     * mutation.
     *
     * @param pop      The size of the population
     * @param problem  The problem to be optimized
     * @param listener
     * @return An optimization procedure that performs hill climbing.
     */
    public static HillClimbing createHillClimber(int popSize,
                                                 AbstractOptimizationProblem problem,
                                                 InterfacePopulationChangedEventListener listener) {
        return createHillClimber(popSize, new MutateESFixedStepSize(0.2), problem, listener);
    }

    /**
     * This method creates a Hill Climber algorithm.
     *
     * @param popSize  The size of the population
     * @param mutator  A mutator for the Hill-Climber
     * @param problem  The problem to be optimized
     * @param listener
     * @return An optimization procedure that performs hill climbing.
     */
    public static HillClimbing createHillClimber(int popSize, InterfaceMutation mutator,
                                                 AbstractOptimizationProblem problem,
                                                 InterfacePopulationChangedEventListener listener) {

        problem.initializeProblem();

        setTemplateOperators(problem, mutator, 1., new NoCrossover(), 0);

        HillClimbing hc = new HillClimbing();
        hc.getPopulation().setTargetSize(popSize);
        hc.addPopulationChangedEventListener(listener);
        hc.setProblem(problem);
        hc.initialize();

        if (listener != null) {
            listener.registerPopulationStateChanged(hc.getPopulation(), "");
        }

        return hc;
    }

    /**
     * This method performs a Monte Carlo Search with the given number of
     * fitness calls.
     *
     * @param problem
     * @param popsize
     * @param listener
     * @return An optimization procedure that performes the random walk.
     */
    public static MonteCarloSearch createMonteCarlo(
            AbstractOptimizationProblem problem, int popsize,
            InterfacePopulationChangedEventListener listener) {

        problem.initializeProblem();
        setTemplateOperators(problem, new NoMutation(), 0, new NoCrossover(), 0);

        MonteCarloSearch mc = new MonteCarloSearch();
        mc.getPopulation().setTargetSize(popsize);
        mc.addPopulationChangedEventListener(listener);
        mc.setProblem(problem);
        mc.initialize();

        if (listener != null) {
            listener.registerPopulationStateChanged(mc.getPopulation(), "");
        }

        return mc;
    }

    /**
     * This method performs a particle swarm optimization. Standard topologies
     * are linear, grid and star.
     *
     * @param problem
     * @param mut
     * @param popsize
     * @param phi1
     * @param phi2
     * @param speedLim
     * @param listener
     * @param topology
     * @return An optimization algorithm that performs particle swarm
     *         optimization.
     * @see ParticleSwarmOpimization
     */
    public static ParticleSwarmOptimization createParticleSwarmOptimization(
            AbstractOptimizationProblem problem, int popsize, double phi1,
            double phi2, double speedLim, PSOTopology selectedTopology, int topologyRange,
            InterfacePopulationChangedEventListener listener) {

        problem.initializeProblem();

        setTemplateOperators(problem, new NoMutation(), 0, new NoCrossover(), 0);

        ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
        pso.setProblem(problem);
        pso.getPopulation().setTargetSize(popsize);
        pso.setPhi1(phi1);
        pso.setPhi2(phi2);
        pso.setSpeedLimit(speedLim);
        pso.setTopology(selectedTopology);
        pso.setTopologyRange(topologyRange);
        pso.addPopulationChangedEventListener(listener);
        pso.initialize();

        if (listener != null) {
            listener.registerPopulationStateChanged(pso.getPopulation(), "");
        }

        return pso;
    }

    /**
     * This method performs a Simulated Annealing Optimization and prints the
     * result as R output. It uses real valued individuals. The mutation
     * probability is always 1.0.
     *
     * @param problem
     * @param popsize
     * @param alpha       The parameter for the linear cooling
     * @param temperature The initial temperature
     * @param mut
     * @param listener
     * @return Returns an optimizer that performs simulated annealing.
     */
    public static SimulatedAnnealing createSimulatedAnnealing(
            AbstractOptimizationProblem problem, int popsize, double alpha,
            double temperature, InterfaceMutation mut,
            InterfacePopulationChangedEventListener listener) {

        problem.initializeProblem();

        setTemplateOperators(problem, mut, 1, new NoCrossover(), 0);

        SimulatedAnnealing sa = new SimulatedAnnealing();
        sa.setAlpha(alpha);
        sa.setInitialTemperature(temperature);
        sa.setProblem(problem);
        sa.getPopulation().setTargetSize(popsize);
        sa.addPopulationChangedEventListener(listener);
        sa.initialize();

        if (listener != null) {
            listener.registerPopulationStateChanged(sa.getPopulation(), "");
        }

        return sa;
    }

    /**
     * Calling initialize here makes problems when using the Matlab interface.
     *
     * @param learningRate
     * @param mutateSigma
     * @param mutationRate
     * @param positiveSamples
     * @param selection
     * @param popsize
     * @param problem
     * @param listener
     * @return
     */
    public static PopulationBasedIncrementalLearning createPBIL(
            double learningRate, double mutateSigma, double mutationRate,
            int positiveSamples, InterfaceSelection selection, int popsize,
            AbstractOptimizationProblem problem, InterfacePopulationChangedEventListener listener) {
        problem.initializeProblem();
        PopulationBasedIncrementalLearning pbil = new PopulationBasedIncrementalLearning();

        pbil.setLearningRate(learningRate);
        pbil.setMutateSigma(mutateSigma);
        pbil.setMutationRate(mutationRate);
        pbil.setPopulation(new PBILPopulation(popsize));
        pbil.setSelectionMethod(selection);
        pbil.setPositiveSamples(positiveSamples);

        pbil.addPopulationChangedEventListener(listener);
        pbil.setProblem(problem);

        if (listener != null) {
            listener.registerPopulationStateChanged(pbil.getPopulation(), "");
        }

        return pbil;
    }

    // /////////////////////////// Termination criteria
    public static InterfaceTerminator makeDefaultTerminator() {
        return new EvaluationTerminator(defaultFitCalls);
    }

    /**
     * The default Terminator finishes after n fitness calls, the default n is
     * returned here.
     *
     * @return the default number of fitness call done before termination
     */
    public static int getDefaultFitCalls() {
        return defaultFitCalls;
    }

    // /////////////////////////// constructing a default OptimizerRunnable

    /**
     * For an optimizer identifier, return the corresponding default parameter
     * set including initialization (thats why the problem is required).
     *
     * @param optType optimizer identifier
     * @param problem corresponding optimization problem
     */
    public static OptimizationParameters getParams(final int optType,
                                                   AbstractOptimizationProblem problem) {
        switch (optType) {
            case STD_ES:
                return standardES(problem);
            case CMA_ES:
                return cmaES(problem);
            case STD_GA:
                return standardGA(problem);
            case PSO:
                return standardPSO(problem);
            case DE:
                return standardDE(problem);
            case TRIBES:
                return tribes(problem);
            case RANDOM:
                return monteCarlo(problem);
            case HILLCL:
                return hillClimbing(problem);
            case CBN_ES:
                return standardCbnES(problem);
            case CL_HILLCL:
                return stdClusteringHillClimbing(problem);
            case CMA_ES_IPOP:
                return cmaESIPOP(problem);
            case CBN_GA:
                return standardCbnGA(problem);
            case PBIL:
                return standardPBIL(problem);
            case MOGA:
                return standardMOGA(problem);
            default:
                System.err.println("Error: optimizer type " + optType
                        + " is unknown!");
                return null;
        }
    }

    /**
     * Return a simple String showing the accessible optimizers. For external
     * access."
     *
     * @return a String listing the accessible optimizers
     */
    public static String showOptimizers() {
        return STD_ES + ": Standard ES \n" + CMA_ES + ": CMA-ES \n" + STD_GA + ": GA \n" + PSO + ": PSO \n" + DE + ": DE \n" + TRIBES + ": Tribes \n" + RANDOM + ": Random (Monte Carlo) "
                + "\n" + HILLCL + ": Hill-Climbing \n" + CBN_ES + ": Cluster-based niching ES \n" + CL_HILLCL + ": Clustering Hill-Climbing \n" + CMA_ES_IPOP + ": IPOP-CMA-ES "
                + "\n" + CBN_GA + ": Cluster-based niching GA \n" + PBIL + ": PBIL \n" + MOGA + ": MOGA, a Multi-Objective Genetic Algorithm";
    }

    /**
     * Produce a runnable optimizer from a strategy identifier, a problem
     * instance and with a given number of fitness calls to be performed. Output
     * is written to a file if the prefix String is given.
     *
     * @param optType
     * @param problem
     * @param fitCalls
     * @param outputFilePrefix
     * @return a runnable optimizer
     */
    public static OptimizerRunnable getOptRunnable(final int optType,
                                                   AbstractOptimizationProblem problem, int fitCalls,
                                                   String outputFilePrefix) {
        return getOptRunnable(optType, problem, new EvaluationTerminator(fitCalls), outputFilePrefix);
    }

    /**
     * Produce a runnable optimizer from a strategy identifier, a problem
     * instance and with a given terminator. Output is written to a file if the
     * prefix String is given. If the terminator is null the current
     * user-defined terminator will be used and if none is set, the default
     * number of fitness calls will be performed.
     *
     * @param optType
     * @param problem
     * @param terminator
     * @param outputFilePrefix
     * @return a runnable optimizer
     */
    public static OptimizerRunnable getOptRunnable(final int optType,
                                                   AbstractOptimizationProblem problem, InterfaceTerminator terminator,
                                                   String outputFilePrefix) {
        OptimizerRunnable opt = null;
        OptimizationParameters params = getParams(optType, problem);
        if (params != null) {
            opt = new OptimizerRunnable(params, outputFilePrefix);
            if (terminator != null) {
                opt.getOptimizationParameters().setTerminator(terminator);
            } else {
                opt.getOptimizationParameters().setTerminator(getTerminator());
            }
        }
        return opt;
    }

    /**
     * Produce a runnable optimizer from a OptimizationParameters instance. Output is
     * written to a file if the prefix String is given.
     *
     * @param params
     * @param outputFilePrefix
     * @return a runnable optimizer
     */
    public static OptimizerRunnable getOptRunnable(OptimizationParameters params, String outputFilePrefix) {
        return new OptimizerRunnable(params, outputFilePrefix);
    }

    // /////////////////////////// constructing a default OptimizerRunnable

    /**
     * Produce a runnable optimizer from a strategy identifier, a problem
     * instance and with the current static terminator in use. Output is written
     * to a file if the prefix String is given.
     *
     * @param optType
     * @param problem
     * @param outputFilePrefix
     * @return a runnable optimizer
     * @see #getOptRunnable(int, AbstractOptimizationProblem, int, String)
     */
    public static OptimizerRunnable getOptRunnable(final int optType,
                                                   AbstractOptimizationProblem problem, String outputFilePrefix) {
        return getOptRunnable(optType, problem, getTerminator(), outputFilePrefix);
    }

    /**
     * Return the current user-defined terminator or, if none was set, the
     * default terminator.
     *
     * @return the current default terminator
     */
    public static InterfaceTerminator getTerminator() {
        if (OptimizerFactory.userTerm != null) {
            return OptimizerFactory.userTerm;
        } else {
            return makeDefaultTerminator();
        }
    }

    /**
     * Return the number of evaluations performed during the last run or -1 if
     * unavailable.
     *
     * @return the number of evaluations performed during the last run or -1
     */
    public static int lastEvalsPerformed() {
        return (lastRunnable != null) ? lastRunnable.getProgress() : -1;
    }

    // /////////////////////// Creating default strategies

    /**
     * Use lambda, default random seed and terminator to produce OptimizationParameters.
     *
     * @param es
     * @param problem
     * @return
     */
    public static OptimizationParameters makeESParams(EvolutionStrategies es,
                                                      AbstractOptimizationProblem problem) {
        return makeParams(es, es.getLambda(), problem, randSeed, getTerminator());
    }

    /**
     * Use default random seed and terminator for a parameter set.
     *
     * @param opt
     * @param popSize
     * @param problem
     * @return
     * @see #makeParams(InterfaceOptimizer, int, AbstractOptimizationProblem,
     *      long, InterfaceTerminator)
     */
    public static OptimizationParameters makeParams(InterfaceOptimizer opt, int popSize, AbstractOptimizationProblem problem) {
        return makeParams(opt, popSize, problem, randSeed, getTerminator());
    }

    /**
     * Use default random seed and the population size of the optimizer.
     *
     * @param opt
     * @param popSize
     * @param problem
     * @return
     * @see #makeParams(InterfaceOptimizer, int, AbstractOptimizationProblem,
     *      long, InterfaceTerminator)
     */
    public static OptimizationParameters makeParams(InterfaceOptimizer opt, AbstractOptimizationProblem problem, InterfaceTerminator term) {
        return makeParams(opt, opt.getPopulation().getTargetSize(), problem, randSeed, term);
    }

    /**
     * Set the population size, initialize the population and return a parameter
     * structure containing all given parts.
     *
     * @param opt
     * @param popSize
     * @param problem
     * @param seed
     * @param term
     * @return
     * @see #makeParams(InterfaceOptimizer, Population,
     *      AbstractOptimizationProblem, long, InterfaceTerminator)
     */
    public static OptimizationParameters makeParams(InterfaceOptimizer opt,
                                                    int popSize, AbstractOptimizationProblem problem, long seed,
                                                    InterfaceTerminator term) {
        Population pop = new Population(popSize);
        RNG.setRandomSeed(seed);
        problem.initializePopulation(pop);
        return makeParams(opt, pop, problem, seed, term);
    }

    /**
     * Create a OptimizationParameters instance and prepare it with the given arguments.
     * The result can be modified and then used to create an OptimizerRunnable,
     * which of course can simply be run.
     *
     * @param opt
     * @param pop
     * @param problem
     * @param seed
     * @param term
     * @return
     * @see OptimizerRunnable
     */
    public static OptimizationParameters makeParams(InterfaceOptimizer opt,
                                                    Population pop, AbstractOptimizationProblem problem, long seed,
                                                    InterfaceTerminator term) {
        OptimizationParameters params = new OptimizationParameters();
        params.setProblem(problem);
        opt.setProblem(problem);
        opt.setPopulation(pop);
        params.setOptimizer(opt);
        params.setTerminator(term);
        params.setSeed(seed);
        return params;
    }

    public static OptimizerRunnable optimize(final int optType,
                                             AbstractOptimizationProblem problem, String outputFilePrefix) {
        return optimize(getOptRunnable(optType, problem, outputFilePrefix));
    }

    public static OptimizerRunnable optimize(OptimizerRunnable runnable) {
        if (runnable == null) {
            return null;
        }
        runnable.run();
        lastRunnable = runnable;
        return runnable;
    }

    /**
     * Create a runnable optimization Runnable and directly start it in an own
     * thread. The Runnable will notify waiting threads and set the isFinished
     * flag when the optimization is complete. If the optType is invalid, null
     * will be returned.
     *
     * @param optType
     * @param problem
     * @param outputFilePrefix
     * @return the OptimizerRunnable instance just started
     */
    public static OptimizerRunnable optimizeInThread(final int optType, AbstractOptimizationProblem problem, String outputFilePrefix) {
        return optimizeInThread(getOptRunnable(optType, problem, outputFilePrefix));
    }

    /**
     * Create a runnable optimization Runnable and directly start it in an own
     * thread. The Runnable will notify waiting threads and set the isFinished
     * flag when the optimization is complete. If the optType is invalid, null
     * will be returned.
     *
     * @param params
     * @param outputFilePrefix
     * @return the OptimizerRunnable instance just started
     */
    public static OptimizerRunnable optimizeInThread(OptimizationParameters params, String outputFilePrefix) {
        return optimizeInThread(new OptimizerRunnable(params, outputFilePrefix));
    }

    /**
     * Start a runnable optimizer in a concurrent thread.
     *
     * @param runnable
     * @return the started runnable
     */
    public static OptimizerRunnable optimizeInThread(OptimizerRunnable runnable) {
        if (runnable != null) {
            new Thread(runnable).start();
            lastRunnable = runnable;
        }
        return runnable;
    }

    // ///////////////////////////// Optimize a given parameter instance
    public static BitSet optimizeToBinary(OptimizationParameters params,
                                          String outputFilePrefix) {
        OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
                outputFilePrefix));
        return runnable.getBinarySolution();
    }

    // ///////////////////////////// Optimize using a default strategy
    public static BitSet optimizeToBinary(final int optType,
                                          AbstractOptimizationProblem problem) {
        return optimizeToBinary(optType, problem, null);
    }

    // ///////////////////////////// Optimize using a default strategy
    public static BitSet optimizeToBinary(final int optType,
                                          AbstractOptimizationProblem problem, String outputFilePrefix) {
        OptimizerRunnable runnable = optimize(optType, problem,
                outputFilePrefix);
        return (runnable != null) ? runnable.getBinarySolution() : null;
    }

    // ///////////////////////////// Optimize a given runnable
    public static BitSet optimizeToBinary(OptimizerRunnable runnable) {
        optimize(runnable);
        return (runnable != null) ? runnable.getBinarySolution() : null;
    }

    public static double[] optimizeToDouble(OptimizationParameters params,
                                            String outputFilePrefix) {
        OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
                outputFilePrefix));
        return runnable.getDoubleSolution();
    }

    public static double[] optimizeToDouble(OptimizationParameters params) {
        OptimizerRunnable runnable = optimize(new OptimizerRunnable(params, false));
        return runnable.getDoubleSolution();
    }

    public static double[] optimizeToDouble(OptimizationParameters params, InterfaceStatistics stats) {
        OptimizerRunnable runnable = optimize(new OptimizerRunnable(params, stats,
                false));
        return runnable.getDoubleSolution();
    }

    public static double[] optimizeToDouble(final int optType, AbstractOptimizationProblem problem) {
        return optimizeToDouble(optType, problem, null);
    }

    public static double[] optimizeToDouble(final int optType,
                                            AbstractOptimizationProblem problem, String outputFilePrefix) {
        OptimizerRunnable runnable = optimize(optType, problem, outputFilePrefix);
        return (runnable != null) ? runnable.getDoubleSolution() : null;
    }

    public static double[] optimizeToDouble(OptimizerRunnable runnable) {
        optimize(runnable);
        return (runnable != null) ? runnable.getDoubleSolution() : null;
    }

    public static IndividualInterface optimizeToInd(OptimizationParameters params) {
        OptimizerRunnable runnable = optimize(new OptimizerRunnable(params, false));
        return runnable.getResult();
    }

    public static IndividualInterface optimizeToInd(OptimizationParameters params, String outputFilePrefix) {
        OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
                outputFilePrefix));
        return runnable.getResult();
    }

    public static IndividualInterface optimizeToInd(final int optType,
                                                    AbstractOptimizationProblem problem) {
        return optimizeToInd(optType, problem, null);
    }

    public static IndividualInterface optimizeToInd(final int optType,
                                                    AbstractOptimizationProblem problem, String outputFilePrefix) {
        OptimizerRunnable runnable = optimize(optType, problem,
                outputFilePrefix);
        return (runnable != null) ? runnable.getResult() : null;
    }

    public static IndividualInterface optimizeToInd(OptimizerRunnable runnable) {
        optimize(runnable);
        return (runnable != null) ? runnable.getResult() : null;
    }

    public static Population optimizeToPop(OptimizationParameters params,
                                           String outputFilePrefix) {
        OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
                outputFilePrefix));
        return runnable.getResultPopulation();
    }

    public static Population optimizeToPop(final int optType,
                                           AbstractOptimizationProblem problem, String outputFilePrefix) {
        OptimizerRunnable runnable = optimize(optType, problem,
                outputFilePrefix);
        return (runnable != null) ? runnable.getResultPopulation() : null;
    }

    public static Population optimizeToPop(OptimizerRunnable runnable) {
        optimize(runnable);
        return (runnable != null) ? runnable.getResultPopulation() : null;
    }

    ///////////////////////////// post processing
    public static Population postProcess(PostProcessMethod method, int steps, double sigma, int nBest) {
        return (lastRunnable == null) ? null : postProcess(lastRunnable,
                new PostProcessParams(method, steps, sigma, nBest));
    }

    public static Population postProcess(InterfacePostProcessParams ppp) {
        return (lastRunnable == null) ? null : postProcess(lastRunnable, ppp);
    }

    /**
     * Post process the given runnable with given parameters. The runnable will
     * not be stored.
     *
     * @param runnable
     * @param steps
     * @param sigma
     * @param nBest
     * @return
     */
    public static Population postProcess(OptimizerRunnable runnable, int steps,
                                         double sigma, int nBest) {
        PostProcessParams ppp = new PostProcessParams(steps, sigma, nBest);
        return postProcess(runnable, ppp);
    }

    /**
     * Post process the given runnable with given parameters. The runnable will
     * not be stored.
     *
     * @param runnable
     * @param ppp
     * @return
     */
    public static Population postProcess(OptimizerRunnable runnable,
                                         InterfacePostProcessParams ppp) {
        runnable.setDoRestart(true);
        runnable.setDoPostProcessOnly(true);
        runnable.setPostProcessingParams(ppp);
        runnable.run(); // this run will not set the lastRunnable -
        // postProcessing
        // starts always anew
        return runnable.getResultPopulation();
    }

    public static List<BitSet> postProcessBinVec(int steps, double sigma,
                                                 int nBest) {
        return (lastRunnable != null) ? postProcessBinVec(lastRunnable,
                new PostProcessParams(steps, sigma, nBest)) : null;
    }

    public static List<BitSet> postProcessBinVec(
            InterfacePostProcessParams ppp) {
        return (lastRunnable != null) ? postProcessBinVec(lastRunnable, ppp)
                : null;
    }

    public static List<BitSet> postProcessBinVec(OptimizerRunnable runnable,
                                                 int steps, double sigma, int nBest) {
        return postProcessBinVec(runnable, new PostProcessParams(steps, sigma,
                nBest));
    }

    /**
     * Post process the given runnable with given parameters. Return the
     * solution set as a vector of BitSets. The runnable will not be stored.
     *
     * @param runnable
     * @param ppp
     * @return
     */
    public static List<BitSet> postProcessBinVec(OptimizerRunnable runnable,
                                                 InterfacePostProcessParams ppp) {
        Population resPop = postProcess(runnable, ppp);
        List<BitSet> ret = new ArrayList<>(resPop.size());
        for (Object o : resPop) {
            if (o instanceof InterfaceDataTypeBinary) {
                InterfaceDataTypeBinary indy = (InterfaceDataTypeBinary) o;
                ret.add(indy.getBinaryData());
            }
        }
        return ret;
    }

    public static List<double[]> postProcessDblVec(int steps, double sigma,
                                                   int nBest) {
        return (lastRunnable == null) ? null : postProcessDblVec(lastRunnable,
                new PostProcessParams(steps, sigma, nBest));
    }

    public static List<double[]> postProcessDblVec(
            InterfacePostProcessParams ppp) {
        return (lastRunnable != null) ? postProcessDblVec(lastRunnable, ppp)
                : null;
    }

    public static List<double[]> postProcessDblVec(
            OptimizerRunnable runnable, int steps, double sigma, int nBest) {
        return postProcessDblVec(runnable, new PostProcessParams(steps, sigma,
                nBest));
    }

    /**
     * Post process the given runnable with given parameters. Return the
     * solution set as a vector of double arrays. The runnable will not be
     * stored.
     *
     * @param runnable
     * @param ppp
     * @return
     */
    public static List<double[]> postProcessDblVec(
            OptimizerRunnable runnable, InterfacePostProcessParams ppp) {
        Population resPop = postProcess(runnable, ppp);
        List<double[]> ret = new ArrayList<>(resPop.size());
        for (Object o : resPop) {
            if (o instanceof InterfaceDataTypeDouble) {
                InterfaceDataTypeDouble indy = (InterfaceDataTypeDouble) o;
                ret.add(indy.getDoubleData());
            }
        }
        return ret;
    }

    public static List<AbstractEAIndividual> postProcessIndVec(int steps,
                                                               double sigma, int nBest) {
        return (lastRunnable != null) ? postProcessIndVec(lastRunnable,
                new PostProcessParams(steps, sigma, nBest)) : null;
    }

    public static List<AbstractEAIndividual> postProcessIndVec(
            InterfacePostProcessParams ppp) {
        return (lastRunnable != null) ? postProcessIndVec(lastRunnable, ppp)
                : null;
    }

    public static List<AbstractEAIndividual> postProcessIndVec(
            OptimizerRunnable runnable, int steps, double sigma, int nBest) {
        return postProcessIndVec(runnable, new PostProcessParams(steps, sigma,
                nBest));
    }

    /**
     * Post process the given runnable with given parameters. Return the
     * solution set as a vector of AbstractEAIndividuals. The runnable will not
     * be stored.
     *
     * @param runnable
     * @param ppp
     * @return
     */
    public static List<AbstractEAIndividual> postProcessIndVec(
            OptimizerRunnable runnable, InterfacePostProcessParams ppp) {
        Population resPop = postProcess(runnable, ppp);
        List<AbstractEAIndividual> ret = new ArrayList<>(
                resPop.size());
        for (Object o : resPop) {
            if (o instanceof AbstractEAIndividual) {
                AbstractEAIndividual indy = (AbstractEAIndividual) o;
                ret.add(indy);
            }
        }
        return ret;
    }

    ///////////////////////////// termination management

    /**
     * Replace the current user-defined terminator by the given one.
     *
     * @param term
     */
    public static void setTerminator(InterfaceTerminator term) {
        OptimizerFactory.userTerm = term;
    }

    /**
     * Add a new InterfaceTerminator to the current user-defined optimizer in a
     * boolean combination. The old and the given terminator will be combined as
     * in (TOld && TNew) if bAnd is true, and as in (TOld || TNew) if bAnd is
     * false. If there was no user-defined terminator (or it was set to null)
     * the new one is used without conjunction.
     *
     * @param newTerm a new InterfaceTerminator instance
     * @param bAnd    indicate the boolean combination
     */
    public static void addTerminator(InterfaceTerminator newTerm, boolean bAnd) {
        if (OptimizerFactory.userTerm == null) {
            OptimizerFactory.userTerm = newTerm;
        } else {
            setTerminator(new CombinedTerminator(OptimizerFactory.userTerm,
                    newTerm, bAnd));
        }
    }

    /**
     * Convenience method setting an EvaluationTerminator with the given number
     * of evaluations.
     *
     * @param maxEvals
     */
    public static void setEvaluationTerminator(int maxEvals) {
        setTerminator(new EvaluationTerminator(maxEvals));
    }

    /**
     * Return the termination message of the last runnable, if available.
     *
     * @return
     */
    public static String terminatedBecause() {
        return (lastRunnable != null) ? lastRunnable.terminatedBecause() : null;
    }

    ///////////////////////////// default parameters

    /**
     * Create a standard multi-start hill-climber parameter set with 50 initial
     * individuals.
     *
     * @return a standard multi-start hill-climber
     */
    public static OptimizationParameters hillClimbing(
            AbstractOptimizationProblem problem) {
        return hillClimbing(problem, 50);
    }

    /**
     * Create a standard multi-start hill-climber parameter set with the given
     * number of individuals.
     *
     * @return a standard multi-start hill-climber
     */
    public static OptimizationParameters hillClimbing(
            AbstractOptimizationProblem problem, int popSize) {
        return makeParams(new HillClimbing(), popSize, problem, randSeed, getTerminator());
    }

    public static OptimizationParameters monteCarlo(
            AbstractOptimizationProblem problem) {
        return makeParams(new MonteCarloSearch(), 50, problem, randSeed, getTerminator());
    }

    /**
     * Create a generic Clustering-based Niching EA with given parameters. Uses
     * ClusteringDensityBased as a default clustering algorithm.
     *
     * @param problem
     * @param opt
     * @param clusterSigma
     * @param minClustSize
     * @param haltingWindowLength
     * @param haltingWindowEpsilon
     * @param popSize
     * @return
     */
    public static OptimizationParameters createCbn(AbstractOptimizationProblem problem, InterfaceOptimizer opt,
                                                   double clusterSigma, int minClustSize, int maxSpecSize, int haltingWindowLength, double haltingWindowEpsilon, int popSize) {
        return createCbn(problem, opt, new ClusteringDensityBased(clusterSigma, minClustSize), maxSpecSize,
                new ClusteringDensityBased(clusterSigma, minClustSize), haltingWindowLength, haltingWindowEpsilon, popSize);
    }

    /**
     * A standard CBNPSO with density based clustering working on personal best
     * positions.
     *
     * @param problem
     * @return
     */
    public static OptimizationParameters standardCbnPSO(AbstractOptimizationProblem problem) {
        return createCbnPSO(problem, cbnDefaultClusterSigma, cbnDefaultMinGroupSize, cbnDefaultMaxGroupSize,
                cbnDefaultHaltingWindowLength, cbnDefaultHaltingWindowEpsilon, 100);
    }

    public static OptimizationParameters createCbnPSO(AbstractOptimizationProblem problem, double clusterSigma, int minClustSize,
                                                      int maxSpecSize, int haltingWindowLength, double haltingWindowEpsilon, int popSize) {
        OptimizationParameters psoParams = standardPSO(problem);
        ParticleSwarmOptimization pso = (ParticleSwarmOptimization) psoParams.getOptimizer();
        ClusteringDensityBased clust = new ClusteringDensityBased(clusterSigma, minClustSize,
                new IndividualDataMetric(ParticleSwarmOptimization.partBestPosKey));
        return createCbn(problem, pso, clust, maxSpecSize, new ClusteringDensityBased(clust),
                haltingWindowLength, haltingWindowEpsilon, popSize);
    }

    /**
     * Create a generic Clustering-based Niching EA with given parameters.
     *
     * @param problem
     * @param opt
     * @param clustDifferentiate
     * @param clustMerge
     * @param haltingWindowLength
     * @param haltingWindowEpsilon
     * @param popSize
     * @return
     */
    public static OptimizationParameters createCbn(AbstractOptimizationProblem problem, InterfaceOptimizer opt,
                                                   InterfaceClustering clustDifferentiate, int maxSpecSize, InterfaceClustering clustMerge, int haltingWindowLength,
                                                   double haltingWindowEpsilon, int popSize) {
        ClusterBasedNichingEA cbn = new ClusterBasedNichingEA();
        cbn.setOptimizer(opt);
        cbn.setMergingCA(clustMerge);
        cbn.setMaxSpeciesSize(maxSpecSize);
        cbn.setDifferentiationCA(clustDifferentiate);
        if (clustMerge != null) {
            cbn.setUseMerging(true);
        }
        cbn.setShowCycle(0); // don't do graphical output
        cbn.setHaltingWindow(haltingWindowLength);
        cbn.setEpsilonBound(haltingWindowEpsilon);
        return makeParams(cbn, popSize, problem, randSeed, getTerminator());
    }

    /**
     * A standard CBNES which employs a (15,50)-ES with further parameters as
     * set by the EvA2 framework.
     *
     * @param problem
     * @return
     */
    public static OptimizationParameters standardCbnES(AbstractOptimizationProblem problem) {
        EvolutionStrategies es = new EvolutionStrategies();
        es.setMu(15);
        es.setLambda(50);
        es.setPlusStrategy(false);
        return createCbn(problem, es, cbnDefaultClusterSigma, cbnDefaultMinGroupSize, cbnDefaultMaxGroupSize, cbnDefaultHaltingWindowLength, cbnDefaultHaltingWindowEpsilon, 100);
    }

    /**
     * A standard CBNES which employs a CMA-ES, see
     * {@link #cmaES(AbstractOptimizationProblem)}.
     *
     * @param problem
     * @return
     */
    public static OptimizationParameters standardCbnCmaES(AbstractOptimizationProblem problem) {
        OptimizationParameters cmaEsParams = cmaES(problem);
        EvolutionStrategies cmaES = (EvolutionStrategies) cmaEsParams.getOptimizer();
        return createCbn(problem, cmaES, cbnDefaultClusterSigma, cbnDefaultMinGroupSize, cbnDefaultMaxGroupSize, cbnDefaultHaltingWindowLength, cbnDefaultHaltingWindowEpsilon, 100);
    }

    /**
     * A standard CBNGA with a GA and further parameters as set by the EvA2
     * framework.
     *
     * @param problem
     * @return
     */
    public static OptimizationParameters standardCbnGA(AbstractOptimizationProblem problem) {
        GeneticAlgorithm ga = new GeneticAlgorithm();
        return createCbn(problem, ga, cbnDefaultClusterSigma, cbnDefaultMinGroupSize, cbnDefaultMaxGroupSize, cbnDefaultHaltingWindowLength, cbnDefaultHaltingWindowEpsilon, 100);
    }

    /**
     * @param problem
     * @return
     */
    public static OptimizationParameters standardPBIL(AbstractOptimizationProblem problem) {
        PopulationBasedIncrementalLearning pbil = createPBIL(0.04, 0.01, 0.5, 1,
                new SelectBestIndividuals(), 50, problem, null);

        return makeParams(pbil, pbil.getPopulation(), problem, randSeed, getTerminator());
    }

    /**
     * Create a standard clustering hill climbing employing simple ES mutation
     * with adaptive step size, starting in parallel 100 local searches and
     * clustering intermediate populations to avoid optima being found several
     * times by the same population (density based clustering with sigma =
     * 0.05). The population is reinitialized if the average progress of one
     * cycle does not exceed 1e-6.
     *
     * @param problem
     * @return
     */
    public static OptimizationParameters stdClusteringHillClimbing(
            AbstractOptimizationProblem problem) {
        return clusteringHillClimbing(problem, 1000, 100, 0.000001,
                PostProcessMethod.hillClimber, 0.05, 0.000001, 0.05);
    }

    /**
     * Create a clustering hillclimber using nelder mead and additional given
     * parameters.
     *
     * @param problem
     * @param evalCycle
     * @param popSize
     * @param minImprovement
     * @param method
     * @param sigmaClust
     * @return
     */
    public static OptimizationParameters clusteringHillClimbingNM(AbstractOptimizationProblem problem,
                                                                  int evalCycle, int popSize, double minImprovement, double sigmaClust) {
        return clusteringHillClimbing(problem, evalCycle, popSize, minImprovement,
                PostProcessMethod.nelderMead, 0.01, 0.00000001, sigmaClust);
    }

    /**
     * Create a custom clustering hillclimber using ES mutation (simple or CMA)
     * or nelder mead. The parameters hcInitialStep and hcStepThresh are only
     * relevant for the simple mutation based hc method.
     *
     * @param problem
     * @param evalCycle
     * @param popSize
     * @param minImprovement
     * @param method
     * @param hcInitialStep
     * @param hcStepThresh
     * @param sigmaClust
     * @return
     */
    public static OptimizationParameters clusteringHillClimbing(
            AbstractOptimizationProblem problem, int evalCycle, int popSize, double minImprovement,
            PostProcessMethod method, double hcInitialStep, double hcStepThresh, double sigmaClust) {
        ClusteringHillClimbing chc = new ClusteringHillClimbing();
        chc.setProblem(problem);

        chc.setEvalCycle(evalCycle);
        chc.setInitialPopSize(popSize);
        chc.setStepSizeInitial(hcInitialStep);
        chc.setLocalSearchMethod(method);
        chc.setMinImprovement(minImprovement);
        chc.setNotifyGuiEvery(0);
        chc.setStepSizeThreshold(hcStepThresh);
        chc.setSigmaClust(sigmaClust);
        return makeParams(chc, popSize, problem, randSeed, getTerminator());
    }

    public static OptimizationParameters cmaES(AbstractOptimizationProblem problem) {
        EvolutionStrategies es = new EvolutionStrategies();
        es.setMu(15);
        es.setLambda(50);
        es.setPlusStrategy(false);

        if (assertIndyType(problem, InterfaceESIndividual.class)) {
            setTemplateOperators(problem, new MutateESCovarianceMatrixAdaption(true), 1, new CrossoverESDefault(), 0);
        } else {
            System.err.println("Error, CMA-ES is implemented for ES individuals only (requires double data types)");
            return null;
        }

        return makeESParams(es, problem);
    }

    /**
     * Create a new IPOP-CMA-ES strategy with Rank-mu-CMA. The population size
     * is set to lambda = (int) (4.0 + 3.0 * Math.log(dim)) and mu =
     * Math.floor(lambda/2.), but lambda may grow during optimization due to
     * restarts with increased lambda. Operator probabilities are set to p_mut =
     * 1 and p_c = 0. The given problem must work with an individual template
     * which implements InterfaceDataTypeDouble.
     *
     * @param problem the optimization problem
     * @return
     */
    public static OptimizationParameters cmaESIPOP(AbstractOptimizationProblem problem) {
        return createCmaEsIPop(problem, 2.);
    }

    public static OptimizationParameters createCmaEsIPop(AbstractOptimizationProblem problem, double incLambdaFact) {
        EvolutionStrategies es = new EvolutionStrategyIPOP();

        AbstractEAIndividual indyTemplate = problem.getIndividualTemplate();
        if ((indyTemplate != null) && ((indyTemplate instanceof InterfaceESIndividual) || (indyTemplate instanceof InterfaceDataTypeDouble))) {
            // set selection strategy
            int dim;
            if (indyTemplate instanceof InterfaceESIndividual) {
                dim = ((InterfaceESIndividual) indyTemplate).getDGenotype().length;
            } else {
                dim = ((InterfaceDataTypeDouble) indyTemplate).getDoubleData().length;
            }
            int lambda = (int) (4.0 + 3.0 * Math.log(dim));
            es.setGenerationStrategy((int) Math.floor(lambda / 2.), lambda, false);
            es.setForceOrigPopSize(false);
            ((EvolutionStrategyIPOP) es).setIncPopSizeFact(incLambdaFact);
            // Set CMA operator for mutation
            AbstractEAIndividual indy = indyTemplate;
            MutateESRankMuCMA cmaMut = new MutateESRankMuCMA();
            AbstractEAIndividual.setOperators(indy, cmaMut, 1., new CrossoverESDefault(), 0.);
        } else {
            System.err.println("Error, CMA-ES is implemented for ES individuals only (requires double data types)");
            return null;
        }

        return makeESParams(es, problem);
    }

    public static OptimizationParameters standardNMS(AbstractOptimizationProblem problem) {
        NelderMeadSimplex nms = NelderMeadSimplex.createNelderMeadSimplex(problem, null);
        return makeParams(nms, 50, problem, randSeed, getTerminator());
    }

    /**
     * This constructs a standard DE variant with current-to-best/1 scheme,
     * F=0.8, k=0.6, lambda=0.6. The population size is 50 by default.
     *
     * @param problem
     * @return
     */
    public static OptimizationParameters standardDE(
            AbstractOptimizationProblem problem) {
        DifferentialEvolution de = new DifferentialEvolution();
        de.setDEType(DEType.DE2_CurrentToBest); // this sets current-to-best
        de.setDifferentialWeight(0.8);
        de.setCrossoverRate(0.6);
        de.setLambda(0.6);
        de.setMt(0.05); // this is not really employed for currentToBest
        return makeParams(de, 50, problem, randSeed, getTerminator());
    }

    public static OptimizationParameters standardES(
            AbstractOptimizationProblem problem) {
        EvolutionStrategies es = new EvolutionStrategies();
        es.setMu(15);
        es.setLambda(50);
        es.setPlusStrategy(false);

        if (assertIndyType(problem, InterfaceESIndividual.class)) {
            setTemplateOperators(problem, new MutateESGlobal(0.2, MutateESCrossoverType.intermediate), 0.9, new CrossoverESDefault(), 0.2);
        } else {
            System.err.println("Error, standard ES is implemented for ES individuals only (requires double data types)");
            return null;
        }
        return makeESParams(es, problem);
    }

    public static OptimizationParameters standardGA(
            AbstractOptimizationProblem problem) {
        GeneticAlgorithm ga = new GeneticAlgorithm();
        ga.setElitism(true);

        return makeParams(ga, 100, problem, randSeed, getTerminator());
    }

    /**
     * A standard constricted PSO instance with phi1=phi2=2.05, grid topology of
     * range 1 and a population size of 30. The chi parameter is set to approx.
     * 0.73 automatically.
     *
     * @param problem
     * @return
     */
    public static OptimizationParameters standardPSO(
            AbstractOptimizationProblem problem) {
        ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
        pso.setPhiValues(2.05, 2.05);
        pso.setTopology(PSOTopology.grid);
        pso.setTopologyRange(1);
        return makeParams(pso, 30, problem, randSeed, getTerminator());
    }

    public static OptimizationParameters tribes(AbstractOptimizationProblem problem) {
        return makeParams(new Tribes(), 1, problem, randSeed, getTerminator());
    }

    /**
     * A standard niching ES which employs predefined values and standard ES
     * variation operators.
     *
     * @param problem
     * @return
     */
    public static OptimizationParameters standardNichingEs(AbstractOptimizationProblem prob) {
        // nichingEs(AbstractOptimizationProblem prob, double nicheRadius, int muPerPeak, int lambdaPerPeak, 
        return createNichingEs(prob, -1, 100, 100,
                // int expectedPeaks, int rndImmigrants, int explorerPeaks, int resetExplInterval, int etaPresel) {
                10, 200, 0, 100, 50);
    }

    /**
     * A niching ES.
     *
     * @param problem
     * @return
     */
    public static OptimizationParameters createNichingEs(AbstractOptimizationProblem prob, double nicheRadius, int muPerPeak, int lambdaPerPeak, int expectedPeaks, int rndImmigrants, int explorerPeaks, int resetExplInterval, int etaPresel) {
        EsDpiNiching nes = new EsDpiNiching(nicheRadius, muPerPeak, lambdaPerPeak, expectedPeaks, rndImmigrants, explorerPeaks, resetExplInterval, etaPresel);

        if (assertIndyType(prob, InterfaceESIndividual.class)) {
            setTemplateOperators(prob, new MutateESGlobal(0.2, MutateESCrossoverType.intermediate), 0.9, new CrossoverESDefault(), 0.2);
        } else {
            System.err.println("Error, standard ES is implemented for ES individuals only (requires double data types)");
            return null;
        }
        // pop. size will be ignored by nes
        return makeParams(nes, 100, prob, randSeed, getTerminator());
    }

    /**
     * This actually employs the rank-mu CMA operator.
     *
     * @param prob
     * @return
     */
    public static OptimizationParameters standardNichingCmaEs(AbstractOptimizationProblem prob) {
        return createNichingCmaEs(prob, -1, 10, 10, 0, 0, -1);
    }

    /**
     * This actually employs the rank-mu CMA operator.
     *
     * @param prob
     * @return
     */
    public static OptimizationParameters createNichingCmaEs(AbstractOptimizationProblem prob, double nicheRad) {
        return createNichingCmaEs(prob, nicheRad, 10, 10, 0, 0, -1);
    }

    /**
     * This actually employs the rank-mu CMA operator.
     *
     * @param prob
     * @return
     */
    public static OptimizationParameters createNichingCmaEs(AbstractOptimizationProblem prob, double nicheRad, double stagnationEpsilon) {
        return createNichingCmaEs(prob, nicheRad, 10, 10, 0, 0, stagnationEpsilon);
    }

    /**
     * This actually employs the rank-mu CMA operator.
     *
     * @param prob
     * @return
     */
    public static OptimizationParameters createNichingCmaEs(AbstractOptimizationProblem prob, double nicheRad, int nicheCount, double stagnationEpsilon) {
        return createNichingCmaEs(prob, nicheRad, 10, nicheCount, 0, 0, stagnationEpsilon);
    }

    /**
     * A generic niching CMA-ES using the rank-mu CMA operator
     *
     * @param prob
     * @param nicheRadius
     * @param lambda
     * @param expectedPeaks
     * @param explorerPeaks
     * @param resetExplInterval
     * @return
     */
    public static OptimizationParameters createNichingCmaEs(AbstractOptimizationProblem prob, double nicheRadius, int lambda, int expectedPeaks, int explorerPeaks, int resetExplInterval, double stagnationEpsilon) {
        EsDpiNiching nes = new EsDpiNichingCma(nicheRadius, lambda, expectedPeaks, explorerPeaks, resetExplInterval);
        if (stagnationEpsilon > 0) {
            nes.setEpsilonBound(stagnationEpsilon);
        }

        if (assertIndyType(prob, InterfaceESIndividual.class)) {
            setTemplateOperators(prob, new MutateESRankMuCMA(), 1., new CrossoverESDefault(), 0.);
        } else {
            System.err.println("Error, standard ES is implemented for ES individuals only (requires double data types)");
            return null;
        }
        // pop. size will be ignored by nes
        return makeParams(nes, 100, prob, randSeed, getTerminator());
    }

    /**
     * Check if a given problem instance has a template individual matching the
     * given class.
     *
     * @param prob
     * @param cls
     * @return
     */
    private static boolean assertIndyType(AbstractOptimizationProblem prob,
                                          Class<InterfaceESIndividual> cls) {
        return cls.isAssignableFrom(prob.getIndividualTemplate().getClass());
    }

    /**
     * Set the evolutionary operators of the individual template of the given
     * problem instance.
     *
     * @param prob
     * @param mute
     * @param pMut
     * @param cross
     * @param pCross
     */
    public static void setTemplateOperators(AbstractOptimizationProblem prob, InterfaceMutation mute, double pMut, InterfaceCrossover cross, double pCross) {
        AbstractEAIndividual indy = prob.getIndividualTemplate();
        if ((indy != null)) {
            indy.setOperators(mute, pMut, cross, pCross);
        }
    }
}
