package eva2.optimization.individuals;


/**
 * This interface gives access to a permutation phenotype and except
 * for problemspecific operators should only be used by the
 * optimization problem.
 */
public interface InterfaceDataTypePermutation {

    /**
     * setLength sets the length of the permutation.
     *
     * @param length int new length
     */
    public void setPermutationDataLength(int[] length);


    /**
     * size returns the size of the permutation.
     *
     * @return int
     */
    public int[] sizePermutation();

    /**
     * This method allows you to read the permutation data
     *
     * @return int[] represent the permutation.
     */
    int[][] getPermutationData();

    /**
     * This method allows you to read the permutation data without
     * an update from the genotype
     *
     * @return int[] representing the permutation.
     */
    public int[][] getPermutationDataWithoutUpdate();

    /**
     * This method allows you to set the permutation.
     *
     * @param perm The new permutation data.
     */
    void setPermutationPhenotype(int[][] perm);

    /**
     * This method allows you to set the permutation data, this can be used for
     * memetic algorithms.
     *
     * @param perm The new permutation data.
     */
    void setPermutationGenotype(int[][] perm);

    public void setFirstindex(int[] firstindex);
}
