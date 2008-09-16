package eva2.server.go.operators.mutation;

import java.io.Serializable;
import java.util.Arrays;

import wsi.ra.math.RNG;
import wsi.ra.math.Jama.EigenvalueDecomposition;
import wsi.ra.math.Jama.Matrix;
import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.enums.ESMutationInitialSigma;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.EvolutionStrategies;
import eva2.tools.EVAERROR;
import eva2.tools.Mathematics;
import eva2.tools.Pair;

/**
 * The collection of all population specific data required for a rank-mu-CMA optimization.
 * Besides methods for initialization, this class implements the InterfacePopulationChangedEventListener
 * and is added as listener to the population it is initialized on. The reason for this is that the
 * CMA parameters must be reinitialized whenever the population itself is initialized, so the reinit
 * event is caught and handled. As there are numerous mutator instances but only one CMA parameter
 * set per population, this way is the good one.
 *  
 * @author mkron
 *
 */
class CMAParamSet implements InterfacePopulationChangedEventListener, Serializable {
	private static final long serialVersionUID = -1940875082544233819L;
	protected double firstSigma = -1.;
	protected double sigma;
	protected double d_sig, c_sig;
	protected double[] meanX, pathC, pathS, eigenvalues;
	protected double[] weights = null;
	protected double[][] range = null;
	protected Matrix              mC;
	protected Matrix              mB;
	protected boolean firstAdaptionDone = false;
	
	public String toString() {
		return "d_sig " + d_sig + ", c_sig " + c_sig + ", sigma " + sigma + ", firstSigma " + firstSigma+ ", firstAdaptionDone " + firstAdaptionDone 
			+ ",\n meanX " + Arrays.toString(meanX) + ", pathC " + Arrays.toString(pathC)+ ", pathS " + Arrays.toString(pathS)+ ", eigenvalues " + Arrays.toString(eigenvalues)
			+ ", weights " + Arrays.toString(weights)+ ",\n mC " + mC.toString() + ",\n mB " + mB.toString();
	}

	/**
	 * Initializes a new CMAParamSet from scratch.
	 * 
	 * @see #initCMAParams(CMAParamSet, int, int, Population, double)
	 * @param mu
	 * @param lambda
	 * @param oldGen
	 * @param initialSigma
	 * @return
	 */
	public static CMAParamSet initCMAParams(int mu, int lambda, Population oldGen, double initialSigma) {
		return initCMAParams(new CMAParamSet(), mu, lambda, oldGen, initialSigma);
	}
	
	/**
	 * Initializes the CMA parameter set for given mu, lambda and a population.
	 * The initialSigma parameter is used as initial sigma directly unless it is <0, in
	 * that case the average range is used as initial sigma.
	 * The parameter instance is also added as listener to the population.
	 * 
	 * @param params	the CMA parameter set to be used - its data are overwritten
	 * @param mu	ES mu parameter
	 * @param lambda	ES lambda parameter
	 * @param pop	associated Population
	 * @param initialSigma	initial sigma or -1 to indicate the usage of average range
	 * @return
	 */
	public static CMAParamSet initCMAParams(CMAParamSet params, int mu, int lambda, Population pop, double initialSigma) {
		// those are from init:
		params.firstAdaptionDone = false;
		params.range = ((InterfaceDataTypeDouble)pop.getEAIndividual(0)).getDoubleRange();

		int dim = params.range.length;
//		if (TRACE_1) System.out.println("WCMA init " + dim);
//		if (TRACE_1) System.out.println("WCMA static init " + dim);
		params.eigenvalues = new double[dim];
		Arrays.fill(params.eigenvalues, 1.);
		params.meanX = new double[dim];
		params.pathC = new double[dim];
		params.pathS = new double[dim];

		params.mC = Matrix.identity(dim, dim);
		params.mB = Matrix.identity(dim, dim);

		// from adaptAfterSel
		params.weights = initWeights(mu, lambda);
		double muEff = getMuEff(params.weights, mu);
		params.c_sig = (muEff+2)/(muEff+dim+3);
//		c_u_sig = Math.sqrt(c_sig * (2.-c_sig));
		params.d_sig = params.c_sig+1+2*Math.max(0, Math.sqrt((muEff-1)/(dim+1)) - 1);
		
		if (initialSigma<0) initialSigma = getAvgRange(params.range);
		if (initialSigma <= 0) {
			EVAERROR.errorMsgOnce("warning: initial sigma <= zero! Working with converged population?");
			initialSigma = 10e-10;
		}
		params.sigma = initialSigma;
//		System.out.println("INitial sigma: "+sigma);
		params.firstSigma = params.sigma;
		params.meanX = pop.getCenter(); // this might be ok?
		pop.addPopulationChangedEventListener(params);
		return params;
	}

