package eva2.server.go.operators.mutation;

import java.io.Serializable;
import java.util.Arrays;

import wsi.ra.math.RNG;
import wsi.ra.math.Jama.EigenvalueDecomposition;
import wsi.ra.math.Jama.Matrix;
import eva2.gui.BeanInspector;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.Mathematics;
import eva2.tools.Pair;


/**
 * Implementing CMA ES with rank-mu-update and weighted recombination. This is partly based on the
 * java implementation provided on http://www.bionik.tu-berlin.de/user/niko/cmaes_inmatlab.html.
 * 
 * N.Hansen & S.Kern 2004: Evaluating the CMA Evolution Strategy on Multimodal Test Functions.
 * Parallel Problem Solving from Nature 2004.
 * 
 * @author mkron
 *
 */
public class MutateESRankMuCMA implements InterfaceMutationGenerational, Serializable {
	int dim;
	private double c_c, expRandStepLen;
	private double[] z, zCor;

	private InitialSigmaEnum initialSig = InitialSigmaEnum.avgInitialDistance;
	private static double firstSigma = -1.;
	private static double sigma;
	private static double d_sig, c_sig;
	private static double[] meanX, pathC, pathS, eigenvalues;
	private static double[] weights = null;
	private static double[][] range = null;
	private static Matrix              mC;
	private static Matrix              mB;
//	private static double[]				mBD;
	private static boolean firstAdaptionDone = false;
	private static boolean TRACE_1 = false;
	private static boolean TRACE_2 = false;
	private static boolean TRACE_TEST = false;
//	private Matrix				BD;

	public MutateESRankMuCMA() {
		firstAdaptionDone = false;
	}

	public MutateESRankMuCMA(MutateESRankMuCMA mutator) {
		this.c_c            = mutator.c_c;
//		this.c_sig            = mutator.c_sig;
//		this.c_u_sig            = mutator.c_u_sig;
//		this.d_sig            = mutator.d_sig;
		this.expRandStepLen     = mutator.expRandStepLen;
		this.dim			= mutator.dim;
		this.initialSig = mutator.initialSig;

//		if (mutator.meanX != null)    this.meanX    = (double[]) mutator.meanX.clone();
//		if (mutator.pathC != null)  this.pathC  = (double[]) mutator.pathC.clone();
//		if (mutator.pathS != null)     this.pathS     = (double[]) mutator.pathS.clone();
		if (mutator.z != null)    this.z    = (double[]) mutator.z.clone();
		if (mutator.zCor != null)    this.zCor    = (double[]) mutator.zCor.clone();
//		if (mutator.eigenvalues != null) this.eigenvalues = (double[]) mutator.eigenvalues.clone();
//		if (mutator.mC != null)    this.mC    = (Matrix) mutator.mC.clone();
//		if (mutator.mB != null)      this.mB      = (Matrix) mutator.mB.clone();
	}

	public Object clone() {
//		if (TRACE) System.out.println("WCMA clone");
		return new MutateESRankMuCMA(this);
	}

	/**
	 * Retrieve the initial sigma for the given population and the user defined method.
	 * @param initGen
	 * @return
	 */
	private double getInitSigma(Population initGen) {
		switch (initialSig) {
		case avgInitialDistance: return initGen.getPopulationMeasures()[0];
		case halfRange: return getAvgRange()/2.;
		default: return 0.2;
		}
	}
	
