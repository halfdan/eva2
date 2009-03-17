package eva2.server.go.populations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import wsi.ra.math.RNG;
import eva2.server.go.IndividualInterface;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.AbstractEAIndividualComparator;
import eva2.server.go.individuals.GAIndividualBinaryData;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.operators.selection.probability.AbstractSelProb;
import eva2.tools.EVAERROR;
import eva2.tools.Mathematics;
import eva2.tools.Pair;


/** This is a basic implementation for a EA Population.
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert, Marcel Kronfeld
 * @version:  $Revision: 307 $
 *            $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public class Population extends ArrayList implements PopulationInterface, Cloneable, java.io.Serializable {

    protected int           m_Generation    = 0;
    protected int           m_FunctionCalls = 0;
    protected int           m_Size          = 50;
    protected Population    m_Archive       = null;

    transient private ArrayList<InterfacePopulationChangedEventListener> listeners = null;
//    transient protected InterfacePopulationChangedEventListener	m_Listener = null;

    // the evaluation interval at which listeners are notified
    protected int 			notifyEvalInterval	= 0;
    protected HashMap<String, Object>		additionalPopData = null;
    
    public static final String funCallIntervalReached = "FunCallIntervalReached";
    public static final String populationInitialized = "PopulationReinitOccured";
    public static final String nextGenerationPerformed = "NextGenerationPerformed";
    
    boolean useHistory						= false;
    public ArrayList<AbstractEAIndividual>  m_History       = new ArrayList<AbstractEAIndividual>();

    // remember when the last sorted queue was prepared
    private int lastQModCount = -1;
    // a sorted queue (for efficiency)
    transient private ArrayList<AbstractEAIndividual> sortedArr = null;
    private int lastFitCrit = -1;
    
    // remember when the last evaluation was performed
//	private Pair<Integer,Integer> evaluationTimeHashes = null;
    // remember when the last evaluation was performed
//	private int evaluationTimeModCount = -1;

    public Population() {
    }
    
    /**
     * Constructor setting initial capacity and population size to the given
     * integer value.
     * 
     * @param initialCapacity initial capacity and population size of the instance
     */
    public Population(int initialCapacity) {
    	super(initialCapacity);
    	setPopulationSize(initialCapacity);
    }

    public Population(Population population) {
        setSameParams(population);
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i) != null)
                this.add((((AbstractEAIndividual)population.get(i))).clone());
        }
        copyHistAndArchive(population);
    }
    
    public void copyHistAndArchive(Population population) {
    	if (population.m_Archive != null) this.m_Archive = (Population)population.m_Archive.clone();
    	if (population.m_History != null) this.m_History = (ArrayList)population.m_History.clone();
    }
    
    /**
     * Takes over all scalar parameters of the given population.
     * @param population
     */
    public void setSameParams(Population population) {
        this.m_Generation       = population.m_Generation;
        this.m_FunctionCalls    = population.m_FunctionCalls;
        this.m_Size             = population.m_Size;
        this.useHistory 		= population.useHistory;
        this.notifyEvalInterval = population.notifyEvalInterval;
//        this.m_Listener			= population.m_Listener;
        if (population.listeners != null) this.listeners			= (ArrayList<InterfacePopulationChangedEventListener>)population.listeners.clone();
        else listeners = null;
        if (population.additionalPopData != null) {
        	additionalPopData = new HashMap<String, Object>();
        	Set<String> keys = additionalPopData.keySet();
        	for (String key : keys) {
        		additionalPopData.put(key, population.additionalPopData.get(key));
        	}
        }
    }

    public void putData(String key, Object value) {
    	if (additionalPopData == null) additionalPopData = new HashMap<String, Object>();
    	additionalPopData.put(key, value);
    }
    
    public Object getData(String key) {
    	if (additionalPopData == null) return null;
    	else return additionalPopData.get(key);
    }
    
    public boolean hasData(String key) {
    	if (additionalPopData != null) return (additionalPopData.get(key)!=null);
    	else return false;
    }
    
    public Object clone() {
        return (Object) new Population(this);
    }
    
    /**
     * Clone the population without cloning every individual. This produces an empty population
     * which can be used to fill with the next generation by an EA and is implemented for efficiency.
     * 
     * @return an empty population with equal members but not containing any individuals
     */
    public Population cloneWithoutInds() {
    // these two basically clone without cloning every individual
    	Population res = new Population();
    	res.setSameParams(this);
    	res.copyHistAndArchive(this);
    	if (additionalPopData!=null) res.additionalPopData = (HashMap<String, Object>)(additionalPopData.clone());
    	return res;
    }


    /** This method inits the state of the population AFTER the individuals
     * have been inited by a problem
     */
    public void init() {
        this.m_History = new ArrayList();
        this.m_Generation       = 0;
        this.m_FunctionCalls    = 0;
//    	evaluationTimeHashes = null;
//    	evaluationTimeModCount = -1;
        if (this.m_Archive != null) {
            this.m_Archive.clear();
            this.m_Archive.init();
        }
        firePropertyChangedEvent(Population.populationInitialized);
    }

    /** This method inits the population. Function and generation counters
     * are reset and m_Size default Individuals are created and initialized by
     * the GAIndividual default init() method.
      */
    public void defaultInitPopulation() {
        GAIndividualBinaryData tmpIndy;

        this.m_Generation       = 0;
        this.m_FunctionCalls    = 0;
        this.m_Archive          = null;
        this.clear();
        for (int i = 0; i < this.m_Size; i++) {
            tmpIndy = new GAIndividualBinaryData();
            tmpIndy.defaultInit();
            super.add(tmpIndy);
        }
    }

    /**
     * Activate or deactivate the history tracking, which stores the best individual in every
     * generation in the incrGeneration() method.
     * 
     * @param useHist
     */
    public void setUseHistory(boolean useHist) {
    	useHistory = useHist;
    }
    
    /** This method will allow you to increment the current number of function calls.
     */
    public void incrFunctionCalls() {
        this.m_FunctionCalls++;
        if (doEvalNotify()) {
//    		System.out.println("checking funcall event...");
        	if ((m_FunctionCalls % notifyEvalInterval) == 0) firePropertyChangedEvent(funCallIntervalReached);
        }
    }
    /** 
     * This method will allow you to increment the current number of function calls by a number > 1.
     * Notice that it might slightly disturb notification if a notifyEvalInterval is set. 
     * 
     * @param d     The number of function calls to increment.
     */
    public void incrFunctionCallsBy(int d) {
    	if (doEvalNotify()) {
//    		System.out.println("checking funcall event...");
    		int nextStep; // next interval boundary
    		while ((nextStep = calcNextBoundary()) <= (m_FunctionCalls+d)) {
    			// 	the notify interval will be stepped over or hit
    			int toHit = (nextStep - m_FunctionCalls);
    			this.m_FunctionCalls += toHit; // little cheat, notify may be after some more evals
    			firePropertyChangedEvent(funCallIntervalReached);
    			d = d-toHit;
//    			this.m_FunctionCalls += (d-toHit);
    		}
    		if (d>0) this.m_FunctionCalls += d; // add up the rest
    	} else this.m_FunctionCalls += d;
    }

	private int calcNextBoundary() {
		return ((m_FunctionCalls/notifyEvalInterval)+1) * notifyEvalInterval;
	}
    
    /** Something has changed
     */
    protected void firePropertyChangedEvent(String name) {
        if (listeners != null) {
        	for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
				InterfacePopulationChangedEventListener listener = (InterfacePopulationChangedEventListener) iterator.next();
				if (listener!=null) listener.registerPopulationStateChanged(this, name);
			}
        }
    }
    
    private boolean doEvalNotify() {
    	return ((listeners != null) && (listeners.size() > 0) && (notifyEvalInterval > 0));
    }
    
    /** This method return the current number of function calls performed.
     * @return The number of function calls performed.
     */
    public int getFunctionCalls() {
        return this.m_FunctionCalls;
    }

    /** This method set the current number of function calls performed.
     * Use with care
     * @param d     The new number of functioncalls.
     */
    public void SetFunctionCalls(int d) {
        this.m_FunctionCalls = d;
    }
    
    /**
     * To initialize (or invalidate) all current fitness values, this method sets them
     * to the given array.
     * 
     * @param f
     */
    public void setAllFitnessValues(double[] f) {
    	AbstractEAIndividual indy;
    	for (int i=0; i<size(); i++) {
    		indy = getEAIndividual(i);
    		indy.SetFitness(f.clone());
    	}
    }

    /** This method allows you to increment the current number of generations.
     * This will be the trigger for the Population, that has moved from t to t+1.
     * Here overaged Individuals can be removed. The best of class can be identified.
     * Stagnation measured etc. pp.
     */
    public void incrGeneration() {
        if (useHistory && (this.size() >= 1)) this.m_History.add(this.getBestEAIndividual());
        for (int i=0; i<size(); i++) ((AbstractEAIndividual)get(i)).incrAge(); 
        this.m_Generation++;
        firePropertyChangedEvent(nextGenerationPerformed);
    }

    /** This method returns the current generation.
     * @return The current generation index.
     */
    public int getGeneration() {
        return this.m_Generation;
    }
    
    /** This method sets the generation.
     * @param gen	the value to set as new generation index
     */
    public void setGenerationTo(int gen) {
        this.m_Generation = gen;
    }
    
    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
    	if (ea != null) {
    		if (listeners == null) listeners = new ArrayList<InterfacePopulationChangedEventListener>(3);
    		if (!listeners.contains(ea)) {
    			listeners.add(ea);
    		}
    	}
    }
    
    public void removePopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
    	if (listeners != null) {
    		listeners.remove(ea);
    	}
    }
    
    /** This method allows you to add a complete population to the current population.
     * Note: After this operation the target population size may be exceeded.
     * @param pop   The population that is to be added.
     */
    public void addPopulation(Population pop) {
        if (pop == null) return;
        for (int i = 0; i < pop.size(); i++) {
        	AbstractEAIndividual indy = (AbstractEAIndividual)pop.get(i);
        	if (indy != null) {
            	this.add(indy);
        	}
        }
    }
    
    /**
     * Resets the fitnes to the maximum possible value for the given individual.
     *
     * @param indy	an individual whose fitness will be reset
     */
    public void resetFitness(IndividualInterface indy) {
    	double[] tmpFit = indy.getFitness();
    	java.util.Arrays.fill(tmpFit, Double.MAX_VALUE);
    	indy.SetFitness(tmpFit);
    }
	
	/**
	 * Return all individuals dominating an individual given by index.
	 * 
	 * @param index 
	 * @return all individuals dominating a given individual
	 */
	public Population getDominatingSet(int index) {
		Population domSet = new Population();
		AbstractEAIndividual indy;
        for (int i = 0; i < super.size(); i++) {
        	if (i != index) {
        		indy = getEAIndividual(i);
        		if (indy.isDominatingDebConstraints(getEAIndividual(index))) domSet.add(indy);
            }
        }
        return domSet;
	}
	
	/**
	 * Return all individuals dominating a given individual.
	 * 
	 * @param indy
	 * @return all individuals dominating a given individual
	 */
	public Population getDominatingSet(AbstractEAIndividual indy) {
		Population domSet = new Population();
		AbstractEAIndividual tmpIndy;
        for (int i = 0; i < super.size(); i++) {
    		tmpIndy = getEAIndividual(i);
    		if (tmpIndy.isDominatingDebConstraints(indy)) domSet.add(tmpIndy);
        }
        return domSet;
	}

	
	/**
	 * Compare two fitness vectors. If bChooseBetter is true, the function delivers the predicate
	 * "first is better than second" using the fitness component indicated by fitIndex or a dominance
	 * criterion if fitIndex < 0.
	 * 
	 * @param bChooseBetter
	 * @param fit1
	 * @param fit2
	 * @param fitIndex
	 * @return
	 */
	private boolean compareFit(boolean bChooseBetter, double[] fit1, double[] fit2, int fitIndex) {
		if (fitIndex < 0) { // multiobjective case
			if (bChooseBetter) return AbstractEAIndividual.isDominatingFitness(fit1, fit2);
			else return AbstractEAIndividual.isDominatingFitness(fit2, fit1);
		} else {
			if (bChooseBetter) return fit1[fitIndex]<fit2[fitIndex];
			else return fit1[fitIndex]>fit2[fitIndex];
		}
	}
	
	/** 
	 * This method will return the index of the current best individual from the
     * population. If the population is empty, -1 is returned.
     * 
     * @see getIndexOfBestOrWorstIndividual()
     * @return The index of the best individual.
     */
    public int getIndexOfBestIndividual() {
    	if (size()<1) return -1;
    	return getIndexOfBestOrWorstIndividual(true, true, -1);
    }
	
	/** 
	 * This method will return the index of the current best individual from the
     * population.
     * 
     * @see getIndexOfBestOrWorstIndividual()
     * @return The index of the best individual.
     */
    public int getIndexOfWorstIndividual() {
    	return getIndexOfBestOrWorstIndividual(false, false, -1);
    }
    
	/** 
	 * This method will return the index of the current best individual from the
     * population in the given fitness component (or using dominance when fitIndex < 0).
     * If the population is empty, -1 is returned.
     * 
     * @see getIndexOfBestOrWorstIndividual()
     * @return The index of the best individual.
     */
    public int getIndexOfBestIndividual(int fitIndex) {
    	if (size()<1) return -1;
    	return getIndexOfBestOrWorstIndividual(true, true, fitIndex);
    }
	
	/** 
	 * This method will return the index of the current best individual from the
     * population in the given fitness component (or using dominance when fitIndex < 0).
     * 
     * @see getIndexOfBestOrWorstIndividual()
     * @return The index of the best individual.
     */
    public int getIndexOfWorstIndividual(int fitIndex) {
    	return getIndexOfBestOrWorstIndividual(false, false, fitIndex);
    }
    
	/** 
	 * This method will return the index of the current best (worst) individual from the
     * population. If indicated, only those are regarded which do not violate the constraints.
     * If all violate the constraints, the smallest (largest) violation is selected.
     * Comparisons are done multicriterial, but note that for incomparable sets (pareto fronts)
     * this selection will not be fair (always the lowest index of incomparable sets will be returned).
     * 
     * @param bBest if true, smallest fitness (regarded best) index is returned, else the highest one
     * @param indicate whether constraints should be regarded
     * @return The index of the best (worst) individual.
     */
    public int getIndexOfBestOrWorstIndividual(boolean bBest, boolean checkConstraints, int fitIndex) {
        int     result          = -1;
        double[]  curSelFitness = null;
        boolean allViolate = true;

        for (int i = 0; i < super.size(); i++) {
            if (!checkConstraints || !(getEAIndividual(i).violatesConstraint())) {
            	allViolate = false;
            	if ((result<0) || (compareFit(bBest, getEAIndividual(i).getFitness(), curSelFitness, fitIndex))) {
            		// fit i is better than remembered
            		result          = i;
            		curSelFitness  = getEAIndividual(i).getFitness(); // remember fit i
            	}
            }
        }
        if (result < 0) {
        	if (checkConstraints && allViolate) {
        		// darn all seem to violate the constraint
        		// so lets search for the guy who is close to feasible
        		
        		// to avoid problems with NaN or infinite fitness value, preselect an ind.
        		result          = 0;
        		double minViol = getEAIndividual(0).getConstraintViolation();
        		for (int i = 1; i < super.size(); i++) {
        			if ((bBest && getEAIndividual(i).getConstraintViolation() < minViol) ||
            			(!bBest && (getEAIndividual(i).getConstraintViolation() > minViol))) {
        				result          = i;
        				minViol  = ((AbstractEAIndividual)super.get(i)).getConstraintViolation();
        			}
        		}
        		System.err.println("Population reports: All individuals violate the constraints, choosing smallest constraint violation.");
        	} else {
        		// not all violate, maybe all are NaN!
        		// so just select a random one
        		System.err.println("Population reports: All individuals seem to have NaN or infinite fitness!");
        		result = RNG.randomInt(size());
        	}
        }
        return result;
    }

    /** 
	 * This method returns the current best individual from the population.
	 * If the population is empty, null is returned.
	 *
     * @return The best individual
     */
    public AbstractEAIndividual getBestEAIndividual() {
    	return getBestEAIndividual(-1);
    }
    
    /** 
     * This method returns the current best individual from the population 
     * by a given fitness component.
     * If the population is empty, null is returned.
     *
     * @param fitIndex the fitness criterion index or -1
     * @return The best individual
     */
    public AbstractEAIndividual getBestEAIndividual(int fitIndex) {
    	if (size()<1) return null;
    	int best = this.getIndexOfBestIndividual(fitIndex);
    	if (best == -1) {
    		System.err.println("This shouldnt happen!");
    		return null;
    	} else {
    		AbstractEAIndividual result = (AbstractEAIndividual)this.get(best);
    		if (result == null) System.err.println("Serious Problem! Population Size: " + this.size());
    		return result;
    	}
    }

    /** 
     * This method returns the n current best individuals from the population, where
     * the sorting criterion is delivered by an AbstractEAIndividualComparator.
     * There are less than n individuals returned if the population is smaller than n.
     * If n is <= 0, then all individuals are returned and effectively just sorted
     * by fitness.
     * This does not check constraints!
     * 
     * @param n	number of individuals to look out for
     * @return The m best individuals, where m <= n
     * 
     */
    public Population getBestNIndividuals(int n) {
    	return getSortedNIndividuals(n, true);
    }
    
    /** 
     * This method returns a clone of the population instance with sorted individuals, where
     * the sorting criterion is delivered by an AbstractEAIndividualComparator.
     * @see #getSortedNIndividuals(int, boolean, Population)
     * 
     * @return a clone of the population instance with sorted individuals, best fitness first
     */
    public Population getSortedBestFirst() {
    	Population result = this.cloneWithoutInds();
    	getSortedNIndividuals(size(), true, result);
    	result.setPopulationSize(result.size());
    	return result;
    }
    
    /** 
     * This method returns the n current best individuals from the population, where
     * the sorting criterion is delivered by an AbstractEAIndividualComparator.
     * @see getSortedNIndividuals(int n, boolean bBestOrWorst, Population res)
     * 
     * @param n	number of individuals to look out for
     * @param bBestOrWorst if true, the best n are returned, else the worst n individuals
     * @return The m sorted best or worst individuals, where m <= n
     * 
     */
    public Population getSortedNIndividuals(int n, boolean bBestOrWorst) {
    	Population result = new Population((n > 0) ? n : this.size());
    	getSortedNIndividuals(n, bBestOrWorst, result);
    	return result;
    }
    
    /** 
     * This method returns the n current best individuals from the population, where
     * the sorting criterion is delivered by an AbstractEAIndividualComparator.
     * There are less than n individuals returned if the population is smaller than n.
     * This does not check constraints!
     * 
     * @param n	number of individuals to look out for
     * @param bBestOrWorst if true, the best n are returned, else the worst n individuals
     * @param res	sorted result population, will be cleared
     * @return The m sorted best or worst individuals, where m <= n
     * 
     */
    public void getSortedNIndividuals(int n, boolean bBestOrWorst, Population res) {
    	if ((n < 0) || (n>super.size())) {
    		// this may happen, treat it gracefully
    		//System.err.println("invalid request to getSortedNIndividuals: n="+n + ", size is " + super.size());
    		n = super.size();
    	}
    	int skip = 0;
    	if (!bBestOrWorst) skip = super.size()-n;
    	
    	ArrayList<AbstractEAIndividual> sorted = getSorted();
    	res.clear();
        for (int i = skip; i < skip+n; i++) {
        	res.add(sorted.get(i));
        }
        res.setPopulationSize(res.size());
    }
    
    /**
     * Set a fitness criterion for sorting procedures. This also affects getBest
     * @param fitIndex
     */
    public void setSortingFitnessCriterion(int fitIndex) {
    	getSorted(fitIndex);
    }
    
    /**
     * Reuses the last fitness criterion. Avoid having to sort again in several calls without modifications in between.
     * The returned array should not be modified!
     * 
     * @return
     */
    protected ArrayList<AbstractEAIndividual> getSorted() {
    	return getSorted(lastFitCrit);
    }

    /**
     * Avoids having to sort again in several calls without modifications in between.
     * The returned array should not be modified!
     * 
     * @param fitIndex the fitness criterion to be used or -1 for pareto dominance
     * @return
     */
    protected ArrayList<AbstractEAIndividual> getSorted(int fitIndex) {
    	if ((fitIndex != lastFitCrit) || (sortedArr == null) || (super.modCount != lastQModCount)) {
    		lastFitCrit=fitIndex; // TODO check if this works right?
    		PriorityQueue<AbstractEAIndividual> sQueue = new PriorityQueue<AbstractEAIndividual>(super.size(), new AbstractEAIndividualComparator(fitIndex));
    		for (int i = 0; i < super.size(); i++) {
    			AbstractEAIndividual indy = getEAIndividual(i);
    			if (indy != null) sQueue.add(indy);
    		}
    		lastQModCount = super.modCount;
    		if (sortedArr==null) sortedArr = new ArrayList<AbstractEAIndividual>(this.size());
    		else sortedArr.clear();
    		AbstractEAIndividual indy;
    		while ((indy=sQueue.poll())!=null) sortedArr.add(indy);
    	}
    	return sortedArr;
    }
    
    /** This method returns n random best individuals from the population.
     * 
     * @param n	number of individuals to look out for
     * @return The n best individuals
     * 
     */
    public List<AbstractEAIndividual> getRandNIndividuals(int n) {
    	return getRandNIndividualsExcept(n, new Population());	
    }
    
    /** This method returns the n current best individuals from the population in an object array.
     * 
     * @param n	number of individuals to look out for
     * @return The n best individuals
     * 
     */
    public Population getRandNIndividualsExcept(int n, Population exclude) {
    	return moveNInds(n, filter(exclude), new Population());
    }

    /**
     * Moves n random individuals from src Population to dst Population and returns dst Population.
     * 
     * @param n
     * @param from
     * @param to
     * @return
     */
    public static Population moveNInds(int n, Population src, Population dst) {
    	if ((n == 0) || (src.size() == 0))  return dst;
    	else { // Ingenious superior Scheme tail recursive style!
    		moveRandIndFromTo(src, dst);
    		return moveNInds(n-1, src, dst);
    	}
	}
    
    /**
     * Move one random individual from src to dst population.
     * @param from
     * @param to
     */
    public static void moveRandIndFromTo(Population src, Population dst) {
    	int k = RNG.randomInt(src.size());
    	dst.add(src.remove(k));
    }
    
    /**
     * Returns a subset of this population which does not contain the individuals
     * in the given exclude list as shallow copies.
     * @param exclude
     * @return
     */
    public Population filter(Population exclude) {
    	if (exclude.size() == 0) return this;
    	Population pop = new Population();
    	for (Object o : this) {
			if (!exclude.contains(o)) pop.add(o);
		}
    	return pop;
    }
    
    /** 
     * This method returns the currently worst individual from the population
     * @return The best individual
     */
    public AbstractEAIndividual getWorstEAIndividual() {
    	return getWorstEAIndividual(-1);
    }
    
	public AbstractEAIndividual getWorstEAIndividual(int fitIndex) {
    	return getEAIndividual(getIndexOfWorstIndividual(fitIndex));
    }

    /** 
     * This method will remove N individuals from the population
     * Note: the current strategy will be remove N individuals
     * at random but later a special heuristic could be introduced.
     * @param n     The number of individuals for be removed
     */
    public void removeNIndividuals(int n) {
        for (int i = 0; i < n; i++) {
            this.remove(RNG.randomInt(0, this.size()-1));
        }
    }

    /** This method will remove double instances from the population.
     * This method relies on the implementation of the equals method
     * in the individuals.
     */
    public void removeDoubleInstances() {
        for (int i = 0; i < this.size(); i++) {
            for (int j = i+1; j < this.size(); j++) {
                if (((AbstractEAIndividual)this.get(i)).equals(this.get(j))) {
                    this.remove(j);
                    j--;
                }
            }
        }
    }

    /** This method will remove double instances from the population.
     * This method relies on the implementation of the equals method
     * in the individuals.
     */
    public void removeDoubleInstancesUsingFitness() {
        for (int i = 0; i < this.size(); i++) {
            for (int j = i+1; j < this.size(); j++) {
                if (((AbstractEAIndividual)this.get(i)).equalFitness((AbstractEAIndividual)this.get(j))) {
                    this.remove(j);
                    j--;
                }
            }
        }
    }

    /** This method returns all marked individuals
     * @return a population of marked individuals
     */
    public Population getMarkedIndividuals() {
        Population result = new Population();
        for (int i = 0; i < this.size(); i++) {
            if (((AbstractEAIndividual)this.get(i)).isMarked()) {
                result.add(this.get(i));
            }
        }
        return result;
    }

    /** This method will unmark all individual in the population
     */
    public void unmarkAllIndividuals() {
        for (int i = 0; i < this.size(); i++) {
            ((AbstractEAIndividual)this.get(i)).unmark();
        }
    }

    /** This method returns problem specific data
     * @return double[]
     */
    public double[] getSpecificData() {
        return null;
    }

    /** This method returns identifiers for the
     * specific data
     * Note: "Pareto-Front" is reserved for mulit-crit. Problems
     * @return String[]
     */
    public String[] getSpecificDataNames() {
        return null;
    }

    /** This method allows you to access the archive
     * @return The archive
     */
    public Population getArchive() {
        return this.m_Archive;
    }

    /** This method allows you to set the current archive
     * @param a     The new archive
     */
    public void SetArchive(Population a) {
        this.m_Archive = a;
    }

    /** This method will return a string description of the GAIndividal
     * noteably the Genotype.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        StringBuilder strB = new StringBuilder(200);
        strB.append("Population:\nPopulation size: ");
        strB.append("Population size: ");
        strB.append(this.size());
        strB.append("\nFunction calls : ");
        strB.append(this.m_FunctionCalls);
        strB.append("\nGenerations    : ");
        strB.append(this.m_Generation); 
        strB.append("\n");
        for (int i = 0; i < this.size(); i++) {
            strB.append(((AbstractEAIndividual)this.get(i)).getStringRepresentation());
            strB.append("\n");
        }
        return strB.toString();
    }

    /**
     * Return a list of individual IDs from the population.
     * @return
     */
    public Long[] getIDList() {
    	Long[] idList = new Long[size()];
    	for (int i=0; i<idList.length; i++) {
			idList[i]=getEAIndividual(i).getIndyID();
		}
    	return idList;
    }
    