	/**
	 * Initialize the default weights for the mu update using a log scale.
	 * 
	 * @param mu
	 * @param lambda
	 * @return
	 */
	public static double[] initWeights(int mu, int lambda) {
		double[] theWeights = new double[mu];
		double sum = 0;
		int type = 0; // zero is default log scale
		for (int i=0; i<mu; i++) {
			if (type == 0) {
				theWeights[i] = (Math.log((lambda+1)/2.)-Math.log(i+1));
			} else theWeights[i] = 1.;
			sum+=theWeights[i];
		}
		for (int i=0; i<mu; i++) theWeights[i] /= sum; 
		return theWeights;
	}

	/**
	 * So-called effective mu value.
	 * 
	 * @param weights
	 * @param mu
	 * @return
	 */
	public static double getMuEff(double[] weights, int mu) {
		double res = 0, u;
		for (int i=0; i<mu;i++) {
			u = weights[i];
			res += u*u;
		}
		return 1./res;
	}

	/**
	 * The default mu_cov is equal to the mu_eff value.
	 * 
	 * @param weights
	 * @param mu
	 * @return
	 */
	public static double getMuCov(double[] weights, int mu) {
		// default parameter value ( HK03, sec. 2)
		return getMuEff(weights, mu);
	}
	
	/**
	 * Calculate the average length of the range intervals over all dimensions.
	 * 
	 * @param range
	 * @return
	 */
    public static double getAvgRange(double[][] range) {
		double sum = 0.;
		for (int i=0; i<range.length; i++) sum+=(range[i][1]-range[i][0]);
		return sum/range.length;
	}
    
	/**
	 * Make sure that the parameter sets of each population are updated (reinitialized)
	 * if a population is reinitialized.
	 * 
	 * @see InterfacePopulationChangedEventListener
	 */
	public void registerPopulationStateChanged(Object source, String name) {
		if (name.equals(Population.populationInitialized)) {
			Population pop = (Population)source;
			if (MutateESRankMuCMA.TRACE_1) System.out.println("Event " + name + " arrived in CMAParamSet!!!");
			CMAParamSet params = (CMAParamSet)(pop.getData(MutateESRankMuCMA.cmaParamsKey));
			int mu;
			if (pop.hasData(EvolutionStrategies.esMuParam)) {
				mu = (Integer)pop.getData(EvolutionStrategies.esMuParam); 
			} else {
				System.err.println("Unknown mu in reinit! using lambda/2...");
				mu = pop.size()/2;
			}
			pop.putData(MutateESRankMuCMA.cmaParamsKey, CMAParamSet.initCMAParams(params, mu, pop.size(), pop, params.firstSigma));
		}
	}
}

/**
 * Implementing CMA ES with rank-mu-update and weighted recombination. More information can be found here:
 * - http://www.bionik.tu-berlin.de/user/niko/cmaesintro.html
 * - N.Hansen & S.Kern 2004: Evaluating the CMA Evolution Strategy on Multimodal Test Functions.
 *   Parallel Problem Solving from Nature 2004.
 * - For the stopping criteria: Auger&Hansen, CEC '05, A Restart CMA ES with increasing population size. 
 * 
 * The implementation uses a structure for keeping all adaptive parameters, CMAParamSet, which is stored
 * in the populations, so that in principle, multi-modal optimization with several populations becomes possible.
 * This of course requires proper handling of the generational cycle, i.e., new generations should be cloned from
 * the former ones (without individuals is ok) so that the parameters are taken over.
 * 
 * @author mkron
 *
 */
public class MutateESRankMuCMA implements InterfaceMutationGenerational, Serializable {
//	int dim;
	private double c_c, expRandStepLen;
	
	// The static member lastParams is used to store the parameter set last seen in an adaption step, which
	// is then later used for the individual mutations. Necessary because each individual has its own mutator
	// instance. It would be cleaner to keep all parameters within the individual and have just one mutation
	// instance, however this would create quite a big baustelle.
	private static transient CMAParamSet lastParams=null;

