package eva2.optimization.individuals;

import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * This individual combines a binary and a real-valued phenotype.
 */
@Description(value = "This is a mixed data type combining an integer vector with a permutation vector.")
public class GIOBGAIndividualIntegerPermutationData extends AbstractEAIndividual implements InterfaceDataTypeInteger, InterfaceDataTypePermutation, java.io.Serializable {

    private InterfaceDataTypeInteger integerData = new GIIndividualIntegerData();
    private InterfaceDataTypePermutation permutationData = new OBGAIndividualPermutationData();

    public GIOBGAIndividualIntegerPermutationData() {
        this.mutationProbability = 1.0;
        this.crossoverProbability = 1.0;
        this.integerData = new GIIndividualIntegerData();
        this.permutationData = new OBGAIndividualPermutationData();
    }

    public GIOBGAIndividualIntegerPermutationData(GIOBGAIndividualIntegerPermutationData individual) {
        this.integerData = (InterfaceDataTypeInteger) ((AbstractEAIndividual) individual.getIntegers()).clone();
        this.permutationData = (InterfaceDataTypePermutation) ((AbstractEAIndividual) individual.getPermutations()).clone();

        // cloning the members of AbstractEAIndividual
        this.age = individual.age;
        this.crossoverOperator = individual.crossoverOperator;
        this.crossoverProbability = individual.crossoverProbability;
        this.mutationOperator = (InterfaceMutation) individual.mutationOperator.clone();
        this.mutationProbability = individual.mutationProbability;
        this.selectionProbability = new double[individual.selectionProbability.length];
        System.arraycopy(individual.selectionProbability, 0, this.selectionProbability, 0, this.selectionProbability.length);
        this.fitness = new double[individual.fitness.length];
        System.arraycopy(individual.fitness, 0, this.fitness, 0, this.fitness.length);
        cloneAEAObjects(individual);
    }

    @Override
    public Object clone() {
        return new GIOBGAIndividualIntegerPermutationData(this);
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
            if (!((AbstractEAIndividual) this.integerData).equalGenotypes((AbstractEAIndividual) indy.integerData)) {
                return false;
            }
            if (!((AbstractEAIndividual) this.permutationData).equalGenotypes((AbstractEAIndividual) indy.permutationData)) {
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
    public void initialize(InterfaceOptimizationProblem opt) {
        ((AbstractEAIndividual) this.integerData).initialize(opt);
        ((AbstractEAIndividual) this.permutationData).initialize(opt);
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        ((AbstractEAIndividual) this.integerData).defaultInit(prob);
        ((AbstractEAIndividual) this.permutationData).defaultInit(prob);
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
                ((AbstractEAIndividual) this.integerData).initByValue(((Object[]) obj)[0], opt);
                ((AbstractEAIndividual) this.permutationData).initByValue(((Object[]) obj)[1], opt);
            } else {
                ((AbstractEAIndividual) this.integerData).initByValue(((Object[]) obj)[1], opt);
                ((AbstractEAIndividual) this.permutationData).initByValue(((Object[]) obj)[0], opt);
            }
        } else {
            ((AbstractEAIndividual) this.integerData).initialize(opt);
            ((AbstractEAIndividual) this.permutationData).initialize(opt);
            System.out.println("Initial value for GIOBGAIndividualIntegerPermutationData is not suitable!");
        }
    }

    /**
     * This method will mutate the individual randomly
     */
    @Override
    public void mutate() {
        if (RNG.flipCoin(this.mutationProbability)) {
            ((AbstractEAIndividual) this.integerData).mutate();
        }
        if (RNG.flipCoin(this.mutationProbability)) {
            ((AbstractEAIndividual) this.permutationData).mutate();
        }
    }

    @Override
    public void defaultMutate() {
        ((AbstractEAIndividual) this.integerData).defaultMutate();
        ((AbstractEAIndividual) this.permutationData).defaultMutate();
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
                numPop.add((AbstractEAIndividual) ((GIOBGAIndividualIntegerPermutationData) partners.get(i)).getIntegers());
            }
            resNum = numTmp.mateWith(numPop);

