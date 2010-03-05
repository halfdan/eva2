package eva2.server.go.individuals;


import java.util.BitSet;

import eva2.server.go.operators.crossover.CrossoverGANPoint;
import eva2.server.go.operators.crossover.InterfaceCrossover;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.MutateGAStandard;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/** This individual uses a binary genotype to code for binary values.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 14:08:24
 * To change this template use Options | File Templates.
 */
public class GAIndividualBinaryData extends AbstractEAIndividual implements InterfaceDataTypeBinary, InterfaceGAIndividual, java.io.Serializable {

    protected BitSet                m_Genotype  = new BitSet();
    protected BitSet                m_Phenotype = new BitSet();
    protected int                   m_GenotypeLength;

    public GAIndividualBinaryData() {
        this.m_MutationProbability  = 0.1;
        this.m_MutationOperator     = new MutateGAStandard();
        this.m_CrossoverProbability = 1.0;
        this.m_CrossoverOperator    = new CrossoverGANPoint();
        this.m_GenotypeLength       = 20;
        this.m_Genotype             = new BitSet();
    }

    public GAIndividualBinaryData(GAIndividualBinaryData individual) {
        if (individual.m_Phenotype != null)
            this.m_Phenotype                = (BitSet) individual.m_Phenotype.clone();
        this.m_GenotypeLength           = individual.m_GenotypeLength;
        if (individual.m_Genotype != null)
            this.m_Genotype                 = (BitSet)individual.m_Genotype.clone();

        // cloning the members of AbstractEAIndividual
        this.m_Age                      = individual.m_Age;
        this.m_CrossoverOperator        = (InterfaceCrossover)individual.m_CrossoverOperator.clone();
        this.m_CrossoverProbability     = individual.m_CrossoverProbability;
        this.m_MutationOperator         = (InterfaceMutation)individual.m_MutationOperator.clone();
        this.m_MutationProbability      = individual.m_MutationProbability;
        this.m_SelectionProbability     = new double[individual.m_SelectionProbability.length];
        for (int i = 0; i < this.m_SelectionProbability.length; i++) {
            this.m_SelectionProbability[i] = individual.m_SelectionProbability[i];
        }
        this.m_Fitness                  = new double[individual.m_Fitness.length];
        for (int i = 0; i < this.m_Fitness.length; i++) {
            this.m_Fitness[i] = individual.m_Fitness[i];
        }
        cloneAEAObjects((AbstractEAIndividual) individual);
    }

    public Object clone() {
        return (Object) new GAIndividualBinaryData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAIndividualBinaryData) {
            GAIndividualBinaryData indy = (GAIndividualBinaryData) individual;
            if (this.m_GenotypeLength != indy.m_GenotypeLength) return false;
            if ((this.m_Genotype == null) || (indy.m_Genotype == null)) return false;            
            if (!this.m_Genotype.equals(indy.m_Genotype)) return false;
            return true;
        } else {
            return false;
        }
    }

    /** This method evaluates the GAIndividual as simple minimize number
     * of bits problem.
     * @return The number of true bits
     */
    public double defaultEvaulateAsMiniBits() {
        double  result = 0;
        for (int i = 0; i < this.m_GenotypeLength; i++) {
            if (this.m_Genotype.get(i)) result++;
        }
        return result;
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
            System.out.println("Initial value for GAIndividualBinaryData is no BitSet!");
        }
        this.m_MutationOperator.init(this, opt);
        this.m_CrossoverOperator.init(this, opt);
    }

    /** This method can be used to read the current fitness of the individual.
     * Please note that the fitness can be based on multiple criteria therefore
     * double[] is used instead of a single double.
     * @return The complete fitness array
     */
    public double[] getFitness() {
        return this.m_Fitness;
    }

    /** This method will return a string description of the GAIndividal
     * noteably the Genotype.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "GAIndividual: (";
      result += "Fitness {";
        for (int i = 0; i < this.m_Fitness.length; i++) result += this.m_Fitness[i] + ";";
        result += "}/SelProb{";
        for (int i = 0; i < this.m_SelectionProbability.length; i++) result += this.m_SelectionProbability[i] + ";";
        result += "})\n Value: ";
        result += "{";
        for (int i = 0; i < this.m_GenotypeLength; i++) {
            if (this.m_Genotype.get(i)) result += "1";
            else result += "0";
        }
        result += "}";
        result += "\n Mutation ("+this.m_MutationProbability+"):" + this.m_MutationOperator.getStringRepresentation();
        return result;
    }
/************************************************************************************
 * InterfaceGAIndividual methods
 */

    /** This method allows you to read the binary data
     * @return BitSet representing the binary data.
     */
    public BitSet getBGenotype() {
        return this.m_Genotype;
    }

    /** This method allows you to set the binary data, this can be used for
     * memetic algorithms.
     * @param binaryData    The new binary data.
     */
    public void SetBGenotype(BitSet binaryData) {
        this.m_Genotype = binaryData;
    }

    /** This method allows the user to read the length of the genotype.
     * This may be necessary since BitSet.length only returns the index
     * of the last significant bit.
     * @return The length of the genotype.
     */
    public int getGenotypeLength() {
        return this.m_GenotypeLength;
    }

    public void defaultInit(InterfaceOptimizationProblem prob) {
        for (int i = 0; i < this.m_GenotypeLength; i++) {
            if (RNG.flipCoin(0.5)) this.m_Genotype.set(i);
            else this.m_Genotype.clear(i);
        }
    }

    /** This method performs a simple one point mutation in the genotype
     */
    public void defaultMutate() {
        int mutationIndex = RNG.randomInt(0, this.m_GenotypeLength);
        //if (mutationIndex > 28) System.out.println("Mutate: " + this.getSolutionRepresentationFor());
        if (this.m_Genotype.get(mutationIndex)) this.m_Genotype.clear(mutationIndex);
        else this.m_Genotype.set(mutationIndex);
        //if (mutationIndex > 28) System.out.println(this.getSolutionRepresentationFor());
    }

/************************************************************************************
 * InterfaceDataTypeBinary methods
 */
    /** This method allows you to request a certain amount of binary data
     * @param length    The lenght of the BitSet that is to be optimized
     */
    public void setBinaryDataLength(int length) {
        this.m_GenotypeLength = length;
    }

    /** This method returns the length of the binary data set
     * @return The number of bits stored
     */
    public int size() {
        return this.m_GenotypeLength;
    }

    /** This method allows you to read the binary data
     * @return BitSet representing the binary data.
     */
    public BitSet getBinaryData() {
        this.m_Phenotype = (BitSet)this.m_Genotype.clone();
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
        this.m_Genotype =(BitSet)binaryData.clone();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GA binary individual";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a GA individual suited to optimize binary values.";
    }
}
