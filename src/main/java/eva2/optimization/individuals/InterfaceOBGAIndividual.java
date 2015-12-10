package eva2.optimization.individuals;

/**
 * This interface gives access to a permutation genotype and should
 * only be used by mutation and crossover operators.
 */

public interface InterfaceOBGAIndividual {


    /**
     * getOBGenotype gets the genotype.
     *
     * @return int[] genotype
     */
    int[][] getOBGenotype();


    /**
     * setOBGenotype sets the genotype of the individual.
     *
     * @param b int[] new genotype
     */
    void setOBGenotype(int[][] b);
}
