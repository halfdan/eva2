package eva2.optimization.individuals;


import eva2.optimization.individuals.codings.ga.GAStandardCodingDouble;
import eva2.optimization.individuals.codings.ga.InterfaceGADoubleCoding;
import eva2.optimization.operator.crossover.CrossoverGAGINPoint;
import eva2.optimization.operator.crossover.InterfaceCrossover;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateGAUniform;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 * This individual uses a binary genotype to code for double values
 * using two alternative encodings.
 */
@Description(value = "This is a GA individual suited to optimize double values.")
public class GAIndividualDoubleData extends AbstractEAIndividual implements InterfaceGAIndividual, InterfaceDataTypeDouble, java.io.Serializable {

    private double[] phenotype;
    private double[][] initializationRange;
    protected BitSet genotype;
    protected int genotypeLength;
    private int precision = 32;
    private InterfaceGADoubleCoding doubleCoding = new GAStandardCodingDouble();

    public GAIndividualDoubleData() {
        this.mutationProbability = 0.1;
        this.mutationOperator = new MutateGAUniform();
        this.crossoverProbability = 0.7;
        this.crossoverOperator = new CrossoverGAGINPoint();
        this.initializationRange = new double[1][2];
        this.initializationRange[0][0] = -10;
        this.initializationRange[0][1] = 10;
        this.genotypeLength = this.precision;
        this.genotype = new BitSet();
    }

    public GAIndividualDoubleData(GAIndividualDoubleData individual) {
        if (individual.phenotype != null) {
            this.phenotype = new double[individual.phenotype.length];
            System.arraycopy(individual.phenotype, 0, this.phenotype, 0, this.phenotype.length);
        }
        this.genotypeLength = individual.genotypeLength;
        this.genotype = (BitSet) individual.genotype.clone();
        this.initializationRange = new double[individual.initializationRange.length][2];
        for (int i = 0; i < this.initializationRange.length; i++) {
            this.initializationRange[i][0] = individual.initializationRange[i][0];
            this.initializationRange[i][1] = individual.initializationRange[i][1];
        }

        // cloning the members of AbstractEAIndividual
        this.age = individual.age;
        this.crossoverOperator = (InterfaceCrossover) individual.crossoverOperator.clone();
        this.crossoverProbability = individual.crossoverProbability;
        this.mutationOperator = (InterfaceMutation) individual.mutationOperator.clone();
        this.mutationProbability = individual.mutationProbability;
        this.selectionProbability = new double[individual.selectionProbability.length];
        System.arraycopy(individual.selectionProbability, 0, this.selectionProbability, 0, this.selectionProbability.length);
        this.precision = individual.precision;
        this.doubleCoding = individual.doubleCoding;
        this.fitness = new double[individual.fitness.length];
        System.arraycopy(individual.fitness, 0, this.fitness, 0, this.fitness.length);
        cloneAEAObjects(individual);
    }

