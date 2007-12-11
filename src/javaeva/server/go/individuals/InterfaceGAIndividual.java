package javaeva.server.go.individuals;

import java.util.BitSet;

/** This interface gives access to a binary genotype and should
 * only be used by mutation and crossover operators. Onyl exception are
 * data type specific optimization strategies like CHC or PBIL.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 14:25:24
 * To change this template use Options | File Templates.
 */
public interface InterfaceGAIndividual {

    /** This method will allow the user to read the GA genotype
     * @return BitSet
     */
    public BitSet getBGenotype();

    /** This method will allow the user to set the current GA genotype.
     * Use this method with care, since the object is returned when using
     * getBinaryData() you can directly alter the genotype without using
     * this method.
     * @param b    The new genotype of the Individual
     */
    public void SetBGenotype(BitSet b);

    /** This method allows the user to read the length of the genotype.
     * This may be necessary since BitSet.lenght only returns the index
     * of the last significat bit.
     * @return The length of the genotype.
     */
    public int getGenotypeLength();

    /** This method performs a simple one point mutation in the genotype
     */
    public void defaultMutate();

    /** This method initializes the GA genotype randomly
     */
    public void defaultInit();
}
