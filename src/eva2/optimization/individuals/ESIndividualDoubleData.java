package eva2.optimization.individuals;


import eva2.optimization.enums.MutateESCrossoverTypeEnum;
import eva2.optimization.operator.crossover.CrossoverESDefault;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateESGlobal;
import eva2.problems.InterfaceHasInitRange;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * This individual uses a real-valued genotype to code for double values.
 */
@Description(value = "This is an ES individual suited to optimize double values.")
public class ESIndividualDoubleData extends AbstractEAIndividual implements InterfaceESIndividual, InterfaceDataTypeDouble, java.io.Serializable {

    private double[] genotype;
    private double[] phenotype;
    private double[][] range;

    public ESIndividualDoubleData() {
        this.mutationProbability = 1.0;
        this.mutationOperator = new MutateESGlobal(0.2, MutateESCrossoverTypeEnum.intermediate);
        this.crossoverProbability = 0.5;
        this.crossoverOperator = new CrossoverESDefault();
        this.genotype = new double[1];
        this.phenotype = null;
        this.range = new double[1][2];
        this.range[0][0] = -10;
        this.range[0][1] = 10;
    }

    public ESIndividualDoubleData(ESIndividualDoubleData individual) {
        if (individual.phenotype != null) {
            this.phenotype = new double[individual.phenotype.length];
            System.arraycopy(individual.phenotype, 0, this.phenotype, 0, this.phenotype.length);
        }
        this.genotype = new double[individual.genotype.length];
        this.range = new double[individual.range.length][2];
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
        return (Object) new ESIndividualDoubleData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof ESIndividualDoubleData) {
            ESIndividualDoubleData indy = (ESIndividualDoubleData) individual;
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            }
            if ((this.range == null) || (indy.range == null)) {
                return false;
            }
            if (this.genotype.length != indy.genotype.length) {
                return false;
            }
            if (this.range.length != indy.range.length) {
                return false;
            }
            for (int i = 0; i < this.genotype.length; i++) {
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
        this.phenotype = null; // mark as invalid
    }

    /**
     * This method returns the length of the double data set
     *
     * @return The number of bits stored
     */
    @Override
    public int size() {
        return this.genotype.length;
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
        if (range.length != this.range.length) {
            System.out.println("Warning: Trying to set a range of length " + range.length + " to a vector of length "
                    + this.range.length + "!\n Use method setDoubleDataLength first! (ESIndividualDoubleData:setDoubleRange)");
        }
        for (int i = 0; ((i < this.range.length) && (i < range.length)); i++) {
            this.range[i][0] = range[i][0];
            this.range[i][1] = range[i][1];
        }
    }

    /**
     * This method will return the range for all double attributes.
     *
     * @return The range array.
     */
    @Override
    public double[][] getDoubleRange() {
        return this.range;
    }

    /**
     * This method allows you to read the double data. A new phenotype array is allocated
     * and the genotype copied.
     *
     * @return BitSet representing the double data.
     */
    @Override
    public double[] getDoubleData() {
        // since the phenotype is set to null if the genotype is changed,
        // it should now be save to only perform the copy if the phenotype is null
        if (this.phenotype != null) {
            return phenotype;
        } else {
            this.phenotype = new double[this.genotype.length];
            System.arraycopy(this.genotype, 0, this.phenotype, 0, this.genotype.length);
            return this.phenotype;
        }
    }

    /**
     * This method allows you to read the double data without
     * an update from the genotype.
     *
     * @return double[] representing the double data.
     */
    @Override
    public double[] getDoubleDataWithoutUpdate() {
        if (phenotype == null) {
            return getDoubleData();
        } else {
            return this.phenotype;
        }
    }

    /**
     * This method allows you to set the phenotype double data. To change the genotype,
     * use SetDoubleDataLamarckian().
     *
     * @param doubleData The new double data.
     */
    @Override
    public void setDoublePhenotype(double[] doubleData) {
        this.phenotype = doubleData;
    }

