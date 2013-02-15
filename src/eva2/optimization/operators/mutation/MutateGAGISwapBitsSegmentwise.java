package eva2.optimization.operators.mutation;

import eva2.tools.EVAERROR;
import java.io.Serializable;

/**
 * This implementation restricts the swap positions of the standard swapping mutation
 * to swaps within subsequences (segments) of the genotype. The segments have a fixed length.
 * 
 * @author mkron
 *
 */
public class MutateGAGISwapBitsSegmentwise extends MutateGAGISwapBits implements Serializable {
	private int			segmentLength = 8;
	
	public MutateGAGISwapBitsSegmentwise() {
		super();
	}

	public MutateGAGISwapBitsSegmentwise(MutateGAGISwapBitsSegmentwise mutator) {
		super(mutator);
		segmentLength = mutator.segmentLength;
	}

	public MutateGAGISwapBitsSegmentwise(int segmentLength) {
		this();
		this.segmentLength = segmentLength;
	}
	
	public MutateGAGISwapBitsSegmentwise(int segmentLen, int minMutations, int maxMutations, boolean preferTrueChange) {
		super(minMutations, maxMutations, preferTrueChange);
		this.segmentLength = segmentLen;
	}
	
    @Override
	public Object clone() {
		return new MutateGAGISwapBitsSegmentwise(this);
	}
	
	public static String globalInfo() {
		return "Segment-wise swapping of elements - the mutation pairs are selected within the same" +
				" subsequence of the genotype.";
	}
	
	@Override
	public String getName() {
		return "GA-GI swap bits segment-wise mutation";
	}
	
	@Override
	protected int getRandomIndex(int genoLen, Object genotype, int lastIndex) {
		// restrict to the same segment of lastIndex if >= 0.
		int iMin=0, iMax=genoLen-1; // default bounds
		if (lastIndex>=0) {
			// select same segment
			iMin = segmentLength * ((int)(lastIndex/segmentLength));
			iMax = iMin+segmentLength-1;
			if (iMax>=genoLen) {
				EVAERROR.errorMsgOnce("Warning, the last segment exceeds the genotype length (so it is not a multiple of the genotype length");
				iMax=genoLen-1;
			}
		}
		return super.getRandomIndex(segmentLength/2, genotype, (lastIndex>=0) ? (valueAt(genotype, lastIndex)) : null,
				iMin, iMax);
	}

	public int getSegmentLength() {
		return segmentLength;
	}
	public void setSegmentLength(int segmentLength) {
		this.segmentLength = segmentLength;
	}
	public String segmentLengthTipText() {
		return "The length of subsequences to which mutations are restricted";
	}
}
