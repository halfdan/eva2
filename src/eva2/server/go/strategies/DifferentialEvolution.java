package eva2.server.go.strategies;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.operators.selection.replacement.ReplacementCrowding;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractMultiObjectiveOptimizationProblem;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;
import eva2.tools.SelectedTag;
import eva2.tools.Tag;

/** Differential evolution implementing DE1 and DE2 following the paper of Storm and
 * Price and the Trigonometric DE published rectently, which doesn't really work that
 * well. Please note that DE will only work on real-valued genotypes and will ignore
 * all mutation and crossover operators selected.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 25.10.2004
 * Time: 14:06:56
 * To change this template use File | Settings | File Templates.
 */
public class DifferentialEvolution implements InterfaceOptimizer, java.io.Serializable {

    private Population                      m_Population        = new Population();
    private AbstractOptimizationProblem		m_Problem           = new F1Problem();
    private SelectedTag                     m_DEType;
    private double                          m_F                 = 0.8;
    private double                          m_k                 = 0.6;
    private double                          m_Lambda            = 0.6;
    private double                          m_Mt                = 0.05;
    private int								maximumAge			= -1;

    private String                          m_Identifier = "";
    transient private InterfacePopulationChangedEventListener m_Listener;

    /**
     * A constructor.
     *
     */
    public DifferentialEvolution() {
    	String[] deTypes = new String[] {"DE1 - DE/rand/1", "DE2 - DE/current-to-best/1", "DE/best/2", "Trigonometric DE"};
    	// sets DE2 as default
        m_DEType = new SelectedTag(1, deTypes);
    }

    /**
     * The copy constructor.
     * 
     * @param a
     */
    public DifferentialEvolution(DifferentialEvolution a) {
        if (a.m_DEType != null)
            this.m_DEType = (SelectedTag)a.m_DEType.clone();
        this.m_Population                   = (Population)a.m_Population.clone();
        this.m_Problem                      = (AbstractOptimizationProblem)a.m_Problem.clone();
        this.m_Identifier                   = a.m_Identifier;
        this.m_F                            = a.m_F;
        this.m_k                            = a.m_k;
        this.m_Lambda                       = a.m_Lambda;
        this.m_Mt                           = a.m_Mt;
    }

    public Object clone() {
        return (Object) new DifferentialEvolution(this);
    }

