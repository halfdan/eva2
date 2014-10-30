package eva2.optimization.operator.initialization;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.individuals.InterfaceGIIndividual;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.math.RNG;
import eva2.util.annotation.Description;

import java.util.BitSet;

/**
 * An initialization method which sets a fixed number of specified target elements per segment,
 * where a segment is a connected subsequence of the genotype.
 * This is usable for binary and integer individuals only.
 * For binary individuals, this allows to control the number of bits per segment by
 * setting the target element to '1'. For
 * integer individuals, it allows to control the number of occurrences of a certain integer
 * per segment. It may also be used to initialize with subsets of integers (by setting 0
 * elements to a certain type and all to a subset of the range).
 * </p><p>
 * The initialization may be parameterized in two ways, where each takes a fixed
 * segment length s. Firstly, a fixed number of bits (k<=s) is set per segment,
 * so each segment has equal cardinality.
 * Secondly, an int-array can be specified which defines possibly varying k_i for
 * each segment i, so different segments may have different cardinality. The array
 * must comply to the binary genotype length of the problem. The array definition
 * has strict priority over the fixed cardinality definition.
 */
@Description("A method which initializes with a fixed number of occurences per segment, which is a fixed-length" +
        " substring of equal length. In the binary case, thus the cardinality of each segment can be predefined.")
