package eva2.optimization.individuals;

import eva2.optimization.operator.crossover.CrossoverESDefault;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateESGlobal;
import eva2.problems.InterfaceHasInitRange;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

import java.util.BitSet;


/**
 * This individual uses a real-valued genotype to code for binary values, either
 * by using a threshold value of by interpreting the double value as probability.
 */
@eva2.util.annotation.Description(value = "This is an ES individual adopted to optimize binary values.")
public class ESIndividualBinaryData extends AbstractEAIndividual implements InterfaceESIndividual, InterfaceDataTypeBinary, java.io.Serializable {

    private BitSet phenotype = new BitSet();
    private double[] genotype;
    private boolean useHardSwitch = false;
    private double[][] initializationRange;

    public ESIndividualBinaryData() {
        this.mutationProbability = 1.0;
        this.mutationOperator = new MutateESGlobal();
        this.crossoverProbability = 0.5;
        this.crossoverOperator = new CrossoverESDefault();
        this.genotype = new double[1];
        this.initializationRange = new double[1][2];
        this.initializationRange[0][0] = 0;
        this.initializationRange[0][1] = 1;
    }

    public ESIndividualBinaryData(ESIndividualBinaryData individual) {
        if (individual.phenotype != null) {
            this.phenotype = (BitSet) individual.phenotype.clone();
        }
        this.genotype = new double[individual.genotype.length];
        this.initializationRange = new double[individual.genotype.length][2];
        for (int i = 0; i < this.genotype.length; i++) {
            this.genotype[i] = individual.genotype[i];
            this.initializationRange[i][0] = individual.initializationRange[i][0];
            this.initializationRange[i][1] = individual.initializationRange[i][1];
        }
        this.useHardSwitch = individual.useHardSwitch;

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
        return new ESIndividualBinaryData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof ESIndividualBinaryData) {
            ESIndividualBinaryData indy = (ESIndividualBinaryData) individual;
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            }
            if ((this.initializationRange == null) || (indy.initializationRange == null)) {
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
 * InterfaceDataTypeBinary methods
 */
    /**
     * This method allows you to request a certain amount of binary data
     *
     * @param length The lenght of the BitSet that is to be optimized
     */
    @Override
    public void setBinaryDataLength(int length) {
        this.genotype = new double[length];
        this.initializationRange = new double[length][2];
        for (int i = 0; i < this.initializationRange.length; i++) {
            this.initializationRange[i][0] = 0;
            this.initializationRange[i][1] = 1;
        }
    }

    /**
     * This method returns the length of the binary data set
     *
     * @return The number of bits stored
     */
    @Override
    public int size() {
        return this.genotype.length;
    }

    /**
     * This method allows you to read the binary data
     *
     * @return BitSet representing the binary data.
     */
    @Override
    public BitSet getBinaryData() {
        if (this.useHardSwitch) {
            // In this case it is only tested if the genotyp is bigger than 0.5
            for (int i = 0; i < this.genotype.length; i++) {
                if (this.genotype[i] > 0.5) {
                    this.phenotype.set(i);
                } else {
                    this.phenotype.clear(i);
                }
            }
        } else {
            // in this case the value of the genotype is interpreted as a probability
            for (int i = 0; i < this.genotype.length; i++) {
                if (RNG.flipCoin(this.genotype[i])) {
                    this.phenotype.set(i);
                } else {
                    this.phenotype.clear(i);
                }
            }
        }
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
        for (int i = 0; i < this.genotype.length; i++) {
            if (this.useHardSwitch) {
                if (binaryData.get(i)) {
                    this.genotype[i] = RNG.randomDouble(0.55, 1.0);
                } else {
                    this.genotype[i] = RNG.randomDouble(0.0, 0.45);
                }
            } else {
                if (binaryData.get(i)) {
                    this.genotype[i] = 0.9;
                } else {
                    this.genotype[i] = 0.1;
                }
            }
        }
    }

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
            System.out.println("Initial value for ESIndividualBinaryData is no BitSet!");
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
        result += "ESIndividual coding double: (";
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
            if (this.genotype[i] < this.initializationRange[i][0]) {
                this.genotype[i] = this.initializationRange[1][0];
            }
            if (this.genotype[i] > this.initializationRange[i][1]) {
                this.genotype[i] = this.initializationRange[1][1];
            }
        }
    }


    /**
     * This method will return the range for all double attributes.
     *
     * @return The initializationRange array.
     */
    @Override
    public double[][] getDoubleRange() {
        return this.initializationRange;
    }

    /**
     * This method performs a eva2.problems.simple one element mutation on the double vector
     */
    @Override
    public void defaultMutate() {
        ESIndividualDoubleData.defaultMutate(genotype, initializationRange);
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        if ((prob != null) && (prob instanceof InterfaceHasInitRange) && (((InterfaceHasInitRange) prob).getInitializationRange() != null)) {
            ESIndividualDoubleData.defaultInit(genotype, (double[][]) ((InterfaceHasInitRange) prob).getInitializationRange());
        } else {
            ESIndividualDoubleData.defaultInit(genotype, initializationRange);
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
        return "ES individual";
    }

    /**
     * This method will toggle between genotype interpretation as bit probability and
     * fixed switch.
     *
     * @param b the Switch.
     */
    public void setToggleInterpretation(boolean b) {
        this.useHardSwitch = b;
    }

    public boolean getToggleInterpretation() {
        return this.useHardSwitch;
    }

    public String toggleInterpretationTipText() {
        return "Toggle between interpretation as probability or if(>0.5).";
    }
}
