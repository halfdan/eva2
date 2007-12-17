package javaeva.server.go.populations;

import javaeva.server.go.IndividualInterface;
import javaeva.server.go.PopulationInterface;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.individuals.GAIndividualBinaryData;
import javaeva.server.go.operators.distancemetric.PhenotypeMetric;
import javaeva.server.go.tools.RandomNumberGenerator;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

/** This is a basic implementation for a EA Population.
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 307 $
 *            $Date: 2007-12-04 14:31:47 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public class Population extends ArrayList implements PopulationInterface, Cloneable, java.io.Serializable {

    protected int           m_Generation    = 0;
    protected int           m_FunctionCalls = 0;
    protected int           m_Size          = 50;
    protected Population    m_Archive       = null;

    public ArrayList        m_History       = new ArrayList();

    public Population() {
    }
    
    public Population(int initialCapacity) {
    	super(initialCapacity);
    }

    public Population(Population population) {
        this.m_Generation       = population.m_Generation;
        this.m_FunctionCalls    = population.m_FunctionCalls;
        this.m_Size             = population.m_Size;
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i) != null)
                this.add((((AbstractEAIndividual)population.get(i))).clone());
        }
        if (population.m_Archive != null) this.m_Archive = (Population)population.m_Archive.clone();
        if (population.m_History != null) this.m_History = (ArrayList)population.m_History.clone();
    }

    public Object clone() {
        return (Object) new Population(this);
    }

    /** This method inits the state of the population AFTER the individuals
     * have been inited by a problem
     */
    public void init() {
        this.m_History = new ArrayList();
        this.m_Generation       = 0;
        this.m_FunctionCalls    = 0;
        if (this.m_Archive != null) {
            this.m_Archive.clear();
            this.m_Archive.init();
        }
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

    /** This method will allow cou to increment the current number of function calls.
     */
    public void incrFunctionCalls() {
        this.m_FunctionCalls++;
    }
    /** This method will allow cou to increment the current number of function calls.
     * @param d     The number of function calls to increment.
     */
    public void incrFunctionCallsby(int d) {
        this.m_FunctionCalls += d;
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

    /** This method allows you to increment the current number of generations.
     * This will be the trigger for the Population, that has moved from t to t+1.
     * Here overaged Individuals can be removed. The best of class can be identified.
     * Stagnation measured etc. pp.
     */
    public void incrGeneration() {
        if (this.size() >= 1) this.m_History.add(this.getBestEAIndividual());
        for (int i=0; i<size(); i++) ((AbstractEAIndividual)get(i)).incrAge(); 
        this.m_Generation++;
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
     * Resets the fitness to the maximum possible value for the given individual.
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

	/** This method will return the index of the current best individual from the
     * population.
     * @return The index of the best individual.
     */
    public int getIndexOfBestIndividual() {
        int     result          = -1;
        double  curBestFitness  = Double.POSITIVE_INFINITY;

        for (int i = 0; i < super.size(); i++) {
            if ((!((AbstractEAIndividual)super.get(i)).violatesConstraint()) && (((AbstractEAIndividual)super.get(i)).getFitness(0) < curBestFitness)) {
                result          = i;
                curBestFitness  = ((AbstractEAIndividual)super.get(i)).getFitness(0);
            }
        }
        if (result == -1) {
            // darn all seem to violate the constraint
            // so lets search for the guy who is close to feasible
            for (int i = 0; i < super.size(); i++) {
                if ((((AbstractEAIndividual)super.get(i)).getConstraintViolation() < curBestFitness)) {
                    result          = i;
                    curBestFitness  = ((AbstractEAIndividual)super.get(i)).getConstraintViolation();
                }
            }
            System.out.println("Population reports: All individuals violate the constraints, choosing smallest constraint violation.");
        }
        return result;
    }

    /** This method returns the current best individual from the population
     * @return The best individual
     */
    public AbstractEAIndividual getBestEAIndividual() {
        int best = this.getIndexOfBestIndividual();
        if (best == -1) best = 0;
        AbstractEAIndividual result = (AbstractEAIndividual)this.get(best);
        if (result == null) System.err.println("Serious Problem! Population Size: " + this.size());
        return result;
    }

    /** This method returns the n current best individuals from the population in an object array.
     * 
     * @param n	number of individuals to look out for
     * @return The n best individuals
     * 
     */
    public Object[] getBestNIndividuals(int n) {
    	LinkedList indList = new LinkedList();
    	PriorityQueue queue = new PriorityQueue(n);
    	AbstractEAIndividual indy;
        double  curNBestFitness  = Double.POSITIVE_INFINITY;

        for (int i = 0; i < super.size(); i++) {
        	indy = (AbstractEAIndividual)super.get(i);
            if ((!indy.violatesConstraint()) && (indy.getFitness(0) < curNBestFitness)) {
                if (indList.size() >= n) {
                	indList.removeLast();
                	queue.remove();
                }
                indList.addFirst(super.get(i));
                // use negative fitness, because queue orders the smallest to top.
                queue.add(new Double(- indy.getFitness(0)));
                if (indList.size() == n) curNBestFitness  = - ((Double)queue.peek()).doubleValue();
            }
        }
        return indList.toArray();
    }

    
    /** This method returns the currently worst individual from the population
     * @return The best individual
     */
    public AbstractEAIndividual getWorstEAIndividual() {
        AbstractEAIndividual result = null;
        double curBestFitness = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < super.size(); i++) {
            //System.out.println("Fitness " + i + " " + ((AbstractEAIndividual)super.get(i)).getFitness(0));
            if (((AbstractEAIndividual)super.get(i)).getFitness(0) > curBestFitness) {
                result = (AbstractEAIndividual)super.get(i);
                curBestFitness = result.getFitness(0);
            }
        }
        if (result == null) {
            result = ((AbstractEAIndividual)super.get(0));
            if (result == null) System.out.println("Serious Problem! Population Size: " + this.size());
        }
        return result;
    }

    /** This method will remove N individuals from the population
     * Note: the current strategy will be ro remove N individuals
     * at random but later a special heuristic could be introduced.
     * @param n     The number of individuals for be removed
     */
    public void removeNIndividuals(int n) {
        for (int i = 0; i < n; i++) {
            this.remove(RandomNumberGenerator.randomInt(0, this.size()-1));
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

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "A population stores the individuals of a generation.";
    }

    /** This method allows you to set the population size
     * @param size
     */
    public void setPopulationSize(int size) {
        this.m_Size = size;
//        int rand;
//        if (this.size() != 0) {
//            while (this.size() < size) {
//                rand = RandomNumberGenerator.randomInt(0, this.size()-1);
//                this.add(((AbstractEAIndividual)this.get(rand)).clone());
//            }
//            while (this.size() > size) {
//                rand = RandomNumberGenerator.randomInt(0, this.size()-1);
//                this.remove(rand);
//            }
//        }
//        System.out.println("This.size() = "+this.size() +" this.getSize() = " + this.m_Size);
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
     * Returns the average, minimal and maximal phenotypic individual distance as diversity measure for the populuation.
     *
     * @return the average, minimal and maximal mean distance of individuals in an array of three
     */
    public double[] getPopulationMeasures() {
    	PhenotypeMetric metric = new PhenotypeMetric();
    	
    	double d;
    	double[] res = new double[3];
    	
    	double meanDist = 0.;
    	double maxDist = Double.MIN_VALUE;
    	double minDist = Double.MAX_VALUE;
    	
        for (int i = 0; i < this.size(); i++) {
        	for (int j = i+1; j < this.size(); j++) {
                d = metric.distance((AbstractEAIndividual)this.get(i), (AbstractEAIndividual)this.get(j));
                meanDist += d;
                if (d < minDist) minDist = d;
                if (d > maxDist) maxDist = d;
            }
        }
        res[0] = meanDist / (this.size() * (this.size()-1) / 2);
        res[1] = minDist;
        res[2] = maxDist;
        
        return res;
    }
    
    public int getGenerations() {
        return this.m_Generation;
    }
}
