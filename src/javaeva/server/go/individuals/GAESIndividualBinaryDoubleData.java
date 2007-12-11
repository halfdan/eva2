package javaeva.server.go.individuals;

import javaeva.server.go.operators.mutation.InterfaceMutation;
import javaeva.server.go.populations.Population;
import javaeva.server.go.problems.InterfaceOptimizationProblem;
import javaeva.server.go.tools.RandomNumberGenerator;

import java.util.BitSet;

/** This individual combines a binary and a real-valued phenotype.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.05.2003
 * Time: 11:35:48
 * To change this template use Options | File Templates.
 */
public class GAESIndividualBinaryDoubleData extends AbstractEAIndividual implements InterfaceDataTypeBinary, InterfaceDataTypeDouble, java.io.Serializable {

    private InterfaceDataTypeDouble     m_Numbers   = new ESIndividualDoubleData();
    private InterfaceDataTypeBinary     m_BitSet    = new GAIndividualBinaryData();

    public GAESIndividualBinaryDoubleData() {
        this.m_MutationProbability  = 1.0;
        this.m_CrossoverProbability = 1.0;
        this.m_Numbers   = new GAIndividualDoubleData();
        this.m_BitSet    = new GAIndividualBinaryData();
    }

    public GAESIndividualBinaryDoubleData(GAESIndividualBinaryDoubleData individual) {
        this.m_Numbers  = (InterfaceDataTypeDouble)((AbstractEAIndividual)individual.getNumbers()).clone();
        this.m_BitSet   = (InterfaceDataTypeBinary)((AbstractEAIndividual)individual.getBitSet()).clone();

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
        return (Object) new GAESIndividualBinaryDoubleData(this);
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAESIndividualBinaryDoubleData) {
            GAESIndividualBinaryDoubleData indy = (GAESIndividualBinaryDoubleData)individual;
            if (!((AbstractEAIndividual)this.m_Numbers).equalGenotypes((AbstractEAIndividual)indy.m_Numbers)) return false;
            if (!((AbstractEAIndividual)this.m_BitSet).equalGenotypes((AbstractEAIndividual)indy.m_BitSet)) return false;
            return true;
        } else {
            return false;
        }
    }

    /** This method will allow a default initialisation of the individual
     * @param opt   The optimization problem that is to be solved.
     */
    public void init(InterfaceOptimizationProblem opt) {
        ((AbstractEAIndividual)this.m_Numbers).init(opt);
        ((AbstractEAIndividual)this.m_BitSet).init(opt);
    }

    /** This method will init the individual with a given value for the
     * phenotype.
     * @param obj   The initial value for the phenotype
     * @param opt   The optimization problem that is to be solved.
     */
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof Object[]) {
            if (((Object[])obj)[0] instanceof double[]) {
                ((AbstractEAIndividual)this.m_Numbers).initByValue(((Object[])obj)[0], opt);
                ((AbstractEAIndividual)this.m_BitSet).initByValue(((Object[])obj)[1], opt);
            } else {
                ((AbstractEAIndividual)this.m_Numbers).initByValue(((Object[])obj)[1], opt);
                ((AbstractEAIndividual)this.m_BitSet).initByValue(((Object[])obj)[0], opt);
            }
        } else {
            ((AbstractEAIndividual)this.m_Numbers).init(opt);
            ((AbstractEAIndividual)this.m_BitSet).init(opt);
            System.out.println("Initial value for GAESIndividualDoubleData is not suitable!");
        }
    }

    /** This method will mutate the individual randomly
     */
    public void mutate() {
        if (RandomNumberGenerator.flipCoin(this.m_MutationProbability))((AbstractEAIndividual)this.m_Numbers).mutate();
        if (RandomNumberGenerator.flipCoin(this.m_MutationProbability))((AbstractEAIndividual)this.m_BitSet).mutate();
    }

    /** This method will mate the Individual with given other individuals
     * of the same type.
     * @param partners  The possible partners
     * @return offsprings
     */
    public AbstractEAIndividual[] mateWith(Population partners) {
        AbstractEAIndividual[] result;
        if (RandomNumberGenerator.flipCoin(this.m_CrossoverProbability)) {
            AbstractEAIndividual[]  resNum, resBin;
            AbstractEAIndividual    numTmp, binTmp;
            Population              numPop, binPop;

    //        String out  = "Input: \n";
    //        out += this.getSolutionRepresentationFor() + "\n" + partners.getSolutionRepresentationFor();
    //        System.out.println(out);

            numTmp = (AbstractEAIndividual)this.getNumbers();
            numPop = new Population();
            for (int i = 0; i < partners.size(); i++) numPop.add(((GAESIndividualBinaryDoubleData)partners.get(i)).getNumbers());
            resNum = numTmp.mateWith(numPop);

            binTmp = (AbstractEAIndividual)this.getBitSet();
            binPop = new Population();
            for (int i = 0; i < partners.size(); i++) binPop.add(((GAESIndividualBinaryDoubleData)partners.get(i)).getBitSet());
            resBin = binTmp.mateWith(binPop);

            result = new GAESIndividualBinaryDoubleData[resNum.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = new GAESIndividualBinaryDoubleData(this);
                ((GAESIndividualBinaryDoubleData)result[i]).setNumbers((InterfaceDataTypeDouble)resNum[i]);
                ((GAESIndividualBinaryDoubleData)result[i]).setBitSet((InterfaceDataTypeBinary)resBin[i]);
            }

    //        result = ((AbstractEAIndividual)this.m_Numbers).mateWith(partners);
    //        AbstractEAIndividual dad = (AbstractEAIndividual)result[0];
    //        Population tpartners = new Population();
    //        for (int i = 1; i < result.length; i++) tpartners.add(result[i]);
    //        result = dad.mateWith(tpartners);

    //        out  = "Result:\n";
    //        for (int i = 0; i < result.length; i++) out += result[i].getSolutionRepresentationFor() + "\n";
    //        System.out.println(out);

        } else {
            // simply return a number of perfect clones
            result = new AbstractEAIndividual[partners.size() +1];
            result[0] = (AbstractEAIndividual)this.clone();
            for (int i = 0; i < partners.size(); i++) {
                result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
            }
        }
        for (int i = 0; i < result.length; i++) result[i].giveNewName();
        return result;
    }

    /** This method will return a string description of the GAIndividal
     * noteably the Genotype.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
        String result = "This is a hybrid Individual:\n";
        result += "The Numbers Part:\n"+((AbstractEAIndividual)this.m_Numbers).getStringRepresentation();
        result += "\nThe Binarys Part:\n"+((AbstractEAIndividual)this.m_BitSet).getStringRepresentation();
        return result;
    }

/**********************************************************************************************************************
 * These are for InterfaceDataTypeDouble

    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    public void setDoubleDataLength (int length) {
        this.m_Numbers.setDoubleDataLength(length);
        this.m_BitSet.setBinaryDataLength(length);
    }

    /** This method returns the length of the double data set
     * @return The number of bits stored
     */
    public int size() {
        return this.m_Numbers.size();
    }

    /** This method will set the range of the double attributes.
     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
     * for dimension d.
     * @param range     The new range for the double data.
     */
    public void SetDoubleRange(double[][] range) {
        this.m_Numbers.SetDoubleRange(range);
    }

    /** This method will return the range for all double attributes.
     * @return The range array.
     */
    public double[][] getDoubleRange() {
        return this.m_Numbers.getDoubleRange();
    }

    /** This method allows you to read the double data
     * @return BitSet representing the double data.
     */
    public double[] getDoubleData() {
        return this.m_Numbers.getDoubleData();
    }

    /** This method allows you to read the double data without
     * an update from the genotype
     * @return double[] representing the double data.
     */
    public double[] getDoubleDataWithoutUpdate() {
        return this.m_Numbers.getDoubleDataWithoutUpdate();
    }

    /** This method allows you to set the double data.
     * @param doubleData    The new double data.
     * @see InterfaceDataTypeDouble.SetDoubleData()
     */
    public void SetDoubleData(double[] doubleData) {
        this.m_Numbers.SetDoubleData(doubleData);
    }

    /** This method allows you to set the double data, this can be used for
     * memetic algorithms.
     * @param doubleData    The new double data.
     * @see InterfaceDataTypeDouble.SetDoubleDataLamarkian()
     */
    public void SetDoubleDataLamarkian(double[] doubleData) {
        this.m_Numbers.SetDoubleDataLamarkian(doubleData);
    }