	private ESMutationInitialSigma initializeSig = ESMutationInitialSigma.avgInitialDistance;
	private double userDefInitSig = 0.2;
	public static final String cmaParamsKey = "RankMuCMAParameters";
	
	public static boolean TRACE_1 = false;
	public static boolean TRACE_2 = false;
	public static boolean TRACE_TEST = false;

	public MutateESRankMuCMA() {
	}

	public MutateESRankMuCMA(MutateESRankMuCMA mutator) {
		this.c_c            = mutator.c_c;
		this.expRandStepLen     = mutator.expRandStepLen;
		this.initializeSig = mutator.initializeSig;
	}

	public Object clone() {
//		if (TRACE) System.out.println("WCMA clone");
		return new MutateESRankMuCMA(this);
	}

	/**
	 * Retrieve the initial sigma for the given population and the user defined method.
	 * For the halfRange case, -1 is returned, as the range is not available here.
	 * @param initGen
	 * @return
	 */
	private double getInitSigma(Population initGen) {
		switch (initializeSig) {
		case avgInitialDistance: 
			// scaled by average range as the measures are normed
			//return initGen.getPopulationMeasures(null)[0]*getAvgRange();
			// use euclidian measures without normation and scaling
			return initGen.getPopulationMeasures(null)[0];
		//case halfRange: return getAvgRange(range)/2.;
		case userDefined: return userDefInitSig ;
		default: return -1.;
		}
	}
	
