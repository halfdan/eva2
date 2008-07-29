package eva2.server.go.operators.postprocess;

import java.util.Collection;

import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.gui.BeanInspector;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.cluster.ClusteringDensityBased;
import eva2.server.go.operators.cluster.InterfaceClustering;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateESFixedStepSize;
import eva2.server.go.operators.mutation.MutateESMutativeStepSizeControl;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractMultiModalProblemKnown;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.FM0Problem;
import eva2.server.go.problems.InterfaceMultimodalProblemKnown;
import eva2.server.go.strategies.HillClimbing;
import eva2.server.stat.InterfaceTextListener;
import eva2.server.stat.StatsParameter;
import eva2.tools.Pair;


/**
 * Postprocess a population / list of individuals to find out a set of distinct optima.
 * 
 * @author mkron
 *
 */
public class PostProcess {
	protected static InterfaceDistanceMetric metric = new PhenotypeMetric();
	private static final boolean TRACE = false;
	// the default mutation step size for HC post processing
	private static double defaultMutationStepSize = 0.01;
	// lower limit mutation step size for HC post processing
	private static double minMutationStepSize = 0.0000000000000001;
	// used for hill climbing post processing and only alive during that period
	private static OptimizerRunnable hcRunnable = null;
	
	public static final int BEST_ONLY = 1;
	public static final int BEST_RAND = 2;
	public static final int RAND_ONLY = 3;
	
	public static final int KEEP_LONERS = 11;
	public static final int DISCARD_LONERS = 12;
	public static final int LONERS_AS_CLUSTERS = 13;
    /** 
     * This method returns a set of individuals corresponding to an optimum in a given list.
     * The individuals returned are to be nearer than epsilon to a given optimum. For each optimum, there is
     * returned zero or one individual at max.
     * If there are several individuals close to an optimum, the fitter one or the closer one
     * may be selected, indicated by the boolean flag bTakeFitter. This means, that an individual may be
     * returned in more than one copy if the optima are close together and the individual lies in between.
     * The returned array may contain null values if an optimum is not considered found at all.
     * 
     * @param pop       A population of possible solutions.
     * @param optima	a set of predefined optima
     * @param epsilon	the threshold up to which an optimum is considered found.
     * @param bTakeFitter	if true, the fitter of two close individuals is selected, otherwise the closer one
     * @return an array of individuals corresponding to the optimum with the same index
     */
    public static AbstractEAIndividual[] getFoundOptimaArray(Population pop, Population optima, double epsilon, boolean bTakeFitter) {
        AbstractEAIndividual   candidate, opt;
//        Population result = new Population(5);
        AbstractEAIndividual[]  found = new AbstractEAIndividual[optima.size()];
        double indDist;
        for (int i = 0; i < found.length; i++) found[i] = null;

        for (int i = 0; i < pop.size(); i++) {
            candidate = (AbstractEAIndividual) pop.get(i);
            for (int j = 0; j < optima.size(); j++) {
                opt = (AbstractEAIndividual) optima.get(j);
                indDist = metric.distance(candidate, opt);
                if (found[j] == null) { // current optimum has not been found yet
                    if (indDist < epsilon) {
                    	found[j] = (AbstractEAIndividual)candidate.clone();
//                    	result.add(found[j]);
                    }
                } else {// there was another one found. set the fitter one or the closer one
                	if (indDist < epsilon) {
	                	if ((bTakeFitter && (candidate.isDominatingDebConstraints(found[j]))) // flag "fitter" and new one is fitter
	                		|| (!bTakeFitter && (indDist < metric.distance(found[j], opt)))) { // flag "closer" and new one is closer
//	                		int index = result.indexOf(found[j]); // do replacement
	                		found[j] = (AbstractEAIndividual)candidate.clone();
//	                		result.set(index, found[j]);
	                	}
                	}
                }
            }
        }
        return found;
    }