//    public Long[][] getParentalIDList() {
//    	Long[][] idList = new Long[size()][];
//    	for (int i=0; i<idList.length; i++) {
//			idList[i]=getEAIndividual(i).getHeritage();
//		}
//    	return idList;   	
//    }
    
    /**
     * Get a string containing representations of all individuals contained.
     */
    public String getIndyList() {
    	StringBuffer sb = new StringBuffer();
    	for (int i=0; i<size(); i++) {
    		sb.append(AbstractEAIndividual.getDefaultStringRepresentation(getEAIndividual(i)));
    		sb.append(", generation: ");
    		sb.append(getGeneration());
    		sb.append("\n");
    	}
    	return sb.toString();
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "A population stores the individuals of a generation.";
    }

    /** 
     * This method allows you to set the population size. Be aware that this will not 
     * directly alter the number of individuals stored. The actual size will be
     * adapted on a reinitialization, for example.
     * 
     * @param size
     */
    public void setPopulationSize(int size) {
        this.m_Size = size;
    }
    
    /**
     * Convenience method.
     * 
     * @param size
     * @return
     */
    public Population setPopSize(int size) {
        this.m_Size = size;
        return this;
    }
    
    public int getPopulationSize() {
        return this.m_Size;
    }
    public String populationSizeTipText() {
        return "The population size.";
    }   
    public AbstractEAIndividual getEAIndividual(int i) {
        return (AbstractEAIndividual)this.get(i);
    }