	/**
	 * Perform the main adaption of sigma and C using evolution paths.
	 * The evolution path is deduced from the center of the selected population compared to the old
	 * mean value.
	 * See Hansen&Kern 04 for further information.
	 * @param oldGen
	 * @param selectedP
	 */
	public void adaptAfterSelection(Population oldGen, Population selectedP) {
		Population selectedSorted = selectedP.getSortedBestFirst();
		
		int mu,lambda;
		mu = selectedP.size();
		lambda = oldGen.size();
		if (mu>= lambda) {
			EVAERROR.errorMsgOnce("Warning: invalid mu/lambda ratio! Setting mu to lambda/2.");
			mu = lambda/2;
		}
		CMAParamSet params;
		if (oldGen.getGeneration() <= 1) { // init new param set. At gen < 1 we shouldnt be called, but better do it once too often
			if (oldGen.hasData(cmaParamsKey)) params = CMAParamSet.initCMAParams((CMAParamSet)oldGen.getData(cmaParamsKey), mu, lambda, oldGen, getInitSigma(oldGen));
			else params = CMAParamSet.initCMAParams(mu, lambda, oldGen, getInitSigma(oldGen));			
		} else {
			if (!oldGen.hasData(cmaParamsKey)) {
				if (oldGen.getGeneration() > 1) EVAERROR.errorMsgOnce("Error: population lost cma parameters. Incompatible optimizer?");
				params = CMAParamSet.initCMAParams(mu, lambda, oldGen, getInitSigma(oldGen));
			} else params = (CMAParamSet)oldGen.getData(cmaParamsKey);
		}
		
		int generation = oldGen.getGeneration();
		
		if (TRACE_1) {
			System.out.println("WCMA adaptGenerational **********");
//			System.out.println("newPop measures: " + BeanInspector.toString(newPop.getPopulationMeasures()));
			System.out.println("mu_eff: " + CMAParamSet.getMuEff(params.weights, mu));
			System.out.println(params.toString());
			System.out.println("*********************************");
		}

		double[] newMeanX = calcMeanX(params.weights, selectedSorted);
		if (TRACE_1) System.out.println("newMeanX:  " + BeanInspector.toString(newMeanX));
		
		int dim=params.meanX.length;
        double[] BDz = new double[dim];
        for (int i=0; i<dim; i++) { /* calculate xmean and BDz~N(0,C) */
        	// Eq. 4 from HK04, most right term
        	BDz[i] = Math.sqrt(CMAParamSet.getMuEff(params.weights, mu)) * (newMeanX[i] - params.meanX[i]) / getSigma(params, i);
        }
//        if (TRACE_2) System.out.println("BDz is " + BeanInspector.toString(BDz));

		double[] newPathS = params.pathS.clone();
		double[] newPathC = params.pathC.clone();
		
		double[] zVect = new double[dim];
        /* calculate z := D^(-1) * B^(-1) * BDz into artmp, we could have stored z instead */
        for (int i = 0; i < dim; ++i) {
        	double sum=0.;
            for (int j = 0; j < dim; ++j) {
                sum += params.mB.get(j,i) * BDz[j]; // times B transposed, (Eq 4) in HK04
            }
            zVect[i] = sum / Math.sqrt(params.eigenvalues[i]);
        }

        /* cumulation for sigma (ps) using B*z */
		for (int i = 0; i < dim; ++i) {
			double sum = 0.;
			for (int j = 0; j < dim; ++j) sum += params.mB.get(i,j) * zVect[j];
			newPathS[i] = (1. - params.c_sig) * params.pathS[i]
			              + Math.sqrt(params.c_sig * (2. - params.c_sig)) * sum;
		}
//		System.out.println("pathS diff: " + BeanInspector.toString(Mathematics.vvSub(newPathS, pathS)));
//		System.out.println("newPathS is " + BeanInspector.toString(newPathS));
        
		double psNorm = Mathematics.norm(newPathS);
		
        double hsig = 0;
        if (psNorm / Math.sqrt(1. - Math.pow(1. - params.c_sig, 2. * generation))
                / expRandStepLen < 1.4 + 2. / (dim + 1.)) {
            hsig = 1;
        }
        for (int i = 0; i < dim; ++i) {
            newPathC[i] = (1. - getCc()) * params.pathC[i] + hsig
            * Math.sqrt(getCc() * (2. - getCc())) * BDz[i];
        }
		
        // TODO missing: "remove momentum in ps"
        
		if (TRACE_1) {
			System.out.println("newPathC: " + BeanInspector.toString(newPathC));
			System.out.println("newPathS: " + BeanInspector.toString(newPathS));
		}

		if (TRACE_1) System.out.println("Bef: C is \n" + params.mC.toString());
		if (params.meanX == null) params.meanX = newMeanX;
		
		updateCov(params, newPathC, newMeanX, hsig, mu, selectedSorted);
		updateBD(params);
			
		if (TRACE_2) System.out.println("Aft: C is \n" + params.mC.toString());

        /* update of sigma */
		double sigFact = Math.exp(((psNorm / expRandStepLen) - 1) * params.c_sig
                / params.d_sig);
		if (Double.isInfinite(sigFact)) params.sigma *= 10.; // in larger search spaces sigma tends to explode after init.  
		else params.sigma *= sigFact;

        if (Double.isInfinite(params.sigma) || Double.isNaN(params.sigma)) {
        	System.err.println("Error, unstable sigma!");
        }
		testAndCorrectNumerics(params, generation, selectedSorted);
        
		if (TRACE_1) {
			System.out.print("psLen=" + (psNorm) + " ");
			outputParams(params, mu);
		}
		
		// take over data
		params.meanX = newMeanX;
		params.pathC = newPathC;
		params.pathS = newPathS;
		params.firstAdaptionDone = true;
		
		lastParams = params;
		oldGen.putData(cmaParamsKey, params);
		selectedP.putData(cmaParamsKey, params);
//		if (TRACE_2) System.out.println("sampling around " + BeanInspector.toString(meanX));
	}

	/**
	 * Expects newPop to have correct number of generations set.
	 */
	public void adaptGenerational(Population oldPop, Population selectedPop, 
			Population newPop, boolean updateSelected) {
		// nothing to do? Oh yes, we can easily transfer the cma-params from the old to the new population. 
		if (!newPop.hasData(cmaParamsKey)) {
			if (!oldPop.hasData(cmaParamsKey)) System.err.println("warning: no cma param set found (MutateESRankMuCMA!");
			else newPop.putData(cmaParamsKey, oldPop.getData(cmaParamsKey));
		}
		//newPop.addData(cmaParamsKey, oldPop.getData(cmaParamsKey));
		//newPop.addPopulationChangedEventListener(this); // listen to every population to be informed about reinit events
	}