	public void adaptAfterSelection(Population oldGen, Population selectedP) {
		Population selectedSorted = selectedP.getSortedBestFirst();
		
		int mu,lambda;
		mu = selectedP.size();
		lambda = oldGen.size();
		if (mu>= lambda) {
			EVAERROR.errorMsgOnce("Warning: invalid mu/lambda ratio! Setting mu to lambda/2.");
			mu = lambda/2;
		}
		if (!firstAdaptionDone) {
			initWeights(mu, lambda);
			double muEff = getMuEff(mu);
			c_sig = (muEff+2)/(muEff+dim+3);
//			c_u_sig = Math.sqrt(c_sig * (2.-c_sig));
			d_sig = c_sig+1+2*Math.max(0, Math.sqrt((muEff-1)/(dim+1)) - 1);
			sigma = getInitSigma(oldGen);
			firstSigma = sigma;
			meanX = oldGen.getCenter(); // this might be ok?
		}
		
		int generation = oldGen.getGeneration();
		
		if (TRACE_1) {
			System.out.println("WCMA adaptGenerational");
//			System.out.println("newPop measures: " + BeanInspector.toString(newPop.getPopulationMeasures()));
			System.out.println("mu_eff: " + getMuEff(mu));
			System.out.println("meanX: " + BeanInspector.toString(meanX));
			System.out.println("pathC: " + BeanInspector.toString(pathC));
			System.out.println("pathS: " + BeanInspector.toString(pathS));
		}

		double[] newMeanX = calcMeanX(selectedSorted);
		if (TRACE_1) System.out.println("newMeanX:  " + BeanInspector.toString(newMeanX));
		
        double[] BDz = new double[dim];
        for (int i=0; i<dim; i++) { /* calculate xmean and BDz~N(0,C) */
        	// Eq. 4 from HK04, most right term
        	BDz[i] = Math.sqrt(getMuEff(mu)) * (newMeanX[i] - meanX[i]) / getSigma(i);
        }
//        if (TRACE_2) System.out.println("BDz is " + BeanInspector.toString(BDz));

		double[] newPathS = pathS.clone();
		double[] newPathC = pathC.clone();
		
		double[] zVect = new double[dim];
        /* calculate z := D^(-1) * B^(-1) * BDz into artmp, we could have stored z instead */
        for (int i = 0; i < dim; ++i) {
        	double sum=0.;
            for (int j = 0; j < dim; ++j) {
                sum += mB.get(j,i) * BDz[j]; // times B transposed, (Eq 4) in HK04
            }
            zVect[i] = sum / Math.sqrt(eigenvalues[i]);
        }

        /* cumulation for sigma (ps) using B*z */
		for (int i = 0; i < dim; ++i) {
			double sum = 0.;
			for (int j = 0; j < dim; ++j) sum += mB.get(i,j) * zVect[j];
			newPathS[i] = (1. - getCs()) * pathS[i]
			              + Math.sqrt(getCs() * (2. - getCs())) * sum;
		}
//		System.out.println("pathS diff: " + BeanInspector.toString(Mathematics.vvSub(newPathS, pathS)));
//		System.out.println("newPathS is " + BeanInspector.toString(newPathS));
        
		double psNorm = Mathematics.norm(newPathS);
		
        double hsig = 0;
        if (psNorm / Math.sqrt(1. - Math.pow(1. - getCs(), 2. * generation))
                / expRandStepLen < 1.4 + 2. / (dim + 1.)) {
            hsig = 1;
        }
        for (int i = 0; i < dim; ++i) {
            newPathC[i] = (1. - getCc()) * pathC[i] + hsig
            * Math.sqrt(getCc() * (2. - getCc())) * BDz[i];
        }
		
        // TODO missing: "remove momentum in ps"
        
		if (TRACE_1) {
			System.out.println("newPathC: " + BeanInspector.toString(newPathC));
			System.out.println("newPathS: " + BeanInspector.toString(newPathS));
		}

		if (TRACE_1) System.out.println("Bef: C is \n" + mC.toString());
		if (meanX == null) meanX = newMeanX;
		
		updateCov(newPathC, newMeanX, hsig, mu, selectedSorted);
		updateBD();
			
		if (TRACE_2) System.out.println("Aft: C is \n" + mC.toString());

        /* update of sigma */
        sigma *= Math.exp(((psNorm / expRandStepLen) - 1) * getCs()
                / getDamps());
        if (Double.isInfinite(sigma) || Double.isNaN(sigma)) {
        	System.err.println("Error, unstable sigma!");
        }
		testAndCorrectNumerics(generation, selectedSorted);
        
//		System.out.println("sigma=" + sigma + " psLen=" + (psNorm) + " chiN="+expRandStepLen + " cs="+getCs()+ " damps="+getDamps() + " diag " + BeanInspector.toString(eigenvalues));
		if (TRACE_1) {
			System.out.print("psLen=" + (psNorm) + " ");
			outputParams(mu);
		}
		
		// take over data
		meanX = newMeanX;
		pathC = newPathC;
		pathS = newPathS;
		firstAdaptionDone = true;
//		if (TRACE_2) System.out.println("sampling around " + BeanInspector.toString(meanX));
	}

