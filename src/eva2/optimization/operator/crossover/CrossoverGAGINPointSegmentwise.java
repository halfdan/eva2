package eva2.optimization.operator.crossover;

import eva2.tools.math.RNG;

/**
 * A variation of the GA n-point crossover. Restricts crossover to segment bounds
 * of fixed length, so crossings occur at multiples of the segment length only. Segments
 * will not be destroyed.
 * 
 * @author mkron
 *
 */
public class CrossoverGAGINPointSegmentwise extends CrossoverGAGINPoint {
	int segmentLength=8;
	
	public CrossoverGAGINPointSegmentwise() {
		super();
	}
	
	public CrossoverGAGINPointSegmentwise(CrossoverGAGINPointSegmentwise o) {
		super(o);
		this.segmentLength=o.segmentLength;
	}
	
	public CrossoverGAGINPointSegmentwise(int nPoints, int segmentLen) {
    	super(nPoints);
    	setSegmentLength(segmentLen);
    }
	
    @Override
	public Object clone() {
		return new CrossoverGAGINPointSegmentwise(this);
	}
	
	@Override
	public boolean equals(Object crossover) {
		if (super.equals(crossover) && (crossover instanceof CrossoverGAGINPointSegmentwise)) {
			return ((CrossoverGAGINPointSegmentwise)crossover).segmentLength==this.segmentLength;
		} else {
                return false;
            }
	}

	@Override
	protected int[] getCrossoverPoints(int length, int numberOfCrossovers) {
		int[] cPoints = new int[numberOfCrossovers];
		int i=0; 
		while (i<numberOfCrossovers && (i<length/segmentLength)) {
			cPoints[i]=segmentLength*RNG.randomInt(length/segmentLength);
			i++;
		}
		return cPoints;
	}

    @Override
	public String getName() {
        return "GA-GI N-Point segment-wise crossover";
    }
    
    public static String globalInfo() {
        return "This is an n-point crossover between m individuals which also splits at certain segment limits. Crossover points are selected from multiples of the segment length.";
    }

	public int getSegmentLength() {
		return segmentLength;
	}
	public void setSegmentLength(int segmentLength) {
		this.segmentLength = segmentLength;
	}
	public String segmentLengthTipText() {
		return "The fixed length of segments (genes) which are not split by crossover.";
	}

}
