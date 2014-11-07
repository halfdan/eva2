package eva2.optimization.population;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 * This implementation of Population Based Incremental Learning is only
 * suited for a BitString based genotype representation.
 */
@Description("This is a PBIL-population, using a probability vector for Bit-String based individuals.")
public class PBILPopulation extends Population implements Cloneable, java.io.Serializable {

    private double[] probabilityVector = new double[1];

    public PBILPopulation() {
    }

    public PBILPopulation(int popSize) {
        super(popSize);
    }

    public PBILPopulation(PBILPopulation population) {
        super(population);

        this.probabilityVector = new double[population.probabilityVector.length];
        System.arraycopy(population.probabilityVector, 0, this.probabilityVector, 0, this.probabilityVector.length);
    }

    @Override
    public Object clone() {
        return new PBILPopulation(this);
    }

    /**
     * This method inits the state of the population AFTER the individuals
     * have been inited by a problem.
     */
    @Override
    public void initialize() {
        this.generationCount = 0;
        this.functionCallCount = 0;
        if (!(this.get(0) instanceof InterfaceGAIndividual)) {
            System.err.println("Members of the population are not instance of InterfaceGAIndividual!");
            return;
        }
        this.probabilityVector = new double[((InterfaceGAIndividual) this.get(0)).getGenotypeLength()];
        for (int i = 0; i < this.probabilityVector.length; i++) {
            this.probabilityVector[i] = 0.5;
        }
    }

    /**
     * This method allows you to learn from several examples
     *
     * @param examples  A population of examples.
     * @param learnRate The learning rate.
     */
    public void learnFrom(Population examples, double learnRate) {
        InterfaceGAIndividual tmpIndy;
        BitSet tmpBitSet;

        for (int i = 0; i < examples.size(); i++) {
            tmpIndy = (InterfaceGAIndividual) (examples.getEAIndividual(i)).clone();
            tmpBitSet = tmpIndy.getBGenotype();
            for (int j = 0; j < this.probabilityVector.length; j++) {
                this.probabilityVector[j] *= (1.0 - learnRate);
                if (tmpBitSet.get(j)) {
                    this.probabilityVector[j] += learnRate;
                }
            }
        }
    }

    /**
     * This method creates a new population based on the bit probability vector.
     */
    public void initPBIL() {
        InterfaceGAIndividual tmpIndy, template = (InterfaceGAIndividual) this.get(0).clone();
        BitSet tmpBitSet;

        this.clear();
        for (int i = 0; i < this.getTargetSize(); i++) {
            tmpIndy = (InterfaceGAIndividual) ((AbstractEAIndividual) template).clone();
            tmpBitSet = tmpIndy.getBGenotype();
            for (int j = 0; j < this.probabilityVector.length; j++) {
                if (RNG.flipCoin(this.probabilityVector[j])) {
                    tmpBitSet.set(j);
                } else {
                    tmpBitSet.clear(j);
                }
            }
            tmpIndy.setBGenotype(tmpBitSet);
            super.add((AbstractEAIndividual) tmpIndy);
        }
    }

    /**
     * This method allows you to mutate the bit probability vector
     *
     * @param mutationRate The mutation rate.
     */
    public void mutateProbabilityVector(double mutationRate, double sigma) {
        for (int j = 0; j < this.probabilityVector.length; j++) {
            if (RNG.flipCoin(mutationRate)) {
                this.probabilityVector[j] += RNG.gaussianDouble(sigma);
            }
            if (this.probabilityVector[j] > 1) {
                this.probabilityVector[j] = 1;
            }
            if (this.probabilityVector[j] < 0) {
                this.probabilityVector[j] = 0;
            }
        }
    }

    /**
     * This method will build a probability vector from the current population.
     */
    public void buildProbabilityVector() {
        int dim = ((InterfaceGAIndividual) this.get(0)).getGenotypeLength();
        BitSet tmpSet;

        this.probabilityVector = new double[dim];
        for (int i = 0; i < this.probabilityVector.length; i++) {
            this.probabilityVector[i] = 0;
        }
        // first count the true bits
        for (int i = 0; i < this.size(); i++) {
            tmpSet = ((InterfaceGAIndividual) this.get(i)).getBGenotype();
            for (int j = 0; j < dim; j++) {
                if (tmpSet.get(j)) {
                    this.probabilityVector[j] += 1;
                }
            }
        }
        // now normalize
        for (int i = 0; i < dim; i++) {
            this.probabilityVector[i] /= this.size();
        }
    }

    /**
     * This method allows you to set the current probability vector.
     *
     * @param pv The new probability vector.
     */
    public void setProbabilityVector(double[] pv) {
        this.probabilityVector = pv;
    }

    public double[] getProbabilityVector() {
        return this.probabilityVector;
    }

    /**
     * This method will return a string description of the GAIndividal
     * notably the Genotype.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        result += "PBIL-Population:\n";
        result += "Probability vector: {";
        for (int i = 0; i < this.probabilityVector.length; i++) {
            result += this.probabilityVector[i] + "; ";
        }
        result += "}\n";
        result += "Population size: " + this.size() + "\n";
        result += "Function calls : " + this.functionCallCount + "\n";
        result += "Generations    : " + this.generationCount;
        //for (int i = 0; i < this.size(); i++) {
        //result += ((AbstractEAIndividual)this.get(i)).getSolutionRepresentationFor()+"\n";
        //}
        return result;
    }
}