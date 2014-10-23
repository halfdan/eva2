package eva2.optimization.individuals;


import eva2.optimization.operator.crossover.CrossoverOBGAPMX;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateOBGAFlip;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.ArrayList;

/**
 * This individual uses a permutation based genotype to code for
 * permutations.
 */
@Description(value = "This is a GA individual coding permutations.")
public class OBGAIndividualPermutationData extends AbstractEAIndividual implements InterfaceDataTypePermutation, InterfaceOBGAIndividual, java.io.Serializable {

    int[][] phenotype;
    int[][] genotype;
    int[] firstindex;

    public OBGAIndividualPermutationData() {
        this.mutationProbability = 0.2;
        this.mutationOperator = new MutateOBGAFlip();
        this.crossoverProbability = 1.0;
        this.crossoverOperator = new CrossoverOBGAPMX();
        this.setPermutationDataLength(new int[]{20});
        firstindex = new int[]{0};
    }

    public OBGAIndividualPermutationData(OBGAIndividualPermutationData individual) {
        if (individual.phenotype != null) {
            this.phenotype = new int[individual.phenotype.length][];
            for (int i = 0; i < phenotype.length; i++) {
                this.phenotype[i] = new int[individual.phenotype[i].length];
                System.arraycopy(individual.phenotype[i], 0, this.phenotype[i], 0, this.phenotype[i].length);
            }
        }
        this.genotype = new int[individual.genotype.length][];
        for (int i = 0; i < genotype.length; i++) {
            this.genotype[i] = new int[individual.genotype[i].length];
            System.arraycopy(individual.genotype[i], 0, this.genotype[i], 0, this.genotype[i].length);
        }

        System.arraycopy(individual.genotype, 0, this.genotype, 0, this.genotype.length);
        this.firstindex = individual.firstindex;
        this.age = individual.age;
        this.crossoverOperator = individual.crossoverOperator;
        this.crossoverProbability = individual.crossoverProbability;
        this.mutationOperator = (InterfaceMutation) individual.mutationOperator.clone();
        this.mutationProbability = individual.mutationProbability;
        this.selectionProbability = new double[individual.selectionProbability.length];
        System.arraycopy(individual.selectionProbability, 0, this.selectionProbability, 0, this.selectionProbability.length);
        this.fitness = new double[individual.fitness.length];
        System.arraycopy(individual.fitness, 0, this.fitness, 0, this.fitness.length);
        this.cloneAEAObjects(individual);
    }

    /**
     * This method checks on equality regarding genotypic equality
     *
     * @param individual The individual to compare to.
     * @return boolean if equal true else false.
     */
    @Override
    public boolean equalGenotypes(AbstractEAIndividual individual) {
        if (individual instanceof OBGAIndividualPermutationData) {
            OBGAIndividualPermutationData indy = (OBGAIndividualPermutationData) individual;
            if ((this.genotype == null) || (indy.genotype == null)) {
                return false;
            }
            if (genotype.length != indy.genotype.length) {
                return false;
            }
            for (int i = 0; i < this.genotype.length; i++) {
                if (this.genotype[i].length != indy.genotype[i].length) {
                    for (int j = 0; j < this.genotype[i].length; j++) {
                        if (this.genotype[i][j] != indy.genotype[i][j]) {
                            return false;
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
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
        if (obj instanceof int[]) {
            this.setPermutationGenotype((int[][]) obj);
        } else {
            this.defaultInit(opt);
            System.out.println("Initial value for OBGAIndividualBinaryData is no Permutation!");
        }
        this.mutationOperator.init(this, opt);
        this.crossoverOperator.init(this, opt);
    }

    /**
     * This method can be used to read the current fitness of the individual.
     * Please note that the fitness can be based on multiple criteria therefore
     * double[] is used instead of a single double.
     *
     * @return The complete fitness array
     */
    @Override
    public double[] getFitness() {
        return this.fitness;
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
        result += "OBGAIndividual: (";
        result += "Fitness {";
        for (int i = 0; i < this.fitness.length; i++) {
            result += this.fitness[i] + ";";
        }
        result += "}/SelProb{";
        for (int i = 0; i < this.selectionProbability.length; i++) {
            result += this.selectionProbability[i] + ";";
        }
        result += "})\n Value: ";
        result += "{";
        int[] sizes = this.sizePermutation();

        for (int i = 0; i < sizes.length; i++) {
            result += "Permutation " + i + ":";
            for (int j = 0; j < sizes[i]; j++) {
                result += " " + this.getPermutationData()[i][j] + " ";
            }
            result += "\n";
        }
        result += "}";
        result += "\n Mutation (" + this.mutationProbability + "):" + this.mutationOperator.getStringRepresentation();
        return result;

    }

    @Override
    public Object clone() {
        return new OBGAIndividualPermutationData(this);
    }

    /**
     * *********************************************************************************
     * InterfaceOBGAIndividual methods
     */

    @Override
    public int[][] getOBGenotype() {
        return this.genotype;
    }

    @Override
    public void setOBGenotype(int[][] g) {
        this.genotype = g;
    }

    @Override
    public void defaultMutate() {
        int[][] permmatrix = this.getPermutationData();
        for (int i = 0; i < permmatrix.length; i++) {
            int[] perm = permmatrix[i];
            int p1 = RNG.randomInt(0, perm.length - 1);
            int p2 = RNG.randomInt(0, perm.length - 1);
            int temp = perm[p1];
            perm[p1] = perm[p2];
            perm[p2] = temp;
        }

        this.setPermutationGenotype(permmatrix);
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        //System.out.println("Default Init!");
        int[][] perm = new int[this.genotype.length][];
        for (int p = 0; p < perm.length; p++) {
            perm[p] = new int[this.genotype[p].length];
            ArrayList pot = new ArrayList();
            for (int i = 0; i < this.sizePermutation()[p]; i++) {
                pot.add(firstindex[p] + i);
            }
            int i = 0;
            while (!pot.isEmpty()) {
                perm[p][i] = (Integer) (pot.remove(RNG.randomInt(0, pot.size() - 1)));
                i++;
            }
        }
        this.setPermutationGenotype(perm);
        // System.out.println(getStringRepresentation());
    }


    /**
     * *********************************************************************************
     * InterfaceDataTypePermutation methods
     */


    @Override
    public void setPermutationDataLength(int[] length) {
        this.genotype = new int[length.length][];
        for (int i = 0; i < length.length; i++) {
            this.genotype[i] = new int[length[i]];
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
    }

    @Override
    public void setPermutationGenotype(int[][] perm) {
        this.setPermutationPhenotype(perm);
        this.genotype = new int[perm.length][];
        for (int i = 0; i < perm.length; i++) {
            this.genotype[i] = new int[perm[i].length];
            System.arraycopy(perm[i], 0, this.genotype[i], 0, perm[i].length);
        }

    }

    @Override
    public int[][] getPermutationData() {
        this.phenotype = new int[this.genotype.length][];
        for (int i = 0; i < this.genotype.length; i++) {
            this.phenotype[i] = new int[this.genotype[i].length];
            System.arraycopy(this.genotype[i], 0, this.phenotype[i], 0, this.genotype[i].length);
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

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "OBGA individual";
    }
}