    /** 
     * Convenience method for getFoundOptimaArray(), returning the same set of optima in a Population.
     * 
     * @see getFoundOptimaArray(Population pop, Population optima, double epsilon, boolean bTakeFitter)
     * @param pop       A population of possible solutions.
     * @param optima	a set of known optima
     * @param epsilon	the threshold up to which an optimum is considered found.
     * @param bTakeFitter	if true, the fitter of two close individuals is selected, otherwise the closer one
     * @return a Population of individuals corresponding to the given optima
     */
    public static Population getFoundOptima(Population pop, Population optima, double epsilon, boolean bTakeFitter) {
    	Population result = new Population(5);
    	AbstractEAIndividual[] optsFound = getFoundOptimaArray(pop, optima, epsilon, bTakeFitter);
    	
    	for (int i=0; i<optsFound.length; i++) {
			if (optsFound[i] != null) result.add(optsFound[i]);
		}
    	result.setPopulationSize(result.size());
    	return result;
    }

    /**
     * Calls clusterBest with a ClusteringDensitiyBased clustering object with the given sigma and a 
     * minimum group size of 2.
     * @see clusterBest(Population pop, InterfaceClustering clustering, double returnQuota, int lonerMode, int takeOverMode)
     * @param pop
     * @param sigmaCluster
     * @param returnQuota
     * @param lonerMode
     * @param takeOverMode 
     * @return
     */
    public static Population clusterBest(Population pop, double sigmaCluster, double returnQuota, int lonerMode, int takeOverMode) {
    	return clusterBest(pop, new ClusteringDensityBased(sigmaCluster, 2), returnQuota, lonerMode, takeOverMode);
    }
    
    /**
     * Cluster the population. Return for every cluster a subset of representatives which are the best individuals 
     * or a random subset of the cluster or the best and a random subset. Returns shallow copies!
     * returnQuota should be in  [0,1] and defines, which percentage of individuals of each cluster is kept, however
     * if returnQuota is > 0, at least one is kept.
     * lonerMode defines whether loners are discarded, kept, or treated as clusters, meaning they are kept if returnQuota > 0.
     * takOverMode defines whether, of a cluster with size > 1, which n individuals are kept. Either the n best only,
     * or the single best and random n-1, or all n random. 
     * 
     * @param pop
     * @param clustering
     * @param returnQuota
     * @param lonerMode
     * @param takeOverMode 
     * @return for every cluster a population of representatives which are the best individuals or a random subset of the cluster
     */
    public static Population clusterBest(Population pop, InterfaceClustering clustering, double returnQuota, int lonerMode, int takeOverMode) {
        //cluster the undifferentiated population
    	Population result = new Population(10);
    	result.setSameParams(pop);
    	Population[] clusters = clustering.cluster(pop);
    	if (TRACE) {
    		System.out.println("found " + clusters.length + " clusters!");
    		int sum=0;
    		for (int j=0; j<clusters.length; j++) {
    			sum += clusters[j].size();
    			if (TRACE) System.out.print(j + " w " + clusters[j].size() + ", ");
    		}
    		System.out.println("\nsum was "+sum);
    	}
        for (int j = 0; j < clusters.length; j++) {
        	if (j==0) { // cluster 0 contains non-assigned individuals 
        		if (lonerMode == DISCARD_LONERS) continue; // loners are discarded
        		else if (lonerMode == KEEP_LONERS) {
        			result.addAll(clusters[j]); // loners are all kept
        			continue;
        		} // default case: treat loners just as the rest
        		if (lonerMode != LONERS_AS_CLUSTERS) System.err.println("invalid loner mode in (), default is treating them like clusters");
        	}
        	if (returnQuota >= 1) result.addAll((Collection<AbstractEAIndividual>)clusters[j]); // easy case
        	else {
        		int n = Math.max(1, (int)(returnQuota*clusters[j].size())); // return at least one per cluster!
        		switch (takeOverMode) {
        		case BEST_ONLY: // another easy case
        			result.addAll((Collection<AbstractEAIndividual>)(clusters[j].getBestNIndividuals(n)));
        			break;
        		case BEST_RAND:
        			Population exclude = new Population();
        			exclude.add(clusters[j].getBestEAIndividual());
        			result.add(exclude.getEAIndividual(0));
        			result.addAll(clusters[j].getRandNIndividualsExcept(n-1, exclude));
        			break;
        		case RAND_ONLY:
        			result.addAll(clusters[j].getRandNIndividuals(n));
        			break;
        		default: System.err.println("Unknown mode in PostProcess:clusterBest!"); break;
        		}
        	}
        }
        result.setPopulationSize(result.size());
        return result;
    }
    
