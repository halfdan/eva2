package eva2.server.go.operators.mutation;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypePermutation;
import eva2.server.go.individuals.InterfaceOBGAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;


/**
 * <p>Title: EvA2</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2003</p>
 *
 * <p>Company: </p>
 * Mutates a permutation by flipping edges a given nubmer of times.
 *
 * @author planatsc
 * @version 1.0
 */
public class MutateOBGAFlip implements InterfaceMutation, java.io.Serializable {

  /**
   * times How many edges getting flipped by the mutation.
   */
  int times = 2;

  public MutateOBGAFlip() {
  }

  public Object clone() {
    return this;
  }

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateOBGAFlip) return true;
        else return false;
    }

    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        //nothing to init
    }

    public void mutate(AbstractEAIndividual individual) {

    	int[][] perm = ( (InterfaceOBGAIndividual) individual).
    	getOBGenotype();
    	for (int p = 0; p < perm.length; p++) {
    		for (int i = 0; i < times; i++) {
    			int p1 = RNG.randomInt(0, perm[p].length - 1);
    			int p2 = RNG.randomInt(0, perm[p].length - 1);
    			int temp = perm[p][p1];
    			perm[p][p1] = perm[p][p2];
    			perm[p][p2] = temp;
    		}
    	}
    	( (InterfaceOBGAIndividual) individual).SetOBGenotype(perm);
    }

    /** This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     * @param indy1     The original mother
     * @param partners  The original partners
     */
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    /** This method allows you to get a string representation of the mutation
     * operator
     * @return A descriptive string.
     */
    public String getStringRepresentation() {
        return "OBGA flip mutation";
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "OBGA flip mutation";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This mutation operators flips edges of the OBGA.";
    }
  public int getTimes() {
    return times;
  }
  public void setTimes(int times) {
    this.times = times;
  }

}
