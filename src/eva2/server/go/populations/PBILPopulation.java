package eva2.server.go.populations;


import java.util.BitSet;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.tools.math.RNG;

/** This implementation of Population Based Incremental Learning is only
 * suited for a BitString based genotyp represenation.
 * Copyright:       Copyright (c) 2003
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */

public class PBILPopulation extends Population implements Cloneable, java.io.Serializable {

    private double[]    m_ProbabilityVector = new double[1];

    public PBILPopulation() {
    }

    public PBILPopulation(int popSize) {
    	super(popSize);
    }
    
    public PBILPopulation(PBILPopulation population) {
    	super(population);

        this.m_ProbabilityVector = new double[population.m_ProbabilityVector.length];
        for (int i = 0; i < this.m_ProbabilityVector.length; i++) {
            this.m_ProbabilityVector[i] = population.m_ProbabilityVector[i];
        }
    }

    public Object clone() {
        return (Object) new PBILPopulation(this);
    }

    /** This method inits the state of the population AFTER the individuals
     * have been inited by a problem
     */
    public void init() {
        this.m_Generation       = 0;
        this.m_FunctionCalls    = 0;
        if (!(this.get(0) instanceof InterfaceGAIndividual)) {
            System.err.println("Members of the population are not instance of InterfaceGAIndividual!");
            return;
        }
        this.m_ProbabilityVector = new double[((InterfaceGAIndividual)this.get(0)).getGenotypeLength()];
        for (int i = 0; i < this.m_ProbabilityVector.length; i++) {
            this.m_ProbabilityVector[i] = 0.5;
        }
    }

    /** This method allows you to learn from several examples
     * @param examples  A population of examples.
     * @param learnRate The learning rate.
     */
    public void learnFrom (Population examples, double learnRate) {
        InterfaceGAIndividual tmpIndy;
        BitSet      tmpBitSet;
        
        for (int i = 0; i < examples.size(); i++) {
            tmpIndy = (InterfaceGAIndividual)(examples.getEAIndividual(i)).clone();
            tmpBitSet = tmpIndy.getBGenotype();
            for (int j = 0; j < this.m_ProbabilityVector.length; j++) {
                this.m_ProbabilityVector[j] = this.m_ProbabilityVector[j] * (1.0 - learnRate);
                if (tmpBitSet.get(j)) this.m_ProbabilityVector[j] += learnRate;
            }
        }
    }

    /** This method creates a new population based on the bit probability vector
     */
    public void initPBIL() {
        InterfaceGAIndividual   tmpIndy, template = (InterfaceGAIndividual)((AbstractEAIndividual)this.get(0)).clone();
        BitSet                  tmpBitSet;

        this.clear();
        for (int i = 0; i < this.getTargetSize(); i++) {
            tmpIndy = (InterfaceGAIndividual)((AbstractEAIndividual)template).clone();
            tmpBitSet = tmpIndy.getBGenotype();
            for (int j = 0; j < this.m_ProbabilityVector.length; j++) {
                if (RNG.flipCoin(this.m_ProbabilityVector[j])) tmpBitSet.set(j);
                else tmpBitSet.clear(j);
            }
            tmpIndy.SetBGenotype(tmpBitSet);
            super.add(tmpIndy);
        }
    }

    /** This method allows you to mutate the bit probability vector
     * @param mutationRate      The mutation rate.
     */
    public void mutateProbabilityVector(double mutationRate, double sigma) {
        for (int j = 0; j < this.m_ProbabilityVector.length; j++) {
            if (RNG.flipCoin(mutationRate)) this.m_ProbabilityVector[j] += RNG.gaussianDouble(sigma);
            if (this.m_ProbabilityVector[j] > 1) this.m_ProbabilityVector[j] = 1;
            if (this.m_ProbabilityVector[j] < 0) this.m_ProbabilityVector[j] = 0;
        }
    }

    /** This method will build a probability vector from the current population
     */
    public void buildProbabilityVector() {
        int     dim = ((InterfaceGAIndividual)this.get(0)).getGenotypeLength();
        BitSet  tmpSet;

        this.m_ProbabilityVector = new double[dim];
        for (int i = 0; i < this.m_ProbabilityVector.length; i++) this.m_ProbabilityVector[i] = 0;
        // first count the true bits
        for (int i = 0; i < this.size(); i++) {
            tmpSet = ((InterfaceGAIndividual)this.get(i)).getBGenotype();
            for (int j = 0; j < dim; j++) {
                if (tmpSet.get(j)) this.m_ProbabilityVector[j] += 1;
            }
        }
        // now normalize
        for (int i = 0; i < dim; i++) {
            this.m_ProbabilityVector[i] = this.m_ProbabilityVector[i]/this.size();
        }
    }

    /** This method allows you to set the current probability vector.
     * @param pv    The new probability vector.
     */
    public void SetProbabilityVector(double[] pv) {
        this.m_ProbabilityVector = pv;
    }
    public double[] getProbabilityVector() {
        return this.m_ProbabilityVector;
    }

    /** This method will return a string description of the GAIndividal
     * noteably the Genotype.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "PBIL-Population:\n";
        result += "Probability vector: {";
        for (int i = 0; i < this.m_ProbabilityVector.length; i++) {
            result += this.m_ProbabilityVector[i] +"; ";
        }
        result += "}\n";
        result += "Population size: " + this.size() + "\n";
        result += "Function calls : " + this.m_FunctionCalls + "\n";
        result += "Generations    : " + this.m_Generation;
        //for (int i = 0; i < this.size(); i++) {
            //result += ((AbstractEAIndividual)this.get(i)).getSolutionRepresentationFor()+"\n";
        //}
        return result;
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a PBIL-population, using a probability vector for Bit-String based individuals.";
    }
}