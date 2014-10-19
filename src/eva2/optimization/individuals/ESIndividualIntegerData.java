package eva2.optimization.individuals;

import eva2.optimization.operator.crossover.CrossoverESDefault;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateESGlobal;
import eva2.problems.InterfaceHasInitRange;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * This individual uses a real-valued genotype to code for integer values.
 */
@Description(value = "This is an ES individual suited to optimize integer values.")
public class ESIndividualIntegerData extends AbstractEAIndividual implements InterfaceESIndividual, InterfaceDataTypeInteger, java.io.Serializable {

    private double[] genotype;
    private int[] phenotype;
    private int[][] range;

    public ESIndividualIntegerData() {
        this.mutationProbability = 1.0;
        this.mutationOperator = new MutateESGlobal();
        this.crossoverProbability = 0.5;
        this.crossoverOperator = new CrossoverESDefault();
        this.genotype = new double[1];
        this.range = new int[1][2];
        this.range[0][0] = -10;
        this.range[0][1] = 10;
    }

    public ESIndividualIntegerData(ESIndividualIntegerData individual) {
        if (individual.phenotype != null) {
            this.phenotype = new int[individual.phenotype.length];
            System.arraycopy(individual.phenotype, 0, this.phenotype, 0, this.phenotype.length);
        }
        this.genotype = new double[individual.genotype.length];
        this.range = new int[individual.genotype.length][2];
        for (int i = 0; i < this.genotype.length; i++) {
            this.genotype[i] = individual.genotype[i];
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
        System.arraycopy(individual.selectionProbability, 0, this.selectionProbability, 0, this.selectionProbability.length);
        this.fitness = new double[individual.fitness.length];
        System.arraycopy(individual.fitness, 0, this.fitness, 0, this.fitness.length);
        cloneAEAObjects(individual);
    }

    @Override
    public Object clone() {
        return new ESIndividualIntegerData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof ESIndividualIntegerData) {
            ESIndividualIntegerData indy = (ESIndividualIntegerData) individual;
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            }
            if ((this.range == null) || (indy.range == null)) {
                return false;
            }
            for (int i = 0; i < this.range.length; i++) {
                if (this.genotype[i] != indy.genotype[i]) {
                    return false;
                }
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
     * This method allows you to request a certain amount of int data
     *
     * @param length The lenght of the int[] that is to be optimized
     */
    @Override
    public void setIntegerDataLength(int length) {
        double[] newDesPa = new double[length];
        int[][] newRange = new int[length][2];

        // copy the old values for the decision parameters and the range
        for (int i = 0; ((i < newDesPa.length) && (i < this.genotype.length)); i++) {
            newDesPa[i] = this.genotype[i];
            newRange[i][0] = this.range[i][0];
            newRange[i][1] = this.range[i][1];
        }

        // if the new length is bigger than the last value fills the extra elements
        for (int i = this.genotype.length; (i < newDesPa.length); i++) {
            newDesPa[i] = this.genotype[this.genotype.length - 1];
            newRange[i][0] = this.range[this.genotype.length - 1][0];
            newRange[i][1] = this.range[this.genotype.length - 1][1];
        }
        this.genotype = newDesPa;
        this.range = newRange;
    }

    /**
     * This method returns the length of the int data set
     *
     * @return The number of ints stored
     */
    @Override
    public int size() {
        return this.genotype.length;
    }

    /**
     * This method will set the range of the int attributes. If range.length
     * does not equal intdata.length only range[i] will be used to set all
     * ranges.
     *
     * @param range The new range for the int data.
     */
    @Override
    public void setIntRange(int[][] range) {
        if (range.length != this.range.length) {
            System.out.println("Warning: Trying to set a range of length " + range.length + " to a vector of length "
                    + this.range.length + "!\n Use method setIntegerDataLength first (ESIndividualIntegerData::setIntRange)!");
        }
        for (int i = 0; ((i < this.range.length) && (i < range.length)); i++) {
            this.range[i][0] = range[i][0];
            this.range[i][1] = range[i][1];
        }
    }

    /**
     * This method will return the range for all int attributes.
     *
     * @return The range array.
     */
    @Override
    public int[][] getIntRange() {
        return this.range;
    }

    /**
     * This method allows you to read the int data
     *
     * @return int[] representing the int data.
     */
    @Override
    public int[] getIntegerData() {
        this.phenotype = new int[this.genotype.length];
        for (int i = 0; i < this.phenotype.length; i++) {
            this.phenotype[i] = (int) this.genotype[i];
            if (this.phenotype[i] < this.range[i][0]) {
                this.phenotype[i] = this.range[i][0];
            }
            if (this.phenotype[i] > this.range[i][1]) {
                this.phenotype[i] = this.range[i][1];
            }
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
     * This method allows you to set the int data.
     *
     * @param intData The new int data.
     */
    @Override
    public void setIntPhenotype(int[] intData) {
        this.phenotype = intData;
    }

    /**
     * This method allows you to set the int data, this can be used for
     * memetic algorithms.
     *
     * @param intData The new int data.
     */
    @Override
    public void setIntGenotype(int[] intData) {
        for (int i = 0; i < this.genotype.length; i++) {
            genotype[i] = (double) intData[i];
        }
        getIntegerData();
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
        if (obj instanceof int[]) {
            int[] bs = (int[]) obj;
            if (bs.length != this.genotype.length) {
                System.out.println("Init value and requested length doesn't match!");
            }
            this.setIntGenotype(bs);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for ESIndividualIntegerData is not int[]!");
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
        result += "ESIndividual coding int: (";
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
        for (int i = 0; i < this.genotype.length; i++) {
            result += this.genotype[i] + "; ";
        }
        result += "]";
        return result;
    }

/************************************************************************************
 * InterfaceESIndividual methods
 */
    /**
     * This method will allow the user to read the ES 'genotype'
     *
     * @return BitSet
     */
    @Override
    public double[] getDGenotype() {
        return this.genotype;
    }

    /**
     * This method will allow the user to set the current ES 'genotype'.
     *
     * @param b The new genotype of the Individual
     */
    @Override
    public void setDGenotype(double[] b) {
        this.genotype = b;
        for (int i = 0; i < this.genotype.length; i++) {
            if (this.genotype[i] < this.range[i][0]) {
                this.genotype[i] = this.range[i][0];
            }
            if (this.genotype[i] > this.range[i][1]) {
                this.genotype[i] = this.range[i][1];
            }
        }
    }

    /**
     * This method performs a simple one element mutation on the double vector
     */
    @Override
    public void defaultMutate() {
        int mutationIndex = RNG.randomInt(0, this.genotype.length - 1);
        this.genotype[mutationIndex] += ((this.range[mutationIndex][1] - this.range[mutationIndex][0]) / 2) * RNG.gaussianDouble(0.05f);
        if (this.genotype[mutationIndex] < this.range[mutationIndex][0]) {
            this.genotype[mutationIndex] = this.range[mutationIndex][0];
        }
        if (this.genotype[mutationIndex] > this.range[mutationIndex][1]) {
            this.genotype[mutationIndex] = this.range[mutationIndex][1];
        }
    }

    /**
     * This method will return the range for all double attributes.
     *
     * @return The range array.
     */
    @Override
    public double[][] getDoubleRange() {
        double[][] result = new double[this.range.length][2];
        for (int i = 0; i < this.range.length; i++) {
            result[i][0] = this.range[i][0];
            result[i][1] = this.range[i][1];
        }
        return result;
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        int[][] range = this.range;
        if ((prob != null) && (prob instanceof InterfaceHasInitRange) && (((InterfaceHasInitRange) prob).getInitializationRange() != null)) {
            range = (int[][]) ((InterfaceHasInitRange) prob).getInitializationRange();
        }
        for (int i = 0; i < this.genotype.length; i++) {
            this.genotype[i] = RNG.randomInt(range[i][0], range[i][1]);
        }
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "ES individual";
    }
}