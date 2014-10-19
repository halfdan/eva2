package eva2.optimization.strategies;

import eva2.gui.BeanInspector;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.operator.crossover.*;
import eva2.optimization.operator.distancemetric.GenotypeMetricBitSet;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.problems.AbstractOptimizationProblem;
import eva2.problems.B1Problem;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.Pair;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * A BinaryScatterSearch implementation taken mainly from [i].
 *
 *         <p/>
 *         F. Gortazar, A. Duarte, M. Laguna and R. Marti: Black Box Scatter Search for
 *         General Classes of Binary Optimization Problems Computers and Operations
 *         research, vol. 37, no. 11, pp. 1977-1986 (2010)
 */
@Description("A basic implementation of a Binary ScatterSearch")
public class BinaryScatterSearch implements InterfaceOptimizer, java.io.Serializable, InterfacePopulationChangedEventListener {
    transient private InterfacePopulationChangedEventListener populationChangedEventListener = null;
    private String identifier = "BinaryScatterSearch";
    private int MaxImpIter = 5;
    private int poolSize = 100;
    private int refSetSize = 10;
    private int fitCrit = -1;
    private int probDim = -1;
    private int generationCycle = 500;
    private double th1 = 0.5;
    private double th2 = 0.5;
    private double g1 = 1.0 / 3.0;
    private double g2 = 1.0 / 3.0;
    private boolean firstTime = true;
    private AbstractEAIndividual template = null;
    private AbstractOptimizationProblem problem = new B1Problem();
    private Population pool = new Population();
    private Population refSet = new Population(10);
    private AdaptiveCrossoverEAMixer cross = new AdaptiveCrossoverEAMixer(new CM1(), new CM2(), new CM3(), new CM4(), new CM5(), new CM6(), new CM7());

    /**
     * Create a new BinaryScatterSearch with default values
     */
    public BinaryScatterSearch() {
    }

    /**
     * Create a new BinaryScatterSearch with the same parameters as the given
     * BinaryScatterSearch
     *
     * @param b
     */
    public BinaryScatterSearch(BinaryScatterSearch b) {
        this.populationChangedEventListener = b.populationChangedEventListener;
        this.identifier = b.identifier;
        this.MaxImpIter = b.MaxImpIter;
        this.poolSize = b.poolSize;
        this.refSetSize = b.refSetSize;
        this.fitCrit = b.fitCrit;
        this.probDim = b.probDim;
        this.generationCycle = b.generationCycle;
        this.th1 = b.th1;
        this.th2 = b.th2;
        this.g1 = b.g1;
        this.g2 = b.g2;
        this.firstTime = b.firstTime;
        this.template = (AbstractEAIndividual) b.template.clone();
        this.problem = (AbstractOptimizationProblem) b.problem.clone();
        this.pool = (Population) b.pool.clone();
        this.refSet = (Population) b.refSet.clone();
        this.cross = b.cross;
    }

    /**
     * Create a new BinaryScatterSearch with the given Parameters
     *
     * @param refSetS                   the refSetSize
     * @param poolS                     the poolSize
     * @param lowerThreshold            the lower Boundary for the local Search
     * @param upperThreshold            the upper Boundary for the local Search
     * @param perCentFirstIndGenerator  how many individuals (in prospect of the
     *                                  poolSize) are generated through the first Generator
     * @param perCentSecondIndGenerator how many individuals (in prospect of the
     *                                  poolSize) are generated through the second Generator
     * @param prob                      the Problem
     */
    public BinaryScatterSearch(
            int refSetS, int poolS, double lowerThreshold, double upperThreshold,
            double perCentFirstIndGenerator, double perCentSecondIndGenerator, AbstractOptimizationProblem prob) {
        this.refSetSize = refSetS;
        this.poolSize = poolS;
        this.th1 = lowerThreshold;
        this.th2 = upperThreshold;
        this.g1 = perCentFirstIndGenerator;
        this.g2 = perCentSecondIndGenerator;
        this.problem = prob;
    }