    /**
     * This method allows you to set the genotype data, this can be used for
     * memetic algorithms.
     *
     * @param doubleData The new double data.
     */
    @Override
    public void setDoubleGenotype(double[] doubleData) {
//        this.setDoublePhenotype(doubleData);
        this.setDoublePhenotype(null); // tag it as invalid
        this.genotype = new double[doubleData.length];
        System.arraycopy(doubleData, 0, this.genotype, 0, doubleData.length);
    }

/************************************************************************************
 * AbstractEAIndividual methods
 */
    /**
     * This method will allow a default initialisation of the individual
     *
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void init(InterfaceOptimizationProblem opt) {
        super.init(opt);
        // evil operators may not respect the range, so at least give some hint
        if (!Mathematics.isInRange(genotype, range)) {
            EVAERROR.errorMsgOnce("Warning: Individual out of range after initialization (and potential initial crossover/mutation)!");
        }
    }

    /**
     * This method will init the individual with a given value for the
     * phenotype.
     *
     * @param obj The initial value for the phenotype
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof double[]) {
            double[] bs = (double[]) obj;
            if (bs.length != this.genotype.length) {
                System.out.println("Init value and requested length doesn't match!");
            }
            this.setDoubleGenotype(bs);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for ESIndividualDoubleData is not double[]!");
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
        StringBuilder strB = new StringBuilder(200);
        strB.append("ESIndividual coding double: (Fitness {");
        for (int i = 0; i < this.fitness.length; i++) {
            strB.append(this.fitness[i]);
            strB.append(";");
        }
        strB.append("}/SelProb{");

        for (int i = 0; i < this.selectionProbability.length; i++) {
            strB.append(this.selectionProbability[i]);
            strB.append(";");
        }
        strB.append("}) Value: [");
        for (int i = 0; i < this.genotype.length; i++) {
            strB.append(this.genotype[i]);
            strB.append("; ");
        }
        strB.append("]");
        return strB.toString();
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
        this.phenotype = null; // mark it as invalid
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
     * This method will allow the user to set the current ES 'genotype'.
     *
     * @param b The new genotype of the Individual
     */
    public void setDGenotypeNocheck(double[] b) {
        this.phenotype = null; // mark it as invalid
        this.genotype = b;
    }

    /**
     * This method performs a simple one element mutation on the double vector
     */
    @Override
    public void defaultMutate() {
        ESIndividualDoubleData.defaultMutate(this.genotype, this.range);
        phenotype = null; // mark it as invalid
    }

    /**
     * Helper method for default ES mutation. A single, uniformly chosen double entry
     * is mutated with a gaussian value.
     * If the range constraint is violated, the value is set on the bound.
     *
     * @param genotype
     * @param range
     */
    public static void defaultMutate(double[] genotype, double[][] range) {
        int mutationIndex = RNG.randomInt(0, genotype.length - 1);
        genotype[mutationIndex] += ((range[mutationIndex][1] - range[mutationIndex][0]) / 2) * RNG.gaussianDouble(0.05f);
        if (genotype[mutationIndex] < range[mutationIndex][0]) {
            genotype[mutationIndex] = range[mutationIndex][0];
        }
        if (genotype[mutationIndex] > range[mutationIndex][1]) {
            genotype[mutationIndex] = range[mutationIndex][1];
        }
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        if ((prob != null) && (prob instanceof InterfaceHasInitRange) && (((InterfaceHasInitRange) prob).getInitRange() != null)) {
            ESIndividualDoubleData.defaultInit(genotype, (double[][]) ((InterfaceHasInitRange) prob).getInitRange());
        } else {
            ESIndividualDoubleData.defaultInit(genotype, range);
        }
        phenotype = null; // mark as invalid
    }

    /**
     * Helper method for initialization. The genotype is distributed uniformly
     * within the given range.
     *
     * @param genotype
     * @param range
     */
    public static void defaultInit(double[] genotype, double[][] range) {
        for (int i = 0; i < genotype.length; i++) {
            genotype[i] = RNG.randomDouble(range[i][0], range[i][1]);
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
