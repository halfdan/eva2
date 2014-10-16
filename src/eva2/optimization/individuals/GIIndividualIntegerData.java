package eva2.optimization.individuals;

import eva2.optimization.operator.crossover.CrossoverGIDefault;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateDefault;
import eva2.problems.InterfaceHasInitRange;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * This individual uses a integer genotype to code for integer values.
 */
@Description(value = "This is a GI individual suited to optimize int values.")
public class GIIndividualIntegerData extends AbstractEAIndividual implements InterfaceGIIndividual, InterfaceDataTypeInteger, java.io.Serializable {

    private int[] phenotype;
    private int[][] initializationRange;
    protected int[] genotype;

    public GIIndividualIntegerData() {
        this.mutationProbability = 0.2;
        this.mutationOperator = new MutateDefault();
        this.crossoverProbability = 0.7;
        this.crossoverOperator = new CrossoverGIDefault();
        this.initializationRange = new int[10][2];
        for (int i = 0; i < this.initializationRange.length; i++) {
            this.initializationRange[i][0] = 0;
            this.initializationRange[i][1] = 7;
        }
        this.genotype = new int[10];
    }

    public GIIndividualIntegerData(int[][] theRange) {
        this();
        setIntRange(theRange);
    }

    public GIIndividualIntegerData(GIIndividualIntegerData individual) {
        if (individual.phenotype != null) {
            this.phenotype = new int[individual.phenotype.length];
            System.arraycopy(individual.phenotype, 0, this.phenotype, 0, this.phenotype.length);
        }
        if (individual.genotype != null) {
            this.genotype = new int[individual.genotype.length];
            System.arraycopy(individual.genotype, 0, this.genotype, 0, this.genotype.length);

        }
        this.initializationRange = new int[individual.initializationRange.length][2];
        for (int i = 0; i < this.initializationRange.length; i++) {
            this.initializationRange[i][0] = individual.initializationRange[i][0];
            this.initializationRange[i][1] = individual.initializationRange[i][1];
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
        this.fitness = new double[individual.fitness.length];
        for (int i = 0; i < this.fitness.length; i++) {
            this.fitness[i] = individual.fitness[i];
        }
        cloneAEAObjects((AbstractEAIndividual) individual);
    }

    @Override
    public Object clone() {
        return (Object) new GIIndividualIntegerData(this);
    }


    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GIIndividualIntegerData) {
            GIIndividualIntegerData indy = (GIIndividualIntegerData) individual;
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            }
            if (this.genotype.length != indy.genotype.length) {
                return false;
            }
            for (int i = 0; i < this.initializationRange.length; i++) {
                if (this.genotype[i] != indy.genotype[i]) {
                    return false;
                }
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
        for (int i = 0; ((i < newDesPa.length) && (i < this.genotype.length)); i++) {
            newDesPa[i] = this.genotype[i];
            newRange[i][0] = this.initializationRange[i][0];
            newRange[i][1] = this.initializationRange[i][1];
        }

        // if the new length is bigger than the last value fills the extra elements
        for (int i = this.genotype.length; (i < newDesPa.length); i++) {
            newDesPa[i] = this.genotype[this.genotype.length - 1];
            newRange[i][0] = this.initializationRange[this.genotype.length - 1][0];
            newRange[i][1] = this.initializationRange[this.genotype.length - 1][1];
        }
        this.genotype = newDesPa;
        this.initializationRange = newRange;
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
    public void setIntRange(int[][] range) {
        if (range.length != this.initializationRange.length) {
            this.setIntegerDataLength(range.length);
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
    public int[][] getIntRange() {
        return this.initializationRange;
    }

    /**
     * This method allows you to read the double data
     *
     * @return BitSet representing the double data.
     */
    @Override
    public int[] getIntegerData() {
        this.phenotype = new int[this.initializationRange.length];
        for (int i = 0; i < this.phenotype.length; i++) {
            this.phenotype[i] = this.genotype[i];
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
        this.genotype = new int[this.initializationRange.length];
        for (int i = 0; i < doubleData.length; i++) {
            this.genotype[i] = doubleData[i];
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
        if (obj instanceof int[]) {
            int[] bs = (int[]) obj;
            if (bs.length != this.initializationRange.length) {
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
        result += "GIIndividual coding int: (";
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
        for (int i = 0; i < this.initializationRange.length; i++) {
            result += "(" + this.initializationRange[i][0] + "; " + this.initializationRange[i][1] + "); ";
        }
        result += "]\n";
        return result;
    }

/************************************************************************************
 * InterfaceGIIndividual methods
 */

    /**
     * This method will allow the user to read the GI genotype
     *
     * @return BitSet
     */
    @Override
    public int[] getIGenotype() {
        return this.genotype;
    }

    /**
     * This method will allow the user to set the current GI genotype.
     * Use this method with care, since the object is returned when using
     * getIGenotype() you can directly alter the genotype without using
     * this method.
     *
     * @param b The new genotype of the Individual
     */
    @Override
    public void setIGenotype(int[] b) {
        this.genotype = b;
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
        return this.genotype.length;
    }

    /**
     * This method performs a simple one point mutation in the genotype
     */
    @Override
    public void defaultMutate() {
        int mutationIndex = RNG.randomInt(0, this.genotype.length - 1);
        this.genotype[mutationIndex] = RNG.randomInt(this.initializationRange[mutationIndex][0], this.initializationRange[mutationIndex][1]);
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        int[][] range = initializationRange;
        if ((prob != null) && (prob instanceof InterfaceHasInitRange) && (((InterfaceHasInitRange) prob).getInitRange() != null)) {
            Object rng = ((InterfaceHasInitRange) prob).getInitRange();
            if (rng instanceof double[][]) {
                double[][] dblRng = (double[][]) rng;
                range = new int[dblRng.length][dblRng[0].length];
                for (int i = 0; i < range.length; i++) {
                    for (int j = 0; j < range[0].length; j++) {
                        range[i][j] = (int) dblRng[i][j];
                    }
                }
            } else if (rng instanceof int[][]) {
                range = (int[][]) rng;
            } else {
                System.err.println("Error, invalid initial range provided by " + prob + ", expecting int[][] or double[][], disregarding initialization range");
            }
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
        return "GI individual";
    }
}