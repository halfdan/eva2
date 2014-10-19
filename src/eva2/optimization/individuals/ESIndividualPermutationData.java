package eva2.optimization.individuals;

import eva2.optimization.operator.crossover.CrossoverESDefault;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateESGlobal;
import eva2.problems.InterfaceHasInitRange;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

/**
 * This individual uses a real-valued genotype to code for a permutations,
 * the sorting of the real-valued genotype gives the permutation.
 */
@Description(value = "This is an ES individual suited to optimize permutations.")
public class ESIndividualPermutationData extends AbstractEAIndividual implements InterfaceESIndividual, InterfaceDataTypePermutation, java.io.Serializable {

    private double[][] genotype;
    private int[][] phenotype;
    private double[][][] initializationRange;
    private int[] firstindex;


    public ESIndividualPermutationData() {
        this.mutationProbability = 1.0;
        this.mutationOperator = new MutateESGlobal();
        this.crossoverProbability = 0.5;
        this.crossoverOperator = new CrossoverESDefault();
        this.genotype = new double[1][1];
        this.initializationRange = new double[1][1][2];
        this.initializationRange[0][0][0] = 0;
        this.initializationRange[0][0][1] = 1;
        this.firstindex = new int[]{0};
    }

    public ESIndividualPermutationData(ESIndividualPermutationData individual) {
        if (individual.phenotype != null) {
            this.phenotype = new int[individual.phenotype.length][];
            for (int i = 0; i < phenotype.length; i++) {
                this.phenotype[i] = new int[individual.phenotype[i].length];
                System.arraycopy(individual.phenotype[i], 0, this.phenotype[i], 0, this.phenotype[i].length);
            }
        }

        this.firstindex = individual.firstindex;
        this.genotype = new double[individual.genotype.length][];
        this.initializationRange = new double[individual.genotype.length][][];
        for (int i = 0; i < this.genotype.length; i++) {
            //         if (individual.phenotype != null) {

            this.genotype[i] = new double[individual.genotype[i].length];
            this.initializationRange[i] = new double[individual.genotype[i].length][2];
            for (int j = 0; j < this.genotype[i].length; j++) {
                this.genotype[i][j] = individual.genotype[i][j];
                this.initializationRange[i][j][0] = individual.initializationRange[i][j][0];
                this.initializationRange[i][j][1] = individual.initializationRange[i][j][1];
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
        System.arraycopy(individual.selectionProbability, 0, this.selectionProbability, 0, this.selectionProbability.length);
        this.fitness = new double[individual.fitness.length];
        System.arraycopy(individual.fitness, 0, this.fitness, 0, this.fitness.length);
        cloneAEAObjects(individual);

    }

    @Override
    public Object clone() {
        return new ESIndividualPermutationData(this);
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
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            }
            if ((this.initializationRange == null) || (indy.initializationRange == null)) {
                return false;
            }
            if (this.initializationRange.length != indy.initializationRange.length) {
                return false;
            }
            for (int i = 0; i < this.initializationRange.length; i++) {
                if (this.genotype[i] != indy.genotype[i]) {
                    return false;
                }
                if (this.initializationRange[i][0] != indy.initializationRange[i][0]) {
                    return false;
                }
                if (this.initializationRange[i][1] != indy.initializationRange[i][1]) {
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

        this.genotype = new double[length.length][];
        this.initializationRange = new double[length.length][][];
        for (int i = 0; i < this.initializationRange.length; i++) {
            this.genotype[i] = new double[length[i]];
        }

        for (int i = 0; i < this.initializationRange.length; i++) {

            this.initializationRange[i] = new double[length[i]][2];
            for (int j = 0; j < this.initializationRange[i].length; j++) {
                this.initializationRange[i][j][0] = 0;
                this.initializationRange[i][j][1] = 1;
            }
        }
    }

    @Override
    public int[] sizePermutation() {
        int[] res = new int[genotype.length];
        for (int i = 0; i < genotype.length; i++) {
            res[i] = genotype[i].length;
        }
        return res;
    }

    @Override
    public void setPermutationPhenotype(int[][] perm) {
        this.phenotype = perm;
        this.initializationRange = new double[perm.length][][];
        for (int i = 0; i < perm.length; i++) {
            this.initializationRange[i] = new double[perm[i].length][2];
            for (int j = 0; j < this.initializationRange[i].length; j++) {
                this.initializationRange[i][j][0] = 0;
                this.initializationRange[i][j][1] = 1;
            }
        }

    }

    @Override
    public void setPermutationGenotype(int[][] perm) {
        this.setPermutationPhenotype(perm);

        this.genotype = new double[perm.length][];
        this.initializationRange = new double[perm.length][][];
        for (int p = 0; p < perm.length; p++) {
            int biggest = Integer.MIN_VALUE;
            int smallest = Integer.MAX_VALUE;
            this.initializationRange[p] = new double[perm[p].length][2];
            for (int i = 0; i < perm[p].length; i++) {
                if (perm[p][i] > biggest) {
                    biggest = perm[p][i];
                }
                if (perm[p][i] < smallest) {
                    smallest = perm[p][i];
                }
                this.initializationRange[p][i][0] = 0;
                this.initializationRange[p][i][1] = 1;
            }
            for (int i = 0; i < this.genotype[p].length; i++) {
                this.genotype[p][i] = (perm[p][i] - smallest) / (double) biggest;
            }
        }


    }

    @Override
    public int[][] getPermutationData() {
        this.phenotype = new int[this.genotype.length][];
        for (int p = 0; p < genotype.length; p++) {
            this.phenotype[p] = new int[genotype[p].length];
            boolean notValid = true;
            while (notValid) {
                notValid = false;
                for (int i = 0; i < this.genotype[p].length; i++) {
                    for (int j = 0; j < this.genotype[p].length; j++) {
                        if ((i != j) && (this.genotype[p][i] == this.genotype[p][j])) {
                            notValid = true;
                            this.genotype[p][j] = RNG.randomDouble(0, 1);
                        }
                    }
                }

            }
            for (int i = 0; i < this.genotype[p].length; i++) {
                for (int j = 0; j < this.genotype[p].length; j++) {
                    if (this.genotype[p][i] > this.genotype[p][j]) {
                        this.phenotype[p][i]++;
                    }
                }
            }
        }
        return this.phenotype;
    }

    /**
     * This method allows you to read the permutation data without
     * an update from the genotype
     *
     * @return int[] representing the permutation.
     */
    @Override
    public int[][] getPermutationDataWithoutUpdate() {
        return this.phenotype;
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
     * This method will initialize the individual with a given value for the
     * phenotype.
     *
     * @param obj The initial value for the phenotype
     * @param opt The optimization problem that is to be solved.
     */
    @Override
    public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof int[][]) {
            int[][] bs = (int[][]) obj;
            if (bs.length != this.genotype.length) {
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
        for (int i = 0; i < this.genotype.length; i++) {
            result += this.genotype[i] + "; ";
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
        return mapMatrixToVector(genotype);
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
        this.genotype = mapVectorToMatrix(b, this.sizePermutation());
        for (int i = 0; i < this.genotype.length; i++) {
            for (int j = 0; j < this.genotype[i].length; j++) {
                if (this.genotype[i][j] < this.initializationRange[i][j][0]) {
                    this.genotype[i][j] = this.initializationRange[i][j][0];
                }
                if (this.genotype[i][j] > this.initializationRange[i][j][1]) {
                    this.genotype[i][j] = this.initializationRange[i][j][1];
                }
            }
        }


    }

    /**
     * This method performs a one element mutation on every permutation coded by a double vector.
     */
    @Override
    public void defaultMutate() {
        for (int i = 0; i < genotype.length; i++) {
            ESIndividualDoubleData.defaultMutate(genotype[i], initializationRange[i]);
        }
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        double[][][] range = initializationRange;
        if ((prob != null) && (prob instanceof InterfaceHasInitRange) && (((InterfaceHasInitRange) prob).getInitRange() != null)) {
            range = (double[][][]) ((InterfaceHasInitRange) prob).getInitRange();
        }

        for (int i = 0; i < this.genotype.length; i++) {
            ESIndividualDoubleData.defaultInit(genotype[i], range[i]);
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
        for (int i = 0; i < this.initializationRange.length; i++) {
            sumentries += this.initializationRange[i].length;
        }
        double[][] res = new double[sumentries][2];
        int counter = 0;
        for (int i = 0; i < this.initializationRange.length; i++) {
            for (int j = 0; j < this.initializationRange[i].length; j++) {
                res[counter][0] = this.initializationRange[i][j][0];
                res[counter][1] = this.initializationRange[i][j][1];
                counter++;
            }
        }
        return res;
    }

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
}
