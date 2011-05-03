package eva2.server.go.populations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.IndividualInterface;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.AbstractEAIndividualComparator;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.operators.distancemetric.EuclideanMetric;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.operators.postprocess.PostProcess;
import eva2.server.go.operators.selection.probability.AbstractSelProb;
import eva2.tools.EVAERROR;
import eva2.tools.Pair;
import eva2.tools.Serializer;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.tools.math.StatisticUtils;
import eva2.tools.math.Jama.Matrix;


/** 
 * A basic implementation of an EA population. Manage a set of potential solutions
 * in form of AbstractEAIndividuals. They can be sorted using an AbstractEAIndividualComparator.
 * Optionally, a history list is kept storing a clone of the best individual of any generation.
 * The Population also provides for appropriate counting of function calls performed.
 * For initialization, the default individual initialization method may be used, as well as a
 * random latin hypercube implementation for InterfaceDataTypeDouble individuals. 
 * 
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert, Marcel Kronfeld
 * @version:  $Revision: 307 $
 *            $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public class Population extends ArrayList implements PopulationInterface, Cloneable, java.io.Serializable {
	private transient static boolean TRACE = false;
    protected int           m_Generation    = 0;
    protected int           m_FunctionCalls = 0;
    protected int           m_TargetSize          = 50;
    protected Population    m_Archive       = null;
    PopulationInitMethod initMethod = PopulationInitMethod.individualDefault;
	private double[] seedPos = new double[10];
	private Pair<Integer,Integer> seedCardinality=new Pair<Integer,Integer>(5,1);

	private double aroundDist=0.1;
	transient private ArrayList<InterfacePopulationChangedEventListener> listeners = null;
    // the evaluation interval at which listeners are notified
    protected int 			notifyEvalInterval	= 0;
    // additional data connected to the population
    protected HashMap<String, Object>		additionalPopData = null;
    // historical best indidivuals may be traced for a given number of generations. Set to -1 to trace all, set to 0 to not trace at all
    int historyMaxLen = 0;
//    boolean useHistory						= false;
    private transient LinkedList<AbstractEAIndividual>  m_History       = new LinkedList<AbstractEAIndividual>();

    // remember when the last sorted queue was prepared
    private int lastQModCount = -1;
    // a sorted queue (for efficiency)
    transient private ArrayList<AbstractEAIndividual> sortedArr = null;
    private Comparator<Object> lastSortingComparator = null;
	private InterfaceDistanceMetric popDistMetric =  null; // an associated metric
//	private AbstractEAIndividualComparator historyComparator = null;

    public static final String funCallIntervalReached = "FunCallIntervalReached";
    public static final String populationInitialized = "PopulationReinitOccured";
    public static final String nextGenerationPerformed = "NextGenerationPerformed";
    
    public Population() {
    	if (TRACE) System.err.println("TRACING POP");
    }
    
    /**
     * Constructor setting initial capacity and target population size to the given
     * integer value.
     * 
     * @param initialCapacity initial capacity and population size of the instance
     */
    public Population(int initialCapacity) {
    	super(initialCapacity);
    	if (TRACE) System.err.println("TRACING POP");
    	setTargetSize(initialCapacity);
    }

    /**
     * Clones parameters, individuals, history and archive. 
     * 
     * @param population
     */
    public Population(Population population) {
    	if (TRACE) System.err.println("TRACING POP");
        setSameParams(population);
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i) != null)
                this.add((((AbstractEAIndividual)population.get(i))).clone());
        }
        copyHistAndArchive(population);
    }
        
    public static Population makePopFromList(List<AbstractEAIndividual> indies) {
		Population pop = new Population(indies.size());
    	if (TRACE) System.err.println("TRACING POP");
		pop.setTargetSize(indies.size());
		for (AbstractEAIndividual indy : indies) {
			pop.add(indy);
		}
		return pop;
	}

    /**
     * Create a new population from a solution set by merging
     * both current population and solutions from the set.
     *  
     * @param allSolutions
     */
	public Population(InterfaceSolutionSet allSolutions) {
		this(allSolutions.getCurrentPopulation().size()+allSolutions.getSolutions().size());
    	if (TRACE) System.err.println("TRACING POP");
		addPopulation(allSolutions.getCurrentPopulation(), false);
		HashMap<Long, Integer> checkCols = new HashMap<Long, Integer>(size());
		for (int i=0; i<size(); i++) {
			checkCols.put(getEAIndividual(i).getIndyID(), 1);
		}
		Population sols = allSolutions.getSolutions();
		for (int i=0; i<sols.size();i++) {
//			addPopulation(allSolutions.getSolutions());
			if (!checkCols.containsKey(sols.getEAIndividual(i).getIndyID())) add(sols.getEAIndividual(i));
		}
	}

	/**
	 * Initialize a population with a binCardinality initialization, meaning that
	 * the individuals are initialized with N(binCard, binStdDev) bits set.
	 * @param targetSize
	 * @param binCard
	 * @param binStdDev
	 */
	public Population(int targetSize, int binCard, int binStdDev) {
		this(targetSize);
		this.setSeedCardinality(new Pair<Integer,Integer>(binCard, binStdDev));
		this.setInitMethod(PopulationInitMethod.binCardinality);
	}
	
	public void hideHideable() {
		setInitMethod(getInitMethod());
	}
	
	/**
	 * Make (shallow) copies of the given instance within the current instance. 
	 * @param population
	 */
	public void copyHistAndArchive(Population population) {
    	if (population.m_Archive != null) this.m_Archive = (Population)population.m_Archive.clone();
    	if (population.m_History != null) this.m_History = (LinkedList<AbstractEAIndividual>)population.m_History.clone();
    	if (population.additionalPopData!=null) {
    		this.additionalPopData = (HashMap<String, Object>)additionalPopData.clone();
    		if (population.additionalPopData.size()>0) {
    			for (String key : population.additionalPopData.keySet()) {
					additionalPopData.put(key, population.additionalPopData.get(key));
				}
    		}
    	}
    }
    
    /**
     * Takes over all scalar parameters of the given population and copies the additional data
     * as well as listeners and the seed position. Those are considered functional.
     * @param population
     */
    public void setSameParams(Population population) {
        this.m_Generation       = population.m_Generation;
        this.m_FunctionCalls    = population.m_FunctionCalls;
        this.m_TargetSize       = population.m_TargetSize;
        this.historyMaxLen 		= population.historyMaxLen;
        this.notifyEvalInterval = population.notifyEvalInterval;
        this.initMethod			= population.initMethod;
    	this.aroundDist			= population.aroundDist;
 	    this.seedCardinality	= population.seedCardinality.clone();
    	if (population.getPopMetric()!=null) this.popDistMetric	= (InterfaceDistanceMetric)population.popDistMetric.clone();
        if (population.seedPos!=null) this.seedPos = population.seedPos.clone();
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

    /**
     * Be aware that this does not check all fields. Fields which are not considered
     * functional are omitted, such as archive, history and the listeners. 
     */
    public boolean equals(Object o) {
    	if (!super.equals(o)) return false;
    	if (o==null) return false;
    	if (!(o instanceof Population)) return false;
    	Population opop = (Population)o;
    	if (this.m_Generation   != opop.m_Generation) return false;
    	if (this.m_FunctionCalls    != opop.m_FunctionCalls) return false;
    	if (this.m_TargetSize       != opop.m_TargetSize) return false;
    	if (this.historyMaxLen 		!= opop.historyMaxLen) return false;
    	if (this.notifyEvalInterval != opop.notifyEvalInterval) return false;
    	if (this.initMethod			!= opop.initMethod) return false;
    	if (this.aroundDist			!= opop.aroundDist) return false;
    	if ((this.seedPos!=null) ^ (opop.seedPos!=null)) return false;
    	if ((this.seedPos!=null) && (!this.seedPos.equals(opop.seedPos))) return false; 
    	//listeners may be omitted
    	if ((this.additionalPopData!=null) ^ (opop.additionalPopData!=null)) return false;
    	if (this.additionalPopData!=null) for (String s : this.additionalPopData.keySet()) {
    		if (this.additionalPopData.get(s)==null && (opop.additionalPopData.get(s)!=null)) return false;
    		if (this.additionalPopData.get(s)!=null && (!this.additionalPopData.get(s).equals(this.additionalPopData))) return false;
		}
    	return true;
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

    /**
     * Clone the population with shallow copies of the individual. This should be used with care!
     * @return
     */
    public Population cloneShallowInds() {
		Population pop = cloneWithoutInds();
		pop.addAll(this);
		return pop;
	}

    /** This method inits the state of the population AFTER the individuals
     * have been inited by a problem
     */
    public void init() {
        this.m_History = new LinkedList<AbstractEAIndividual>();
        this.m_Generation       = 0;
        this.m_FunctionCalls    = 0;
        double[] popSeed = null;
//    	evaluationTimeHashes = null;
//    	evaluationTimeModCount = -1;
        if (this.m_Archive != null) {
            this.m_Archive.clear();
            this.m_Archive.init();
        }
        switch (initMethod) {
        case individualDefault:
        	break;
        case randomLatinHypercube:
        	createRLHSampling(this, false);
        	break;
        case aroundSeed:
        case aroundRandomSeed:
        	AbstractEAIndividual template = (AbstractEAIndividual) getEAIndividual(0).clone();
        	// use random initial position or the predefined one
        	if (initMethod==PopulationInitMethod.aroundRandomSeed) popSeed=RNG.randomDoubleArray(PostProcess.getDoubleRange(template));
        	else popSeed = seedPos;
        	if (template.getDoublePosition().length<=popSeed.length) {
        		if (template.getDoublePosition().length<popSeed.length) {
        			double[] smallerSeed = new double[template.getDoublePosition().length];
        			System.arraycopy(popSeed, 0, smallerSeed, 0, smallerSeed.length);
        			AbstractEAIndividual.setDoublePosition(template, smallerSeed);
        		} else AbstractEAIndividual.setDoublePosition(template, popSeed);
        		PostProcess.createPopInSubRange(this, aroundDist, this.getTargetSize(), template);
        	} else System.err.println("Warning, skipping seed initialization: too small individual seed!");
        	break;
      	case binCardinality:
        	createBinCardinality(this, true, seedCardinality.head(), seedCardinality.tail());
        	break;
        }
        //System.out.println("After pop init: " + this.getStringRepresentation());
        firePropertyChangedEvent(Population.populationInitialized);
    }
    
    /**
     * Reset all values changing during the "life" of a population (such as history,
     * generation counter etc).
     */
	public void resetProperties() {
	    m_Generation    = 0;
	    m_FunctionCalls = 0;
	    if (m_Archive!=null) {
	    	m_Archive.clear();
		    m_Archive.clearHistory();
	    }
	    clearHistory();
		modCount++;
	    // a sorted queue (for efficiency)
	    sortedArr = null;
	}
    
    public double[] getInitPos() {
    	return seedPos ;
    }
    public void setInitPos(double[] si) {
    	seedPos=si;
    }
    public String initPosTipText() {
    	return "Position around which the population will be (randomly) initialized. Be aware that the vector length must match (or exceed) problem dimension!";
    }

    public void setInitAround(double d) {
    	aroundDist = d;
    }
    public double getInitAround() {
    	return aroundDist;
    }
    public String initAroundTipText() {
    	return "Lenght of hypercube within which individuals are initialized around the initial position.";
    }
    
    /** This method inits the population. Function and generation counters
     * are reset and m_Size default Individuals are created and initialized by
     * the GAIndividual default init() method.
      */
    public void defaultInit(AbstractEAIndividual template) {
        this.m_Generation       = 0;
        this.m_FunctionCalls    = 0;
        this.m_Archive          = null;
        this.clear();
        for (int i = 0; i < this.m_TargetSize; i++) {
            AbstractEAIndividual tmpIndy = (AbstractEAIndividual)template.clone();
            tmpIndy.defaultInit(null);
            super.add(tmpIndy);
        }
    }

    /**
     * Create a population instance which distributes the individuals according to 
     * a random latin hypercube sampling.
     *  
     * @param popSize
     * @param template
     * @return
     */     
    public static Population createRLHSampling(int popSize, AbstractEAIndividual template) {
    	Population pop = new Population(popSize);
    	pop.add(template);
    	createRLHSampling(pop, true);
    	return pop;
    }
    
    /**
     * Create a population instance which distributes the individuals according to 
     * a random latin hypercube sampling.
     *  
     * @param popSize
     * @param template
     * @return
     */     
    public static void createRLHSampling(Population pop, boolean fillPop) {
    	if (pop.size()<=0) {
    		System.err.println("createRLHSampling needs at least one template individual in the population");
    		return;
    	}
    	AbstractEAIndividual template = pop.getEAIndividual(0);
    	if (fillPop) pop.fill(template);
    	if (template instanceof InterfaceDataTypeDouble) {
    		double[][] range = ((InterfaceDataTypeDouble)template).getDoubleRange();
//    		Population pop = new Population(popSize);
    		Matrix rlhM = StatisticUtils.rlh(pop.size(), range, true);
    		for (int i=0; i<pop.size(); i++) {
    			AbstractEAIndividual tmpIndy = pop.getEAIndividual(i);
    			((InterfaceDataTypeDouble)tmpIndy).SetDoubleGenotype(rlhM.getRowShallow(i));
    		}
    	} else {
    		System.err.println("Error: data type double required for Population.createUniformSampling");
    	}
    }
    
    /**
     * Initialize a population of GA individuals with a certain cardinality and possibly variation.
     * At least one template individual must be contained in the population. All individuals must have
     * binary genotypes. The bitsets are then initialized with the given cardinality (random no. bits set)
     * and possibly gaussian variation defined by std.dev. which is truncated at zero and the genotype length.
     *  
     * @param pop the population instance
     * @param fillPop if true, fill the population up to the target size
     * @param cardinality an integer giving the number of (random) bits to set
     * @param stdDev standard deviation of the cardinality variation (can be zero to fix the cardinality)
     * @return
     */     
    public static void createBinCardinality(Population pop, boolean fillPop, int cardinality, int stdDev) {
    	if (pop.size()<=0) {
    		System.err.println("createBinCardinality needs at least one template individual in the population");
    		return;
    	}
    	AbstractEAIndividual template = pop.getEAIndividual(0);
    	if (fillPop) pop.fill(template);
    	if (template instanceof InterfaceGAIndividual) {
//    		InterfaceGAIndividual gaIndy = (InterfaceGAIndividual)template;
    		for (int i=0; i<pop.size(); i++) {
    			InterfaceGAIndividual gaIndy = (InterfaceGAIndividual)pop.getEAIndividual(i);
    			int curCard = cardinality;
    			if (stdDev>0) curCard += (int)Math.round(RNG.gaussianDouble((double)stdDev));
    			curCard=Math.max(0, Math.min(curCard, gaIndy.getGenotypeLength()));
//    			System.out.println("Current cardinality: " + curCard);
    			gaIndy.SetBGenotype(RNG.randomBitSet(curCard, gaIndy.getGenotypeLength()));
//    			System.out.println(pop.getEAIndividual(i));
    		}
    	} else {
    		System.err.println("Error: InterfaceGAIndividual required for binary cardinality initialization!");
    	}
    }
    
    /**
     * Fill the population up to the target size with clones of a template individual.
     * 
     * @param template	a template individual used to fill the population
     */
    public void fill(AbstractEAIndividual template) {
    	if (this.size()<this.getTargetSize()) {
    		for (int i=this.size(); i<this.getTargetSize(); i++) {
    			this.add((AbstractEAIndividual)template.clone());
    		}
    	}
    }
    
    /**
     * Activate or deactivate the history tracking, which stores the best individual in every
     * generation in the incrGeneration() method.
     * 
     * @param useHist
     */
    public void setUseHistory(boolean useHist) {
    	if (useHist) setMaxHistoryLength(-1); // trace full history!
    	else setMaxHistoryLength(0); // dont trace at all!
//    	this.setUseHistory(useHist, (useHist ? (new AbstractEAIndividualComparator()) : null));
    }
    public boolean isUsingHistory() {
    	return historyMaxLen!=0;
    }
    public void setMaxHistoryLength(int len) {
    	historyMaxLen = len; 
    }
    public int getMaxHistLength() {
    	return historyMaxLen;
    }
//    /**
//     * Activate or deactivate the history tracking, which stores the best individual in every
//     * generation in the incrGeneration() method.
//     * 
//     * @param useHist
//     * @param histComparator comparator to use for determining the best current individual
//     */
//    public void setUseHistory(boolean useHist, AbstractEAIndividualComparator histComparator) {
//    	useHistory = useHist;
//    	this.historyComparator  = histComparator;
//    }
    
    public int getHistoryLength() {
    	if (historyMaxLen!=0) return m_History.size();
    	else return 0;
    }
    
    public LinkedList<AbstractEAIndividual> getHistory() {
    	return m_History;
    }
    
    public void SetHistory(LinkedList<AbstractEAIndividual> theHist) {
    	m_History = theHist;
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
     * @param d     The number of function calls to increment by.
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
        	for (Iterator<InterfacePopulationChangedEventListener> iterator = listeners.iterator(); iterator.hasNext();) {
				InterfacePopulationChangedEventListener listener = iterator.next();
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
        if (isUsingHistory() && (this.size() >= 1)) {
        	if (historyMaxLen>0 && (m_History.size()>=historyMaxLen)) {
        		// oldest one must be replaced.. should be the one in front
        		m_History.removeFirst();
        	}
        	this.m_History.add((AbstractEAIndividual)this.getBestEAIndividual().clone());
        }
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
     * This method does not check for duplicate object pointers.
     * 
     * @param pop   The population that is to be added.
     */
    public Population addPopulation(Population pop) {
    	return this.addPopulation(pop, false);
    }
    
    /**
     * This method allows you to add a complete population to the current population.
     * Note: After this operation the target population size may be exceeded.
     * If indicated, this method checks for and avoids duplicate object pointers.
     * 
     * @param pop   The population that is to be added.
     * @param avoidDuplicatePointers if true, duplicate object pointers are forbidden
     */
    public Population addPopulation(Population pop, boolean avoidDuplicatePointers) {
        if (pop != null) {
        	for (int i = 0; i < pop.size(); i++) {
        		AbstractEAIndividual indy = (AbstractEAIndividual)pop.get(i);
        		if (avoidDuplicatePointers && this.contains(indy)) {
        			System.err.println("Warning, duplicate indy avoided in Population.addPopulation! Index of " + this.indexOf(indy));
        		} else {
        			if (indy != null) {
        				this.add(indy);
        			}
        		}
        	}
        }
        return this;
    }
    
    /** 
     * Fill the population up to the given size with random elements
     * from the given population.
     * 
     * @param upTo 	target size of the population
     * @param fromPop   The population that is to be added.
     * 
     */
    public boolean fillWithRandom(int upTo, Population fromPop) {
    	if (upTo <= this.size()) return true;
    	else if (fromPop==null || (fromPop.size()<1)) return false;
    	else {
    		int[] perm = RNG.randomPerm(fromPop.size());
    		int i=0;
    		while ((i<perm.length) && (this.size()<upTo)) { // until instance is filled or no more indys can be selected
    			AbstractEAIndividual indy = (AbstractEAIndividual)fromPop.get(perm[i]);
//				System.out.println("checking " + indy.getStringRepresentation());
    			if ((indy != null) && (!containsByPosition(indy))) {
    				this.add(indy);
    			} else {
//    				System.out.println(indy.getStringRepresentation() + " was contained!");
    			}
    			i++;
        	}
    		if (size()==upTo) return true;
    		else return false;
        }
    }
    
    /**
     * Find a list of pairs of indices within the population at which individuals with equal
     * positions can be found.
     * @return
     */
    public List<Pair<Integer,Integer>> findSamePositions() {
    	ArrayList<Pair<Integer,Integer>> dupes = new ArrayList<Pair<Integer,Integer>>();
    	for (int i=0; i<size()-1; i++) {
    		int nextIndex = indexByPosition(i+1, getEAIndividual(i));
    		if (nextIndex >= 0) dupes.add(new Pair<Integer,Integer>(i, nextIndex));
    	}
    	return dupes;
    }
    
    /**
     * Return true if an individual with equal position is contained within the population.
     * 
     * @param indy
     * @return
     */
    public boolean containsByPosition(AbstractEAIndividual indy) {
		return indexByPosition(0,indy)>=0;
	}

    /**
     * Return the index of the first individual which has an equal position or -1.
     * 
     * @param startIndex the first index to start the search
     * @param indy
     * @return the index of the first individual which has an equal position or -1
     */
	public int indexByPosition(int startIndex, AbstractEAIndividual indy) {
		for (int i=startIndex; i<size(); i++) {
			if (Arrays.equals(AbstractEAIndividual.getDoublePositionShallow(indy), AbstractEAIndividual.getDoublePositionShallow(getEAIndividual(i)))) return i;
		}
		return -1;
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
    public int getIndexOfBestIndividualPrefFeasible() {
    	if (size()<1) return -1;
    	return getIndexOfBestOrWorstIndy(true, true, -1);
    }
	
	/** 
	 * This method will return the index of the current worst individual from the
     * population.
     * 
     * @see getIndexOfBestOrWorstIndividual()
     * @return The index of the worst individual.
     */
    public int getIndexOfWorstIndividualNoConstr() {
    	return getIndexOfBestOrWorstIndy(false, false, -1);
    }
    
	/** 
	 * This method will return the index of the current best individual from the
     * population in the given fitness component (or using dominance when fitIndex < 0).
     * If the population is empty, -1 is returned.
     * 
     * @see getIndexOfBestOrWorstIndividual()
     * @return The index of the best individual.
     */
    public int getIndexOfBestIndividualPrefFeasible(int fitIndex) {
    	if (size()<1) return -1;
    	return getIndexOfBestOrWorstIndy(true, true, fitIndex);
    }
	
	/** 
	 * This method will return the index of the current best individual from the
     * population in the given fitness component (or using dominance when fitIndex < 0).
     * 
     * @see getIndexOfBestOrWorstIndividual()
     * @return The index of the best individual.
     */
    public int getIndexOfWorstIndividualNoConstr(int fitIndex) {
    	return getIndexOfBestOrWorstIndy(false, false, fitIndex);
    }
    
    /**
     * Return the best feasible individual or null if the population contains
     * no or only infeasible individuals. This considers both aspects: the constraint setting
     * as well as penalization. An individual is feasible only if it is both unpenalized and
     * not violating the constraints.
     * 
     * @param fitIndex
     * @return the best feasible individual or null if none is feasible
     */
    public AbstractEAIndividual getBestFeasibleIndividual(int fitIndex) {
    	int index=getIndexOfBestOrWorstFeasibleIndividual(true, fitIndex);
    	if (index<0) return null;
    	else return getEAIndividual(index);
    }
    
//    /**
//     * Return the best individual which has a zero penalty value (is feasible concerning 
//     * penalty). If all are penalized, null is returned.
//     * 
//     * @param fitIndex
//     * @return
//     */
//    public AbstractEAIndividual getBestUnpenalizedIndividual(int fitIndex) {
//        int     result          = -1;
//        double[]  curSelFitness = null;
//        boolean allViolate = true;
//
//        for (int i = 0; i < super.size(); i++) {
//            if (!(getEAIndividual(i).isMarkedPenalized())) {
//            	allViolate = false;
//            	if ((result<0) || (compareFit(true, getEAIndividual(i).getFitness(), curSelFitness, fitIndex))) {
//            		// fit i is better than remembered
//            		result          = i;
//            		curSelFitness  = getEAIndividual(i).getFitness(); // remember fit i
//            	}
//            }
//        }
//        if (allViolate) {
//        	return null;
//        } else {
//        	return getEAIndividual(result);
//        }
//    }
    
	/** 
	 * This method will return the index of the current best (worst) individual from the
     * population. A given comparator is employed for individual comparisons.
     * 
     * @param bBest if true, the best (first) index is returned, else the worst (last) one
     * @param comparator indicate whether constraints should be regarded
     * @return The index of the best (worst) individual.
     */
    public int getIndexOfBestOrWorstIndividual(boolean bBest, Comparator<Object> comparator) {
    	ArrayList<?> sorted = sortBy(comparator);
    	if (bBest) return indexOf(sorted.get(0));
    	else return indexOfInstance(sorted.get(sorted.size()-1));
    }

    public int getIndexOfBestEAIndividual(AbstractEAIndividualComparator comparator) {
    	return getIndexOfBestOrWorstIndividual(true, comparator);
    }
    
    public AbstractEAIndividual getBestEAIndividual(Comparator<Object> comparator) {
    	int index = getIndexOfBestOrWorstIndividual(true, comparator);
    	return getEAIndividual(index);
    }

    /**
     * Return the index of the best (or worst) indy using an AbstractEAIndividualComparator
     * that checks the constraints first and then for the given fitness criterion (or 
     * a pareto criterion if it is -1).
     * 
     * @param bBest  if true, the best (first) index is returned, else the worst (last) one
     * @param checkConstraints
     * @param fitIndex
     * @see #getIndexOfBestOrWorstIndividual(boolean, Comparator)
     * @see AbstractEAIndividualComparator
     * @return
     */
    public int getIndexOfBestOrWorstIndy(boolean bBest, boolean checkConstraints, int fitIndex) {
    	return getIndexOfBestOrWorstIndividual(bBest, new AbstractEAIndividualComparator(fitIndex, checkConstraints));
    }
    
    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * This only checks for pointer equality. Different instances which
     * have equal properties will be considered different. 
     */
    public int indexOfInstance(Object o) {
    	if (o == null) {
    		System.err.println("Error, instance null should not be contained! (Population.indexOfInstance)");
    	} else {
    		for (int i = 0; i < size(); i++) {
    			if (o==get(i)) {
    				return i;
    			}
    		}
    	}
    	return -1;
    }

//	/** 
//	 * This method will return the index of the current best (worst) individual from the
//     * population. If indicated, only those are regarded which do not violate the constraints.
//     * If all violate the constraints, the smallest (largest) violation is selected.
//     * Comparisons are done multicriterial, but note that for incomparable sets (pareto fronts)
//     * this selection will not be fair (always the lowest index of incomparable sets will be returned).
//     * 
//     * @param bBest if true, smallest fitness (regarded best) index is returned, else the highest one
//     * @param indicate whether constraints should be regarded
//     * @return The index of the best (worst) individual.
//     */
//    public int getIndexOfBestOrWorstIndividual(boolean bBest, boolean checkConstraints, int fitIndex) {
//        int     result          = -1;
//        double[]  curSelFitness = null;
//        boolean allViolate = true;
//
//        for (int i = 0; i < super.size(); i++) {
//            if (!checkConstraints || !(getEAIndividual(i).violatesConstraint())) {
//            	allViolate = false;
//            	if ((result<0) || (compareFit(bBest, getEAIndividual(i).getFitness(), curSelFitness, fitIndex))) {
//            		// fit i is better than remembered
//            		result          = i;
//            		curSelFitness  = getEAIndividual(i).getFitness(); // remember fit i
//            	}
//            }
//        }
//        if (result < 0) {
//        	if (checkConstraints && allViolate) {
//        		// darn all seem to violate the constraint
//        		// so lets search for the guy who is close to feasible
//        		
//        		// to avoid problems with NaN or infinite fitness value, preselect an ind.
//        		result          = 0;
//        		double minViol = getEAIndividual(0).getConstraintViolation();
//        		for (int i = 1; i < super.size(); i++) {
//        			if ((bBest && getEAIndividual(i).getConstraintViolation() < minViol) ||
//            			(!bBest && (getEAIndividual(i).getConstraintViolation() > minViol))) {
//        				result          = i;
//        				minViol  = ((AbstractEAIndividual)super.get(i)).getConstraintViolation();
//        			}
//        		}
////        		System.err.println("Population reports: All individuals violate the constraints, choosing smallest constraint violation.");
//        		// this is now really reported nicer...
//        	} else {
//        		// not all violate, maybe all are NaN!
//        		// so just select a random one
//        		System.err.println("Population reports: All individuals seem to have NaN or infinite fitness!");
//        		result = RNG.randomInt(size());
//        	}
//        }
//        int testcmp = getIndexOfBestOrWorstIndy(bBest, checkConstraints, fitIndex);
//        if (result!=testcmp) {
//        	if (((getEAIndividual(result).getFitness(0)!=getEAIndividual(testcmp).getFitness(0)) && (!violatesConstraints(testcmp) || !violatesConstraints(result)))
//        			|| (violatesConstraints(testcmp) && violatesConstraints(result) && (!equalConstraintViolation(testcmp, result)))) {
//        		System.err.println("Error in comparison!!! " + getIndexOfBestOrWorstIndy(bBest, checkConstraints, fitIndex));
//        	}
//        } else System.err.println("Succ with " + bBest + " " + checkConstraints + " " + fitIndex);
//        return result;
//    }

//    private boolean equalConstraintViolation(int a, int b) {
//    	return (getEAIndividual(a).getConstraintViolation()==getEAIndividual(b).getConstraintViolation());
//	}
//
//	private boolean violatesConstraints(int i) {
//    	return (getEAIndividual(i).isMarkedPenalized() || getEAIndividual(i).violatesConstraint());
//	}

	/**
     * Search the population for the best (worst) considering the given criterion (or all criteria
     * for fitIndex<0) which also does not violate constraints.
     * Returns -1 if no feasible solution is found, else the index of the best feasible solution.
     * 
     * @param bBest
     * @param fitIndex
     * @return -1 if no feasible solution is found, else the index of the best feasible individual
     */
    public int getIndexOfBestOrWorstFeasibleIndividual(boolean bBest, int fitIndex) {
        int     result          = -1;
        double[]  curSelFitness = null;

        for (int i = 0; i < super.size(); i++) {
            if (!(getEAIndividual(i).violatesConstraint()) && !(getEAIndividual(i).isMarkedPenalized())) {
            	if ((result<0) || (compareFit(bBest, getEAIndividual(i).getFitness(), curSelFitness, fitIndex))) {
            		// fit i is better than remembered
            		result          = i;
            		curSelFitness  = getEAIndividual(i).getFitness(); // remember fit i
            	}
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
     * This method returns the currently best individual from the population 
     * by a given fitness component while prioritizing feasible ones.
     * If the population is empty, null is returned.
     *
     * @param fitIndex the fitness criterion index or -1
     * @return The best individual
     */
    public AbstractEAIndividual getBestEAIndividual(int fitIndex) {
    	if (size()<1) return null;
    	int best = this.getIndexOfBestIndividualPrefFeasible(fitIndex);
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
    public Population getBestNIndividuals(int n, int fitIndex) {
    	if (n<=0 || (n>super.size())) n=super.size();
    	Population pop = new Population(n);
    	getSortedNIndividuals(n, true, pop, new AbstractEAIndividualComparator(fitIndex));
    	return pop;
    }
    
    /** 
     * This method returns the n current worst individuals from the population, where
     * the sorting criterion is delivered by an AbstractEAIndividualComparator.
     * There are less than n individuals returned if the population is smaller than n.
     * If n is <= 0, then all individuals are returned and effectively just sorted
     * by fitness.
     * This does not check constraints!
     * 
     * @param n	number of individuals to look out for
     * @return The m worst individuals, where m <= n
     * 
     */
    public Population getWorstNIndividuals(int n, int fitIndex) {
    	Population pop = new Population(n);
    	getSortedNIndividuals(n, false, pop, new AbstractEAIndividualComparator(fitIndex));
    	return pop;
    }
    
    /** 
     * This method returns a clone of the population instance with sorted individuals, where
     * the sorting criterion is delivered by a Comparator.
     * @see #getSortedNIndividuals(int, boolean, Population, Comparator)
     * 
     * @return a clone of the population instance with sorted individuals, best fitness first
     */
    public Population getSortedBestFirst(Comparator<Object> comp) {
    	Population result = this.cloneWithoutInds();
    	getSortedNIndividuals(size(), true, result, comp);
    	result.synchSize();
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
//    public Population getSortedNIndividuals(int n, boolean bBestOrWorst) {
//    	Population result = new Population((n > 0) ? n : this.size());
//    	getSortedNIndividuals(n, bBestOrWorst, result);
//    	return result;
//    }
    
    /** 
     * This method returns the n current best individuals from the population, where
     * the sorting criterion is delivered by a Comparator instance.
     * There are less than n individuals returned if the population is smaller than n.
     * This does not check constraints!
     * 
     * @param n	number of individuals to look out for
     * @param bBestOrWorst if true, the best n are returned, else the worst n individuals
     * @param res	sorted result population, will be cleared
     * @param comp the Comparator to use with individuals
     * @return The m sorted best or worst individuals, where m <= n
     * 
     */
    public void getSortedNIndividuals(int n, boolean bBestOrWorst, Population res, Comparator<Object> comp) {
    	if ((n < 0) || (n>super.size())) {
    		// this may happen, treat it gracefully
    		//System.err.println("invalid request to getSortedNIndividuals: n="+n + ", size is " + super.size());
    		n = super.size();
    	}
    	int skip = 0;
    	if (!bBestOrWorst) skip = super.size()-n;
    	
//    	hier getSorted aufrufen
    	ArrayList<AbstractEAIndividual> sorted = getSorted(comp);
    	res.clear();
        for (int i = skip; i < skip+n; i++) {
        	res.add(sorted.get(i));
        }
        res.synchSize();
    }
    
//    /**
//     * Get the n best (or worst) individuals from the population. The last comparator
//     * is reused, or if none has been employed yet, a standard comparator is used.
//     * 
//     * @param n
//     * @param bBestOrWorst
//     * @param res
//     */
//    public void getSortedNIndividuals(int n, boolean bBestOrWorst, Population res) {
//    	getSortedNIndividuals(n, bBestOrWorst, res, lastSortingComparator);
//    }
    
    /**
     * From the given list, remove all but the first n elements.
     * @param n
     * @param l
     */
    public static List<AbstractEAIndividual> toHead(int n, List<AbstractEAIndividual> l) {
    	l.subList(n, l.size()).clear();
    	return l;
    }
    
    /**
     * From the given list, remove all but the last n elements.
     * @param n
     * @param l
     */
    public static List<AbstractEAIndividual> toTail(int n, List<AbstractEAIndividual> l) {
    	l.subList(0, l.size()-n).clear();
    	return l;
    }
    
    /**
     * Return a new population containing only the last n elements of the instance.
     * @param n
     * @param l
     */
    public Population toTail(int n) {
    	Population retPop = new Population(n);
    	retPop.addAll(subList(0, size()-n));
    	return retPop;
    }
    
    /**
     * Set a fitness criterion for sorting procedures. This sorts the
     * population once and influences further getBest* methods if no
     * specific comparator is given.
     * @param fitIndex
     */
    public void setSortingFitnessCriterion(int fitIndex) {
    	getSorted(new AbstractEAIndividualComparator(fitIndex));
    }
    
//    /**
//     * Reuses the last fitness criterion. Avoid having to sort again in several calls without modifications in between.
//     * The returned array should not be modified!
//     * 
//     * @return
//     */
//    protected ArrayList<AbstractEAIndividual> getSorted() {
//    	return getSorted(lastFitCrit);
//    }

    /**
     * Sort the population returning a new ArrayList.
     * The returned array should not be modified!
     * 
     * @param comp A comparator by which sorting is performed - it should work on AbstractEAIndividual instances.
     * @return
     */
    protected ArrayList<AbstractEAIndividual> sortBy(Comparator<Object> comp) {
    	if (super.size()==0) return new ArrayList<AbstractEAIndividual>();
		PriorityQueue<AbstractEAIndividual> sQueue = new PriorityQueue<AbstractEAIndividual>(super.size(), comp);
		for (int i = 0; i < super.size(); i++) {
			AbstractEAIndividual indy = getEAIndividual(i);
			if (indy != null) sQueue.add(indy);
		}
		ArrayList<AbstractEAIndividual> sArr = new ArrayList<AbstractEAIndividual>(this.size());
		AbstractEAIndividual indy;
		while ((indy=sQueue.poll())!=null) sArr.add(indy);
		return sArr;
    }
    
    /**
     * Return a sorted list of individuals. The order is based on the
     * given comparator. Repeated calls do not resort the population every
     * time as long as an equal comparator is used (implement the equals() method!)
     * and the population has not been modified.
     * The returned array must not be altered!
     * 
     * @param fitIndex the fitness criterion to be used or -1 for pareto dominance
     * @return
     */
    public ArrayList<AbstractEAIndividual> getSorted(Comparator<Object> comp) {
//    	Comparator<Object> comp = new AbstractEAIndividualComparator(fitIndex);
//    	if (!comp.equals(lastSortingComparator)) return sortedArr;
    	if (!comp.equals(lastSortingComparator) || (sortedArr == null) || (super.modCount != lastQModCount)) {
//    		lastFitCrit=fitIndex;lastSortingComparator
    		ArrayList<AbstractEAIndividual> sArr = sortBy(comp);
    		if (sortedArr==null) sortedArr = sArr;
    		else {
    			sortedArr.clear();
    			sortedArr.addAll(sArr);
    		}
    		lastSortingComparator = (Comparator<Object>) Serializer.deepClone(comp);
    		lastQModCount = super.modCount;
    	}
    	return sortedArr;
    }

    /**
     * Returns the sorted population as a new population instance.
     * @see getSorted(Comparator)
     */
    public Population getSortedPop(Comparator<Object> comp) {
    	Population pop = this.cloneWithoutInds();
    	ArrayList<AbstractEAIndividual> sortedIndies = getSorted(comp);
    	pop.addAll(sortedIndies);
    	return pop;
    }
    
    /** 
     * This method retrieves n random individuals from the population and
     * returns them within a new population.
     * 
     * @param n	number of individuals to look out for
     * @return The n best individuals
     * 
     */
    public Population getRandNIndividuals(int n) {
    	if (n>=size()) return (Population)clone();
    	else {
        	Population pop = cloneShallowInds();
        	Population retPop = cloneWithoutInds();
        	moveNInds(n, pop, retPop);
        	return retPop;
    	}
    }

	/** 
     * This method removes n random individuals from the population and
     * returns them within a new population.
     * 
     * @param n	number of individuals to look out for
     * @return The n best individuals
     * 
     */
    public Population moveRandNIndividuals(int n) {
    	return moveRandNIndividualsExcept(n, new Population());	
    }
    
    /** 
     * This method removes n random individuals from the population (excluding the given ones)
     * and returns them in a new population instance. 
     * 
     * @param n	number of individuals to look out for
     * @return The n random individuals
     * 
     */
    public Population moveRandNIndividualsExcept(int n, Population exclude) {
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
    	dst.add(src.removeIndexSwitched(k));
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
    	return getEAIndividual(getIndexOfWorstIndividualNoConstr(fitIndex));
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
    public void removeRedundantIndies() {
        for (int i = 0; i < this.size(); i++) {
            for (int j = i+1; j < this.size(); j++) {
                if (((AbstractEAIndividual)this.get(i)).equals(this.get(j))) {
                    this.remove(j);
                    j--;
                }
            }
        }
    }

    public Population removeRedundantIndiesAsNew() {
    	Population pop = this.cloneShallowInds();
    	pop.removeRedundantIndies();
    	return pop;
    }
	/**
	 * Return the number of individuals within this instance that are
	 * seen as being equal by the equals relation of the individual.
	 * 
	 * @return the number of equal individuals
	 * 
	 */
	public int getRedundancyCount() {
		int redund = 0;
		for (int i=0; i<size()-1; i++) {
			for (int j=i+1; j<size(); j++) {
				if (getEAIndividual(i).equals(getEAIndividual(j))) {
					redund++;
					break; // jump because the i-th is redundant and we dont want to count all redundant pairs
				}
			}
		}
		return redund;
	}
	
    /** This method will remove instances with equal fitness from the population.
     */
    public void removeRedundantIndiesUsingFitness() {
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

    /** 
     * This method will return a string description of the Population 
     * including the individuals.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        StringBuilder strB = new StringBuilder(200);
        strB.append("Population:\nPopulation size: ");
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

    public String getName() {
    	return "Population-"+getTargetSize();
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
    public static String globalInfo() {
        return "A population stores the individuals of a generation.";
    }

    /** 
     * This method allows you to set the population size. Be aware that this will not 
     * directly alter the number of individuals stored. The actual size will be
     * adapted on a reinitialization, for example.
     * 
     * @param size
     */
    public void setTargetSize(int size) {
        this.m_TargetSize = size;
    }
    
    /**
     * Convenience method.
     * 
     * @param size
     * @return
     */
    public Population setTargetPopSize(int size) {
        setTargetSize(size);
        return this;
    }
    
    public int getTargetSize() {
        return this.m_TargetSize;
    }
    public String targetSizeTipText() {
        return "The initial population size.";
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
    	if (o==null) {
    		EVAERROR.errorMsgOnce("Warning: tried to add null as individual, skipping add... (Population.add(IndividualInterface)), possibly multiple cases.");
    		return false;
    	} else return addIndividual((IndividualInterface)o);
    }

    public Population addToPop(IndividualInterface o) {
    	add(o);
    	return this;
    }
    
    /**
	 * ArrayList does not increase the modCount in set. Maybe because it is not
	 * seen as "structural change"?
     */
    public Object set(int index, Object element) {
    	Object prev = super.set(index, element);
    	modCount++;
    	return prev;
    }
    
    /**
	 * ArrayList does not increase the modCount in set. Why???
	 * This method keeps the internally sorted array in synch to minimize complete resorting events.
     */
    public Object set(int index, Object element, int fitIndex) {
    	Object prev = super.set(index, element);
    	modCount++;
    	// Nelder Mead somehow managed to produce inconsistent sorted population - some hidden add/replace?
//    	if (lastFitCrit==fitIndex && (lastQModCount==(modCount-1))) {
//    		// if nothing happened between this event and the last sorting by the same criterion...
//    		if (!sortedArr.remove(prev)) {
//    			System.err.println("Error in Population.set!");
//    		}
//    		int i=0;
//    		AbstractEAIndividualComparator comp = new AbstractEAIndividualComparator(fitIndex);
//    		while (i<sortedArr.size() && comp.compare(element, sortedArr.get(i))>0) {
//    			i++;
//    		}
//    		sortedArr.add(i, (AbstractEAIndividual)element);
//    		lastQModCount=modCount;
//    	}
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
    public AbstractEAIndividual removeIndexSwitched(int index) {
    	AbstractEAIndividual indy = getEAIndividual(index);
    	int lastIndex = size()-1;
    	if (index < lastIndex) set(index, get(lastIndex));
    	remove(lastIndex);    	
    	return indy;
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
    
    /**
     * 
     * @param ind
     * @return true if an indy was actually removed, else false
     */
    public boolean removeMember(IndividualInterface ind) {
    	long id = ((AbstractEAIndividual)ind).getIndyID();
        for (int i = 0; i < this.size(); i++) {
            if (getEAIndividual(i).getIndyID()==id) {
            	removeIndexSwitched(i);
            	return true;
            }
        }
        return false;
    }

    /**
     * Remove a set of individuals from the instance. If indicated by the errorOnMissing-flag
     * a RuntimeException is thrown as soon as one individual to be removed is not contained
     * in the instance. Otherwise, this is ignored.
     * 
     * @param tmpPop
     * @param errorOnMissing
     */
	public void removeMembers(Population popToRemove, boolean errorOnMissing) {
		for (int i=0; i<popToRemove.size(); i++) {
			if (!removeMember(popToRemove.getEAIndividual(i))) {
				if (errorOnMissing) throw new RuntimeException("Error, member to be removed was missing (Population.removeMembers)!");
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
     * Returns the average, minimal and maximal individual distance as diversity measure for the population.
     * This uses the default population metric of the instance.
     * 
     * @return the average, minimal and maximal mean distance of individuals in an array of three
     */
    public double[] getPopulationMeasures() {
    	return getPopulationMeasures(getPopMetric());
    }
    
    public InterfaceDistanceMetric getPopMetric() {
    	if (popDistMetric==null) popDistMetric = new PhenotypeMetric();
    	return popDistMetric;
    }
    public void setPopMetric(InterfaceDistanceMetric metric) {
    	popDistMetric=metric;
    }
    public String popMetricTipText() {
    	return "Set a default distance metric to be used with the population.";
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
//    	Integer lastMeasuresModCount = (Integer)getData(lastPopMeasuresFlagKey);
//    	if (lastMeasuresModCount!=null && (lastMeasuresModCount==modCount)) {
//    		double[] measures = (double[])getData(lastPopMeasuresDatKey);
//            if (TRACE ) {
//            	double[] verify = calcPopulationMeasures(metric);
//            	if (Mathematics.dist(measures, verify, 2)>1e-10) {
//            		System.out.println(getStringRepresentation());
//            		System.err.println("Warning, invalid measures!!!");
//            		
//            	}
//            }
//    		return measures;
//    	} 
    	double[] res = getPopulationMeasures(this, metric);
//        putData(lastPopMeasuresFlagKey, new Integer(modCount));
//        putData(lastPopMeasuresDatKey, res);
//        System.out.println(getStringRepresentation());
//        System.out.println("0-1-dist: " + BeanInspector.toString(metric.distance((AbstractEAIndividual)this.get(0), (AbstractEAIndividual)this.get(1))));
        return res;
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
	public static double[] getPopulationMeasures(List<AbstractEAIndividual> pop, InterfaceDistanceMetric metric) {
		double d;
    	double[] res = new double[3];
    	
    	double distSum = 0.;
    	double maxDist = Double.MIN_VALUE;
    	double minDist = Double.MAX_VALUE;
    	
    	for (int i = 0; i < pop.size(); i++) {
    		for (int j = i+1; j < pop.size(); j++) {
    			try {
    				if (metric == null) {
    					if (pop.get(i) instanceof InterfaceESIndividual) { 
    						// short cut if the distance may directly work on the genotype 
    						d = EuclideanMetric.euclideanDistance(AbstractEAIndividual.getDoublePositionShallow(pop.get(i)), 
    	    						AbstractEAIndividual.getDoublePositionShallow(pop.get(j)));
    					} else {
    						d = PhenotypeMetric.dist(pop.get(i), pop.get(j));
    					}
    				} else {
    					d = metric.distance((AbstractEAIndividual)pop.get(i), (AbstractEAIndividual)pop.get(j));
    				}
    			} catch (Exception e) {
    				EVAERROR.errorMsgOnce("Exception when calculating population measures ... possibly no double position available?");
    				d = 0;
    			}
    			distSum += d;
    			if (d < minDist) minDist = d;
    			if (d > maxDist) maxDist = d;
            }
        }
        res[1] = minDist;
        res[2] = maxDist;
        if (pop.size() > 1) res[0] = distSum / (pop.size() * (pop.size()-1) / 2);
        else { // only one indy?
        	res[1]=0;
        	res[2]=0;
        }
		return res;
	}
	
	/**
	 * Return the minimal, maximal, average, median, and variance of correlations between any two solutions.
	 * @return
	 */
	public double[] getCorrelations() {
		return Population.getCorrelations(this);
	}
	
	/**
	 * Return the minimal, maximal, average, median, and variance of correlations between any two solutions.
	 * 
	 * @param pop the population instance to look at
	 * @return
	 */
	public static double[] getCorrelations(Population pop) {
		if (pop.size()<2) {
			return new double[]{1.,1.,1.,1.};
		}
		if (!(pop.getEAIndividual(0) instanceof InterfaceDataTypeDouble)) {
			// EVAERROR.errorMsgOnce("Warning: population correlations can only be calculated for double valued data!");
			return new double[]{Double.NaN,Double.NaN,Double.NaN,Double.NaN};
		}
		double[] cors = new double[pop.size()*(pop.size()-1)/2]; 
		int index=0;
		double corsSum=0, minCor = 10., maxCor=-10.;
        for (int i = 0; i < pop.size()-1; i++) {
        	for (int j = i+1; j < pop.size(); j++) {
        		double cor = StatisticUtils.correlation(pop.getEAIndividual(i).getDoublePosition(), pop.getEAIndividual(j).getDoublePosition());
        		cors[index++] = cor; 
        		corsSum += cor;
        		if (cor>maxCor) maxCor=cor;
        		if (cor<minCor) minCor=cor;
        	}
        }
        double var = StatisticUtils.variance(cors, true);
        double[] res = new double[] {minCor, maxCor, corsSum/cors.length, Mathematics.median(cors, false), var};
        return res;
	}
	
    /**
     * Returns the average, minimal and maximal individual fitness and std dev. for the population in the given criterion.
     *
     * @param fitCrit fitness dimension to be used
     * @return the average, minimal, maximal and std dev. of fitness of individuals in an array
     */
    public double[] getFitnessMeasures(int fitCrit) {
    	return Population.getFitnessMeasures(this, fitCrit);
    }
    
    /**
     * Returns the average, minimal and maximal individual fitness and std dev. for the population in the given criterion.
     *
     * @param fitCrit fitness dimension to be used
     * @return the average, minimal, maximal and std dev. of fitness of individuals in an array
     */
    public static double[] getFitnessMeasures(List<AbstractEAIndividual> pop, int fitCrit) {
    	double d;
    	double[] res = new double[4];
    	
    	res[0] = 0.;
    	res[1] = Double.MAX_VALUE;
    	res[2] = Double.MIN_VALUE;
    	res[3] = 0;
    	
        for (int i = 0; i < pop.size(); i++) {
        		d = pop.get(i).getFitness(fitCrit);
        		res[0] += d;
                if (d < res[1]) res[1] = d;
                if (d > res[2]) res[2] = d;
        }
        
        if (pop.size()==0) {
        	res[0]=res[1]=res[2]=res[3]=Double.NaN;
        } else {
        	// calc standard deviation
        	res[0] = res[0] / pop.size();
        	for (int i=0; i< pop.size(); i++) {
        		d = res[0]-pop.get(i).getFitness(fitCrit);
        		res[3]+=d*d;
        	}
        	res[3] /= pop.size();
        	res[3] = Math.sqrt(res[3]);
        }
        
        
        return res;
    }
    
	 /**
	  * Search for the closest (farthest) individual to the given position.
	  * Return a Pair of the individuals index and distance.
	  * If the population is empty, a Pair of (-1,-1) is returned.
	  * 
	  * @param pos
	  * @param pop
	  * @param closestOrFarthest if true, the closest individual is retrieved, otherwise the farthest
	  * @return 
	  */
	 public static Pair<Integer,Double> getClosestFarthestIndy(double[] pos, Population pop, boolean closestOrFarthest) {
		 double dist = -1.;
		 int sel=-1;
		 for (int i = 0; i < pop.size(); i++) {
			 AbstractEAIndividual indy = pop.getEAIndividual(i);
			 double[] indyPos = AbstractEAIndividual.getDoublePositionShallow(indy);
			 double curDist = EuclideanMetric.euclideanDistance(pos, indyPos);
			 if ((dist<0) 	|| (!closestOrFarthest && (dist < curDist)) 
					 		|| (closestOrFarthest && (dist > curDist))) {
				 dist = curDist;
				 sel = i;
			 }
		 }
		 return new Pair<Integer,Double>(sel,dist);
	 }
	 
	 /**
	  * Search for the closest (farthest) individual to the given individual.
	  * Return a Pair of the individuals index and distance.
	  * If the population is empty, a Pair of (-1,-1) is returned.
	  * 
	  * @param pos
	  * @param pop
	  * @param closestOrFarthest if true, the closest individual is retrieved, otherwise the farthest
	  * @return 
	  */
	 public static Pair<Integer,Double> getClosestFarthestIndy(AbstractEAIndividual refIndy, Population pop, InterfaceDistanceMetric metric, boolean closestOrFarthest) {
		 double dist = -1.;
		 int sel=-1;
		 for (int i = 0; i < pop.size(); i++) {
			 if (pop.getEAIndividual(i)!=null) {
				 double curDist = metric.distance(refIndy, pop.getEAIndividual(i));
				 if ((dist<0) 	|| (!closestOrFarthest && (dist < curDist)) 
						 || (closestOrFarthest && (dist > curDist))) {
					 dist = curDist;
					 sel = i;
				 }
			 }
		 }
		 return new Pair<Integer,Double>(sel,dist);
	 }
	 

	 /**
	  * Check if the given indy is closer than d to any individual in the population.
	  * @param indy
	  * @param pop
	  * @param d
	  * @return true if d(indy,pop)<=d, else false
	  */
	 public boolean isWithinPopDist(AbstractEAIndividual indy, double d, InterfaceDistanceMetric metric) {
		 Pair<Integer,Double> closest = Population.getClosestFarthestIndy(indy, this, metric, true);
		 if (closest.tail()<=d) {
			 return true;
		 } else {
//			 if (size()==1) {
//				 System.out.println("dist is " + metric.distance(getEAIndividual(0), indy));
//			 }
			 return false;
		 }
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
			Mathematics.vvAdd(centerPos, AbstractEAIndividual.getDoublePositionShallow(getEAIndividual(i)), centerPos);
		}
		Mathematics.svDiv(size(), centerPos, centerPos);
		return centerPos;
	}
	
	/**
	 * Return an individual which is located at the center of the population.
	 * The fitness will be invalid.
	 * 
	 * @return
	 */
	public IndividualInterface getCenterIndy() {
		AbstractEAIndividual indy = (AbstractEAIndividual)getEAIndividual(0).clone();
		double[] center = getCenter();
		indy.setDoublePosition(indy, center);
		indy.SetFitness(null);
		return indy;
	}
	
	/**
	 * Calculate the weighted center position of the population. Weights must add up to one!
	 * 
	 * @return the average position of the population
	 */
	public double[] getCenterWeighted(double[] weights) {
		if (size()==0 || (weights.length > size()) || (weights.length==0)) {
			EVAERROR.errorMsgOnce("Invalid pop size in DistractingPopulation:getCenterWeighted!");
		}
		double[] centerPos = AbstractEAIndividual.getDoublePosition(getEAIndividual(0));
		Mathematics.svMult(weights[0], centerPos, centerPos);
		for (int i=1; i<weights.length; i++) {
			Mathematics.svvAddScaled(weights[i], AbstractEAIndividual.getDoublePositionShallow(getEAIndividual(i)), centerPos, centerPos);
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
		double[] mean = AbstractEAIndividual.getDoublePosition(getEAIndividual(0));
		
		if (mean != null) {
			Arrays.fill(mean, 0.);
			AbstractEAIndividual indy = null;
			for (int i=0; i<size(); i++) {
				indy = getEAIndividual(i);
				double[] pos = AbstractEAIndividual.getDoublePositionShallow(indy);
				Mathematics.svvAddScaled(indy.getSelectionProbability(criterion), pos, mean, mean);
			}
		}
		return mean;
	}
	
	/**
	 * Search for the closest individual to the indexed individual within the population. Return
	 * its index or -1 if none could be found.
	 * 
	 * @param indy
	 * @return closest neighbor (euclidian measure) of the given individual in the given population 
	 */
	public int getNeighborIndex(int neighborIndex) {
		// get the neighbor...
		int foundIndex = -1;
		double mindist = Double.POSITIVE_INFINITY;

		for (int i = 0; i < size(); ++i){ 
			AbstractEAIndividual currentindy = getEAIndividual(i);
			if (i!=neighborIndex){ // dont compare particle to itself or a copy of itself
				double dist = EuclideanMetric.euclideanDistance(AbstractEAIndividual.getDoublePositionShallow(getEAIndividual(neighborIndex)),
						AbstractEAIndividual.getDoublePositionShallow(currentindy));
				if (dist  < mindist){ 
					mindist = dist;
					foundIndex = i;
				}
			}
		}
		if (foundIndex == -1){
			System.err.println("Pop too small or all individuals in population are equal !?");
			return -1;
		}
		return foundIndex;
	}
	
	/**
	 * Calculate the average of the distance of each individual to its closest neighbor in the population.
	 * The boolean parameter switches between range-normalized and simple euclidian distance. If calcVariance
	 * is true, the variance is calculated and returned as second entry
	 * 
	 * @param normalizedPhenoMetric
	 * @return a double array containing the average (or average and variance) of the distance of each individual to its closest neighbor
	 */
	public double[] getAvgDistToClosestNeighbor(boolean normalizedPhenoMetric, boolean calcVariance) {
		PhenotypeMetric metric = new PhenotypeMetric();
		ArrayList<Double> distances = null;
		if (calcVariance) distances = new ArrayList<Double>(size());
		double sum = 0;
		double d=0;
		for (int i = 0; i < size(); ++i){
			AbstractEAIndividual neighbor, indy = getEAIndividual(i);
			int neighborIndex = getNeighborIndex(i);
			if (neighborIndex >= 0) neighbor = getEAIndividual(neighborIndex);
			else {
				System.err.println("Warning, neigbhorIndex<0 in Population.getAvgDistToClosestNeighbor");
				return null;
			}
			if (normalizedPhenoMetric){
				d = metric.distance(indy, neighbor);
			} else { 
				d = EuclideanMetric.euclideanDistance(AbstractEAIndividual.getDoublePositionShallow(indy),
						AbstractEAIndividual.getDoublePositionShallow(neighbor));
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
		if (size() != getTargetSize()) {
			while (size() > getTargetSize()) remove(size()-1);
			if (size() < getTargetSize()) {
				if (size() == 0) System.err.println("Cannot grow empty population!");
				else {
					int origSize=size();
					int k=0;
					while (size()< getTargetSize()) {
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
		setTargetSize(size());
	}

	/**
	 * Update the range of all individuals to the given one. If forceRange is true
	 * and the individuals are out of range, they are projected into the range
	 * by force.
	 * 
	 * @param modRange
	 * @param b
	 */
	public void updateRange(double[][] range, boolean forceRange) {
		for (int i=0; i<size(); i++) {
			((InterfaceDataTypeDouble)getEAIndividual(i)).SetDoubleRange(range);
			double[] pos = ((InterfaceDataTypeDouble)getEAIndividual(i)).getDoubleData();
			if (!Mathematics.isInRange(pos, range)) {
				Mathematics.projectToRange(pos, range);
				((InterfaceDataTypeDouble)getEAIndividual(i)).SetDoubleGenotype(pos);
			}
		}
	}

    public PopulationInitMethod getInitMethod() {
		return initMethod;
	}

	public void setInitMethod(PopulationInitMethod initMethod) {
		this.initMethod = initMethod;
		GenericObjectEditor.setShowProperty(this.getClass(), "initAround", initMethod==PopulationInitMethod.aroundSeed);
		GenericObjectEditor.setShowProperty(this.getClass(), "initPos", initMethod==PopulationInitMethod.aroundSeed);
		GenericObjectEditor.setShowProperty(this.getClass(), "seedCardinality", initMethod==PopulationInitMethod.binCardinality);
	}
	
	public String initMethodTipText() {
		return "Define the initial sampling method. Note that anything other than inidividualDefault will override the individual initialization concerning the positions in solution space.";
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
	/**
	 * Add the population data of a given population to this instance.
	 * Note that collisions are not checked!
	 */
	public void addDataFromPopulation(Population pop) {
		if (pop.additionalPopData!=null) {
			Set<String> keys = pop.additionalPopData.keySet();
			for (String key : keys) {
				Object earlierDat = this.getData(key);
				if (earlierDat!=null && !(earlierDat.equals(pop.getData(key)))) System.err.println("Warning: Population already contained data keyed by " + key + ", overwriting data " + earlierDat + " with " + pop.getData(key));
				this.putData(key, pop.getData(key));
			}
		}
	}

	/**
	 * Returns true if the given individual is member of this population,
	 * where the comparison is done by individual ID.
	 * 
	 * @param indy
	 * @return
	 */
	public boolean isMemberByID(AbstractEAIndividual indy) {
		for (int i=0; i<size(); i++) {
			if (getEAIndividual(i).getIndyID()==indy.getIndyID()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if the targeted size of the population has been reached.
	 */
	public boolean targetSizeReached() {
		return this.size()>=this.getTargetSize();
	}
	
	/**
	 * Return true if the targeted size of the population has been exceeded.
	 */
	public boolean targetSizeExceeded() {
		return this.size()>this.getTargetSize();
	}

	/**
	 * Return the number of free individual slots until the target size is reached.
	 * Returns zero if the target size is already reached or exceeded.
	 * 
	 * @return
	 */
	public int getFreeSlots() {
		return Math.max(0, this.getTargetSize() - this.size());
	}

	public boolean assertMembers(Population pop) {
		for (int i=0; i<pop.size(); i++) {
			if (!isMemberByID(pop.getEAIndividual(i))) {
				System.err.println("Warning, indy " + i + " is not a member of " + this);
				return false;
			}
		}
		return true;
	}

	/**
	 * Put an equal data object into every individual. Old data may be overwritten. 
	 * @param key
	 * @param obj
	 */
	public void putDataAllIndies(String key, Object obj) {
		for (int i=0; i<size(); i++) {
			getEAIndividual(i).putData(key, obj);
		}
	}

	/**
	 * Return true if the current instance is a subset of the given population, otherwise false.
	 * 
	 * @param offspring
	 * @return
	 */
	public boolean isSubSet(Population set) {
		Population filtered = this.filter(set); // if this is a subset of s, filtered must be empty. 
		return (filtered.size()==0);
	}

	/**
	 * Return the set of individuals which are members of both this and the other population.
	 * Uses the individuals equals predicate.
	 * 
	 * @param other
	 * @return
	 */
	public Population setCut(Population other) {
		Population cut = new Population();
		for (int i=0; i<size(); i++) {
			if (other.indexOf(getEAIndividual(i))>=0) cut.add(getEAIndividual(i));
		}
		return cut;
	}

	/**
	 * Copy all additional population data of the given instance
	 * into the current instance (by references only).
	 * 
	 * @param pop
	 */
	public void copyHashData(Population pop) {
		if (pop!=null && (pop.additionalPopData!=null)) {
			for (String key : pop.additionalPopData.keySet()) {
				Object origData = pop.getData(key);
				Object maybeClone = Serializer.deepClone(origData);
				if (maybeClone!=null) putData(key, maybeClone);
				else {
					System.err.println("Warning, additional pop data could not be cloned!");
					putData(key, origData);
				}
			}
		}
	}

	public void clearHistory() {
		if (m_History!=null) m_History.clear();
	}

	/**
	 * Checks if any individual entry is null. If so, false is returned, otherwise true.
	 * 
	 * @return
	 */
	public boolean checkNoNullIndy() {
		for (int i=0; i<size(); i++) {
			if (get(i)==null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Return a subset of (shallow cloned) individuals which have a lower (or equal) fitness in a given 
	 * criterion.
	 * 
	 * @param upperBound
	 * @param fitCrit
	 * @return
	 */
	public Population filterByFitness(double upperBound, int fitCrit) {
		Population res = this.cloneWithoutInds();
		for (int i=0; i<size(); i++) {
			if (getEAIndividual(i).getFitness(fitCrit)<=upperBound) res.add(get(i));
		}
		return res;
	}

	/**
	 * Return a clone of the best historic individual or null if the history is empty.
	 * 
	 * @return the best historic individual or null if the history is empty 
	 */
    public AbstractEAIndividual getBestHistoric() {
    	AbstractEAIndividual bestIndy = null;
    	if (getHistory()!=null) {
    		for (AbstractEAIndividual indy : getHistory()) {
    			if (bestIndy==null || (indy.isDominating(bestIndy))) bestIndy=indy;
    		}
    	}
    	return (bestIndy==null) ? null : (AbstractEAIndividual)bestIndy.clone();
	}

	public Pair<Integer, Integer> getSeedCardinality() {
		return seedCardinality;
	}
	public void setSeedCardinality(Pair<Integer, Integer> seedCardinality) {
		this.seedCardinality = seedCardinality;
	}
	public String seedCardinalityTipText() {
		return "The initial cardinality for binary genotype individuals, given as pair of mean and std.dev.";
	}
	
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