	/**
	 * Requires selected population to be sorted by fitness.
	 * 
	 * @param iterations
	 * @param selected
	 */
    void testAndCorrectNumerics(CMAParamSet params, int iterations, Population selected) { // not much left here
    	/* Flat Fitness, Test if function values are identical */
    	if (iterations > 1) {
    		// selected pop is sorted
    		if (nearlySame(selected.getEAIndividual(0).getFitness(),selected.getEAIndividual(selected.size()-1).getFitness())) {
    			if (TRACE_1) System.err.println("flat fitness landscape, consider reformulation of fitness, step-size increased");
    			params.sigma *= Math.exp(0.2+params.c_sig/params.d_sig);
//    			sigma=0.1;
    		}
    	}
    	/* Align (renormalize) scale C (and consequently sigma) */
    	/* e.g. for infinite stationary state simulations (noise
    	 * handling needs to be introduced for that) */
    	double fac = 1.;
    	double minEig = 1e-12;
    	double maxEig = 1e8;
    	if (Mathematics.max(params.eigenvalues) < minEig) 
    		fac = 1./Math.sqrt(Mathematics.max(params.eigenvalues));
    	else if (Mathematics.min(params.eigenvalues) > maxEig)
    		fac = 1./Math.sqrt(Mathematics.min(params.eigenvalues));

    	if (fac != 1.) {
    		System.err.println("Scaling by " + fac);
    		params.sigma /= fac;
    		for(int i = 0; i < params.meanX.length; ++i) {
    			params.pathC[i] *= fac;
    			params.eigenvalues[i] *= fac*fac;
    			for (int j = 0; j <= i; ++j) {
    				params.mC.set(i, j, params.mC.get(i,j)*fac*fac);
    				if (i!=j) params.mC.set(j, i, params.mC.get(i,j));
    			}
    		}
    	}
    } // Test...
	
	private boolean nearlySame(double[] bestFitness, double[] worstFitness) {
		double epsilon = 1e-14;
		for (int i=0; i<bestFitness.length; i++) if (Math.abs(bestFitness[i]-worstFitness[i])>epsilon) return false;
		return true;
	}

	/**
	 * Return the range scaled sigma parameter for dimension i.
	 *  
	 * @param i
	 * @return
	 */
	private double getSigma(CMAParamSet params, int i) {
		return params.sigma;
	}
//
//	private double getDamps() {
//		return d_sig;
//	}

	private double getCc() {
		return c_c;
	}

//	private double getCs() {
//		return c_sig;
//	}

	private double calcExpRandStepLen(int dim) {
		// scale by avg range?
		return Math.sqrt(dim)*(1.-(1./(4*dim))+(1./(21*dim*dim)));
	}

	/* update C */
	private void updateCov(CMAParamSet params, double[] newPathC, double[] newMeanX, double hsig, int mu, Population selected) {
		double newVal = 0;
    	int dim = newMeanX.length;
		double ccv = getCCov(params.weights, mu, dim);
        if (ccv > 0) {
        	double mcv = CMAParamSet.getMuCov(params.weights, mu);
        	/* (only upper triangle!) */
            /* update covariance matrix */
        	//System.out.println("CCov " + getCCov(selected) + " Cc " + getCc() + " muCov " + getMuCov(selected));
            for (int i = 0; i < dim; ++i)
                for (int j = 0; j <= i; ++j) {
//                	oldVal = mC.get(i,j);
                	newVal = (1 - ccv) * params.mC.get(i,j)
                    + ccv
                    * (1. / mcv)
                    * (newPathC[i] * newPathC[j] + (1 - hsig) * getCc()
                            * (2. - getCc()) * params.mC.get(i,j));
                	params.mC.set(i,j,newVal);
                    for (int k = 0; k < mu; ++k) { /*
                    * additional rank mu
                    * update
                    */
                    	double[] x_k = ((InterfaceDataTypeDouble)selected.getEAIndividual(k)).getDoubleData();
                    	newVal = params.mC.get(i,j)+ ccv * (1 - 1. / mcv)
                        * params.weights[k]	* (x_k[i] - params.meanX[i])
                        							* (x_k[j] - params.meanX[j]) / (getSigma(params, i) * getSigma(params, j)); // TODO right sigmas?
                    	params.mC.set(i,j, newVal);
                    }
                }
            // fill rest of C
            for (int i = 0; i < dim; ++i) {
                for (int j = i+1; j < dim; ++j) {

                	params.mC.set(i, j, params.mC.get(j,i));
                }
                
            }
        	if (params.mC.get(0,1) != params.mC.get(1,0)) {
        		System.err.println("WARNING, C is not symmetric!");
        	}            
//            maxsqrtdiagC = Math.sqrt(math.max(math.diag(C)));
//            minsqrtdiagC = Math.sqrt(math.min(math.diag(C)));
        } // update of C
        
	}

