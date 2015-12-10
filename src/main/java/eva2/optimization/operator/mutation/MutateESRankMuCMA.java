package eva2.optimization.operator.mutation;

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.enums.ESMutationInitialSigma;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.EAIndividualComparator;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.distancemetric.EuclideanMetric;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.EvolutionStrategies;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.Pair;
import Jama.EigenvalueDecomposition;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.io.Serializable;

/**
 * Implementing CMA ES with rank-mu-update and weighted recombination. More information can be found here:
 * - http://www.bionik.tu-berlin.de/user/niko/cmaesintro.html
 * - N.Hansen & S.Kern 2004: Evaluating the CMA Evolution Strategy on Multimodal Test Functions.
 * Parallel Problem Solving from Nature 2004.
 * - For the stopping criteria: Auger&Hansen, CEC '05, A Restart CMA ES with increasing population size.
 * <p>
 * The implementation uses a structure for keeping all adaptive parameters, CMAParamSet, which is stored
 * in the populations, so that in principle, multi-modal optimization with several populations becomes possible.
 * This of course requires proper handling of the generational cycle, i.e., new generations should be cloned from
 * the former ones (without individuals is ok) so that the parameters are taken over.
 */
@Description("The CMA mutator scheme with static cov. matrix, rank-mu update and weighted recombination.")
public class MutateESRankMuCMA implements InterfaceAdaptOperatorGenerational, InterfaceMutation, Serializable {
    //	int dim;
    private double c_c, expRandStepLen;

    // The static member lastParams is used to store the parameter set last seen in an adaption step, which
    // is then later used for the individual mutations. Necessary because each individual has its own mutator
    // instance. It would be cleaner to keep all parameters within the individual and have just one mutation
    // instance, however this would create quite a big baustelle.
    private static transient CMAParamSet lastParams = null;

    private ESMutationInitialSigma initializeSig = ESMutationInitialSigma.quarterRange;
    private double userDefInitSig = 0.2;

    private boolean doRankMuUpdate = true;
    private boolean checkRange = true;
    public static final String cmaParamsKey = "RankMuCMAParameters";


    public MutateESRankMuCMA() {
    }

    public MutateESRankMuCMA(MutateESRankMuCMA mutator) {
        this.c_c = mutator.c_c;
        this.expRandStepLen = mutator.expRandStepLen;
        this.initializeSig = mutator.initializeSig;
        this.userDefInitSig = mutator.userDefInitSig;
        this.checkRange = mutator.checkRange;
        this.doRankMuUpdate = mutator.doRankMuUpdate;
    }

    @Override
    public Object clone() {
        return new MutateESRankMuCMA(this);
    }

    public static double[] getMeanXOfPop(Population pop) {
        CMAParamSet params = (CMAParamSet) pop.getData(MutateESRankMuCMA.cmaParamsKey);
        if (params == null) {
            return null;
        } else {
            return params.meanX;
        }
    }

    /**
     * Retrieve the initial sigma for the given population and the user defined method.
     * For the halfRange case, -1 is returned, as the range is not available here but set
     * in initializing the CMAParams.
     *
     * @param initGen
     * @return
     */
    private double getInitSigma(Population initGen) {
        switch (initializeSig) {
            case avgInitialDistance:
                // scaled by average range as the measures are normed
                //return initGen.getPopulationMeasures(null)[0]*getAvgRange();
                // use euclidian measures without normation and scaling
                return initGen.getPopulationMeasures(new EuclideanMetric())[0]; // use euclidean metric which is not normed by range instead of phenotype metric
            //case halfRange: return getAvgRange(range)/2.;
            case userDefined:
                return userDefInitSig;
            case halfRange:
                return -0.5;
            case quarterRange:
                return -0.25;
        }
        throw new RuntimeException("Unknown initial sigma type!");
    }

