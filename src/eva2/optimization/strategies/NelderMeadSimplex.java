package eva2.optimization.strategies;

import eva2.gui.BeanInspector;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.Population;
import eva2.optimization.population.SolutionSet;
import eva2.optimization.problems.AbstractOptimizationProblem;
import eva2.optimization.problems.AbstractProblemDouble;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;

import java.io.Serializable;
import java.util.Vector;

/**
 * Nelder-Mead-Simplex does not guarantee an equal number of evaluations within
 * each optimize call because of the different step types. Range check is now
 * available by projection at the bounds.
 *
 * @author mkron
 */
public class NelderMeadSimplex implements InterfaceOptimizer, Serializable, InterfacePopulationChangedEventListener {

    private int populationSize = 100;
    // simulating the generational cycle. Set rather small (eg 5) for use as local search, higher for global search (eg 50)
    private int generationCycle = 50;
    private int fitIndex = 0; // choose criterion for multi objective functions
    private Population m_Population;
    private AbstractOptimizationProblem m_Problem;
    private transient Vector<InterfacePopulationChangedEventListener> m_Listener;
    private String m_Identifier = "NelderMeadSimplex";
    private boolean checkConstraints = true;

    public NelderMeadSimplex() {
        setPopulation(new Population(populationSize));
    }

    public NelderMeadSimplex(int popSize) {
        populationSize = popSize;
        setPopulation(new Population(populationSize));
    }

    public NelderMeadSimplex(NelderMeadSimplex a) {
        m_Problem = (AbstractOptimizationProblem) a.m_Problem.clone();
        setPopulation((Population) a.m_Population.clone());
        populationSize = a.populationSize;
        generationCycle = a.generationCycle;
        m_Identifier = a.m_Identifier;
    }

    @Override
    public NelderMeadSimplex clone() {
        return new NelderMeadSimplex(this);
    }

