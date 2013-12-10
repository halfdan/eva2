package eva2.optimization.individuals;


import eva2.optimization.individuals.codings.ga.GAStandardCodingInteger;
import eva2.optimization.individuals.codings.ga.InterfaceGAIntegerCoding;
import eva2.optimization.operator.crossover.CrossoverGAGINPoint;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateGANBit;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 * This individual uses a binary genotype to code for binary values using
 * two alternative encodings.
 */
@Description(value = "This is a GA individual suited to optimize int values.")
public class GAIndividualIntegerData extends AbstractEAIndividual implements InterfaceGAIndividual, InterfaceDataTypeInteger, java.io.Serializable {

    private int[] phenotype;
    private int[][] range;
    protected BitSet genotype;
    protected int[] codingLengths;
    private InterfaceGAIntegerCoding intergerCoding = new GAStandardCodingInteger();

    public GAIndividualIntegerData() {
        this.mutationProbability = 0.2;
        this.mutationOperator = new MutateGANBit();
        this.crossoverProbability = 0.7;
        this.crossoverOperator = new CrossoverGAGINPoint();
        this.range = new int[1][2];
        this.codingLengths = new int[1];
        this.codingLengths[0] = 3;
        this.range[0][0] = 0;
        this.range[0][1] = 7;
        this.genotype = new BitSet();
    }

    public GAIndividualIntegerData(GAIndividualIntegerData individual) {
        if (individual.phenotype != null) {
            this.phenotype = new int[individual.phenotype.length];
            System.arraycopy(individual.phenotype, 0, this.phenotype, 0, this.phenotype.length);
        }
        this.genotype = (BitSet) individual.genotype.clone();
        this.range = new int[individual.range.length][2];
        this.codingLengths = new int[individual.codingLengths.length];
        for (int i = 0; i < this.range.length; i++) {
            this.codingLengths[i] = individual.codingLengths[i];
            this.range[i][0] = individual.range[i][0];
            this.range[i][1] = individual.range[i][1];
        }

        // cloning the members of AbstractEAIndividual
        this.age = individual.age;
        this.crossoverOperator = individual.crossoverOperator;
        this.crossoverProbability = individual.crossoverProbability;
        this.mutationOperator = (InterfaceMutation) individual.mutationOperator.clone();
        this.mutationProbability = individual.mutationProbability;
        this.selectionProbability = new double[individual.selectionProbability.length];
        for (int i = 0; i < this.selectionProbability.length; i++) {
            this.selectionProbability[i] = individual.selectionProbability[i];
        }
        this.intergerCoding = individual.intergerCoding;
        this.fitness = new double[individual.fitness.length];
        for (int i = 0; i < this.fitness.length; i++) {
            this.fitness[i] = individual.fitness[i];
        }
        cloneAEAObjects((AbstractEAIndividual) individual);
    }

