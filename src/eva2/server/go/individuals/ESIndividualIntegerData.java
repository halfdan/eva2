package eva2.server.go.individuals;


import java.util.Arrays;

import eva2.server.go.IndividualInterface;
import eva2.server.go.operators.crossover.CrossoverESDefault;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateESGlobal;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;

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
        this.m_MutationProbability  = 1.0;
        this.m_MutationOperator     = new MutateESGlobal();
        this.m_CrossoverProbability = 0.5;
        this.m_CrossoverOperator    = new CrossoverESDefault();
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
        this.m_Age                      = individual.m_Age;
        this.m_CrossoverOperator        = individual.m_CrossoverOperator;
        this.m_CrossoverProbability     = individual.m_CrossoverProbability;
        this.m_MutationOperator         = (InterfaceMutation)individual.m_MutationOperator.clone();
        this.m_MutationProbability      = individual.m_MutationProbability;
        this.m_SelectionProbability = new double[individual.m_SelectionProbability.length];
        for (int i = 0; i < this.m_SelectionProbability.length; i++) {
            this.m_SelectionProbability[i] = individual.m_SelectionProbability[i];
        }
        this.m_Fitness = new double[individual.m_Fitness.length];
        for (int i = 0; i < this.m_Fitness.length; i++) {
            this.m_Fitness[i] = individual.m_Fitness[i];
        }
        cloneAEAObjects((AbstractEAIndividual) individual);
    }

    public Object clone() {
        return (Object) new ESIndividualIntegerData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof ESIndividualIntegerData) {
            ESIndividualIntegerData indy = (ESIndividualIntegerData) individual;
            if ((this.m_Genotype == null) || (indy.m_Genotype == null)) return false;
            if ((this.m_Range == null) || (indy.m_Range == null)) return false;            
            for (int i = 0; i < this.m_Range.length; i++) {
                if (this.m_Genotype[i] != indy.m_Genotype[i]) return false;
                if (this.m_Range[i][0] != indy.m_Range[i][0]) return false;
                if (this.m_Range[i][1] != indy.m_Range[i][1]) return false;
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
    public int size() {
        return this.m_Genotype.length;
    }

    /** This method will set the range of the int attributes. If range.length
     * does not equal intdata.length only range[i] will be used to set all
     * ranges.
     * @param range     The new range for the int data.
     */
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
    public int[][] getIntRange() {
        return this.m_Range;
    }

    /** This method allows you to read the int data
     * @return int[] representing the int data.
     */
    public int[] getIntegerData() {
        this.m_Phenotype = new int[this.m_Genotype.length];
        for (int i = 0; i < this.m_Phenotype.length; i++) {
            this.m_Phenotype[i] = (int) this.m_Genotype[i];
            if (this.m_Phenotype[i] < this.m_Range[i][0]) this.m_Phenotype[i] = this.m_Range[i][0];
            if (this.m_Phenotype[i] > this.m_Range[i][1]) this.m_Phenotype[i] = this.m_Range[i][1];
        }
        return this.m_Phenotype;
    }

    /** This method allows you to read the int data without
     * an update from the genotype
     * @return int[] representing the int data.
     */
    public int[] getIntegerDataWithoutUpdate() {
        return this.m_Phenotype;
    }

    /** This method allows you to set the int data.
     * @param intData    The new int data.
     */
    public void SetIntegerData(int[] intData) {
        this.m_Phenotype = intData;
    }

    /** This method allows you to set the int data, this can be used for
     * memetic algorithms.
     * @param intData    The new int data.
     */
    public void SetIntegerDataLamarkian(int[] intData) {
        this.SetIntegerData(intData);
    }

/************************************************************************************
 * AbstractEAIndividual methods
 */
    /** This method will allow a default initialisation of the individual
     * @param opt   The optimization problem that is to be solved.
     */
    public void init(InterfaceOptimizationProblem opt) {
        this.defaultInit();
        this.m_MutationOperator.init(this, opt);
        this.m_CrossoverOperator.init(this, opt);
    }

    /** This method will init the individual with a given value for the
     * phenotype.
     * @param obj   The initial value for the phenotype
     * @param opt   The optimization problem that is to be solved.
     */
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof int[]) {
            int[]  bs = (int[]) obj;
            if (bs.length != this.m_Genotype.length) System.out.println("Init value and requested length doesn't match!");
            this.SetIntegerDataLamarkian(bs);
        } else {
            this.defaultInit();
            System.out.println("Initial value for ESIndividualIntegerData is not int[]!");
        }
        this.m_MutationOperator.init(this, opt);
        this.m_CrossoverOperator.init(this, opt);
    }

    /** This method will return a string description of the GAIndividal
     * noteably the Genotype.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "ESIndividual coding int: (";
        result += "Fitness {";
        for (int i = 0; i < this.m_Fitness.length; i++) result += this.m_Fitness[i] + ";";
        result += "}/SelProb{";
        for (int i = 0; i < this.m_SelectionProbability.length; i++) result += this.m_SelectionProbability[i] + ";";
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
    public double[] getDGenotype() {
        return this.m_Genotype;
    }

    /** This method will allow the user to set the current ES 'genotype'.
     * @param b    The new genotype of the Individual
     */
    public void SetDGenotype(double[] b) {
        this.m_Genotype = b;
        for (int i = 0; i < this.m_Genotype.length; i++) {
            if (this.m_Genotype[i] < this.m_Range[i][0]) this.m_Genotype[i] = this.m_Range[i][0];
            if (this.m_Genotype[i] > this.m_Range[i][1]) this.m_Genotype[i] = this.m_Range[i][1];
        }
    }

    /** This method performs a simple one element mutation on the double vector
     */
    public void defaultMutate() {
        int mutationIndex = RNG.randomInt(0, this.m_Genotype.length-1);
        this.m_Genotype[mutationIndex] += ((this.m_Range[mutationIndex][1] - this.m_Range[mutationIndex][0])/2)*RNG.gaussianDouble(0.05f);
        if (this.m_Genotype[mutationIndex] < this.m_Range[mutationIndex][0]) this.m_Genotype[mutationIndex] = this.m_Range[mutationIndex][0];
        if (this.m_Genotype[mutationIndex] > this.m_Range[mutationIndex][1]) this.m_Genotype[mutationIndex] = this.m_Range[mutationIndex][1];
    }

    /** This method will return the range for all double attributes.
     * @return The range array.
     */
    public double[][] getDoubleRange() {
        double[][] result = new double[this.m_Range.length][2];
        for (int i = 0; i < this.m_Range.length; i++) {
            result[i][0] = this.m_Range[i][0];
            result[i][1] = this.m_Range[i][1];
        }
        return result;
    }

    /** This method initializes the double vector
     */
    public void defaultInit() {
        for (int i = 0; i < this.m_Genotype.length; i++) {
            this.m_Genotype[i] = RNG.randomInt(this.m_Range[i][0], this.m_Range[i][1]);
        }
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "ES indiviudal";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a ES individual suited to optimize ini values.";
    }
}