    /**
     * Create a new BinaryScatterSearch with the given Parameters
     *
     * @param refSetS                   the refSetSize
     * @param poolS                     the poolSize
     * @param lowerThreshold            the lower Boundary for the local Search
     * @param upperThreshold            the upper Boundary for the local Search
     * @param perCentFirstIndGenerator  how many individuals (in prospect of the
     *                                  poolSize) are generated through the first Generator
     * @param perCentSecondIndGenerator how many individuals (in prospect of the
     *                                  poolSize) are generated through the second Generator
     * @param prob                      the Problem
     * @param cross                     the Crossover-Operators
     */
    public BinaryScatterSearch(
            int refSetS, int poolS, double lowerThreshold, double upperThreshold,
            double perCentFirstIndGenerator, double perCentSecondIndGenerator,
            AbstractOptimizationProblem prob, AdaptiveCrossoverEAMixer cross) {
        this.refSetSize = refSetS;
        this.poolSize = poolS;
        this.th1 = lowerThreshold;
        this.th2 = upperThreshold;
        this.g1 = perCentFirstIndGenerator;
        this.g2 = perCentSecondIndGenerator;
        this.problem = prob;
        this.cross = cross;
    }

    /**
     * @return a copy of the current BinaryScatterSearch
     */
    @Override
    public Object clone() {
        return new BinaryScatterSearch(this);
    }

    @Override
    public String getName() {
        return "BSS";
    }

