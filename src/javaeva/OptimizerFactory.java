package javaeva;

import javaeva.gui.BeanInspector;
import javaeva.server.go.IndividualInterface;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceESIndividual;
import javaeva.server.go.operators.cluster.ClusteringDensityBased;
import javaeva.server.go.operators.crossover.CrossoverESDefault;
import javaeva.server.go.operators.mutation.MutateESCovarianceMartixAdaption;
import javaeva.server.go.operators.mutation.MutateESGlobal;
import javaeva.server.go.operators.terminators.CombinedTerminator;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.AbstractOptimizationProblem;
import javaeva.server.go.strategies.ClusterBasedNichingEA;
import javaeva.server.go.strategies.DifferentialEvolution;
import javaeva.server.go.strategies.EvolutionStrategies;
import javaeva.server.go.strategies.GeneticAlgorithm;
import javaeva.server.go.strategies.HillClimbing;
import javaeva.server.go.strategies.InterfaceOptimizer;
import javaeva.server.go.strategies.MonteCarloSearch;
import javaeva.server.go.strategies.ParticleSwarmOptimization;
import javaeva.server.go.strategies.Tribes;
import javaeva.server.modules.GOParameters;

/**
 * The OptimizerFactory allows quickly creating some optimizers without thinking much
 * about parameters. You can access a runnable Optimization thread and directly start it,
 * or access its fully prepared GOParameter instance, change some parameters, and start it then.
 *  
 * @author mkron
 *
 */
public class OptimizerFactory {
	private static InterfaceTerminator term = null;
	public final static int STD_ES 	= 1;
	public final static int CMA_ES 	= 2;
	public final static int STD_GA = 3;
	public final static int PSO 	= 4;
	public final static int DE 		= 5;
	public final static int TRIBES	= 6;
	public final static int RANDOM = 7;
	public final static int HILLCL = 8;
	public final static int CBN_ES = 9;
	
	public final static int defaultFitCalls = 10000;
	public final static int randSeed = 0;
	
	/**
	 * Return a simple String showing the accessible optimizers. For external access."
	 * 
	 * @return a String listing the accessible optimizers
	 */
	public static String showOptimizers() {
		return "1: Standard ES; 2: CMA-ES; 3: GA; 4: PSO; 5: DE; 6: Tribes; 7: Random (Monte Carlo); 8: HillClimbing; 9: Cluster-based niching ES";
	}
	
	/**
	 * The default Terminator finishes after n fitness calls, the default n is returned here.
	 * @return the default number of fitness call done before termination
	 */
	public static int getDefaultFitCalls() {
		return defaultFitCalls;
	}
	
	/**
	 * Create a runnable optimization Runnable and directly start it in an own thread. The Runnable
	 * will notify waiting threads and set the isFinished flag when the optimization is complete.
	 * If the optType is invalid, null will be returned.
	 * 
	 * @param optType
	 * @param problem
	 * @param outputFilePrefix
	 * @return
	 */
	public static OptimizerRunnable optimizeInThread(final int optType, AbstractOptimizationProblem problem, String outputFilePrefix) {
		OptimizerRunnable runnable = getOptRunnable(optType, problem, outputFilePrefix);
		if (runnable != null) new Thread(runnable).start();
		return runnable;
	}
	
	// TODO hier weiter kommentieren
	public static IndividualInterface optimize(final int optType, AbstractOptimizationProblem problem, String outputFilePrefix) {
		OptimizerRunnable runnable = getOptRunnable(optType, problem, outputFilePrefix);
		if (runnable != null) {
			new Thread(runnable).run();
			return runnable.getSolution();
		} else return null;
	}
	
	public static OptimizerRunnable getOptRunnable(final int optType, AbstractOptimizationProblem problem, String outputFilePrefix) {
		return getOptRunnable(optType, problem, defaultFitCalls, outputFilePrefix);
	}
	
