package eva2.optimization.strategies;

import eva2.gui.BeanInspector;
import eva2.gui.GenericObjectEditor;
import eva2.optimization.go.InterfacePopulationChangedEventListener;
import eva2.optimization.enums.DETypeEnum;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operators.selection.replacement.ReplacementCrowding;
import eva2.optimization.operators.selection.replacement.ReplacementNondominatedSortingDistanceCrowding;
import eva2.optimization.populations.InterfaceSolutionSet;
import eva2.optimization.populations.Population;
import eva2.optimization.populations.SolutionSet;
import eva2.optimization.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.optimization.problems.AbstractOptimizationProblem;
import eva2.optimization.problems.F1Problem;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import java.util.Vector;

/**
 * Differential evolution implementing DE1 and DE2 following the paper of Storm
 * and Price and the Trigonometric DE published recently. Please note that DE
 * will only work on real-valued genotypes and will ignore all mutation and
 * crossover operators selected. Added aging mechanism to provide for
 * dynamically changing problems. If an individual reaches the age limit, it is
 * doomed and replaced by the next challenge vector, even if its worse.
 *
 */
public class DifferentialEvolution implements InterfaceOptimizer, java.io.Serializable {

    protected Population m_Population = new Population();
    protected transient Population children = null;
    protected AbstractOptimizationProblem m_Problem = new F1Problem();
    private DETypeEnum m_DEType;
    private double m_F = 0.8;
    private double m_k = 0.6; // AKA CR
    private double m_Lambda = 0.6;
    private double m_Mt = 0.05;
    private int maximumAge = -1;
    private boolean reEvaluate = false;
    // to log the parents of a newly created indy.
    public boolean doLogParents = false; // deactivate for better performance
    private transient Vector<AbstractEAIndividual> parents = null;
    private boolean randomizeFKLambda = false;
    private boolean generational = true;
    private String m_Identifier = "";
    transient private Vector<InterfacePopulationChangedEventListener> m_Listener = new Vector<InterfacePopulationChangedEventListener>();
    private boolean forceRange = true;
    private boolean cyclePop = false; // if true, individuals are used as parents in a cyclic sequence - otherwise randomly 
    private boolean compareToParent = true;  // if true, the challenge indy is compared to its parent, otherwise to a random individual

    /**
     * A constructor.
     *
     */
    public DifferentialEvolution() {
        // sets DE2 as default
        m_DEType = DETypeEnum.DE2_CurrentToBest;
    }

    public DifferentialEvolution(int popSize, DETypeEnum type, double f, double k, double lambda, double mt) {
        m_Population = new Population(popSize);
        m_DEType = type;
        m_F = f;
        m_k = k;
        m_Lambda = lambda;
        m_Mt = mt;
    }

    /**
     * The copy constructor.
     *
     * @param a
     */
    public DifferentialEvolution(DifferentialEvolution a) {
        this.m_DEType = a.m_DEType;
        this.m_Population = (Population) a.m_Population.clone();
        this.m_Problem = (AbstractOptimizationProblem) a.m_Problem.clone();
        this.m_Identifier = a.m_Identifier;
        this.m_F = a.m_F;
        this.m_k = a.m_k;
        this.m_Lambda = a.m_Lambda;
        this.m_Mt = a.m_Mt;

        this.maximumAge = a.maximumAge;
        this.randomizeFKLambda = a.randomizeFKLambda;
        this.forceRange = a.forceRange;
        this.cyclePop = a.cyclePop;
        this.compareToParent = a.compareToParent;
    }

    @Override
    public Object clone() {
        return (Object) new DifferentialEvolution(this);
    }

