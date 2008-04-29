package eva2.server.go.individuals;

import eva2.server.go.operators.crossover.CrossoverGIDefault;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateGIDefault;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import wsi.ra.math.RNG;

/** This individual uses a integer genotype to code for integer values.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 18.05.2005
 * Time: 16:55:23
 * To change this template use File | Settings | File Templates.
 */
public class GIIndividualIntegerData extends AbstractEAIndividual implements InterfaceGIIndividual, InterfaceDataTypeInteger, java.io.Serializable {

    private int[]                       m_Phenotype;
    private int[][]                     m_Range;
    protected int[]                     m_Genotype;

    public GIIndividualIntegerData() {
        this.m_MutationProbability  = 0.1;
        this.m_MutationOperator     = new MutateGIDefault();
        this.m_CrossoverProbability = 0.7;
        this.m_CrossoverOperator    = new CrossoverGIDefault();
        this.m_Range                = new int[10][2];
        for (int i = 0; i < this.m_Range.length; i++) {
            this.m_Range[i][0]          = 0;
            this.m_Range[i][1]          = 7;
        }
        this.m_Genotype             = new int[10];
    }

    public GIIndividualIntegerData(GIIndividualIntegerData individual) {
        if (individual.m_Phenotype != null) {
            this.m_Phenotype            = new int[individual.m_Phenotype.length];
            System.arraycopy(individual.m_Phenotype, 0, this.m_Phenotype, 0, this.m_Phenotype.length);
        }
        if (individual.m_Genotype != null) {
            this.m_Genotype            = new int[individual.m_Genotype.length];
            System.arraycopy(individual.m_Genotype, 0, this.m_Genotype, 0, this.m_Genotype.length);

        }
        this.m_Range            = new int[individual.m_Range.length][2];
        for (int i = 0; i < this.m_Range.length; i++) {
            this.m_Range[i][0]      = individual.m_Range[i][0];
            this.m_Range[i][1]      = individual.m_Range[i][1];
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
        return (Object) new GIIndividualIntegerData(this);
    }


    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GIIndividualIntegerData) {
            GIIndividualIntegerData indy = (GIIndividualIntegerData) individual;
            if ((this.m_Genotype == null) || (indy.m_Genotype == null)) return false;
            if (this.m_Genotype.length != indy.m_Genotype.length) return false;
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
    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    public void setIntegerDataLength (int length) {
        int[]        newDesPa = new int[length];
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
        this.m_Genotype   = newDesPa;
        this.m_Range      = newRange;
    }

    /** This method returns the length of the double data set
     * @return The number of bits stored
     */
    public int size() {
        return this.m_Range.length;
    }

    /** This method will set the range of the double attributes. If range.length
     * does not equal doubledata.length only range[i] will be used to set all
     * ranges.
     * @param range     The new range for the double data.
     */
    public void SetIntRange(int[][] range) {
        if (range.length != this.m_Range.length) {
            System.out.println("Warning: Trying to set a range of length " + range.length + " to a vector of length "
                    + this.m_Range.length + "!\n Use method setDoubleDataLength first!");
        }
        for (int i = 0; ((i < this.m_Range.length) && (i < range.length)); i++) {
            this.m_Range[i][0] = range[i][0];
            this.m_Range[i][1] = range[i][1];
        }
        this.setIntegerDataLength(range.length);
    }

    /** This method will return the range for all double attributes.
     * @return The range array.
     */
    public int[][] getIntRange() {
        return this.m_Range;
    }

    /** This method allows you to read the double data
     * @return BitSet representing the double data.
     */
    public int[] getIntegerData() {
        this.m_Phenotype = new int[this.m_Range.length];
        for (int i = 0; i < this.m_Phenotype.length; i++) {
            this.m_Phenotype[i] = this.m_Genotype[i];
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

    /** This method allows you to set the double data.
     * @param doubleData    The new double data.
     */
    public void SetIntegerData(int[] doubleData) {
        this.m_Phenotype = doubleData;
    }

    /** This method allows you to set the double data, this can be used for
     * memetic algorithms.
     * @param doubleData    The new double data.
     */
    public void SetIntegerDataLamarkian(int[] doubleData) {
        this.SetIntegerData(doubleData);
        this.m_Genotype = new int[this.m_Range.length];
        for (int i = 0; i < doubleData.length; i++) {
            this.m_Genotype[i] = doubleData[i];
        }
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
            if (bs.length != this.m_Range.length) System.out.println("Init value and requested length doesn't match!");
            this.SetIntegerDataLamarkian(bs);
        } else {
            this.defaultInit();
            System.out.println("Initial value for GAIndividualDoubleData is not double[]!");
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
        result += "GIIndividual coding int: (";
      result += "Fitness {";
        for (int i = 0; i < this.m_Fitness.length; i++) result += this.m_Fitness[i] + ";";
        result += "}/SelProb{";
        for (int i = 0; i < this.m_SelectionProbability.length; i++) result += this.m_SelectionProbability[i] + ";";
        result += "})\n Value: ";
        result += "[";
        int[]   d = this.getIntegerData();
        for (int i = 0; i < d.length; i++) {
            result += d[i] + "; ";
        }
        result += "]\n";
        result += "CodingRange:  [";
        for (int i = 0; i < this.m_Range.length; i++) {
            result += "("+this.m_Range[i][0]+"; "+this.m_Range[i][1]+"); ";
        }
        result += "]\n";
        return result;
    }

/************************************************************************************
 * InterfaceGIIndividual methods
 */

    /** This method will allow the user to read the GI genotype
     * @return BitSet
     */
    public int[] getIGenotype() {
        return this.m_Genotype;
    }

    /** This method will allow the user to set the current GI genotype.
     * Use this method with care, since the object is returned when using
     * getIGenotype() you can directly alter the genotype without using
     * this method.
     * @param b    The new genotype of the Individual
     */
    public void SetIGenotype(int[] b) {
        this.m_Genotype = b;
    }

    /** This method allows the user to read the length of the genotype.
     * This may be necessary since BitSet.lenght only returns the index
     * of the last significat bit.
     * @return The length of the genotype.
     */
    public int getGenotypeLength() {
        return this.m_Genotype.length;
    }

    /** This method performs a simple one point mutation in the genotype
     */
    public void defaultMutate() {
        int mutationIndex = RNG.randomInt(0, this.m_Genotype.length-1);
        this.m_Genotype[mutationIndex] = RNG.randomInt(this.m_Range[mutationIndex][0], this.m_Range[mutationIndex][1]);
    }

    /** This method initializes the GA genotype randomly
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
        return "GI individual";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a GI individual suited to optimize int values.";
    }

}