	/**
	 * Expects newPop to have correct number of generations set.
	 */
	public void adaptGenerational(Population oldPop, Population selectedPop, 
			Population newPop, boolean updateSelected) {
		// nothing to do?	
	}

	/**
	 * Requires selected population to be sorted by fitness.
	 * 
	 * @param iterations
	 * @param selected
	 */
    void testAndCorrectNumerics(int iterations, Population selected) { // not much left here
    	/* Flat Fitness, Test if function values are identical */
    	if (iterations > 1) {
    		// selected pop is sorted
    		if (nearlySame(selected.getEAIndividual(0).getFitness(),selected.getEAIndividual(selected.size()-1).getFitness())) {
    			if (TRACE_1) System.err.println("flat fitness landscape, consider reformulation of fitness, step-size increased");
    			sigma *= Math.exp(0.2+getCs()/getDamps());
//    			sigma=0.1;
    		}
    	}
    	/* Align (renormalize) scale C (and consequently sigma) */
    	/* e.g. for infinite stationary state simulations (noise
    	 * handling needs to be introduced for that) */
    	double fac = 1.;
    	double minEig = 1e-12;
    	double maxEig = 1e8;
    	if (Mathematics.max(eigenvalues) < minEig) 
    		fac = 1./Math.sqrt(Mathematics.max(eigenvalues));
    	else if (Mathematics.min(eigenvalues) > maxEig)
    		fac = 1./Math.sqrt(Mathematics.min(eigenvalues));

    	if (fac != 1.) {
    		System.err.println("Scaling by " + fac);
    		sigma /= fac;
    		for(int i = 0; i < dim; ++i) {
    			pathC[i] *= fac;
    			eigenvalues[i] *= fac*fac;
    			for (int j = 0; j <= i; ++j) {
    				mC.set(i, j, mC.get(i,j)*fac*fac);
    				if (i!=j) mC.set(j, i, mC.get(i,j));
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
	private double getSigma(int i) {
		return sigma;
	}

	private double getDamps() {
		return d_sig;
	}

	private double getCc() {
		return c_c;
	}

	private double getCs() {
		return c_sig;
	}

	private double calcExpRandStepLen() {
		// scale by avg range?
		return Math.sqrt(dim)*(1.-(1./(4*dim))+(1./(21*dim*dim)));
	}

    private double getAvgRange() {
		double sum = 0.;
		for (int i=0; i<dim; i++) sum+=(range[i][1]-range[i][0]);
		return sum/dim;
	}

	/* update C */
	private void updateCov(double[] newPathC, double[] newMeanX, double hsig, int mu, Population selected) {
		double newVal = 0;
        if (getCCov(mu) > 0) {
        	/* (only upper triangle!) */
            /* update covariance matrix */
        	//System.out.println("CCov " + getCCov(selected) + " Cc " + getCc() + " muCov " + getMuCov(selected));
            for (int i = 0; i < dim; ++i)
                for (int j = 0; j <= i; ++j) {
//                	oldVal = mC.get(i,j);
                	newVal = (1 - getCCov(mu)) * mC.get(i,j)
                    + getCCov(mu)
                    * (1. / getMuCov(mu))
                    * (newPathC[i] * newPathC[j] + (1 - hsig) * getCc()
                            * (2. - getCc()) * mC.get(i,j));
                    mC.set(i,j,newVal);
                    for (int k = 0; k < mu; ++k) { /*
                    * additional rank mu
                    * update
                    */
                    	double[] x_k = ((InterfaceDataTypeDouble)selected.getEAIndividual(k)).getDoubleData();
                    	newVal = mC.get(i,j)+ getCCov(mu) * (1 - 1. / getMuCov(mu))
                        * getWeight(k)	* (x_k[i] - meanX[i])
                        							* (x_k[j] - meanX[j]) / (getSigma(i) * getSigma(j)); // TODO right sigmas?
                        mC.set(i,j, newVal);
                    }
                }
            // fill rest of C
            for (int i = 0; i < dim; ++i) {
                for (int j = i+1; j < dim; ++j) {

                	mC.set(i, j, mC.get(j,i));
                }
                
            }
        	if (mC.get(0,1) != mC.get(1,0)) {
        		System.err.println("WARNING");
        	}            
//            maxsqrtdiagC = Math.sqrt(math.max(math.diag(C)));
//            minsqrtdiagC = Math.sqrt(math.min(math.diag(C)));
        } // update of C
        
	}

	private double getMuCov(int mu) {
		// default parameter value ( HK03, sec. 2)
		return getMuEff(mu);
	}

	private double getCCov(int mu) {
		// ( HK03, sec. 2)
		//return Math.min(1., 2*getMuEff(selected)/(dim*dim));
		double ccov = (2./(getMuCov(mu)*Math.pow(dim+Math.sqrt(2.), 2)))+(1.-(1./getMuCov(mu)))*Math.min(1., (2*getMuEff(mu)-1.)/(dim*dim+2*dim+4+getMuEff(mu)));
		return ccov;
	}

	private double getMuEff(int mu) {
		double res = 0, u;
		for (int i=0; i<mu;i++) {
			u = getWeight(i);
			res += u*u;
		}
		return 1./res;
	}
	
	private void outputParams(int mu) {
		System.out.println("sigma=" + sigma + " chiN="+expRandStepLen + " cs="+getCs()+ " damps="+getDamps() + " Cc=" + getCc() + " Ccov=" + getCCov(mu) + " mueff=" + getMuEff(mu) + " mucov="+getMuCov(mu));
	}

	private void updateBD() {
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
		mC = (mC.plus(mC.transpose()).times(0.5)); // MAKE C SYMMETRIC

		EigenvalueDecomposition helper;
//		this.m_Counter      = 0;
		helper              = new EigenvalueDecomposition(mC);
		mB              = helper.getV(); // Return the eigenvector matrix
		eigenvalues  = helper.getRealEigenvalues();
		
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
	private double[] calcMeanX(Population selectedPop) {
		return selectedPop.getCenterWeighted(weights);
	}

	private double getWeight(int i) {
		return weights[i];
	}

	private void initWeights(int mu, int lambda) {
		weights = new double[mu];
		double sum = 0;
		int type = 0; // zero is default log scale
		for (int i=0; i<mu; i++) {
			if (type == 0) {
				weights[i] = (Math.log((lambda+1)/2.)-Math.log(i+1));
			} else weights[i] = 1.;
			sum+=weights[i];
		}
		for (int i=0; i<mu; i++) weights[i] /= sum; 
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
		// TODO recheck all this; some is handled in adaptGeneration on the first call
		firstAdaptionDone = false;
		range = ((InterfaceDataTypeDouble)individual).getDoubleRange();
		dim = range.length;
		if (TRACE_1) System.out.println("WCMA init " + dim);
		c_c = (4./(dim+4));
		c_sig = Double.NaN; // mark as not yet initialized
//		c_u_sig = Double.NaN;
		d_sig = Double.NaN; // init in first adaption step!  
		if (TRACE_1) System.out.println("WCMA static init " + dim);
		eigenvalues = new double[dim];
		Arrays.fill(eigenvalues, 1.);
		
		meanX = new double[dim];
		pathC = new double[dim];
		pathS = new double[dim];
//		mBD = new double[dim];
		mC = Matrix.identity(dim, dim);
		mB = Matrix.identity(dim, dim);
		sigma = 0.2;
		z = new double[dim];
		zCor = new double[dim];
		expRandStepLen = calcExpRandStepLen();

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
			
			((InterfaceDataTypeDouble)individual).SetDoubleGenotype(mutate(x, range, 0));
			
//			if (TRACE) System.out.println("WCMA mutate, aft: " + BeanInspector.toString(x));
		} else System.err.println("Error, expecting InterfaceDataTypeDouble");
	}

	private double[] mutate(double[] x, double[][] range, int count) {
		if (firstAdaptionDone) {
			double[] artmp = new double[x.length];
			for (int i = 0; i < dim; ++i) {
				artmp[i] = Math.sqrt(eigenvalues[i]) * RNG.gaussianDouble(1.);
			}
//			System.out.println("Sampling around " + BeanInspector.toString(meanX));
			/* add mutation (sigma * B * (D*z)) */
			for (int i = 0; i < dim; ++i) {
				double sum = 0.;
				for (int j = 0; j < dim; ++j)
					sum += mB.get(i,j) * artmp[j];
				x[i] = meanX[i]+getSigma(i)*sum;
			}
		} else {
			// no valid meanX yet, so just do a gaussian jump with sigma
			for (int i = 0; i < dim; ++i) {
				x[i] += RNG.gaussianDouble(getSigma(i));
			}
		}
		if (isInRange(x, range)) return x;
		else {
			if (count > 5) return repairMutation(x, range); // allow some nice tries before using brute force
			else return mutate(x, range, count+1); // for really bad initial deviations this might be a quasi infinite loop
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
	
	private boolean isInRange(double[] x, double[][] range) {
		for (int i=0; i<x.length; i++) {
			if (x[i]<range[i][0] || (x[i]>range[i][1])) return false;
		}
		return true;
	}

	/**
	 * After optimization start, this returns the initial sigma value
	 * actually employed.
	 * 
	 * @return the initial sigma value actually employed 
	 */
	public double getFirstSigma() {
		return firstSigma;
	}
	
	/**
	 * @return the initialSig
	 */
	public InitialSigmaEnum getInitialSigma() {
		return initialSig;
	}

	/**
	 * @param initialSig the initialSig to set
	 */
	public void setInitialSigma(InitialSigmaEnum initialSig) {
		this.initialSig = initialSig;
	}

	public String initialSigmaTipText() {
		return "Method to use for setting the initial step size.";
	}

	/**
	 * From Auger&Hansen, CEC '05, stopping criterion TolX.
	 * 
	 * @param tolX
	 * @return
	 */
	public boolean testAllDistBelow(double tolX) {
//		if all(sigma*(max(abs(pc), sqrt(diag(C)))) < stopTolX) 
		boolean res = true;
		int i=0;
		while (res && i<dim) {
			res = res && (getSigma(i)*Math.max(Math.abs(pathC[i]), Math.sqrt(mC.get(i,i))) < tolX);
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
	public boolean testNoChangeAddingDevAxis(double d, int gen) {
//		  if all(xmean == xmean + 0.1*sigma*BD(:,1+floor(mod(countiter,N))))
//	    i = 1+floor(mod(countiter,N));
//		stopflag(end+1) = {'warnnoeffectaxis'};
		
		int k = gen%dim;
		double[] ev_k = mB.getColumn(k);
		Mathematics.svMult(Math.sqrt(eigenvalues[k]), ev_k, ev_k); // this is now e_k*v_k = BD(:,...)
		
		int i=0;
		boolean res = true;
		while (res && (i<dim)) {
			res = res && (meanX[i] == (meanX[i] + d*getSigma(i)*ev_k[i]));
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
	public boolean testNoEffectCoord(double d) {
//		if any(xmean == xmean + 0.2*sigma*sqrt(diag(C))) 
//		stopflag(end+1) = {'warnnoeffectcoord'};
		boolean ret = false;
		int i=0;
		while ((i<dim) && !ret) {
			ret = ret || (meanX[i]==(meanX[i] + d*getSigma(i)*Math.sqrt(mC.get(i, i))));
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
	public boolean testCCondition(double d) {
//	    if (min(diag(D)) <= 0) || (max(diag(D)) > 1e14*min(diag(D))) 
//		  stopflag(end+1) = {'warnconditioncov'};
		Pair<Double,Double> minMax = mC.getMinMaxDiag();
		if ((minMax.head <= 0) || (minMax.tail >= d)) {		
			if (TRACE_TEST) System.out.println("testCCondition hit");
			return true;
		} else return false;
	}
}

enum InitialSigmaEnum  {
	halfRange, avgInitialDistance;
}