    /**
     * Perform the main adaption of sigma and C using evolution paths.
     * The evolution path is deduced from the center of the selected population compared to the old
     * mean value.
     * See Hansen&Kern 04 for further information.
     *
     * @param oldGen
     * @param selectedP
     */
    @Override
    public void adaptAfterSelection(Population oldGen, Population selectedP) {
        Population selectedSorted = selectedP.getSortedBestFirst(new EAIndividualComparator(-1));

        int mu, lambda;
        mu = selectedP.size();
        lambda = oldGen.size();
        int generation = oldGen.getGeneration();
        if (mu >= lambda) {
            // try to override by oldGen additional data:
            if (oldGen.hasData(EvolutionStrategies.esMuParam)) {
                mu = (Integer) oldGen.getData(EvolutionStrategies.esMuParam);
            }
            if (oldGen.hasData(EvolutionStrategies.esLambdaParam)) {
                lambda = (Integer) oldGen.getData(EvolutionStrategies.esLambdaParam);
            }
        }
        if (mu >= lambda) {
            mu = Math.max(1, lambda / 2);
            EVAERROR.errorMsgOnce("Warning: invalid mu/lambda ratio! Setting mu to lambda/2 = " + mu + ", lambda = " + lambda);
        }
        CMAParamSet params;
        if (oldGen.getGeneration() <= 1) { // initialize new param set. At gen < 1 we shouldnt be called, but better do it once too often
            if (oldGen.hasData(cmaParamsKey)) {
                params = CMAParamSet.initCMAParams((CMAParamSet) oldGen.getData(cmaParamsKey), mu, lambda, oldGen, getInitSigma(oldGen));
            } else {
                params = CMAParamSet.initCMAParams(mu, lambda, oldGen, getInitSigma(oldGen));
            }
        } else {
            if (!oldGen.hasData(cmaParamsKey)) {
                if (oldGen.getGeneration() > 1) {
                    EVAERROR.errorMsgOnce("Error: population lost cma parameters. Incompatible optimizer?");
                }
                params = CMAParamSet.initCMAParams(mu, lambda, oldGen, getInitSigma(oldGen));
            } else {
                params = (CMAParamSet) oldGen.getData(cmaParamsKey);
            }
        }

        if (lambda == 1 && (oldGen.size() == 1) && (selectedP.size() == 1) && (oldGen.getEAIndividual(0).equals(selectedP.getEAIndividual(0)))) {
            // nothing really happened, so do not adapt and just store default params
            lastParams = (CMAParamSet) params.clone();
            oldGen.putData(cmaParamsKey, params);
            selectedP.putData(cmaParamsKey, params);
            return;
        }

        double[] newMeanX = calcMeanX(params.weights, selectedSorted);
        int dim = params.meanX.length;
        double[] BDz = new double[dim];
        for (int i = 0; i < dim; i++) { /* calculate xmean and BDz~N(0,C) */
            // Eq. 4 from HK04, most right term
            BDz[i] = Math.sqrt(CMAParamSet.getMuEff(params.weights, mu)) * (newMeanX[i] - params.meanX[i]) / getSigma(params, i);
        }

        double[] newPathS = params.pathS.clone();
        double[] newPathC = params.pathC.clone();

        double[] zVect = new double[dim];
        /* calculate z := D^(-1) * B^(-1) * BDz into artmp, we could have stored z instead */
        for (int i = 0; i < dim; ++i) {
            double sum = 0.;
            for (int j = 0; j < dim; ++j) {
                sum += params.mB.get(j, i) * BDz[j]; // times B transposed, (Eq 4) in HK04
            }
            if (params.eigenvalues[i] < 0) {
                EVAERROR.errorMsgOnce("Warning: negative eigenvalue in MutateESRankMuCMA! (possibly multiple cases)");
                zVect[i] = 0;
            } else {
                zVect[i] = sum / Math.sqrt(params.eigenvalues[i]);
                if (!checkValidDouble(zVect[i])) {
                    System.err.println("Error, infinite zVect entry!");
                    zVect[i] = 0; // TODO MK
                }
            }
        }

        /* cumulation for sigma (ps) using B*z */
        for (int i = 0; i < dim; ++i) {
            double sum = 0.;
            for (int j = 0; j < dim; ++j) {
                sum += params.mB.get(i, j) * zVect[j];
            }
            newPathS[i] = (1. - params.c_sig) * params.pathS[i]
                    + Math.sqrt(params.c_sig * (2. - params.c_sig)) * sum;
            if (!checkValidDouble(newPathS[i])) {
                System.err.println("Error, infinite pathS!");
            }
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
            checkValidDouble(newPathC[i]);
        }

        if (params.meanX == null) {
            params.meanX = newMeanX;
        }

        updateCov(params, newPathC, newMeanX, hsig, mu, selectedSorted);
        updateBD(params);

        /* update of sigma */
        double sigFact = Math.exp(((psNorm / expRandStepLen) - 1) * params.c_sig
                / params.d_sig);
        if (Double.isInfinite(sigFact)) {
            params.sigma *= 10.;
        } // in larger search spaces sigma tends to explode after initialize.
        else {
            params.sigma *= sigFact;
        }

        if (!testAndCorrectNumerics(params, generation, selectedSorted)) {
            // parameter seemingly exploded...
            params = CMAParamSet.initCMAParams(params, mu, lambda, params.meanX, ((InterfaceDataTypeDouble) oldGen.getEAIndividual(0)).getDoubleRange(), params.firstSigma);
        }

        // take over data
        params.meanX = newMeanX;
        params.pathC = newPathC;
        params.pathS = newPathS;
        params.firstAdaptionDone = true;

        lastParams = (CMAParamSet) params.clone();
        oldGen.putData(cmaParamsKey, params);
        selectedP.putData(cmaParamsKey, params);
    }