	private double getCCov(double[] weights, int mu, int dim) {
		// ( HK03, sec. 2)
		//return Math.min(1., 2*getMuEff(selected)/(dim*dim));
		double muC=CMAParamSet.getMuCov(weights, mu);
		double muE=CMAParamSet.getMuEff(weights,mu);
		double ccov = (2./(muC*Math.pow(dim+Math.sqrt(2.), 2)))+(1.-(1./muC))*Math.min(1., (2*muE-1.)/(dim*dim+2*dim+4+muE));
		return ccov;
	}
	
	private void outputParams(CMAParamSet params, int mu) {
		System.out.println("sigma=" + params.sigma + " chiN="+expRandStepLen + " cs="+params.c_sig
				+ " damps="+params.d_sig + " Cc=" + getCc() + " Ccov=" + getCCov(params.weights, mu, params.meanX.length) 
				+ " mueff=" + CMAParamSet.getMuEff(params.weights, mu) + " mucov="+CMAParamSet.getMuCov(params.weights, mu));
	}

	private void updateBD(CMAParamSet params) {
//		C=triu(C)+transpose(triu(C,1)); % enforce symmetry
//		[B,D] = eig(C);
//		% limit condition of C to 1e14 + 1
//		if max(diag(D)) > 1e14*min(diag(D))
//		tmp = max(diag(D))/1e14 - min(diag(D));
//		C = C + tmp*eye(N);
//		D = D + tmp*eye(N);
//		end
//		D = diag(sqrt(diag(D))); % D contains standard deviations now
//		BD = B*D; % for speed up only
		////////////////////////////////////////////7
		params.mC = (params.mC.plus(params.mC.transpose()).times(0.5)); // MAKE C SYMMETRIC

		EigenvalueDecomposition helper;
//		this.m_Counter      = 0;
		helper              = new EigenvalueDecomposition(params.mC);
		params.mB              = helper.getV(); // Return the eigenvector matrix
		params.eigenvalues  = helper.getRealEigenvalues();
		
//		double[] sqrtEig = eigenvalues.clone();
//		for (int i = 0; i < sqrtEig.length; i++) {
//			sqrtEig[i] = Math.sqrt(eigenvalues[i]);
//		}
//		mB.times(sqrtEig, mBD);
		
//		Matrix test = (Matrix)mB.clone();
//		System.out.println(test);
//		System.out.println(test.transpose());
//		System.out.println(test.times(test.transpose()));
//		test = (Matrix)mB.clone();
//		double[] mult = new double[dim];
//		int spalte=3;
//		for (int i=0; i<dim; i++) {
//			System.out.println(test.get(i,spalte));
//			for (int j=0; j<dim; j++) mult[i] += mC.get(i,j)*test.get(j, spalte);
//		}
//		System.out.println("eigenv: " + eigenvalues[spalte]);
//		System.out.println(BeanInspector.toString(mult));
//		System.exit(1);
	}

	/**
	 *  calculate weighted mean of the selected population
	 * @param selectedPop
	 * @return
	 */
	private double[] calcMeanX(double[] weights, Population selectedPop) {
		return selectedPop.getCenterWeighted(weights);
	}
	
	public void crossoverOnStrategyParameters(AbstractEAIndividual indy1,
			Population partners) {
		// nothing to do

	}

	public String getName() {
		return "Rank-Mu-CMA-Mutator";
	}

	public String getStringRepresentation() {
		return "Rank-Mu-CMA-Mutator";
	}

	public String globalInfo() {
		return "The CMA mutator scheme with static cov. matrix, rank-mu update and weighted recombination.";
	}
	
