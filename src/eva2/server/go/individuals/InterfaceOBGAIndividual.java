package eva2.server.go.individuals;

/** This interface gives access to a permutation genotype and should
 * only be used by mutation and crossover operators.
 * <p>Title: EvA2</p>
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
}