    public void init() {
        this.m_Problem.initPopulation(this.m_Population);
//        children = new Population(m_Population.size());
        this.evaluatePopulation(this.m_Population);
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will init the optimizer with a given population
     * @param pop       The initial population
     * @param reset     If true the population is reset.
     */
    public void initByPopulation(Population pop, boolean reset) {
        this.m_Population = (Population)pop.clone();
        if (reset) this.m_Population.init();
//        if (reset) this.m_Population.init();
//        else children = new Population(m_Population.size());
        this.evaluatePopulation(this.m_Population);
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }

    /** This method will evaluate the current population using the
     * given problem.
     * @param population The population that is to be evaluated
     */
    private void evaluatePopulation(Population population) {
        this.m_Problem.evaluate(population);
        population.incrGeneration();
    }

    /** 
     * This method return a difference vector between two random individuals from the population.
     * This method should make sure that the delta is not zero.
     * 
     * @param pop   The population to choose from
     * @return The delta vector
     */
    private double[] fetchDeltaRandom(Population pop) {
        double[]                x1, x2;
        double[]                result;
        boolean					isEmpty;
        int                     iterations = 0;

        x1 = getRandomGenotype(pop);
        
        result = new double[x1.length];
        isEmpty = true;
        while (isEmpty && (iterations < pop.size())) {
            x2 = getRandomGenotype(pop);
            
            for (int i = 0; i < x1.length; i++) {
                result[i] = x1[i] - x2[i];
                isEmpty = (isEmpty && (result[i]==0));
            }
            iterations++;
        }
        while (isEmpty) { // so now the hard way: construct a random vector
            for (int i = 0; i < x1.length; i++) {
                if (RNG.flipCoin(1/(double)x1.length))
                    result[i] = 0.01*RNG.gaussianDouble(0.1);
                else result[i] = 0;
                isEmpty = (isEmpty && (result[i]==0));
            }
        }

        return result;
    }

    /** 
     * This method will return the delta vector to the best individual
     * 
     * @param pop   The population to choose the best from
     * @param indy  The current individual
     * @return the delta vector
     */
    private double[] fetchDeltaBest(Population pop, InterfaceESIndividual indy) {
        double[]                x1, xb, result;
        x1 = indy.getDGenotype();
		result = new double[x1.length];
        if (m_Problem instanceof AbstractMultiObjectiveOptimizationProblem) {
        	// implements MODE for the multi-objective case: a dominating individual is selected for difference building
        	Population domSet = pop.getDominatingSet((AbstractEAIndividual)indy);
        	if (domSet.size() > 0) {
        		xb = getRandomGenotype(domSet);
        	} else {
        		return result; // just return a zero vector. this will happen automatically if domSet contains only the individual itself
        	}
        } else {
	        xb = getBestGenotype(pop);
        }

        for (int i = 0; i < x1.length; i++) {
            result[i] = xb[i] - x1[i];
        }
        
        return result;
    }

    /** This method returns two parents to the original individual
     * @param pop   The population to choose from
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

    /** This method will generate one new individual from the given population
     * @param pop   The current population
     * @return AbstractEAIndividual
     */
    public AbstractEAIndividual generateNewIndividual(Population pop) {
        AbstractEAIndividual        indy;
        InterfaceESIndividual       endy;
        try {
            indy = (AbstractEAIndividual)(pop.getEAIndividual(RNG.randomInt(0, pop.size()-1))).getClone();
            endy = (InterfaceESIndividual)indy;
        } catch (java.lang.ClassCastException e) {
            System.err.println("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
            return (AbstractEAIndividual)((AbstractEAIndividual)pop.get(RNG.randomInt(0, pop.size()-1))).getClone();
        }
        double[] nX, vX, oX;
        oX = endy.getDGenotype();
        vX = endy.getDGenotype();
        nX = new double[oX.length];
        switch (this.m_DEType.getSelectedTag().getID()) {
            case 0 : {
                // this is DE1 or DE/rand/1
                double[] delta = this.fetchDeltaRandom(pop);
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.m_F*delta[i];
                }
                break;
            }
            case 1 : {
                // this is DE2 or DE/current-to-best/1
                double[] rndDelta = this.fetchDeltaRandom(pop);
                double[] bestDelta = this.fetchDeltaBest(pop, endy);
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.m_Lambda * bestDelta[i] + this.m_F * rndDelta[i];
                }
                break;
            }
            case 2: {
            	// DE/best/2
            	oX = getBestGenotype(pop);
            	double[] delta1 = this.fetchDeltaRandom(pop);
            	double[] delta2 = this.fetchDeltaRandom(pop);
                for (int i = 0; i < oX.length; i++) {
                    vX[i] = oX[i] + this.m_F * (delta1[i] + delta2[i]);
                }            	
            	break;
            }
            case 3 : {
                // this is trigonometric mutation
                if (RNG.flipCoin(this.m_Mt)) {
                    double[]    xj, xk, xl; xj = oX;
                    double      p, pj, pk, pl;
                    InterfaceESIndividual indy1 = null, indy2 = null;
                    try {
                        // and i got indy!
                        indy1 = (InterfaceESIndividual)pop.get(RNG.randomInt(0, pop.size()-1));
                        indy2 = (InterfaceESIndividual)pop.get(RNG.randomInt(0, pop.size()-1));
                    } catch (java.lang.ClassCastException e) {
                        System.out.println("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
                    }
                    xk = indy1.getDGenotype();
                    xl = indy2.getDGenotype();
                    p = Math.abs(((AbstractEAIndividual)endy).getFitness(0)) + Math.abs(((AbstractEAIndividual)indy1).getFitness(0)) + Math.abs(((AbstractEAIndividual)indy2).getFitness(0));
                    pj = Math.abs(((AbstractEAIndividual)endy).getFitness(0))/p;
                    pk = Math.abs(((AbstractEAIndividual)indy1).getFitness(0))/p;
                    pl = Math.abs(((AbstractEAIndividual)indy2).getFitness(0))/p;
                    for (int i = 0; i < oX.length; i++) {
                        vX[i] = (xj[i] + xk[i] + xl[i])/3.0 + ((pk-pj)*(xj[i]-xk[i])) + ((pl-pk)*(xk[i]-xl[i])) + ((pj-pl)*(xl[i]-xj[i]));
                    }
                } else {
                    // this is DE1
                    double[] delta = this.fetchDeltaRandom(pop);
                    for (int i = 0; i < oX.length; i++) {
                        vX[i] = oX[i] + this.m_F*delta[i];
                    }
                }
                break;
            }
        }
        for (int i =0; i < oX.length; i++) {
            if (RNG.flipCoin(this.m_k)) {
                // it remains the same
                nX[i] = oX[i];
            } else {
                // it is altered
                nX[i] = vX[i];
            }
        }
        // setting the new genotype and fitness
        endy.SetDGenotype(nX);
        indy.SetAge(0);
        double[] fit = new double[1];
        fit[0] = 0;
        indy.SetFitness(fit);
        return indy;
    }


    private double[] getBestGenotype(Population pop) {
    	double[] xb;
        try {
            xb = ((InterfaceESIndividual)pop.getBestIndividual()).getDGenotype();
        } catch (java.lang.ClassCastException e) {
            System.err.println("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
            return new double[1];
        }
        return xb;
	}
    
    private double[] getRandomGenotype(Population pop) {
    	double[] x1;
	    try {
	        x1 = ((InterfaceESIndividual)pop.get(RNG.randomInt(0, pop.size()-1))).getDGenotype();
	    } catch (java.lang.ClassCastException e) {
	        System.err.println("Differential Evolution currently requires InterfaceESIndividual as basic data type!");
	        return new double[1];
	    }
	    return x1;
    }

	public void optimize() {
        AbstractEAIndividual    indy = null, org;
        int index;
        
        int nextDoomed = getNextDoomed(m_Population, 0);
        
        // required for dynamic problems especially
        m_Problem.evaluatePopulationStart(m_Population);
        /**
         * MK: added aging mechanism to provide for dynamically changing problems. If an individual
         * reaches the age limit, it is doomed and replaced by the next challenge vector, even if its worse.
         */
        
        for (int i = 0; i < this.m_Population.size(); i++) {
        	indy = this.generateNewIndividual(this.m_Population);
        	this.m_Problem.evaluate(indy);
        	this.m_Population.incrFunctionCalls();
        	if (nextDoomed >= 0) {	// this one is lucky, may replace an 'old' one
        		m_Population.replaceIndividualAt(nextDoomed, indy);
        		nextDoomed = getNextDoomed(m_Population, nextDoomed+1);	
        	} else {
            	if (m_Problem instanceof AbstractMultiObjectiveOptimizationProblem) {
					ReplacementCrowding repl = new ReplacementCrowding();
					repl.insertIndividual(indy, m_Population, null);
				} else {
					index   = RNG.randomInt(0, this.m_Population.size()-1);
					org     = (AbstractEAIndividual)this.m_Population.get(index);
					if (indy.isDominatingDebConstraints(org)) this.m_Population.replaceIndividualAt(index, indy);
				}
        	}
        }
        
//////// this was a non-steady-state-version
//        if (children==null) children = new Population(m_Population.size());
//        for (int i = 0; i < this.m_Population.size(); i++) {
//            indy = this.generateNewIndividual(this.m_Population);
//            this.m_Problem.evaluate(indy);
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
//	            // if (envHasChanged) this.m_Problem.evaluate(org);
//	            if (indy.isDominatingDebConstraints(org)) {
//	            	this.m_Population.replaceIndividualAt(index, indy);
//	            }
//        	}
//        }
//        children.clear();
//////// this was the original version
//        for (int i = 0; i < this.m_Population.size(); i++) {
//            indy = this.generateNewIndividual(this.m_Population);
//            this.m_Problem.evaluate(indy);
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
        this.firePropertyChangedEvent("NextGenerationPerformed");
    }
    
    /**
     * Search for the first individual which is older than the age limit and return its index.
     * If there is no age limit or  all individuals are younger, -1 is returned. The start index
     * of the search may be provided to make iterative search efficient.
     *
     * @param pop	Population to search
     * @param startIndex	index to start the search from
     * @return	index of an overaged individual or -1
     */
    protected int getNextDoomed(Population pop, int startIndex) {
    	if (maximumAge > 0) {
    		for (int i=startIndex; i<pop.size(); i++) {
    			if (((AbstractEAIndividual)pop.get(i)).getAge() >= maximumAge) return i;
    		}
    	}
    	return -1;
    }
    
    /** This method allows you to add the LectureGUI as listener to the Optimizer
     * @param ea
     */
    public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
        this.m_Listener = ea;
    }
    /** Something has changed
     * @param name
     */
    protected void firePropertyChangedEvent (String name) {
        if (this.m_Listener != null) this.m_Listener.registerPopulationStateChanged(this, name);
    }

    /** This method will set the problem that is to be optimized
     * @param problem
     */
    public void SetProblem (InterfaceOptimizationProblem problem) {
        this.m_Problem = (AbstractOptimizationProblem)problem;
    }
    public InterfaceOptimizationProblem getProblem () {
        return (InterfaceOptimizationProblem)this.m_Problem;
    }

    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "Differential Evolution:\n";
        result += "Optimization Problem: ";
        result += this.m_Problem.getStringRepresentationForProblem(this) +"\n";
        result += this.m_Population.getStringRepresentation();
        return result;
    }
    /** This method allows you to set an identifier for the algorithm
     * @param name      The indenifier
     */
     public void SetIdentifier(String name) {
        this.m_Identifier = name;
    }
     public String getIdentifier() {
         return this.m_Identifier;
     }

