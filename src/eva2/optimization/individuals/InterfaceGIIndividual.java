package eva2.optimization.individuals;


/**
 * This interface gives access to a integer genotype and should
 * only be used by mutation and crossover operators.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 18.05.2005
 * Time: 16:54:03
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceGIIndividual {

    /**
     * This method allows you to request a certain amount of int data
     *
     * @param length The lenght of the int[] that is to be optimized
     */
    public void setIntegerDataLength(int length);

    /**
     * This method will return the range for all int attributes.
     *
     * @return The range array.
     */
    public int[][] getIntRange();

    /**
     * This method will set the range of the int attributes.
     * Note: range[d][0] gives the lower bound and range[d] gives the upper bound
     * for dimension d where both are included.
     *
     * @param range The new range for the int data.
     */
    public void setIntRange(int[][] range);

    /**
     * This method will allow the user to read the GI genotype
     *
     * @return BitSet
     */
    public int[] getIGenotype();

    /**
     * This method will allow the user to set the current GI genotype.
     * Use this method with care, since the object is returned when using
     * getIGenotype() you can directly alter the genotype without using
     * this method.
     *
     * @param b The new genotype of the Individual
     */
    public void setIGenotype(int[] b);

    /**
     * This method allows the user to read the length of the genotype.
     * This may be necessary since BitSet.lenght only returns the index
     * of the last significat bit.
     *
     * @return The length of the genotype.
     */
    public int getGenotypeLength();
}