	public static double[] populationMeasures(Population pop) {
		double[] measures = pop.getPopulationMeasures();
		return measures;
	}
    
    /**
     * Filter the individuals of a population which have a fitness norm within given bounds.
     * Returns shallow copies!
     * 
     * @param pop
     * @param lower
     * @param upper
     * @return
     */
    public static Population filterFitnessIn(Population pop, double lower, double upper) {
    	Population result = filterFitness(pop, upper, true);
    	return filterFitness(result, lower, false);
    }
    
    /**
     * Filter the individuals of a population which have a fitness norm below a given value.
     * Returns shallow copies!
     * 
     * @param pop
     * @param fitNorm
     * @param bSmaller if true, return individuals with lower or equal, else with higher fitness norm only
     * @return
     */
    public static Population filterFitness(Population pop, double fitNorm, boolean bSmallerEq) {
    	AbstractEAIndividual indy;
    	Population result = new Population();
    	for (int i=0; i<pop.size(); i++) {
			indy = pop.getEAIndividual(i);
			if (bSmallerEq && (PhenotypeMetric.norm(indy.getFitness())<=fitNorm)) result.add(indy);
			else if (!bSmallerEq && (PhenotypeMetric.norm(indy.getFitness())>fitNorm)) result.add(indy);
		}
    	return result;
    }
    
    /**
     * Create a fitness histogram of an evaluated population within the given interval and nBins number of bins.
     * Therefore a bin is of size (upperBound-lowerBound)/nBins, and bin 0 starts at lowerBound.
     * Returns an integer array with the number of individuals in each bin.  
     * 
     * @param pop	the population to scan.
     * @param lowerBound	lower bound of the fitness interval
     * @param upperBound	upper bound of the fitness interval
     * @param nBins	number of bins
     * @return	an integer array with the number of individuals in each bin
     * @see filterFitnessIn()
     */
    public static int[] createFitNormHistogram(Population pop, double lowerBound, double upperBound, int nBins) {
    	int[] res = new int[nBins];
    	double lower = lowerBound;
    	double step = (upperBound - lowerBound) / nBins;
    	for (int i=0; i<nBins; i++) {
    		if (TRACE) System.out.println("checking between " + lower + " and " + (lower+step));
    		res[i] = filterFitnessIn(pop, lower, lower+step).size();
    		if (TRACE) System.out.println("found " + res[i]);
    		lower += step;
    	}
    	return res;
    }
    
    
//    /** 
//     * This method returns a set of individuals corresponding to an optimum in a given list.
//     * The individuals returned are to be nearer than epsilon to a given optimum. It is not
//     * guaranteed, however, that the best individuals are returned. For each optimum, there is
//     * returned zero or one individual at max.  
//     * If the optima are close together (e.g. closer than epsilon), or there is no threshold given,
//     * it may happen that an individual is not returned if it is second closest to one optimum
//     * and closest to another one.  
//     * 
//     * @param pop       A population of possible solutions.
//     * @param optima	a set of predefined optima
//     * @param epsilon	the threshold up to which an optimum is considered found.
//     * @return a list of individuals corresponding to an optimum from a list.
//     */
//    public static List<AbstractEAIndividual> getClosestFoundOptima(Population pop, Population optima, double epsilon) {
//        AbstractEAIndividual   indy;
//        ArrayList<AbstractEAIndividual> result = new ArrayList<AbstractEAIndividual>(5);
//        AbstractEAIndividual[]          foundIndy = new AbstractEAIndividual[optima.size()];
//
//        for (int i = 0; i < pop.size(); i++) {
//        	indy = (AbstractEAIndividual) pop.get(i);
//        	IndexFitnessPair bestHit = getClosestIndy(indy, optima);
//            if (foundIndy[bestHit.index] == null) { // there has no indy been assigned yet
//            	// assign the current indy if no epsilon is required, or epsilon-threshold is fulfilled
//            	if (epsilon < 0 || (bestHit.dist < epsilon)) foundIndy[bestHit.index] = indy;
//            } else {
//            	// assign current indy only if it is better than the earlier assigned one
//            	// in that case, epsilon is fulfilled automatically
//            	double oldDist = metric.distance(foundIndy[bestHit.index], (AbstractEAIndividual)optima.get(bestHit.index));
//            	if (bestHit.dist < oldDist) foundIndy[bestHit.index] = indy;
//            }
//        }
//        return result;
//    }