    @Override
    public void setIdentifier(String name) {
        m_Identifier = name;
    }

    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        m_Problem = (AbstractOptimizationProblem) problem;
    }

    public boolean setProblemAndPopSize(InterfaceOptimizationProblem problem) {
        setProblem(problem);
        if (m_Problem instanceof AbstractProblemDouble) {
            setPopulationSize(((AbstractProblemDouble) problem).getProblemDimension() + 1);
            return true;
        } else {
            Object ret = BeanInspector.callIfAvailable(problem, "getProblemDimension", null);
            if (ret != null) {
                setPopulationSize(((Integer) ret) + 1);
                return true;
            }
        }
        return false;
    }

    @Override
    public void addPopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (ea != null) {
            if (m_Listener == null) {
                m_Listener = new Vector<InterfacePopulationChangedEventListener>();
            }
            if (!m_Listener.contains(ea)) {
                m_Listener.add(ea);
            }
        }
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (m_Listener == null) {
            return false;
        } else {
            return m_Listener.remove(ea);
        }
    }

    protected double[] calcChallengeVect(double[] centroid, double[] refX) {
        double[] r = new double[centroid.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = 2 * centroid[i] - refX[i];
        }
//		double alpha = 1.3;
//		for (int i=0; i<r.length; i++) r[i] = centroid[i] + alpha*(centroid[i] - refX[i]);
        return r;
    }

    protected boolean firstIsBetter(double[] first, double[] second) {
        return first[fitIndex] < second[fitIndex];
    }

    protected boolean firstIsBetterEqual(double[] first, double[] second) {
        return first[fitIndex] <= second[fitIndex];
    }

    protected boolean firstIsBetter(AbstractEAIndividual first, AbstractEAIndividual second) {
        return firstIsBetter(first.getFitness(), second.getFitness());
    }

    protected boolean firstIsBetterEqual(AbstractEAIndividual first, AbstractEAIndividual second) {
        return firstIsBetterEqual(first.getFitness(), second.getFitness());
    }

    public AbstractEAIndividual simplexStep(Population subpop) {
        // parameter

        // hole die n-1 besten individuen der fitness dimension fitIndex
        subpop.setSortingFitnessCriterion(fitIndex);
        Population bestpop = subpop.getBestNIndividuals(subpop.size() - 1, fitIndex);
        // und das schlechteste
        AbstractEAIndividual worst = subpop.getWorstEAIndividual(fitIndex);
        AbstractEAIndividual best = subpop.getBestEAIndividual(fitIndex);

        double[][] range = ((InterfaceDataTypeDouble) worst).getDoubleRange();
        double[] x_worst = ((InterfaceDataTypeDouble) worst).getDoubleData();
        int dim = x_worst.length;

        // Centroid berechnen
        double[] centroid = new double[dim];
        for (int i = 0; i < bestpop.size(); i++) {
            for (int j = 0; j < dim; j++) {
                AbstractEAIndividual bestIndi = (AbstractEAIndividual) bestpop.getIndividual(i);
                double[] bestIndyPos = ((InterfaceDataTypeDouble) bestIndi).getDoubleDataWithoutUpdate();
                centroid[j] += bestIndyPos[j] / bestpop.size(); // bug?
            }
        }

        // Reflection
        double[] r = calcChallengeVect(centroid, x_worst);

        if (checkConstraints) {
            if (!Mathematics.isInRange(r, range)) {
                Mathematics.reflectBounds(r, range);
            }
        }
//		AbstractEAIndividual reflectedInd = (AbstractEAIndividual)((AbstractEAIndividual)bestpop.getIndividual(1)).clone(); 
//		((InterfaceDataTypeDouble)reflectedInd).SetDoubleGenotype(r);
//
//		problem.evaluate(reflectedInd);
        AbstractEAIndividual reflectedInd = createEvalIndy(bestpop, r);
        this.m_Population.incrFunctionCalls();

        if (firstIsBetter(best, reflectedInd) && firstIsBetter(reflectedInd, bestpop.getWorstEAIndividual(fitIndex))) {
            return reflectedInd;
        } else if (firstIsBetter(reflectedInd, best)) { //neues besser als bisher bestes => Expansion
            double[] e = new double[dim];
            for (int i = 0; i < dim; i++) {
                e[i] = 3 * centroid[i] - 2 * x_worst[i];
            }
            if (checkConstraints && !Mathematics.isInRange(e, range)) {
                Mathematics.projectToRange(e, range);
            }

            AbstractEAIndividual e_ind = createEvalIndy(bestpop, e);
            this.m_Population.incrFunctionCalls();

            if (firstIsBetter(e_ind, reflectedInd)) { //expandiertes ist besser als reflektiertes
                return e_ind;
            } else {
                return reflectedInd;
            }
        } else if (firstIsBetterEqual(bestpop.getWorstEAIndividual(fitIndex), reflectedInd)) {
            //kontrahiere da neues indi keine verbesserung brachte
            double[] c = new double[dim];
            for (int i = 0; i < dim; i++) {
                c[i] = 0.5 * centroid[i] + 0.5 * x_worst[i];
            }
            if (checkConstraints && !Mathematics.isInRange(c, range)) {
                Mathematics.projectToRange(c, range);
            }

//			AbstractEAIndividual c_ind = (AbstractEAIndividual)((AbstractEAIndividual)bestpop.getIndividual(1)).clone(); 
//			((InterfaceDataTypeDouble)c_ind).SetDoubleGenotype(c);
//			problem.evaluate(c_ind);
            AbstractEAIndividual c_ind = createEvalIndy(bestpop, c);
            this.m_Population.incrFunctionCalls();
            if (firstIsBetterEqual(c_ind, worst)) {
                return c_ind;
            }
        }
        return null;
    }

    private AbstractEAIndividual createEvalIndy(Population pop, double[] newGenotype) {
        AbstractEAIndividual e_ind = (AbstractEAIndividual) ((AbstractEAIndividual) pop.getIndividual(1)).clone();
        ((InterfaceDataTypeDouble) e_ind).SetDoubleGenotype(newGenotype);
        e_ind.resetConstraintViolation();
        m_Problem.evaluate(e_ind);
        if (e_ind.getFitness(0) < 6000) {
            m_Problem.evaluate(e_ind);
        }
        return e_ind;
    }

    @Override
    public String getIdentifier() {
        return m_Identifier;
    }

    @Override
    public String getName() {
        return m_Identifier;
    }

    public static String globalInfo() {
        return "The Nelder-Mead simplex search algorithm for local search. Reflection on bounds may be used for constraint handling.";
    }

    @Override
    public Population getPopulation() {
        return m_Population;
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return m_Problem;
    }

    @Override
    public String getStringRepresentation() {
        StringBuilder strB = new StringBuilder(200);
        strB.append("Nelder-Mead-Simplex Strategy:\nOptimization Problem: ");
        strB.append(this.m_Problem.getStringRepresentationForProblem(this));
        strB.append("\n");
        strB.append(this.m_Population.getStringRepresentation());
        return strB.toString();
    }

    @Override
    public void init() {
        initByPopulation(m_Population, true);
    }

    @Override
    public void initByPopulation(Population pop, boolean reset) {
        setPopulation(pop);
        pop.addPopulationChangedEventListener(this);
        if (reset) {
            m_Problem.initializePopulation(m_Population);
            m_Problem.evaluate(m_Population);
        }
//		fireNextGenerationPerformed();
    }

    private void fireNextGenerationPerformed() {
        if (m_Listener != null) {
            for (int i = 0; i < m_Listener.size(); i++) {
                m_Listener.elementAt(i).registerPopulationStateChanged(this, Population.nextGenerationPerformed);
            }
        }
    }

    @Override
    public void optimize() {
        // make at least as many calls as there are individuals within the population.
        // this simulates the generational loop expected by some other modules
        int evalCntStart = m_Population.getFunctionCalls();
        int evalsDone = 0;
        m_Problem.evaluatePopulationStart(m_Population);
        do {
            AbstractEAIndividual ind = simplexStep(m_Population);
            if (ind != null) { //Verbesserung gefunden
                double[] x = ((InterfaceDataTypeDouble) ind).getDoubleData();
                double[][] range = ((InterfaceDataTypeDouble) ind).getDoubleRange();
                if (!Mathematics.isInRange(x, range)) {
                    System.err.println("WARNING: nelder mead step produced indy out of range!");
//					Mathematics.projectToRange(x, range);
//					((InterfaceDataTypeDouble)ind).SetDoubleGenotype(x);
//					problem.evaluate(ind);
//					this.m_Population.incrFunctionCalls();
                }
                m_Population.set(m_Population.getIndexOfWorstIndividualNoConstr(fitIndex), ind, fitIndex);
            } else {//keine Verbesserung gefunden shrink!!

                double[] u_1 = ((InterfaceDataTypeDouble) m_Population.getBestEAIndividual(fitIndex)).getDoubleData();

                for (int j = 0; j < m_Population.size(); j++) {
                    double[] c = ((InterfaceDataTypeDouble) m_Population.getEAIndividual(j)).getDoubleData();
                    for (int i = 0; i < c.length; i++) {
                        c[i] = 0.5 * c[i] + 0.5 * u_1[i];
                    }
                    ((InterfaceDataTypeDouble) m_Population.getEAIndividual(j)).SetDoubleGenotype(c);
//					m_Population.getEAIndividual(j).resetConstraintViolation(); // not a good idea because during evaluation, a stats update may be performed which mustnt see indies which are evaluated, but possible constraints have been reset.
                }
                m_Problem.evaluate(m_Population);
            }
            evalsDone = m_Population.getFunctionCalls() - evalCntStart;
        } while (evalsDone < generationCycle);
        m_Problem.evaluatePopulationEnd(m_Population);
        this.m_Population.incrGeneration();
    }

    @Override
    public void setPopulation(Population pop) {
        m_Population = pop;
        m_Population.addPopulationChangedEventListener(this);
        m_Population.setNotifyEvalInterval(populationSize);
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        Population pop = getPopulation();
        return new SolutionSet(pop, pop);
    }

    /**
     * @return the populationSize
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * @param populationSize the populationSize to set
     */
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
        if (m_Population != null) {
            m_Population.setTargetSize(populationSize);
            m_Population.setNotifyEvalInterval(m_Population.getTargetSize());
        }
    }

    public String populationSizeTipText() {
        return "The population size should be adapted to the dimensions of the problem (e.g. n+1)";
    }

    @Override
    public void registerPopulationStateChanged(Object source, String name) {
        if (name.compareTo(Population.funCallIntervalReached) == 0) {
            fireNextGenerationPerformed();
        }// else System.err.println("unknown event!");
    }

    /**
     * This method creates a Nelder-Mead instance.
     *
     * @param pop      The size of the population
     * @param problem  The problem to be optimized
     * @param listener
     * @return An optimization procedure that performs nelder mead optimization.
     */
    public static final NelderMeadSimplex createNelderMeadSimplex(AbstractOptimizationProblem problem,
                                                                  InterfacePopulationChangedEventListener listener) {

        problem.initializeProblem();
        NelderMeadSimplex nms = new NelderMeadSimplex();
        nms.setProblemAndPopSize(problem);

        if (listener != null) {
            nms.addPopulationChangedEventListener(listener);
        }
        nms.init();

        if (listener != null) {
            listener.registerPopulationStateChanged(nms.getPopulation(), "");
        }

        return nms;
    }

    /**
     * This method creates a Nelder-Mead instance with an initial population
     * around a given candidate solution. The population is created as a simplex
     * with given perturbation ratio or randomly across the search range if the
     * perturbation ratio is zero or below zero.
     *
     * @param problem           The problem to be optimized
     * @param candidate         starting point of the search
     * @param perturbationRatio perturbation ratio relative to the problem range
     *                          for the initial simplex creation
     * @param listener
     * @return An optimization procedure that performs nelder mead optimization.
     */
    public static final NelderMeadSimplex createNelderMeadSimplexLocal(AbstractOptimizationProblem problem,
                                                                       AbstractEAIndividual candidate, double perturbationRatio,
                                                                       InterfacePopulationChangedEventListener listener) {

        // TODO this method might be superfluous when using PostProcess
        problem.initializeProblem();
        NelderMeadSimplex nms = new NelderMeadSimplex();
        nms.setProblemAndPopSize(problem);

        Population initialPop;
        if (perturbationRatio <= 0) { // random case
            initialPop = new Population(nms.getPopulationSize());
            problem.initializePopulation(initialPop);
            initialPop.set(0, candidate);
        } else {
            double[][] range = ((InterfaceDataTypeDouble) candidate).getDoubleRange();
            if (range.length != nms.getPopulationSize() - 1) {
                System.err.println("Unexpected population size for nelder mead!");
            }
            initialPop = createNMSPopulation(candidate, perturbationRatio, range, true);
        }
        if (listener != null) {
            nms.addPopulationChangedEventListener(listener);
        }
        nms.initByPopulation(initialPop, false);
        //nms.setPopulation(initialPop);

        return nms;
    }

    /**
     * From a given candidate solution, create n solutions around the candidate,
     * where every i-th new candidate differs in i dimensions by a distance of
     * perturbRatio relative to the range in that dimension (respecting the
     * range). The new solutions are returned as a population, which, if
     * includeCand is true, also contains the initial candidate. However, the
     * new candidates have not been evaluated.
     *
     * @param candidate
     * @param perturbRelative
     * @param range
     * @param includeCand
     * @return
     */
    public static Population createNMSPopulation(AbstractEAIndividual candidate, double perturbRelative, double[][] range, boolean includeCand) {
        Population initPop = new Population();
        if (includeCand) {
            initPop.add(candidate);
        }
        if (perturbRelative >= 1. || (perturbRelative <= 0.)) {
            System.err.println("Warning: perturbation ratio should lie between 0 and 1! (NelderMeadSimplex:createNMSPopulation)");
        }
        addPerturbedPopulation(perturbRelative, initPop, range, candidate);
        return initPop;
    }

    private static void addPerturbedPopulation(double perturbationRatio,
                                               Population initialPop, double[][] range, AbstractEAIndividual candidate) {
        AbstractEAIndividual indy = (AbstractEAIndividual) candidate.clone();
        // span by perturbation, every new individual i is modified in dimension i by
        // a value of perturbRatio*range_i such that a simplex of relative side length perturbRatio is created. 
        for (int i = 0; i < range.length; i += 1) {
            double curPerturb = ((range[i][1] - range[i][0]) * perturbationRatio);
            double[] dat = ((InterfaceDataTypeDouble) indy).getDoubleData();
            if (dat[i] == range[i][1]) { // in this case the bound is said to be too close 
                dat[i] = Math.max(dat[i] - curPerturb, range[i][0]);
            } else {
                dat[i] = Math.min(dat[i] + curPerturb, range[i][1]);
            }
            ((InterfaceDataTypeDouble) indy).SetDoubleGenotype(dat);
            indy.resetConstraintViolation();
            initialPop.add((AbstractEAIndividual) indy.clone());
        }
        initialPop.synchSize();
    }

    /**
     * @param generationCycle the generationCycle to set
     */
    public void setGenerationCycle(int generationCycle) {
        this.generationCycle = generationCycle;
    }

    public boolean isCheckRange() {
        return checkConstraints;
    }

    public void setCheckRange(boolean checkRange) {
        this.checkConstraints = checkRange;
    }

    public String checkRangeTipText() {
        return "Mark to check range constraints by reflection/projection";
    }

    public int getCritIndex() {
        return fitIndex;
    }

    public void setCritIndex(int fitIndex) {
        this.fitIndex = fitIndex;
    }

    public String critIndexTipText() {
        return "For multi-criterial problems, set the index of the fitness to be used in 0..n-1. Default is 0";
    }
}
