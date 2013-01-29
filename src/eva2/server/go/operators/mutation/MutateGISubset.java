package eva2.server.go.operators.mutation;

import eva2.gui.BeanInspector;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGIIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.math.RNG;
import java.io.Serializable;

/**
 * An integer mutation operator which switches elements within a given subset only.
 * 
 * @author mkron
 *
 */
public class MutateGISubset implements InterfaceMutation, Serializable {
	private int[] mutableSet = new int[]{0,1};
	private int minNumMutations = 1;
	private int maxNumMutations = 3;
	private boolean enforceMutablePositions = true;
	
	public MutateGISubset() {}
	
	public MutateGISubset(MutateGISubset o) {
		this.minNumMutations = o.minNumMutations;
		this.maxNumMutations = o.maxNumMutations;
		this.enforceMutablePositions = o.enforceMutablePositions;
		if (o.mutableSet!=null) {
			this.mutableSet=new int[o.mutableSet.length];
			System.arraycopy(o.mutableSet, 0, this.mutableSet, 0, o.mutableSet.length);
		}
	}
	
	public MutateGISubset(int[] muteSet, int minMutes, int maxMutes) {
		setMutableSet(muteSet);
		setMinNumMutations(minMutes);
		setMaxNumMutations(maxMutes);
	}
	
    @Override
	public Object clone() {
		return new MutateGISubset(this);
	}
	
    @Override
	public void crossoverOnStrategyParameters(AbstractEAIndividual indy1,
			Population partners) {
		// nothing to do
	}

    @Override
	public String getStringRepresentation() {
		return "GI subset mutation in " + BeanInspector.toString(mutableSet);
	}

    @Override
	public void init(AbstractEAIndividual individual,
			InterfaceOptimizationProblem opt) {
		// nothing to do
		
	}

    @Override
	public void mutate(AbstractEAIndividual individual) {
		if (individual instanceof InterfaceGIIndividual) {
			InterfaceGIIndividual giIndy = (InterfaceGIIndividual)individual;
			int[] genotype = giIndy.getIGenotype();
			int[][] range = giIndy.getIntRange();
			int numMutes=RNG.randomInt(minNumMutations, maxNumMutations);
			for (int i=0; i<numMutes; i++) {
				maybePerformSingleMutation(genotype, range, genotype.length/numMutes);
			}
		}
	}

	private void maybePerformSingleMutation(int[] genotype, int[][] range, int maxTries) {
		int index=RNG.randomInt(genotype.length);
		if (enforceMutablePositions && !(isMutable(genotype[index]))) {
			// if the mutation event should be forced, search a mutable element with a max. no. tries
			do {
				index=RNG.randomInt(genotype.length);
				maxTries--;
			} while (maxTries>0 && (!isMutable(genotype[index])));
		}
		if (isMutable(genotype[index])) { // if it is in the mutable subset
			genotype[index]=randomValidElement(range, index, genotype[index]);
		}
	}

	/**
	 * Return a randomly chosen element of the mutable subset lying in the range
	 * or the old value if the randomly chosen value 
	 * @param range
	 * @param index
	 * @param oldVal
	 * @return
	 */
	private int randomValidElement(int[][] range, int index, int oldVal) {
		int v = mutableSet[RNG.randomInt(mutableSet.length)];
		if (v>=range[index][0] && (v<=range[index][1])) {
                return v;
            }
		else {
			EVAERROR.errorMsgOnce("Warning, mutation subset violates range definition!");
			return oldVal;
		}
	}

	/**
	 * Return true if the given value is member of the mutable subset, otherwise false.
	 * @param v
	 * @return
	 */
	private boolean isMutable(int v) {
		for (int i=0; i<mutableSet.length; i++) {
			if (mutableSet[i]==v) {
                        return true;
                    }
		}
		return false;
	}

	public static String globalInfo() {
		return "A mutation operator which switches positions within a given subset only. A random " +
				"position is chosen but mutated only if its allele is contained" +
				" in the mutable set. The new allele is chosen from this set as well." +
				" In case the random positions do not contain a mutable allele, the switching is skipped. " +
				"This means that fewer switches may occur than expected from the minimal bound.";
	}
	
	public int[] getMutableSet() {
		return mutableSet;
	}
	public void setMutableSet(int[] mutationSubset) {
		this.mutableSet = mutationSubset;
	}
	public String mutableSetTipText() {
		return "A subset of integers among which mutations may occur.";
	}

	public int getMinNumMutations() {
		return minNumMutations;
	}
	public void setMinNumMutations(int minNumMutations) {
		this.minNumMutations = minNumMutations;
	}
	public String minNumMutationsTipText() {
		return "A lower bound for the number of positions changed per mutation event";
	}
	
	public int getMaxNumMutations() {
		return maxNumMutations;
	}
	public void setMaxNumMutations(int maxNumMutations) {
		this.maxNumMutations = maxNumMutations;
	}
	public String maxNumMutationsTipText() {
		return "An upper bound for the number of positions changed per mutation event";
	}

	public boolean isEnforceMutablePositions() {
		return enforceMutablePositions;
	}
	public void setEnforceMutablePositions(boolean enforceMutablePositions) {
		this.enforceMutablePositions = enforceMutablePositions;
	}
	public String enforceMutablePositionsTipText() {
		return "If true, the probability to hit a mutable position in a mutation event is increased by trying multiple times.";
	}
}