    /**
     * Search a population and find the closest individual to a given individual. Return the
     * best distance and corresponding index in a pair structure.
     * 
     * @param indy
     * @param pop
     * @return	index and distance to the closest individual in the population 
     */
	public static Pair<Double, Integer> getClosestIndy(AbstractEAIndividual indy, Population pop) {
		double bestDist = -1, tmpDist = -1;
		int bestIndex = -1;
		AbstractEAIndividual opt;
        for (int j = 0; j < pop.size(); j++) {
        	opt = (AbstractEAIndividual) pop.get(j);
        	tmpDist = metric.distance(indy, opt);	// distance current indy to current optimum
        	if (bestDist < 0 || (tmpDist < bestDist)) { // we have a better hit
        		bestIndex = j;
        		bestDist = tmpDist;
        	}
        }
        return new Pair<Double, Integer>(bestDist, bestIndex);
	}
	
	/**
	 * Optimize a population with a default hill-climbing heuristic for a number of fitness evaluations.
	 * As mutation operator, a mutative step size ES mutation is used, the step size of which is not allowed
	 * to increase above the initial stepSize. Returns the number of evaluations actually performed, which
	 * may be slightly above the maxSteps given.
	 * 
	 * @param pop 	the set of individuals to work on
	 * @param problem	the optimization problem
	 * @param maxSteps	the number of evaluations to perform during HC
	 * @param stepSize	the initial mutation step size
	 * @param minStepSize the minimal step size allowed for a mutation
	 * @return the number of evaluations actually performed
	 */
	public static int processWithHC(Population pop, AbstractOptimizationProblem problem, int maxSteps, double stepSize, double minStepSize) {
//		pop.SetFunctionCalls(0); // or else optimization wont restart on an "old" population
//		pop.setGenerationTo(0);
		int stepsBef = pop.getFunctionCalls();
		processWithHC(pop, problem, new EvaluationTerminator(pop.getFunctionCalls()+maxSteps), new MutateESMutativeStepSizeControl(stepSize, minStepSize, stepSize));
		return pop.getFunctionCalls()-stepsBef;
	}
	
	/**
	 * Perform hill climbing with default mutation parameters.
	 * 
	 * @see processWithHC(Population pop, AbstractOptimizationProblem problem, int maxSteps, double stepSize, double minStepSize)
	 * @param pop
	 * @param problem
	 * @param maxSteps
	 * @return	the number of evaluations actually performed
	 */
	public static int processWithHC(Population pop, AbstractOptimizationProblem problem, int maxSteps) {
		return processWithHC(pop, problem, maxSteps, defaultMutationStepSize, minMutationStepSize);
	}
	
	/**
	 * Optimize a population with a default hill-climbing heuristic with a given termination criterion and mutation operator.
	 * 
	 * @param pop
	 * @param problem
	 * @param term
	 * @param mute
	 */
	public static void processWithHC(Population pop, AbstractOptimizationProblem problem, InterfaceTerminator term, InterfaceMutation mute) {
		HillClimbing hc = new HillClimbing();
		// HC depends heavily on the selected mutation operator!
		hc.SetProblem(problem);
		mute.init(problem.getIndividualTemplate(), problem);
		hc.SetMutationOperator(mute);
		if (pop.size() != pop.getPopulationSize()) {
			System.err.println(pop.size() + " vs. "+ pop.getPopulationSize());
			System.err.println("warning: population size and vector size dont match! (PostProcess::processWithHC)");
		}
		hc.setPopulation(pop);
//		hc.initByPopulation(pop, false);
		hcRunnable = new OptimizerRunnable(OptimizerFactory.makeParams(hc, pop, problem, 0, term), true);
		hcRunnable.getGOParams().setDoPostProcessing(false);
		hcRunnable.setVerbosityLevel(StatsParameter.VERBOSITY_NONE);
		hcRunnable.run();
		hcRunnable.getGOParams().setDoPostProcessing(true);
		hcRunnable = null;
	}
	
