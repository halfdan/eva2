package eva2.optimization.individuals;


import eva2.optimization.individuals.codings.ga.GAStandardCodingDouble;
import eva2.optimization.individuals.codings.ga.InterfaceGADoubleCoding;
import eva2.optimization.operator.crossover.CrossoverGAGINPoint;
import eva2.optimization.operator.crossover.InterfaceCrossover;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateGAUniform;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import java.util.BitSet;

/** This individual uses a binary genotype to code for double values
 * using two alternative encodings.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 16:00:30
 * To change this template use Options | File Templates.
 */
public class GAIndividualDoubleData extends AbstractEAIndividual implements InterfaceGAIndividual, InterfaceDataTypeDouble, java.io.Serializable {

    private double[]                    m_Phenotype;
    private double[][]                  m_Range;
    protected BitSet                    m_Genotype;
    protected int                       m_GenotypeLength;
    private int                         m_Precision             = 32;
    private InterfaceGADoubleCoding     m_DoubleCoding          = new GAStandardCodingDouble();

    public GAIndividualDoubleData() {
        this.mutationProbability = 0.1;
        this.mutationOperator = new MutateGAUniform();
        this.crossoverProbability = 0.7;
        this.crossoverOperator = new CrossoverGAGINPoint();
        this.m_Range                = new double[1][2];
        this.m_Range[0][0]          = -10;
        this.m_Range[0][1]          = 10;
        this.m_GenotypeLength       = this.m_Precision;
        this.m_Genotype             = new BitSet();
    }

    public GAIndividualDoubleData(GAIndividualDoubleData individual) {
        if (individual.m_Phenotype != null) {
            this.m_Phenotype            = new double[individual.m_Phenotype.length];
            System.arraycopy(individual.m_Phenotype, 0, this.m_Phenotype, 0, this.m_Phenotype.length);
        }
        this.m_GenotypeLength   = individual.m_GenotypeLength;
        this.m_Genotype         = (BitSet) individual.m_Genotype.clone();
        this.m_Range            = new double[individual.m_Range.length][2];
        for (int i = 0; i < this.m_Range.length; i++) {
            this.m_Range[i][0]      = individual.m_Range[i][0];
            this.m_Range[i][1]      = individual.m_Range[i][1];
        }

        // cloning the members of AbstractEAIndividual
        this.age = individual.age;
        this.crossoverOperator = (InterfaceCrossover)individual.crossoverOperator.clone();
        this.crossoverProbability = individual.crossoverProbability;
        this.mutationOperator = (InterfaceMutation)individual.mutationOperator.clone();
        this.mutationProbability = individual.mutationProbability;
        this.selectionProbability = new double[individual.selectionProbability.length];
        for (int i = 0; i < this.selectionProbability.length; i++) {
            this.selectionProbability[i] = individual.selectionProbability[i];
        }
        this.m_Precision                = individual.m_Precision;
        this.m_DoubleCoding             = individual.m_DoubleCoding;
        this.fitness = new double[individual.fitness.length];
        for (int i = 0; i < this.fitness.length; i++) {
            this.fitness[i] = individual.fitness[i];
        }
        cloneAEAObjects((AbstractEAIndividual) individual);
    }