	public static OptimizerRunnable getOptRunnable(final int optType, AbstractOptimizationProblem problem, int fitCalls, String outputFilePrefix) {
		OptimizerRunnable opt = null;
		switch (optType) {
		case STD_ES:
			opt = new OptimizerRunnable(standardES(problem), outputFilePrefix); 
			break;
		case CMA_ES:
			opt = new OptimizerRunnable(cmaES(problem), outputFilePrefix); 
			break;
		case STD_GA:
			opt = new OptimizerRunnable(standardGA(problem), outputFilePrefix); 
			break;
		case PSO:
			opt = new OptimizerRunnable(standardPSO(problem), outputFilePrefix); 
			break;
		case DE:
			opt = new OptimizerRunnable(standardDE(problem), outputFilePrefix); 
			break;
		case TRIBES:
			opt = new OptimizerRunnable(tribes(problem), outputFilePrefix); 
			break;
		case RANDOM:
			opt = new OptimizerRunnable(monteCarlo(problem), outputFilePrefix); 
			break;
		case HILLCL:
			opt = new OptimizerRunnable(hillClimbing(problem), outputFilePrefix); 
			break;
		case CBN_ES:
			opt = new OptimizerRunnable(cbnES(problem), outputFilePrefix); 
			break;
		default:
			System.err.println("Error: optimizer type " + optType + " is unknown!");
			return null;
		}
		if (fitCalls != defaultFitCalls) opt.getGOParams().setTerminator(new EvaluationTerminator(fitCalls));
		return opt;
	}
	
	public static InterfaceTerminator defaultTerminator() {
		if (term == null) term = new EvaluationTerminator(defaultFitCalls);
		return term;
	}
	
	public static void setTerminator(InterfaceTerminator term) {
		OptimizerFactory.term = term;
	}
	
	public static InterfaceTerminator getTerminator() {
		return OptimizerFactory.term;
	}
	
	/**
	 * Add an InterfaceTerminator to any new optimizer in a boolean combination. The old and the given
	 * terminator will be combined as in (TOld && TNew) if bAnd is true, and as in (TOld || TNew) if bAnd
	 * is false. 
	 * @param newTerm a new InterfaceTerminator instance
	 * @param bAnd indicate the boolean combination
	 */
	public static void addTerminator(InterfaceTerminator newTerm, boolean bAnd) {
		if (OptimizerFactory.term == null) OptimizerFactory.term = term;
		else setTerminator(new CombinedTerminator(OptimizerFactory.term, newTerm, bAnd));
	}
	
	public static GOParameters makeParams(InterfaceOptimizer opt, Population pop, AbstractOptimizationProblem problem, long seed, InterfaceTerminator term) {
		GOParameters params = new GOParameters();
		params.setProblem(problem);
		opt.SetProblem(problem);
		opt.setPopulation(pop);
		params.setOptimizer(opt);
		params.setTerminator(term);
		params.setSeed(seed);
		return params;
	}
	
	public static GOParameters standardES(AbstractOptimizationProblem problem) {
		EvolutionStrategies es = new EvolutionStrategies();
		es.setMyu(15);
		es.setLambda(50);
		es.setPlusStrategy(false);
		
		Object maybeTemplate = BeanInspector.callIfAvailable(problem, "getEAIndividual", null);
		if ((maybeTemplate != null) && (maybeTemplate instanceof InterfaceESIndividual)) {
			// Set CMA operator for mutation
			AbstractEAIndividual indy = (AbstractEAIndividual)maybeTemplate;
			indy.setMutationOperator(new MutateESGlobal());
			indy.setCrossoverOperator(new CrossoverESDefault());
		} else {
			System.err.println("Error, standard ES is implemented for ES individuals only (requires double data types)");
			return null;
		}
		
		Population pop = new Population();
		pop.setPopulationSize(es.getLambda());
		
		return makeParams(es, pop, problem, randSeed, defaultTerminator());
	}
	
