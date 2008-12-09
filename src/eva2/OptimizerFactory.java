/*
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 */
package eva2;

import java.util.BitSet;
import java.util.Vector;

import eva2.server.go.IndividualInterface;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.enums.DETypeEnum;
import eva2.server.go.enums.PostProcessMethod;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.operators.archiving.ArchivingNSGAII;
import eva2.server.go.operators.archiving.InformationRetrievalInserting;
import eva2.server.go.operators.archiving.InterfaceArchiving;
import eva2.server.go.operators.archiving.InterfaceInformationRetrieval;
import eva2.server.go.operators.cluster.ClusteringDensityBased;
import eva2.server.go.operators.crossover.CrossoverESDefault;
import eva2.server.go.operators.crossover.InterfaceCrossover;
import eva2.server.go.operators.crossover.NoCrossover;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateESCovarianceMatrixAdaption;
import eva2.server.go.operators.mutation.MutateESFixedStepSize;
import eva2.server.go.operators.mutation.MutateESGlobal;
import eva2.server.go.operators.mutation.MutateESRankMuCMA;
import eva2.server.go.operators.mutation.NoMutation;
import eva2.server.go.operators.postprocess.InterfacePostProcessParams;
import eva2.server.go.operators.postprocess.PostProcessParams;
import eva2.server.go.operators.selection.InterfaceSelection;
import eva2.server.go.operators.selection.SelectBestIndividuals;
import eva2.server.go.operators.terminators.CombinedTerminator;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.strategies.ClusterBasedNichingEA;
import eva2.server.go.strategies.ClusteringHillClimbing;
import eva2.server.go.strategies.DifferentialEvolution;
import eva2.server.go.strategies.EvolutionStrategies;
import eva2.server.go.strategies.EvolutionStrategyIPOP;
import eva2.server.go.strategies.GeneticAlgorithm;
import eva2.server.go.strategies.GradientDescentAlgorithm;
import eva2.server.go.strategies.HillClimbing;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.go.strategies.MonteCarloSearch;
import eva2.server.go.strategies.MultiObjectiveEA;
import eva2.server.go.strategies.ParticleSwarmOptimization;
import eva2.server.go.strategies.SimulatedAnnealing;
import eva2.server.go.strategies.Tribes;
import eva2.server.modules.GOParameters;

