package eva2.optimization.individuals;

import eva2.optimization.operator.crossover.CrossoverESDefault;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateESGlobal;
import eva2.optimization.problems.InterfaceHasInitRange;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * This individual uses a real-valued genotype to code for a permutations,
 * the sorting of the real-valued genotype gives the permutation.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 21.10.2004
 * Time: 17:02:53
 * To change this template use File | Settings | File Templates.
 */
public class ESIndividualPermutationData extends AbstractEAIndividual implements InterfaceESIndividual, InterfaceDataTypePermutation, java.io.Serializable {

    private double[][] m_Genotype;
    private int[][] m_Phenotype;
    private double[][][] m_Range;
    private int[] firstindex;


    public ESIndividualPermutationData() {
        this.mutationProbability = 1.0;
        this.mutationOperator = new MutateESGlobal();
        this.crossoverProbability = 0.5;
        this.crossoverOperator = new CrossoverESDefault();
        this.m_Genotype = new double[1][1];
        this.m_Range = new double[1][1][2];
        this.m_Range[0][0][0] = 0;
        this.m_Range[0][0][1] = 1;
        this.firstindex = new int[]{0};
    }

    public ESIndividualPermutationData(ESIndividualPermutationData individual) {
        if (individual.m_Phenotype != null) {
            this.m_Phenotype = new int[individual.m_Phenotype.length][];
            for (int i = 0; i < m_Phenotype.length; i++) {
                this.m_Phenotype[i] = new int[individual.m_Phenotype[i].length];
                System.arraycopy(individual.m_Phenotype[i], 0, this.m_Phenotype[i], 0, this.m_Phenotype[i].length);
            }
        }

        this.firstindex = individual.firstindex;
        this.m_Genotype = new double[individual.m_Genotype.length][];
        this.m_Range = new double[individual.m_Genotype.length][][];
        for (int i = 0; i < this.m_Genotype.length; i++) {
            //         if (individual.phenotype != null) {

            this.m_Genotype[i] = new double[individual.m_Genotype[i].length];
            this.m_Range[i] = new double[individual.m_Genotype[i].length][2];
            for (int j = 0; j < this.m_Genotype[i].length; j++) {
                this.m_Genotype[i][j] = individual.m_Genotype[i][j];
                this.m_Range[i][j][0] = individual.m_Range[i][j][0];
                this.m_Range[i][j][1] = individual.m_Range[i][j][1];
                //           }
            }
        }

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
        return (Object) new ESIndividualPermutationData(this);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof ESIndividualPermutationData) {
            ESIndividualPermutationData indy = (ESIndividualPermutationData) individual;
            if ((this.m_Genotype == null) || (indy.m_Genotype == null)) {
                return false;
            }
            if ((this.m_Range == null) || (indy.m_Range == null)) {
                return false;
            }
            if (this.m_Range.length != indy.m_Range.length) {
                return false;
            }
            for (int i = 0; i < this.m_Range.length; i++) {
                if (this.m_Genotype[i] != indy.m_Genotype[i]) {
                    return false;
                }
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

    /**
     * *********************************************************************************
     * InterfaceDataTypePermutation methods
     */

    @Override
    public void setPermutationDataLength(int[] length) {

        this.m_Genotype = new double[length.length][];
        this.m_Range = new double[length.length][][];
        for (int i = 0; i < this.m_Range.length; i++) {
            this.m_Genotype[i] = new double[length[i]];
        }

        for (int i = 0; i < this.m_Range.length; i++) {

            this.m_Range[i] = new double[length[i]][2];
            for (int j = 0; j < this.m_Range[i].length; j++) {
                this.m_Range[i][j][0] = 0;
                this.m_Range[i][j][1] = 1;
            }
        }
    }

    @Override
    public int[] sizePermutation() {
        int[] res = new int[m_Genotype.length];
        for (int i = 0; i < m_Genotype.length; i++) {
            res[i] = m_Genotype[i].length;
        }
        return res;
    }

    @Override
    public void setPermutationPhenotype(int[][] perm) {
        this.m_Phenotype = perm;
        this.m_Range = new double[perm.length][][];
        for (int i = 0; i < perm.length; i++) {
            this.m_Range[i] = new double[perm[i].length][2];
            for (int j = 0; j < this.m_Range[i].length; j++) {
                this.m_Range[i][j][0] = 0;
                this.m_Range[i][j][1] = 1;
            }
        }

    }

    @Override
    public void setPermutationGenotype(int[][] perm) {
        this.setPermutationPhenotype(perm);

        this.m_Genotype = new double[perm.length][];
        this.m_Range = new double[perm.length][][];
        for (int p = 0; p < perm.length; p++) {
            int biggest = Integer.MIN_VALUE;
            int smallest = Integer.MAX_VALUE;
            this.m_Range[p] = new double[perm[p].length][2];
            for (int i = 0; i < perm[p].length; i++) {
                if (perm[p][i] > biggest) {
                    biggest = perm[p][i];
                }
                if (perm[p][i] < smallest) {
                    smallest = perm[p][i];
                }
                this.m_Range[p][i][0] = 0;
                this.m_Range[p][i][1] = 1;
            }
            for (int i = 0; i < this.m_Genotype[p].length; i++) {
                this.m_Genotype[p][i] = (perm[p][i] - smallest) / (double) biggest;
            }
        }


    }

    @Override
    public int[][] getPermutationData() {
        this.m_Phenotype = new int[this.m_Genotype.length][];
        for (int p = 0; p < m_Genotype.length; p++) {
            this.m_Phenotype[p] = new int[m_Genotype[p].length];
            boolean notValid = true;
            while (notValid) {
                notValid = false;
                for (int i = 0; i < this.m_Genotype[p].length; i++) {
                    for (int j = 0; j < this.m_Genotype[p].length; j++) {
                        if ((i != j) && (this.m_Genotype[p][i] == this.m_Genotype[p][j])) {
                            notValid = true;
                            this.m_Genotype[p][j] = RNG.randomDouble(0, 1);
                        }
                    }
                }

            }
            for (int i = 0; i < this.m_Genotype[p].length; i++) {
                for (int j = 0; j < this.m_Genotype[p].length; j++) {
                    if (this.m_Genotype[p][i] > this.m_Genotype[p][j]) {
                        this.m_Phenotype[p][i]++;
                    }
                }
            }
        }
        return this.m_Phenotype;
    }

    /**
     * This method allows you to read the permutation data without
     * an update from the genotype
     *
     * @return int[] representing the permutation.
     */
    @Override
    public int[][] getPermutationDataWithoutUpdate() {
        return this.m_Phenotype;
    }

    public int[] getFirstindex() {
        return firstindex;
    }

    @Override
    public void setFirstindex(int[] firstindex) {
        this.firstindex = firstindex;
    }

    /************************************************************************************
     * AbstractEAIndividual methods
     */

    /**
     * This method will init the individual with a given value for the
     * phenotype.
     *
     * @param obj The initial value for the phenotype
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof int[][]) {
            int[][] bs = (int[][]) obj;
            if (bs.length != this.m_Genotype.length) {
                System.out.println("Init value and requested length doesn't match!");
            }
            this.setPermutationGenotype(bs);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for ESIndividualPermutationData is not int[]!");
        }
        this.mutationOperator.init(this, opt);
        this.crossoverOperator.init(this, opt);
    }

    /**
     * This method will return a string description of the GAIndividal
     * noteably the Genotype.
     *
     * @return A descriptive string
     */
    @Override
    public String getStringRepresentation() {
        String result = "";
        result += "ESIndividual coding permutation: (";
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
        for (int i = 0; i < this.m_Genotype.length; i++) {
            result += this.m_Genotype[i] + "; ";
        }
        result += "]";
        return result;
    }

    /************************************************************************************
     * InterfaceESIndividual methods
     */
    /**
     * This method will allow the user to read the ES 'genotype'
     *
     * @return BitSet
     */
    @Override
    public double[] getDGenotype() {
        return mapMatrixToVector(m_Genotype);
    }


    public double[] mapMatrixToVector(double[][] matrix) {
        int sumentries = 0;
        for (int i = 0; i < matrix.length; i++) {
            sumentries += matrix[i].length;
        }
        double[] res = new double[sumentries];
        int counter = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                res[counter] = matrix[i][j];
                counter++;
            }
        }
        return res;
    }

