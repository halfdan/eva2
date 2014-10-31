package eva2.optimization.individuals;


import eva2.optimization.operator.crossover.CrossoverGAGINPoint;
import eva2.optimization.operator.crossover.InterfaceCrossover;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateGANBit;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 * This individual uses a binary genotype to code for binary values.
 */
@Description(value = "This is a GA individual suited to optimize binary values.")
public class GAIndividualBinaryData extends AbstractEAIndividual implements InterfaceDataTypeBinary, InterfaceGAIndividual, java.io.Serializable {

    protected BitSet genotype = new BitSet();
    protected BitSet phenotype = new BitSet();
    protected int genotypeLength;

    public GAIndividualBinaryData() {
        this.mutationProbability = 0.1;
        this.mutationOperator = new MutateGANBit();
        this.crossoverProbability = 1.0;
        this.crossoverOperator = new CrossoverGAGINPoint();
        this.genotypeLength = 20;
        this.genotype = new BitSet();
    }

    public GAIndividualBinaryData(int genotypeLen) {
        this();
        this.setBinaryDataLength(genotypeLen);
    }

    public GAIndividualBinaryData(GAIndividualBinaryData individual) {
        if (individual.phenotype != null) {
            this.phenotype = (BitSet) individual.phenotype.clone();
        }
        this.genotypeLength = individual.genotypeLength;
        if (individual.genotype != null) {
            this.genotype = (BitSet) individual.genotype.clone();
        }

        // cloning the members of AbstractEAIndividual
        this.age = individual.age;
        this.crossoverOperator = (InterfaceCrossover) individual.crossoverOperator.clone();
        this.crossoverProbability = individual.crossoverProbability;
        this.mutationOperator = (InterfaceMutation) individual.mutationOperator.clone();
        this.mutationProbability = individual.mutationProbability;
        this.selectionProbability = new double[individual.selectionProbability.length];
        System.arraycopy(individual.selectionProbability, 0, this.selectionProbability, 0, this.selectionProbability.length);
        this.fitness = new double[individual.fitness.length];
        System.arraycopy(individual.fitness, 0, this.fitness, 0, this.fitness.length);
        cloneAEAObjects(individual);
    }

    @Override
    public Object clone() {
        return new GAIndividualBinaryData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAIndividualBinaryData) {
            GAIndividualBinaryData indy = (GAIndividualBinaryData) individual;
            if (this.genotypeLength != indy.genotypeLength) {
                return false;
            }
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            }
            if (!this.genotype.equals(indy.genotype)) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method evaluates the GAIndividual as eva2.problems.simple minimize number
     * of bits problem.
     *
     * @return The number of true bits
     */
    public double defaultEvaulateAsMiniBits() {
        double result = 0;
        for (int i = 0; i < this.genotypeLength; i++) {
            if (this.genotype.get(i)) {
                result++;
            }
        }
        return result;
    }

/************************************************************************************
 * AbstractEAIndividual methods
 */
    /**
     * This method will initialize the individual with a given value for the
     * phenotype.
     *
     * @param obj The initial value for the phenotype
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof BitSet) {
            BitSet bs = (BitSet) obj;
            this.setBinaryGenotype(bs);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for GAIndividualBinaryData is no BitSet!");
        }
        this.mutationOperator.initialize(this, opt);
        this.crossoverOperator.init(this, opt);
    }

    /**
     * This method can be used to read the current fitness of the individual.
     * Please note that the fitness can be based on multiple criteria therefore
     * double[] is used instead of a single double.
     *
     * @return The complete fitness array
     */
    @Override
    public double[] getFitness() {
        return this.fitness;
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
        result += "GAIndividual: (";
        result += "Fitness {";
        for (int i = 0; i < this.fitness.length; i++) {
            result += this.fitness[i] + ";";
        }
        result += "}/SelProb{";
        for (int i = 0; i < this.selectionProbability.length; i++) {
            result += this.selectionProbability[i] + ";";
        }
        result += "})\n Value: ";
        result += "{";
        for (int i = 0; i < this.genotypeLength; i++) {
            if (i % 8 == 0) {
                result += "|";
            }
            if (this.genotype.get(i)) {
                result += "1";
            } else {
                result += "0";
            }
        }
        result += "}";
        result += "\n Mutation (" + this.mutationProbability + "):" + this.mutationOperator.getStringRepresentation();
        return result;
    }

    /**
     * This method allows you to read the binary data
     *
     * @return BitSet representing the binary data.
     */
    @Override
    public BitSet getBGenotype() {
        return this.genotype;
    }

    /**
     * This method allows you to set the binary data, this can be used for
     * memetic algorithms.
     *
     * @param binaryData The new binary data.
     */
    @Override
    public void setBGenotype(BitSet binaryData) {
        this.genotype = binaryData;
    }

    /**
     * This method allows the user to read the length of the genotype.
     * This may be necessary since BitSet.length only returns the index
     * of the last significant bit.
     *
     * @return The length of the genotype.
     */
    @Override
    public int getGenotypeLength() {
        return this.genotypeLength;
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        for (int i = 0; i < this.genotypeLength; i++) {
            if (RNG.flipCoin(0.5)) {
                this.genotype.set(i);
            } else {
                this.genotype.clear(i);
            }
        }
    }

    /**
     * This method performs a eva2.problems.simple one point mutation in the genotype
     */
    @Override
    public void defaultMutate() {
        int mutationIndex = RNG.randomInt(0, this.genotypeLength);
        //if (mutationIndex > 28) System.out.println("Mutate: " + this.getSolutionRepresentationFor());
        if (this.genotype.get(mutationIndex)) {
            this.genotype.clear(mutationIndex);
        } else {
            this.genotype.set(mutationIndex);
        }
        //if (mutationIndex > 28) System.out.println(this.getSolutionRepresentationFor());
    }

/************************************************************************************
 * InterfaceDataTypeBinary methods
 */
    /**
     * This method allows you to request a certain amount of binary data
     *
     * @param length The lenght of the BitSet that is to be optimized
     */
    @Override
    public void setBinaryDataLength(int length) {
        this.genotypeLength = length;
    }

    /**
     * This method returns the length of the binary data set
     *
     * @return The number of bits stored
     */
    @Override
    public int size() {
        return this.genotypeLength;
    }

    /**
     * This method allows you to read the binary data
     *
     * @return BitSet representing the binary data.
     */
    @Override
    public BitSet getBinaryData() {
        this.phenotype = (BitSet) this.genotype.clone();
        return this.phenotype;
    }

    /**
     * This method allows you to read the binary data without
     * an update from the genotype
     *
     * @return BitSet representing the binary data.
     */
    @Override
    public BitSet getBinaryDataWithoutUpdate() {
        return this.phenotype;
    }

    /**
     * This method allows you to set the binary data.
     *
     * @param binaryData The new binary data.
     */
    @Override
    public void setBinaryPhenotype(BitSet binaryData) {
        this.phenotype = binaryData;
    }

    /**
     * This method allows you to set the binary data, this can be used for
     * memetic algorithms.
     *
     * @param binaryData The new binary data.
     */
    @Override
    public void setBinaryGenotype(BitSet binaryData) {
        this.setBinaryPhenotype(binaryData);
        this.genotype = (BitSet) binaryData.clone();
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "GA binary individual";
    }
}
