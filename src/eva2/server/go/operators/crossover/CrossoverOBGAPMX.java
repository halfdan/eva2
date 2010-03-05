package eva2.server.go.operators.crossover;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypePermutation;
import eva2.server.go.individuals.InterfaceOBGAIndividual;
import eva2.server.go.operators.crossover.InterfaceCrossover;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * <p>Title: EvA2</p>
 * <p>Description: PMX-Crossover as defined in http://www.cs.rit.edu/usr/local/pub/pga/Genetic/Slides_etc/ga_5_og.pdf</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author planatsc
 * @version 1.0
 */

public class CrossoverOBGAPMX implements InterfaceCrossover, java.io.Serializable {

    public CrossoverOBGAPMX() {

    }
    public CrossoverOBGAPMX(CrossoverOBGAPMX c) {
    }
    /** This method will enable you to clone a given mutation operator
     * @return The clone
     */
    public Object clone() {
        return new CrossoverOBGAPMX(this);
    }

  public AbstractEAIndividual[] mate(AbstractEAIndividual indy1, Population partners) {
    AbstractEAIndividual[] result = null;
    result = new AbstractEAIndividual[partners.size()+1];
    result[0] = (AbstractEAIndividual) (indy1).clone();
    for (int i = 0; i < partners.size(); i++) result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();

    if ((indy1 instanceof InterfaceOBGAIndividual) && (partners.get(0) instanceof InterfaceOBGAIndividual)) {
      int[][] pperm1;
      int[][] pperm2;
      pperm1 = new int[((InterfaceOBGAIndividual) result[0]).getOBGenotype().length][];
      pperm2 = new int[((InterfaceOBGAIndividual) result[1]).getOBGenotype().length][];

      for (int i = 0; i < ((InterfaceOBGAIndividual) result[0]).getOBGenotype().length; i++) {

        int[] perm1 = (int[])((InterfaceOBGAIndividual) result[0]).getOBGenotype()[i].clone();
        int[] perm2 = (int[])((InterfaceOBGAIndividual) result[1]).getOBGenotype()[i].clone();
        int begin = RNG.randomInt(0,perm1.length-2);
        int end = RNG.randomInt(begin,perm1.length-1);
        for (int pos = begin; pos <= end; pos++) {
          int crosspoint = pos;
          int p1inp2 = 0;
          int p2inp1 = 0;
          while (perm1[p2inp1] != perm2[crosspoint]) p2inp1++;
          while (perm2[p1inp2] != perm1[crosspoint]) p1inp2++;
          perm1[crosspoint] = perm2[crosspoint];
          perm2[crosspoint] = perm2[p1inp2];
          perm1[p2inp1] = perm2[crosspoint];
          perm2[p1inp2] = perm1[crosspoint];
        }
        pperm1[i] = perm1;
        pperm2[i] = perm2;
      }

      ((InterfaceOBGAIndividual) result[0]).SetOBGenotype(pperm1);
      ((InterfaceOBGAIndividual) result[1]).SetOBGenotype(pperm2);
      //((InterfaceDataTypePermutation) result[0]).SetPermutationDataLamarckian(pperm1);
      //((InterfaceDataTypePermutation) result[1]).SetPermutationDataLamarckian(pperm2);
    }
    //in case the crossover was successfull lets give the mutation operators a chance to mate the strategy parameters
      for (int i = 0; i < result.length; i++) result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);


    return result;

  }

    /** This method allows you to evaluate wether two crossover operators
     * are actually the same.
     * @param crossover   The other crossover operator
     */
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverOBGAPMX) return true;
        else return false;
    }

  public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
    // nothing to init!
  }

    public String getStringRepresentation() {
        return this.getName();
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "OBGA PMX crossover";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "The infamous PMX crossover for Permutations.";
    }

}
