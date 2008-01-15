package javaeva.server.go.individuals;

import javaeva.server.go.individuals.codings.ga.GAStandardCodingDouble;
import javaeva.server.go.individuals.codings.ga.GAStandardCodingInteger;
import javaeva.server.go.individuals.codings.ga.InterfaceGADoubleCoding;
import javaeva.server.go.individuals.codings.ga.InterfaceGAIntegerCoding;
import javaeva.server.go.operators.crossover.CrossoverGANPoint;
import javaeva.server.go.operators.mutation.InterfaceMutation;
import javaeva.server.go.operators.mutation.MutateGAStandard;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.tools.RandomNumberGenerator;

import java.util.BitSet;

/** This individual uses a binary genotype to code for binary values using
 * two alternative encodings.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.05.2004
 * Time: 11:16:33
 * To change this template use File | Settings | File Templates.
 */
public class GAIndividualIntegerData extends AbstractEAIndividual implements InterfaceGAIndividual, InterfaceDataTypeInteger, java.io.Serializable {

    private int[]                       m_Phenotype;
    private int[][]                     m_Range;
    protected BitSet                    m_Genotype;
    protected int[]                     m_CodingLenghts;
    private InterfaceGAIntegerCoding    m_IntegerCoding          = new GAStandardCodingInteger();

    public GAIndividualIntegerData() {
        this.m_MutationProbability  = 0.1;
        this.m_MutationOperator     = new MutateGAStandard();
        this.m_CrossoverProbability = 0.7;
        this.m_CrossoverOperator    = new CrossoverGANPoint();
        this.m_Range                = new int[1][2];
        this.m_CodingLenghts        = new int[1];
        this.m_CodingLenghts[0]     = 3;
        this.m_Range[0][0]          = 0;
        this.m_Range[0][1]          = 7;
        this.m_Genotype             = new BitSet();
    }