/**********************************************************************************************************************
 * Implementing the PopulationInterface
 */
    public Object getClone() {
        return this.clone();
    }

    public IndividualInterface getIndividual(int i) {
        return (IndividualInterface)this.get(i);
    }
    
    public boolean add(IndividualInterface o) {
    	return addIndividual((IndividualInterface)o);
    }

    /**
	 * ArrayList does not increase the modCount in set. Why???
     */
    public Object set(int index, Object element) {
    	Object prev = super.set(index, element);
    	modCount++;
    	return prev;
    }
    
    public boolean addIndividual(IndividualInterface ind) {
        super.add(ind);
        return true;
    }
    
    /**
     * Remove an individual from the population efficiently by switching it with last position
     * and removing that.
     *
     * @param index	individual index to be removed
     */
    public void removeIndexSwitched(int index) {
    	int lastIndex = size()-1;
    	if (index < lastIndex) set(index, get(lastIndex));
    	remove(lastIndex);    	
    }
    
    /**
     * Replaces an individual at a certain index with the given one. The old one is returned. 
     *
     * @param index
     * @param ind
     * @return
     */
    public IndividualInterface replaceIndividualAt(int index, IndividualInterface ind) {
    	return (IndividualInterface)set(index, ind);
    }
    
    public void removeIndividual (IndividualInterface ind) {
        for (int i = 0; i < this.size(); i++) {
            if (ind.equals(this.get(i))) {
                this.remove(i);
                return;
            }
        }
    }

    public IndividualInterface getBestIndividual() {
        return (IndividualInterface)this.getBestEAIndividual();
    }
    public IndividualInterface getWorstIndividual() {
        return (IndividualInterface)this.getWorstEAIndividual();
    }

    public double[] getBestFitness() {
        return this.getBestEAIndividual().getFitness();
    }
    public double[] getWorstFitness() {
        return this.getWorstEAIndividual().getFitness();
    }

    public double[] getMeanFitness() {
        double[] result, tmp;
        tmp = this.getBestFitness();
        result = new double[tmp.length];
        for (int i = 0; i <this.size(); i++) {
            tmp = ((AbstractEAIndividual)this.get(i)).getFitness();
            for (int j = 0; j < result.length; j++) {
                result[j] += tmp[j];
            }
        }
        for (int j = 0; j < result.length; j++) {
            result[j] = result[j]/((double)this.size());
        }
        return result;
    }
    
    /**
     * Returns the average, minimal and maximal phenotypic individual distance as diversity measure for the population.
     * Distances are thus scaled by the problem range.
     * 
     * @return the average, minimal and maximal mean distance of individuals in an array of three
     */
    public double[] getPopulationMeasures() {
    	return getPopulationMeasures(new PhenotypeMetric());
    }
    
    /**
     * Returns the average, minimal and maximal individual distance as diversity measure for the population.
     * If the given metric argument is null, the euclidian distance of individual positions is used, which
     * presumes that {@link AbstractEAIndividual.getDoublePosition(indy)} returns a valid double position for the
     * individuals of the population.
     * This is of course rather expensive computationally. 
     *
     * @return the average, minimal and maximal mean distance of individuals in an array of three
     */
    public double[] getPopulationMeasures(InterfaceDistanceMetric metric) {
    	double d;
    	double[] res = new double[3];
    	
    	double meanDist = 0.;
    	double maxDist = Double.MIN_VALUE;
    	double minDist = Double.MAX_VALUE;
    	
        for (int i = 0; i < this.size(); i++) {
        	for (int j = i+1; j < this.size(); j++) {
        		if (metric == null) d = PhenotypeMetric.euclidianDistance(AbstractEAIndividual.getDoublePosition(getEAIndividual(i)), 
                		AbstractEAIndividual.getDoublePosition(getEAIndividual(j)));
        		else d = metric.distance((AbstractEAIndividual)this.get(i), (AbstractEAIndividual)this.get(j));
                meanDist += d;
                if (d < minDist) minDist = d;
                if (d > maxDist) maxDist = d;
            }
        }
        res[1] = minDist;
        res[2] = maxDist;
        if (this.size() > 1) res[0] = meanDist / (this.size() * (this.size()-1) / 2);
        else { // only one indy?
        	res[1]=0;
        	res[2]=0;
        }
        return res;
    }
    
    /**
     * Returns the average, minimal and maximal individual fitness and std dev. for the population in the given criterion.
     *
     * @param fitCrit fitness dimension to be used
     * @return the average, minimal, maximal and std dev. of fitness of individuals in an array
     */
    public double[] getFitnessMeasures(int fitCrit) {
    	double d;
    	double[] res = new double[4];
    	
    	res[0] = 0.;
    	res[1] = Double.MAX_VALUE;
    	res[2] = Double.MIN_VALUE;
    	res[3] = 0;
    	
        for (int i = 0; i < this.size(); i++) {
        		d = this.getEAIndividual(i).getFitness(fitCrit);
        		res[0] += d;
                if (d < res[1]) res[1] = d;
                if (d > res[2]) res[2] = d;
        }
        
        if (size()==0) {
        	res[0]=res[1]=res[2]=res[3]=Double.NaN;
        } else {
        	// calc standard deviation
        	res[0] = res[0] / this.size();
        	for (int i=0; i< this.size(); i++) {
        		d = res[0]-this.getEAIndividual(i).getFitness(fitCrit);
        		res[3]+=d*d;
        	}
        	res[3] /= this.size();
        	res[3] = Math.sqrt(res[3]);
        }
        
        
        return res;
    }
    
	/**
	 * Calculate the average position of the population.
	 * 
	 * @return the average position of the population
	 */
	public double[] getCenter() {
		if (size()==0) EVAERROR.errorMsgOnce("Invalid pop size in DistractingPopulation:getCenter!");
		double[] centerPos = AbstractEAIndividual.getDoublePosition(getEAIndividual(0));
		for (int i=1; i<size(); i++) {
			Mathematics.vvAdd(centerPos, AbstractEAIndividual.getDoublePosition(getEAIndividual(i)), centerPos);
		}
		Mathematics.svDiv(size(), centerPos, centerPos);
		return centerPos;
	}
	
	/**
	 * Calculate the weighted center position of the population. Weights must add up to one!
	 * 
	 * @return the average position of the population
	 */
	public double[] getCenterWeighted(double[] weights) {
		if (size()==0 || (weights.length > size())) EVAERROR.errorMsgOnce("Invalid pop size in DistractingPopulation:getCenterWeighted!");
		double[] centerPos = AbstractEAIndividual.getDoublePosition(getEAIndividual(0));
		Mathematics.svMult(weights[0], centerPos, centerPos);
		for (int i=1; i<weights.length; i++) {
			Mathematics.svvAddScaled(weights[i], AbstractEAIndividual.getDoublePosition(getEAIndividual(i)), centerPos, centerPos);
		}
		return centerPos;
	}

	/**
	 * Return the population center weighted by fitness, using the same scaling as provided
	 * by a SelectionProbability instance.
	 * This only works for those individuals that have a position representation, meaning that
	 * AbstractEAIndidivual.getDoublePosition(individual) returns a valid position.
	 * If they dont, null is returned.
	 *
	 * @see AbstractEAIndidivual.getDoublePosition(individual)
	 * @param criterion
	 * @return
	 */
	public double[] getCenterWeighted(AbstractSelProb selProb, int criterion, boolean obeyConst) {
		selProb.computeSelectionProbability(this, "Fitness", obeyConst);
		double[] mean = AbstractEAIndividual.getDoublePosition(getEAIndividual(0)).clone();
		
		if (mean != null) {
			Arrays.fill(mean, 0.);
			AbstractEAIndividual indy = null;
			for (int i=0; i<size(); i++) {
				indy = getEAIndividual(i);
				double[] pos = AbstractEAIndividual.getDoublePosition(indy);
				Mathematics.svvAddScaled(indy.getSelectionProbability(criterion), pos, mean, mean);
			}
		}
		return mean;
	}
	
	/**
	 * Search for the closest individual which is not equal to the given individual. Return
	 * its index or -1 if none could be found.
	 * 
	 * @param indy
	 * @return closest neighbor (euclidian measure) of the given individual in the given population 
	 */
	public int getNeighborIndex(AbstractEAIndividual indy) {
		// get the neighbor...
		int index = -1;
		double mindist = Double.POSITIVE_INFINITY;

		for (int i = 0; i < size(); ++i){ 
			AbstractEAIndividual currentindy = getEAIndividual(i);
			if (!indy.equals(currentindy)){ // dont compare particle to itself or a copy of itself
				double dist = PhenotypeMetric.euclidianDistance(AbstractEAIndividual.getDoublePosition(indy),
						AbstractEAIndividual.getDoublePosition(currentindy));
				if (dist  < mindist){ 
					mindist = dist;
					index = i;
				}
			}
		}
		if (index == -1){
			System.err.println("Pop too small or all individuals in population are equal !?");
			return -1;
		}
		return index;
	}
	
	/**
	 * Calculate the average of the distance of each individual to its closest neighbor in the population.
	 * The boolean parameter switches between range-normalized and simple euclidian distance. If calcVariance
	 * is true, the variance is calculated and returned as second entry
	 * 
	 * @param normalizedPhenoMetric
	 * @return a double array containing the average (or average and variance) of the distance of each individual to its closest neighbor
	 */
	public double[] getAvgDistToClosestNeighbor(boolean normalizedPhenoMetric, boolean calcVariance){
		PhenotypeMetric metric = new PhenotypeMetric();
		ArrayList<Double> distances = null;
		if (calcVariance) distances = new ArrayList<Double>(size());
		double sum = 0;
		double d=0;
		for (int i = 0; i < size(); ++i){
			AbstractEAIndividual neighbor, indy = getEAIndividual(i);
			int neighborIndex = getNeighborIndex(indy);
			if (neighborIndex >= 0) neighbor = getEAIndividual(neighborIndex);
			else return null;
			if (normalizedPhenoMetric){
				d = metric.distance(indy, neighbor);
			} else { 
				d = PhenotypeMetric.euclidianDistance(AbstractEAIndividual.getDoublePosition(indy),
						AbstractEAIndividual.getDoublePosition(neighbor));
			}
			if (calcVariance) distances.add(d);
			sum += d;
		}
		double avg = sum/(double)size();
		double[] res;
		if (calcVariance) {
			res = new double[2];
			double var = 0;
			for (int i=0; i<distances.size(); i++) {
				var += Math.pow(distances.get(i)-avg, 2);
			}
			res[1]=var;
		}
		else res = new double[1];
		res[0] = avg;

		return res;
	}
	
	/**
	 * Fire an event every n function calls, the event sends the public String funCallIntervalReached.
	 * Be aware that if this interval is smaller than the population size, it may happen that a notification
	 * is fired before all individuals have been evaluated once, meaning that a false zero fitness
	 * appears at the beginning of the optimization.
	 * 
	 * @param notifyEvalInterval the notifyEvalInterval to set
	 */
	public void setNotifyEvalInterval(int notifyEvalInterval) {
		this.notifyEvalInterval = notifyEvalInterval;
	}

	/**
	 * Fit the population to its targeted population size. If it contains too many
	 * individuals, the last ones are removed. If it contains too few individuals,
	 * the first ones are cloned in a cycle.
	 * If the size matches, nothing happens. If there is no individual already contained,
	 * this method cannot grow, of course.
	 */
	public void fitToSize() {
		if (size() != getPopulationSize()) {
			while (size() > getPopulationSize()) remove(size()-1);
			if (size() < getPopulationSize()) {
				if (size() == 0) System.err.println("Cannot grow empty population!");
				else {
					int origSize=size();
					int k=0;
					while (size()< getPopulationSize()) {
						addIndividual((AbstractEAIndividual)getEAIndividual(k%origSize).clone());
					}
				}
			}
		}
	}

	/**
	 * Calculate the fitness sum over all individuals for one criterion.
	 * 
	 * @param criterion
	 * @return the fitness sum over all individuals for one criterio
	 */
	public double getFitSum(int criterion) {
		double fSum = 0.;
		for (int i=0; i<size(); i++) {
			fSum += getEAIndividual(i).getFitness(criterion);
		}
		return fSum;
	}

	/**
	 * Set the desired population size parameter to the actual current size.
	 * 
	 */
	public void synchSize() {
		setPopulationSize(size());
	}