    /**
     * Expects newPop to have correct number of generations set.
     */
    @Override
    public void adaptGenerational(Population oldPop, Population selectedPop,
                                  Population newPop, boolean updateSelected) {
        // nothing to do? Oh yes, we can easily transfer the cma-params from the old to the new population.
        if (!newPop.hasData(cmaParamsKey)) {
            if (!oldPop.hasData(cmaParamsKey)) {
                System.err.println("warning: no cma param set found (MutateESRankMuCMA!");
            } else {
                newPop.putData(cmaParamsKey, oldPop.getData(cmaParamsKey));
            }
        }
        //newPop.addData(cmaParamsKey, oldPop.getData(cmaParamsKey));
        //newPop.addPopulationChangedEventListener(this); // listen to every population to be informed about reinit events
    }

    /**
     * Requires selected population to be sorted by fitness.
     *
     * @param params     refering parameter set
     * @param iterations number of iterations performed
     * @param selected   selected population
     * @return true if the parameters seem ok or were corrected, false if new parameters must be produced
     */
    private boolean testAndCorrectNumerics(CMAParamSet params, int iterations, Population selected) { // not much left here
        boolean corrected = true;
        /* Flat Fitness, Test if function values are identical */
        if (iterations > 1 && (selected.size() > 1)) {
            // selected pop is sorted
            if (nearlySame(selected.getEAIndividual(0).getFitness(), selected.getEAIndividual(selected.size() - 1).getFitness())) {
                params.sigma *= Math.exp(0.2 + params.c_sig / params.d_sig);
            }
        }

        if (!checkValidDouble(params.sigma)) {
            System.err.println("Error, unstable sigma!");
            corrected = false;
        }

    	/* Align (renormalize) scale C (and consequently sigma) */
        /* e.g. for infinite stationary state simulations (noise
    	 * handling needs to be introduced for that) */
        double fac = 1.;
        double minEig = 1e-12;
        double maxEig = 1e8;
        if (Mathematics.max(params.eigenvalues) < minEig) {
            fac = 1. / Math.sqrt(Mathematics.max(params.eigenvalues));
        } else if (Mathematics.min(params.eigenvalues) > maxEig) {
            fac = 1. / Math.sqrt(Mathematics.min(params.eigenvalues));
        }

        if (fac != 1.) {
            params.sigma /= fac;
            for (int i = 0; i < params.meanX.length; ++i) {
                params.pathC[i] *= fac;
                params.eigenvalues[i] *= fac * fac;
                for (int j = 0; j <= i; ++j) {
                    params.mC.set(i, j, params.mC.get(i, j) * fac * fac);
                    if (i != j) {
                        params.mC.set(j, i, params.mC.get(i, j));
                    }
                }
            }
        }
        return corrected;
    } // Test...

