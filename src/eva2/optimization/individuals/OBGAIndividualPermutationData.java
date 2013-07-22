package eva2.optimization.individuals;


import eva2.optimization.operator.crossover.CrossoverOBGAPMX;
import eva2.optimization.operator.mutation.InterfaceMutation;
import eva2.optimization.operator.mutation.MutateOBGAFlip;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

import java.util.ArrayList;

/**
 * This individual uses a permutation based genotype to code for
 * permutations.
 * <p>Title: EvA2</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author planatsc
 * @version 1.0
 */

public class OBGAIndividualPermutationData extends AbstractEAIndividual implements InterfaceDataTypePermutation, InterfaceOBGAIndividual, java.io.Serializable {

    int[][] m_Phenotype;
    int[][] m_Genotype;
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
        if (individual.m_Phenotype != null) {
            this.m_Phenotype = new int[individual.m_Phenotype.length][];
            for (int i = 0; i < m_Phenotype.length; i++) {
                this.m_Phenotype[i] = new int[individual.m_Phenotype[i].length];
                System.arraycopy(individual.m_Phenotype[i], 0, this.m_Phenotype[i], 0, this.m_Phenotype[i].length);
            }
        }
        this.m_Genotype = new int[individual.m_Genotype.length][];
        for (int i = 0; i < m_Genotype.length; i++) {
            this.m_Genotype[i] = new int[individual.m_Genotype[i].length];
            System.arraycopy(individual.m_Genotype[i], 0, this.m_Genotype[i], 0, this.m_Genotype[i].length);
        }

        System.arraycopy(individual.m_Genotype, 0, this.m_Genotype, 0, this.m_Genotype.length);
        this.firstindex = individual.firstindex;
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
            if ((this.m_Genotype == null) || (indy.m_Genotype == null)) {
                return false;
            }
            if (m_Genotype.length != indy.m_Genotype.length) {
                return false;
            }
            for (int i = 0; i < this.m_Genotype.length; i++) {
                if (this.m_Genotype[i].length != indy.m_Genotype[i].length) {
                    for (int j = 0; j < this.m_Genotype[i].length; j++) {
                        if (this.m_Genotype[i][j] != indy.m_Genotype[i][j]) {
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
        if (obj instanceof int[]) {
            this.SetPermutationGenotype((int[][]) obj);
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
        return this.m_Genotype;
    }

    @Override
    public void SetOBGenotype(int[][] g) {
        this.m_Genotype = g;
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

        this.SetPermutationGenotype(permmatrix);
    }

    @Override
    public void defaultInit(InterfaceOptimizationProblem prob) {
        //System.out.println("Default Init!");
        int[][] perm = new int[this.m_Genotype.length][];
        for (int p = 0; p < perm.length; p++) {
            perm[p] = new int[this.m_Genotype[p].length];
            ArrayList pot = new ArrayList();
            for (int i = 0; i < this.sizePermutation()[p]; i++) {
                pot.add(new Integer(firstindex[p] + i));
            }
            int i = 0;
            while (!pot.isEmpty()) {
                perm[p][i] = ((Integer) (pot.remove(RNG.randomInt(0, pot.size() - 1)))).intValue();
                i++;
            }
        }
        this.SetPermutationGenotype(perm);
        // System.out.println(getStringRepresentation());
    }


    /**
     * *********************************************************************************
     * InterfaceDataTypePermutation methods
     */


    @Override
    public void setPermutationDataLength(int[] length) {
        this.m_Genotype = new int[length.length][];
        for (int i = 0; i < length.length; i++) {
            this.m_Genotype[i] = new int[length[i]];
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
    public void SetPermutationPhenotype(int[][] perm) {
        this.m_Phenotype = perm;
    }

    @Override
    public void SetPermutationGenotype(int[][] perm) {
        this.SetPermutationPhenotype(perm);
        this.m_Genotype = new int[perm.length][];
        for (int i = 0; i < perm.length; i++) {
            this.m_Genotype[i] = new int[perm[i].length];
            System.arraycopy(perm[i], 0, this.m_Genotype[i], 0, perm[i].length);
        }

    }

    @Override
    public int[][] getPermutationData() {
        this.m_Phenotype = new int[this.m_Genotype.length][];
        for (int i = 0; i < this.m_Genotype.length; i++) {
            this.m_Phenotype[i] = new int[this.m_Genotype[i].length];
            System.arraycopy(this.m_Genotype[i], 0, this.m_Phenotype[i], 0, this.m_Genotype[i].length);
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

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This is a GA individual coding permutations.";
    }
}
