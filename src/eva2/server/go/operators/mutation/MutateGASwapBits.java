package eva2.server.go.operators.mutation;


import java.util.BitSet;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

/**
 * Swap two random bits of a GA individual. If preferPairs is true, unequal pairs
 * are picked with some preference (by trying for l/2 times, where l is the binary
 * individual length). 
 * 
 * User: streiche, mkron
 * Date: 05.08.2004
 * Time: 17:45:36
 * To change this template use File | Settings | File Templates.
 */
public class MutateGASwapBits implements InterfaceMutation, java.io.Serializable {
	private int         m_NumberOfMutations = 1;
	private boolean 	preferPairs = true; // if true, pairs of (1,0) are swapped with higher probability

	public MutateGASwapBits() {

	}
	public MutateGASwapBits(MutateGASwapBits mutator) {
		this.m_NumberOfMutations     = mutator.m_NumberOfMutations;
		this.setPreferTrueChange(mutator.isPreferTrueChange());
	}

	/** This method will enable you to clone a given mutation operator
	 * @return The clone
	 */
	public Object clone() {
		return new MutateGASwapBits(this);
	}

    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGASwapBits) {
            MutateGASwapBits mut = (MutateGASwapBits)mutator;
            if (this.m_NumberOfMutations != mut.m_NumberOfMutations) return false;
            return true;
        } else return false;
    }

	/** This method allows you to init the mutation operator
	 * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

	}

    /** This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     * @param indy1     The original mother
     * @param partners  The original partners
     */
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

	/** This method will mutate a given AbstractEAIndividual. If the individual
	 * doesn't implement InterfaceGAIndividual nothing happens.
	 * @param individual    The individual that is to be mutated
	 */
	public void mutate(AbstractEAIndividual individual) {
		//System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
		if (individual instanceof InterfaceGAIndividual) {
			BitSet  tmpBitSet   = ((InterfaceGAIndividual)individual).getBGenotype();
			int[][]   mutationIndices = new int[this.m_NumberOfMutations][2];
            boolean tmpBit;
			for (int i = 0; i < mutationIndices.length; i++) {
                mutationIndices[i][0] = getRandomIndex(individual, true); // may prefer true bits
                mutationIndices[i][1] = getRandomIndex(individual, false); // may prefer false bits
            }
			// double instances of mutationIndices could be checked here... *sigh*
			for (int i = 0; i < mutationIndices.length; i++) {
                tmpBit = tmpBitSet.get(mutationIndices[i][1]);
                tmpBitSet.set(mutationIndices[i][1], tmpBitSet.get(mutationIndices[i][0]));
                tmpBitSet.set(mutationIndices[i][0], tmpBit);
			}
			((InterfaceGAIndividual)individual).SetBGenotype(tmpBitSet);
		}
		//System.out.println("After Mutate:  " +((GAIndividual)individual).getSolutionRepresentationFor());
	}

	private int getRandomIndex(AbstractEAIndividual individual, boolean maybePrefered) {
		int genoLen = ((InterfaceGAIndividual)individual).getGenotypeLength();
		int k = RNG.randomInt(0, genoLen); 
		if (isPreferTrueChange()) {
			int maxTries=genoLen/2;
			while (!(maybePrefered==((InterfaceGAIndividual)individual).getBGenotype().get(k)) && (maxTries>=0)) {
				k=(k+RNG.randomInt(1,genoLen))%genoLen; // try next random position
				maxTries--;
			}
		}
		return k;
	}
	
//	private int getRandomSecondIndex(int firstIndex, AbstractEAIndividual individual) {
//		int genoLen = ((InterfaceGAIndividual)individual).getGenotypeLength();
//		return RNG.randomInt(0, genoLen);
//	}
	
	/** This method allows you to get a string representation of the mutation
	 * operator
	 * @return A descriptive string.
	 */
	public String getStringRepresentation() {
		return "GA swap bits mutation";
	}

/**********************************************************************************************************************
 * These are for GUI
 */
	/** This method allows the CommonJavaObjectEditorPanel to read the
	 * name to the current object.
	 * @return The name.
	 */
	public String getName() {
		return "GA swap bits mutation";
	}
	/** This method returns a global info string
	 * @return description
	 */
	public static String globalInfo() {
		return "This mutation operator swaps n random bits.";
	}

	/** This method allows you to set the number of mutations that occur in the
	 * genotype.
	 * @param mutations   The number of mutations.
	 */
	public void setNumberOfMutations(int mutations) {
		if (mutations < 0) mutations = 0;
		this.m_NumberOfMutations = mutations;
	}
	public int getNumberOfMutations() {
		return this.m_NumberOfMutations;
	}
	public String numberOfMutationsTipText() {
		return "The number of bits to be swapped.";
	}
	public void setPreferTrueChange(boolean preferPairs) {
		this.preferPairs = preferPairs;
	}
	public boolean isPreferTrueChange() {
		return preferPairs;
	}
	public String preferTrueChangeTipText() {
		return "If set to true, mutation events will prefer swapping 1 and 0"; 
	}
}