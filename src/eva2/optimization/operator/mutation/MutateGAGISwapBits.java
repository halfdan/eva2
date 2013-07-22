package eva2.optimization.operator.mutation;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.individuals.InterfaceGIIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

import java.util.BitSet;

/**
 * Swap random pairs of values of a GA/GI individual. If preferPairs is true, unequal pairs
 * are picked with some preference (by trying for l/2 times, where l is the binary
 * individual length).
 * A range of number of par mutations can be given from which the actual number of pairs
 * is drawn in a uniform way.
 * <p/>
 * User: streiche, mkron
 * Date: 05.08.2004
 * Time: 17:45:36
 * To change this template use File | Settings | File Templates.
 */
public class MutateGAGISwapBits implements InterfaceMutation, java.io.Serializable {
    private int minNumMutations = 1;
    private int maxNumMutations = 3;
    private boolean preferTrueChange = true; // if true, pairs of (1,0) are swapped with higher probability

    public MutateGAGISwapBits() {
    }

    public MutateGAGISwapBits(MutateGAGISwapBits mutator) {
        minNumMutations = mutator.minNumMutations;
        maxNumMutations = mutator.maxNumMutations;
        this.setPreferTrueChange(mutator.isPreferTrueChange());
    }

    public MutateGAGISwapBits(int minMutations, int maxMutations, boolean preferTrueChange) {
        minNumMutations = minMutations;
        maxNumMutations = maxMutations;
        this.preferTrueChange = preferTrueChange;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGAGISwapBits(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGAGISwapBits) {
            MutateGAGISwapBits mut = (MutateGAGISwapBits) mutator;
            if (this.minNumMutations != mut.minNumMutations) {
                return false;
            }
            if (this.maxNumMutations != mut.maxNumMutations) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method allows you to init the mutation operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

    }

    /**
     * This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     *
     * @param indy1    The original mother
     * @param partners The original partners
     */
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

    /**
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual or InterfaceGIIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
//		System.err.println("Before Mutate: " +(individual.getStringRepresentation()));
        if (individual instanceof InterfaceGAIndividual || (individual instanceof InterfaceGIIndividual)) {
            Object genotype = null;
            int genoLen = -1;
            if (individual instanceof InterfaceGAIndividual) {
                genotype = ((InterfaceGAIndividual) individual).getBGenotype();
                genoLen = ((InterfaceGAIndividual) individual).getGenotypeLength();
            } else {
                genotype = ((InterfaceGIIndividual) individual).getIGenotype();
                genoLen = ((InterfaceGIIndividual) individual).getGenotypeLength();
            }
            int[][] mutationIndices = selectMutationIndices(genoLen, genotype);
//			System.err.println("Indices are " + BeanInspector.toString(mutationIndices));
            // double instances of mutationIndices could be checked here... *sigh*
            for (int i = 0; i < mutationIndices.length; i++) {
                Object tmp = valueAt(genotype, mutationIndices[i][1]);
                setValueAt(genotype, mutationIndices[i][1], valueAt(genotype, mutationIndices[i][0]));
                setValueAt(genotype, mutationIndices[i][0], tmp);
//				tmpBit = tmpBitSet.get(mutationIndices[i][1]);
//				tmpBitSet.set(mutationIndices[i][1], tmpBitSet.get(mutationIndices[i][0]));
//				tmpBitSet.set(mutationIndices[i][0], tmpBit);
            }
            if (genotype instanceof BitSet) {
                ((InterfaceGAIndividual) individual).SetBGenotype((BitSet) genotype);
            } else {
                ((InterfaceGIIndividual) individual).SetIGenotype((int[]) genotype);
            }
        }
//		System.err.println("After Mutate:  " +(individual.getStringRepresentation()));
    }

    /**
     * Select the mutation indices for the current mutation operation.
     *
     * @param genoLen
     * @param genotype
     * @return
     */
    protected int[][] selectMutationIndices(int genoLen, Object genotype) {
        int numMutes = RNG.randomInt(minNumMutations, maxNumMutations);
        int[][] mutationIndices = new int[numMutes][2];
        for (int i = 0; i < mutationIndices.length; i++) {
            mutationIndices[i][0] = getRandomIndex(genoLen, genotype, -1); // may prefer true bits
            mutationIndices[i][1] = getRandomIndex(genoLen, genotype, mutationIndices[i][0]); // may prefer false bits
        }
        return mutationIndices;
    }

    private void setValueAt(Object genotype, int i, Object val) {
        if (genotype instanceof BitSet) {
            ((BitSet) genotype).set(i, (Boolean) val);
        } else {
            ((int[]) genotype)[i] = (Integer) val;
        }
    }

    protected int getRandomIndex(int genoLen, Object genotype, int lastIndex) {
        return getRandomIndex(genoLen / 2, genotype, (lastIndex >= 0) ? (valueAt(genotype, lastIndex)) : null, 0, genoLen - 1);
    }

    /**
     * Select a random index within the given genotype lying in [iMin,iMax]. If applicable, the given
     * value is avoided with certain probility, namely by trying to find a different value for maxTries
     * times.
     *
     * @param maxTries
     * @param genotype
     * @param maybePreferedValue
     * @param iMin
     * @param iMax
     * @return
     */
    protected int getRandomIndex(int maxTries, Object genotype, Object avoidValue, int iMin, int iMax) {
        int k = RNG.randomInt(iMin, iMax);
        if (isPreferTrueChange() && avoidValue != null) {
            while ((avoidValue == valueAt(genotype, k)) && (maxTries >= 0)) {
                k = RNG.randomInt(iMin, iMax); // try next random position
                maxTries--;
            }
        }
        return k;
    }

//	private int getRandomSecondIndex(int firstIndex, AbstractEAIndividual individual) {
//	int genoLen = ((InterfaceGAIndividual)individual).getGenotypeLength();
//	return RNG.randomInt(0, genoLen);
//	}

    protected Object valueAt(Object genotype, int k) {
        if (genotype instanceof BitSet) {
            return ((BitSet) genotype).get(k);
        } else {
            return ((int[]) genotype)[k];
        }
    }

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "GA/GI swap values mutation";
    }

    /**********************************************************************************************************************
     * These are for GUI
     */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "GA-GI swap bits mutation";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This mutation operator swaps n random position pairs (bits or integers). The number of mutations is" +
                " chosen uniformly in a given interval.";
    }

    /**
     * This method allows you to set the number of mutations that occur in the
     * genotype.
     *
     * @param mutations The number of mutations.
     */
    public void setMinNumberOfMutations(int mutations) {
        if (mutations < 0) {
            mutations = 0;
        }
        this.minNumMutations = mutations;
    }

    public int getMinNumberOfMutations() {
        return this.minNumMutations;
    }

    public String minNumberOfMutationsTipText() {
        return "The minimum number of values to be swapped.";
    }

    public void setMaxNumberOfMutations(int mutations) {
        if (mutations < 0) {
            mutations = 0;
        }
        this.maxNumMutations = mutations;
    }

    public int getMaxNumberOfMutations() {
        return this.maxNumMutations;
    }

    public String maxNumberOfMutationsTipText() {
        return "The maximum number of values to be swapped.";
    }

    public void setPreferTrueChange(boolean preferPairs) {
        this.preferTrueChange = preferPairs;
    }

    public boolean isPreferTrueChange() {
        return preferTrueChange;
    }

    public String preferTrueChangeTipText() {
        return "If set to true, mutation events will prefer swapping non-equal values";
    }
}