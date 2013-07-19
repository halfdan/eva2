package eva2.optimization.individuals;

import eva2.optimization.operator.crossover.CrossoverESDefault;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateESGlobal;
import eva2.optimization.problems.InterfaceHasInitRange;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/** This individual uses a real-valued genotype to code for integer values.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 15.04.2004
 * Time: 17:18:58
 * To change this template use File | Settings | File Templates.
 */
public class ESIndividualIntegerData extends AbstractEAIndividual implements InterfaceESIndividual, InterfaceDataTypeInteger, java.io.Serializable {

    private double[]                m_Genotype;
    private int[]                   m_Phenotype;
    private int[][]                 m_Range;

    public ESIndividualIntegerData() {
        this.mutationProbability = 1.0;
        this.mutationOperator = new MutateESGlobal();
        this.crossoverProbability = 0.5;
        this.crossoverOperator = new CrossoverESDefault();
        this.m_Genotype             = new double[1];
        this.m_Range                = new int[1][2];
        this.m_Range[0][0]          = -10;
        this.m_Range[0][1]          = 10;
    }

    public ESIndividualIntegerData(ESIndividualIntegerData individual) {
        if (individual.m_Phenotype != null) {
            this.m_Phenotype            = new int[individual.m_Phenotype.length];
            System.arraycopy(individual.m_Phenotype, 0, this.m_Phenotype, 0, this.m_Phenotype.length);
        }
        this.m_Genotype                 = new double[individual.m_Genotype.length];
        this.m_Range                    = new int[individual.m_Genotype.length][2];
        for (int i = 0; i < this.m_Genotype.length; i++) {
            this.m_Genotype[i]              = individual.m_Genotype[i];
            this.m_Range[i][0]              = individual.m_Range[i][0];
            this.m_Range[i][1]              = individual.m_Range[i][1];
        }

        // cloning the members of AbstractEAIndividual
        this.age = individual.age;
        this.crossoverOperator = individual.crossoverOperator;
        this.crossoverProbability = individual.crossoverProbability;
        this.mutationOperator = (InterfaceMutation)individual.mutationOperator.clone();
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
        return (Object) new ESIndividualIntegerData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof ESIndividualIntegerData) {
            ESIndividualIntegerData indy = (ESIndividualIntegerData) individual;
            if ((this.m_Genotype == null) || (indy.m_Genotype == null)) {
                return false;
            }
            if ((this.m_Range == null) || (indy.m_Range == null)) {
                return false;
            }            
            for (int i = 0; i < this.m_Range.length; i++) {
                if (this.m_Genotype[i] != indy.m_Genotype[i]) {
                    return false;
                }
                if (this.m_Range[i][0] != indy.m_Range[i][0]) {
                    return false;
                }
                if (this.m_Range[i][1] != indy.m_Range[i][1]) {
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
    /** This method allows you to request a certain amount of int data
     * @param length    The lenght of the int[] that is to be optimized
     */
    @Override
    public void setIntegerDataLength (int length) {
        double[]     newDesPa = new double[length];
        int[][]      newRange = new int[length][2];

        // copy the old values for the decision parameters and the range
        for (int i = 0; ((i < newDesPa.length) && (i < this.m_Genotype.length)); i++) {
            newDesPa[i]     = this.m_Genotype[i];
            newRange[i][0]  = this.m_Range[i][0];
            newRange[i][1]  = this.m_Range[i][1];
        }

        // if the new length is bigger than the last value fills the extra elements
        for (int i = this.m_Genotype.length; (i < newDesPa.length); i++) {
            newDesPa[i]     = this.m_Genotype[this.m_Genotype.length-1];
            newRange[i][0]  = this.m_Range[this.m_Genotype.length-1][0];
            newRange[i][1]  = this.m_Range[this.m_Genotype.length-1][1];
        }
        this.m_Genotype             = newDesPa;
        this.m_Range                = newRange;
    }

    /** This method returns the length of the int data set
     * @return The number of ints stored
     */
    @Override
    public int size() {
        return this.m_Genotype.length;
    }

    /** This method will set the range of the int attributes. If range.length
     * does not equal intdata.length only range[i] will be used to set all
     * ranges.
     * @param range     The new range for the int data.
     */
    @Override
    public void SetIntRange(int[][] range) {
        if (range.length != this.m_Range.length) {
            System.out.println("Warning: Trying to set a range of length " + range.length + " to a vector of length "
                    + this.m_Range.length + "!\n Use method setIntegerDataLength first (ESIndividualIntegerData::SetIntRange)!");
        }
        for (int i = 0; ((i < this.m_Range.length) && (i < range.length)); i++) {
            this.m_Range[i][0] = range[i][0];
            this.m_Range[i][1] = range[i][1];
        }
    }

    /** This method will return the range for all int attributes.
     * @return The range array.
     */
    @Override
    public int[][] getIntRange() {
        return this.m_Range;
    }

    /** This method allows you to read the int data
     * @return int[] representing the int data.
     */
    @Override
    public int[] getIntegerData() {
        this.m_Phenotype = new int[this.m_Genotype.length];
        for (int i = 0; i < this.m_Phenotype.length; i++) {
            this.m_Phenotype[i] = (int) this.m_Genotype[i];
            if (this.m_Phenotype[i] < this.m_Range[i][0]) {
                this.m_Phenotype[i] = this.m_Range[i][0];
            }
            if (this.m_Phenotype[i] > this.m_Range[i][1]) {
                this.m_Phenotype[i] = this.m_Range[i][1];
            }
        }
        return this.m_Phenotype;
    }

    /** This method allows you to read the int data without
     * an update from the genotype
     * @return int[] representing the int data.
     */
    @Override
    public int[] getIntegerDataWithoutUpdate() {
        return this.m_Phenotype;
    }

    /** This method allows you to set the int data.
     * @param intData    The new int data.
     */
    @Override
    public void SetIntPhenotype(int[] intData) {
        this.m_Phenotype = intData;
    }

    /** This method allows you to set the int data, this can be used for
     * memetic algorithms.
     * @param intData    The new int data.
     */
    @Override
    public void SetIntGenotype(int[] intData) {
    	for (int i = 0; i < this.m_Genotype.length; i++) {
    		m_Genotype[i]=(double)intData[i];
    	}
    	getIntegerData();
    }

/************************************************************************************
 * AbstractEAIndividual methods
 */

    /** This method will init the individual with a given value for the
     * phenotype.
     * @param obj   The initial value for the phenotype
     * @param opt   The optimization problem that is to be solved.
     */
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof int[]) {
            int[]  bs = (int[]) obj;
            if (bs.length != this.m_Genotype.length) {
                System.out.println("Init value and requested length doesn't match!");
            }
            this.SetIntGenotype(bs);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for ESIndividualIntegerData is not int[]!");
        }
        this.mutationOperator.init(this, opt);
        this.crossoverOperator.init(this, opt);
    }

    /** This method will return a string description of the GAIndividal
     * noteably the Genotype.
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
        for (int i = 0; i < this.m_Genotype.length; i++) {
            result += this.m_Genotype[i] + "; ";
        }
        result += "]";
        return result;
    }

/************************************************************************************
 * InterfaceESIndividual methods
 */
    /** This method will allow the user to read the ES 'genotype'
     * @return BitSet
     */
    @Override
    public double[] getDGenotype() {
        return this.m_Genotype;
    }

    /** This method will allow the user to set the current ES 'genotype'.
     * @param b    The new genotype of the Individual
     */
    @Override
    public void SetDGenotype(double[] b) {
        this.m_Genotype = b;
        for (int i = 0; i < this.m_Genotype.length; i++) {
            if (this.m_Genotype[i] < this.m_Range[i][0]) {
                this.m_Genotype[i] = this.m_Range[i][0];
            }
            if (this.m_Genotype[i] > this.m_Range[i][1]) {
                this.m_Genotype[i] = this.m_Range[i][1];
            }
        }
    }

    /** This method performs a simple one element mutation on the double vector
     */
    @Override
    public void defaultMutate() {
        int mutationIndex = RNG.randomInt(0, this.m_Genotype.length-1);
        this.m_Genotype[mutationIndex] += ((this.m_Range[mutationIndex][1] - this.m_Range[mutationIndex][0])/2)*RNG.gaussianDouble(0.05f);
        if (this.m_Genotype[mutationIndex] < this.m_Range[mutationIndex][0]) {
            this.m_Genotype[mutationIndex] = this.m_Range[mutationIndex][0];
        }
        if (this.m_Genotype[mutationIndex] > this.m_Range[mutationIndex][1]) {
            this.m_Genotype[mutationIndex] = this.m_Range[mutationIndex][1];
        }
    }

    /** This method will return the range for all double attributes.
     * @return The range array.
     */
    @Override
    public double[][] getDoubleRange() {
        double[][] result = new double[this.m_Range.length][2];
        for (int i = 0; i < this.m_Range.length; i++) {
            result[i][0] = this.m_Range[i][0];
            result[i][1] = this.m_Range[i][1];
        }
        return result;
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
    	int[][] range = m_Range;
        if ((prob != null) && (prob instanceof InterfaceHasInitRange) && (((InterfaceHasInitRange)prob).getInitRange()!=null)) {
            range = (int[][])((InterfaceHasInitRange)prob).getInitRange();
        }
    	for (int i = 0; i < this.m_Genotype.length; i++) {
            this.m_Genotype[i] = RNG.randomInt(range[i][0], range[i][1]);
        }
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    @Override
    public String getName() {
        return "ES individual";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is an ES individual suited to optimize integer values.";
    }
}