	/**
	 * Stop the hill-climbing post processing if its currently running.
	 */
	public static void stopHC() {
		if (hcRunnable != null) synchronized (hcRunnable) {
			if (hcRunnable != null) hcRunnable.stopOpt();
		}
	}
	
	public static void main(String[] args) {
		AbstractOptimizationProblem problem = new FM0Problem();
		InterfaceMultimodalProblemKnown mmp = (InterfaceMultimodalProblemKnown)problem;
		OptimizerRunnable runnable = OptimizerFactory.getOptRunnable(OptimizerFactory.STD_GA, problem, 500, null);
		runnable.run();
		Population pop = runnable.getGOParams().getOptimizer().getPopulation();
//		System.out.println("no optima found: " + mmp.getNumberOfFoundOptima(pop));
		Population found = getFoundOptima(pop, mmp.getRealOptima(), 0.05, true);
		System.out.println("all found (" + found.size() + "): " + BeanInspector.toString(found));

		Pair<Population, Double> popD = new Pair<Population, Double>(pop, 1.);
		int i=0;
		int evalCnt = 0;
		while (popD.tail() > 0.01) {
			i++;
//			public static PopDoublePair clusterHC(pop, problem, sigmaCluster, funCalls, keepClusterRatio, mute) {

			popD = clusterHC(popD.head(), problem, 0.01, 1000 - (evalCnt % 1000), 0.5, new MutateESFixedStepSize(0.02));
			evalCnt += popD.head().getFunctionCalls();
		}
		found = getFoundOptima(popD.head(), mmp.getRealOptima(), 0.05, true);
		System.out.println("found at " + i + " (" + found.size() + "): " + BeanInspector.toString(found));
		System.out.println("funcalls: " + evalCnt);
//		System.out.println(BeanInspector.toString(pop.getMeanFitness()));
		
//		System.out.println("no optima found: " + mmp.getNumberOfFoundOptima(pop));
//		System.out.println("best after: " + AbstractEAIndividual.getDefaultStringRepresentation(pop.getBestEAIndividual()));
		
	}
	