    private boolean nearlySame(double[] bestFitness, double[] worstFitness) {
        double epsilon = 1e-14;
        for (int i = 0; i < bestFitness.length; i++) {
            if (Math.abs(bestFitness[i] - worstFitness[i]) > epsilon) {
                return false;
            }
        }
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
        return Math.sqrt(dim) * (1. - (1. / (4 * dim)) + (1. / (21 * dim * dim)));
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
            for (int i = 0; i < dim; ++i) {
                for (int j = 0; j <= i; ++j) {
//                	oldVal = mC.get(i,j);
                    newVal = (1 - ccv) * params.mC.get(i, j)
                            + ccv
                            * (1. / mcv)
                            * (newPathC[i] * newPathC[j] + (1 - hsig) * getCc()
                            * (2. - getCc()) * params.mC.get(i, j));
                    checkValidDouble(newVal);
                    params.mC.set(i, j, newVal);
                    if (isRankMu()) {
                        for (int k = 0; k < mu; ++k) {
                                        /*
                                         * additional rank mu
                                         * update
                                         */
                            //                    	double[] x_k = ((InterfaceDataTypeDouble)selected.getEAIndividual(k)).getDoubleData();
                            double[] x_k = AbstractEAIndividual.getDoublePositionShallow(selected.getEAIndividual(k));
                            newVal = params.mC.get(i, j) + ccv * (1 - 1. / mcv)
                                    * params.weights[k] * (x_k[i] - params.meanX[i])
                                    * (x_k[j] - params.meanX[j]) / (getSigma(params, i) * getSigma(params, j)); // TODO right sigmas?
                            checkValidDouble(newVal);
                            params.mC.set(i, j, newVal);
                        }
                    }
                }
            }
            // fill rest of C
            for (int i = 0; i < dim; ++i) {
                for (int j = i + 1; j < dim; ++j) {

                    params.mC.set(i, j, params.mC.get(j, i));
                }

            }
            if (params.mC.get(0, 1) != params.mC.get(1, 0)) {
                System.err.println("WARNING, C is not symmetric!");
            }
//            maxsqrtdiagC = Math.sqrt(math.max(math.diag(C)));
//            minsqrtdiagC = Math.sqrt(math.min(math.diag(C)));
        } // update of C

    }

    public boolean isRankMu() {
        return doRankMuUpdate;
    }

    public void setRankMu(boolean rankUpd) {
        doRankMuUpdate = rankUpd;
    }

    public String rankMuTipText() {
        return "Choose between rank one and rank mu update.";
    }

    /**
     * Return true if v is a valid numeric value (not NaN and not Infinity), else false.
     *
     * @param v
     * @return
     */
    private boolean checkValidDouble(double v) {
        boolean valid = !(Double.isNaN(v) || Double.isInfinite(v));
        if (!valid) {
            System.err.println("Invalid double in rankMuCMA!");
        }
        return valid;
    }

    private double getCCov(double[] weights, int mu, int dim) {
        // ( HK03, sec. 2)
//		return Math.min(1., 2*CMAParamSet.getMuEff(weights, mu)/(dim*dim));
        double muC = CMAParamSet.getMuCov(weights, mu);
        double muE = CMAParamSet.getMuEff(weights, mu);
        double ccov = (2. / (muC * Math.pow(dim + Math.sqrt(2.), 2)))
                + (1. - (1. / muC)) * Math.min(1., (2 * muE - 1.) / (dim * dim + 2 * dim + 4 + muE));
        return ccov;
    }

    private void outputParams(CMAParamSet params, int mu) {
        System.out.println("sigma=" + params.sigma + " chiN=" + expRandStepLen + " cs=" + params.c_sig
                + " damps=" + params.d_sig + " Cc=" + getCc() + " Ccov=" + getCCov(params.weights, mu, params.meanX.length)
                + " mueff=" + CMAParamSet.getMuEff(params.weights, mu) + " mucov=" + CMAParamSet.getMuCov(params.weights, mu));
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
//		this.counter      = 0;
        helper = new EigenvalueDecomposition(params.mC);
        params.mB = helper.getV(); // Return the eigenvector matrix
        params.eigenvalues = helper.getRealEigenvalues();

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
     * calculate weighted mean of the selected population
     *
     * @param selectedPop
     * @return
     */
    private double[] calcMeanX(double[] weights, Population selectedPop) {
        return selectedPop.getCenterWeighted(weights);
    }

    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1,
                                              Population partners) {
        // nothing to do

    }

    public String getName() {
        return "Rank-Mu-CMA-Mutator";
    }

    @Override
    public String getStringRepresentation() {
        return "Rank-Mu-CMA-Mutator";
    }

    @Override
    public void initialize(AbstractEAIndividual individual,
                           InterfaceOptimizationProblem opt) {
        double[][] range = ((InterfaceDataTypeDouble) individual).getDoubleRange();
        int dim = range.length;
        c_c = (4. / (dim + 4));
        expRandStepLen = calcExpRandStepLen(dim);

    }

    @Override
    public void mutate(AbstractEAIndividual individual) {
        if (individual instanceof InterfaceDataTypeDouble) {
            double[] x = ((InterfaceDataTypeDouble) individual).getDoubleData();
            double[][] range = ((InterfaceDataTypeDouble) individual).getDoubleRange();

            // this is a critical point: where do the CMA parameters for this individual's mutation come from?
            // for GA and ES we can expect that selection occurred directly before the mutation cycle,
            // so we take the parameter set from the last adaption step.
            ((InterfaceDataTypeDouble) individual).setDoubleGenotype(mutate(lastParams, x, range, 0));

        } else {
            System.err.println("Error, expecting InterfaceDataTypeDouble");
        }
    }

    private double[] mutate(CMAParamSet params, double[] x, double[][] range, int count) {
        int dim = range.length;
        if (!Mathematics.isInRange(params.meanX, range)) { // TODO Im not sure why this can happen...
            System.err.println("Error in MutateESRankMuCMA.mutate !");
            Mathematics.projectToRange(params.meanX, range);
        }
        if (params != null && (params.firstAdaptionDone)) {
            double[] sampl = new double[dim]; // generate scaled random vector (D * z)
            for (int i = 0; i < dim; ++i) {
                sampl[i] = Math.sqrt(params.eigenvalues[i]) * RNG.gaussianDouble(1.);
                if (Double.isNaN(sampl[i])) {
                    sampl[i] = 0;
                }
            }
            //			System.out.println("Sampling around " + BeanInspector.toString(meanX));
			/* add mutation (sigma * B * (D*z)) */
            addMutationStep(params, x, sampl);
            int cnt = 0;
            while (!Mathematics.isInRange(x, range)) {
//			     % You may handle constraints here. You may either resample
//			    % arz(:,k) and/or multiply it with a factor between -1 and 1
//			    % (the latter will decrease the overall step size) and
//			    % recalculate arx accordingly. Do not change arx or arz in any
//			    % other way.
                // multiply sampl by a no in [-1,1] and try again
                double r; // actually we use [-1,-0.5] or [0.5, 1]
                cnt++;
                if (cnt > 10 * x.length) {
                    // well... lets give it up. Probably the meanX is close to the bounds in several dimensions
                    // which is unlikely to be solved by random sampling.
//					if (!Mathematics.isInRange(params.meanX, range)) {
//						System.err.println("Error in MutateESRankMuCMA.mutate !"); break;
//					}
                    r = 0;
                } else {
                    r = RNG.randomDouble(-0.5, 0.5);
                    if (Math.abs(r) < 0.5) {
                        r += Math.signum(r) * 0.5;
                    } // r is in [-1,-0.5] or [0.5,1]
                }
//				System.out.println("Reducing step by " + r + " for " + BeanInspector.toString(params.meanX));
                Mathematics.svMult(r, sampl, sampl);
                addMutationStep(params, x, sampl);
            }
        } else {
            if (params == null) {
                System.err.println("Error in MutateESRankMuCMA: parameter set was null! Skipping mutation...");
            }            // no valid meanX yet, so just do a gaussian jump with sigma
            for (int i = 0; i < dim; ++i) {
                x[i] += RNG.gaussianDouble(getSigma(params, i));
                checkValidDouble(x[i]);
            }
        }
        if (!Mathematics.isInRange(params.meanX, range)) {
            System.err.println("Error B in MutateESRankMuCMA.mutate !");
        }
        return x;
    }

    private void addMutationStep(CMAParamSet params, double[] x, double[] sampl) {
		/* add mutation (sigma * B * (D*z)) */
        for (int i = 0; i < x.length; ++i) {
            double sum = 0.;
            for (int j = 0; j < x.length; ++j) {
                sum += params.mB.get(i, j) * sampl[j];
            }
            x[i] = params.meanX[i] + getSigma(params, i) * sum;
            checkValidDouble(x[i]);
        }
    }

    private double[] mutateOrig(CMAParamSet params, double[] x, double[][] range, int count) {
        int dim = range.length;
        int maxRetries;

        if (checkRange) {
            maxRetries = 100 * dim;
        } else {
            maxRetries = 0;
        } // take the first sample, not matter in or out of range
        do
        { // this is a loop in case that the range needs to be checked and the current sampling fails to keep the range
            if (params != null && (params.firstAdaptionDone)) {
                double[] sampl = new double[dim]; // generate scaled random vector (D * z)
                for (int i = 0; i < dim; ++i) {
                    sampl[i] = Math.sqrt(params.eigenvalues[i]) * RNG.gaussianDouble(1.);
                    if (Double.isNaN(sampl[i])) {
                        sampl[i] = 0;
                    }
                }
                //			System.out.println("Sampling around " + BeanInspector.toString(meanX));
				/* add mutation (sigma * B * (D*z)) */
                for (int i = 0; i < dim; ++i) {
                    double sum = 0.;
                    for (int j = 0; j < dim; ++j) {
                        sum += params.mB.get(i, j) * sampl[j];
                    }
                    x[i] = params.meanX[i] + getSigma(params, i) * sum;
                    checkValidDouble(x[i]);
                }
            } else {
                if (params == null) {
                    System.err.println("Error in MutateESRankMuCMA: parameter set was null! Skipping mutation...");
                }            // no valid meanX yet, so just do a gaussian jump with sigma
                for (int i = 0; i < dim; ++i) {
                    x[i] += RNG.gaussianDouble(getSigma(params, i));
                    checkValidDouble(x[i]);
                }
            }
        } while ((maxRetries--) > 0 && !(Mathematics.isInRange(x, range)));
        if (checkRange && !(Mathematics.isInRange(x, range))) {
            return repairMutation(x, range);
        } // allow some nice tries before using brute force
        else {
            return x;
        }
    }

    private double[] repairMutation(double[] x, double[][] range) {
        EVAERROR.errorMsgOnce("Warning, brute-forcing constraints! Too large initial sigma? (pot. multiple errors)");
        Mathematics.projectToRange(x, range);
        return x;
    }

    /**
     * After optimization start, this returns the initial sigma value
     * actually employed.
     *
     * @return the initial sigma value actually employed
     */
    public double getFirstSigma(Population pop) {
        return ((CMAParamSet) pop.getData(cmaParamsKey)).firstSigma;
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
        GenericObjectEditor.setHideProperty(this.getClass(), "userDefInitSig", initialSig != ESMutationInitialSigma.userDefined);
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
        boolean res = true;
        CMAParamSet params = (CMAParamSet) pop.getData(cmaParamsKey);
        int i = 0;
        while (res && i < params.meanX.length) {
            res = res && (getSigma(params, i) * Math.max(Math.abs(params.pathC[i]), Math.sqrt(params.mC.get(i, i))) < tolX);
            i++;
        }

        return res;
    }

    /**
     * From Auger&Hansen, CEC '05, stopping criterion noeffectaxis.
     *
     * @param d
     * @param gen
     * @return
     */
    public boolean testNoChangeAddingDevAxis(Population pop, double d, int gen) {
        CMAParamSet params = (CMAParamSet) pop.getData(cmaParamsKey);
        int dim = params.meanX.length;
        int k = gen % dim;
        double[] ev_k = Mathematics.getColumn(params.mB, k);
        Mathematics.svMult(Math.sqrt(params.eigenvalues[k]), ev_k, ev_k); // this is now e_k*v_k = BD(:,...)

        int i = 0;
        boolean res = true;
        while (res && (i < dim)) {
            res = res && (params.meanX[i] == (params.meanX[i] + d * getSigma(params, i) * ev_k[i]));
            i++;
        }
        return res;
    }

    /**
     * From Auger&Hansen, CEC '05, stopping criterion noeffectcoord.
     *
     * @param d
     * @return
     */
    public boolean testNoEffectCoord(Population pop, double d) {
        boolean ret = false;
        CMAParamSet params = (CMAParamSet) pop.getData(cmaParamsKey);
        int i = 0;
        while ((i < params.meanX.length) && !ret) {
            ret = ret || (params.meanX[i] == (params.meanX[i] + d * getSigma(params, i) * Math.sqrt(params.mC.get(i, i))));
            i++;
        }
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
        CMAParamSet params = (CMAParamSet) pop.getData(cmaParamsKey);
        Pair<Double, Double> minMax = Mathematics.getMinMaxDiag(params.mC);
        return (minMax.head <= 0) || (minMax.tail >= d);
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

    public String userDefInitSigTipText() {
        return "Set a manual initial sigma which should be related to the initial individual distribution.";
    }

    public boolean isCheckRange() {
        return checkRange;
    }

    public void setCheckRange(boolean checkRange) {
        this.checkRange = checkRange;
    }

    public String checkRangeTipText() {
        return "Force the operator to remain within the problem range.";
    }
}