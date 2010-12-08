package eva2.server.go.individuals;

import java.util.BitSet;

import eva2.server.go.operators.crossover.CrossoverESDefault;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateESGlobal;
import eva2.server.go.problems.InterfaceHasInitRange;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;


/** This individual uses a real-valued genotype to code for binary values, either
 * by using a threshold value of by interpreting the double value as probability.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 02.07.2003
 * Time: 10:37:43
 * To change this template use Options | File Templates.
 */
public class ESIndividualBinaryData extends AbstractEAIndividual implements InterfaceESIndividual, InterfaceDataTypeBinary, java.io.Serializable {

    private BitSet                      m_Phenotype = new BitSet();
    private double[]                    m_Genotype;
    private boolean                     m_UseHardSwitch = false;
    private double[][]                  m_Range;

    public ESIndividualBinaryData() {
        this.m_MutationProbability  = 1.0;
        this.m_MutationOperator     = new MutateESGlobal();
        this.m_CrossoverProbability = 0.5;
        this.m_CrossoverOperator    = new CrossoverESDefault();
        this.m_Genotype             = new double[1];
        this.m_Range                = new double[1][2];
        this.m_Range[0][0]          = 0;
        this.m_Range[0][1]          = 1;
    }

    public ESIndividualBinaryData(ESIndividualBinaryData individual) {
        if (individual.m_Phenotype != null)
            this.m_Phenotype                = (BitSet) individual.m_Phenotype.clone();
        this.m_Genotype                 = new double[individual.m_Genotype.length];
        this.m_Range                    = new double[individual.m_Genotype.length][2];
        for (int i = 0; i < this.m_Genotype.length; i++) {
            this.m_Genotype[i]              = individual.m_Genotype[i];
            this.m_Range[i][0]              = individual.m_Range[i][0];
            this.m_Range[i][1]              = individual.m_Range[i][1];
        }
        this.m_UseHardSwitch            = individual.m_UseHardSwitch;

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
        return (Object) new ESIndividualBinaryData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof ESIndividualBinaryData) {
            ESIndividualBinaryData indy = (ESIndividualBinaryData) individual;
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
 * InterfaceDataTypeBinary methods
 */
    /** This method allows you to request a certain amount of binary data
     * @param length    The lenght of the BitSet that is to be optimized
     */
    public void setBinaryDataLength(int length) {
        this.m_Genotype         = new double[length];
        this.m_Range            = new double[length][2];
        for (int i = 0; i < this.m_Range.length; i++) {
            this.m_Range[i][0] = 0;
            this.m_Range[i][1] = 1;
        }
    }

    /** This method returns the length of the binary data set
     * @return The number of bits stored
     */
    public int size() {
        return this.m_Genotype.length;
    }

    /** This method allows you to read the binary data
     * @return BitSet representing the binary data.
     */
    public BitSet getBinaryData() {
        if (this.m_UseHardSwitch) {
            // In this case it is only tested if the genotyp is bigger than 0.5
            for (int i = 0; i < this.m_Genotype.length; i++) {
                if (this.m_Genotype[i] > 0.5) {
                    this.m_Phenotype.set(i);
                } else {
                    this.m_Phenotype.clear(i);
                }
            }
        } else {
            // in this case the value of the genotype is interpreted as a probability
            for (int i = 0; i < this.m_Genotype.length; i++) {
                if (RNG.flipCoin(this.m_Genotype[i])) {
                    this.m_Phenotype.set(i);
                } else {
                    this.m_Phenotype.clear(i);
                }
            }
        }
        return this.m_Phenotype;
    }

    /** This method allows you to read the binary data without
     * an update from the genotype
     * @return BitSet representing the binary data.
     */
    public BitSet getBinaryDataWithoutUpdate() {
        return this.m_Phenotype;
    }

    /** This method allows you to set the binary data.
     * @param binaryData    The new binary data.
     */
    public void SetBinaryPhenotype(BitSet binaryData) {
        this.m_Phenotype = binaryData;
    }
    /** This method allows you to set the binary data, this can be used for
     * memetic algorithms.
     * @param binaryData    The new binary data.
     */
    public void SetBinaryGenotype(BitSet binaryData) {
        this.SetBinaryPhenotype(binaryData);
        for (int i = 0; i < this.m_Genotype.length; i++) {
            if (this.m_UseHardSwitch) {
                if (binaryData.get(i)) this.m_Genotype[i] = RNG.randomDouble(0.55,1.0);
                else this.m_Genotype[i] = RNG.randomDouble(0.0,0.45);
            } else {
                if (binaryData.get(i)) this.m_Genotype[i] = 0.9;
                else this.m_Genotype[i] = 0.1;
            }
        }
    }

/************************************************************************************
 * AbstractEAIndividual methods
 */
    /** This method will init the individual with a given value for the
     * phenotype.
     * @param obj   The initial value for the phenotype
     * @param opt   The optimization problem that is to be solved.
     */
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof BitSet) {
            BitSet  bs = (BitSet) obj;
            this.SetBinaryGenotype(bs);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for ESIndividualBinaryData is no BitSet!");
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
        result += "ESIndividual coding double: (";
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
            if (this.m_Genotype[i] < this.m_Range[i][0]) this.m_Genotype[i] = this.m_Range[1][0];
            if (this.m_Genotype[i] > this.m_Range[i][1]) this.m_Genotype[i] = this.m_Range[1][1];
        }
    }

//    /** This method will set the range of the double attributes. If range.length
//     * does not equal doubledata.length only range[i] will be used to set all
//     * ranges.
//     * @param range     The new range for the double data.
//     */
//    public void SetDoubleRange(double[][] range) {
//        this.m_Range = range;
//    }

    /** This method will return the range for all double attributes.
     * @return The range array.
     */
    public double[][] getDoubleRange() {
        return this.m_Range;
    }

    /** This method performs a simple one element mutation on the double vector
     */
    public void defaultMutate() {
    	ESIndividualDoubleData.defaultMutate(m_Genotype, m_Range);
    }

    public void defaultInit(InterfaceOptimizationProblem prob) {
    	if ((prob != null) && (prob instanceof InterfaceHasInitRange) && (((InterfaceHasInitRange)prob).getInitRange()!=null)) ESIndividualDoubleData.defaultInit(m_Genotype, (double[][])((InterfaceHasInitRange)prob).getInitRange());
    	else ESIndividualDoubleData.defaultInit(m_Genotype, m_Range);
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "ES individual";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is an ES individual adopted to optimize binary values.";
    }

    /** This method will toggle between genotype interpretation as bit probability and
     * fixed switch.
     * @param b     the Switch.
     */
    public void setToggleInterpretation(boolean b) {
        this.m_UseHardSwitch = b;
    }
    public boolean getToggleInterpretation() {
        return this.m_UseHardSwitch;
    }
    public String toggleInterpretationTipText() {
        return "Toggle between interpretation as probability or if(>0.5).";
    }
}