	/**
	 * Cluster a population and reduce it by a certain ratio, then optimize the remaining individuals for a given number of function calls with a HC.
	 * Return a pair of the optimized population and the improvement in the mean fitness (not normed) that was achieved by the HC run. The returned
	 * individuals are deep clones, so the given population is not altered. Of a cluster of individuals, the given
	 * ratio of individuals is kept, more precisely, the best one is kept while the remaining are selected randomly. All loners are kept. 
	 * 
	 * @param pop	the population to work on
	 * @param problem	the target problem instance 
	 * @param sigmaCluster	minimum clustering distance 
	 * @param funCalls	number of function calls for the optimization step
	 * @param keepClusterRatio	of a cluster of individuals, this ratio of individuals is kept for optimization  
	 * @param mute	the mutation operator to be used by the hill climber
	 * @return a pair of the optimized population and the improvement in the mean fitness (not normed) achieved by the HC run
	 */
	public static Pair<Population, Double> clusterHC(Population pop, AbstractOptimizationProblem problem, double sigmaCluster, int funCalls, double keepClusterRatio, InterfaceMutation mute) {
		Population clust = (Population)clusterBest(pop, new ClusteringDensityBased(sigmaCluster, 2), keepClusterRatio, KEEP_LONERS, BEST_RAND).clone();
//		System.out.println("keeping " + clust.size() + " for hc....");
		double[] meanFit = clust.getMeanFitness();
		processWithHC(clust, problem, new EvaluationTerminator(pop.getFunctionCalls()+funCalls), mute);
		double improvement = PhenotypeMetric.euclidianDistance(meanFit, clust.getMeanFitness());
		if (TRACE) System.out.println("improvement by " + improvement);
		return new Pair<Population, Double>(clust, improvement);
	}
	
//	/**
//	 * Do some post processing on a multimodal problem. If the real optima are known, only the number of
//	 * found optima is printed. Otherwise, the population is clustered and for the population of cluster-representatives, 
//	 * some diversity measures and a fitness histogram are printed.
//	 * 
//	 * @see Population.getPopulationMeasures()
//	 * @see createFitNormHistogram
//	 * @param mmProb	the target problem
//	 * @param pop	the solution set
//	 * @param sigmaCluster	the min clustering distance 
//	 * @param out	a PrintStream for data output
//	 * @param minBnd	lower fitness bound
//	 * @param maxBnd	upper fitness bound
//	 * @param bins	number of bins for the fitness histogram
//	 * @return 
//	 */
//	public static Population outputResult(AbstractOptimizationProblem mmProb, Population pop, double sigmaCluster, PrintStream out, double minBnd, double maxBnd, int bins, int hcSteps) {
//		ClusteringDensityBased clust = new ClusteringDensityBased(sigmaCluster);
//		clust.setMinimumGroupSize(2);
//		Population clusteredBest = clusterBest(pop, clust, 0, KEEP_LONERS, BEST_ONLY);
//		if (hcSteps > 0) { // HC post process
//			Population tmpPop = (Population)clusteredBest.clone();
//			processWithHC(tmpPop, (AbstractOptimizationProblem)mmProb, hcSteps, 0.001);
//			clusteredBest = clusterBest(tmpPop, clust, 0, KEEP_LONERS, BEST_ONLY);
//		}
//		double[] meas = clusteredBest.getPopulationMeasures();
//		int[] sols = createFitNormHistogram(clusteredBest, minBnd, maxBnd, bins);
//		out.println("measures: " + BeanInspector.toString(meas));
//		out.println("solution hist.: " + BeanInspector.toString(sols));
//
//		Object[] bestArr = clusteredBest.toArray();
//		for (Object locOpt : bestArr) {
////			out.print((AbstractEAIndividual.getDefaultDataString((IndividualInterface)locOpt)));
//			out.println(AbstractEAIndividual.getDefaultStringRepresentation(((AbstractEAIndividual)locOpt)));
//		}
//		return clusteredBest;
//	}

//	public static Population outputResultKnown(InterfaceMultimodalProblemKnown mmProb, Population pop, double sigmaCluster, PrintStream out, double minBnd, double maxBnd, int bins) {
//		Population found = getFoundOptima(pop, ((InterfaceMultimodalProblemKnown)mmProb).getRealOptima(), ((InterfaceMultimodalProblemKnown)mmProb).getEpsilon(), true);
//		for (double epsilon=0.1; epsilon > 0.00000001; epsilon/=10.) {
//			//out.println("no optima found: " + ((InterfaceMultimodalProblemKnown)mmProb).getNumberOfFoundOptima(pop));
//			out.println("found " + getFoundOptima(pop, ((InterfaceMultimodalProblemKnown)mmProb).getRealOptima(), epsilon, true).size() + " for epsilon = " + epsilon);
//		}
//		out.println("max peak ratio is " + mmProb.getMaximumPeakRatio(found));
//		return found;
//	}
	
	/**
	 * Do some data output for multimodal problems with known optima. The listener may be null, but then the method is
	 * not really doing much at this state.
	 */
	public static void procMultiModalKnown(Population solutions, InterfaceMultimodalProblemKnown mmkProb, InterfaceTextListener listener) {
//		Population found = getFoundOptima(solutions, mmkProb.getRealOptima(), mmkProb.getEpsilon(), true);
		if (listener != null) {
			listener.println("number of known optima is " + mmkProb.getRealOptima().size());
			listener.println("default epsilon is " + mmkProb.getEpsilon());
			listener.println("optima found with default epsilon: " + getFoundOptima(solutions, mmkProb.getRealOptima(), mmkProb.getEpsilon(), true).size());
			listener.println("max peak ratio is " + mmkProb.getMaximumPeakRatio(getFoundOptima(solutions, mmkProb.getRealOptima(), mmkProb.getEpsilon(), true)));
			for (double epsilon=0.1; epsilon > 0.00000001; epsilon/=10.) {
				//	out.println("no optima found: " + ((InterfaceMultimodalProblemKnown)mmProb).getNumberOfFoundOptima(pop));
				listener.println("found " + getFoundOptima(solutions, mmkProb.getRealOptima(), epsilon, true).size() + " for epsilon = " + epsilon + ", maxPeakRatio: " + AbstractMultiModalProblemKnown.getMaximumPeakRatio(mmkProb,solutions, epsilon));
			}
		}
	}
	