/**********************************************************************************************************************
 * These are for   InterfaceDataTypeBinary

    /** This method allows you to request a certain amount of binary data
     * @param length    The lenght of the BitSet that is to be optimized
     */
    public void setBinaryDataLength (int length) {
        this.m_Numbers.setDoubleDataLength(length);
        this.m_BitSet.setBinaryDataLength(length);
    }

    /** This method returns the length of the binary data set
     * @return The number of bits stored
     */
    public int GetBinaryDataLength() {
        return this.m_BitSet.size();
    }

    /** This method allows you to read the binary data
     * @return BitSet representing the binary data.
     */
    public BitSet getBinaryData() {
        return this.m_BitSet.getBinaryData();
    }

    /** This method allows you to read the binary data without
     * an update from the genotype
     * @return BitSet representing the binary data.
     */
    public BitSet getBinaryDataWithoutUpdate() {
        return this.m_BitSet.getBinaryDataWithoutUpdate();
    }

    /** This method allows you to set the binary data.
     * @param binaryData    The new binary data.
     * @see InterfaceDataTypeBinary.SetBinaryData()
     */
    public void SetBinaryData(BitSet binaryData) {
        this.m_BitSet.SetBinaryData(binaryData);
    }

    /** This method allows you to set the binary data, this can be used for
     * memetic algorithms.
     * @param binaryData    The new binary data.
     * @see InterfaceBinaryData.SetBinaryDataLamarkian()
     */
    public void SetBinaryDataLamarkian(BitSet binaryData) {
        this.m_BitSet.SetBinaryDataLamarkian(binaryData);
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GA/ES indiviudal";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a mixed data type combining a BitSet and a real-valued vector.";
    }

    /** This method will allow you to set the inner constants
     * @param Numbers     The new representation for the inner constants.
      */
    public void setNumbers(InterfaceDataTypeDouble Numbers) {
        this.m_Numbers = Numbers;
    }
    public InterfaceDataTypeDouble getNumbers() {
        return this.m_Numbers;
    }
    public String numbersTipText() {
        return "Choose the type of inner binary representation to use.";
    }
    /** This method will allow you to set the inner constants
     * @param BitSet     The new representation for the inner constants.
      */
    public void setBitSet(InterfaceDataTypeBinary BitSet) {
        this.m_BitSet = BitSet;
    }
    public InterfaceDataTypeBinary getBitSet() {
        return this.m_BitSet;
    }
    public String bitSetTipText() {
        return "Choose the type of inner real-valued representation to use.";
    }
}