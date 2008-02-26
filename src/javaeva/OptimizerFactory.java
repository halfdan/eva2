package javaeva;

import javaeva.gui.BeanInspector;
import javaeva.server.go.IndividualInterface;
import javaeva.server.go.InterfaceTerminator;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.InterfaceESIndividual;
import javaeva.server.go.operators.crossover.CrossoverESDefault;
import javaeva.server.go.operators.mutation.MutateESCovarianceMartixAdaption;
import javaeva.server.go.operators.mutation.MutateESDefault;
import javaeva.server.go.operators.mutation.MutateESGlobal;
import javaeva.server.go.operators.terminators.CombinedTerminator;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.AbstractOptimizationProblem;
import javaeva.server.go.strategies.DifferentialEvolution;
import javaeva.server.go.strategies.EvolutionStrategies;
import javaeva.server.go.strategies.GeneticAlgorithm;
import javaeva.server.go.strategies.InterfaceOptimizer;
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
	
	public final static int defaultFitCalls = 10000;
	public final static int randSeed = 0;
	
	/**
	 * Return a simple String showing the accessible optimizers. For external access."
	 * 
	 * @return a String listing the accessible optimizers
	 */
	public static String showOptimizers() {
		return "1: Standard ES; 2: CMA-ES; 3: GA; 4: PSO; 5: DE; 6: Tribes";
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
		switch (optType) {
		case STD_ES:
			return new OptimizerRunnable(standardES(problem), outputFilePrefix);
		case CMA_ES:
			return new OptimizerRunnable(cmaES(problem), outputFilePrefix);
		case STD_GA:
			return new OptimizerRunnable(standardGA(problem), outputFilePrefix);
		case PSO:
			return new OptimizerRunnable(standardPSO(problem), outputFilePrefix);
		case DE:
			return new OptimizerRunnable(standardDE(problem), outputFilePrefix);
		case TRIBES:
			return new OptimizerRunnable(tribes(problem), outputFilePrefix);
		}
		System.err.println("Error: optimizer type " + optType + " is unknown!");
		return null;
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
}