    @Override
    public Object clone() {
        return (Object) new GAIndividualDoubleData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAIndividualDoubleData) {
            GAIndividualDoubleData indy = (GAIndividualDoubleData) individual;
            //@todo Eigendlich k�nnte ich noch das Koding vergleichen
            if (this.m_GenotypeLength != indy.m_GenotypeLength) {
                return false;
            }
            if ((this.m_Genotype == null) || (indy.m_Genotype == null)) {
                return false;
            }            
            if (!this.m_Genotype.equals(indy.m_Genotype)) {
                return false;
            }
            for (int i = 0; i < this.m_Range.length; i++) {
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
 * InterfaceDataTypeDouble methods
 */
    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    @Override
    public void setDoubleDataLength (int length) {
        double[]        newDesPa = new double[length];
        double[][]      newRange = new double[length][2];

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
        this.m_GenotypeLength   = length * this.m_Precision;

//        changed 28.08.03 by request of Spieth
//        this.m_DecisionParameters   = new double[length];
//        this.m_Range                = new double[length][2];
//        for (int i = 0; i < this.m_Range.length; i++) {
//            this.m_Range[i][0] = -10;
//            this.m_Range[i][1] = 10;
//        }
    }

    /** This method returns the length of the double data set
     * @return The number of bits stored
     */
    @Override
    public int size() {
        return this.m_Range.length;
    }

    /** This method will set the range of the double attributes. If range.length
     * does not equal doubledata.length only range[i] will be used to set all
     * ranges.
     * @param range     The new range for the double data.
     */
    @Override
    public void SetDoubleRange(double[][] range) {
        if (range.length != this.m_Range.length) {
            System.out.println("Warning: Trying to set a range of length " + range.length + " to a vector of length "
                    + this.m_Range.length + "!\n Use method setDoubleDataLength first!");
        }
        for (int i = 0; ((i < this.m_Range.length) && (i < range.length)); i++) {
            this.m_Range[i][0] = range[i][0];
            this.m_Range[i][1] = range[i][1];
        }
    }

    /** This method will return the range for all double attributes.
     * @return The range array.
     */
    @Override
    public double[][] getDoubleRange() {
        return this.m_Range;
    }

    /** This method allows you to read the double data
     * @return BitSet representing the double data.
     */
    @Override
    public double[] getDoubleData() {
        int[] locus = new int[2];
        this.m_Phenotype = new double[this.m_Range.length];
        for (int i = 0; i < this.m_Range.length; i++) {
            locus[0] = i * this.m_Precision;
            locus[1] = this.m_Precision;
            this.m_Phenotype[i] = this.m_DoubleCoding.decodeValue(this.m_Genotype, this.m_Range[i], locus, false);
        }
        return this.m_Phenotype;
    }
        
    /** This method allows you to read the double data without
     * an update from the genotype
     * @return double[] representing the double data.
     */
    @Override
    public double[] getDoubleDataWithoutUpdate() {
        return this.m_Phenotype;
    }

    /** This method allows you to set the phenotype data. To change the genotype data, 
     * use SetDoubleDataLamarckian.
     * @param doubleData    The new double data.
     */
    @Override
    public void SetDoublePhenotype(double[] doubleData) {
        this.m_Phenotype = doubleData;
    }

    /** This method allows you to set the double data, this can be used for
     * memetic algorithms.
     * @param doubleData    The new double data.
     */
    @Override
    public void SetDoubleGenotype(double[] doubleData) {
        this.SetDoublePhenotype(doubleData);
        int[] locus = new int[2];
        for (int i = 0; i < doubleData.length; i++) {
            locus[0] = i * this.m_Precision;
            locus[1] = this.m_Precision;
            this.m_DoubleCoding.codeValue(doubleData[i], this.m_Range[i], this.m_Genotype, locus);
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
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof double[]) {
            double[]  bs = (double[]) obj;
            if (bs.length != this.m_Range.length) {
                System.out.println("Init value and requested length doesn't match!");
            }
            this.SetDoubleGenotype(bs);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for GAIndividualDoubleData is not double[]!");
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
        result += "GAIndividual coding double: (";
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
        double[] d = this.getDoubleData();
        for (int i = 0; i < d.length; i++) {
            result += d[i] + "; ";
        }
        result += "]\n";
        result += "{";
        for (int i = 0; i < this.m_GenotypeLength; i++) {
            if (this.m_Genotype.get(i)) {
                result += "1";
            }
            else {
                result += "0";
            }
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
    @Override
    public BitSet getBGenotype() {
        return this.m_Genotype;
    }

    /** This method allows you to set the binary data, this can be used for
     * memetic algorithms.
     * @param binaryData    The new binary data.
     */
    @Override
    public void SetBGenotype(BitSet binaryData) {
        this.m_Genotype = binaryData;
    }

    /** This method allows the user to read the length of the genotype.
     * This may be necessary since BitSet.lenght only returns the index
     * of the last significat bit.
     * @return The length of the genotype.
     */
    @Override
    public int getGenotypeLength() {
        return this.m_GenotypeLength;
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        for (int i = 0; i < this.m_GenotypeLength; i++) {
            if (RNG.flipCoin(0.5)) {
                this.m_Genotype.set(i);
            }
            else {
                this.m_Genotype.clear(i);
            }
        }
    }

    /** This method performs a simple one point mutation in the genotype
     */
    @Override
    public void defaultMutate() {
        int mutationIndex = RNG.randomInt(0, this.m_GenotypeLength);
        if (this.m_Genotype.get(mutationIndex)) {
            this.m_Genotype.clear(mutationIndex);
        }
        else {
            this.m_Genotype.set(mutationIndex);
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
        return "GA individual";
    }

    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is a GA individual suited to optimize double values.";
    }

    /** This method allows you to set the Coding that is to be used, currently either standard binary
     * coding or Gray coding.
     * @param coding The used genotype coding method
     */
    public void setGACoding(InterfaceGADoubleCoding coding) {
        this.m_DoubleCoding = coding;
    }
    public InterfaceGADoubleCoding getGACoding() {
        return this.m_DoubleCoding;
    }
    public String gADoubleCodingTipText() {
        return "Choose the coding to use.";
    }

    /** This method allows you to set the number of mulitruns that are to be performed,
     * necessary for stochastic optimizers to ensure reliable results.
     * @param precision The number of multiruns that are to be performed
     */
    public void setPrecision(int precision) {
        this.m_Precision = precision;
    }
    public int getPrecision() {
        return this.m_Precision;
    }
    public String precisionTipText() {
        return "Gives the number of bits to be used to code a double.";
    }
}