    /** This method is required to free the memory on a RMIServer,
     * but there is nothing to implement.
     */
    public void freeWilly() {

    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Differential Evolution using a steady-state population scheme.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "DE";
    }

    /** Assuming that all optimizer will store thier data in a population
     * we will allow acess to this population to query to current state
     * of the optimizer.
     * @return The population of current solutions to a given problem.
     */
    public Population getPopulation() {
        return this.m_Population;
    }
    public void setPopulation(Population pop){
        this.m_Population = pop;
    }
    public String populationTipText() {
        return "Edit the properties of the population used.";
    }
    
    public Population getAllSolutions() {
    	return getPopulation();
    }

    /** F is a real and constant factor which controls the amplification of the differential variation
     * @param f
     */
    public void setF (double f) {
        this.m_F = f;
    }
    public double getF() {
        return this.m_F;
    }
    public String fTipText() {
        return "F is a real and constant factor which controls the amplification of the differential variation.";
    }

    /** Probability of alteration through DE (something like a discrete uniform crossover is performed here)
     * @param k
     */
    public void setK(double k) {
        if (k < 0) k = 0;
        if (k > 1) k = 1;        
        this.m_k = k;
    }
    public double getK() {
        return this.m_k;
    }
    public String kTipText() {
        return "Probability of alteration through DE (something like a discrete uniform crossover is performed here).";
    }