    @Override
    public void addPopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        this.populationChangedEventListener = ea;
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (populationChangedEventListener == ea) {
            populationChangedEventListener = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * evaluate the given Individual and increments the counter. if the
     * individual is null, only the counter is incremented
     *
     * @param indy the individual you want to evaluate
     */
    private void evaluate(AbstractEAIndividual indy) {
        // evaluate the given individual if it is not null
        if (indy == null) {
            System.err.println("tried to evaluate null");
            return;
        }
        this.problem.evaluate(indy);
        // increment the number of evaluations 
        this.refSet.incrFunctionCalls();
    }

    /**
     * Default initialization
     */
    private void defaultInit() {
        this.refSet = new Population();
        this.template = this.problem.getIndividualTemplate();
        if (!(template instanceof InterfaceDataTypeBinary)) {
            System.err.println("Requiring binary data!");
        } else {
            Object dim = BeanInspector.callIfAvailable(problem, "getProblemDimension", null);
            if (dim == null) {
                System.err.println("Couldnt get problem dimension!");
            }
            probDim = (Integer) dim;
            ((InterfaceDataTypeBinary) this.template).setBinaryGenotype(new BitSet(probDim));
        }
        this.firstTime = true;
        this.cross.init(this.template, problem, refSet, Double.MAX_VALUE);
        refSet.addPopulationChangedEventListener(this);
        this.refSet.setNotifyEvalInterval(this.generationCycle);
    }

    @Override
    public void initialize() {
        defaultInit();
        initRefSet(diversify());
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    @Override
    public void initializeByPopulation(Population pop, boolean reset) {
        defaultInit();
        initRefSet(diversify(pop));
        this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
    }

    /**
     * @return a new diversified Population
     */
    private Population diversify() {
        return diversify(new Population());
    }

    /**
     * @param pop the initial Population
     * @return a diversified Population with all the Individuals in the initial
     *         Population
     */
    private Population diversify(Population pop) {
        int numToInit = this.poolSize - pop.size();
        if (numToInit > 0) {
            pop.addAll(generateG1((int) (numToInit * this.g1)));

            generateG2(pop, (int) (numToInit * this.g2));
            generateG3(pop, poolSize - pop.size());
        }
        return pop;
    }

    /**
     * Generate a new Population with diverse Individuals starting with
     * 000...000, then 010101...01, 101010...10, 001001001...001,
     * 110110110...110 and so on The returned population is evaluated.
     *
     * @param pop the initial Population
     * @return the new Population
     */
    private Population generateG1(int numToInit) {
        Population pop = generateG1Pop(numToInit, this.template);
        for (int i = 0; i < pop.size(); i++) {
            evaluate(pop.getEAIndividual(i));
        }
        return pop;
    }

    /**
     * Generate a new Population with diverse Individuals starting with
     * 000...000, then 010101...01, 101010...10, 001001001...001,
     * 110110110...110 and so on
     *
     * @param targetSize The size of the new Population
     * @param template The template individual to use
     * @return a new Population
     */
    public static Population generateG1Pop(int targetSize, AbstractEAIndividual template) {
        boolean method1 = true;
        int i = 1;
        Population pop = new Population(targetSize);
        while (pop.size() < targetSize) {
            AbstractEAIndividual indy = (AbstractEAIndividual) template.clone();
            BitSet data = getBinaryData(indy);
            if (method1) {
                method1 = !method1;
                data.set(0, data.size(), true);
                for (int j = 0; j < data.size(); j += i) {
                    data.flip(j);
                }
                ((InterfaceDataTypeBinary) indy).setBinaryGenotype(data);
                if (i == 1) {
                    i++;
                    method1 = !method1;
                }
            } else {
                method1 = !method1;
                if (i != 1) {
                    data.set(0, data.size(), false);
                    for (int j = 0; j < data.size(); j += i) {
                        data.flip(j);
                    }
                    ((InterfaceDataTypeBinary) indy).setBinaryGenotype(data);
                }
                i++;
            }
            pop.add(indy);
        }
        return pop;
    }

    /**
     * Generate new Individuals that have the individuals of the given
     * Population as a base
     *
     * @param pop the population
     * @return the new Population
     */
    private Population generateG2(Population pop, int numToInit) {
        int origSize = pop.size();
        while (pop.size() < origSize + numToInit) {
            AbstractEAIndividual indy = (AbstractEAIndividual) template.clone();
            InterfaceDataTypeBinary dblIndy = (InterfaceDataTypeBinary) indy;
            BitSet data = dblIndy.getBinaryData();
            data.set(0, data.size(), false);
            for (int i = 0; i < data.size(); i++) {
                if (RNG.flipCoin(Math.min(0.1 + score(i, pop), 1))) {
                    data.set(i, true);
                }
            }
            dblIndy.setBinaryGenotype(data);
            if (!contains(dblIndy, pop)) {
                pop.add(dblIndy);
                evaluate(indy);
            }
        }
        return pop;
    }

    /**
     * Generate new Individuals that have the individuals of the given
     * Population as a base
     *
     * @param pop the population
     * @return the new Population
     */
    private Population generateG3(Population pop, int numToInit) {
        int origSize = pop.size();
        while (pop.size() < origSize + numToInit) {
            AbstractEAIndividual indy = (AbstractEAIndividual) template.clone();
            InterfaceDataTypeBinary dblIndy = (InterfaceDataTypeBinary) indy;
            BitSet data = dblIndy.getBinaryData();
            data.set(0, data.size(), true);
            for (int i = 0; i < data.size(); i++) {
                if (RNG.flipCoin(Math.max(0, 1 - score(i, pop)))) {//???
                    data.set(i, false);
                }
            }
            dblIndy.setBinaryGenotype(data);
            if (!contains(dblIndy, pop)) {
                pop.add(dblIndy);
                evaluate(indy);
            }
        }
        return pop;
    }

    /**
     * calculate the number of individuals in the given Population that have a 1
     * at the i-th position
     *
     * @param i   the position
     * @param pop the population
     * @return The number of individuals that have a '1' on the i-th position
     */
    private static double calculateNumberOFPI1(int i, Population pop) {
        int result = 0;
        for (int j = 0; j < pop.size(); j++) {
            AbstractEAIndividual indy = pop.getEAIndividual(j);
            BitSet binData = getBinaryData(indy);
            if (binData.get(i)) {
                result++;
            }
        }
        return result;
    }

    /**
     * calculate the number of individuals in the given Population that have a 0
     * at the i-th position
     *
     * @param i   the position
     * @param pop the population
     * @return The number of individuals that have a '0' on the i-th position
     */
    private static double calculateNumberOFPI0(int i, Population pop) {
        int result = 0;
        for (int j = 0; j < pop.size(); j++) {
            AbstractEAIndividual indy = pop.getEAIndividual(j);
            BitSet binData = getBinaryData(indy);
            if (!binData.get(i)) {
                result++;
            }
        }
        return result;
    }

    private static BitSet getBinaryData(AbstractEAIndividual indy) {
        if (indy instanceof InterfaceGAIndividual) {
            return ((InterfaceGAIndividual) indy).getBGenotype();
        } else if (indy instanceof InterfaceDataTypeBinary) {
            return ((InterfaceDataTypeBinary) indy).getBinaryData();
        } else {
            throw new RuntimeException("Unable to get binary representation for " + indy.getClass());
        }
    }

    /**
     * calculate the sum of all the FitnessValues of the individuals that have a
     * '0' at the i-th position
     *
     * @param i   the position
     * @param pop the population
     * @return the sum
     */
    private static double calculateSumPI0(int i, Population pop) {
        double result = 0;
        for (int j = 0; j < pop.size(); j++) {
            AbstractEAIndividual indy = pop.getEAIndividual(j);
            BitSet binData = getBinaryData(indy);
            if (binData.get(i)) {
                result += indy.getFitness(0);
            }
        }
        return result;
    }

    /**
     * calculate the sum of all the FitnessValues of the individuals that have a
     * '0' at the i-th position
     *
     * @param i   the position
     * @param pop the population
     * @return the sum
     */
    private static double calculateSumPI1(int i, Population pop) {
        double result = 0;
        for (int j = 0; j < pop.size(); j++) {
            AbstractEAIndividual indy = pop.getEAIndividual(j);
            BitSet binData = getBinaryData(indy);
            if (binData.get(i)) {
                result += indy.getFitness(0);
            }
        }
        return result;
    }

    /**
     * calculates a score that gives a reference Point if the Bit on the i-th
     * position is right. If the bit is set to '1' and you get a high score then
     * the Bit is probably set correct. If the bit is set to '0' and you get a
     * low score then the Bit is probably set correct.
     *
     * @param i   the position
     * @param pop the population
     * @return the score
     */
    public static double score(int i, Population pop) {
        double sumPI1 = calculateSumPI1(i, pop);
        double sumPI0 = calculateSumPI0(i, pop);
        double numberOfPI1 = calculateNumberOFPI1(i, pop);
        double numberOfPI0 = calculateNumberOFPI0(i, pop);
        double v = (sumPI1 / numberOfPI1) / ((sumPI1 / numberOfPI1) + (sumPI0 / numberOfPI0));
        return v;
    }

    /**
     * calculate the first RefSet with the given Population as a reference Point
     *
     * @param pop the generated Pool
     */
    private void initRefSet(Population pop) {
        this.problem.evaluatePopulationStart(this.refSet);
        this.pool = pop;
        refSetUpdate(true);
        Population best = this.refSet.getBestNIndividuals(this.refSetSize / 2, fitCrit);
        for (int i = 0; i < best.size(); i++) {
            //improve(best.getEAIndividual(i));
            AbstractEAIndividual indy = best.getEAIndividual(i);
            AbstractEAIndividual x = improve((AbstractEAIndividual) indy.clone());
            if (x.getFitness(0) < indy.getFitness(0) && !contains((InterfaceDataTypeBinary) x, this.refSet)) {
                this.refSet.remove(indy);
                this.refSet.add(x);
            }
        }
        this.problem.evaluatePopulationEnd(this.refSet);
    }

    /**
     * Update the reference Set
     *
     * @param replaceWorstHalf replaces the worst half of the RefSet if set
     * @return has the Population changed
     */
    private boolean refSetUpdate(boolean replaceWorstHalf) {
        boolean refSetChanged = false;
        Population rest = (Population) this.pool.clone();
        if (this.firstTime) {
            Population tmp = this.pool.getBestNIndividuals(this.pool.size(), this.fitCrit);
            int i = 0;
            while (this.refSet.size() < this.refSetSize) {
                this.refSet.add(tmp.get(i));
                i++;
            }
            rest = this.pool.getWorstNIndividuals(this.poolSize - i, this.fitCrit);
            refSetChanged = true;
        }
        if (replaceWorstHalf) {
            refSetChanged = true;
            // delete the worst half of refSet
            this.refSet.removeMembers(this.refSet.getWorstNIndividuals(this.refSet.size() - this.refSetSize / 2, this.fitCrit), false);
            while (this.refSet.size() < this.refSetSize) {
                ArrayList<Pair<Integer, Double>> list = new ArrayList<Pair<Integer, Double>>();
                for (int i = 0; i < this.refSet.size(); i++) {
                    AbstractEAIndividual indy = this.refSet.getEAIndividual(i);
                    list.add(Population.getClosestFarthestIndy(indy, rest, new GenotypeMetricBitSet(), false));
                }
                Pair<Integer, Double> pair = list.get(0);
                for (Pair<Integer, Double> p : list) {
                    if (p.getTail() < pair.getTail()) {
                        pair = p;
                    }
                }
                this.refSet.add(rest.getEAIndividual(pair.getHead()));
                rest.remove(pair.getHead());
            }
        } else {
            Population best = this.pool.getBestNIndividuals(this.refSetSize, this.fitCrit);
            while (best.size() > 0 && best.getBestEAIndividual().getFitness(0) < this.refSet.getWorstEAIndividual().getFitness(0)) {
                if (!contains((InterfaceDataTypeBinary) best.getBestIndividual(), this.refSet)) {
                    refSetChanged = true;
                    this.refSet.remove(this.refSet.getWorstEAIndividual());
                    this.refSet.add(best.getBestIndividual());
                }
                best.remove(best.getBestEAIndividual());
            }
        }
        this.firstTime = false;
        return refSetChanged;
    }

    /**
     * Order the given List according to the score of the given values
     *
     * @param list the initial List
     * @return the ordered List
     */
    private ArrayList<Integer> order(ArrayList<Integer> list) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (Integer s : list) {
            boolean done = false;
            if (result.isEmpty()) {
                result.add(s);
            } else {
                for (int i = 0; i < result.size(); i++) {
                    if (score(s, this.refSet) > score(result.get(i), this.refSet) && !done) {
                        result.add(i, s);
                        done = true;
                    }
                }
                if (!done) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    /**
     * Do a local search
     *
     * @param indy the individual that will be improved
     * @return the new improved individual
     */
    private AbstractEAIndividual improve(AbstractEAIndividual indy) {
        AbstractEAIndividual tmpIndy = (AbstractEAIndividual) indy.clone();
        BitSet data = ((InterfaceDataTypeBinary) tmpIndy).getBinaryData();
        ArrayList<Integer> cl = new ArrayList<Integer>();
        int localIter = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)) {
                if (score(i, this.refSet) <= this.th2) {
                    cl.add(i);
                }
            } else {
                if (score(i, this.refSet) >= this.th1) {
                    cl.add(i);
                }
            }
        }
        cl = order(cl);
        boolean improvement = true;
        while (improvement && localIter < this.MaxImpIter) {
            improvement = false;
            for (int i : cl) {
                data.flip(i);
                ((InterfaceDataTypeBinary) tmpIndy).setBinaryGenotype(data);
                evaluate(tmpIndy);
                if (tmpIndy.getFitness(0) < indy.getFitness(0)) {
                    improvement = true;
                    indy = (AbstractEAIndividual) tmpIndy.clone();
                    data = ((InterfaceDataTypeBinary) tmpIndy).getBinaryData();
                } else {
                    tmpIndy = (AbstractEAIndividual) indy.clone();
                }
            }
            cl = order(cl);
            for (int i = 0; i < cl.size() - 1; i++) {
                boolean valI = ((InterfaceDataTypeBinary) tmpIndy).getBinaryData().get(i);
                for (int j = i + 1; j < cl.size(); j++) {
                    boolean valJ = ((InterfaceDataTypeBinary) tmpIndy).getBinaryData().get(i);
                    if (valJ != valI) {
                        data.set(i, valJ);
                        data.set(j, valI);
                        ((InterfaceDataTypeBinary) tmpIndy).setBinaryGenotype(data);
                        evaluate(tmpIndy);
                        if (tmpIndy.getFitness(0) < indy.getFitness(0)) {
                            improvement = true;
                            indy = (AbstractEAIndividual) tmpIndy.clone();
                            data = ((InterfaceDataTypeBinary) tmpIndy).getBinaryData();
                            i = cl.size();
                            j = cl.size();
                        } else {
                            tmpIndy = (AbstractEAIndividual) indy.clone();
                        }
                    }
                }
            }
            localIter++;
        }
        return indy;
    }

    /**
     * Combine all the individuals in the reference Set (always 2)
     *
     * @return the List with all the combinations
     */
    public ArrayList<Population> generateSubsets() {
        ArrayList<Population> result = new ArrayList<Population>();
        for (int i = 0; i < this.refSet.size(); i++) {
            for (int j = i + 1; j < this.refSet.size(); j++) {
                Population tmp = new Population();
                tmp.add(this.refSet.getIndividual(i));
                tmp.add(this.refSet.getIndividual(j));
                result.add((Population) tmp.clone());
            }
        }
        return result;
    }

    /**
     * combine the first individual with the second one
     *
     * @param pop the Population
     * @return the new Individual
     */
    public AbstractEAIndividual combineSolution(Population pop) {
        AbstractEAIndividual result = (AbstractEAIndividual) template.clone();
        if (pop.size() >= 2) {
            AbstractEAIndividual indy1 = pop.getEAIndividual(0);
            pop.remove(0);
            // Because some Crossover-Methods need the score, we need to give them the RefSet
            for (int i = 0; i < this.refSet.size(); i++) {
                pop.add(this.refSet.getEAIndividual(i));
            }
            this.cross.update(indy1, problem, refSet, indy1.getFitness(0));
            result = this.cross.mate(indy1, pop)[0];
            //result = indy1.mateWith(s)[0];
        } else if (pop.size() > 0) {
            result = pop.getBestEAIndividual();
        } else {
            System.err.println("Population empty");
            //return null;
        }
        return result;
    }

    /**
     * look if the individual is already in the population
     *
     * @param indy the Individual to be tested
     * @param pop  the population in where to search
     * @return is the individual already in the Population
     */
    private boolean contains(InterfaceDataTypeBinary indy, Population pop) {
        if (pop.size() <= 0) {
            return false;
        } else {
            BitSet data = indy.getBinaryData();
            for (int i = 0; i < pop.size(); i++) {
                BitSet tmpData = ((InterfaceDataTypeBinary) pop.getEAIndividual(i)).getBinaryData();
                if (tmpData.equals(data)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void optimize() {
        problem.evaluatePopulationStart(refSet);
        int funCallsStart = this.refSet.getFunctionCalls();
        do {
            // generate a new Pool
            this.pool = new Population();
            ArrayList<Population> newSubsets = generateSubsets();
            for (Population s : newSubsets) {
                AbstractEAIndividual x = combineSolution(s);
                if (!contains((InterfaceDataTypeBinary) x, this.pool) && this.pool.size() <= this.poolSize) {
                    this.pool.add(x);
                }
            }
            this.refSet.incrFunctionCallsBy(this.pool.size());
            // increase the number of evaluations by the number of evaluations that are performed in the crossover-step
            for (int i = 0; i < this.cross.getEvaluations(); i++) {
                this.refSet.incrFunctionCalls();
            }
            // reset the extra evaluations
            this.cross.resetEvaluations();
            // get the best half of the Populations
            Population best = this.refSet.getBestNIndividuals(this.refSetSize / 2, fitCrit);
            // do a local search on the better half of the reference Set
            for (int i = 0; i < best.size(); i++) {
                AbstractEAIndividual indy = best.getEAIndividual(i);
                AbstractEAIndividual x = improve((AbstractEAIndividual) indy.clone());
                if (x.getFitness(0) < indy.getFitness(0) && !contains((InterfaceDataTypeBinary) x, this.refSet)) {
                    this.refSet.remove(indy);
                    this.refSet.add(x);
                }
            }
            // update the referenceSet
            boolean changed = refSetUpdate(false);
            // if no change occurred, replace the worst half of the referenceSet
            if (!changed) {
                refSetUpdate(true);
            }
        } while (refSet.getFunctionCalls() - funCallsStart < generationCycle);
        problem.evaluatePopulationEnd(refSet);
    }

    @Override
    public Population getPopulation() {
        return this.refSet;
    }

    @Override
    public void setPopulation(Population pop) {
        this.refSet = pop;
        this.refSetSize = pop.getTargetSize();
    }

    public String populationTipText() {
        return "The Population";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        return new SolutionSet(this.refSet);
    }

    @Override
    public void setIdentifier(String name) {
        this.identifier = name;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.problem = (AbstractOptimizationProblem) problem;
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return this.problem;
    }

    @Override
    public String getStringRepresentation() {
        return "BinaryScatterSearch";
    }

    protected void firePropertyChangedEvent(String name) {
        if (this.populationChangedEventListener != null) {
            this.populationChangedEventListener.registerPopulationStateChanged(this, name);
        }
    }

    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        // The events of the interim hill climbing population will be caught here 
        if (name.compareTo(Population.FUN_CALL_INTERVAL_REACHED) == 0) {
            // set funcalls to real value
            refSet.setFunctionCalls(((Population) source).getFunctionCalls());

            //			System.out.println("FunCallIntervalReached at " + (((Population)source).getFunctionCalls()));

            this.firePropertyChangedEvent(Population.NEXT_GENERATION_PERFORMED);
        }
    }

    //----------GUI----------
    public int getPoolSize() {
        return this.poolSize;
    }

    public void setPoolSize(int i) {
        this.poolSize = i;
    }

    public String poolSizeTipText() {
        return "The number of individuals created in the diversification step";
    }

    public double getThresholdHigh() {
        return th2;
    }

    public void setThresholdHigh(double t) {
        this.th2 = t;
    }

    public String thresholdHighTipText() {
        return "Only scores set to 0 with a value below this value will be improved";
    }

    public double getThresholdLow() {
        return th1;
    }

    public void setThresholdLow(double t) {
        this.th1 = t;
    }

    public String thresholdLowTipText() {
        return "Only scores set to 1 with a value above this value will be improved";
    }

    public int getLocalSearchSteps() {
        return this.MaxImpIter;
    }

    public void setLocalSearchSteps(int i) {
        this.MaxImpIter = i;
    }

    public String localSearchStepsTipText() {
        return "Maximum number of local search iterations";
    }

    public AdaptiveCrossoverEAMixer getCrossoverMethods() {
        return this.cross;
    }

    public void setCrossoverMethods(AdaptiveCrossoverEAMixer c) {
        this.cross = c;
    }

    public String crossoverMethodsTipText() {
        return "The crossover Methods used to create the pool";
    }

    public int getGenerationCycle() {
        return this.generationCycle;
    }

    public void setGenerationCycle(int i) {
        this.generationCycle = i;
    }

    public String generationCycleTipText() {
        return "The number of evaluations done in every generation Cycle";
    }

    public double getPerCentFirstGenMethod() {
        return this.g1;
    }

    public void setPerCentFirstGenMethod(double d) {
        this.g1 = d;
    }

    public String perCentFirstGenMethodTipText() {
        return "The number of individuals generated with the first Generation Method. The percentage, that is not covered with the first and the second method will be covered with a third method";
    }

    public double getPerCentSecondGenMethod() {
        return this.g2;
    }

    public void setPerCentSecondGenMethod(double d) {
        this.g2 = d;
    }

    public String perCentSecondGenMethodTipText() {
        return "The number of individuals generated with the second Generation Method. The percentage, that is not covered with the first and the second method will be covered with a third method";
    }
}