	public void init(AbstractEAIndividual individual,
			InterfaceOptimizationProblem opt) {
//		firstAdaptionDone = false;
//		range = ((InterfaceDataTypeDouble)individual).getDoubleRange();
//		dim = range.length;
//		if (TRACE_1) System.out.println("WCMA init " + dim);
//		c_c = (4./(dim+4));
//		c_sig = Double.NaN; // mark as not yet initialized
////		c_u_sig = Double.NaN;
//		d_sig = Double.NaN; // init in first adaption step!  
//		if (TRACE_1) System.out.println("WCMA static init " + dim);
//		eigenvalues = new double[dim];
//		Arrays.fill(eigenvalues, 1.);
//		
//		meanX = new double[dim];
//		pathC = new double[dim];
//		pathS = new double[dim];
////		mBD = new double[dim];
//		mC = Matrix.identity(dim, dim);
//		mB = Matrix.identity(dim, dim);
//		sigma = 0.2;
		// ^^^ moved to CMAParamSet initialization
		double[][] range = ((InterfaceDataTypeDouble)individual).getDoubleRange();
		int dim = range.length;
		c_c = (4./(dim+4));
		expRandStepLen = calcExpRandStepLen(dim);

	}

	public void mutate(AbstractEAIndividual individual) {
//		if (!firstAdaptionDone) {
//			if (TRACE) System.out.println("No mutation before first adaptions step");
//			return;
//		}
		if (individual instanceof InterfaceDataTypeDouble) {
			double[]    x       = ((InterfaceDataTypeDouble)individual).getDoubleData();
//			if (TRACE) System.out.println("WCMA mutate, bef: " + BeanInspector.toString(x));
			double[][]  range  = ((InterfaceDataTypeDouble)individual).getDoubleRange();
			
			// this is a critical point: where do the CMA parameters for this individual's mutation come from?
			// for GA and ES we can expect that selection occured directly before the mutation cycle,
			// so we take the parameter set from the last adpation step.
			((InterfaceDataTypeDouble)individual).SetDoubleGenotype(mutate(lastParams, x, range, 0));
			
//			if (TRACE) System.out.println("WCMA mutate, aft: " + BeanInspector.toString(x));
		} else System.err.println("Error, expecting InterfaceDataTypeDouble");
	}

	private double[] mutate(CMAParamSet params, double[] x, double[][] range, int count) {
		int dim = range.length;
		if (params!=null && (params.firstAdaptionDone)) {
			double[] sampl = new double[dim]; // generate scaled random vector (D * z)
			for (int i = 0; i < dim; ++i) {
				sampl[i] = Math.sqrt(params.eigenvalues[i]) * RNG.gaussianDouble(1.);
			}
//			System.out.println("Sampling around " + BeanInspector.toString(meanX));
			/* add mutation (sigma * B * (D*z)) */
			for (int i = 0; i < dim; ++i) {
				double sum = 0.;
				for (int j = 0; j < dim; ++j)
					sum += params.mB.get(i,j) * sampl[j];
				x[i] = params.meanX[i]+getSigma(params, i)*sum;
			}
		} else {
			if (params==null) {
				System.err.println("Error in MutateESRankMuCMA: parameter set was null! Skipping mutation...");
			}			// no valid meanX yet, so just do a gaussian jump with sigma
			for (int i = 0; i < dim; ++i) {
				x[i] += RNG.gaussianDouble(getSigma(params, i));
			}
		}
		if (Mathematics.isInRange(x, range)) return x;
		else {
			if (count > 5) return repairMutation(x, range); // allow some nice tries before using brute force
			else return mutate(params, x, range, count+1); // for really bad initial deviations this might be a quasi infinite loop
		}
	}
	
	private double[] repairMutation(double[] x, double[][] range) {
// TODO	    % You may handle constraints here. You may either resample
//	    % arz(:,k) and/or multiply it with a factor between -1 and 1
//	    % (the latter will decrease the overall step size) and
//	    % recalculate arx accordingly. Do not change arx or arz in any
//	    % other way.
		for (int i=0; i<x.length; i++) {
			if (x[i]<range[i][0]) x[i]=range[i][0];
			else if (x[i]>range[i][1]) x[i]=range[i][1];
		}
		return x;	
	}

	/**
	 * After optimization start, this returns the initial sigma value
	 * actually employed.
	 * 
	 * @return the initial sigma value actually employed 
	 */
	public double getFirstSigma(Population pop) {
		return ((CMAParamSet)pop.getData(cmaParamsKey)).firstSigma;
	}
	
	public void hideHideable() {
		this.setInitializeSigma(getInitializeSigma());
	}
	
	/**
	 * @return the initialSig
	 */
	public ESMutationInitialSigma getInitializeSigma() {
		return initializeSig;
	}

