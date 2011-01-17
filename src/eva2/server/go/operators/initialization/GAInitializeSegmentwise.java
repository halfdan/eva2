package eva2.server.go.operators.initialization;

import java.util.BitSet;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.math.RNG;

/**
 * An initialization method which sets a fixed number of bits per segment,
 * where a segment is a connected subsequence of the genotype.
 * This is usable for GA individuals only.
 * 
 * The initialization may be parameterized in two ways, where each takes a fixed
 * segment length s. Firstly, a fixed number of bits (k<=s) is set per segment,
 * so each segment has equal cardinality.
 * Secondly, an int-array can be specified which defines possibly varying k_i for
 * each segment i, so different segments may have different cardinaltiy. The array
 * must comply to the binary genotype length of the problem. The array definition
 * has strict priority over the fixed cardinality definition.  
 * 
 * @author mkron
 *
 */
public class GAInitializeSegmentwise implements InterfaceInitialization, java.io.Serializable {
	protected int[] bitsPerSegmentArray = new int[0];
	private int bitsPerSegment=1;
	private int segmentLength=4;
	
	public GAInitializeSegmentwise() {}
	
	public GAInitializeSegmentwise(GAInitializeSegmentwise o) {
		bitsPerSegment = o.bitsPerSegment;
		segmentLength = o.segmentLength;
		if (o.bitsPerSegmentArray!=null) {
			bitsPerSegmentArray = new int[o.bitsPerSegmentArray.length];
			System.arraycopy(o.bitsPerSegmentArray, 0, bitsPerSegmentArray, 0, bitsPerSegmentArray.length);
		}
	}
	
	public GAInitializeSegmentwise(int segLen, int[] bitsPerSeg) {
		segmentLength = segLen;
		bitsPerSegmentArray = bitsPerSeg;
	}
	
	public InterfaceInitialization clone() {
		return new GAInitializeSegmentwise(this);
	}
	
	@Override
	public void initialize(AbstractEAIndividual indy,
			InterfaceOptimizationProblem problem) {
		if (indy instanceof InterfaceGAIndividual) {
			InterfaceGAIndividual gaIndy =  ((InterfaceGAIndividual)indy);
			int genotypeLen = gaIndy.getGenotypeLength();
			BitSet genotype = gaIndy.getBGenotype();
			if (bitsPerSegmentArray==null || (bitsPerSegmentArray.length==0)) {
				// regard only a fixed number of bits per segment
				for (int i=0; i<genotypeLen; i+=segmentLength) {
					// create the next segment
					BitSet nextSeg=RNG.randomBitSet(bitsPerSegment, segmentLength);
					for (int k=i;k<i+segmentLength; k++) {
						// transfer the new segment to the genotype
						if (k<genotypeLen) genotype.set(k, nextSeg.get(k-i));
					}
				}
				// write back the genotype (it may have been cloned, who knows...)
				gaIndy.SetBGenotype(genotype);
			} else { // the number of bits to set may vary from segment to segment.
				if (bitsPerSegmentArray.length * segmentLength != genotypeLen) EVAERROR.errorMsgOnce("Warning, potential mismatch between segment lengths and genotype length in " + this.getClass());
				if (bitsPerSegmentArray.length * segmentLength < genotypeLen) System.err.println("Warning, " + (genotypeLen - bitsPerSegmentArray.length * segmentLength) + " bits will not be initialized!");
				for (int s=0; s<bitsPerSegmentArray.length; s++) {
					// look at each segment individually
					BitSet nextSeg=RNG.randomBitSet(bitsPerSegmentArray[s], bitsPerSegment);
					for (int k=(s)*bitsPerSegment; k<(s+1)*bitsPerSegment; k++) {
						if (k<genotypeLen) genotype.set(k, nextSeg.get(k-(s*bitsPerSegment)));
					}
				}
			}
		} else throw new RuntimeException("Error: "+ this.getClass() + " must be used with individuals of type " + InterfaceGAIndividual.class + "!");
	}

	
	public int[] getBitsPerSegmentArray() {
		return bitsPerSegmentArray;
	}
	public void setBitsPerSegmentArray(int[] bitsPerSegmentArray) {
		this.bitsPerSegmentArray = bitsPerSegmentArray;
	}
	public String bitsPerSegmentArrayTipText() {
		return "A value per segment defining the number of bits to set for that segment, or null if fixed";
	}

	public int getBitsPerSegment() {
		return bitsPerSegment;
	}
	public void setBitsPerSegment(int bitsPerSegment) {
		this.bitsPerSegment = bitsPerSegment;
	}
	public String bitsPerSegmentTipText() {
		return "If not array-wise defined, this fixed number of bits is set per segment";
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
	
	////////
	public String getName() {
		return "GA segment-wise init";
	}
	
	public String globalInfo() {
		return "A method which initializes a fixed number of bits per binary segment, which is a fixed number" +
				"of bits for substrings of equal length"; 
	}
}