/**
 * <p>
 * The OptimizerFactory allows quickly creating some optimizers without thinking
 * much about parameters. You can access a runnable Optimization thread and
 * directly start it, or access its fully prepared GOParameter instance, change
 * some parameters, and start it then.
 * </p>
 * <p>
 * On the other hand this class provides an almost complete list of all
 * currently available optimization procedures in EvA2. The arguments passed to
 * the methods initialize the respective optimization procedure. To perform an
 * optimization one has to do the following: <code>
 * InterfaceOptimizer optimizer = OptimizerFactory.createCertainOptimizer(arguments);
 * EvaluationTerminator terminator = new EvaluationTerminator(numOfFitnessCalls);
 * while (!terminator.isTerminated(optimizer.getPopulation())) optimizer.optimize();
 * </code>
 * </p>
 *
 * @version 0.1
 * @since 2.0
 * @author mkron
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de>
 * @date 17.04.2007
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

	public final static int defaultFitCalls = 10000;

	public final static int randSeed = 0;

	private static OptimizerRunnable lastRunnable = null;

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
	public static final DifferentialEvolution createDifferentialEvolution(
			AbstractOptimizationProblem problem, int popsize, double f,
			double lambda, double CR,
			InterfacePopulationChangedEventListener listener) {

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0.0);
		tmpIndi.setMutationOperator(new NoMutation());
		tmpIndi.setMutationProbability(0.0);

		DifferentialEvolution de = new DifferentialEvolution();
		de.SetProblem(problem);
		de.getPopulation().setPopulationSize(popsize);
		de.setDEType(DETypeEnum.DE2_CurrentToBest);
		de.setF(f);
		de.setK(CR);
		de.setLambda(lambda);
		de.addPopulationChangedEventListener(listener);
		de.init();

		return de;
	}

	/**
	 * This method performs the optimization using an Evolution strategy.
	 *
	 * @param mu
	 * @param lambda
	 * @param plus
	 *            if true this operator uses elitism otherwise a comma strategy.
	 * @param mutationoperator
	 * @param pm
	 * @param crossoveroperator
	 * @param pc
	 * @param selection environmental selection operator
	 * @param problem
	 * @param listener
	 * @return An optimization algorithm that employs an evolution strategy.
	 */
	public static final EvolutionStrategies createEvolutionStrategy(int mu,
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
	 * @param plus
	 *            if true this operator uses elitism otherwise a comma strategy.
	 * @param mutationoperator
	 * @param pm
	 * @param crossoveroperator
	 * @param pc
	 * @param incPopSizeFact factor by which to inrease lambda ro restart, default is 2
	 * @param stagThresh	if the fitness changes below this value during a stagnation phase, a restart is initiated  
	 * @param problem
	 * @param listener
	 * @return An optimization algorithm that employs an IPOP-ES.
	 */
	public static final EvolutionStrategyIPOP createEvolutionStrategyIPOP(int mu,
			int lambda, boolean plus, InterfaceMutation mutationoperator,
			double pm, InterfaceCrossover crossoveroperator, double pc, double incPopSizeFact, double stagThresh,
			AbstractOptimizationProblem problem, InterfacePopulationChangedEventListener listener) {
		EvolutionStrategyIPOP esIPOP = (EvolutionStrategyIPOP)createES(new EvolutionStrategyIPOP(mu, lambda, plus), mutationoperator, pm, crossoveroperator, pc, new SelectBestIndividuals(), problem, listener);
		esIPOP.setIncPopSizeFact(incPopSizeFact);
//		esIPOP.setStagnationGenerations(stagTimeGens);
		esIPOP.setStagThreshold(stagThresh);
		return esIPOP;
	}

	private static final EvolutionStrategies createES(EvolutionStrategies theES, InterfaceMutation mutationoperator,
			double pm, InterfaceCrossover crossoveroperator, double pc,
			InterfaceSelection selection, AbstractOptimizationProblem problem,
			InterfacePopulationChangedEventListener listener) {

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		AbstractEAIndividual.setOperators(tmpIndi, mutationoperator, pm, crossoveroperator, pc);

		EvolutionStrategies es = new EvolutionStrategies();
		es.addPopulationChangedEventListener(listener);
		//es.setParentSelection(selection);
		//es.setPartnerSelection(selection);
		es.setEnvironmentSelection(selection);
		es.SetProblem(problem);
		es.init();

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
	public static final GeneticAlgorithm createGeneticAlgorithm(
			InterfaceMutation mut, double pm, InterfaceCrossover cross,
			double pc, InterfaceSelection select, int popsize,
			AbstractOptimizationProblem problem,
			InterfacePopulationChangedEventListener listener) {

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setCrossoverOperator(cross);
		tmpIndi.setCrossoverProbability(pc);
		tmpIndi.setMutationOperator(mut);
		tmpIndi.setMutationProbability(pm);

		GeneticAlgorithm ga = new GeneticAlgorithm();
		ga.SetProblem(problem);
		ga.getPopulation().setPopulationSize(popsize);
		ga.setParentSelection(select);
		ga.setPartnerSelection(select);
		ga.addPopulationChangedEventListener(listener);
		ga.init();

		return ga;
	}

	/**
	 * This method creates a multi-objective EA optimizer. Remember to set a
	 * multi-objective selection method within the specific optimizer. This uses
	 * a standard archiving strategy (NSGAII) and InformationRetrievalInserting.
	 *
	 * @param subOpt
	 *            the specific optimizer to use
	 * @param archiveSize
	 *            maximum size of the archive
	 * @param problem
	 * @param listener
	 * @return An optimization algorithm that employs a multi-objective
	 *         optimizer
	 */
	public static final MultiObjectiveEA createMultiObjectiveEA(
			InterfaceOptimizer subOpt, int archiveSize,
			AbstractOptimizationProblem problem,
			InterfacePopulationChangedEventListener listener) {

		return createMultiObjectiveEA(subOpt, new ArchivingNSGAII(),
				archiveSize, new InformationRetrievalInserting(), problem,
				listener);
	}

	/**
	 * This method creates a multi-objective EA optimizer. Remember to set a
	 * multi-objective selection method within the specific optimizer.
	 *
	 * @param subOpt
	 *            the specific optimizer to use
	 * @param archiving
	 *            the archiving strategy collecting the pareto front
	 * @param archiveSize
	 *            maximum size of the archive
	 * @param infoRetrieval
	 *            information retrieval strategy
	 * @param problem
	 * @param listener
	 * @return An optimization algorithm that employs a multi-objective
	 *         optimizer
	 */
	public static final MultiObjectiveEA createMultiObjectiveEA(
			InterfaceOptimizer subOpt, InterfaceArchiving archiving,
			int archiveSize, InterfaceInformationRetrieval infoRetrieval,
			AbstractOptimizationProblem problem,
			InterfacePopulationChangedEventListener listener) {

		problem.initProblem();
		subOpt.SetProblem(problem);

		return new MultiObjectiveEA(subOpt, archiving, archiveSize,
				infoRetrieval, problem);
	}

	/**
	 * This starts a Gradient Descent.
	 *
	 * @param problem
	 * @return An optimization algorithm that performs gradient descent.
	 */
	public static final GradientDescentAlgorithm createGradientDescent(
			AbstractOptimizationProblem problem) {

		System.err.println("Currently not implemented!");

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0.0);

		GradientDescentAlgorithm gd = new GradientDescentAlgorithm();

		// TODO implement!

		return gd;
	}

	/**
	 * This method performs a Hill Climber algorithm.
	 *
	 * @param pop
	 *            The size of the population
	 * @param problem
	 *            The problem to be optimized
	 * @param listener
	 * @return An optimization procedure that performes hill climbing.
	 */
	public static final HillClimbing createHillClimber(int pop,
			AbstractOptimizationProblem problem,
			InterfacePopulationChangedEventListener listener) {

		problem.initProblem();

		MutateESFixedStepSize mutator = new MutateESFixedStepSize();
		mutator.setSigma(0.2); // mutations step size
		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setMutationOperator(mutator);
		tmpIndi.setMutationProbability(1.0);
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0);

		HillClimbing hc = new HillClimbing();
		hc.getPopulation().setPopulationSize(pop);
		hc.addPopulationChangedEventListener(listener);
		hc.SetProblem(problem);
		hc.init();

		return hc;
	}

	/**
	 * This method performs a Monte Carlo Search with the given number of
	 * fitnesscalls.
	 *
	 * @param problem
	 * @param popsize
	 * @param listener
	 * @return An optimization procedure that performes the random walk.
	 */
	public static final MonteCarloSearch createMonteCarlo(
			AbstractOptimizationProblem problem, int popsize,
			InterfacePopulationChangedEventListener listener) {

		problem.initProblem();
		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setMutationOperator(new NoMutation());
		tmpIndi.setMutationProbability(0);
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0);

		MonteCarloSearch mc = new MonteCarloSearch();
		mc.getPopulation().setPopulationSize(popsize);
		mc.addPopulationChangedEventListener(listener);
		mc.SetProblem(problem);
		mc.init();

		return mc;
	}


	/**
	 * This method performs a particle swarm optimization. Standard topologies are
	 * linear (0), grid (1) and star (2).
	 *
	 * @param problem
	 * @param mut
	 * @param popsize
	 * @param phi1
	 * @param phi2
	 * @param k
	 * @param listener
	 * @param topology
	 * @see ParticleSwarmOpimization
	 * @return An optimization algorithm that performs particle swarm
	 *         optimization.
	 */
	public static final ParticleSwarmOptimization createParticleSwarmOptimization(
			AbstractOptimizationProblem problem, int popsize, double phi1,
			double phi2, double k,
			InterfacePopulationChangedEventListener listener,
			int selectedTopology) {

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0.0);
		tmpIndi.setMutationOperator(new NoMutation());
		tmpIndi.setMutationProbability(0.0);

		ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
		pso.SetProblem(problem);
		pso.getPopulation().setPopulationSize(popsize);
		pso.setPhi1(phi1);
		pso.setPhi2(phi2);
		pso.setSpeedLimit(k);
		pso.getTopology().setSelectedTag(selectedTopology);
		pso.addPopulationChangedEventListener(listener);
		pso.init();

		return pso;
	}

	/**
	 * This method performs a Simulated Annealing Optimization and prints the
	 * result as R output. It uses real valued individuals. The mutation
	 * probability is always 1.0.
	 *
	 * @param problem
	 * @param popsize
	 * @param alpha
	 *            The parameter for the linear cooling
	 * @param temperature
	 *            The initial temperature
	 * @param mut
	 * @param listener
	 * @return Returns an optimizer that performs simulated annealing.
	 */
	public static final SimulatedAnnealing createSimulatedAnnealing(
			AbstractOptimizationProblem problem, int popsize, double alpha,
			double temperature, InterfaceMutation mut,
			InterfacePopulationChangedEventListener listener) {

		problem.initProblem();

		AbstractEAIndividual tmpIndi = problem.getIndividualTemplate();
		tmpIndi.setCrossoverOperator(new NoCrossover());
		tmpIndi.setCrossoverProbability(0.0);
		tmpIndi.setMutationOperator(mut);
		tmpIndi.setMutationProbability(1.0);

		SimulatedAnnealing sa = new SimulatedAnnealing();
		sa.setAlpha(alpha);
		sa.setInitialTemperature(temperature);
		sa.SetProblem(problem);
		sa.getPopulation().setPopulationSize(popsize);
		sa.addPopulationChangedEventListener(listener);
		sa.init();

		return sa;
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
	public static final int getDefaultFitCalls() {
		return defaultFitCalls;
	}

	// /////////////////////////// constructing a default OptimizerRunnable

	/**
	 * For an optimizer identifier, return the corresponding default parameter set including
	 * initialization (thats why the problem is required).
	 * 
	 * @param optType	optimizer identifier
	 * @param problem	corresponding optimization problem
	 */
	public static GOParameters getParams(final int optType,
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
			return cbnES(problem);
		case CL_HILLCL:
			return clusteringHillClimbing(problem);
		case CMA_ES_IPOP: 
			return cmaESIPOP(problem);
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
		return "1: Standard ES \n2: CMA-ES \n3: GA \n4: PSO \n5: DE \n6: Tribes \n7: Random (Monte Carlo) "
				+ "\n8: Hill-Climbing \n9: Cluster-based niching ES \n10: Clustering Hill-Climbing \n11: IPOP-CMA-ES.";
	}

	/**
	 * Produce a runnable optimizer from a strategy identifier, a problem instance and with a given
	 * number of fitness calls to be performed. Output is written to a file if the prefix String is given.
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
	 * Produce a runnable optimizer from a strategy identifier, a problem instance and with a given
	 * terminator. Output is written to a file if the prefix String is given. If the terminator is null
	 * the current user-defined terminator will be used and if none is set, the default number of fitness
	 * calls will be performed.
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
		GOParameters params = getParams(optType, problem);
		if (params != null) {
			opt = new OptimizerRunnable(params, outputFilePrefix);
			if (terminator != null) opt.getGOParams().setTerminator(terminator);
			else opt.getGOParams().setTerminator(getTerminator());
		}
		return opt;
	}
	
	// /////////////////////////// constructing a default OptimizerRunnable
	/**
	 * Produce a runnable optimizer from a strategy identifier, a problem instance and with the current
	 * static terminator in use. Output is written to a file if the prefix String is given.
	 * @see #getOptRunnable(int, AbstractOptimizationProblem, int, String)
	 * @param optType
	 * @param problem
	 * @param outputFilePrefix
	 * @return a runnable optimizer
	 */
	public static OptimizerRunnable getOptRunnable(final int optType,
			AbstractOptimizationProblem problem, String outputFilePrefix) {
		return getOptRunnable(optType, problem, getTerminator(), outputFilePrefix);
	}

	/**
	 * Return the current user-defined or, if none was set, the default terminator.
	 * 
	 * @return the current default terminator
	 */
	public static InterfaceTerminator getTerminator() {
		if (OptimizerFactory.userTerm != null) return OptimizerFactory.userTerm;
		else return makeDefaultTerminator();
	}

	/**
	 * Return the number of evaluations performed during the last run or -1 if unavailable.
	 * 
	 * @return the number of evaluations performed during the last run or -1
	 */
	public static int lastEvalsPerformed() {
		return (lastRunnable != null) ? lastRunnable.getProgress() : -1;
	}

	// /////////////////////// Creating default strategies

	/**
	 * Use lambda, default random seed and terminator to produce GOParameters.
	 * 
	 * @param es
	 * @param problem
	 * @return
	 */
	public static GOParameters makeESParams(EvolutionStrategies es,
			AbstractOptimizationProblem problem) {
		return makeParams(es, es.getLambda(), problem, randSeed, makeDefaultTerminator());
	}

	/**
	 * Use default random seed and terminator for a parameter set.
	 * 
	 * @see #makeParams(InterfaceOptimizer, int, AbstractOptimizationProblem, long, InterfaceTerminator)
	 * @param opt
	 * @param popSize
	 * @param problem
	 * @return
	 */
	public static GOParameters makeParams(InterfaceOptimizer opt, int popSize, AbstractOptimizationProblem problem) {
		return makeParams(opt, popSize, problem, randSeed, makeDefaultTerminator());
	}

	public static GOParameters makeParams(InterfaceOptimizer opt,
			int popSize, AbstractOptimizationProblem problem, long seed,
			InterfaceTerminator term) {
		Population pop = new Population(popSize);
		problem.initPopulation(pop);
		return makeParams(opt, pop, problem, seed, term);
	}
	
	/**
	 * Create a GOParameters instance and prepare it with the given arguments. The result can be
	 * modified and then used to create an OptimizerRunnable, which of course can simply be run.
	 * 
	 * @see OptimizerRunnable
	 * @param opt
	 * @param pop
	 * @param problem
	 * @param seed
	 * @param term
	 * @return
	 */
	public static GOParameters makeParams(InterfaceOptimizer opt,
			Population pop, AbstractOptimizationProblem problem, long seed,
			InterfaceTerminator term) {
		GOParameters params = new GOParameters();
		params.setProblem(problem);
		opt.SetProblem(problem);
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
		if (runnable == null)
			return null;
		new Thread(runnable).run();
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
	public static OptimizerRunnable optimizeInThread(GOParameters params, String outputFilePrefix) {
		return optimizeInThread(new OptimizerRunnable(params, outputFilePrefix));
	}

	/**
	 * Start a runnable optimizer in a concurrent thread.
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
	public static BitSet optimizeToBinary(GOParameters params,
			String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
				outputFilePrefix));
		return runnable.getBinarySolution();
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

	public static double[] optimizeToDouble(GOParameters params,
			String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
				outputFilePrefix));
		return runnable.getDoubleSolution();
	}

	public static double[] optimizeToDouble(final int optType,
			AbstractOptimizationProblem problem, String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(optType, problem,
				outputFilePrefix);
		return (runnable != null) ? runnable.getDoubleSolution() : null;
	}

	public static double[] optimizeToDouble(OptimizerRunnable runnable) {
		optimize(runnable);
		return (runnable != null) ? runnable.getDoubleSolution() : null;
	}

	public static IndividualInterface optimizeToInd(GOParameters params,
			String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
				outputFilePrefix));
		return runnable.getResult();
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

	public static Population optimizeToPop(GOParameters params,
			String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(new OptimizerRunnable(params,
				outputFilePrefix));
		return runnable.getSolutionSet();
	}

	public static Population optimizeToPop(final int optType,
			AbstractOptimizationProblem problem, String outputFilePrefix) {
		OptimizerRunnable runnable = optimize(optType, problem,
				outputFilePrefix);
		return (runnable != null) ? runnable.getSolutionSet() : null;
	}

	public static Population optimizeToPop(OptimizerRunnable runnable) {
		optimize(runnable);
		return (runnable != null) ? runnable.getSolutionSet() : null;
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
		return runnable.getSolutionSet();
	}

	public static Vector<BitSet> postProcessBinVec(int steps, double sigma,
			int nBest) {
		return (lastRunnable != null) ? postProcessBinVec(lastRunnable,
				new PostProcessParams(steps, sigma, nBest)) : null;
	}

	public static Vector<BitSet> postProcessBinVec(
			InterfacePostProcessParams ppp) {
		return (lastRunnable != null) ? postProcessBinVec(lastRunnable, ppp)
				: null;
	}

	public static Vector<BitSet> postProcessBinVec(OptimizerRunnable runnable,
			int steps, double sigma, int nBest) {
		return postProcessBinVec(runnable, new PostProcessParams(steps, sigma,
				nBest));
	}

	/**
	 * Post process the given runnable with given parameters. Return the solution set
	 * as a vector of BitSets. The runnable will not be stored.
	 * 
	 * @param runnable
	 * @param ppp
	 * @return
	 */
	public static Vector<BitSet> postProcessBinVec(OptimizerRunnable runnable,
			InterfacePostProcessParams ppp) {
		Population resPop = postProcess(runnable, ppp);
		Vector<BitSet> ret = new Vector<BitSet>(resPop.size());
		for (Object o : resPop) {
			if (o instanceof InterfaceDataTypeBinary) {
				InterfaceDataTypeBinary indy = (InterfaceDataTypeBinary) o;
				ret.add(indy.getBinaryData());
			}
		}
		return ret;
	}

	public static Vector<double[]> postProcessDblVec(int steps, double sigma,
			int nBest) {
		return (lastRunnable == null) ? null : postProcessDblVec(lastRunnable,
				new PostProcessParams(steps, sigma, nBest));
	}

	public static Vector<double[]> postProcessDblVec(
			InterfacePostProcessParams ppp) {
		return (lastRunnable != null) ? postProcessDblVec(lastRunnable, ppp)
				: null;
	}

	public static Vector<double[]> postProcessDblVec(
			OptimizerRunnable runnable, int steps, double sigma, int nBest) {
		return postProcessDblVec(runnable, new PostProcessParams(steps, sigma,
				nBest));
	}
	
	/**
	 * Post process the given runnable with given parameters. Return the solution set
	 * as a vector of double arrays. The runnable will not be stored.
	 * 
	 * @param runnable
	 * @param ppp
	 * @return
	 */
	public static Vector<double[]> postProcessDblVec(
			OptimizerRunnable runnable, InterfacePostProcessParams ppp) {
		Population resPop = postProcess(runnable, ppp);
		Vector<double[]> ret = new Vector<double[]>(resPop.size());
		for (Object o : resPop) {
			if (o instanceof InterfaceDataTypeDouble) {
				InterfaceDataTypeDouble indy = (InterfaceDataTypeDouble) o;
				ret.add(indy.getDoubleData());
			}
		}
		return ret;
	}

	public static Vector<AbstractEAIndividual> postProcessIndVec(int steps,
			double sigma, int nBest) {
		return (lastRunnable != null) ? postProcessIndVec(lastRunnable,
				new PostProcessParams(steps, sigma, nBest)) : null;
	}

	public static Vector<AbstractEAIndividual> postProcessIndVec(
			InterfacePostProcessParams ppp) {
		return (lastRunnable != null) ? postProcessIndVec(lastRunnable, ppp)
				: null;
	}

	public static Vector<AbstractEAIndividual> postProcessIndVec(
			OptimizerRunnable runnable, int steps, double sigma, int nBest) {
		return postProcessIndVec(runnable, new PostProcessParams(steps, sigma,
				nBest));
	}
	
	/**
	 * Post process the given runnable with given parameters. Return the solution set
	 * as a vector of AbstractEAIndividuals. The runnable will not be stored.
	 * 
	 * @param runnable
	 * @param ppp
	 * @return
	 */
	public static Vector<AbstractEAIndividual> postProcessIndVec(
			OptimizerRunnable runnable, InterfacePostProcessParams ppp) {
		Population resPop = postProcess(runnable, ppp);
		Vector<AbstractEAIndividual> ret = new Vector<AbstractEAIndividual>(
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
	 * Add a new InterfaceTerminator to the current user-defined optimizer in a boolean combination.
	 * The old and the given terminator will be combined as in (TOld && TNew) if
	 * bAnd is true, and as in (TOld || TNew) if bAnd is false.
	 * If there was no user-defined terminator (or it was set to null) the new one is used without conjunction. 
	 *
	 * @param newTerm
	 *            a new InterfaceTerminator instance
	 * @param bAnd
	 *            indicate the boolean combination
	 */
	public static void addTerminator(InterfaceTerminator newTerm, boolean bAnd) {
		if (OptimizerFactory.userTerm == null)
			OptimizerFactory.userTerm = newTerm;
		else
			setTerminator(new CombinedTerminator(OptimizerFactory.userTerm,
					newTerm, bAnd));
	}
	
	/**
	 * Convenience method setting an EvaluationTerminator with the given
	 * number of evaluations.
	 * 
	 * @param maxEvals
	 */
	public static void setEvaluationTerminator(int maxEvals) {
		setTerminator(new EvaluationTerminator(maxEvals));
	}

	/**
	 * Return the termination message of the last runnable, if available. 
	 * @return
	 */
	public static String terminatedBecause() {
		return (lastRunnable != null) ? lastRunnable.terminatedBecause() : null;
	}
	
	///////////////////////////// default parameters
	/**
	 * Create a standard multi-start hill-climber parameter set with 50 initial individuals.
	 * 
	 * @return	a standard multi-start hill-climber
	 */
	public static final GOParameters hillClimbing(
			AbstractOptimizationProblem problem) {
		return makeParams(new HillClimbing(), 50, problem, randSeed, makeDefaultTerminator());
	}

	public static final GOParameters monteCarlo(
			AbstractOptimizationProblem problem) {
		return makeParams(new MonteCarloSearch(), 50, problem, randSeed, makeDefaultTerminator());
	}
	
	public static final GOParameters cbnES(AbstractOptimizationProblem problem) {
		ClusterBasedNichingEA cbn = new ClusterBasedNichingEA();
		EvolutionStrategies es = new EvolutionStrategies();
		es.setMu(15);
		es.setLambda(50);
		es.setPlusStrategy(false);
		cbn.setOptimizer(es);
		ClusteringDensityBased clustering = new ClusteringDensityBased(0.1);
		cbn.setConvergenceCA((ClusteringDensityBased) clustering.clone());
		cbn.setDifferentationCA(clustering);
		cbn.setShowCycle(0); // don't do graphical output

		return makeParams(cbn, 100, problem, randSeed, makeDefaultTerminator());
	}

	public static final GOParameters clusteringHillClimbing(
			AbstractOptimizationProblem problem) {
		ClusteringHillClimbing chc = new ClusteringHillClimbing();
		chc.SetProblem(problem);

		chc.setHcEvalCycle(1000);
		chc.setInitialPopSize(100);
		chc.setStepSizeInitial(0.05);
		chc.setMinImprovement(0.000001);
		chc.setNotifyGuiEvery(0);
		chc.setStepSizeThreshold(0.000001);
		chc.setSigmaClust(0.05);
		return makeParams(chc, 100, problem, randSeed, makeDefaultTerminator());
	}

	public static final GOParameters cmaES(AbstractOptimizationProblem problem) {
		EvolutionStrategies es = new EvolutionStrategies();
		es.setMu(15);
		es.setLambda(50);
		es.setPlusStrategy(false);

		AbstractEAIndividual indyTemplate = problem.getIndividualTemplate();
		if ((indyTemplate != null)
				&& (indyTemplate instanceof InterfaceESIndividual)) {
			// Set CMA operator for mutation
			AbstractEAIndividual indy = (AbstractEAIndividual) indyTemplate;
			MutateESCovarianceMatrixAdaption cmaMut = new MutateESCovarianceMatrixAdaption();
			cmaMut.setCheckConstraints(true);
			AbstractEAIndividual.setOperators(indy, cmaMut, 1., new CrossoverESDefault(), 0.);
		} else {
			System.err
					.println("Error, CMA-ES is implemented for ES individuals only (requires double data types)");
			return null;
		}

		return makeESParams(es, problem);
	}

	/**
	 * Create a new IPOP-CMA-ES strategy with Rank-mu-CMA. The population size is set to
	 * lambda = (int) (4.0 + 3.0 * Math.log(dim)) and mu = Math.floor(lambda/2.), but
	 * lambda may grow during optimization due to restarts with increased lambda.
	 * Operator probabilities are set to p_mut = 1 and p_c = 0.
	 * The given problem must work with an individual template which implements 
	 * InterfaceDataTypeDouble.
	 * 
	 * @param problem the optimization problem
	 * @return
	 */
	public static final GOParameters cmaESIPOP(AbstractOptimizationProblem problem) {
		EvolutionStrategies es = new EvolutionStrategyIPOP();

		AbstractEAIndividual indyTemplate = problem.getIndividualTemplate();
		if ((indyTemplate != null) && ((indyTemplate instanceof InterfaceESIndividual) || (indyTemplate instanceof InterfaceDataTypeDouble))) {
			// set selection strategy
			int dim;
			if (indyTemplate instanceof InterfaceESIndividual) dim=((InterfaceESIndividual)indyTemplate).getDGenotype().length;
			else dim = ((InterfaceDataTypeDouble)indyTemplate).getDoubleData().length;
			int lambda = (int) (4.0 + 3.0 * Math.log(dim));
			es.setGenerationStrategy((int)Math.floor(lambda/2.),lambda, false);
			es.setForceOrigPopSize(false);
			// Set CMA operator for mutation
			AbstractEAIndividual indy = (AbstractEAIndividual) indyTemplate;
			MutateESRankMuCMA cmaMut = new MutateESRankMuCMA();
			AbstractEAIndividual.setOperators(indy, cmaMut, 1., new CrossoverESDefault(), 0.);
		} else {
			System.err
					.println("Error, CMA-ES is implemented for ES individuals only (requires double data types)");
			return null;
		}

		return makeESParams(es, problem);
	}
	
	public static final GOParameters standardDE(
			AbstractOptimizationProblem problem) {
		DifferentialEvolution de = new DifferentialEvolution();
		de.setDEType(DETypeEnum.DE2_CurrentToBest); // this sets current-to-best
		de.setF(0.8);
		de.setK(0.6);
		de.setLambda(0.6);
		de.setMt(0.05);
		return makeParams(de, 50, problem, randSeed, makeDefaultTerminator());
	}

	public static final GOParameters standardES(
			AbstractOptimizationProblem problem) {
		EvolutionStrategies es = new EvolutionStrategies();
		es.setMu(15);
		es.setLambda(50);
		es.setPlusStrategy(false);

		AbstractEAIndividual indy = problem.getIndividualTemplate();

		if ((indy != null) && (indy instanceof InterfaceESIndividual)) {
			// Set CMA operator for mutation
			indy.setMutationOperator(new MutateESGlobal());
			indy.setCrossoverOperator(new CrossoverESDefault());
		} else {
			System.err
					.println("Error, standard ES is implemented for ES individuals only (requires double data types)");
			return null;
		}

		return makeESParams(es, problem);
	}

	public static final GOParameters standardGA(
			AbstractOptimizationProblem problem) {
		GeneticAlgorithm ga = new GeneticAlgorithm();
		ga.setElitism(true);

		return makeParams(ga, 100, problem, randSeed, makeDefaultTerminator());
	}

	public static final GOParameters standardPSO(
			AbstractOptimizationProblem problem) {
		ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
		pso.setPhiValues(2.05, 2.05);
		pso.getTopology().setSelectedTag("Grid");
		return makeParams(pso, 30, problem, randSeed, makeDefaultTerminator());
	}

	public static final GOParameters tribes(AbstractOptimizationProblem problem) {
		return makeParams(new Tribes(), 1, problem, randSeed, makeDefaultTerminator());
	}
}