    @Override
    public Object clone() {
        return new GAIndividualDoubleData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAIndividualDoubleData) {
            GAIndividualDoubleData indy = (GAIndividualDoubleData) individual;
            //@todo Eigendlich k�nnte ich noch das Koding vergleichen
            if (this.genotypeLength != indy.genotypeLength) {
                return false;
            }
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            }
            if (!this.genotype.equals(indy.genotype)) {
                return false;
            }
            for (int i = 0; i < this.initializationRange.length; i++) {
                if (this.initializationRange[i][0] != indy.initializationRange[i][0]) {
                    return false;
                }
                if (this.initializationRange[i][1] != indy.initializationRange[i][1]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

/************************************************************************************
 * InterfaceDataTypeDouble methods
 */
    /**
     * This method allows you to request a certain amount of double data
     *
     * @param length The lenght of the double[] that is to be optimized
     */
    @Override
    public void setDoubleDataLength(int length) {
        double[] newDesPa = new double[length];
        double[][] newRange = new double[length][2];

        // copy the old values for the decision parameters and the range
        for (int i = 0; ((i < newDesPa.length) && (i < this.initializationRange.length)); i++) {
            newRange[i][0] = this.initializationRange[i][0];
            newRange[i][1] = this.initializationRange[i][1];
        }

        // if the new length is bigger than the last value fills the extra elements
        for (int i = this.initializationRange.length; (i < newDesPa.length); i++) {
            newRange[i][0] = this.initializationRange[this.initializationRange.length - 1][0];
            newRange[i][1] = this.initializationRange[this.initializationRange.length - 1][1];
        }
        this.initializationRange = newRange;
        this.genotypeLength = length * this.precision;
    }

    /**
     * This method returns the length of the double data set
     *
     * @return The number of bits stored
     */
    @Override
    public int size() {
        return this.initializationRange.length;
    }

    /**
     * This method will set the range of the double attributes. If range.length
     * does not equal doubledata.length only range[i] will be used to set all
     * ranges.
     *
     * @param range The new range for the double data.
     */
    @Override
    public void setDoubleRange(double[][] range) {
        if (range.length != this.initializationRange.length) {
            System.out.println("Warning: Trying to set a range of length " + range.length + " to a vector of length "
                    + this.initializationRange.length + "!\n Use method setDoubleDataLength first!");
        }
        for (int i = 0; ((i < this.initializationRange.length) && (i < range.length)); i++) {
            this.initializationRange[i][0] = range[i][0];
            this.initializationRange[i][1] = range[i][1];
        }
    }

    /**
     * This method will return the range for all double attributes.
     *
     * @return The range array.
     */
    @Override
    public double[][] getDoubleRange() {
        return this.initializationRange;
    }

    /**
     * This method allows you to read the double data
     *
     * @return BitSet representing the double data.
     */
    @Override
    public double[] getDoubleData() {
        int[] locus = new int[2];
        this.phenotype = new double[this.initializationRange.length];
        for (int i = 0; i < this.initializationRange.length; i++) {
            locus[0] = i * this.precision;
            locus[1] = this.precision;
            this.phenotype[i] = this.doubleCoding.decodeValue(this.genotype, this.initializationRange[i], locus, false);
        }
        return this.phenotype;
    }

    /**
     * This method allows you to read the double data without
     * an update from the genotype
     *
     * @return double[] representing the double data.
     */
    @Override
    public double[] getDoubleDataWithoutUpdate() {
        return this.phenotype;
    }

    /**
     * This method allows you to set the phenotype data. To change the genotype data,
     * use setDoubleDataLamarckian.
     *
     * @param doubleData The new double data.
     */
    @Override
    public void setDoublePhenotype(double[] doubleData) {
        this.phenotype = doubleData;
    }

    /**
     * This method allows you to set the double data, this can be used for
     * memetic algorithms.
     *
     * @param doubleData The new double data.
     */
    @Override
    public void setDoubleGenotype(double[] doubleData) {
        this.setDoublePhenotype(doubleData);
        int[] locus = new int[2];
        for (int i = 0; i < doubleData.length; i++) {
            locus[0] = i * this.precision;
            locus[1] = this.precision;
            this.doubleCoding.codeValue(doubleData[i], this.initializationRange[i], this.genotype, locus);
        }
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
        if (obj instanceof double[]) {
            double[] bs = (double[]) obj;
            if (bs.length != this.initializationRange.length) {
                System.out.println("Init value and requested length doesn't match!");
            }
            this.setDoubleGenotype(bs);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for GAIndividualDoubleData is not double[]!");
        }
        this.mutationOperator.initialize(this, opt);
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
        result += "GAIndividual coding double: (";
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
        double[] d = this.getDoubleData();
        for (int i = 0; i < d.length; i++) {
            result += d[i] + "; ";
        }
        result += "]\n";
        result += "{";
        for (int i = 0; i < this.genotypeLength; i++) {
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
     * This may be necessary since BitSet.lenght only returns the index
     * of the last significat bit.
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
     * This method performs a simple one point mutation in the genotype
     */
    @Override
    public void defaultMutate() {
        int mutationIndex = RNG.randomInt(0, this.genotypeLength);
        if (this.genotype.get(mutationIndex)) {
            this.genotype.clear(mutationIndex);
        } else {
            this.genotype.set(mutationIndex);
        }
    }
/**********************************************************************************************************************
 * These are for GUI
 */
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
    public void setGACoding(InterfaceGADoubleCoding coding) {
        this.doubleCoding = coding;
    }

    public InterfaceGADoubleCoding getGACoding() {
        return this.doubleCoding;
    }

    public String gADoubleCodingTipText() {
        return "Choose the coding to use.";
    }

    /**
     * This method allows you to set the number of mulitruns that are to be performed,
     * necessary for stochastic optimizers to ensure reliable results.
     *
     * @param precision The number of multiruns that are to be performed
     */
    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getPrecision() {
        return this.precision;
    }

    public String precisionTipText() {
        return "Gives the number of bits to be used to code a double.";
    }
}