    @Override
    public void init() {
        this.m_Problem.initializePopulation(this.m_Population);
//        children = new Population(m_Population.size());
        this.evaluatePopulation(this.m_Population);
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    public void hideHideable() {
        setDEType(getDEType());
    }

    /**
     * This method will init the optimizer with a given population
     *
     * @param pop The initial population
     * @param reset If true the population is reset.
     */
    @Override
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population) pop.clone();
        if (reset) {
            this.m_Population.init();
            this.evaluatePopulation(this.m_Population);
            this.firePropertyChangedEvent(Population.nextGenerationPerformed);
        }
//        if (reset) this.m_Population.init();
//        else children = new Population(m_Population.size());
    }

    /**
     * This method will evaluate the current population using the given problem.
     *
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
        this.m_Problem.evaluate(population);
        population.incrGeneration();
    }

    /**
     * This method returns a difference vector between two random individuals
     * from the population. This method should make sure that delta is not zero.
     *
     * @param pop The population to choose from
     * @return The delta vector
     */
    private double[] fetchDeltaRandom(Population pop) {
        double[] x1, x2;
        double[] result;
        boolean isEmpty;
        int iterations = 0;

        AbstractEAIndividual x1Indy = getRandomIndy(pop);
        x1 = getGenotype(x1Indy);

        if (parents != null) {
            parents.add(x1Indy);
        }

        result = new double[x1.length];
        isEmpty = true;
        AbstractEAIndividual x2Indy = null;
        while (isEmpty && (iterations < pop.size())) {
            x2Indy = getRandomIndy(pop);
            x2 = getGenotype(x2Indy);

            for (int i = 0; i < x1.length; i++) {
                result[i] = x1[i] - x2[i];
                isEmpty = (isEmpty && (result[i] == 0));
            }
            iterations++;
        }
        if (!isEmpty && (parents != null)) {
            parents.add(x2Indy);
        }

        while (isEmpty) {
            // for n (popSize) iterations there were only zero vectors found
            // so now the hard way: construct a random vector
            for (int i = 0; i < x1.length; i++) {
                if (RNG.flipCoin(1 / (double) x1.length)) {
                    result[i] = 0.01 * RNG.gaussianDouble(0.1);
                } else {
                    result[i] = 0;
                }
                isEmpty = (isEmpty && (result[i] == 0));
            }
            // single parent! dont add another one
        }

        return result;
    }

    /**
     * This method returns a difference vector between two random individuals
     * from the population. This method should make sure that delta is not zero.
     *
     * @param pop The population to choose from
     * @return The delta vector
     */
    private double[] fetchDeltaCurrentRandom(Population pop, InterfaceDataTypeDouble indy) {
        double[] x1, x2;
        double[] result;
       boolean isEmpty;
        int iterations = 0;


        x1 = indy.getDoubleData();


        if (parents != null) {
            parents.add((AbstractEAIndividual) indy);
        }

        result = new double[x1.length];
        isEmpty = true;
        AbstractEAIndividual x2Indy = null;
        while (isEmpty && (iterations < pop.size())) {
            x2Indy = getRandomIndy(pop);
            x2 = getGenotype(x2Indy);

            for (int i = 0; i < x1.length; i++) {
                result[i] = x1[i] - x2[i];
                isEmpty = (isEmpty && (result[i] == 0));
            }
            iterations++;
        }
        if (!isEmpty && (parents != null)) {
            parents.add(x2Indy);
        }

        while (isEmpty) {
            // for n (popSize) iterations there were only zero vectors found
            // so now the hard way: construct a random vector
            for (int i = 0; i < x1.length; i++) {
                if (RNG.flipCoin(1 / (double) x1.length)) {
                    result[i] = 0.01 * RNG.gaussianDouble(0.1);
                } else {
                    result[i] = 0;
                }
                isEmpty = (isEmpty && (result[i] == 0));
            }
            // single parent! dont add another one
        }

        return result;
    }

    /**
     * This method will return the delta vector to the best individual
     *
     * @param pop The population to choose the best from
     * @param indy The current individual
     * @return the delta vector
     */
    private double[] fetchDeltaBest(Population pop, InterfaceDataTypeDouble indy) {
        double[] x1, result;
        AbstractEAIndividual xbIndy;

        x1 = indy.getDoubleData();
        result = new double[x1.length];
        if (m_Problem instanceof AbstractMultiObjectiveOptimizationProblem) {
            // implements MODE for the multi-objective case: a dominating individual is selected for difference building
            Population domSet = pop.getDominatingSet((AbstractEAIndividual) indy);
            if (domSet.size() > 0) {
                xbIndy = getRandomIndy(domSet);
            } else {
                return result; // just return a zero vector. this will happen automatically if domSet contains only the individual itself
            }
        } else {
            xbIndy = getBestIndy(pop);
        }

        double[] xb = getGenotype(xbIndy);
        if (parents != null) {
            parents.add(xbIndy);
        } // given indy argument is already listed

        for (int i = 0; i < x1.length; i++) {
            result[i] = xb[i] - x1[i];
        }

        return result;
    }

    /**
     * This method returns two parents to the original individual
     *
     * @param pop The population to choose from
     * @return the delta vector
     */