	/**
	 * Universal post processing method, receiving parameter instance for specification.
	 * Optional clustering and HC step, output contains population measures, fitness histogram and
	 * a list of solutions after post processing.
	 * 
	 * @param params
	 * @param inputPop
	 * @param problem
	 * @param listener
	 * @return the clustered, post-processed population
	 */
	public static Population postProcess(InterfacePostProcessParams params, Population inputPop, AbstractOptimizationProblem problem, InterfaceTextListener listener) {
		if (params.isDoPostProcessing()) {
			Population clusteredPop, outputPop;
			if (params.getPostProcessClusterSigma() > 0) {
				clusteredPop = (Population)PostProcess.clusterBest(inputPop, params.getPostProcessClusterSigma(), 0, PostProcess.KEEP_LONERS, PostProcess.BEST_ONLY).clone();
				if (clusteredPop.size() < inputPop.size()) {
					if (listener != null) listener.println("Initial clustering reduced population size from " + inputPop.size() + " to " + clusteredPop.size());
				} else if (listener != null) listener.println("Initial clustering yielded no size reduction.");
			} else clusteredPop = inputPop;
			
			if (params.getPostProcessSteps() > 0) {
				int stepsDone = processWithHC(clusteredPop, problem, params.getPostProcessSteps());
				if (listener != null) listener.println("HC post processing: " + stepsDone + " steps done.");
				// some individuals may have now converged again
				if (params.getPostProcessClusterSigma() > 0) {
					// so if wished, cluster again.
					outputPop = (Population)PostProcess.clusterBest(clusteredPop, params.getPostProcessClusterSigma(), 0, PostProcess.KEEP_LONERS, PostProcess.BEST_ONLY).clone();
					if (outputPop.size() < clusteredPop.size()) {
						if (listener != null) listener.println("Second clustering reduced population size from " + clusteredPop.size() + " to " + outputPop.size());
					} else if (listener != null) listener.println("Second clustering yielded no size reduction.");
				} else outputPop = clusteredPop;
			} else outputPop = clusteredPop;
			
			double upBnd = PhenotypeMetric.norm(outputPop.getWorstEAIndividual().getFitness())*1.1;
			upBnd = Math.pow(10,Math.floor(Math.log10(upBnd)+1));
			double lowBnd = 0;
			int[] sols = PostProcess.createFitNormHistogram(outputPop, lowBnd, upBnd, 20);
//			PostProcessInterim.outputResult((AbstractOptimizationProblem)goParams.getProblem(), outputPop, 0.01, System.out, 0, 2000, 20, goParams.getPostProcessSteps());
			if (outputPop.size()>1) {
				if (listener != null) listener.println("measures: " + BeanInspector.toString(outputPop.getPopulationMeasures()));
				if (listener != null) listener.println("solution histogram in [" + lowBnd + "," + upBnd + "]: " + BeanInspector.toString(sols));
			}
			
			//////////// multimodal data output?
			if (problem instanceof InterfaceMultimodalProblemKnown) procMultiModalKnown(outputPop, (InterfaceMultimodalProblemKnown)problem, listener);
			
			Population nBestPop = outputPop.getBestNIndividuals(params.getPrintNBest()); // n individuals are returned and sorted, all of them if n<=0
			if (listener != null) listener.println("Best after post process:" + ((outputPop.size()>nBestPop.size()) ? ( "(first " + nBestPop.size() + " of " + outputPop.size() + ")") : ""));
			//////////// output some individual data
			if (listener != null) for (int i=0; i<nBestPop.size(); i++) {
				listener.println(AbstractEAIndividual.getDefaultStringRepresentation(nBestPop.getEAIndividual(i)));
			}
			return nBestPop;
		} else return inputPop;
	}
}

