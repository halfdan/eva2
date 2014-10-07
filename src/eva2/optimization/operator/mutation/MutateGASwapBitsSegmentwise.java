package eva2.optimization.operator.mutation;


import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.population.Population;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.math.RNG;

import java.util.BitSet;

/**
 * Swap two random bits of a GA individual within subsequences (segments) of fixed length.
 * If preferPairs is true, unequal pairs
 * are picked with some preference (by trying for >= s/2 times, where s is the binary
 * segment length).
 * Multiple mutations per segment can occur if the boolean switch is activated, meaning
 * that further mutations are performed recursively with p_mut. Thus, the probability
 * to perform k mutations per segment is (p_mut)^k. However, more than s mutations per segment will
 * never be performed.
 * <p/>
 * User: mkron
 * Date: 05.08.2004
 * Time: 17:45:36
 * To change this template use File | Settings | File Templates.
 */
public class MutateGASwapBitsSegmentwise implements InterfaceMutation, java.io.Serializable {
    private double mutationProbPerSegment = 0.7;
    private boolean multiplesPerSegment = false;
    private int segmentLength = 8;
    private boolean preferPairs = true; // if true, pairs of (1,0) are swapped with higher probability

    public MutateGASwapBitsSegmentwise() {

    }

    public MutateGASwapBitsSegmentwise(MutateGASwapBitsSegmentwise mutator) {
        this.mutationProbPerSegment = mutator.mutationProbPerSegment;
        this.setPreferTrueChange(mutator.isPreferTrueChange());
        this.multiplesPerSegment = mutator.multiplesPerSegment;
        this.segmentLength = mutator.segmentLength;
    }

    /**
     * A constructor setting all properties.
     *
     * @param p_mut
     * @param multPerSeg
     * @param segmentLen
     * @param prefPairs
     */
    public MutateGASwapBitsSegmentwise(double p_mut, boolean multPerSeg, int segmentLen, boolean prefPairs) {
        mutationProbPerSegment = p_mut;
        multiplesPerSegment = multPerSeg;
        segmentLength = segmentLen;
        preferPairs = prefPairs;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateGASwapBitsSegmentwise(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGASwapBitsSegmentwise) {
            MutateGASwapBitsSegmentwise mut = (MutateGASwapBitsSegmentwise) mutator;
            if (this.mutationProbPerSegment != mut.mutationProbPerSegment) {
                return false;
            }
            if (this.segmentLength != mut.segmentLength) {
                return false;
            }
            if (this.multiplesPerSegment != mut.multiplesPerSegment) {
                return false;
            }
            if (this.preferPairs != mut.preferPairs) {
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
     * doesn't implement InterfaceGAIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
//		System.out.println("Before Mutate: " +(individual).getStringRepresentation());
        if (individual instanceof InterfaceGAIndividual) {
            BitSet tmpBitSet = ((InterfaceGAIndividual) individual).getBGenotype();
            int genLen = ((InterfaceGAIndividual) individual).getGenotypeLength();
            for (int i = 0; i < genLen; i += segmentLength) {
                if (i + segmentLength > genLen) { // avoid to violate genotype length in a segment mutation
                    EVAERROR.errorMsgOnce("Warning, genotype length is not a multiple of the segment length.. ignoring last bits in " + this.getClass());
                    break;
                }
                if (RNG.flipCoin(mutationProbPerSegment)) {
                    int cntMutes = 0;
                    // swap bits within a segment within certain probability
                    do { // this may happen multiple times depending on the settings
                        swapBitsInSegmentAt(tmpBitSet, i, segmentLength);
                        cntMutes++;
                        // multiples only if the corresponding switch is true and another flipCoin succeeds.
                        // more than segmentLength mutations will never be performed per segment
                    } while (multiplesPerSegment && cntMutes < segmentLength && RNG.flipCoin(mutationProbPerSegment));
                }
            }
            ((InterfaceGAIndividual) individual).setBGenotype(tmpBitSet); // write back the genotype
        }
//		System.out.println("After Mutate:  " +(individual).getStringRepresentation());
    }

    /**
     * Swap one pair of bits within an indicated segment.
     *
     * @param tmpBitSet
     * @param i
     * @param segmentLength2
     */
    private void swapBitsInSegmentAt(BitSet bs, int i, int segLen) {
        int indexOne = getRandomIndex(bs, i, segLen, true); // may prefer true bits
        int indexTwo = getRandomIndex(bs, i, segLen, false); // may prefer false bits

        boolean tmpBit = bs.get(indexTwo);
        bs.set(indexTwo, bs.get(indexOne));
        bs.set(indexOne, tmpBit);
    }

    private int getRandomIndex(BitSet bs, int offset, int len, boolean maybePrefered) {
        int k = RNG.randomInt(offset, offset + len - 1);
        if (isPreferTrueChange()) {
            int maxTries = (1 + len) / 2;
            while (!(maybePrefered == bs.get(k)) && (maxTries >= 0)) {
                k = RNG.randomInt(offset, offset + len - 1);
                ; // try next random position
                maxTries--;
            }
        }
        return k;
    }

//	private int getRandomSecondIndex(int firstIndex, AbstractEAIndividual individual) {
//		int genoLen = ((InterfaceGAIndividual)individual).getGenotypeLength();
//		return RNG.randomInt(0, genoLen);
//	}

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "GA swap bits segment-wise mutation";
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
        return "GA swap bits segment-wise mutation";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This mutation operator swaps bits in subsegments of the genotype. Each segment is mutated" +
                " with a certain probability. Depending on the setting, multiple mutations per segment may occur.";
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

    public double getMutationProbPerSegment() {
        return mutationProbPerSegment;
    }

    public void setMutationProbPerSegment(double mutationProbPerSegment) {
        this.mutationProbPerSegment = mutationProbPerSegment;
    }

    public boolean isMultiplesPerSegment() {
        return multiplesPerSegment;
    }

    public void setMultiplesPerSegment(boolean multiplesPerSegment) {
        this.multiplesPerSegment = multiplesPerSegment;
    }

    public int getSegmentLength() {
        return segmentLength;
    }

    public void setSegmentLength(int segmentLength) {
        this.segmentLength = segmentLength;
    }

    public String segmentLengthTipText() {
        return "The length of sub-segments to regard (substrings of the GA genotype)";
    }
}