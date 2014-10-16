package eva2.optimization.individuals;


import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 * This individual combines a binary and a real-valued phenotype.
 */
@Description(value = "This is a mixed data type combining a BitSet and a real-valued vector.")
public class GAESIndividualBinaryDoubleData extends AbstractEAIndividual implements InterfaceDataTypeBinary, InterfaceDataTypeDouble, java.io.Serializable {

    private InterfaceDataTypeDouble doubleIndividual = new ESIndividualDoubleData();
    private InterfaceDataTypeBinary binaryIndividual = new GAIndividualBinaryData();

    public GAESIndividualBinaryDoubleData() {
        this.mutationProbability = 1.0;
        this.crossoverProbability = 1.0;
        this.doubleIndividual = new GAIndividualDoubleData();
        this.binaryIndividual = new GAIndividualBinaryData();
    }

    public GAESIndividualBinaryDoubleData(GAESIndividualBinaryDoubleData individual) {
        this.doubleIndividual = (InterfaceDataTypeDouble) ((AbstractEAIndividual) individual.getNumbers()).clone();
        this.binaryIndividual = (InterfaceDataTypeBinary) ((AbstractEAIndividual) individual.getBitSet()).clone();

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
        return (Object) new GAESIndividualBinaryDoubleData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GAESIndividualBinaryDoubleData) {
            GAESIndividualBinaryDoubleData indy = (GAESIndividualBinaryDoubleData) individual;
            if (!((AbstractEAIndividual) this.doubleIndividual).equalGenotypes((AbstractEAIndividual) indy.doubleIndividual)) {
                return false;
            }
            if (!((AbstractEAIndividual) this.binaryIndividual).equalGenotypes((AbstractEAIndividual) indy.binaryIndividual)) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method will allow a default initialisation of the individual
     *
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void init(InterfaceOptimizationProblem opt) {
        ((AbstractEAIndividual) this.doubleIndividual).init(opt);
        ((AbstractEAIndividual) this.binaryIndividual).init(opt);
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        ((AbstractEAIndividual) this.doubleIndividual).defaultInit(prob);
        ((AbstractEAIndividual) this.binaryIndividual).defaultInit(prob);
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
        if (obj instanceof Object[]) {
            if (((Object[]) obj)[0] instanceof double[]) {
                ((AbstractEAIndividual) this.doubleIndividual).initByValue(((Object[]) obj)[0], opt);
                ((AbstractEAIndividual) this.binaryIndividual).initByValue(((Object[]) obj)[1], opt);
            } else {
                ((AbstractEAIndividual) this.doubleIndividual).initByValue(((Object[]) obj)[1], opt);
                ((AbstractEAIndividual) this.binaryIndividual).initByValue(((Object[]) obj)[0], opt);
            }
        } else {
            ((AbstractEAIndividual) this.doubleIndividual).init(opt);
            ((AbstractEAIndividual) this.binaryIndividual).init(opt);
            System.out.println("Initial value for GAESIndividualDoubleData is not suitable!");
        }
    }

    /**
     * This method will mutate the individual randomly
     */
    @Override
    public void mutate() {
        if (RNG.flipCoin(this.mutationProbability)) {
            ((AbstractEAIndividual) this.doubleIndividual).mutate();
        }
        if (RNG.flipCoin(this.mutationProbability)) {
            ((AbstractEAIndividual) this.binaryIndividual).mutate();
        }
    }

    @Override
    public void defaultMutate() {
        ((AbstractEAIndividual) this.doubleIndividual).defaultMutate();
        ((AbstractEAIndividual) this.binaryIndividual).defaultMutate();
    }

    /**
     * This method will mate the Individual with given other individuals
     * of the same type.
     *
     * @param partners The possible partners
     * @return offsprings
     */
    @Override
    public AbstractEAIndividual[] mateWith(Population partners) {
        AbstractEAIndividual[] result;
        if (RNG.flipCoin(this.crossoverProbability)) {
            AbstractEAIndividual[] resNum, resBin;
            AbstractEAIndividual numTmp, binTmp;
            Population numPop, binPop;

            //        String out  = "Input: \n";
            //        out += this.getSolutionRepresentationFor() + "\n" + partners.getSolutionRepresentationFor();
            //        System.out.println(out);

            numTmp = (AbstractEAIndividual) this.getNumbers();
            numPop = new Population();
            for (int i = 0; i < partners.size(); i++) {
                numPop.add(((GAESIndividualBinaryDoubleData) partners.get(i)).getNumbers());
            }
            resNum = numTmp.mateWith(numPop);

            binTmp = (AbstractEAIndividual) this.getBitSet();
            binPop = new Population();
            for (int i = 0; i < partners.size(); i++) {
                binPop.add(((GAESIndividualBinaryDoubleData) partners.get(i)).getBitSet());
            }
            resBin = binTmp.mateWith(binPop);

            result = new GAESIndividualBinaryDoubleData[resNum.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = new GAESIndividualBinaryDoubleData(this);
                ((GAESIndividualBinaryDoubleData) result[i]).setNumbers((InterfaceDataTypeDouble) resNum[i]);
                ((GAESIndividualBinaryDoubleData) result[i]).setBitSet((InterfaceDataTypeBinary) resBin[i]);
            }

            //        result = ((AbstractEAIndividual)this.doubleIndividual).mateWith(partners);
            //        AbstractEAIndividual dad = (AbstractEAIndividual)result[0];
            //        Population tpartners = new Population();
            //        for (int i = 1; i < result.length; i++) tpartners.add(result[i]);
            //        result = dad.mateWith(tpartners);

            //        out  = "Result:\n";
            //        for (int i = 0; i < result.length; i++) out += result[i].getSolutionRepresentationFor() + "\n";
            //        System.out.println(out);

        } else {
            // simply return a number of perfect clones
            result = new AbstractEAIndividual[partners.size() + 1];
            result[0] = (AbstractEAIndividual) this.clone();
            for (int i = 0; i < partners.size(); i++) {
                result[i + 1] = (AbstractEAIndividual) ((AbstractEAIndividual) partners.get(i)).clone();
            }
        }
        for (int i = 0; i < result.length; i++) {
            result[i].giveNewName();
        }
        return result;
    }

    /**
     * This method will return a string description of the GAIndividal
     * noteably the Genotype.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "This is a hybrid Individual:\n";
        result += "The Numbers Part:\n" + ((AbstractEAIndividual) this.doubleIndividual).getStringRepresentation();
        result += "\nThe Binarys Part:\n" + ((AbstractEAIndividual) this.binaryIndividual).getStringRepresentation();
        return result;
    }

    /**
     * *******************************************************************************************************************
     * These are for InterfaceDataTypeDouble
     * <p/>
     * /** This method allows you to request a certain amount of double data
     *
     * @param length The lenght of the double[] that is to be optimized
     */
    @Override
    public void setDoubleDataLength(int length) {
        this.doubleIndividual.setDoubleDataLength(length);
        this.binaryIndividual.setBinaryDataLength(length);
    }

    /**
     * This method returns the length of the double data set
     *
     * @return The number of bits stored
     */
    @Override
    public int size() {
        return this.doubleIndividual.size();
    }

    /**
     * This method will set the range of the double attributes.
     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
     * for dimension d.
     *
     * @param range The new range for the double data.
     */
    @Override
    public void setDoubleRange(double[][] range) {
        this.doubleIndividual.setDoubleRange(range);
    }

    /**
     * This method will return the range for all double attributes.
     *
     * @return The range array.
     */
    @Override
    public double[][] getDoubleRange() {
        return this.doubleIndividual.getDoubleRange();
    }

    /**
     * This method allows you to read the double data
     *
     * @return BitSet representing the double data.
     */
    @Override
    public double[] getDoubleData() {
        return this.doubleIndividual.getDoubleData();
    }

    /**
     * This method allows you to read the double data without
     * an update from the genotype
     *
     * @return double[] representing the double data.
     */
    @Override
    public double[] getDoubleDataWithoutUpdate() {
        return this.doubleIndividual.getDoubleDataWithoutUpdate();
    }

    /**
     * This method allows you to set the double data.
     *
     * @param doubleData The new double data.
     * @see InterfaceDataTypeDouble.SetDoubleData()
     */
    @Override
    public void setDoublePhenotype(double[] doubleData) {
        this.doubleIndividual.setDoublePhenotype(doubleData);
    }

    /**
     * This method allows you to set the double data, this can be used for
     * memetic algorithms.
     *
     * @param doubleData The new double data.
     * @see InterfaceDataTypeDouble.SetDoubleDataLamarckian()
     */
    @Override
    public void setDoubleGenotype(double[] doubleData) {
        this.doubleIndividual.setDoubleGenotype(doubleData);
    }

    /**
     * *******************************************************************************************************************
     * These are for   InterfaceDataTypeBinary
     * <p/>
     * /** This method allows you to request a certain amount of binary data
     *
     * @param length The lenght of the BitSet that is to be optimized
     */
    @Override
    public void setBinaryDataLength(int length) {
        this.doubleIndividual.setDoubleDataLength(length);
        this.binaryIndividual.setBinaryDataLength(length);
    }

    /**
     * This method returns the length of the binary data set
     *
     * @return The number of bits stored
     */
    public int GetBinaryDataLength() {
        return this.binaryIndividual.size();
    }

    /**
     * This method allows you to read the binary data
     *
     * @return BitSet representing the binary data.
     */
    @Override
    public BitSet getBinaryData() {
        return this.binaryIndividual.getBinaryData();
    }

    /**
     * This method allows you to read the binary data without
     * an update from the genotype
     *
     * @return BitSet representing the binary data.
     */
    @Override
    public BitSet getBinaryDataWithoutUpdate() {
        return this.binaryIndividual.getBinaryDataWithoutUpdate();
    }

    /**
     * This method allows you to set the binary data.
     *
     * @param binaryData The new binary data.
     * @see InterfaceDataTypeBinary.SetBinaryData()
     */
    @Override
    public void setBinaryPhenotype(BitSet binaryData) {
        this.binaryIndividual.setBinaryPhenotype(binaryData);
    }

    /**
     * This method allows you to set the binary data, this can be used for
     * memetic algorithms.
     *
     * @param binaryData The new binary data.
     * @see InterfaceBinaryData.setBinaryDataLamarckian()
     */
    @Override
    public void setBinaryGenotype(BitSet binaryData) {
        this.binaryIndividual.setBinaryGenotype(binaryData);
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
        return "GA/ES individual";
    }

    /**
     * This method will allow you to set the inner constants
     *
     * @param Numbers The new representation for the inner constants.
     */
    public void setNumbers(InterfaceDataTypeDouble Numbers) {
        this.doubleIndividual = Numbers;
    }

    public InterfaceDataTypeDouble getNumbers() {
        return this.doubleIndividual;
    }

    public String numbersTipText() {
        return "Choose the type of inner binary representation to use.";
    }

    /**
     * This method will allow you to set the inner constants
     *
     * @param BitSet The new representation for the inner constants.
     */
    public void setBitSet(InterfaceDataTypeBinary BitSet) {
        this.binaryIndividual = BitSet;
    }

    public InterfaceDataTypeBinary getBitSet() {
        return this.binaryIndividual;
    }

    public String bitSetTipText() {
        return "Choose the type of inner real-valued representation to use.";
    }
}