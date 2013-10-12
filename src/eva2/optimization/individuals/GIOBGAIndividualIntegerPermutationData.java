package eva2.optimization.individuals;

import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * This individual combines a binary and a real-valued phenotype.
 */
@Description(text = "This is a mixed data type combining an integer vector with a permutation vector.")
public class GIOBGAIndividualIntegerPermutationData extends AbstractEAIndividual implements InterfaceDataTypeInteger, InterfaceDataTypePermutation, java.io.Serializable {

    private InterfaceDataTypeInteger m_Integer = new GIIndividualIntegerData();
    private InterfaceDataTypePermutation m_Permutation = new OBGAIndividualPermutationData();

    public GIOBGAIndividualIntegerPermutationData() {
        this.mutationProbability = 1.0;
        this.crossoverProbability = 1.0;
        this.m_Integer = new GIIndividualIntegerData();
        this.m_Permutation = new OBGAIndividualPermutationData();
    }

    public GIOBGAIndividualIntegerPermutationData(GIOBGAIndividualIntegerPermutationData individual) {
        this.m_Integer = (InterfaceDataTypeInteger) ((AbstractEAIndividual) individual.getIntegers()).clone();
        this.m_Permutation = (InterfaceDataTypePermutation) ((AbstractEAIndividual) individual.getPermutations()).clone();

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
        return (Object) new GIOBGAIndividualIntegerPermutationData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof GIOBGAIndividualIntegerPermutationData) {
            GIOBGAIndividualIntegerPermutationData indy = (GIOBGAIndividualIntegerPermutationData) individual;
            if (!((AbstractEAIndividual) this.m_Integer).equalGenotypes((AbstractEAIndividual) indy.m_Integer)) {
                return false;
            }
            if (!((AbstractEAIndividual) this.m_Permutation).equalGenotypes((AbstractEAIndividual) indy.m_Permutation)) {
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
        ((AbstractEAIndividual) this.m_Integer).init(opt);
        ((AbstractEAIndividual) this.m_Permutation).init(opt);
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        ((AbstractEAIndividual) this.m_Integer).defaultInit(prob);
        ((AbstractEAIndividual) this.m_Permutation).defaultInit(prob);
    }

    /**
     * This method will init the individual with a given value for the
     * phenotype.
     *
     * @param obj The initial value for the phenotype
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof Object[]) {
            if (((Object[]) obj)[0] instanceof double[]) {
                ((AbstractEAIndividual) this.m_Integer).initByValue(((Object[]) obj)[0], opt);
                ((AbstractEAIndividual) this.m_Permutation).initByValue(((Object[]) obj)[1], opt);
            } else {
                ((AbstractEAIndividual) this.m_Integer).initByValue(((Object[]) obj)[1], opt);
                ((AbstractEAIndividual) this.m_Permutation).initByValue(((Object[]) obj)[0], opt);
            }
        } else {
            ((AbstractEAIndividual) this.m_Integer).init(opt);
            ((AbstractEAIndividual) this.m_Permutation).init(opt);
            System.out.println("Initial value for GIOBGAIndividualIntegerPermutationData is not suitable!");
        }
    }

    /**
     * This method will mutate the individual randomly
     */
    @Override
    public void mutate() {
        if (RNG.flipCoin(this.mutationProbability)) {
            ((AbstractEAIndividual) this.m_Integer).mutate();
        }
        if (RNG.flipCoin(this.mutationProbability)) {
            ((AbstractEAIndividual) this.m_Permutation).mutate();
        }
    }

    @Override
    public void defaultMutate() {
        ((AbstractEAIndividual) this.m_Integer).defaultMutate();
        ((AbstractEAIndividual) this.m_Permutation).defaultMutate();
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

            numTmp = (AbstractEAIndividual) this.getIntegers();
            numPop = new Population();
            for (int i = 0; i < partners.size(); i++) {
                numPop.add(((GIOBGAIndividualIntegerPermutationData) partners.get(i)).getIntegers());
            }
            resNum = numTmp.mateWith(numPop);

            binTmp = (AbstractEAIndividual) this.getPermutations();
            binPop = new Population();
            for (int i = 0; i < partners.size(); i++) {
                binPop.add(((GIOBGAIndividualIntegerPermutationData) partners.get(i)).getPermutations());
            }
            resBin = binTmp.mateWith(binPop);

            result = new GIOBGAIndividualIntegerPermutationData[resNum.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = new GIOBGAIndividualIntegerPermutationData(this);
                ((GIOBGAIndividualIntegerPermutationData) result[i]).setIntegers((InterfaceDataTypeInteger) resNum[i]);
                ((GIOBGAIndividualIntegerPermutationData) result[i]).setPermutations((InterfaceDataTypePermutation) resBin[i]);
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
        result += "The Integer Part:\n" + ((AbstractEAIndividual) this.m_Integer).getStringRepresentation();
        result += "\nThe Permutation Part:\n" + ((AbstractEAIndividual) this.m_Permutation).getStringRepresentation();
        return result;
    }

    /**
     * *******************************************************************************************************************
     * These are for InterfaceDataTypeInteger
     * <p/>
     * /** This method allows you to request a certain amount of int data
     *
     * @param length The lenght of the int[] that is to be optimized
     */
    @Override
    public void setIntegerDataLength(int length) {
        this.m_Integer.setIntegerDataLength(length);
    }

    /**
     * This method returns the length of the int data set
     *
     * @return The number of integers stored
     */
    @Override
    public int size() {
        return this.m_Integer.size();
    }

    /**
     * This method will set the range of the int attributes.
     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
     * for dimension d.
     *
     * @param range The new range for the int data.
     */
    @Override
    public void setIntRange(int[][] range) {
        this.m_Integer.setIntRange(range);
    }

    /**
     * This method will return the range for all int attributes.
     *
     * @return The range array.
     */
    @Override
    public int[][] getIntRange() {
        return this.m_Integer.getIntRange();
    }

    /**
     * This method allows you to read the int data
     *
     * @return int[] representing the int data.
     */
    @Override
    public int[] getIntegerData() {
        return this.m_Integer.getIntegerData();
    }

    /**
     * This method allows you to read the int data without
     * an update from the genotype
     *
     * @return int[] representing the int data.
     */
    @Override
    public int[] getIntegerDataWithoutUpdate() {
        return this.m_Integer.getIntegerDataWithoutUpdate();
    }

    /**
     * This method allows you to set the int data.
     *
     * @param intData The new int data.
     */
    @Override
    public void setIntPhenotype(int[] intData) {
        this.m_Integer.setIntPhenotype(intData);
    }

    /**
     * This method allows you to set the int data, this can be used for
     * memetic algorithms.
     *
     * @param intData The new int data.
     */
    @Override
    public void setIntGenotype(int[] intData) {
        this.m_Integer.setIntGenotype(intData);
    }

/**********************************************************************************************************************
 * These are for   InterfaceDataTypePermutation
 */
    /**
     * setLength sets the length of the permutation.
     *
     * @param length int new length
     */
    @Override
    public void setPermutationDataLength(int[] length) {
        this.m_Permutation.setPermutationDataLength(length);
        this.m_Integer.setIntegerDataLength(length.length);
    }

    /**
     * size returns the size of the permutation.
     *
     * @return int
     */
    @Override
    public int[] sizePermutation() {
        return this.m_Permutation.sizePermutation();
    }

    /**
     * This method allows you to read the permutation data
     *
     * @return int[] represent the permutation.
     */
    @Override
    public int[][] getPermutationData() {
        return this.m_Permutation.getPermutationData();
    }

    /**
     * This method allows you to read the permutation data without
     * an update from the genotype
     *
     * @return int[] representing the permutation.
     */
    @Override
    public int[][] getPermutationDataWithoutUpdate() {
        return this.m_Permutation.getPermutationDataWithoutUpdate();
    }

    /**
     * This method allows you to set the permutation.
     *
     * @param perm The new permutation data.
     */
    @Override
    public void setPermutationPhenotype(int[][] perm) {
        this.setPermutationPhenotype(perm);
    }

    /**
     * This method allows you to set the permutation data, this can be used for
     * memetic algorithms.
     *
     * @param perm The new permutation data.
     */
    @Override
    public void setPermutationGenotype(int[][] perm) {
        this.setPermutationGenotype(perm);
    }

    @Override
    public void setFirstindex(int[] firstindex) {
        this.m_Permutation.setFirstindex(firstindex);
    }

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
    public void setIntegers(InterfaceDataTypeInteger Numbers) {
        this.m_Integer = Numbers;
    }

    public InterfaceDataTypeInteger getIntegers() {
        return this.m_Integer;
    }

    public String integersTipText() {
        return "Choose the type of inner integers to use.";
    }

    /**
     * This method will allow you to set the inner constants
     *
     * @param p The new representation for the inner constants.
     */
    public void setPermutations(InterfaceDataTypePermutation p) {
        this.m_Permutation = p;
    }

    public InterfaceDataTypePermutation getPermutations() {
        return this.m_Permutation;
    }

    public String permutationsTipText() {
        return "Choose the type of inner permutation to use.";
    }
}