    /** Enhance greediness through amplification of the differential vector to the best individual for DE2
     * @param l
     */
    public void setLambda (double l) {
        this.m_Lambda = l;
    }
    public double getLambda() {
        return this.m_Lambda;
    }
    public String lambdaTipText() {
        return "Enhance greediness through amplification of the differential vector to the best individual for DE2.";
    }

    /** In case of trig. mutation DE, the TMO is applied wit probability Mt
     * @param l
     */
    public void setMt (double l) {
        this.m_Mt = l;
        if (this.m_Mt < 0) this.m_Mt = 0;
        if (this.m_Mt > 1) this.m_Mt = 1;
    }
    public double getMt() {
        return this.m_Mt;
    }
    public String mtTipText() {
        return "In case of trig. mutation DE, the TMO is applied wit probability Mt.";
    }

    /** This method allows you to choose the type of Differential Evolution.
     * @param s  The type.
     */
    public void setDEType(SelectedTag s) {
        this.m_DEType = s;
    }
    public SelectedTag getDEType() {
        return this.m_DEType;
    }
    public String dETypeTipText() {
        return "Choose the type of Differential Evolution.";
    }

	/**
	 * @return the maximumAge
	 **/
	public int getMaximumAge() {
		return maximumAge;
	}

	/**
	 * @param maximumAge the maximumAge to set
	 **/
	public void setMaximumAge(int maximumAge) {
		this.maximumAge = maximumAge;
	}
	
	public String maximumAgeTipText() {
		return "The maximum age of individuals, older ones are discarded. Set to -1 (or 0) to deactivate";
	}
}