package eva2.optimization.strategies;

import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.gui.BeanInspector;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.operator.distancemetric.PhenotypeMetric;
import eva2.optimization.operator.postprocess.PostProcess;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.InterfaceTerminator;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.F1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.Pair;
import eva2.tools.SelectedTag;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.ArrayList;

/**
 * A ScatterSearch implementation taken mainly from [1]. Unfortunately, some
 * parameters as well as the local search method are not well defined in [1], so
 * this implementation allows HC and Nelder-Mead as local search. If local
 * search is activated, an additional filter is defined, meaning that only those
 * individuals with a high quality fitness are further improved by local search.
 * The threshold fitness is either defined relatively to the best/worst fitness
 * values in the reference set or as an absolute value (in both cases only the
 * first fitness criterion is regarded).
 *
 *         <p/>
 *         [1] M.Rodiguez-Fernandez, J.Egea, J.Banga: Novel metaheuristic for parameter
 *         estimation in nonlinear dynamic biological systems. BMC Bioinformatics 2006,
 *         7:483. BioMed Central 2006.
 */
@Description("A scatter search variant after Rodiguez-Fernandez, J.Egea and J.Banga: Novel metaheuristic for parameter estimation in nonlinear dynamic biological systems, BMC Bioinf. 2006")
public class ScatterSearch implements InterfaceOptimizer, java.io.Serializable, InterfacePopulationChangedEventListener {

    transient private InterfacePopulationChangedEventListener listener = null;
    private String identifier = "ScatterSearch";
    private AbstractOptimizationProblem optimizationProblem = new F1Problem();
    private Population oldRefSet, refSet = new Population(10);
    private transient Population combinations = null;
    private AbstractEAIndividual template = null;
    private double[][] range = null;
    private int refSetSize = 10; // default: 10
    private int poolSize = 100;   // default: 10*refSetSize;
    // splitting each dimension into intervals to do diverse initialization
    private int intervals = 4;
    private int localSearchSteps = 100;
    private double localSearchFitnessFilter = 1.5;
    private int probDim = -1;
    private boolean firstTime = true;
    private int lastImprovementCount = 0;
    private SelectedTag localSearchMethod = new SelectedTag(1, "Hill-Climber", "Nelder-Mead");
    // simulate an EvA generational cycle
    private int generationCycle = 50;
    private int fitCrit = -1;
    protected boolean checkRange = true;
    protected boolean doLocalSearch = false;
    private boolean relativeFitCriterion = false;
    private double nelderMeadInitPerturbation = 0.01;
    private double improvementEpsilon = 0.1; // minimal relative fitness improvement for a candidate to be taken over into the refset
    private double minDiversityEpsilon = 0.0001; // minimal phenotypic distance for a candidate to be taken over into the refset

    public ScatterSearch() {
        GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
        hideHideable();
    }

    public ScatterSearch(ScatterSearch o) {
        this.refSet = (Population) o.refSet.clone();
        this.optimizationProblem = (AbstractOptimizationProblem) o.optimizationProblem.clone();
        this.template = (AbstractEAIndividual) o.template.clone();
        this.range = ((InterfaceDataTypeDouble) template).getDoubleRange();
        this.refSetSize = o.refSetSize;
        this.poolSize = o.poolSize;
        this.intervals = o.intervals;
        this.localSearchSteps = o.localSearchSteps;
        this.localSearchFitnessFilter = o.localSearchFitnessFilter;
        this.probDim = o.probDim;
        this.firstTime = o.firstTime;
        this.lastImprovementCount = o.lastImprovementCount;
    }

    @Override
    public Object clone() {
        return new ScatterSearch(this);
    }