    public double[][] mapVectorToMatrix(double[] vector, int[] sizes) {
        double[][] matrix = new double[sizes.length][];
        int counter = 0;
        for (int i = 0; i < sizes.length; i++) {
            matrix[i] = new double[sizes[i]];
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = vector[counter];
                counter++;
            }
        }

        return matrix;
    }

    /**
     * This method will allow the user to set the current ES 'genotype'.
     *
     * @param b The new genotype of the Individual
     */
    @Override
    public void setDGenotype(double[] b) {
        this.m_Genotype = mapVectorToMatrix(b, this.sizePermutation());
        for (int i = 0; i < this.m_Genotype.length; i++) {
            for (int j = 0; j < this.m_Genotype[i].length; j++) {
                if (this.m_Genotype[i][j] < this.m_Range[i][j][0]) {
                    this.m_Genotype[i][j] = this.m_Range[i][j][0];
                }
                if (this.m_Genotype[i][j] > this.m_Range[i][j][1]) {
                    this.m_Genotype[i][j] = this.m_Range[i][j][1];
                }
            }
        }


    }

    /**
     * This method performs a one element mutation on every permutation coded by a double vector.
     */
    @Override
    public void defaultMutate() {
        for (int i = 0; i < m_Genotype.length; i++) {
            ESIndividualDoubleData.defaultMutate(m_Genotype[i], m_Range[i]);
        }
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        double[][][] range = m_Range;
        if ((prob != null) && (prob instanceof InterfaceHasInitRange) && (((InterfaceHasInitRange) prob).getInitRange() != null)) {
            range = (double[][][]) ((InterfaceHasInitRange) prob).getInitRange();
        }

        for (int i = 0; i < this.m_Genotype.length; i++) {
            ESIndividualDoubleData.defaultInit(m_Genotype[i], range[i]);
        }
    }

    /**
     * This method will return the range for all double attributes.
     *
     * @return The range array.
     */
    @Override
    public double[][] getDoubleRange() {
        int sumentries = 0;
        for (int i = 0; i < this.m_Range.length; i++) {
            sumentries += this.m_Range[i].length;
        }
        double[][] res = new double[sumentries][2];
        int counter = 0;
        for (int i = 0; i < this.m_Range.length; i++) {
            for (int j = 0; j < this.m_Range[i].length; j++) {
                res[counter][0] = this.m_Range[i][j][0];
                res[counter][1] = this.m_Range[i][j][1];
                counter++;
            }
        }
        return res;
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
        return "ES individual";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is an ES individual suited to optimize permutations.";
    }

}