    public GAIndividualIntegerData(GAIndividualIntegerData individual) {
        if (individual.m_Phenotype != null) {
            this.m_Phenotype            = new int[individual.m_Phenotype.length];
            System.arraycopy(individual.m_Phenotype, 0, this.m_Phenotype, 0, this.m_Phenotype.length);
        }
        this.m_Genotype         = (BitSet) individual.m_Genotype.clone();
        this.m_Range            = new int[individual.m_Range.length][2];
        this.m_CodingLenghts    = new int[individual.m_CodingLenghts.length];
        for (int i = 0; i < this.m_Range.length; i++) {
            this.m_CodingLenghts[i] = individual.m_CodingLenghts[i];
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
        this.m_IntegerCoding            = individual.m_IntegerCoding;
        this.m_Fitness = new double[individual.m_Fitness.length];
        for (int i = 0; i < this.m_Fitness.length; i++) {
            this.m_Fitness[i] = individual.m_Fitness[i];
        }
        cloneAEAObjects((AbstractEAIndividual) individual);
    }

    public Object clone() {
        return (Object) new GAIndividualIntegerData(this);
    }


    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAIndividualIntegerData) {
            GAIndividualIntegerData indy = (GAIndividualIntegerData) individual;
            //@todo Eigendlich k�nnte ich noch das Koding vergleichen
            if ((this.m_Genotype == null) || (indy.m_Genotype == null)) return false;
            if (!this.m_Genotype.equals(indy.m_Genotype)) return false;
            if (this.m_Range.length != indy.m_Range.length) return false;
            for (int i = 0; i < this.m_Range.length; i++) {
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
        for (int i = 0; ((i < newDesPa.length) && (i < this.m_Range.length)); i++) {
            newRange[i][0]  = this.m_Range[i][0];
            newRange[i][1]  = this.m_Range[i][1];
        }

        // if the new length is bigger than the last value fills the extra elements
        for (int i = this.m_Range.length; (i < newDesPa.length); i++) {
            newRange[i][0]  = this.m_Range[this.m_Range.length-1][0];
            newRange[i][1]  = this.m_Range[this.m_Range.length-1][1];
        }
        this.m_Range            = newRange;
        this.m_CodingLenghts    = new int[this.m_Range.length];
        for (int i = 0; i < this.m_Range.length; i++) {
            this.m_CodingLenghts[i] = this.m_IntegerCoding.calculateNecessaryBits(this.m_Range[i]);
        }
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
        int[] locus = new int[2];
        locus[0] = 0;
        locus[1] = 0;
        this.m_Phenotype = new int[this.m_Range.length];
        for (int i = 0; i < this.m_Phenotype.length; i++) {
            locus[0] += locus[1];
            locus[1] = this.m_CodingLenghts[i];
            this.m_Phenotype[i] = this.m_IntegerCoding.decodeValue(this.m_Genotype, this.m_Range[i], locus, false);
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
        if (doubleData != null) {
	        int[] locus = new int[2];
	        locus[0] = 0;
	        locus[1] = 0;
	        for (int i = 0; i < doubleData.length; i++) {
	            locus[0] += locus[1];
	            locus[1] = this.m_CodingLenghts[i];
	            this.m_IntegerCoding.codeValue(doubleData[i], this.m_Range[i], this.m_Genotype, locus);
	        }
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

    /** This method will mutate the individual randomly
     */
    public void mutate() {
        if (RandomNumberGenerator.flipCoin(this.m_MutationProbability)) this.m_MutationOperator.mutate(this);
    }

    /** This method will mate the Individual with given other individuals
     * of the same type.
     * @param partners  The possible partners
     * @return offsprings
     */
    public AbstractEAIndividual[] mateWith(Population partners) {
        AbstractEAIndividual[] result;
        if (RandomNumberGenerator.flipCoin(this.m_CrossoverProbability)) {
            result = this.m_CrossoverOperator.mate(this, partners);
        } else {
            // simply return a number of perfect clones
            result = new AbstractEAIndividual[partners.size() +1];
            result[0] = (AbstractEAIndividual)this.clone();
            for (int i = 0; i < partners.size(); i++) {
                result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
            }
            return result;
        }
        for (int i = 0; i < result.length; i++) result[i].giveNewName();
        return result;
    }

    /** This method will return a string description of the GAIndividal
     * noteably the Genotype.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "";
        result += "GAIndividual coding int: (";
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
        result += "CodingLength: [";
        for (int i = 0; i < this.m_CodingLenghts.length; i++) {
            result += this.m_CodingLenghts[i] + "; ";
        }
        result += "]\n";
        result += "{";
        int overallLength = 0;
        for (int i = 0; i < this.m_CodingLenghts.length; i++) overallLength += this.m_CodingLenghts[i];
        for (int i = 0; i < overallLength; i++) {
            if (this.m_Genotype.get(i)) result += "1";
            else result += "0";
        }
        result += "}";
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
     * This may be necessary since BitSet.lenght only returns the index
     * of the last significat bit.
     * @return The length of the genotype.
     */
    public int getGenotypeLength() {
        int overallLength = 0;
        for (int i = 0; i < this.m_CodingLenghts.length; i++) overallLength += this.m_CodingLenghts[i];
        return overallLength;
    }

    /** This method inits the genotpye of the individual
     */
    public void defaultInit() {
        int overallLength = 0;
        for (int i = 0; i < this.m_CodingLenghts.length; i++) overallLength += this.m_CodingLenghts[i];
        for (int i = 0; i < overallLength; i++) {
            if (RandomNumberGenerator.flipCoin(0.5)) this.m_Genotype.set(i);
            else this.m_Genotype.clear(i);
        }
    }

    /** This method performs a simple one point mutation in the genotype
     */
    public void defaultMutate() {
        int overallLength = 0;
        for (int i = 0; i < this.m_CodingLenghts.length; i++) overallLength += this.m_CodingLenghts[i];
        int mutationIndex = RandomNumberGenerator.randomInt(0, overallLength);
        if (this.m_Genotype.get(mutationIndex)) this.m_Genotype.clear(mutationIndex);
        else this.m_Genotype.set(mutationIndex);
    }

    public static void main(String[] args) {
        System.out.println("Test this stuff!");
        GAIndividualIntegerData indy = new GAIndividualIntegerData();
        int     dimension = 10;
        int[][] range = new int[dimension][2];
        for (int i = 0; i < dimension; i++) {
            range[i][0] = 0;
            range[i][1] = i+1;
        }
        indy.setIntegerDataLength(dimension);
        indy.SetIntRange(range);
        indy.defaultInit();
        System.out.println(""+indy.getStringRepresentation());
        System.out.println("System.exit(0)");
        int[] data = indy.getIntegerData();
        String tmp = "Before {";
        for (int i = 0; i < data.length; i++) {
            tmp += data[i] +"; ";
        }
        System.out.println(tmp+"}");
        tmp = "Setting {";
        for (int i = 0; i < data.length; i++) {
            data[i] = RandomNumberGenerator.randomInt(range[i][0], range[i][1]);
            tmp += data[i] + "; ";
        }
        System.out.println(tmp+"}");
        indy.SetIntegerDataLamarkian(data);
        System.out.println(""+indy.getStringRepresentation());
        data = indy.getIntegerData();
        tmp = "After {";
        for (int i = 0; i < data.length; i++) {
            tmp += data[i] +"; ";
        }
        System.out.println(tmp+"}");
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GA indiviudal";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a GA individual suited to optimize int values.";
    }

    /** This method allows you to set the Coding that is to be used, currently either standard binary
     * coding or Gray coding.
     * @param coding The used genotype coding method
     */
    public void setGACoding(InterfaceGAIntegerCoding coding) {
        this.m_IntegerCoding = coding;
    }
    public InterfaceGAIntegerCoding getGACoding() {
        return this.m_IntegerCoding;
    }
    public String gAIntegerCodingTipText() {
        return "Choose the coding to use.";
    }
}