//	/**
//	 * Check whether the population at the current state has been marked as
//	 * evaluated. This allows to avoid double evaluations. 
//	 * 
//	 * @return true if the population has been marked as evaluated in its current state, else false 
//	 */
//	public boolean isEvaluated() {
//		if (evaluationTimeModCount != modCount) return false;
//		Pair<Integer,Integer> hashes = getIndyHashSums();
//		
//		if (evaluationTimeHashes == null) return false;
//		else return ((hashes.head().equals(evaluationTimeHashes.head())) && (hashes.tail().equals(evaluationTimeHashes.tail())) && (evaluationTimeModCount == modCount));
//	}

//	/**
//	 * Mark the population at the current state as evaluated. Changes to the modCount or hashes of individuals
//	 * will invalidate the mark.
//	 *  
//	 * @see  isEvaluated()
//	 */
//	public void setEvaluated() {
//		evaluationTimeModCount = modCount;
//		evaluationTimeHashes = getIndyHashSums();
//	}
	
//	private Pair<Integer,Integer> getIndyHashSums() {
//		int hashSum = 0, hashSumAbs = 0;
//		int hash;
//		for (int i=0; i<size(); i++) {
//			hash = get(i).hashCode();
//			hashSum += hash;
//			hashSumAbs += Math.abs(hash);
//		}
//		return new Pair(hashSum, hashSumAbs);
//	}
}