    public void hideHideable() {
        setLSShowProps();
        GenericObjectEditor.setHideProperty(this.getClass(), "population", true);
    }

    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.optimizationProblem = (AbstractOptimizationProblem) problem;
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(refSet);
    }

    @Override
    public Population getPopulation() {
        return refSet;
    }

    @Override
    public void initialize() {
        defaultInit();
        initRefSet(diversify());
    }

    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        defaultInit();

        initRefSet(diversify(pop));
    }

    /**
     * Eval an initial population and extract the first refset.
     *
     * @param pop
     */
    private void initRefSet(Population pop) {
        optimizationProblem.evaluate(pop);
        refSet = getRefSetFitBased(new Population(refSetSize), pop);
        refSet.incrFunctionCallsBy(pop.size());

        refSet.addPopulationChangedEventListener(this);
        refSet.setNotifyEvalInterval(generationCycle);
    }

    /**
     * Do default initialization.
     */
    private void defaultInit() {
        firstTime = true;
        refSet = null;
        combinations = null;
        template = optimizationProblem.getIndividualTemplate();
        if (!(template instanceof InterfaceDataTypeDouble)) {
            System.err.println("Requiring double data!");
        } else {
            Object dim = BeanInspector.callIfAvailable(optimizationProblem, "getProblemDimension", null);
            if (dim == null) {
                System.err.println("Couldnt get problem dimension!");
            }
            probDim = (Integer) dim;
            range = ((InterfaceDataTypeDouble) template).getDoubleRange();
        }
    }

    /**
     * Something has changed
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.listener != null) {
            this.listener.registerPopulationStateChanged(this, name);
        }
    }

    //	public double evaluate(double[] x) {
//		AbstractEAIndividual indy = (AbstractEAIndividual)template.clone();
//		((InterfaceDataTypeDouble)indy).setDoubleGenotype(x);
//		optimizationProblem.evaluate(indy);
//		return indy.getFitness(0);
//	}
    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        // The events of the interim hill climbing population will be caught here 
        if (name.compareTo(Population.FUN_CALL_INTERVAL_REACHED) == 0) {
//			if ((((Population)source).size() % 50) > 0) {
//			System.out.println("bla");
//			}
            // set funcalls to real value
            refSet.setFunctionCalls(((Population) source).getFunctionCalls());

//			System.out.println("FunCallIntervalReached at " + (((Population)source).getFunctionCalls()));

            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
        // do not react to NextGenerationPerformed
        //else System.err.println("ERROR, event was " + name);
    }

    @Override
    public void optimize() {
        // Diversification
        // Refset Formation
        // L: Combination of refset elements
        // if (localSolverCall) then 
        // if (pass filters) then compute local solvers
        // refset update
        // if (uncombined elements) then goto L
        // if (!stop criterion) then
        // refset regeneration
        // goto L
        if (!firstTime) {
            if (lastImprovementCount == 0) {
                refSet = regenerateRefSet();
            }
        }
        firstTime = false;

        optimizationProblem.evaluatePopulationStart(refSet);
        int funCallsStart = refSet.getFunctionCalls();
        do {
            if (combinations == null || combinations.size() == 0) {
                combinations = generateCombinations(refSet);
                oldRefSet = (Population) refSet.clone();
                lastImprovementCount = 0;
            }
            if (combinations.size() > 0) {
                updateRefSet(refSet, combinations, oldRefSet);
            }
        } while (refSet.getFunctionCalls() - funCallsStart < generationCycle);
        optimizationProblem.evaluatePopulationEnd(refSet);
    }

    private boolean isDoLocalSolver(AbstractEAIndividual cand, Population refSet) {
//		if (lastLocalSearch + localSearchInterval < refSet.getGeneration()) {
        if (doLocalSearch) {
            // filter: only check those within 50% of the worst indy relative to the best.
            if (relativeFitCriterion) {
                double fitRange = refSet.getWorstFitness()[0] - refSet.getBestFitness()[0];
                return (cand.getFitness(0) < (refSet.getBestFitness()[0] + (fitRange * localSearchFitnessFilter)));
            } else {
                // absolute fitness criterion
                return (cand.getFitness(0) < localSearchFitnessFilter);
            }
        } else {
            return false;
        }
    }

    private Population regenerateRefSet() {
        Population diversifiedPop = diversify();
        int keep = refSetSize / 2;
        Population newRefSet = refSet.cloneWithoutInds();

        newRefSet.addAll(refSet.getBestNIndividuals(keep, fitCrit));


        int h = newRefSet.size();
        ArrayList<double[]> distVects = new ArrayList<>();
        for (int i = 1; i < h; i++) {
            distVects.add(getDiffVect(newRefSet.getEAIndividual(0), newRefSet.getEAIndividual(i)));
        }

        double maxSP = -1;
        int sel = -1;
        while (h < refSetSize) {
            for (int i = 0; i < diversifiedPop.size(); i++) {
                // 	the difference of cand and best is multiplied by each earlier difference from refSet indies
                double[] vP = calcVectP(diversifiedPop.getEAIndividual(i), newRefSet.getEAIndividual(0), distVects);
                double maxTmp = getMaxInVect(vP);
                if ((i == 0) || (maxTmp < maxSP)) {
                    maxSP = maxTmp;
                    sel = i;
                }
                // 	selected the one with smallest maxSP!
            }
            AbstractEAIndividual winner = diversifiedPop.getEAIndividual(sel);
            // evaluate the new indy
            optimizationProblem.evaluate(winner);
            // 	add it to the newRefSet, increase h
            newRefSet.add(winner);
            newRefSet.incrFunctionCalls();
            // 	newRefSet not sorted anymore?
            h++;
            // 	update distVects
            distVects.add(getDiffVect(newRefSet.getEAIndividual(0), winner));
            // remove from pop.
            diversifiedPop.remove(sel);
            // 	redo the loop
        }
        return newRefSet;
    }

    private double getMaxInVect(double[] vals) {
        double dmax = vals[0];
        for (int j = 1; j < vals.length; j++) {
            if (vals[j] > dmax) {
                dmax = vals[j];
            }
        }
        return dmax;
    }

    private double[] calcVectP(AbstractEAIndividual candidate, AbstractEAIndividual best, ArrayList<double[]> distVects) {
        // p = (best - candidate)*transposed(M)
        double[] diff = getDiffVect(best, candidate);
        return multScalarTransposed(diff, distVects);
    }

    private double[] multScalarTransposed(double[] diff, ArrayList<double[]> distVects) {
        // d[0]*m[0][0], d[1]*m[0][1] etc.
        double[] res = new double[distVects.size()];
        for (int i = 0; i < distVects.size(); i++) {
            res[i] = Mathematics.vvMult(diff, distVects.get(i));
        }
        return res;
    }

    private double[] getDiffVect(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        double[] v1 = ((InterfaceDataTypeDouble) indy1).getDoubleData();
        double[] v2 = ((InterfaceDataTypeDouble) indy2).getDoubleData();
        return Mathematics.vvSub(v1, v2);
    }

    /**
     * Maybe replace the single worst indy in the refset by the best candidate,
     * which may be locally optimized in a local search step. The best candidate
     * is removed from the candidate set in any case. The candidate set may be
     * cleared if all following individuals would never be taken over to the
     * refset.
     *
     * @param refSet
     * @param candidates
     * @param oldRefSet  only to be used as for phenotypic diversity measure
     */
    private void updateRefSet(Population refSet, Population candidates, Population oldRefSet) {
        int bestIndex = candidates.getIndexOfBestIndividualPrefFeasible();
        AbstractEAIndividual bestCand = candidates.getEAIndividual(bestIndex);
        AbstractEAIndividual worstRef = refSet.getWorstEAIndividual();

        if (isDoLocalSolver(bestCand, refSet)) {
            Pair<AbstractEAIndividual, Integer> lsRet = localSolver(bestCand, localSearchSteps);
            if ((Math.abs(lsRet.tail() - localSearchSteps) / localSearchSteps) > 0.05) {
                System.err.println("Warning, more than 5% difference in local search step");
            }
            bestCand = lsRet.head();
            refSet.incrFunctionCallsBy(lsRet.tail());
        }

        if (bestCand.isDominatingEqual(worstRef)) {
            if (diversityCriterionFulfilled(bestCand, refSet, oldRefSet)) {
                int replIndex = refSet.indexOf(worstRef);
                refSet.set(replIndex, bestCand);
                lastImprovementCount++;
            } else if (bestCand.isDominating(refSet.getBestEAIndividual())) {
                // exception: always accept best solution found so far
                int closestIndex = getClosestIndy(bestCand, refSet);
                refSet.set(closestIndex, bestCand);
                lastImprovementCount++;
            }
            candidates.remove(bestIndex);
        } else {
            // if the best candidate is worse and no local search is performed, all following will be worse - at least in the uni-criterial case
            // so we can just clear the rest of the candidates
            if (!doLocalSearch && (bestCand.getFitness().length == 1)) {
                candidates.clear();
            } else {
                candidates.remove(bestIndex);
            }
        }
    }

    private Pair<AbstractEAIndividual, Integer> localSolver(AbstractEAIndividual cand, int hcSteps) {
        if (localSearchMethod.getSelectedTagID() == 0) {
            return localSolverHC(cand, hcSteps);
        } else {
            return PostProcess.localSolverNMS(cand, hcSteps, nelderMeadInitPerturbation, optimizationProblem);
        }
    }

    private Pair<AbstractEAIndividual, Integer> localSolverHC(AbstractEAIndividual cand, int hcSteps) {
        // use HC for a start...
        Population hcPop = new Population(1);
        hcPop.add(cand);
        int stepsDone = PostProcess.processWithHC(hcPop, optimizationProblem, hcSteps);
        return new Pair<>(hcPop.getEAIndividual(0), stepsDone);
    }

    private int getClosestIndy(AbstractEAIndividual indy, Population refSet) {
        double tmpDst, dist = PhenotypeMetric.dist(indy, refSet.getEAIndividual(0));
        int sel = 0;
        for (int i = 1; i < refSet.size(); i++) {
            tmpDst = PhenotypeMetric.dist(indy, refSet.getEAIndividual(i));
            if (tmpDst < dist) {
                tmpDst = dist;
                sel = i;
            }
        }
        return sel;
    }

    /**
     * Check for both a genotype and phenotype diversity criterion which both
     * must be fulfilled for a candidate to be accepted.
     *
     * @param cand
     * @param popCompGeno
     * @param popComPheno
     * @return
     */
    private boolean diversityCriterionFulfilled(AbstractEAIndividual cand, Population popCompGeno, Population popComPheno) {
        double minDist = PhenotypeMetric.dist(cand, popCompGeno.getEAIndividual(0));
        for (int i = 1; i < popCompGeno.size(); i++) {
            minDist = Math.min(minDist, PhenotypeMetric.dist(cand, popCompGeno.getEAIndividual(i)));
        }

        boolean minDistFulfilled = ((minDiversityEpsilon <= 0) || (minDist > minDiversityEpsilon));

        if (minDistFulfilled && (improvementEpsilon > 0)) {
            boolean minImprovementFulfilled = (cand.getFitness(0) < ((1. - improvementEpsilon) * popComPheno.getBestEAIndividual().getFitness(0)));
            return minImprovementFulfilled;
        } else {
            return minDistFulfilled;
        }
    }

    /**
     * Recombines the refset to new indies which are also evaluated.
     *
     * @param refSet
     * @return
     */
    private Population generateCombinations(Population refSet) {
        // 3 pair types: better-better, better-worse, worse-worse (half of the pop);
        Population combs = new Population();
        Population refSorted = refSet.getBestNIndividuals(refSet.size(), fitCrit);
        int half = refSet.size() / 2;
        for (int i = 0; i < half - 1; i++) { // better-better
            AbstractEAIndividual indy1 = refSorted.getEAIndividual(i);
            for (int j = i + 1; j < half; j++) {
                AbstractEAIndividual indy2 = refSorted.getEAIndividual(j);
                combs.add(combineTypeOne(indy1, indy2));
                combs.add(combineTypeTwo(indy1, indy2));
                combs.add(combineTypeTwo(indy1, indy2));
                combs.add(combineTypeThree(indy1, indy2));
            }
        }

        for (int i = 0; i < half; i++) { // better-worse
            AbstractEAIndividual indy1 = refSorted.getEAIndividual(i);
            for (int j = half; j < refSet.size(); j++) {
                AbstractEAIndividual indy2 = refSorted.getEAIndividual(j);
                combs.add(combineTypeOne(indy1, indy2));
                combs.add(combineTypeTwo(indy1, indy2));
                combs.add(combineTypeThree(indy1, indy2));
            }
        }

        for (int i = half; i < refSet.size() - 1; i++) { // worse-worse
            AbstractEAIndividual indy1 = refSorted.getEAIndividual(i);
            for (int j = i + 1; j < refSet.size(); j++) {
                AbstractEAIndividual indy2 = refSorted.getEAIndividual(j);
                combs.add(combineTypeTwo(indy1, indy2));
                if (RNG.flipCoin(0.5)) {
                    combs.add(combineTypeOne(indy1, indy2));
                } else {
                    combs.add(combineTypeThree(indy1, indy2));
                }
            }
        }
        return combs;
    }

    private AbstractEAIndividual combineTypeOne(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        return combine(indy1, indy2, true, false);
    }

    private AbstractEAIndividual combineTypeTwo(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        return combine(indy1, indy2, true, true);
    }

    private AbstractEAIndividual combineTypeThree(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        return combine(indy1, indy2, false, true);
    }

    private AbstractEAIndividual combine(AbstractEAIndividual indy1, AbstractEAIndividual indy2, boolean bFirst, boolean bAdd) {
        AbstractEAIndividual resIndy = (AbstractEAIndividual) indy1.clone();
        double[] v1 = ((InterfaceDataTypeDouble) indy1).getDoubleData();
        double[] v2 = ((InterfaceDataTypeDouble) indy2).getDoubleData();

        double[] dVect = RNG.randomDoubleArray(0, 1, probDim);
        for (int i = 0; i < probDim; i++) {
            dVect[i] *= (v2[i] - v1[i]) / 2.;
        }
        double[] candidate = bFirst ? v1 : v2;
        double[] combi = bAdd ? Mathematics.vvAdd(candidate, dVect) : Mathematics.vvSub(candidate, dVect);
        if (checkRange) {
            Mathematics.projectToRange(combi, range);
        }
        ((InterfaceDataTypeDouble) resIndy).setDoubleGenotype(combi);
        optimizationProblem.evaluate(resIndy);
        refSet.incrFunctionCalls();
        return resIndy;
    }

    @Override
    public void setPopulation(Population pop) {
        refSet = pop;
    }

    private Population getRefSetFitBased(Population curRefSet, Population divPop) {
        int h = refSetSize / 2;
        curRefSet.addAll(divPop.getBestNIndividuals(h, fitCrit));
        Population rest = divPop.getWorstNIndividuals(refSetSize - h, fitCrit);
        // contains worst indies
        double[][] distances = new double[rest.size()][refSetSize];

        for (int i = 0; i < distances.length; i++) { // compute euc. distances of all rest indies to all refset indies.
            for (int j = 0; j < h; j++) {
                distances[i][j] = PhenotypeMetric.dist(rest.getEAIndividual(i), curRefSet.getEAIndividual(j));
            }
        }
        while (curRefSet.size() < refSetSize) {
            // "the vector having highest minimum distance will join the refset."
            int sel = selectHighestMinDistance(distances, h);
            // add the selected diverse indy
            curRefSet.add(rest.getEAIndividual(sel));
            // compute distances
            for (int i = 0; i < distances.length; i++) {
                distances[i][h] = PhenotypeMetric.dist(rest.getEAIndividual(i), curRefSet.getEAIndividual(h));
            }
            // dont! remove it from the rest indi set
            //rest.remove(sel);
            // instead, set a min dist of -1 which will guarantee its not selected again
            distances[sel][0] = -1.;
            h++;
        }

        curRefSet.synchSize();
        return curRefSet;
    }

    private double getMinInCol(int col, int maxRow, double[][] vals) {
        double dmin = vals[col][0];
        if (dmin < 0) {
            return dmin;
        } // tweak for trick
        for (int j = 1; j < maxRow; j++) {
            if (vals[col][j] < dmin) {
                dmin = vals[col][j];
            }
        }
        return dmin;
    }

    private int selectHighestMinDistance(double[][] distances, int maxRow) {
        // first index: rest indies, sec index: refSet indies
        // select the first index with highest minimum.
        double highestMin = getMinInCol(0, maxRow, distances);
        int sel = 0;
        for (int i = 1; i < distances.length; i++) {
            double dtmp = getMinInCol(i, maxRow, distances);
            if (dtmp > highestMin) {
                highestMin = dtmp;
                sel = i;
            }
        }
        return sel;
    }

    private Population diversify() {
        return diversify(new Population());
    }

    private Population diversify(Population pop) {
        int[][] freq = new int[probDim][intervals];
        if (pop.size() > 0) { // count the interval appearances of already given individuals.
            for (int k = 0; k < pop.size(); k++) {
                double[] params = ((InterfaceDataTypeDouble) pop.getEAIndividual(k)).getDoubleData();
                for (int j = 0; j < probDim; j++) {
                    for (int iv = 0; iv < intervals; iv++) {
                        if (isInRangeInterval(params[j], j, iv)) {
                            freq[j][iv]++;
                        }
                    }
                }
            }
        } else {
            // or start with diagonals
            for (int i = 0; i < intervals; i++) {
                pop.add(createDiagIndies(i));
                for (int j = 0; j < probDim; j++) {
                    freq[j][i] = 1;
                }
            }
        }

        while (pop.size() < poolSize) {
            pop.add(createDiverseIndy(freq));
        }
        pop.setTargetSize(poolSize);

        return pop;
    }

    private AbstractEAIndividual createDiverseIndy(int freq[][]) {
        AbstractEAIndividual indy = (AbstractEAIndividual) template.clone();
        InterfaceDataTypeDouble dblIndy = (InterfaceDataTypeDouble) indy;
        double[] genes = dblIndy.getDoubleData();

        for (int i = 0; i < probDim; i++) {
            int interv = selectInterv(i, freq);
            genes[i] = randInRangeInterval(i, interv);
            freq[i][interv]++;
        }

        dblIndy.setDoubleGenotype(genes);
        return indy;
    }

    private double getFreqDepProb(int dim, int interv, int freq[][]) {
        double sum = 0;
        for (int k = 0; k < intervals; k++) {
            sum += (double) freq[dim][k];
        }
        return freq[dim][interv] / sum;
    }

    private int selectInterv(int dim, int freq[][]) {
        double[] probs = new double[intervals];
        for (int i = 0; i < intervals; i++) {
            probs[i] = getFreqDepProb(dim, i, freq);
        }
        double rnd = RNG.randomDouble();
        int sel = 0;
        double sum = probs[0];
        while (sum < rnd) {
            sel++;
            sum += probs[sel];
        }
        if (sum >= 1.0000001) {
            System.err.println("Check this: sum>=1 in selectInterv");
        }
        return sel;
    }

    /**
     * Create probDim individuals where each dimension is initialized within
     * subinterval i for individual i.
     *
     * @param interval
     * @return
     */
    private AbstractEAIndividual createDiagIndies(int interval) {
        AbstractEAIndividual indy = (AbstractEAIndividual) template.clone();
        InterfaceDataTypeDouble dblIndy = (InterfaceDataTypeDouble) indy;
        double[] genes = dblIndy.getDoubleData();
        for (int i = 0; i < probDim; i++) {
            genes[i] = randInRangeInterval(i, interval);
        }
        dblIndy.setDoubleGenotype(genes);
        return indy;
    }

    private boolean isInRangeInterval(double d, int dim, int interval) {
        double dimStep = (range[dim][1] - range[dim][0]) / intervals;
        double lowB = range[dim][0] + (dimStep * interval);
        double upB = lowB + dimStep;
        return isInRange(d, lowB, upB);
    }

    private boolean isInRange(double d, double lowB, double upB) {
        return (lowB <= d) && (d < upB);
    }

    private double randInRangeInterval(int dim, int interval) {
        double dimStep = (range[dim][1] - range[dim][0]) / intervals;
        double lowB = range[dim][0] + (dimStep * interval);
        double upB = lowB + dimStep;
        return RNG.randomDouble(lowB, upB);
    }


    @Override
    public InterfaceOptimizationProblem getProblem() {
        return optimizationProblem;
    }

    @Override
    public String getStringRepresentation() {
        return "ScatterSearch";
    }

    @Override
    public void addPopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        listener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (listener == ea) {
            listener = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getName() {
        return "ScatterSearch";
    }

    private boolean useLSHC() {
        return localSearchMethod.getSelectedTagID() == 0;
    }

    /**
     * @return the doLocalSearch
     */
    public boolean isDoLocalSearch() {
        return doLocalSearch;
    }

    /**
     * @param doLocalSearch the doLocalSearch to set
     */
    public void setDoLocalSearch(boolean doLocalSearch) {
        this.doLocalSearch = doLocalSearch;
        setLSShowProps();
    }

    private void setLSShowProps() {
        GenericObjectEditor.setShowProperty(this.getClass(), "localSearchFitnessFilter", doLocalSearch);
        GenericObjectEditor.setShowProperty(this.getClass(), "localSearchSteps", doLocalSearch);
        GenericObjectEditor.setShowProperty(this.getClass(), "nelderMeadInitPerturbation", doLocalSearch && !useLSHC());
        GenericObjectEditor.setShowProperty(this.getClass(), "localSearchRelativeFilter", doLocalSearch);
        GenericObjectEditor.setShowProperty(this.getClass(), "localSearchMethod", doLocalSearch);
    }

    public String doLocalSearchTipText() {
        return "Perform a local search step";
    }

    /**
     * @return the refSetSize
     */
    public int getRefSetSize() {
        return refSetSize;
    }

    /**
     * @param refSetSize the refSetSize to set
     */
    public void setRefSetSize(int refSetSize) {
        this.refSetSize = refSetSize;
    }

    public String refSetSizeTipText() {
        return "Size of the reference set from which new candidates are created (similar to population size)";
    }

    /**
     * @return the localSearchSteps
     */
    public int getLocalSearchSteps() {
        return localSearchSteps;
    }

    /**
     * @param localSearchSteps the localSearchSteps to set
     */
    public void setLocalSearchSteps(int localSearchSteps) {
        this.localSearchSteps = localSearchSteps;
    }

    public String localSearchStepsTipText() {
        return "Define the number of evaluations performed for one local search.";
    }

    /**
     * @return the localSearchFitnessFilter
     */
    public double getLocalSearchFitnessFilter() {
        return localSearchFitnessFilter;
    }

    /**
     * @param localSearchFitnessFilter the localSearchFitnessFilter to set
     */
    public void setLocalSearchFitnessFilter(double localSearchFitnessFilter) {
        this.localSearchFitnessFilter = localSearchFitnessFilter;
    }

    public String localSearchFitnessFilterTipText() {
        return "Local search is performed only if the fitness is better than this value (absolute crit) or by this factor * (worst-best) fitness (relative).";
    }

    ////////////////////////////////////////////7

    /**
     * This method performs a scatter search runnable.
     */
    public static OptimizerRunnable createScatterSearch(
            int localSearchSteps, double localSearchFitnessFilter,
            double nmInitPerturb, boolean relativeFitCrit,
            int refSetSize,
            InterfaceTerminator term, String dataPrefix,
            AbstractOptimizationProblem problem, InterfacePopulationChangedEventListener listener) {

//		optimizationProblem.initializeProblem();

        OptimizationParameters params = specialSS(localSearchSteps, localSearchFitnessFilter, nmInitPerturb, relativeFitCrit, refSetSize, problem, term);

        OptimizerRunnable rnbl = new OptimizerRunnable(params, dataPrefix);
        return rnbl;
    }

    public static OptimizationParameters standardSS(
            AbstractOptimizationProblem problem) {
        return specialSS(0, 0, 0.1, true, 10, problem, new EvaluationTerminator(10000));
    }

    public static OptimizationParameters specialSS(
            int localSearchSteps, double localSearchFitnessFilter,
            double nmInitPerturb, boolean relativeFitCrit,
            int refSetSize,
            AbstractOptimizationProblem problem, InterfaceTerminator term) {
        ScatterSearch ss = new ScatterSearch();
        problem.initializeProblem();
        ss.setProblem(problem);
        ss.setRefSetSize(refSetSize);
        ss.setNelderMeadInitPerturbation(nmInitPerturb);
        ss.setLocalSearchRelativeFilter(relativeFitCrit);
        if (localSearchSteps > 0) {
            ss.setDoLocalSearch(true);
            ss.setLocalSearchSteps(localSearchSteps);
            ss.setLocalSearchFitnessFilter(localSearchFitnessFilter);
        } else {
            ss.setDoLocalSearch(false);
        }
        Population pop = new Population();
        pop.setTargetSize(refSetSize);
        pop.init();
        problem.initializePopulation(pop);
        ss.initializeByPopulation(pop, true);

        return OptimizerFactory.makeParams(ss, pop, problem, 0, term);
    }

    /**
     * @return the relativeFitCriterion
     */
    public boolean isLocalSearchRelativeFilter() {
        return relativeFitCriterion;
    }

    /**
     * @param relativeFitCriterion the relativeFitCriterion to set
     */
    public void setLocalSearchRelativeFilter(boolean relativeFitCriterion) {
        this.relativeFitCriterion = relativeFitCriterion;
    }

    public String localSearchRelativeFilterTipText() {
        return "If selected, local search will be triggered by relative fitness, else by absolute";
    }

    /**
     * @return the nelderMeadInitPerturbation
     */
    public double getNelderMeadInitPerturbation() {
        return nelderMeadInitPerturbation;
    }

    /**
     * @param nelderMeadInitPerturbation the nelderMeadInitPerturbation to set
     */
    public void setNelderMeadInitPerturbation(double nelderMeadInitPerturbation) {
        this.nelderMeadInitPerturbation = nelderMeadInitPerturbation;
    }

    public String nelderMeadInitPerturbationTipText() {
        return "The relative range of the initial perturbation for creating the initial Nelder-Mead-Simplex";
    }

    /**
     * @return the localSearchMethod
     */
    public SelectedTag getLocalSearchMethod() {
        return localSearchMethod;
    }

    /**
     * @param localSearchMethod the localSearchMethod to set
     */
    public void setLocalSearchMethod(SelectedTag localSearchMethod) {
        this.localSearchMethod = localSearchMethod;
        setLSShowProps();
    }

    public String localSearchMethodTipText() {
        return "The local search method to use";
    }

    /**
     * @return the poolSize
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * @param poolSize the poolSize to set
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String poolSizeTipText() {
        return "The number of individuals created in the diversification step";
    }

    public double getImprovementEpsilon() {
        return improvementEpsilon;
    }

    public void setImprovementEpsilon(double improvementEpsilon) {
        this.improvementEpsilon = improvementEpsilon;
    }

    public String improvementEpsilonTipText() {
        return "Minimal relative fitness improvement for a candidate to enter the refSet - set to zero to deactivate.";
    }

    public double getMinDiversityEpsilon() {
        return minDiversityEpsilon;
    }

    public void setMinDiversityEpsilon(double minDiversityEpsilon) {
        this.minDiversityEpsilon = minDiversityEpsilon;
    }

    public String minDiversityEpsilonTipText() {
        return "Minimal distance to other individuals in the refSet for a candidate to enter the refSet - set to zero to deactivate.";
    }
}