public class GAGIInitializeSegmentwise implements InterfaceInitialization, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    protected int[] bitsPerSegmentArray = new int[0];
    private int bitsPerSegment = 1;
    private int segmentLength = 4;
    private int targetElement = 1;
    private int[] otherElements = new int[]{};
    private double disturbanceDegree = 0.0;

    public GAGIInitializeSegmentwise() {
    }

    public GAGIInitializeSegmentwise(GAGIInitializeSegmentwise o) {
        bitsPerSegment = o.bitsPerSegment;
        segmentLength = o.segmentLength;
        targetElement = o.targetElement;
        if (o.otherElements != null) {
            otherElements = new int[o.otherElements.length];
            System.arraycopy(o.otherElements, 0, otherElements, 0, otherElements.length);
        }
        if (o.bitsPerSegmentArray != null) {
            bitsPerSegmentArray = new int[o.bitsPerSegmentArray.length];
            System.arraycopy(o.bitsPerSegmentArray, 0, bitsPerSegmentArray, 0, bitsPerSegmentArray.length);
        }
        disturbanceDegree = o.disturbanceDegree;
    }

    public GAGIInitializeSegmentwise(int segLen, int bitsPerSeg, double disturbanceRatio) {
        segmentLength = segLen;
        bitsPerSegmentArray = new int[0];
        bitsPerSegment = bitsPerSeg;
        disturbanceDegree = disturbanceRatio;
    }

    public GAGIInitializeSegmentwise(int segLen, int[] bitsPerSegArr, double disturbanceRatio) {
        segmentLength = segLen;
        bitsPerSegmentArray = bitsPerSegArr;
        disturbanceDegree = disturbanceRatio;
    }

    public GAGIInitializeSegmentwise(int segLen, int[] bitsPerSeg) {
        this(segLen, bitsPerSeg, 0);
    }

    /**
     * Constructor for the integer case defining the target element and other elements.
     *
     * @param segLen
     * @param bitsPerSeg
     * @param targetElement
     * @param otherElements
     */
    public GAGIInitializeSegmentwise(int segLen, int[] bitsPerSeg, int targetElement, int[] otherElements, double disturbRatio) {
        segmentLength = segLen;
        bitsPerSegmentArray = bitsPerSeg;
        this.targetElement = targetElement;
        this.otherElements = otherElements;
        this.disturbanceDegree = disturbRatio;
    }

    @Override
    public InterfaceInitialization clone() {
        return new GAGIInitializeSegmentwise(this);
    }

    @Override
    public void initialize(AbstractEAIndividual indy,
                           InterfaceOptimizationProblem problem) {
        if (indy instanceof InterfaceGAIndividual || indy instanceof InterfaceGIIndividual) {
            int genotypeLen = -1;
            Object genotype = null;
            int[][] intRange = null; // may remain null in the binary case
            if (indy instanceof InterfaceGAIndividual) {
                genotypeLen = ((InterfaceGAIndividual) indy).getGenotypeLength();
                genotype = ((InterfaceGAIndividual) indy).getBGenotype();
            } else {
                genotypeLen = ((InterfaceGIIndividual) indy).getGenotypeLength();
                genotype = ((InterfaceGIIndividual) indy).getIGenotype();
                intRange = ((InterfaceGIIndividual) indy).getIntRange();
            }
            if (bitsPerSegmentArray == null || (bitsPerSegmentArray.length == 0)) {
                // regard only a fixed number of bits per segment
                for (int i = 0; i < genotypeLen; i += segmentLength) {
                    // create the next segment
                    BitSet nextSeg = RNG.randomBitSet(bitsPerSegment, segmentLength);
                    setNewVals(nextSeg, genotype, i, genotypeLen, intRange);

                }
            } else { // the number of bits to set may vary from segment to segment.
                if (bitsPerSegmentArray.length * segmentLength != genotypeLen) {
                    EVAERROR.errorMsgOnce("Warning, potential mismatch between segment lengths and genotype length in " + this.getClass());
                }
                if (bitsPerSegmentArray.length * segmentLength < genotypeLen) {
                    System.err.println("Warning, " + (genotypeLen - bitsPerSegmentArray.length * segmentLength) + " bits will not be initialized!");
                }
                for (int s = 0; s < bitsPerSegmentArray.length; s++) {
                    // look at each segment individually
                    BitSet nextSeg = RNG.randomBitSet(bitsPerSegmentArray[s], segmentLength);
                    setNewVals(nextSeg, genotype, s * segmentLength, genotypeLen, intRange);
//					for (int k=(s)*segmentLength; k<(s+1)*segmentLength; k++) {
//						if (k<genotypeLen) genotype.set(k, nextSeg.get(k-(s*segmentLength)));
//					}
                }
            }
            if (disturbanceDegree > 0) {
                disturb(indy, genotype, genotypeLen, intRange);
            }

            // write back the genotype (it may have been cloned, who knows...)
            if (indy instanceof InterfaceGAIndividual) {
                ((InterfaceGAIndividual) indy).setBGenotype((BitSet) genotype);
            } else {
                ((InterfaceGIIndividual) indy).setIGenotype((int[]) genotype);
            }
//			System.out.println(BeanInspector.toString(genotype));
        } else {
            throw new RuntimeException("Error: " + this.getClass() + " must be used with binary or integer individuals!");
        }
    }

    private void disturb(AbstractEAIndividual indy, Object genotype, int genotypeLen, int[][] intRange) {
        for (int i = 0; i < genotypeLen; i++) {
            if (RNG.flipCoin(disturbanceDegree)) {
                setRandomValue(i, genotype, intRange);
            }
        }
    }

    private void setRandomValue(int i, Object genotype, int[][] range) {
        if (genotype instanceof BitSet) {
            BitSet geno = (BitSet) genotype;
            geno.set(i, RNG.randomBoolean());
        } else if (genotype instanceof int[]) {
            int[] geno = (int[]) genotype;
            if (otherElements.length > 0) { // may choose between target and all other elements
                int rnd = RNG.randomInt(0, otherElements.length); // between 0 and length (inclusively)
                if (rnd == otherElements.length) {
                    geno[i] = targetElement;
                } else {
                    geno[i] = otherElements[rnd];
                }
            } else { // or choose a random int within the range
                if (range == null) {
                    System.err.println("Error, missing int range to perform random disturbance in " + this.getClass());
                }
                geno[i] = RNG.randomInt(range[i][0], range[i][1]);
            }
        }
    }

    /**
     * Treat both the binary and the integer case.
     *
     * @param nextSeg
     * @param genotype
     * @param offset
     * @param maxLen
     * @param intRange
     */
    private void setNewVals(BitSet nextSeg, Object genotype, int offset, int maxLen, int[][] intRange) {
        // the bits in the nextSeg bitset define the structure of the next segment starting at the offset.
        // maxLen is the length of the full data type.
        for (int k = offset; k < offset + segmentLength; k++) {
            // transfer the new segment to the genotype
            if (k < maxLen) {
                if (genotype instanceof BitSet) { // binary case
                    ((BitSet) genotype).set(k, getBoolVal(nextSeg.get(k - offset)));
                } else if (genotype instanceof int[]) { // integer case
                    ((int[]) genotype)[k] = getIntVal(nextSeg.get(k - offset), k, intRange);
                }
            }
        }
    }

    /**
     * Return the integer value selected at the given position.
     *
     * @param initBit
     * @param pos
     * @param range
     * @return
     */
    private int getIntVal(boolean initBit, int pos, int[][] range) {
        // if true, the target value is returned, otherwise the non-target value
        // or a random value in range
        if (initBit) {
            return targetElement;
        } else {
            if ((otherElements == null) || (otherElements.length == 0)) { // all but the one to set
                int rangeLenMinusOne = range[pos][1] - range[pos][0]; // bounds are included, so one is missing
                int newVal = RNG.randomInt(rangeLenMinusOne); // a random integer with one missing (the largest)
                if (newVal >= targetElement) {
                    newVal++;
                } // make sure the elementToSet is not returned but all others may be, including the largest
                return newVal;
            } else {
                // select one randomly from the array
                int k = RNG.randomInt(otherElements.length);
                return otherElements[k];
            }
        }
    }

    private boolean getBoolVal(boolean initBit) {
        // the new value is TRUE if the nextSeg-bit is set and 1 is set as target value,
        // otherwise FALSE (if target value is 0) -- thus target value=1 and
        // bitsPerSegment=k is equivalent to target value=0 and bitsPerSegment=segmentLength-k
        if (initBit) {
            return (targetElement == 1);
        } else {
            return (targetElement != 1);
        }
    }

    public int[] getTargetElementsPerSegmentArray() {
        return bitsPerSegmentArray;
    }

    public void setTargetElementsPerSegmentArray(int[] bitsPerSegmentArray) {
        this.bitsPerSegmentArray = bitsPerSegmentArray;
    }

    public String targetElementsPerSegmentArrayTipText() {
        return "A value per segment defining the number of target elements to set for that segment, or null if fixed";
    }

    public int getTargetElementsPerSegment() {
        return bitsPerSegment;
    }

    public void setTargetElementsPerSegment(int bitsPerSegment) {
        this.bitsPerSegment = bitsPerSegment;
    }

    public String targetElementsPerSegmentTipText() {
        return "If not defined as an array, this fixed number of target elements is set per segment";
    }

    public int getSegmentLength() {
        return segmentLength;
    }

    public void setSegmentLength(int segmentLength) {
        this.segmentLength = segmentLength;
    }

    public String segmentLengthTipText() {
        return "The fixed length of a segment, which is a substring of the binary genotype";
    }

    public String getName() {
        return "GA-GI segment-wise initialize";
    }

    public int getTargetElement() {
        return targetElement;
    }

    public void setTargetElement(int elementToSet) {
        this.targetElement = elementToSet;
    }

    public String targetElementTipText() {
        return "The element to set in a defined number per segment";
    }

    public int[] getOtherElements() {
        return otherElements;
    }

    public void setOtherElements(int[] elementsNotToSet) {
        this.otherElements = elementsNotToSet;
    }

    public String otherElementsTipText() {
        return "Set of elements at the rest of instances among which is chosen randomly - if empty, all allowed ones except for the elementToSet are used.";
    }
}