	public static GOParameters cmaES(AbstractOptimizationProblem problem) {
		EvolutionStrategies es = new EvolutionStrategies();
		es.setMyu(15);
		es.setLambda(50);
		es.setPlusStrategy(false);
		
		// TODO improve this by adding getEAIndividual to AbstractEAIndividual? 
		Object maybeTemplate = BeanInspector.callIfAvailable(problem, "getEAIndividual", null);
		if ((maybeTemplate != null) && (maybeTemplate instanceof InterfaceESIndividual)) {
			// Set CMA operator for mutation
			AbstractEAIndividual indy = (AbstractEAIndividual)maybeTemplate;
			MutateESCovarianceMartixAdaption cmaMut = new MutateESCovarianceMartixAdaption();
			cmaMut.setCheckConstraints(true);
			indy.setMutationOperator(cmaMut);
			indy.setCrossoverOperator(new CrossoverESDefault());
		} else {
			System.err.println("Error, CMA-ES is implemented for ES individuals only (requires double data types)");
			return null;
		}
		
		Population pop = new Population();
		pop.setPopulationSize(es.getLambda());
		
		return makeParams(es, pop, problem, randSeed, defaultTerminator());
	}
	
	public static GOParameters standardPSO(AbstractOptimizationProblem problem) {
		ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
		Population pop = new Population();
		pop.setPopulationSize(30);
		pso.setPopulation(pop);
		pso.setPhiValues(2.05, 2.05);
		pso.getTopology().setSelectedTag("Grid");
		return makeParams(pso, pop, problem, randSeed, defaultTerminator());
	}
	
	public static GOParameters standardDE(AbstractOptimizationProblem problem) {
		DifferentialEvolution de = new DifferentialEvolution();
		Population pop = new Population();
		pop.setPopulationSize(50);
		de.setPopulation(pop);
		de.getDEType().setSelectedTag(1); // this sets current-to-best
	    de.setF(0.8);
	    de.setK(0.6);
	    de.setLambda(0.6);
	    de.setMt(0.05);
				
		return makeParams(de, pop, problem, randSeed, defaultTerminator());
	}
	
	public static GOParameters standardGA(AbstractOptimizationProblem problem) {
		GeneticAlgorithm ga = new GeneticAlgorithm();
		Population pop = new Population();
		pop.setPopulationSize(100);
		ga.setPopulation(pop);
		ga.setElitism(true);

		return makeParams(ga, pop, problem, randSeed, defaultTerminator());
	}
	
	public static GOParameters tribes(AbstractOptimizationProblem problem) {
		Tribes tr = new Tribes();
		Population pop = new Population();
		pop.setPopulationSize(1); // only for init
		problem.initPopulation(pop);
		return makeParams(tr, pop, problem, randSeed, defaultTerminator());
	}
	
	public static GOParameters hillClimbing(AbstractOptimizationProblem problem) {
		HillClimbing hc = new HillClimbing();
		hc.SetProblem(problem);
		Population pop = new Population();
		pop.setPopulationSize(50);
		problem.initPopulation(pop);
		return makeParams(hc, pop, problem, randSeed, defaultTerminator());
	}
	
	public static GOParameters monteCarlo(AbstractOptimizationProblem problem) {
		MonteCarloSearch mc = new MonteCarloSearch();
		Population pop = new Population();
		pop.setPopulationSize(50);
		problem.initPopulation(pop);
		return makeParams(mc, pop, problem, randSeed, defaultTerminator());
	}
	
	public static GOParameters cbnES(AbstractOptimizationProblem problem) {
		ClusterBasedNichingEA cbn = new ClusterBasedNichingEA();
		EvolutionStrategies es = new EvolutionStrategies();
		es.setMyu(15);
		es.setLambda(50);
		es.setPlusStrategy(false);
		cbn.setOptimizer(es);
		ClusteringDensityBased clustering = new ClusteringDensityBased(0.1);
	    cbn.setConvergenceCA((ClusteringDensityBased)clustering.clone());
	    cbn.setDifferentationCA(clustering);
	    cbn.setShowCycle(10); // dont do graphical output
	    
		Population pop = new Population();
		pop.setPopulationSize(100);
		problem.initPopulation(pop);
		
		return makeParams(cbn, pop, problem, randSeed, defaultTerminator());
	}
}