//    private double[][] chooseRandomParents(Population pop) {
//        InterfaceESIndividual indy1, indy2;
//        double[][] result = new double[2][];
//        try {
//            indy1 = (InterfaceESIndividual)pop.get(RNG.randomInt(0, pop.size()-1));
//            indy2 = (InterfaceESIndividual)pop.get(RNG.randomInt(0, pop.size()-1));
//        } catch (java.lang.ClassCastException e) {
//            System.out.println("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
//            return result;
//        }
//        result[0] = indy1.getDGenotype();
//        result[1] = indy2.getDGenotype();
//        return result;
//    }
    /**
     * This method will generate one new individual from the given population
     *
     * @param pop The current population
     * @return AbstractEAIndividual
     */
    public AbstractEAIndividual generateNewIndividual(Population pop, int parentIndex) {
//    	int firstParentIndex;
        AbstractEAIndividual indy;
        InterfaceDataTypeDouble esIndy;

        if (doLogParents) {
            parents = new Vector<AbstractEAIndividual>();
        } else {
            parents = null;
        }
        try {
            // select one random indy as starting individual. its a parent in any case.
            if (parentIndex < 0) {
                parentIndex = RNG.randomInt(0, pop.size() - 1);
            }
            indy = (AbstractEAIndividual) (pop.getEAIndividual(parentIndex)).getClone();
            esIndy = (InterfaceDataTypeDouble) indy;
        } catch (java.lang.ClassCastException e) {
            throw new RuntimeException("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
//            return (AbstractEAIndividual)((AbstractEAIndividual)pop.get(RNG.randomInt(0, pop.size()-1))).getClone();
        }
        double[] nX, vX, oX;
        oX = esIndy.getDoubleData();
        vX = oX.clone();
        nX = new double[oX.length];
        switch (this.m_DEType) {
            case DE1_Rand_1: {
                // this is DE1 or DE/rand/1
                double[] delta = this.fetchDeltaRandom(pop);
                if (parents != null) {
                    parents.add(pop.getEAIndividual(parentIndex));
                }  // Add wherever oX is used directly
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.getCurrentF() * delta[i];
                }
                break;
            }
            case DE_CurrentToRand: {
                // this is DE/current-to-rand/1
                double[] rndDelta = this.fetchDeltaRandom(pop);
                double[] bestDelta = this.fetchDeltaCurrentRandom(pop, esIndy);
                if (parents != null) {
                    parents.add(pop.getEAIndividual(parentIndex));
                }  // Add wherever oX is used directly
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.getCurrentLambda() * bestDelta[i] + this.getCurrentF() * rndDelta[i];
                }
                break;
            }
            case DE2_CurrentToBest: {
                // this is DE2 or DE/current-to-best/1
                double[] rndDelta = this.fetchDeltaRandom(pop);
                double[] bestDelta = this.fetchDeltaBest(pop, esIndy);
                if (parents != null) {
                    parents.add(pop.getEAIndividual(parentIndex));
                }  // Add wherever oX is used directly
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.getCurrentLambda() * bestDelta[i] + this.getCurrentF() * rndDelta[i];
                }
                break;
            }
            case DE_Best_2: {
                // DE/best/2
                AbstractEAIndividual bestIndy = getBestIndy(pop);
                oX = getGenotype(bestIndy);
                if (parents != null) {
                    parents.add(bestIndy);
                }  // Add best instead of preselected
                double[] delta1 = this.fetchDeltaRandom(pop);
                double[] delta2 = this.fetchDeltaRandom(pop);
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.getCurrentF() * (delta1[i] + delta2[i]);
                }
                break;
            }
            case TrigonometricDE: {
                // this is trigonometric mutation
                if (parents != null) {
                    parents.add(pop.getEAIndividual(parentIndex));
                }  // Add wherever oX is used directly
                if (RNG.flipCoin(this.m_Mt)) {
                    double[] xk, xl;
                    double p, pj, pk, pl;
                    InterfaceDataTypeDouble indy1 = null, indy2 = null;
                    try {
                        // and i got indy!
                        indy1 = (InterfaceDataTypeDouble) pop.get(RNG.randomInt(0, pop.size() - 1));
                        indy2 = (InterfaceDataTypeDouble) pop.get(RNG.randomInt(0, pop.size() - 1));
                        if (parents != null) {
                            parents.add((AbstractEAIndividual) indy1);
                            parents.add((AbstractEAIndividual) indy2);
                        }
                    } catch (java.lang.ClassCastException e) {
                        EVAERROR.errorMsgOnce("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
                    }
                    xk = indy1.getDoubleData();
                    xl = indy2.getDoubleData();
                    p = Math.abs(((AbstractEAIndividual) esIndy).getFitness(0)) + Math.abs(((AbstractEAIndividual) indy1).getFitness(0)) + Math.abs(((AbstractEAIndividual) indy2).getFitness(0));
                    pj = Math.abs(((AbstractEAIndividual) esIndy).getFitness(0)) / p;
                    pk = Math.abs(((AbstractEAIndividual) indy1).getFitness(0)) / p;
                    pl = Math.abs(((AbstractEAIndividual) indy2).getFitness(0)) / p;
                    for (int i = 0; i < oX.length; i++) {
                        vX[i] = (oX[i] + xk[i] + xl[i]) / 3.0 + ((pk - pj) * (oX[i] - xk[i])) + ((pl - pk) * (xk[i] - xl[i])) + ((pj - pl) * (xl[i] - oX[i]));
                    }
                } else {
                    // this is DE1
                    double[] delta = this.fetchDeltaRandom(pop);
                    if (parents != null) {
                        parents.add(pop.getEAIndividual(parentIndex));
                    }  // Add wherever oX is used directly
                    for (int i = 0; i < oX.length; i++) {
                        vX[i] = oX[i] + this.getCurrentF() * delta[i];
                    }
                }
                break;
            }
        }
        int k = RNG.randomInt(oX.length); // at least one position is changed
        for (int i = 0; i < oX.length; i++) {
            if ((i == k) || RNG.flipCoin(this.getCurrentK())) {
                // it is altered
                nX[i] = vX[i];
            } else {
                // it remains the same
                nX[i] = oX[i];
            }
        }
        // setting the new genotype and fitness
        if (forceRange) {
            Mathematics.projectToRange(nX, esIndy.getDoubleRange());
        } // why did this never happen before?
        esIndy.SetDoubleGenotype(nX);
        indy.SetAge(0);
        indy.resetConstraintViolation();
        double[] fit = new double[1];
        fit[0] = 0;
        indy.setFitness(fit);
        if (parents != null) {
            indy.setParents(parents);
        }
        return indy;
    }

    private double getCurrentK() {
        if (randomizeFKLambda) {
            return RNG.randomDouble(m_k * 0.8, m_k * 1.2);
        } else {
            return m_k;
        }
    }

    private double getCurrentLambda() {
        if (randomizeFKLambda) {
            return RNG.randomDouble(m_Lambda * 0.8, m_Lambda * 1.2);
        } else {
            return m_Lambda;
        }
    }

    private double getCurrentF() {
        if (randomizeFKLambda) {
            return RNG.randomDouble(m_F * 0.8, m_F * 1.2);
        } else {
            return m_F;
        }
    }

    private AbstractEAIndividual getBestIndy(Population pop) {
        return (AbstractEAIndividual) pop.getBestIndividual();
    }

    private AbstractEAIndividual getRandomIndy(Population pop) {
        if (pop.size() < 1) {
            System.err.println("Error: invalid pop size in DE!");
            System.err.println("DE: \n" + BeanInspector.toString(this) + "\nPop: \n" + BeanInspector.toString(pop));
        }

        int randIndex = RNG.randomInt(0, pop.size() - 1);
        return pop.getEAIndividual(randIndex);
    }

    private double[] getGenotype(AbstractEAIndividual indy) {
        try {
            return ((InterfaceDataTypeDouble) indy).getDoubleData();
        } catch (java.lang.ClassCastException e) {
            EVAERROR.errorMsgOnce("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
            return null;
        }
    }

    @Override
    public void optimize() {
        if (generational) {
            optimizeGenerational();
        } else {
            optimizeSteadyState();
        }
    }

    /**
     * This generational DE variant calls the method
     * AbstractOptimizationProblem.evaluate(Population). Its performance may be
     * slightly worse for schemes that rely on current best individuals, because
     * improvements are not immediately incorporated as in the steady state DE.
     * However it may be easier to parallelize.
     *
     */
    public void optimizeGenerational() {
//        AbstractEAIndividual    indy = null, orig;
        int parentIndex;
        // required for dynamic problems especially
//        problem.evaluatePopulationStart(m_Population);
        if (children == null) {
            children = new Population(m_Population.size());
        } else {
            children.clear();
        }
        for (int i = 0; i < this.m_Population.size(); i++) {
            if (cyclePop) {
                parentIndex = i;
            } else {
                parentIndex = RNG.randomInt(0, this.m_Population.size() - 1);
            }
            AbstractEAIndividual indy = generateNewIndividual(m_Population, parentIndex);
            children.add(indy);
        }

        children.setGenerationTo(m_Population.getGeneration());
        m_Problem.evaluate(children);

        /**
         * MdP: added a reevalutation mechanism for dynamically changing
         * problems
         */
        if (isReEvaluate()) {
            for (int i = 0; i < this.m_Population.size(); i++) {

                if (((AbstractEAIndividual) m_Population.get(i)).getAge() >= maximumAge) {
                    this.m_Problem.evaluate(((AbstractEAIndividual) m_Population.get(i)));
                    ((AbstractEAIndividual) m_Population.get(i)).SetAge(0);
                    m_Population.incrFunctionCalls();
                }
            }
        }

        int nextDoomed = getNextDoomed(m_Population, 0);
        for (int i = 0; i < this.m_Population.size(); i++) {
            AbstractEAIndividual indy = children.getEAIndividual(i);
            if (cyclePop) {
                parentIndex = i;
            } else {
                parentIndex = RNG.randomInt(0, this.m_Population.size() - 1);
            }
            if (nextDoomed >= 0) {	// this one is lucky, may replace an 'old' one
                m_Population.replaceIndividualAt(nextDoomed, indy);
                nextDoomed = getNextDoomed(m_Population, nextDoomed + 1);
            } else {
                if (m_Problem instanceof AbstractMultiObjectiveOptimizationProblem & indy.getFitness().length > 1) {
                    ReplacementCrowding repl = new ReplacementCrowding();
                    repl.insertIndividual(indy, m_Population, null);
                } else {
//					index   = RNG.randomInt(0, this.m_Population.size()-1);
                    if (!compareToParent) {
                        parentIndex = RNG.randomInt(0, this.m_Population.size() - 1);
                    }
                    AbstractEAIndividual orig = (AbstractEAIndividual) this.m_Population.get(parentIndex);
                    if (indy.isDominatingDebConstraints(orig)) {
                        this.m_Population.replaceIndividualAt(parentIndex, indy);
                    }
                }
            }
        }
        this.m_Population.incrFunctionCallsBy(children.size());
        this.m_Population.incrGeneration();
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    public void optimizeSteadyState() {
        AbstractEAIndividual indy = null, orig;
        int index;

        int nextDoomed = getNextDoomed(m_Population, 0);

        // required for dynamic problems especially
        m_Problem.evaluatePopulationStart(m_Population);


        /**
         * MdP: added a reevalutation mechanism for dynamically changing
         * problems
         */
        if (isReEvaluate()) {
            nextDoomed = -1;
            for (int i = 0; i < this.m_Population.size(); i++) {

                if (((AbstractEAIndividual) m_Population.get(i)).getAge() >= maximumAge) {
                    this.m_Problem.evaluate(((AbstractEAIndividual) m_Population.get(i)));
                    ((AbstractEAIndividual) m_Population.get(i)).SetAge(0);
                    m_Population.incrFunctionCalls();
                }
            }
        }


        for (int i = 0; i < this.m_Population.size(); i++) {
            if (cyclePop) {
                index = i;
            } else {
                index = RNG.randomInt(0, this.m_Population.size() - 1);
            }
            indy = generateNewIndividual(m_Population, index);
//        	if (cyclePop) indy = this.generateNewIndividual(this.m_Population, i);
//        	else indy = this.generateNewIndividual(this.m_Population, -1);
            this.m_Problem.evaluate(indy);
            this.m_Population.incrFunctionCalls();
            if (nextDoomed >= 0) {	// this one is lucky, may replace an 'old' one
                m_Population.replaceIndividualAt(nextDoomed, indy);
                nextDoomed = getNextDoomed(m_Population, nextDoomed + 1);
            } else {
                if (m_Problem instanceof AbstractMultiObjectiveOptimizationProblem) {

                    if (indy.isDominatingDebConstraints(m_Population.getEAIndividual(index))) { //child dominates the parent replace the parent
                        m_Population.replaceIndividualAt(index, indy);
                    } else if (!(m_Population.getEAIndividual(index).isDominatingDebConstraints(indy))) { //do nothing if parent dominates the child use crowding if neither one dominates the other one
                        ReplacementNondominatedSortingDistanceCrowding repl = new ReplacementNondominatedSortingDistanceCrowding();
                        repl.insertIndividual(indy, m_Population, null);
                    }
                    //	ReplacementCrowding repl = new ReplacementCrowding();
                    //	repl.insertIndividual(indy, m_Population, null);


                } else {
//					index   = RNG.randomInt(0, this.m_Population.size()-1);
                    if (!compareToParent) {
                        index = RNG.randomInt(0, this.m_Population.size() - 1);
                    }
                    orig = (AbstractEAIndividual) this.m_Population.get(index);
                    if (indy.isDominatingDebConstraints(orig)) {
                        this.m_Population.replaceIndividualAt(index, indy);
                    }
                }
            }
        }

//////// this was a non-steady-state-version
//        if (children==null) children = new Population(m_Population.size());
//        for (int i = 0; i < this.m_Population.size(); i++) {
//            indy = this.generateNewIndividual(this.m_Population);
//            this.problem.evaluate(indy);
//            this.m_Population.incrFunctionCalls();
//            children.add(indy);
//        }
//        int nextDoomed = getNextDoomed(m_Population, 0);
//        
//        for (int i=0; i<this.m_Population.size(); i++) {
//    		indy 	= (AbstractEAIndividual)children.get(i);
//        	if (nextDoomed >= 0) {	// kid is lucky, it may replace an 'old' individual
//        		m_Population.replaceIndividualAt(nextDoomed, indy);
//        		nextDoomed = getNextDoomed(m_Population, nextDoomed+1);
//        	} else {	// duel with random one
//	            index   = RNG.randomInt(0, this.m_Population.size()-1);
//	            org     = (AbstractEAIndividual)this.m_Population.get(index);
//	            // if (envHasChanged) this.problem.evaluate(org);
//	            if (indy.isDominatingDebConstraints(org)) {
//	            	this.m_Population.replaceIndividualAt(index, indy);
//	            }
//        	}
//        }
//        children.clear();
//////// this was the original version
//        for (int i = 0; i < this.m_Population.size(); i++) {
//            indy = this.generateNewIndividual(this.m_Population);
//            this.problem.evaluate(indy);
//            this.m_Population.incrFunctionCalls();
//            index   = RNG.randomInt(0, this.m_Population.size()-1);
//            org     = (AbstractEAIndividual)this.m_Population.get(index);
//            if (indy.isDominatingDebConstraints(org)) {
//                this.m_Population.remove(index);
//                this.m_Population.add(index, indy);
//            }
//        }
        m_Problem.evaluatePopulationEnd(m_Population);
        this.m_Population.incrGeneration();
        this.firePropertyChangedEvent(Population.nextGenerationPerformed);
    }

    /**
     * Search for the first individual which is older than the age limit and
     * return its index. If there is no age limit or all individuals are
     * younger, -1 is returned. The start index of the search may be provided to
     * make iterative search efficient.
     *
     * @param pop	Population to search
     * @param startIndex	index to start the search from
     * @return	index of an overaged individual or -1
     */
    protected int getNextDoomed(Population pop, int startIndex) {
        if (maximumAge > 0) {
            for (int i = startIndex; i < pop.size(); i++) {
                if (((AbstractEAIndividual) pop.get(i)).getAge() >= maximumAge) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * This method allows you to add the LectureGUI as listener to the Optimizer
     *
     * @param ea
     */
    @Override
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        if (this.m_Listener == null) {
            this.m_Listener = new Vector<InterfacePopulationChangedEventListener>();
        }
        this.m_Listener.add(ea);
    }

    @Override
    public boolean removePopulationChangedEventListener(
            InterfacePopulationChangedEventListener ea) {
        if (m_Listener != null && m_Listener.removeElement(ea)) {

            return true;
        } else {
            return false;
        }
    }

    /**
     * Something has changed
     *
     * @param name
     */
    protected void firePropertyChangedEvent(String name) {
        if (this.m_Listener != null) {
            for (int i = 0; i < this.m_Listener.size(); i++) {
                this.m_Listener.get(i).registerPopulationStateChanged(this, name);
            }
        }
    }

    /**
     * This method will set the problem that is to be optimized
     *
     * @param problem
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.m_Problem = (AbstractOptimizationProblem) problem;
    }

    @Override
    public InterfaceOptimizationProblem getProblem() {
        return (InterfaceOptimizationProblem) this.m_Problem;
    }

    /**
     * This method will return a string describing all properties of the
     * optimizer and the applied methods.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        result += "Differential Evolution:\n";
        result += "Optimization Problem: ";
        result += this.m_Problem.getStringRepresentationForProblem(this) + "\n";
        result += this.m_Population.getStringRepresentation();
        return result;
    }

    /**
     * This method allows you to set an identifier for the algorithm
     *
     * @param name The identifier
     */
    @Override
    public void setIdentifier(String name) {
        this.m_Identifier = name;
    }

    @Override
    public String getIdentifier() {
        return this.m_Identifier;
    }

    /**
     * ********************************************************************************************************************
     * These are for GUI
     */
    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Differential Evolution using a steady-state population scheme.";
    }

    /**
     * This method will return a naming String
     *
     * @return The name of the algorithm
     */
    @Override
    public String getName() {
        return "DE";
    }

    /**
     * Assuming that all optimizer will store their data in a population we will
     * allow access to this population to query to current state of the
     * optimizer.
     *
     * @return The population of current solutions to a given problem.
     */
    @Override
    public Population getPopulation() {
        return this.m_Population;
    }

    @Override
    public void setPopulation(Population pop) {
        this.m_Population = pop;
    }

    public String populationTipText() {
        return "Edit the properties of the population used.";
    }

    @Override
    public InterfaceSolutionSet getAllSolutions() {
        Population pop = getPopulation();
        return new SolutionSet(pop, pop);
    }

    /**
     * F is a real and constant factor which controls the amplification of the
     * differential variation
     *
     * @param f
     */
    public void setF(double f) {
        this.m_F = f;
    }

    public double getF() {
        return this.m_F;
    }

    public String fTipText() {
        return "F is a real and constant factor which controls the amplification of the differential variation.";
    }

    /**
     * Probability of alteration through DE (something like a discrete uniform
     * crossover is performed here)
     *
     * @param k
     */
    public void setK(double k) {
        if (k < 0) {
            k = 0;
        }
        if (k > 1) {
            k = 1;
        }
        this.m_k = k;
    }

    public double getK() {
        return this.m_k;
    }

    public String kTipText() {
        return "Probability of alteration through DE (a.k.a. CR, similar to discrete uniform crossover).";
    }

    /**
     * Enhance greediness through amplification of the differential vector to
     * the best individual for DE2
     *
     * @param l
     */
    public void setLambda(double l) {
        this.m_Lambda = l;
    }

    public double getLambda() {
        return this.m_Lambda;
    }

    public String lambdaTipText() {
        return "Enhance greediness through amplification of the differential vector to the best individual for DE2.";
    }

    /**
     * In case of trig. mutation DE, the TMO is applied wit probability Mt
     *
     * @param l
     */
    public void setMt(double l) {
        this.m_Mt = l;
        if (this.m_Mt < 0) {
            this.m_Mt = 0;
        }
        if (this.m_Mt > 1) {
            this.m_Mt = 1;
        }
    }

    public double getMt() {
        return this.m_Mt;
    }

    public String mtTipText() {
        return "In case of trigonometric mutation DE, the TMO is applied with probability Mt.";
    }

    /**
     * This method allows you to choose the type of Differential Evolution.
     *
     * @param s The type.
     */
    public void setDEType(DETypeEnum s) {
        this.m_DEType = s;
        // show mt for trig. DE only
        GenericObjectEditor.setShowProperty(this.getClass(), "lambda", s == DETypeEnum.DE2_CurrentToBest);
        GenericObjectEditor.setShowProperty(this.getClass(), "mt", s == DETypeEnum.TrigonometricDE);
    }

    public DETypeEnum getDEType() {
        return this.m_DEType;
    }

    public String dETypeTipText() {
        return "Choose the type of Differential Evolution.";
    }

    /**
     * @return the maximumAge
	 *
     */
    public int getMaximumAge() {
        return maximumAge;
    }

    /**
     * @param maximumAge the maximumAge to set
	 *
     */
    public void setMaximumAge(int maximumAge) {
        this.maximumAge = maximumAge;
    }

    public String maximumAgeTipText() {
        return "The maximum age of individuals, older ones are discarded. Set to -1 (or 0) to deactivate";
    }

    /**
     * Check whether the problem range will be enforced.
     *
     * @return the forceRange
     */
    public boolean isCheckRange() {
        return forceRange;
    }

    /**
     * @param forceRange the forceRange to set
     */
    public void setCheckRange(boolean forceRange) {
        this.forceRange = forceRange;
    }

    public String checkRangeTipText() {
        return "Set whether to enforce the problem range.";
    }

    public boolean isRandomizeFKLambda() {
        return randomizeFKLambda;
    }

    public void setRandomizeFKLambda(boolean randomizeFK) {
        this.randomizeFKLambda = randomizeFK;
    }

    public String randomizeFKLambdaTipText() {
        return "If true, values for k, f, lambda are randomly sampled around +/- 20% of the given values.";
    }

//	public boolean isCyclePop() {
//		return cyclePop;
//	}
//
//	public void setCyclePop(boolean cyclePop) {
//		this.cyclePop = cyclePop;
//	}
//	
//	public String cyclePopTipText() {
//		return "Use all individuals as parents in cyclic sequence instead of randomly.";
//	}
    public boolean isCompareToParent() {
        return compareToParent;
    }

    public void setCompareToParent(boolean compareToParent) {
        this.compareToParent = compareToParent;
    }

    public String compareToParentTipText() {
        return "Compare a challenge individual to its original parent instead of a random one.";
    }

    public boolean isGenerational() {
        return generational;
    }

    public void setGenerational(boolean generational) {
        this.generational = generational;
    }

    public String generationalTipText() {
        return "Switch to generational DE as opposed to standard steady-state DE";
    }

    public boolean isCyclePop() {
        return cyclePop;
    }

    public void setCyclePop(boolean cycle) {
        this.cyclePop = cycle;
    }

    public String cyclePopTipText() {
        return "if true, individuals are used as parents in a cyclic sequence - otherwise randomly ";
    }

    /**
     * @return the maximumAge
	 *
     */
    public boolean isReEvaluate() {
        return reEvaluate;
    }

    /**
     * @param maximumAge the maximumAge to set
	 *
     */
    public void setReEvaluate(boolean reEvaluate) {
        this.reEvaluate = reEvaluate;
    }

    public String reEvaluateTipText() {
        return "Reeavulates individuals which are older than maximum age instead of discarding them";
    }
}