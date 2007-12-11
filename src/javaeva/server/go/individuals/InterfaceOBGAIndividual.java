package javaeva.server.go.individuals;

/** This interface gives access to a permutation genotype and should
 * only be used by mutation and crossover operators.
 * <p>Title: The JavaEvA</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author planatsc
 * @version 1.0
 */

public interface InterfaceOBGAIndividual {


  /**
   * getOBGenotype gets the genotype.
   *
   * @return int[] genotype
   */
  public int[][] getOBGenotype();


  /**
   * SetOBGenotype sets the genotype of the individual.
   *
   * @param b int[] new genotype
   */
  public void SetOBGenotype(int[][] b);


  /**
   * defaultMutate perfoms a mutation by flipping two elements in the permutation
   */
  public void defaultMutate();

    /** This method initializes the program
     */
    public void defaultInit();
}