    @Override
    public Object clone() {
        return (Object) new GAIndividualIntegerData(this);
    }


    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAIndividualIntegerData) {
            GAIndividualIntegerData indy = (GAIndividualIntegerData) individual;
            //@todo Eigendlich k�nnte ich noch das Koding vergleichen
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            }
            if (!this.genotype.equals(indy.genotype)) {
                return false;
            }
            if (this.range.length != indy.range.length) {
                return false;
            }
            for (int i = 0; i < this.range.length; i++) {
                if (this.range[i][0] != indy.range[i][0]) {
                    return false;
                }
                if (this.range[i][1] != indy.range[i][1]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

/************************************************************************************
 * InterfaceDataTypeInteger methods
 */
    /**
     * This method allows you to request a certain amount of double data
     *
     * @param length The lenght of the double[] that is to be optimized
     */
    @Override
    public void setIntegerDataLength(int length) {
        int[] newDesPa = new int[length];
        int[][] newRange = new int[length][2];

        // copy the old values for the decision parameters and the range
        for (int i = 0; ((i < newDesPa.length) && (i < this.range.length)); i++) {
            newRange[i][0] = this.range[i][0];
            newRange[i][1] = this.range[i][1];
        }

        // if the new length is bigger than the last value fills the extra elements
        for (int i = this.range.length; (i < newDesPa.length); i++) {
            newRange[i][0] = this.range[this.range.length - 1][0];
            newRange[i][1] = this.range[this.range.length - 1][1];
        }
        this.range = newRange;
        this.codingLengths = new int[this.range.length];
        for (int i = 0; i < this.range.length; i++) {
            this.codingLengths[i] = this.intergerCoding.calculateNecessaryBits(this.range[i]);
        }
    }

    /**
     * This method returns the length of the double data set
     *
     * @return The number of bits stored
     */
    @Override
    public int size() {
        return this.range.length;
    }

    /**
     * This method will set the range of the integer attributes. If range.length
     * does not equal doubledata.length only range[i] will be used to set all
     * ranges.
     *
     * @param range The new range for the double data.
     */
    @Override
    public void setIntRange(int[][] range) {
        if (range.length != this.range.length) {
            System.out.println("Warning: Trying to set a range of length " + range.length + " to a vector of length "
                    + this.range.length + "!\n Use method setDoubleDataLength first!");
        }
        for (int i = 0; ((i < this.range.length) && (i < range.length)); i++) {
            this.range[i][0] = range[i][0];
            this.range[i][1] = range[i][1];
        }
        this.setIntegerDataLength(range.length);
    }

    /**
     * Set lower and upper integer range in all dimensions.
     *
     * @param lower
     * @param upper
     */
    public void SetIntRange(int lower, int upper) {
        for (int i = 0; i < range.length; i++) {
            setIntRange(i, lower, upper);
            codingLengths[i] = intergerCoding.calculateNecessaryBits(range[i]);
        }
    }

    /**
     * Set lower and upper integer range in a specific dimension.
     *
     * @param index
     * @param lower
     * @param upper
     */
    public void setIntRange(int index, int lower, int upper) {
        range[index][0] = lower;
        range[index][1] = upper;
        codingLengths[index] = intergerCoding.calculateNecessaryBits(range[index]);
    }

    /**
     * This method will return the range for all double attributes.
     *
     * @return The range array.
     */
    @Override
    public int[][] getIntRange() {
        return this.range;
    }

    /**
     * This method allows you to read the double data
     *
     * @return BitSet representing the double data.
     */
    @Override
    public int[] getIntegerData() {
        int[] locus = new int[2];
        locus[0] = 0;
        locus[1] = 0;
        this.phenotype = new int[this.range.length];
        for (int i = 0; i < this.phenotype.length; i++) {
            locus[0] += locus[1];
            locus[1] = this.codingLengths[i];
            this.phenotype[i] = this.intergerCoding.decodeValue(this.genotype, this.range[i], locus, false);
        }
        return this.phenotype;
    }

    /**
     * This method allows you to read the int data without
     * an update from the genotype
     *
     * @return int[] representing the int data.
     */
    @Override
    public int[] getIntegerDataWithoutUpdate() {
        return this.phenotype;
    }

    /**
     * This method allows you to set the double data.
     *
     * @param doubleData The new double data.
     */
    @Override
    public void setIntPhenotype(int[] doubleData) {
        this.phenotype = doubleData;
    }

    /**
     * This method allows you to set the double data, this can be used for
     * memetic algorithms.
     *
     * @param doubleData The new double data.
     */
    @Override
    public void setIntGenotype(int[] doubleData) {
        this.setIntPhenotype(doubleData);
        if (doubleData != null) {
            int[] locus = new int[2];
            locus[0] = 0;
            locus[1] = 0;
            for (int i = 0; i < doubleData.length; i++) {
                locus[0] += locus[1];
                locus[1] = this.codingLengths[i];
                this.intergerCoding.codeValue(doubleData[i], this.range[i], this.genotype, locus);
            }
        }
    }

/************************************************************************************
 * AbstractEAIndividual methods
 */
    /**
     * This method will init the individual with a given value for the
     * phenotype.
     *
     * @param obj The initial value for the phenotype
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof int[]) {
            int[] bs = (int[]) obj;
            if (bs.length != this.range.length) {
                System.out.println("Init value and requested length doesn't match!");
            }
            this.setIntGenotype(bs);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for GAIndividualDoubleData is not double[]!");
        }
        this.mutationOperator.init(this, opt);
        this.crossoverOperator.init(this, opt);
    }

    /**
     * This method will return a string description of the GAIndividal
     * noteably the Genotype.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        result += "GAIndividual coding int: (";
        result += "Fitness {";
        for (int i = 0; i < this.fitness.length; i++) {
            result += this.fitness[i] + ";";
        }
        result += "}/SelProb{";
        for (int i = 0; i < this.selectionProbability.length; i++) {
            result += this.selectionProbability[i] + ";";
        }
        result += "})\n Value: ";
        result += "[";
        int[] d = this.getIntegerData();
        for (int i = 0; i < d.length; i++) {
            result += d[i] + "; ";
        }
        result += "]\n";
        result += "CodingRange:  [";
        for (int i = 0; i < this.range.length; i++) {
            result += "(" + this.range[i][0] + "; " + this.range[i][1] + "); ";
        }
        result += "]\n";
        result += "CodingLength: [";
        for (int i = 0; i < this.codingLengths.length; i++) {
            result += this.codingLengths[i] + "; ";
        }
        result += "]\n";
        result += "{";
        int overallLength = 0;
        for (int i = 0; i < this.codingLengths.length; i++) {
            overallLength += this.codingLengths[i];
        }
        for (int i = 0; i < overallLength; i++) {
            if (this.genotype.get(i)) {
                result += "1";
            } else {
                result += "0";
            }
        }
        result += "}";
        return result;
    }

/************************************************************************************
 * InterfaceGAIndividual methods
 */

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
     * This may be necessary since BitSet.lentgh only returns the index
     * of the last significant bit.
     *
     * @return The length of the genotype.
     */
    @Override
    public int getGenotypeLength() {
        int overallLength = 0;
        for (int codingLength : this.codingLengths) {
            overallLength += codingLength;
        }
        return overallLength;
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        int overallLength = 0;
        for (int codingLength : this.codingLengths) {
            overallLength += codingLength;
        }
        for (int i = 0; i < overallLength; i++) {
            if (RNG.flipCoin(0.5)) {
                this.genotype.set(i);
            } else {
                this.genotype.clear(i);
            }
        }
    }

    /**
     * This method performs a simple one point mutation in the genotype
     */
    @Override
    public void defaultMutate() {
        int overallLength = 0;
        for (int codingLength : this.codingLengths) {
            overallLength += codingLength;
        }
        int mutationIndex = RNG.randomInt(0, overallLength);
        if (this.genotype.get(mutationIndex)) {
            this.genotype.clear(mutationIndex);
        } else {
            this.genotype.set(mutationIndex);
        }
    }

    public static void main(String[] args) {
        System.out.println("Test this stuff!");
        GAIndividualIntegerData indy = new GAIndividualIntegerData();
        int dimension = 10;
        int[][] range = new int[dimension][2];
        for (int i = 0; i < dimension; i++) {
            range[i][0] = 0;
            range[i][1] = i + 1;
        }
        indy.setIntegerDataLength(dimension);
        indy.setIntRange(range);
        indy.defaultInit(null);
        System.out.println("" + indy.getStringRepresentation());
        System.out.println("System.exit(0)");
        int[] data = indy.getIntegerData();
        String tmp = "Before {";
        for (int aData : data) {
            tmp += aData + "; ";
        }
        System.out.println(tmp + "}");
        tmp = "Setting {";
        for (int i = 0; i < data.length; i++) {
            data[i] = RNG.randomInt(range[i][0], range[i][1]);
            tmp += data[i] + "; ";
        }
        System.out.println(tmp + "}");
        indy.setIntGenotype(data);
        System.out.println("" + indy.getStringRepresentation());
        data = indy.getIntegerData();
        tmp = "After {";
        for (int i = 0; i < data.length; i++) {
            tmp += data[i] + "; ";
        }
        System.out.println(tmp + "}");
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "GA individual";
    }

    /**
     * This method allows you to set the Coding that is to be used, currently either standard binary
     * coding or Gray coding.
     *
     * @param coding The used genotype coding method
     */
    public void setGACoding(InterfaceGAIntegerCoding coding) {
        this.intergerCoding = coding;
    }

    public InterfaceGAIntegerCoding getGACoding() {
        return this.intergerCoding;
    }

    public String gAIntegerCodingTipText() {
        return "Choose the coding to use.";
    }
}