	/**
	 * @param initialSig the initialSig to set
	 */
	public void setInitializeSigma(ESMutationInitialSigma initialSig) {
		this.initializeSig = initialSig;
		GenericObjectEditor.setHideProperty(this.getClass(), "userDefInitSig", initialSig!=ESMutationInitialSigma.userDefined);
	}

	public String initializeSigmaTipText() {
		return "Method to use for setting the initial step size.";
	}

	/**
	 * From Auger&Hansen, CEC '05, stopping criterion TolX.
	 * 
	 * @param tolX
	 * @return
	 */
	public boolean testAllDistBelow(Population pop, double tolX) {
//		if all(sigma*(max(abs(pc), sqrt(diag(C)))) < stopTolX) 
		boolean res = true;
		CMAParamSet params = (CMAParamSet)pop.getData(cmaParamsKey);
		int i=0;
		while (res && i<params.meanX.length) {
			res = res && (getSigma(params, i)*Math.max(Math.abs(params.pathC[i]), Math.sqrt(params.mC.get(i,i))) < tolX);
			i++;
		}
		if (TRACE_TEST) if (res) System.out.println("testAllDistBelow hit");
		return res;
	}

	/**
	 * From Auger&Hansen, CEC '05, stopping criterion noeffectaxis.
	 * @param d
	 * @param gen
	 * @return
	 */
	public boolean testNoChangeAddingDevAxis(Population pop, double d, int gen) {
//		  if all(xmean == xmean + 0.1*sigma*BD(:,1+floor(mod(countiter,N))))
//	    i = 1+floor(mod(countiter,N));
//		stopflag(end+1) = {'warnnoeffectaxis'};
		CMAParamSet params = (CMAParamSet)pop.getData(cmaParamsKey);
		int dim=params.meanX.length;
		int k = gen%dim;
		double[] ev_k = params.mB.getColumn(k);
		Mathematics.svMult(Math.sqrt(params.eigenvalues[k]), ev_k, ev_k); // this is now e_k*v_k = BD(:,...)
		
		int i=0;
		boolean res = true;
		while (res && (i<dim)) {
			res = res && (params.meanX[i] == (params.meanX[i] + d*getSigma(params, i)*ev_k[i]));
			i++;
		}
		if (TRACE_TEST) if (res) System.out.println("testNoChangeAddingDevAxis hit");
		return res;
	}

	/**
	 * From Auger&Hansen, CEC '05, stopping criterion noeffectcoord.
	 * @param d
	 * @return
	 */
	public boolean testNoEffectCoord(Population pop, double d) {
//		if any(xmean == xmean + 0.2*sigma*sqrt(diag(C))) 
//		stopflag(end+1) = {'warnnoeffectcoord'};
		boolean ret = false;
		CMAParamSet params = (CMAParamSet)pop.getData(cmaParamsKey);
		int i=0;
		while ((i<params.meanX.length) && !ret) {
			ret = ret || (params.meanX[i]==(params.meanX[i] + d*getSigma(params, i)*Math.sqrt(params.mC.get(i, i))));
			i++;
		}
		if (TRACE_TEST) if (ret) System.out.println("testNoEffectCoord hit");
		return ret;
	}

	/**
	 * Test condition of C (Auger&Hansen, CEC '05, stopping criterion conditioncov).
	 * Return true, if a diagonal entry is <= 0 or >= d.
	 * 
	 * @param d
	 * @return true, if a diagonal entry is <= 0 or >= d, else false
	 */
	public boolean testCCondition(Population pop, double d) {
//	    if (min(diag(D)) <= 0) || (max(diag(D)) > 1e14*min(diag(D))) 
//		  stopflag(end+1) = {'warnconditioncov'};
		CMAParamSet params = (CMAParamSet)pop.getData(cmaParamsKey);
		Pair<Double,Double> minMax = params.mC.getMinMaxDiag();
		if ((minMax.head <= 0) || (minMax.tail >= d)) {		
			if (TRACE_TEST) System.out.println("testCCondition hit");
			return true;
		} else return false;
	}

	/**
	 * @return the userDefInitSig
	 */
	public double getUserDefInitSig() {
		return userDefInitSig;
	}

	/**
	 * @param userDefInitSig the userDefInitSig to set
	 */
	public void setUserDefInitSig(double userDefInitSig) {
		this.userDefInitSig = userDefInitSig;
	}
}