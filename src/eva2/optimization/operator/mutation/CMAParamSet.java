package eva2.optimization.operator.mutation;

import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.population.Population;
import eva2.optimization.strategies.EvolutionStrategies;
import eva2.tools.EVAERROR;
import eva2.tools.math.Jama.Matrix;
import eva2.tools.math.Mathematics;

import java.io.Serializable;
import java.util.Arrays;


/**
 * The collection of all population specific data required for a rank-mu-CMA optimization.
 * Besides methods for initialization, this class implements the InterfacePopulationChangedEventListener
 * and is added as listener to the population it is initialized on. The reason for this is that the
 * CMA parameters must be reinitialized whenever the population itself is initialized, so the reinit
 * event is caught and handled. As there are numerous mutator instances but only one CMA parameter
 * set per population, this way is the good one.
 *
 * @author mkron
 */
class CMAParamSet implements InterfacePopulationChangedEventListener, Serializable {
    private static final long serialVersionUID = -1940875082544233819L;
    protected double firstSigma = -1.;
    protected double sigma;
    protected double d_sig, c_sig;
    protected double[] meanX, pathC, pathS, eigenvalues;
    protected double[] weights = null;
    protected double[][] range = null;
    protected Matrix mC;
    protected Matrix mB;
    protected boolean firstAdaptionDone = false;

    public CMAParamSet(CMAParamSet o) {
        firstSigma = o.firstSigma;
        sigma = o.sigma;
        d_sig = o.d_sig;
        c_sig = o.c_sig;
        meanX = o.meanX.clone();
        pathC = o.pathC.clone();
        pathS = o.pathS.clone();
        eigenvalues = o.eigenvalues.clone();
        weights = o.weights.clone();
        range = o.range;
        mC = o.mC;
        mB = o.mB;
        firstAdaptionDone = o.firstAdaptionDone;
    }

    public CMAParamSet() {
    }

    @Override
    public Object clone() {
        return new CMAParamSet(this);
    }

    @Override
    public String toString() {
        return "d_sig " + d_sig + ", c_sig " + c_sig + ", sigma " + sigma + ", firstSigma " + firstSigma + ", firstAdaptionDone " + firstAdaptionDone
                + ",\n meanX " + Arrays.toString(meanX) + ", pathC " + Arrays.toString(pathC) + ", pathS " + Arrays.toString(pathS) + ", eigenvalues " + Arrays.toString(eigenvalues)
                + ", weights " + Arrays.toString(weights) + ",\n mC " + mC.toString() + ",\n mB " + mB.toString();
    }

    /**
     * Initializes a new CMAParamSet from scratch.
     *
     * @param mu
     * @param lambda
     * @param oldGen
     * @param initialSigma
     * @return
     * @see #initCMAParams(CMAParamSet, int, int, Population, double)
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
     * @param params       the CMA parameter set to be used - its data are overwritten
     * @param mu           ES mu parameter
     * @param lambda       ES lambda parameter
     * @param pop          associated Population
     * @param initialSigma initial sigma or -1 to indicate the usage of average range
     * @return
     */
    public static CMAParamSet initCMAParams(CMAParamSet params, int mu, int lambda, Population pop, double initialSigma) {
        initCMAParams(params, mu, lambda, pop.getBestEAIndividual().getDoublePosition(), ((InterfaceDataTypeDouble) pop.getEAIndividual(0)).getDoubleRange(), initialSigma);
        pop.addPopulationChangedEventListener(params);
        return params;
    }

    /**
     * Initializes the CMA parameter set for given mu, lambda and a population.
     * The initialSigma parameter is used as initial sigma directly unless it is <0, in
     * that case the average range is used as initial sigma.
     *
     * @param params       the CMA parameter set to be used - its data are overwritten
     * @param mu           ES mu parameter
     * @param lambda       ES lambda parameter
     * @param pop          associated Population
     * @param initialSigma initial sigma or -1 to indicate the usage of average range
     * @return
     */
    public static CMAParamSet initCMAParams(CMAParamSet params, int mu, int lambda, double[] center, double[][] range, double initialSigma) {
        // those are from init:
        params.firstAdaptionDone = false;
        params.range = range;

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
        params.c_sig = (muEff + 2) / (muEff + dim + 3);
//		c_u_sig = Math.sqrt(c_sig * (2.-c_sig));
        params.d_sig = params.c_sig + 1 + 2 * Math.max(0, Math.sqrt((muEff - 1) / (dim + 1)) - 1);

        if (initialSigma < 0) { // this means we scale the average range
            if (initialSigma != -0.25 && (initialSigma != -0.5)) {
                EVAERROR.errorMsgOnce("Warning, unexpected initial sigma in CMAParamSet!");
            }
            initialSigma = -initialSigma * Mathematics.getAvgRange(params.range);
        }
        if (initialSigma <= 0) {
            EVAERROR.errorMsgOnce("warning: initial sigma <= zero! Working with converged population?");
            initialSigma = 10e-10;
        }
        params.sigma = initialSigma;
//		System.out.println("INitial sigma: "+sigma);
        params.firstSigma = params.sigma;
//		System.out.println("new center is " + BeanInspector.toString(center));
        params.meanX = center; // this might be ok?
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
        if (lambda == 1) {
            System.err.println("Invalid lambda value in CMAParamSet.initWeights!");
            lambda++; //kind of a cheat to save against div. by zero
        }
        double[] theWeights = new double[mu];
        double sum = 0;
        int type = 0; // zero is default log scale
        for (int i = 0; i < mu; i++) {
            if (type == 0) {
                theWeights[i] = (Math.log((lambda + 1) / 2.) - Math.log(i + 1));
            } else {
                theWeights[i] = 1.;
            }
            sum += theWeights[i];
        }
        for (int i = 0; i < mu; i++) {
            theWeights[i] /= sum;
        }
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
        for (int i = 0; i < mu; i++) {
            u = weights[i];
            res += u * u;
        }
        return 1. / res;
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
     * Make sure that the parameter sets of each population are updated (reinitialized)
     * if a population is reinitialized.
     *
     * @see InterfacePopulationChangedEventListener
     */
    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        if (name.equals(Population.populationInitialized)) {
            Population pop = (Population) source;
            if (MutateESRankMuCMA.TRACE_1) {
                System.out.println("Event " + name + " arrived in CMAParamSet!!!");
            }
            CMAParamSet params = (CMAParamSet) (pop.getData(MutateESRankMuCMA.cmaParamsKey));
            int mu;
            if (pop.hasData(EvolutionStrategies.esMuParam)) {
                mu = (Integer) pop.getData(EvolutionStrategies.esMuParam);
            } else {
                System.err.println("Unknown mu in reinit! using lambda/2...");
                mu = pop.size() / 2;
            }
            pop.putData(MutateESRankMuCMA.cmaParamsKey, CMAParamSet.initCMAParams(params, mu, pop.size(), pop, params.firstSigma));
        }
    }
}