            binTmp = (AbstractEAIndividual) this.getPermutations();
            binPop = new Population();
            for (int i = 0; i < partners.size(); i++) {
                binPop.add((AbstractEAIndividual) ((GIOBGAIndividualIntegerPermutationData) partners.get(i)).getPermutations());
            }
            resBin = binTmp.mateWith(binPop);

            result = new GIOBGAIndividualIntegerPermutationData[resNum.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = new GIOBGAIndividualIntegerPermutationData(this);
                ((GIOBGAIndividualIntegerPermutationData) result[i]).setIntegers((InterfaceDataTypeInteger) resNum[i]);
                ((GIOBGAIndividualIntegerPermutationData) result[i]).setPermutations((InterfaceDataTypePermutation) resBin[i]);
            }
        } else {
            // simply return a number of perfect clones
            result = new AbstractEAIndividual[partners.size() + 1];
            result[0] = (AbstractEAIndividual) this.clone();
            for (int i = 0; i < partners.size(); i++) {
                result[i + 1] = (AbstractEAIndividual) (partners.get(i)).clone();
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
        result += "The Integer Part:\n" + ((AbstractEAIndividual) this.integerData).getStringRepresentation();
        result += "\nThe Permutation Part:\n" + ((AbstractEAIndividual) this.permutationData).getStringRepresentation();
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
        this.integerData.setIntegerDataLength(length);
    }

    /**
     * This method returns the length of the int data set
     *
     * @return The number of integers stored
     */
    @Override
    public int size() {
        return this.integerData.size();
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
        this.integerData.setIntRange(range);
    }

    /**
     * This method will return the range for all int attributes.
     *
     * @return The range array.
     */
    @Override
    public int[][] getIntRange() {
        return this.integerData.getIntRange();
    }

    /**
     * This method allows you to read the int data
     *
     * @return int[] representing the int data.
     */
    @Override
    public int[] getIntegerData() {
        return this.integerData.getIntegerData();
    }

    /**
     * This method allows you to read the int data without
     * an update from the genotype
     *
     * @return int[] representing the int data.
     */
    @Override
    public int[] getIntegerDataWithoutUpdate() {
        return this.integerData.getIntegerDataWithoutUpdate();
    }

    /**
     * This method allows you to set the int data.
     *
     * @param intData The new int data.
     */
    @Override
    public void setIntPhenotype(int[] intData) {
        this.integerData.setIntPhenotype(intData);
    }

    /**
     * This method allows you to set the int data, this can be used for
     * memetic algorithms.
     *
     * @param intData The new int data.
     */
    @Override
    public void setIntGenotype(int[] intData) {
        this.integerData.setIntGenotype(intData);
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
        this.permutationData.setPermutationDataLength(length);
        this.integerData.setIntegerDataLength(length.length);
    }

    /**
     * size returns the size of the permutation.
     *
     * @return int
     */
    @Override
    public int[] sizePermutation() {
        return this.permutationData.sizePermutation();
    }

    /**
     * This method allows you to read the permutation data
     *
     * @return int[] represent the permutation.
     */
    @Override
    public int[][] getPermutationData() {
        return this.permutationData.getPermutationData();
    }

    /**
     * This method allows you to read the permutation data without
     * an update from the genotype
     *
     * @return int[] representing the permutation.
     */
    @Override
    public int[][] getPermutationDataWithoutUpdate() {
        return this.permutationData.getPermutationDataWithoutUpdate();
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
        this.permutationData.setFirstindex(firstindex);
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
        this.integerData = Numbers;
    }

    public InterfaceDataTypeInteger getIntegers() {
        return this.integerData;
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
        this.permutationData = p;
    }

    public InterfaceDataTypePermutation getPermutations() {
        return this.permutationData;
    }

    public String permutationsTipText() {
        return "Choose the type of inner permutation to use.";
    }
}